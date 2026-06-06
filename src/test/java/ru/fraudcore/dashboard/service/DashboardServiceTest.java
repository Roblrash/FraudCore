package ru.fraudcore.dashboard.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.cases.repository.FraudCaseRepository;
import ru.fraudcore.dashboard.dto.RiskLevelCountDto;
import ru.fraudcore.dashboard.dto.TransactionTypeCountDto;
import ru.fraudcore.transactions.entity.RiskLevel;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.entity.TransactionType;
import ru.fraudcore.transactions.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private FraudCaseRepository fraudCaseRepository;

    @Test
    void shouldCalculateDashboardStatistics() {
        DashboardService service = new DashboardService(transactionRepository, fraudCaseRepository);

        when(transactionRepository.count()).thenReturn(100L);
        when(transactionRepository.countByStatus(TransactionStatus.APPROVED)).thenReturn(70L);
        when(transactionRepository.countByStatus(TransactionStatus.APPROVED_BY_ANALYST)).thenReturn(10L);
        when(transactionRepository.countByStatus(TransactionStatus.TEMPORARILY_BLOCKED)).thenReturn(20L);
        when(fraudCaseRepository.count()).thenReturn(20L);
        when(fraudCaseRepository.countByStatus(FraudCaseStatus.CLOSED)).thenReturn(15L);
        when(transactionRepository.averageRiskScore()).thenReturn(42.5);
        when(transactionRepository.sumAmountByStatus(TransactionStatus.TEMPORARILY_BLOCKED)).thenReturn(new BigDecimal("1000"));
        when(fraudCaseRepository.countByRiskLevelGroup()).thenReturn(List.of(new RiskLevelCountDto(RiskLevel.HIGH, 15L)));
        when(transactionRepository.countByType()).thenReturn(List.of(new TransactionTypeCountDto(TransactionType.TRANSFER, 80L)));

        var response = service.getDashboard();

        assertThat(response.totalTransactions()).isEqualTo(100);
        assertThat(response.suspiciousTransactionRate()).isEqualTo(20.0);
        assertThat(response.casesByRiskLevel()).containsEntry("HIGH", 15L);
    }
}
