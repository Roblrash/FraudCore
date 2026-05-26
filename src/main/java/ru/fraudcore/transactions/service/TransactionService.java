package ru.fraudcore.transactions.service;

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
import ru.fraudcore.common.exception.ConflictException;
import ru.fraudcore.common.exception.NotFoundException;
import ru.fraudcore.common.response.PageResponse;
import ru.fraudcore.kafka.event.TransactionCreatedEvent;
import ru.fraudcore.kafka.producer.TransactionCreatedEventProducer;
import ru.fraudcore.metrics.service.FraudMetricsService;
import ru.fraudcore.scoring.repository.RiskRuleResultRepository;
import ru.fraudcore.transactions.dto.*;
import ru.fraudcore.transactions.entity.Transaction;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.entity.TransactionType;
import ru.fraudcore.transactions.mapper.TransactionMapper;
import ru.fraudcore.transactions.repository.TransactionRepository;
import ru.fraudcore.transactions.repository.TransactionSpecifications;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionCreatedEventProducer transactionCreatedEventProducer;
    private final AuditService auditService;
    private final RiskRuleResultRepository riskRuleResultRepository;
    private final FraudMetricsService metricsService;

    @Transactional
    public TransactionAcceptedResponse createTransaction(CreateTransactionRequest request) {
        if (transactionRepository.existsByExternalId(request.externalId())) {
            throw new ConflictException("Транзакция с таким externalId уже существует");
        }

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setRiskScore(null);
        transaction.setRiskLevel(null);
        transaction.setProcessedAt(null);

        Transaction saved = transactionRepository.save(transaction);

        auditService.log(null, AuditAction.TRANSACTION_RECEIVED, "TRANSACTION", saved.getId(), "Транзакция принята");
        metricsService.incrementTransactionsReceived();

        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                saved.getId(),
                saved.getExternalId(),
                saved.getClientId(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getType().name(),
                saved.getCreatedAt()
        );
        transactionCreatedEventProducer.publish(event);
        auditService.log(null, AuditAction.KAFKA_EVENT_PUBLISHED, "TRANSACTION", saved.getId(), "Опубликовано в topic=transaction.created");

        return new TransactionAcceptedResponse(
                saved.getId(),
                saved.getExternalId(),
                saved.getStatus(),
                "Транзакция принята в обработку антифрод-анализа"
        );
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Транзакция не найдена"));
        return transactionMapper.toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Transaction getEntityById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Транзакция не найдена"));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> findAll(
            String clientId,
            String externalId,
            TransactionStatus status,
            TransactionType type,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String sortBy,
            String sortDirection,
            int page,
            int size
    ) {
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy == null || sortBy.isBlank() ? "createdAt" : sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Transaction> spec = Specification.where(TransactionSpecifications.clientIdEquals(clientId))
                .and(TransactionSpecifications.externalIdEquals(externalId))
                .and(TransactionSpecifications.statusEquals(status))
                .and(TransactionSpecifications.typeEquals(type))
                .and(TransactionSpecifications.createdAtFrom(dateFrom))
                .and(TransactionSpecifications.createdAtTo(dateTo))
                .and(TransactionSpecifications.minAmount(minAmount))
                .and(TransactionSpecifications.maxAmount(maxAmount));

        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);
        return PageResponse.<TransactionResponse>builder()
                .content(transactions.stream().map(transactionMapper::toResponse).toList())
                .page(transactions.getNumber())
                .size(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .last(transactions.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public RiskExplanationResponse getRiskExplanation(Long transactionId) {
        Transaction transaction = getEntityById(transactionId);
        var reasons = riskRuleResultRepository.findAllByTransactionId(transactionId).stream()
                .map(r -> new RiskReasonResponse(r.getRuleCode(), r.getPoints(), r.getDescription()))
                .toList();
        return new RiskExplanationResponse(
                transaction.getId(),
                transaction.getRiskScore(),
                transaction.getRiskLevel(),
                reasons
        );
    }
}
