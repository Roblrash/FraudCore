package ru.fraudcore.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.fraudcore.config.KafkaTopicsProperties;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaTopicsProperties topics;

    @Bean
    public NewTopic transactionCreatedTopic() {
        return new NewTopic(topics.getTransactionCreated(), 1, (short) 1);
    }

    @Bean
    public NewTopic transactionScoredTopic() {
        return new NewTopic(topics.getTransactionScored(), 1, (short) 1);
    }

    @Bean
    public NewTopic fraudCaseCreatedTopic() {
        return new NewTopic(topics.getFraudCaseCreated(), 1, (short) 1);
    }

    @Bean
    public NewTopic fraudCaseClosedTopic() {
        return new NewTopic(topics.getFraudCaseClosed(), 1, (short) 1);
    }
}
