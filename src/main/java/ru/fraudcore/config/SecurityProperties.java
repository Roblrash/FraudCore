package ru.fraudcore.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "fraudcore.security")
public class SecurityProperties {

    @NotBlank
    private String jwtSecret;

    @Min(1)
    private long jwtExpirationMinutes;
}
