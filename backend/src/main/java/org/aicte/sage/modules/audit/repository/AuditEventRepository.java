package org.aicte.sage.modules.audit.repository;

import org.aicte.sage.modules.audit.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    List<AuditEvent> findAllByInstitutionIdOrderByCreatedAtDesc(UUID institutionId);
    List<AuditEvent> findAllByPrincipalOrderByCreatedAtDesc(String principal);
    List<AuditEvent> findAllByCorrelationId(String correlationId);
}
