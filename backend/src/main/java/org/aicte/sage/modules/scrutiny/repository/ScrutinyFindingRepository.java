package org.aicte.sage.modules.scrutiny.repository;

import org.aicte.sage.modules.scrutiny.model.FindingStatus;
import org.aicte.sage.modules.scrutiny.model.ScrutinyFinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScrutinyFindingRepository extends JpaRepository<ScrutinyFinding, UUID> {
    List<ScrutinyFinding> findByApplicationId(UUID applicationId);
    List<ScrutinyFinding> findByEvaluationId(UUID evaluationId);
    List<ScrutinyFinding> findByApplicationIdAndStatus(UUID applicationId, FindingStatus status);
    List<ScrutinyFinding> findByApplicationIdAndResolvedFalse(UUID applicationId);
    List<ScrutinyFinding> findByDocumentId(UUID documentId);
    List<ScrutinyFinding> findByDocumentIdIn(List<UUID> documentIds);
}
