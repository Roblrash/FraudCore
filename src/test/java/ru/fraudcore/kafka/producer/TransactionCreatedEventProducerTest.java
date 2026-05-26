package ru.fraudcore.kafka.producer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.fraudcore.config.KafkaTopicsProperties;
import ru.fraudcore.kafka.event.TransactionCreatedEvent;
import ru.fraudcore.metrics.service.FraudMetricsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionCreatedEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private FraudMetricsService metricsService;

    @Test
    void shouldPublishEventToTopic() {
        KafkaTopicsProperties topics = new KafkaTopicsProperties();
        topics.setTransactionCreated("transaction.created");

        TransactionCreatedEventProducer producer = new TransactionCreatedEventProducer(kafkaTemplate, topics, metricsService);
        TransactionCreatedEvent event = new TransactionCreatedEvent(UUID.randomUUID(), 1L, "tx-1", "c1",
                new BigDecimal("100"), "RUB", "TRANSFER", LocalDateTime.now());

        producer.publish(event);

        verify(kafkaTemplate).send("transaction.created", "1", event);
        verify(metricsService).incrementKafkaPublished();
    }
}
