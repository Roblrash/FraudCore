package ru.fraudcore.metrics.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class FraudMetricsService {

    private final Counter transactionsReceived;
    private final Counter transactionsApproved;
    private final Counter transactionsBlocked;
    private final Counter casesCreated;
    private final Counter casesClosed;
    private final Counter kafkaEventsPublished;
    private final Counter kafkaEventsConsumed;
    private final Counter kafkaConsumerErrors;
    private final Timer caseDecisionTimer;
    private final DistributionSummary riskScoreSummary;

    public FraudMetricsService(MeterRegistry meterRegistry) {
        this.transactionsReceived = meterRegistry.counter("fraud_transactions_received_total");
        this.transactionsApproved = meterRegistry.counter("fraud_transactions_approved_total");
        this.transactionsBlocked = meterRegistry.counter("fraud_transactions_blocked_total");
        this.casesCreated = meterRegistry.counter("fraud_cases_created_total");
        this.casesClosed = meterRegistry.counter("fraud_cases_closed_total");
        this.kafkaEventsPublished = meterRegistry.counter("fraud_kafka_events_published_total");
        this.kafkaEventsConsumed = meterRegistry.counter("fraud_kafka_events_consumed_total");
        this.kafkaConsumerErrors = meterRegistry.counter("fraud_kafka_consumer_errors_total");
        this.caseDecisionTimer = meterRegistry.timer("fraud_case_decision_time_seconds");
        this.riskScoreSummary = DistributionSummary.builder("fraud_risk_score_average")
                .baseUnit("score")
                .register(meterRegistry);
    }

    public void incrementTransactionsReceived() { transactionsReceived.increment(); }

    public void incrementTransactionsApproved() { transactionsApproved.increment(); }

    public void incrementTransactionsBlocked() { transactionsBlocked.increment(); }

    public void incrementCasesCreated() { casesCreated.increment(); }

    public void incrementCasesClosed() { casesClosed.increment(); }

    public void incrementKafkaPublished() { kafkaEventsPublished.increment(); }

    public void incrementKafkaConsumed() { kafkaEventsConsumed.increment(); }

    public void incrementKafkaConsumerErrors() { kafkaConsumerErrors.increment(); }

    public void recordCaseDecisionSeconds(double seconds) { caseDecisionTimer.record((long) (seconds * 1_000_000_000L), java.util.concurrent.TimeUnit.NANOSECONDS); }

    public void recordRiskScore(int score) { riskScoreSummary.record(score); }
}
