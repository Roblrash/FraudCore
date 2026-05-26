package ru.fraudcore.transactions.entity;

public enum TransactionStatus {
    PENDING,
    APPROVED,
    TEMPORARILY_BLOCKED,
    APPROVED_BY_ANALYST,
    DECLINED_BY_ANALYST
}
