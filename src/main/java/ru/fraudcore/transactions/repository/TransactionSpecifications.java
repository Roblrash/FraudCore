package ru.fraudcore.transactions.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.fraudcore.transactions.entity.RiskLevel;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> clientIdEquals(String clientId) {
        return (root, query, cb) -> clientId == null ? null : cb.equal(root.get("clientId"), clientId);
    }

    public static Specification<Transaction> externalIdEquals(String externalId) {
        return (root, query, cb) -> externalId == null ? null : cb.equal(root.get("externalId"), externalId);
    }

    public static Specification<Transaction> statusEquals(TransactionStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Transaction> typeEquals(TransactionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> riskLevelEquals(RiskLevel riskLevel) {
        return (root, query, cb) -> riskLevel == null ? null : cb.equal(root.get("riskLevel"), riskLevel);
    }

    public static Specification<Transaction> createdAtFrom(LocalDateTime dateFrom) {
        return (root, query, cb) -> dateFrom == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom);
    }

    public static Specification<Transaction> createdAtTo(LocalDateTime dateTo) {
        return (root, query, cb) -> dateTo == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), dateTo);
    }

    public static Specification<Transaction> minAmount(BigDecimal minAmount) {
        return (root, query, cb) -> minAmount == null ? null : cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Transaction> maxAmount(BigDecimal maxAmount) {
        return (root, query, cb) -> maxAmount == null ? null : cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }
}
