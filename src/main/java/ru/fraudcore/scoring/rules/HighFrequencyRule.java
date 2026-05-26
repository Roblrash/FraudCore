package ru.fraudcore.scoring.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.fraudcore.scoring.dto.RiskRuleResultDraft;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HighFrequencyRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public Optional<RiskRuleResultDraft> evaluate(Transaction transaction) {
        LocalDateTime tenMinutesAgo = transaction.getCreatedAt().minusMinutes(10);
        long count = transactionRepository.countByClientIdAndCreatedAtAfter(transaction.getClientId(), tenMinutesAgo);

        if (count > 5) {
            return Optional.of(new RiskRuleResultDraft(
                    "HIGH_FREQUENCY",
                    20,
                    "У клиента более 5 транзакций за последние 10 минут"
            ));
        }
        return Optional.empty();
    }
}
