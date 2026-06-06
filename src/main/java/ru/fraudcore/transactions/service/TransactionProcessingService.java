package ru.fraudcore.transactions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fraudcore.audit.entity.AuditAction;
import ru.fraudcore.audit.service.AuditService;
import ru.fraudcore.cases.entity.FraudCase;
import ru.fraudcore.cases.service.FraudCaseService;
import ru.fraudcore.common.transaction.AfterCommitExecutor;
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
import java.util.concurrent.CompletableFuture;

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
    private final AfterCommitExecutor afterCommitExecutor;

    @Transactional
    public void processTransactionCreatedEvent(TransactionCreatedEvent event) {
        auditService.log(null, AuditAction.KAFKA_EVENT_CONSUMED, "TRANSACTION", event.transactionId(), "Прочитано из topic=transaction.created");
        metricsService.incrementKafkaConsumed();

        Transaction transaction = transactionRepository.findById(event.transactionId()).orElse(null);
        if (transaction == null) {
            log.error("Транзакция {} из события не найдена", event.transactionId());
            metricsService.incrementKafkaConsumerErrors();
            throw new IllegalStateException("Транзакция из события не найдена: " + event.transactionId());
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
        Long transactionId = transaction.getId();
        afterCommitExecutor.execute(() -> handlePublication(
                transactionScoredEventProducer.publish(scoredEvent),
                "TransactionScoredEvent",
                "TRANSACTION",
                transactionId,
                "Опубликовано в topic=transaction.scored"
        ));

        if (createdCase != null) {
            FraudCaseCreatedEvent caseCreatedEvent = new FraudCaseCreatedEvent(
                    UUID.randomUUID(),
                    createdCase.getId(),
                    transactionId,
                    createdCase.getRiskScore(),
                    createdCase.getRiskLevel().name(),
                    LocalDateTime.now()
            );
            Long caseId = createdCase.getId();
            afterCommitExecutor.execute(() -> handlePublication(
                    fraudCaseCreatedEventProducer.publish(caseCreatedEvent),
                    "FraudCaseCreatedEvent",
                    "CASE",
                    caseId,
                    "Опубликовано в topic=fraud.case.created"
            ));
        }
    }

    private void handlePublication(
            CompletableFuture<?> publication,
            String eventName,
            String entityType,
            Long entityId,
            String auditDetails
    ) {
        publication.whenComplete((result, publishError) -> {
            if (publishError != null) {
                log.error("Не удалось опубликовать {} для {}Id={}", eventName, entityType.toLowerCase(), entityId, publishError);
                return;
            }
            try {
                auditService.log(null, AuditAction.KAFKA_EVENT_PUBLISHED, entityType, entityId, auditDetails);
            } catch (Exception auditError) {
                log.error("{} опубликовано, но не удалось записать AuditLog для {}Id={}",
                        eventName, entityType.toLowerCase(), entityId, auditError);
            }
        });
    }
}
