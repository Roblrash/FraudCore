package ru.fraudcore.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "fraudcore.scoring")
public class ScoringProperties {

    private List<String> suspiciousCounterparties = new ArrayList<>();
}
