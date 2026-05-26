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
class NewRecipientRuleTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldTriggerWhenNoPreviousRecipient() {
        NewRecipientRule rule = new NewRecipientRule(transactionRepository);
        Transaction tx = Transaction.builder()
                .clientId("c1")
                .recipient("new-rec")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.countByClientIdAndRecipientAndCreatedAtBefore("c1", "new-rec", tx.getCreatedAt())).thenReturn(0L);

        assertThat(rule.evaluate(tx)).isPresent();
    }

    @Test
    void shouldNotTriggerWhenRecipientExists() {
        NewRecipientRule rule = new NewRecipientRule(transactionRepository);
        Transaction tx = Transaction.builder()
                .clientId("c1")
                .recipient("old-rec")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.countByClientIdAndRecipientAndCreatedAtBefore("c1", "old-rec", tx.getCreatedAt())).thenReturn(2L);

        assertThat(rule.evaluate(tx)).isEmpty();
    }
}
