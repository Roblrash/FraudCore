package ru.fraudcore.dashboard.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardResponse(
        long totalTransactions,
        long approvedTransactions,
        long temporarilyBlockedTransactions,
        long createdCases,
        long closedCases,
        double averageRiskScore,
        BigDecimal temporarilyBlockedAmount,
        double suspiciousTransactionRate,
        Map<String, Long> casesByRiskLevel,
        Map<String, Long> transactionsByType
) {
}
