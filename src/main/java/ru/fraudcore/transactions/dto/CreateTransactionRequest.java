package ru.fraudcore.transactions.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.fraudcore.transactions.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTransactionRequest(
        @NotBlank @Size(max = 255) String externalId,
        @NotBlank @Size(max = 255) String clientId,
        @NotBlank @Size(max = 255) String clientFullName,
        @Size(max = 64) String clientPhone,
        @NotNull
        @Positive(message = "Сумма должна быть положительной")
        @Digits(integer = 17, fraction = 2, message = "Сумма должна содержать не более 17 целых и 2 дробных знаков")
        BigDecimal amount,
        @NotBlank @Size(max = 16) String currency,
        @NotNull TransactionType type,
        @Size(max = 255) String merchant,
        @Size(max = 255) String recipient,
        @Size(max = 128) String country,
        @Size(max = 128) String city,
        @NotNull LocalDateTime createdAt
) {
}
