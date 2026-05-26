package ru.fraudcore.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record FraudCaseClosedEvent(
        UUID eventId,
        Long caseId,
        Long transactionId,
        String decision,
        String transactionStatus,
        LocalDateTime closedAt
) {
}
