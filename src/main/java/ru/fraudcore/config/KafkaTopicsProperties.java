package ru.fraudcore.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "fraudcore.kafka.topics")
public class KafkaTopicsProperties {

    @NotBlank
    private String transactionCreated;

    @NotBlank
    private String transactionScored;

    @NotBlank
    private String fraudCaseCreated;

    @NotBlank
    private String fraudCaseClosed;
}
