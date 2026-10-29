package org.aicte.sage.modules.compliance.repository;

import org.aicte.sage.modules.compliance.entity.ComplianceCheckRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComplianceCheckRunRepository extends JpaRepository<ComplianceCheckRun, UUID> {
    Page<ComplianceCheckRun> findByInstitutionId(UUID institutionId, Pageable pageable);
}
