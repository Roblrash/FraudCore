package ru.fraudcore.transactions.dto;

import ru.fraudcore.transactions.entity.RiskLevel;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String externalId,
        String clientId,
        String clientFullName,
        String clientPhone,
        BigDecimal amount,
        String currency,
        TransactionType type,
        String merchant,
        String recipient,
        String country,
        String city,
        TransactionStatus status,
        Integer riskScore,
        RiskLevel riskLevel,
        LocalDateTime createdAt,
        LocalDateTime processedAt
) {
}
