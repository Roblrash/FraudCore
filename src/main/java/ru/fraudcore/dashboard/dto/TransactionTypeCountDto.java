package ru.fraudcore.dashboard.dto;

import ru.fraudcore.transactions.entity.TransactionType;

public record TransactionTypeCountDto(TransactionType type, long count) {
}
