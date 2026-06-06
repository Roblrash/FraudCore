package ru.fraudcore.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.cases.repository.FraudCaseRepository;
import ru.fraudcore.dashboard.dto.DashboardResponse;
import ru.fraudcore.dashboard.dto.RiskLevelCountDto;
import ru.fraudcore.dashboard.dto.TransactionTypeCountDto;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final FraudCaseRepository fraudCaseRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long totalTransactions = transactionRepository.count();
        long approvedTransactions = transactionRepository.countByStatus(TransactionStatus.APPROVED)
                + transactionRepository.countByStatus(TransactionStatus.APPROVED_BY_ANALYST);
        long blockedTransactions = transactionRepository.countByStatus(TransactionStatus.TEMPORARILY_BLOCKED);
        long createdCases = fraudCaseRepository.count();
        long closedCases = fraudCaseRepository.countByStatus(FraudCaseStatus.CLOSED);
        double averageRiskScore = transactionRepository.averageRiskScore();

        BigDecimal blockedAmount = transactionRepository.sumAmountByStatus(TransactionStatus.TEMPORARILY_BLOCKED);
        double suspiciousRate = totalTransactions == 0
                ? 0
                : BigDecimal.valueOf((double) blockedTransactions * 100 / totalTransactions)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        Map<String, Long> casesByRiskLevel = fraudCaseRepository.countByRiskLevelGroup().stream()
                .collect(Collectors.toMap(r -> r.riskLevel().name(), RiskLevelCountDto::count));
        Map<String, Long> transactionsByType = transactionRepository.countByType().stream()
                .collect(Collectors.toMap(r -> r.type().name(), TransactionTypeCountDto::count));

        return new DashboardResponse(
                totalTransactions,
                approvedTransactions,
                blockedTransactions,
                createdCases,
                closedCases,
                BigDecimal.valueOf(averageRiskScore).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                blockedAmount,
                suspiciousRate,
                casesByRiskLevel,
                transactionsByType
        );
    }
}
