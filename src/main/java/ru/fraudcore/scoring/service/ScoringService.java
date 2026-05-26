package ru.fraudcore.scoring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.fraudcore.scoring.dto.RiskRuleResultDraft;
import ru.fraudcore.scoring.dto.ScoringResult;
import ru.fraudcore.scoring.rules.FraudRule;
import ru.fraudcore.transactions.entity.RiskLevel;
import ru.fraudcore.transactions.entity.Transaction;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private final List<FraudRule> rules;

    public ScoringResult score(Transaction transaction) {
        List<RiskRuleResultDraft> triggered = rules.stream()
                .map(rule -> rule.evaluate(transaction))
                .flatMap(java.util.Optional::stream)
                .toList();

        int score = triggered.stream().mapToInt(RiskRuleResultDraft::points).sum();
        score = Math.min(score, 100);

        return new ScoringResult(score, toRiskLevel(score), triggered);
    }

    public RiskLevel toRiskLevel(int score) {
        if (score < 40) {
            return RiskLevel.LOW;
        }
        if (score < 60) {
            return RiskLevel.MEDIUM;
        }
        if (score < 80) {
            return RiskLevel.HIGH;
        }
        return RiskLevel.CRITICAL;
    }
}
