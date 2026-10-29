package org.aicte.sage.modules.workflow.repository;

import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.workflow.model.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {
    Optional<WorkflowDefinition> findByCode(String code);
    Optional<WorkflowDefinition> findFirstByCategoryAndActiveTrue(ApplicationCategory category);
}
