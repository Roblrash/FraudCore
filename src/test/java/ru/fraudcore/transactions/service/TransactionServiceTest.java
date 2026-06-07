package ru.fraudcore.transactions.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.fraudcore.audit.service.AuditService;
import ru.fraudcore.common.exception.BadRequestException;
import ru.fraudcore.common.transaction.AfterCommitExecutor;
import ru.fraudcore.kafka.producer.TransactionCreatedEventProducer;
import ru.fraudcore.metrics.service.FraudMetricsService;
import ru.fraudcore.scoring.repository.RiskRuleResultRepository;
import ru.fraudcore.transactions.dto.CreateTransactionRequest;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.entity.TransactionType;
import ru.fraudcore.transactions.mapper.TransactionMapper;
import ru.fraudcore.transactions.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionCreatedEventProducer eventProducer;
    @Mock
    private AuditService auditService;
    @Mock
    private RiskRuleResultRepository riskRuleResultRepository;
    @Mock
    private FraudMetricsService metricsService;

    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    void shouldCreatePendingTransactionAndPublishEvent() {
        TransactionService service = new TransactionService(
                transactionRepository,
                mapper,
                eventProducer,
                auditService,
                riskRuleResultRepository,
                metricsService,
                new AfterCommitExecutor()
        );

        CreateTransactionRequest request = new CreateTransactionRequest(
                "tx-1", "c1", "Ivan", "+7999", new BigDecimal("150000"), "RUB", TransactionType.TRANSFER,
                null, "Unknown Recipient", "RU", "Moscow", LocalDateTime.of(2026, 5, 26, 2, 30)
        );

        when(transactionRepository.existsByExternalId("tx-1")).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(1L);
            return tx;
        });
        when(eventProducer.publish(any())).thenReturn(CompletableFuture.completedFuture(null));

        var response = service.createTransaction(request);

        assertThat(response.status()).isEqualTo(TransactionStatus.PENDING);
        verify(eventProducer, times(1)).publish(any());
        verify(auditService, atLeastOnce()).log(any(), any(), any(), any(), any());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void shouldSortRiskLevelByNumericRiskScore() {
        TransactionService service = new TransactionService(
                transactionRepository,
                mapper,
                eventProducer,
                auditService,
                riskRuleResultRepository,
                metricsService,
                new AfterCommitExecutor()
        );
        when(transactionRepository.findAll(
                anyTransactionSpecification(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        service.findAll(
                null, null, null, null, null,
                null, null, null, null,
                "riskLevel", "desc", 0, 20
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepository).findAll(anyTransactionSpecification(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("riskScore")).isNotNull();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("riskLevel")).isNull();
    }

    @Test
    void shouldRejectInvalidFilterRanges() {
        TransactionService service = new TransactionService(
                transactionRepository,
                mapper,
                eventProducer,
                auditService,
                riskRuleResultRepository,
                metricsService,
                new AfterCommitExecutor()
        );

        assertThatThrownBy(() -> service.findAll(
                null, null, null, null, null,
                LocalDateTime.of(2026, 6, 8, 0, 0),
                LocalDateTime.of(2026, 6, 7, 0, 0),
                null, null, "createdAt", "desc", 0, 20
        )).isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> service.findAll(
                null, null, null, null, null,
                null, null,
                new BigDecimal("100"), new BigDecimal("10"),
                "createdAt", "desc", 0, 20
        )).isInstanceOf(BadRequestException.class);
    }

    private Specification<Transaction> anyTransactionSpecification() {
        return any();
    }
}
