package ru.fraudcore.scoring.rules;

import org.junit.jupiter.api.Test;
import ru.fraudcore.transactions.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HighAmountRuleTest {

    private final HighAmountRule rule = new HighAmountRule();

    @Test
    void shouldTriggerForHighAmount() {
        Transaction tx = Transaction.builder().amount(new BigDecimal("100000")).createdAt(LocalDateTime.now()).build();

        var result = rule.evaluate(tx);

        assertThat(result).isPresent();
        assertThat(result.get().points()).isEqualTo(30);
    }

    @Test
    void shouldNotTriggerForLowAmount() {
        Transaction tx = Transaction.builder().amount(new BigDecimal("99999")).createdAt(LocalDateTime.now()).build();

        assertThat(rule.evaluate(tx)).isEmpty();
    }
}
