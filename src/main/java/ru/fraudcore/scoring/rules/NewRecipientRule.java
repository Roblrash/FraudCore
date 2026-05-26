package ru.fraudcore.scoring.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.fraudcore.scoring.dto.RiskRuleResultDraft;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.repository.TransactionRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NewRecipientRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public Optional<RiskRuleResultDraft> evaluate(Transaction transaction) {
        if (transaction.getRecipient() == null || transaction.getRecipient().isBlank()) {
            return Optional.empty();
        }

        long previous = transactionRepository.countByClientIdAndRecipientAndCreatedAtBefore(
                transaction.getClientId(),
                transaction.getRecipient(),
                transaction.getCreatedAt()
        );

        if (previous == 0) {
            return Optional.of(new RiskRuleResultDraft(
                    "NEW_RECIPIENT",
                    25,
                    "Получатель ранее не встречался в транзакциях клиента"
            ));
        }
        return Optional.empty();
    }
}
