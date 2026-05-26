package ru.fraudcore.kafka.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fraudcore.kafka.event.TransactionCreatedEvent;
import ru.fraudcore.metrics.service.FraudMetricsService;
import ru.fraudcore.transactions.service.TransactionProcessingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionCreatedEventConsumerTest {

    @Mock
    private TransactionProcessingService processingService;
    @Mock
    private FraudMetricsService metricsService;

    @Test
    void shouldDelegateEventToProcessingService() {
        TransactionCreatedEventConsumer consumer = new TransactionCreatedEventConsumer(processingService, metricsService);
        TransactionCreatedEvent event = new TransactionCreatedEvent(UUID.randomUUID(), 1L, "tx-1", "c1",
                new BigDecimal("100"), "RUB", "TRANSFER", LocalDateTime.now());

        consumer.consume(event);

        verify(processingService).processTransactionCreatedEvent(event);
    }
}
