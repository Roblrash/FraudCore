package ru.fraudcore.cases.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.fraudcore.cases.entity.FraudCase;
import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.transactions.entity.RiskLevel;

import java.util.List;

public interface FraudCaseRepository extends JpaRepository<FraudCase, Long>, JpaSpecificationExecutor<FraudCase> {

    boolean existsByTransactionId(Long transactionId);

    long countByStatus(FraudCaseStatus status);

    long countByRiskLevel(RiskLevel riskLevel);

    @Query("select f.riskLevel, count(f.id) from FraudCase f group by f.riskLevel")
    List<Object[]> countByRiskLevelGroup();
}
