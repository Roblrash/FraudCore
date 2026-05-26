package ru.fraudcore.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.fraudcore.kafka.event.TransactionCreatedEvent;
import ru.fraudcore.metrics.service.FraudMetricsService;
import ru.fraudcore.transactions.service.TransactionProcessingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCreatedEventConsumer {

    private final TransactionProcessingService transactionProcessingService;
    private final FraudMetricsService metricsService;

    @KafkaListener(topics = "${fraudcore.kafka.topics.transaction-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(TransactionCreatedEvent event) {
        log.info("Получено событие TransactionCreatedEvent transactionId={}", event.transactionId());
        try {
            transactionProcessingService.processTransactionCreatedEvent(event);
        } catch (Exception e) {
            metricsService.incrementKafkaConsumerErrors();
            log.error("Ошибка обработки события TransactionCreatedEvent transactionId={}", event.transactionId(), e);
            throw e;
        }
    }
}
