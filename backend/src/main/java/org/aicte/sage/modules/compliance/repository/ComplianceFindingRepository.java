package org.aicte.sage.modules.compliance.repository;

import org.aicte.sage.modules.compliance.entity.ComplianceFinding;
import org.aicte.sage.modules.compliance.entity.ComplianceFindingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplianceFindingRepository extends JpaRepository<ComplianceFinding, UUID> {

    Page<ComplianceFinding> findByInstitutionId(UUID institutionId, Pageable pageable);

    Page<ComplianceFinding> findByInstitutionIdAndStatus(UUID institutionId, ComplianceFindingStatus status, Pageable pageable);

    Page<ComplianceFinding> findByStatus(ComplianceFindingStatus status, Pageable pageable);

    Optional<ComplianceFinding> findByInstitutionIdAndRuleCodeAndStatus(
            UUID institutionId, String ruleCode, ComplianceFindingStatus status);

    long countByInstitutionIdAndStatus(UUID institutionId, ComplianceFindingStatus status);

    long countByStatus(ComplianceFindingStatus status);
}
