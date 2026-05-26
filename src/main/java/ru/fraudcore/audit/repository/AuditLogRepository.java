package ru.fraudcore.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fraudcore.audit.entity.AuditLog;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtAsc(String entityType, Long entityId);
}
