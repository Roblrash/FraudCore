package ru.fraudcore.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import ru.fraudcore.cases.repository.FraudCaseRepository;
import ru.fraudcore.common.exception.ConflictException;
import ru.fraudcore.scoring.repository.RiskRuleResultRepository;
import ru.fraudcore.transactions.dto.CreateTransactionRequest;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.entity.TransactionType;
import ru.fraudcore.transactions.repository.TransactionRepository;
import ru.fraudcore.transactions.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class FraudCoreIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("fraudcore")
            .withUsername("fraudcore")
            .withPassword("fraudcore");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private FraudCaseRepository fraudCaseRepository;
    @Autowired
    private RiskRuleResultRepository riskRuleResultRepository;

    @Test
    void shouldProcessKafkaFlowAndCreateCaseForHighRiskTransaction() throws InterruptedException {
        var lowRisk = new CreateTransactionRequest(
                "itx-low-1",
                "client-1",
                "Ivan Petrov",
                "+79991234567",
                new BigDecimal("1000"),
                "RUB",
                TransactionType.TRANSFER,
                "Normal Shop",
                "Trusted",
                "RU",
                "Moscow",
                LocalDateTime.of(2026, 5, 26, 12, 0)
        );

        var highRisk = new CreateTransactionRequest(
                "itx-high-1",
                "client-2",
                "Petr Ivanov",
                "+79991234568",
                new BigDecimal("150000"),
                "RUB",
                TransactionType.TRANSFER,
                "Bad Merchant",
                "Unknown Recipient",
                "RU",
                "Moscow",
                LocalDateTime.of(2026, 5, 26, 2, 30)
        );

        Long lowId = transactionService.createTransaction(lowRisk).id();
        Long highId = transactionService.createTransaction(highRisk).id();

        waitForProcessed(lowId);
        waitForProcessed(highId);

        var lowTx = transactionRepository.findById(lowId).orElseThrow();
        var highTx = transactionRepository.findById(highId).orElseThrow();

        assertThat(lowTx.getStatus()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(highTx.getStatus()).isEqualTo(TransactionStatus.TEMPORARILY_BLOCKED);
        assertThat(fraudCaseRepository.existsByTransactionId(highId)).isTrue();
        assertThat(riskRuleResultRepository.findAllByTransactionId(highId)).isNotEmpty();

        var page = transactionService.findAll("client-2", null, null, null, null, null,
                null, null, "createdAt", "desc", 0, 10);
        assertThat(page.content()).hasSize(1);
    }

    @Test
    void shouldRejectDuplicateExternalId() {
        var request = new CreateTransactionRequest(
                "itx-dup-1",
                "client-3",
                "Test User",
                null,
                new BigDecimal("5000"),
                "RUB",
                TransactionType.CARD_PAYMENT,
                null,
                "Recipient",
                "RU",
                "Moscow",
                LocalDateTime.of(2026, 5, 26, 14, 0)
        );

        transactionService.createTransaction(request);
        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(ConflictException.class);
    }

    private void waitForProcessed(Long transactionId) throws InterruptedException {
        for (int i = 0; i < 40; i++) {
            var tx = transactionRepository.findById(transactionId).orElseThrow();
            if (tx.getStatus() != TransactionStatus.PENDING) {
                return;
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException("Transaction was not processed in time: " + transactionId);
    }
}
