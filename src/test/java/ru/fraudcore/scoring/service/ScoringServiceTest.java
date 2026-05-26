package ru.fraudcore.scoring.service;

import org.junit.jupiter.api.Test;
import ru.fraudcore.scoring.dto.RiskRuleResultDraft;
import ru.fraudcore.scoring.rules.FraudRule;
import ru.fraudcore.transactions.entity.RiskLevel;
import ru.fraudcore.transactions.entity.Transaction;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTest {

    @Test
    void shouldAggregateRulesAndClampScoreToHundred() {
        FraudRule r1 = tx -> Optional.of(new RiskRuleResultDraft("R1", 70, "d1"));
        FraudRule r2 = tx -> Optional.of(new RiskRuleResultDraft("R2", 50, "d2"));
        ScoringService scoringService = new ScoringService(List.of(r1, r2));

        var result = scoringService.score(Transaction.builder().build());

        assertThat(result.riskScore()).isEqualTo(100);
        assertThat(result.riskLevel()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(result.ruleResults()).hasSize(2);
    }
}
