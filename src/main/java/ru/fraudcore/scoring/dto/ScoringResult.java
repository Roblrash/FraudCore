package ru.fraudcore.scoring.dto;

import ru.fraudcore.transactions.entity.RiskLevel;

import java.util.List;

public record ScoringResult(
        Integer riskScore,
        RiskLevel riskLevel,
        List<RiskRuleResultDraft> ruleResults
) {
}
