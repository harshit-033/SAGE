package org.aicte.sage.modules.notifications.repository;

import org.aicte.sage.modules.notifications.entity.Alert;
import org.aicte.sage.modules.notifications.entity.AlertSeverity;
import org.aicte.sage.modules.notifications.entity.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    Optional<Alert> findByDeduplicationHash(String deduplicationHash);

    Page<Alert> findByInstitutionId(UUID institutionId, Pageable pageable);

    Page<Alert> findByInstitutionIdAndStatus(UUID institutionId, AlertStatus status, Pageable pageable);

    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE (a.recipientUser.id = :userId OR a.targetRole IN :roles) AND (:institutionId IS NULL OR a.institution.id = :institutionId)")
    Page<Alert> findAlertsForUser(
            @Param("userId") UUID userId,
            @Param("roles") List<String> roles,
            @Param("institutionId") UUID institutionId,
            Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE a.institution.id IN :institutionIds OR a.jurisdictionState IN :states")
    Page<Alert> findScopedAlerts(
            @Param("institutionIds") List<UUID> institutionIds,
            @Param("states") List<String> states,
            Pageable pageable);

    long countByInstitutionIdAndStatus(UUID institutionId, AlertStatus status);

    long countByStatus(AlertStatus status);

    long countBySeverityAndStatus(AlertSeverity severity, AlertStatus status);
}
