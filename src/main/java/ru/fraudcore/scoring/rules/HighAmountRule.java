package ru.fraudcore.scoring.rules;

import org.springframework.stereotype.Component;
import ru.fraudcore.scoring.dto.RiskRuleResultDraft;
import ru.fraudcore.transactions.entity.Transaction;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class HighAmountRule implements FraudRule {

    @Override
    public Optional<RiskRuleResultDraft> evaluate(Transaction transaction) {
        if (transaction.getAmount() != null && transaction.getAmount().compareTo(new BigDecimal("100000")) >= 0) {
            return Optional.of(new RiskRuleResultDraft(
                    "HIGH_AMOUNT",
                    30,
                    "Сумма транзакции больше или равна 100000"
            ));
        }
        return Optional.empty();
    }
}
