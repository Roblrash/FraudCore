package ru.fraudcore.cases.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.fraudcore.cases.entity.FraudCase;
import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.transactions.entity.RiskLevel;

import java.time.LocalDateTime;

public final class FraudCaseSpecifications {

    private FraudCaseSpecifications() {
    }

    public static Specification<FraudCase> statusEquals(FraudCaseStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<FraudCase> riskLevelEquals(RiskLevel riskLevel) {
        return (root, query, cb) -> riskLevel == null ? null : cb.equal(root.get("riskLevel"), riskLevel);
    }

    public static Specification<FraudCase> assignedTo(Long analystId) {
        return (root, query, cb) -> analystId == null ? null : cb.equal(root.get("assignedAnalyst").get("id"), analystId);
    }

    public static Specification<FraudCase> createdAtFrom(LocalDateTime dateFrom) {
        return (root, query, cb) -> dateFrom == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom);
    }

    public static Specification<FraudCase> createdAtTo(LocalDateTime dateTo) {
        return (root, query, cb) -> dateTo == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), dateTo);
    }
}
