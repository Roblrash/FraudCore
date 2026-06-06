package ru.fraudcore.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.fraudcore.config.KafkaTopicsProperties;
import ru.fraudcore.kafka.event.TransactionCreatedEvent;
import ru.fraudcore.metrics.service.FraudMetricsService;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCreatedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;
    private final FraudMetricsService metricsService;

    public CompletableFuture<SendResult<String, Object>> publish(TransactionCreatedEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topics.getTransactionCreated(), event.transactionId().toString(), event);
        future.thenAccept(result -> {
            metricsService.incrementKafkaPublished();
            log.info("Опубликовано событие TransactionCreatedEvent для transactionId={}", event.transactionId());
        });
        return future;
    }
}
