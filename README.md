# FraudCore

## 1. Описание проекта

**FraudCore** — backend-сервис антифрод-мониторинга банковских транзакций на Java 21 + Spring Boot 3.

Сервис принимает транзакции через REST API, сохраняет их со статусом `PENDING`, публикует событие в Kafka и асинхронно выполняет скоринг в consumer. Подозрительные операции временно блокируются, для них создаются кейсы аналитиков.

## 2. Бизнес-сценарий

1. Система получает транзакцию.
2. Транзакция сохраняется как `PENDING`.
3. Публикуется `TransactionCreatedEvent` в `transaction.created`.
4. Consumer применяет набор fraud-правил.
5. Если риск низкий — `APPROVED`.
6. Если риск высокий — `TEMPORARILY_BLOCKED` и создаётся `FraudCase`.
7. Аналитик берёт кейс, принимает решение и закрывает кейс.

## 3. Основной функционал

- Регистрация и авторизация аналитика (JWT).
- Создание транзакции и асинхронный скоринг через Kafka.
- Explainable scoring (`RiskRuleResult`) по каждой сработавшей причине.
- Управление fraud-кейсами (`assign-to-me`, `decision`).
- Аудит действий (`AuditLog`).
- Dashboard-статистика.
- Метрики Prometheus + Grafana.

## 4. Технологический стек

- Java 21
- Spring Boot 3
- Spring Web, Spring Data JPA, Hibernate
- PostgreSQL
- Flyway
- Spring Validation
- Spring Security + JWT
- Lombok, MapStruct
- Springdoc OpenAPI
- Apache Kafka + Spring Kafka
- JUnit 5, Mockito, Testcontainers
- Docker Compose
- Actuator, Micrometer, Prometheus, Grafana

## 5. Архитектура и почему модульный монолит

Проект реализован как **модульный монолит**: единое Spring Boot приложение, разделённое на бизнес-модули (`auth`, `transactions`, `scoring`, `cases`, `audit`, `dashboard`, `kafka`).

Почему так:

- Простота деплоя и разработки.
- Чёткие границы доменов в коде.
- Kafka демонстрирует event-driven подход без усложнения микросервисами.

## 6. Как используется Kafka

Kafka используется внутри одного backend-приложения для асинхронной обработки:

- REST слой только принимает транзакцию и публикует событие.
- Consumer запускает скоринг и обновляет финальный статус.

### Kafka topics

- `transaction.created`
- `transaction.scored`
- `fraud.case.created`
- `fraud.case.closed`

### Kafka events

- `TransactionCreatedEvent`
- `TransactionScoredEvent`
- `FraudCaseCreatedEvent`
- `FraudCaseClosedEvent`

## 7. Жизненный цикл транзакции

1. `POST /api/v1/transactions`
2. Save with `PENDING`
3. Publish `transaction.created`
4. Consume event
5. Run scoring rules
6. Save `RiskRuleResult`
7. Update status:
   - `< 60` -> `APPROVED`
   - `>= 60` -> `TEMPORARILY_BLOCKED`
8. Publish `transaction.scored`
9. If blocked -> create case + publish `fraud.case.created`

## 8. Жизненный цикл fraud case

1. Создан со статусом `NEW`.
2. Аналитик берёт в работу -> `IN_PROGRESS`.
3. Аналитик принимает решение:
   - `APPROVE_TRANSACTION` -> транзакция `APPROVED_BY_ANALYST`
   - `DECLINE_TRANSACTION` -> транзакция `DECLINED_BY_ANALYST`
4. Кейс закрывается (`CLOSED`) и публикуется `fraud.case.closed`.

## 9. Risk scoring (Strategy Pattern)

Правила:

- `HIGH_AMOUNT` (+30)
- `NIGHT_OPERATION` (+20)
- `NEW_RECIPIENT` (+25)
- `HIGH_FREQUENCY` (+20)
- `UNUSUAL_LOCATION` (+15)
- `SUSPICIOUS_MERCHANT` (+25)

Score ограничен 100.

- 0-39 `LOW`
- 40-59 `MEDIUM`
- 60-79 `HIGH`
- 80-100 `CRITICAL`

## 10. API (основные группы)

- Auth: `/api/v1/auth/register`, `/api/v1/auth/login`
- Users: `/api/v1/users/me`
- Transactions: create/list/get/risk-explanation
- Cases: list/get/assign/decision
- Audit: transaction/case history
- Dashboard: `/api/v1/dashboard`

## 11. Безопасность

- JWT bearer auth.
- Публичные endpoint:
  - `POST /api/v1/auth/register`
  - `POST /api/v1/auth/login`
  - `/swagger-ui/**`
  - `/v3/api-docs/**`
  - `/actuator/health`
- Остальные endpoint защищены.

## 12. База данных

Flyway миграция: `src/main/resources/db/migration/V1__init_schema.sql`

Таблицы:

- `users`
- `transactions`
- `fraud_cases`
- `risk_rule_results`
- `audit_logs`

Индексы:

- `users.email` (unique)
- `transactions.external_id` (unique)
- `transactions.client_id`
- `transactions.status`
- `transactions.created_at`
- `fraud_cases.status`
- `fraud_cases.risk_level`
- `fraud_cases.assigned_analyst_id`
- `audit_logs(entity_type, entity_id)`

## 13. Структура проекта

```text
src/main/java/ru/fraudcore
+-- auth
+-- users
+-- transactions
+-- scoring
+-- cases
+-- audit
+-- dashboard
+-- kafka
+-- metrics
+-- common
L-- config
```

## 14. Запуск через Docker Compose

```bash
docker compose up --build
```

Остановка:

```bash
docker compose down
```

Сервисы:

- App: `http://localhost:8080`
- PostgreSQL: `localhost:5432`
- Kafka: `localhost:9092`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (`admin/admin`)

## 15. Локальный запуск

Требуется:

- Java 21
- Maven 3.9+
- PostgreSQL
- Kafka

```bash
mvn clean spring-boot:run
```

## 16. Swagger / OpenAPI

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## 17. Prometheus и Grafana

- Prometheus: [http://localhost:9090](http://localhost:9090)
- Grafana: [http://localhost:3000](http://localhost:3000)

Ключевые метрики:

- `fraud_transactions_received_total`
- `fraud_transactions_approved_total`
- `fraud_transactions_blocked_total`
- `fraud_cases_created_total`
- `fraud_cases_closed_total`
- `fraud_case_decision_time_seconds`
- `fraud_risk_score_average`
- `fraud_kafka_events_published_total`
- `fraud_kafka_events_consumed_total`
- `fraud_kafka_consumer_errors_total`

## 18. Запуск тестов

```bash
mvn test
```

Интеграционные тесты используют:

- Testcontainers PostgreSQL
- KafkaContainer

## 19. Примеры curl

Регистрация:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"analyst@example.com","password":"password123","fullName":"Ivan Ivanov"}'
```

Логин:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"analyst@example.com","password":"password123"}'
```

Создание транзакции:

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId":"tx-10001",
    "clientId":"client-777",
    "clientFullName":"Ivan Petrov",
    "clientPhone":"+79991234567",
    "amount":150000,
    "currency":"RUB",
    "type":"TRANSFER",
    "merchant":null,
    "recipient":"Unknown Recipient",
    "country":"RU",
    "city":"Moscow",
    "createdAt":"2026-05-26T02:30:00"
  }'
```

## 20. Проверка асинхронной Kafka-обработки

1. Создайте транзакцию через `POST /api/v1/transactions`.
2. Убедитесь, что в ответе статус `PENDING`.
3. Запросите `GET /api/v1/transactions/{id}` через несколько секунд.
4. Проверьте финальный статус `APPROVED` или `TEMPORARILY_BLOCKED`.
5. Для blocked-транзакции проверьте создание кейса в `GET /api/v1/cases`.
6. В логах приложения проверьте publish/consume событий.

## 21. Что можно улучшить

- Outbox pattern для гарантированной доставки событий.
- Retry/DLT policy для Kafka consumer.
- Расширенные fraud rules и конфигурация rule weights из БД.
- Fine-grained observability dashboards в Grafana.
- Нагрузочное тестирование и профили производительности.
