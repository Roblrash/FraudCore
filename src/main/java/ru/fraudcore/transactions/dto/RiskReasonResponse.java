package ru.fraudcore.transactions.dto;

public record RiskReasonResponse(
        String ruleCode,
        Integer points,
        String description
) {
}
