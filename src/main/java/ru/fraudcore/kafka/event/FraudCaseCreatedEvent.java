package ru.fraudcore.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record FraudCaseCreatedEvent(
        UUID eventId,
        Long caseId,
        Long transactionId,
        Integer riskScore,
        String riskLevel,
        LocalDateTime createdAt
) {
}
