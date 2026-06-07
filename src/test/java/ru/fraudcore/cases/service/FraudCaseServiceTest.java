package ru.fraudcore.cases.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fraudcore.audit.service.AuditService;
import ru.fraudcore.cases.dto.FraudCaseDecisionRequest;
import ru.fraudcore.cases.entity.FraudCase;
import ru.fraudcore.cases.entity.FraudCaseDecision;
import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.cases.mapper.FraudCaseMapper;
import ru.fraudcore.cases.repository.FraudCaseRepository;
import ru.fraudcore.common.exception.BadRequestException;
import ru.fraudcore.common.transaction.AfterCommitExecutor;
import ru.fraudcore.kafka.producer.FraudCaseClosedEventProducer;
import ru.fraudcore.metrics.service.FraudMetricsService;
import ru.fraudcore.scoring.repository.RiskRuleResultRepository;
import ru.fraudcore.transactions.entity.RiskLevel;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.repository.TransactionRepository;
import ru.fraudcore.users.entity.User;
import ru.fraudcore.users.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudCaseServiceTest {

    @Mock
    private FraudCaseRepository fraudCaseRepository;
    @Mock
    private FraudCaseMapper fraudCaseMapper;
    @Mock
    private UserService userService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private FraudCaseClosedEventProducer fraudCaseClosedEventProducer;
    @Mock
    private RiskRuleResultRepository riskRuleResultRepository;
    @Mock
    private FraudMetricsService metricsService;

    @Test
    void shouldCreateCaseForBlockedTransaction() {
        FraudCaseService service = buildService();
        Transaction tx = Transaction.builder().id(1L).status(TransactionStatus.TEMPORARILY_BLOCKED).build();

        when(fraudCaseRepository.existsByTransactionId(1L)).thenReturn(false);
        when(fraudCaseRepository.save(any(FraudCase.class))).thenAnswer(inv -> {
            FraudCase c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        FraudCase result = service.createForBlockedTransaction(tx, 70, RiskLevel.HIGH);

        assertThat(result.getId()).isEqualTo(10L);
        verify(auditService).log(any(), any(), any(), any(), any());
    }

    @Test
    void shouldCloseCaseAndUpdateTransactionStatus() {
        FraudCaseService service = buildService();

        User analyst = User.builder().id(2L).email("analyst@test.com").build();
        Transaction tx = Transaction.builder().id(1L).status(TransactionStatus.TEMPORARILY_BLOCKED).build();
        FraudCase fraudCase = FraudCase.builder()
                .id(100L)
                .status(FraudCaseStatus.IN_PROGRESS)
                .assignedAnalyst(analyst)
                .assignedAt(LocalDateTime.now().minusMinutes(5))
                .transaction(tx)
                .build();

        when(userService.getCurrentUserEntity()).thenReturn(analyst);
        when(fraudCaseRepository.findById(100L)).thenReturn(Optional.of(fraudCase));
        when(fraudCaseRepository.save(any(FraudCase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fraudCaseClosedEventProducer.publish(any())).thenReturn(CompletableFuture.completedFuture(null));

        var response = service.makeDecision(100L, new FraudCaseDecisionRequest(FraudCaseDecision.DECLINE_TRANSACTION, "fraud"));

        assertThat(response.transactionStatus()).isEqualTo(TransactionStatus.DECLINED_BY_ANALYST);
        verify(fraudCaseClosedEventProducer).publish(any());
    }

    @Test
    void shouldSortRiskLevelByNumericRiskScore() {
        FraudCaseService service = buildService();
        when(fraudCaseRepository.findAll(
                anyFraudCaseSpecification(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        service.findAll(null, null, false, null, null, "riskLevel", "desc", 0, 20);

        var pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(fraudCaseRepository).findAll(anyFraudCaseSpecification(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("riskScore")).isNotNull();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("riskLevel")).isNull();
    }

    @Test
    void shouldRejectInvalidDateRange() {
        FraudCaseService service = buildService();

        assertThatThrownBy(() -> service.findAll(
                null,
                null,
                false,
                LocalDateTime.of(2026, 6, 8, 0, 0),
                LocalDateTime.of(2026, 6, 7, 0, 0),
                "createdAt",
                "desc",
                0,
                20
        )).isInstanceOf(BadRequestException.class);
    }

    private FraudCaseService buildService() {
        return new FraudCaseService(
                fraudCaseRepository,
                fraudCaseMapper,
                userService,
                transactionRepository,
                auditService,
                fraudCaseClosedEventProducer,
                riskRuleResultRepository,
                metricsService,
                new AfterCommitExecutor()
        );
    }

    private Specification<FraudCase> anyFraudCaseSpecification() {
        return any();
    }
}
