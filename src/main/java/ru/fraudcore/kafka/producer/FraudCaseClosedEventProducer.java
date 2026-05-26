package ru.fraudcore.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.fraudcore.config.KafkaTopicsProperties;
import ru.fraudcore.kafka.event.FraudCaseClosedEvent;
import ru.fraudcore.metrics.service.FraudMetricsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudCaseClosedEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;
    private final FraudMetricsService metricsService;

    public void publish(FraudCaseClosedEvent event) {
        kafkaTemplate.send(topics.getFraudCaseClosed(), event.caseId().toString(), event);
        metricsService.incrementKafkaPublished();
        log.info("Опубликовано событие FraudCaseClosedEvent для caseId={}", event.caseId());
    }
}
