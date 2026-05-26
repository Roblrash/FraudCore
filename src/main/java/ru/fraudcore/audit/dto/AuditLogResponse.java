package ru.fraudcore.audit.dto;

import ru.fraudcore.audit.entity.AuditAction;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        AuditAction action,
        String entityType,
        Long entityId,
        String details,
        LocalDateTime createdAt
) {
}
