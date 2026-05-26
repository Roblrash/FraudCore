package ru.fraudcore.scoring.rules;

import org.springframework.stereotype.Component;
import ru.fraudcore.scoring.dto.RiskRuleResultDraft;
import ru.fraudcore.transactions.entity.Transaction;

import java.util.Optional;

@Component
public class NightOperationRule implements FraudRule {

    @Override
    public Optional<RiskRuleResultDraft> evaluate(Transaction transaction) {
        int hour = transaction.getCreatedAt().getHour();
        if (hour >= 0 && hour < 6) {
            return Optional.of(new RiskRuleResultDraft(
                    "NIGHT_OPERATION",
                    20,
                    "Транзакция создана в ночное время"
            ));
        }
        return Optional.empty();
    }
}
