package ru.fraudcore.cases.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.fraudcore.cases.entity.FraudCase;
import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.dashboard.dto.RiskLevelCountDto;

import java.util.List;

public interface FraudCaseRepository extends JpaRepository<FraudCase, Long>, JpaSpecificationExecutor<FraudCase> {

    boolean existsByTransactionId(Long transactionId);

    long countByStatus(FraudCaseStatus status);

    @Query("""
            select new ru.fraudcore.dashboard.dto.RiskLevelCountDto(f.riskLevel, count(f.id))
            from FraudCase f
            group by f.riskLevel
            """)
    List<RiskLevelCountDto> countByRiskLevelGroup();
}
