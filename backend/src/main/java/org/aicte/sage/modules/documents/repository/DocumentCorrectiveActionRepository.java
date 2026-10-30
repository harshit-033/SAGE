package org.aicte.sage.modules.documents.repository;

import org.aicte.sage.modules.documents.model.CorrectiveActionStatus;
import org.aicte.sage.modules.documents.model.DocumentCorrectiveAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentCorrectiveActionRepository extends JpaRepository<DocumentCorrectiveAction, UUID> {

    List<DocumentCorrectiveAction> findByDocumentId(UUID documentId);

    List<DocumentCorrectiveAction> findByApplicationId(UUID applicationId);

    @Query("SELECT a FROM DocumentCorrectiveAction a WHERE " +
           "(:institutionId IS NULL OR a.institution.id = :institutionId) AND " +
           "(:status IS NULL OR a.status = :status) " +
           "ORDER BY a.createdAt DESC")
    Page<DocumentCorrectiveAction> searchActions(
            @Param("institutionId") UUID institutionId,
            @Param("status") CorrectiveActionStatus status,
            Pageable pageable
    );

    long countByInstitutionIdAndStatus(UUID institutionId, CorrectiveActionStatus status);

    long countByInstitutionId(UUID institutionId);

    @Query("SELECT COUNT(a) FROM DocumentCorrectiveAction a WHERE a.institution.id = :institutionId AND a.status IN ('OPEN', 'IN_PROGRESS', 'SUBMITTED_FOR_REVIEW', 'OVERDUE')")
    long countPendingActionsByInstitution(@Param("institutionId") UUID institutionId);

    Optional<DocumentCorrectiveAction> findByDocumentIdAndActionTypeAndStatus(
            UUID documentId, String actionType, CorrectiveActionStatus status);
}
