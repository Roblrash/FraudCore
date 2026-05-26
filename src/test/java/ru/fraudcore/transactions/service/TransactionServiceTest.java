package ru.fraudcore.transactions.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fraudcore.audit.service.AuditService;
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

import static org.assertj.core.api.Assertions.assertThat;
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
                metricsService
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

        var response = service.createTransaction(request);

        assertThat(response.status()).isEqualTo(TransactionStatus.PENDING);
        verify(eventProducer, times(1)).publish(any());
        verify(auditService, atLeastOnce()).log(any(), any(), any(), any(), any());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.PENDING);
    }
}
