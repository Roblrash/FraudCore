package ru.fraudcore.transactions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    boolean existsByExternalId(String externalId);

    Optional<Transaction> findByExternalId(String externalId);

    long countByClientIdAndRecipientAndCreatedAtBefore(String clientId, String recipient, LocalDateTime createdAt);

    long countByClientIdAndCreatedAtAfter(String clientId, LocalDateTime from);

    long countByClientIdAndCountryAndCityAndCreatedAtBefore(String clientId, String country, String city, LocalDateTime createdAt);

    long countByStatus(TransactionStatus status);

    @Query("select coalesce(avg(t.riskScore), 0) from Transaction t where t.riskScore is not null")
    Double averageRiskScore();

    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.status = :status")
    BigDecimal sumAmountByStatus(TransactionStatus status);

    @Query("select t.type, count(t.id) from Transaction t group by t.type")
    List<Object[]> countByType();
}
