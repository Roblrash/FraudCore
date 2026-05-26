package ru.fraudcore.audit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fraudcore.audit.dto.AuditLogResponse;
import ru.fraudcore.audit.service.AuditService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/transactions/{id}/audit")
    public List<AuditLogResponse> transactionAudit(@PathVariable Long id) {
        return auditService.getTransactionHistory(id).stream()
                .map(a -> new AuditLogResponse(a.getId(), a.getActorUserId(), a.getAction(), a.getEntityType(), a.getEntityId(), a.getDetails(), a.getCreatedAt()))
                .toList();
    }

    @GetMapping("/cases/{id}/audit")
    public List<AuditLogResponse> caseAudit(@PathVariable Long id) {
        return auditService.getCaseHistory(id).stream()
                .map(a -> new AuditLogResponse(a.getId(), a.getActorUserId(), a.getAction(), a.getEntityType(), a.getEntityId(), a.getDetails(), a.getCreatedAt()))
                .toList();
    }
}
