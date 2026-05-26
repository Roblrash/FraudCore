package ru.fraudcore.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        SecurityProperties.class,
        KafkaTopicsProperties.class,
        ScoringProperties.class
})
public class AppConfig {
}
