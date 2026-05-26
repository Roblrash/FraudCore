package ru.fraudcore.scoring.dto;

public record RiskRuleResultDraft(
        String ruleCode,
        Integer points,
        String description
) {
}
