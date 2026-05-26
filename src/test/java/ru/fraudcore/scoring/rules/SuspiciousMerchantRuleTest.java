package ru.fraudcore.scoring.rules;

import org.junit.jupiter.api.Test;
import ru.fraudcore.config.ScoringProperties;
import ru.fraudcore.transactions.entity.Transaction;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SuspiciousMerchantRuleTest {

    @Test
    void shouldTriggerForSuspiciousCounterparty() {
        ScoringProperties props = new ScoringProperties();
        props.setSuspiciousCounterparties(List.of("Bad Merchant"));
        SuspiciousMerchantRule rule = new SuspiciousMerchantRule(props);

        Transaction tx = Transaction.builder().merchant("Bad Merchant").build();

        assertThat(rule.evaluate(tx)).isPresent();
    }

    @Test
    void shouldNotTriggerForRegularCounterparty() {
        ScoringProperties props = new ScoringProperties();
        props.setSuspiciousCounterparties(List.of("Bad Merchant"));
        SuspiciousMerchantRule rule = new SuspiciousMerchantRule(props);

        Transaction tx = Transaction.builder().merchant("Normal Shop").recipient("Trusted").build();

        assertThat(rule.evaluate(tx)).isEmpty();
    }
}
