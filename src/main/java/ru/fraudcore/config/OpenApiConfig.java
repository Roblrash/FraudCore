package ru.fraudcore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fraudCoreOpenApi() {
        return new OpenAPI().info(new Info()
                .title("FraudCore API")
                .description("Backend API для антифрод-мониторинга банковских транзакций")
                .version("v1"));
    }
}
