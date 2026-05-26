package ru.fraudcore.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.fraudcore.config.KafkaTopicsProperties;
import ru.fraudcore.kafka.event.TransactionScoredEvent;
import ru.fraudcore.metrics.service.FraudMetricsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionScoredEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;
    private final FraudMetricsService metricsService;

    public void publish(TransactionScoredEvent event) {
        kafkaTemplate.send(topics.getTransactionScored(), event.transactionId().toString(), event);
        metricsService.incrementKafkaPublished();
        log.info("Опубликовано событие TransactionScoredEvent для transactionId={}", event.transactionId());
    }
}
