package ru.fraudcore.dashboard.dto;

import ru.fraudcore.transactions.entity.RiskLevel;

public record RiskLevelCountDto(RiskLevel riskLevel, long count) {
}
