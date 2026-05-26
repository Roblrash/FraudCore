package ru.fraudcore.scoring.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.repository.TransactionRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnusualLocationRuleTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldTriggerForNewLocation() {
        UnusualLocationRule rule = new UnusualLocationRule(transactionRepository);
        Transaction tx = Transaction.builder()
                .clientId("c1")
                .country("RU")
                .city("Moscow")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.countByClientIdAndCountryAndCityAndCreatedAtBefore("c1", "RU", "Moscow", tx.getCreatedAt())).thenReturn(0L);

        assertThat(rule.evaluate(tx)).isPresent();
    }

    @Test
    void shouldNotTriggerForKnownLocation() {
        UnusualLocationRule rule = new UnusualLocationRule(transactionRepository);
        Transaction tx = Transaction.builder()
                .clientId("c1")
                .country("RU")
                .city("Moscow")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.countByClientIdAndCountryAndCityAndCreatedAtBefore("c1", "RU", "Moscow", tx.getCreatedAt())).thenReturn(3L);

        assertThat(rule.evaluate(tx)).isEmpty();
    }
}
