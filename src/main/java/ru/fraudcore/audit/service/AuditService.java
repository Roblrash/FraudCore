package ru.fraudcore.audit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fraudcore.audit.entity.AuditAction;
import ru.fraudcore.audit.entity.AuditLog;
import ru.fraudcore.audit.repository.AuditLogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(Long actorUserId, AuditAction action, String entityType, Long entityId, String details) {
        AuditLog entry = AuditLog.builder()
                .actorUserId(actorUserId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getTransactionHistory(Long transactionId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc("TRANSACTION", transactionId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getCaseHistory(Long caseId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc("CASE", caseId);
    }
}
