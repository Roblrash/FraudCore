package ru.fraudcore.scoring.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.fraudcore.transactions.entity.Transaction;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_rule_results")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskRuleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "rule_code", nullable = false)
    private String ruleCode;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
