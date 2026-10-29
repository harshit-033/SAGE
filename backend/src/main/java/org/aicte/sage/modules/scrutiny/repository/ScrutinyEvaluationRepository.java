package org.aicte.sage.modules.scrutiny.repository;

import org.aicte.sage.modules.scrutiny.model.ScrutinyEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScrutinyEvaluationRepository extends JpaRepository<ScrutinyEvaluation, UUID> {
    List<ScrutinyEvaluation> findByApplicationIdOrderByEvaluatedAtDesc(UUID applicationId);
}
