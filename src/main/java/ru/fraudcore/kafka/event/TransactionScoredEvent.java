package ru.fraudcore.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionScoredEvent(
        UUID eventId,
        Long transactionId,
        Integer riskScore,
        String riskLevel,
        String transactionStatus,
        LocalDateTime createdAt
) {
}
