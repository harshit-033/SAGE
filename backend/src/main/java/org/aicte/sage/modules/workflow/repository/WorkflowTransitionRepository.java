package org.aicte.sage.modules.workflow.repository;

import org.aicte.sage.modules.workflow.model.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, UUID> {
    List<WorkflowTransition> findByApplicationIdOrderByCreatedAtAsc(UUID applicationId);
}
