package ru.fraudcore.scoring.rules;

import org.junit.jupiter.api.Test;
import ru.fraudcore.transactions.entity.Transaction;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NightOperationRuleTest {

    private final NightOperationRule rule = new NightOperationRule();

    @Test
    void shouldTriggerAtNight() {
        Transaction tx = Transaction.builder().createdAt(LocalDateTime.of(2026, 5, 26, 2, 30)).build();

        assertThat(rule.evaluate(tx)).isPresent();
    }

    @Test
    void shouldNotTriggerAtDay() {
        Transaction tx = Transaction.builder().createdAt(LocalDateTime.of(2026, 5, 26, 12, 0)).build();

        assertThat(rule.evaluate(tx)).isEmpty();
    }
}
