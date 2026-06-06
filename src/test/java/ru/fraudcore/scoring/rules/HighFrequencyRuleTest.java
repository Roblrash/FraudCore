package ru.fraudcore.scoring.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.repository.TransactionRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HighFrequencyRuleTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldTriggerForMoreThanFiveTransactions() {
        HighFrequencyRule rule = new HighFrequencyRule(transactionRepository);
        Transaction tx = Transaction.builder().clientId("c1").createdAt(LocalDateTime.now()).build();

        when(transactionRepository.countByClientIdAndCreatedAtBetween(
                eq("c1"),
                eq(tx.getCreatedAt().minusMinutes(10)),
                eq(tx.getCreatedAt())
        )).thenReturn(6L);

        assertThat(rule.evaluate(tx)).isPresent();
    }

    @Test
    void shouldNotTriggerForFiveOrLessTransactions() {
        HighFrequencyRule rule = new HighFrequencyRule(transactionRepository);
        Transaction tx = Transaction.builder().clientId("c1").createdAt(LocalDateTime.now()).build();

        when(transactionRepository.countByClientIdAndCreatedAtBetween(
                eq("c1"),
                eq(tx.getCreatedAt().minusMinutes(10)),
                eq(tx.getCreatedAt())
        )).thenReturn(5L);

        assertThat(rule.evaluate(tx)).isEmpty();
    }
}
