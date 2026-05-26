package ru.fraudcore.scoring.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.fraudcore.config.ScoringProperties;
import ru.fraudcore.scoring.dto.RiskRuleResultDraft;
import ru.fraudcore.transactions.entity.Transaction;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SuspiciousMerchantRule implements FraudRule {

    private final ScoringProperties scoringProperties;

    @Override
    public Optional<RiskRuleResultDraft> evaluate(Transaction transaction) {
        String merchant = normalize(transaction.getMerchant());
        String recipient = normalize(transaction.getRecipient());

        boolean hit = scoringProperties.getSuspiciousCounterparties().stream()
                .map(this::normalize)
                .anyMatch(item -> item.equals(merchant) || item.equals(recipient));

        if (hit) {
            return Optional.of(new RiskRuleResultDraft(
                    "SUSPICIOUS_MERCHANT",
                    25,
                    "Мерчант или получатель входит в список подозрительных"
            ));
        }
        return Optional.empty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
