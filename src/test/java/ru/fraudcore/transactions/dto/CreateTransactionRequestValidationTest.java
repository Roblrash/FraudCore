package ru.fraudcore.transactions.dto;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import ru.fraudcore.transactions.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTransactionRequestValidationTest {

    @Test
    void shouldRejectValuesThatDoNotFitApiContract() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            CreateTransactionRequest request = new CreateTransactionRequest(
                    "x".repeat(256),
                    "",
                    "client",
                    "1".repeat(65),
                    new BigDecimal("123456789012345678.123"),
                    "R".repeat(17),
                    TransactionType.TRANSFER,
                    null,
                    null,
                    null,
                    null,
                    LocalDateTime.now()
            );

            var fields = validatorFactory.getValidator().validate(request).stream()
                    .map(violation -> violation.getPropertyPath().toString())
                    .collect(java.util.stream.Collectors.toSet());

            assertThat(fields).contains("externalId", "clientId", "clientPhone", "amount", "currency");
        }
    }
}
