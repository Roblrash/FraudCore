package ru.fraudcore.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.fraudcore.config.KafkaTopicsProperties;
import ru.fraudcore.kafka.event.TransactionCreatedEvent;
import ru.fraudcore.metrics.service.FraudMetricsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCreatedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;
    private final FraudMetricsService metricsService;

    public void publish(TransactionCreatedEvent event) {
        kafkaTemplate.send(topics.getTransactionCreated(), event.transactionId().toString(), event);
        metricsService.incrementKafkaPublished();
        log.info("Опубликовано событие TransactionCreatedEvent для transactionId={}", event.transactionId());
    }
}
