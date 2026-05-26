package ru.fraudcore.cases.dto;

import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.transactions.entity.RiskLevel;

import java.time.LocalDateTime;

public record FraudCaseSummaryResponse(
        Long id,
        Long transactionId,
        Integer riskScore,
        RiskLevel riskLevel,
        FraudCaseStatus status,
        Long assignedAnalystId,
        LocalDateTime createdAt,
        LocalDateTime assignedAt,
        LocalDateTime closedAt
) {
}
