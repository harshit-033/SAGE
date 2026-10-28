package org.aicte.sage.modules.audit.repository;

import org.aicte.sage.modules.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    List<AuditEvent> findAllByInstitutionIdOrderByCreatedAtDesc(UUID institutionId);
    List<AuditEvent> findAllByPrincipalOrderByCreatedAtDesc(String principal);
    List<AuditEvent> findAllByCorrelationId(String correlationId);

    @Query("SELECT a FROM AuditEvent a WHERE " +
           "(:principal IS NULL OR :principal = '' OR LOWER(a.principal) LIKE LOWER(CONCAT('%', :principal, '%'))) AND " +
           "(:eventType IS NULL OR :eventType = '' OR a.eventType = :eventType) AND " +
           "(:resourceType IS NULL OR :resourceType = '' OR a.resourceType = :resourceType) AND " +
           "(:correlationId IS NULL OR :correlationId = '' OR a.correlationId = :correlationId)")
    Page<AuditEvent> searchAuditEvents(@Param("principal") String principal,
                                       @Param("eventType") String eventType,
                                       @Param("resourceType") String resourceType,
                                       @Param("correlationId") String correlationId,
                                       Pageable pageable);
}
