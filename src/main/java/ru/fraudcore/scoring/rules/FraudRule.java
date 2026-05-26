package ru.fraudcore.scoring.rules;

import ru.fraudcore.scoring.dto.RiskRuleResultDraft;
import ru.fraudcore.transactions.entity.Transaction;

import java.util.Optional;

public interface FraudRule {

    Optional<RiskRuleResultDraft> evaluate(Transaction transaction);
}
