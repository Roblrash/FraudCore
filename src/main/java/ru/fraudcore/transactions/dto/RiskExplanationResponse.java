package ru.fraudcore.transactions.dto;

import ru.fraudcore.transactions.entity.RiskLevel;

import java.util.List;

public record RiskExplanationResponse(
        Long transactionId,
        Integer riskScore,
        RiskLevel riskLevel,
        List<RiskReasonResponse> reasons
) {
}
