package ru.fraudcore.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID eventId,
        Long transactionId,
        String externalId,
        String clientId,
        BigDecimal amount,
        String currency,
        String type,
        LocalDateTime createdAt
) {
}
