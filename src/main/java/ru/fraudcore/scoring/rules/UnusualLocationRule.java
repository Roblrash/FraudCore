package ru.fraudcore.scoring.rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.fraudcore.scoring.dto.RiskRuleResultDraft;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.repository.TransactionRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UnusualLocationRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public Optional<RiskRuleResultDraft> evaluate(Transaction transaction) {
        if (transaction.getCountry() == null || transaction.getCity() == null) {
            return Optional.empty();
        }

        long count = transactionRepository.countByClientIdAndCountryAndCityAndCreatedAtBefore(
                transaction.getClientId(),
                transaction.getCountry(),
                transaction.getCity(),
                transaction.getCreatedAt()
        );

        if (count == 0) {
            return Optional.of(new RiskRuleResultDraft(
                    "UNUSUAL_LOCATION",
                    15,
                    "У клиента ранее не было транзакций в этой стране/городе"
            ));
        }
        return Optional.empty();
    }
}
