package ru.fraudcore.transactions.dto;

import ru.fraudcore.transactions.entity.TransactionStatus;

public record TransactionAcceptedResponse(
        Long id,
        String externalId,
        TransactionStatus status,
        String message
) {
}
