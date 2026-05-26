package ru.fraudcore.cases.dto;

import ru.fraudcore.cases.entity.FraudCaseDecision;
import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.transactions.dto.RiskReasonResponse;
import ru.fraudcore.transactions.entity.RiskLevel;
import ru.fraudcore.transactions.entity.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FraudCaseResponse(
        Long id,
        Long transactionId,
        TransactionStatus transactionStatus,
        Integer riskScore,
        RiskLevel riskLevel,
        FraudCaseStatus status,
        Long assignedAnalystId,
        String assignedAnalystEmail,
        FraudCaseDecision decision,
        String decisionComment,
        LocalDateTime createdAt,
        LocalDateTime assignedAt,
        LocalDateTime closedAt,
        Long version,
        List<RiskReasonResponse> reasons
) {
}
