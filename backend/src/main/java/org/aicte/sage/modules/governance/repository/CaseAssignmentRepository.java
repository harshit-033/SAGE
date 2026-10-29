package org.aicte.sage.modules.governance.repository;

import org.aicte.sage.modules.governance.entity.CaseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CaseAssignmentRepository extends JpaRepository<CaseAssignment, UUID> {

    List<CaseAssignment> findAllByUserIdAndActiveTrue(UUID userId);

    @Query("SELECT c.institutionId FROM CaseAssignment c WHERE c.userId = :userId AND c.active = true")
    List<UUID> findActiveInstitutionIdsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(c) > 0 FROM CaseAssignment c " +
           "WHERE c.userId = :userId AND c.institutionId = :institutionId AND c.active = true")
    boolean hasActiveCaseAssignment(@Param("userId") UUID userId, @Param("institutionId") UUID institutionId);

    @Query("SELECT COUNT(c) > 0 FROM CaseAssignment c " +
           "WHERE c.userId = :userId AND c.institutionId = :institutionId AND c.assignmentType = :type AND c.active = true")
    boolean hasActiveCaseAssignmentOfType(@Param("userId") UUID userId,
                                          @Param("institutionId") UUID institutionId,
                                          @Param("type") String type);
}
