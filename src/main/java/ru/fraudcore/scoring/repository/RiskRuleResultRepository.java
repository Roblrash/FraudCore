package ru.fraudcore.scoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fraudcore.scoring.entity.RiskRuleResult;

import java.util.List;

public interface RiskRuleResultRepository extends JpaRepository<RiskRuleResult, Long> {

    List<RiskRuleResult> findAllByTransactionId(Long transactionId);
}
