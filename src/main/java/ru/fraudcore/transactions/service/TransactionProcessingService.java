package ru.fraudcore.transactions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fraudcore.audit.entity.AuditAction;
import ru.fraudcore.audit.service.AuditService;
import ru.fraudcore.cases.entity.FraudCase;
import ru.fraudcore.cases.service.FraudCaseService;
import ru.fraudcore.kafka.event.FraudCaseCreatedEvent;
import ru.fraudcore.kafka.event.TransactionCreatedEvent;
import ru.fraudcore.kafka.event.TransactionScoredEvent;
import ru.fraudcore.kafka.producer.FraudCaseCreatedEventProducer;
import ru.fraudcore.kafka.producer.TransactionScoredEventProducer;
import ru.fraudcore.metrics.service.FraudMetricsService;
import ru.fraudcore.scoring.dto.ScoringResult;
import ru.fraudcore.scoring.entity.RiskRuleResult;
import ru.fraudcore.scoring.repository.RiskRuleResultRepository;
import ru.fraudcore.scoring.service.ScoringService;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessingService {

    private final TransactionRepository transactionRepository;
    private final ScoringService scoringService;
    private final RiskRuleResultRepository riskRuleResultRepository;
    private final FraudCaseService fraudCaseService;
    private final TransactionScoredEventProducer transactionScoredEventProducer;
    private final FraudCaseCreatedEventProducer fraudCaseCreatedEventProducer;
    private final AuditService auditService;
    private final FraudMetricsService metricsService;

    @Transactional
    public void processTransactionCreatedEvent(TransactionCreatedEvent event) {
        auditService.log(null, AuditAction.KAFKA_EVENT_CONSUMED, "TRANSACTION", event.transactionId(), "Прочитано из topic=transaction.created");
        metricsService.incrementKafkaConsumed();

        Transaction transaction = transactionRepository.findById(event.transactionId()).orElse(null);
        if (transaction == null) {
            log.error("Транзакция {} из события не найдена", event.transactionId());
            metricsService.incrementKafkaConsumerErrors();
            return;
        }

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.info("Пропуск обработки транзакции {}, так как статус {}", transaction.getId(), transaction.getStatus());
            return;
        }

        ScoringResult scoringResult = scoringService.score(transaction);
        metricsService.recordRiskScore(scoringResult.riskScore());

        scoringResult.ruleResults().forEach(rule -> riskRuleResultRepository.save(
                RiskRuleResult.builder()
                        .transaction(transaction)
                        .ruleCode(rule.ruleCode())
                        .points(rule.points())
                        .description(rule.description())
                        .build()
        ));

        transaction.setRiskScore(scoringResult.riskScore());
        transaction.setRiskLevel(scoringResult.riskLevel());
        transaction.setProcessedAt(LocalDateTime.now());

        FraudCase createdCase = null;
        if (scoringResult.riskScore() < 60) {
            transaction.setStatus(TransactionStatus.APPROVED);
            auditService.log(null, AuditAction.TRANSACTION_APPROVED, "TRANSACTION", transaction.getId(), "Автоматически одобрена после скоринга");
            metricsService.incrementTransactionsApproved();
        } else {
            transaction.setStatus(TransactionStatus.TEMPORARILY_BLOCKED);
            auditService.log(null, AuditAction.TRANSACTION_TEMPORARILY_BLOCKED, "TRANSACTION", transaction.getId(), "Временно заблокирована после скоринга");
            metricsService.incrementTransactionsBlocked();
            createdCase = fraudCaseService.createForBlockedTransaction(transaction, scoringResult.riskScore(), scoringResult.riskLevel());
        }

        transactionRepository.save(transaction);

        TransactionScoredEvent scoredEvent = new TransactionScoredEvent(
                UUID.randomUUID(),
                transaction.getId(),
                scoringResult.riskScore(),
                scoringResult.riskLevel().name(),
                transaction.getStatus().name(),
                LocalDateTime.now()
        );
        transactionScoredEventProducer.publish(scoredEvent);
        auditService.log(null, AuditAction.KAFKA_EVENT_PUBLISHED, "TRANSACTION", transaction.getId(), "Опубликовано в topic=transaction.scored");

        if (createdCase != null) {
            FraudCaseCreatedEvent caseCreatedEvent = new FraudCaseCreatedEvent(
                    UUID.randomUUID(),
                    createdCase.getId(),
                    transaction.getId(),
                    createdCase.getRiskScore(),
                    createdCase.getRiskLevel().name(),
                    LocalDateTime.now()
            );
            fraudCaseCreatedEventProducer.publish(caseCreatedEvent);
            auditService.log(null, AuditAction.KAFKA_EVENT_PUBLISHED, "CASE", createdCase.getId(), "Опубликовано в topic=fraud.case.created");
        }
    }
}
