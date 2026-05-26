package ru.fraudcore.transactions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.fraudcore.transactions.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTransactionRequest(
        @NotBlank String externalId,
        @NotBlank String clientId,
        @NotBlank String clientFullName,
        String clientPhone,
        @NotNull @Positive(message = "Сумма должна быть положительной") BigDecimal amount,
        @NotBlank String currency,
        @NotNull TransactionType type,
        String merchant,
        String recipient,
        String country,
        String city,
        @NotNull LocalDateTime createdAt
) {
}
