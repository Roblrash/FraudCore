package ru.fraudcore.cases.dto;

import ru.fraudcore.cases.entity.FraudCaseDecision;
import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.transactions.entity.TransactionStatus;

public record FraudCaseDecisionResponse(
        Long caseId,
        FraudCaseStatus status,
        FraudCaseDecision decision,
        TransactionStatus transactionStatus
) {
}
