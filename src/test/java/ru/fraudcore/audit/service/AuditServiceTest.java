package ru.fraudcore.audit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fraudcore.audit.entity.AuditAction;
import ru.fraudcore.audit.entity.AuditLog;
import ru.fraudcore.audit.repository.AuditLogRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldSaveAuditLog() {
        AuditService service = new AuditService(auditLogRepository);

        service.log(1L, AuditAction.CASE_CREATED, "CASE", 10L, "details");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void shouldReadTransactionAuditHistory() {
        AuditService service = new AuditService(auditLogRepository);

        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc("TRANSACTION", 1L))
                .thenReturn(List.of(AuditLog.builder().id(1L).build()));

        assertThat(service.getTransactionHistory(1L)).hasSize(1);
    }
}
