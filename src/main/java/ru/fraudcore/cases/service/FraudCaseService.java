package ru.fraudcore.cases.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fraudcore.audit.entity.AuditAction;
import ru.fraudcore.audit.service.AuditService;
import ru.fraudcore.cases.dto.FraudCaseDecisionRequest;
import ru.fraudcore.cases.dto.FraudCaseDecisionResponse;
import ru.fraudcore.cases.dto.FraudCaseResponse;
import ru.fraudcore.cases.dto.FraudCaseSummaryResponse;
import ru.fraudcore.cases.entity.FraudCase;
import ru.fraudcore.cases.entity.FraudCaseDecision;
import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.cases.mapper.FraudCaseMapper;
import ru.fraudcore.cases.repository.FraudCaseRepository;
import ru.fraudcore.cases.repository.FraudCaseSpecifications;
import ru.fraudcore.common.exception.ConflictException;
import ru.fraudcore.common.exception.ForbiddenException;
import ru.fraudcore.common.exception.NotFoundException;
import ru.fraudcore.common.response.PageResponse;
import ru.fraudcore.kafka.event.FraudCaseClosedEvent;
import ru.fraudcore.kafka.producer.FraudCaseClosedEventProducer;
import ru.fraudcore.metrics.service.FraudMetricsService;
import ru.fraudcore.scoring.repository.RiskRuleResultRepository;
import ru.fraudcore.transactions.dto.RiskReasonResponse;
import ru.fraudcore.transactions.entity.RiskLevel;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.repository.TransactionRepository;
import ru.fraudcore.users.entity.User;
import ru.fraudcore.users.service.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FraudCaseService {

    private final FraudCaseRepository fraudCaseRepository;
    private final FraudCaseMapper fraudCaseMapper;
    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;
    private final FraudCaseClosedEventProducer fraudCaseClosedEventProducer;
    private final RiskRuleResultRepository riskRuleResultRepository;
    private final FraudMetricsService metricsService;

    @Transactional
    public FraudCase createForBlockedTransaction(Transaction transaction, Integer riskScore, RiskLevel riskLevel) {
        if (transaction.getStatus() != TransactionStatus.TEMPORARILY_BLOCKED) {
            throw new ConflictException("Кейс можно создать только для транзакции со статусом TEMPORARILY_BLOCKED");
        }
        if (fraudCaseRepository.existsByTransactionId(transaction.getId())) {
            throw new ConflictException("Для этой транзакции кейс уже существует");
        }

        FraudCase fraudCase = FraudCase.builder()
                .transaction(transaction)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .status(FraudCaseStatus.NEW)
                .build();

        FraudCase saved = fraudCaseRepository.save(fraudCase);
        auditService.log(null, AuditAction.CASE_CREATED, "CASE", saved.getId(), "Создан для transactionId=" + transaction.getId());
        metricsService.incrementCasesCreated();
        return saved;
    }

    @Transactional(readOnly = true)
    public PageResponse<FraudCaseSummaryResponse> findAll(
            FraudCaseStatus status,
            RiskLevel riskLevel,
            Boolean assignedToMe,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String sortBy,
            String sortDirection,
            int page,
            int size
    ) {
        Long analystId = Boolean.TRUE.equals(assignedToMe) ? userService.getCurrentUserEntity().getId() : null;

        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy == null || sortBy.isBlank() ? "createdAt" : sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<FraudCase> spec = Specification.where(FraudCaseSpecifications.statusEquals(status))
                .and(FraudCaseSpecifications.riskLevelEquals(riskLevel))
                .and(FraudCaseSpecifications.assignedTo(analystId))
                .and(FraudCaseSpecifications.createdAtFrom(dateFrom))
                .and(FraudCaseSpecifications.createdAtTo(dateTo));

        Page<FraudCase> casesPage = fraudCaseRepository.findAll(spec, pageable);
        return PageResponse.<FraudCaseSummaryResponse>builder()
                .content(casesPage.stream().map(fraudCaseMapper::toSummary).toList())
                .page(casesPage.getNumber())
                .size(casesPage.getSize())
                .totalElements(casesPage.getTotalElements())
                .totalPages(casesPage.getTotalPages())
                .last(casesPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public FraudCaseResponse getById(Long id) {
        FraudCase fraudCase = findCaseById(id);
        return toDetailedResponse(fraudCase);
    }

    @Transactional
    public FraudCaseResponse assignToMe(Long caseId) {
        User analyst = userService.getCurrentUserEntity();
        FraudCase fraudCase = findCaseById(caseId);

        if (fraudCase.getStatus() == FraudCaseStatus.CLOSED) {
            throw new ConflictException("Закрытый кейс нельзя назначить");
        }
        if (fraudCase.getStatus() == FraudCaseStatus.IN_PROGRESS) {
            throw new ConflictException("Кейс уже находится в работе");
        }

        fraudCase.setStatus(FraudCaseStatus.IN_PROGRESS);
        fraudCase.setAssignedAnalyst(analyst);
        fraudCase.setAssignedAt(LocalDateTime.now());

        FraudCase saved = fraudCaseRepository.save(fraudCase);
        auditService.log(analyst.getId(), AuditAction.CASE_ASSIGNED, "CASE", saved.getId(), "Назначен на аналитика");
        return toDetailedResponse(saved);
    }

    @Transactional
    public FraudCaseDecisionResponse makeDecision(Long caseId, FraudCaseDecisionRequest request) {
        User analyst = userService.getCurrentUserEntity();
        FraudCase fraudCase = findCaseById(caseId);

        if (fraudCase.getStatus() == FraudCaseStatus.CLOSED) {
            throw new ConflictException("Закрытый кейс нельзя изменить");
        }
        if (fraudCase.getStatus() == FraudCaseStatus.NEW) {
            throw new ConflictException("Перед принятием решения кейс нужно взять в работу");
        }
        if (fraudCase.getAssignedAnalyst() == null || !fraudCase.getAssignedAnalyst().getId().equals(analyst.getId())) {
            throw new ForbiddenException("Кейс назначен другому аналитику");
        }

        Transaction transaction = fraudCase.getTransaction();
        if (request.decision() == FraudCaseDecision.APPROVE_TRANSACTION) {
            transaction.setStatus(TransactionStatus.APPROVED_BY_ANALYST);
        } else {
            transaction.setStatus(TransactionStatus.DECLINED_BY_ANALYST);
        }

        fraudCase.setDecision(request.decision());
        fraudCase.setDecisionComment(request.comment());
        fraudCase.setStatus(FraudCaseStatus.CLOSED);
        fraudCase.setClosedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
        FraudCase saved = fraudCaseRepository.save(fraudCase);

        auditService.log(analyst.getId(), AuditAction.CASE_DECISION_MADE, "CASE", saved.getId(), "Решение=" + request.decision().name());
        auditService.log(analyst.getId(), AuditAction.CASE_CLOSED, "CASE", saved.getId(), "Кейс закрыт аналитиком");

        FraudCaseClosedEvent event = new FraudCaseClosedEvent(
                UUID.randomUUID(),
                saved.getId(),
                transaction.getId(),
                request.decision().name(),
                transaction.getStatus().name(),
                saved.getClosedAt()
        );
        fraudCaseClosedEventProducer.publish(event);
        auditService.log(analyst.getId(), AuditAction.KAFKA_EVENT_PUBLISHED, "CASE", saved.getId(), "Опубликовано в topic=fraud.case.closed");

        metricsService.incrementCasesClosed();
        if (saved.getAssignedAt() != null) {
            metricsService.recordCaseDecisionSeconds(Duration.between(saved.getAssignedAt(), saved.getClosedAt()).toSeconds());
        }

        return new FraudCaseDecisionResponse(saved.getId(), saved.getStatus(), saved.getDecision(), transaction.getStatus());
    }

    private FraudCase findCaseById(Long id) {
        return fraudCaseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Кейс не найден"));
    }

    private FraudCaseResponse toDetailedResponse(FraudCase fraudCase) {
        var reasons = riskRuleResultRepository.findAllByTransactionId(fraudCase.getTransaction().getId()).stream()
                .map(r -> new RiskReasonResponse(r.getRuleCode(), r.getPoints(), r.getDescription()))
                .toList();
        return new FraudCaseResponse(
                fraudCase.getId(),
                fraudCase.getTransaction().getId(),
                fraudCase.getTransaction().getStatus(),
                fraudCase.getRiskScore(),
                fraudCase.getRiskLevel(),
                fraudCase.getStatus(),
                fraudCase.getAssignedAnalyst() == null ? null : fraudCase.getAssignedAnalyst().getId(),
                fraudCase.getAssignedAnalyst() == null ? null : fraudCase.getAssignedAnalyst().getEmail(),
                fraudCase.getDecision(),
                fraudCase.getDecisionComment(),
                fraudCase.getCreatedAt(),
                fraudCase.getAssignedAt(),
                fraudCase.getClosedAt(),
                fraudCase.getVersion(),
                reasons
        );
    }
}
