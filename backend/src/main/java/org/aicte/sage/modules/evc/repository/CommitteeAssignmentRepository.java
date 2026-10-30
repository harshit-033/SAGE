package org.aicte.sage.modules.evc.repository;

import org.aicte.sage.modules.evc.entity.CommitteeAssignment;
import org.aicte.sage.modules.evc.entity.EvcInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommitteeAssignmentRepository extends JpaRepository<CommitteeAssignment, UUID> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"expert"})
    List<CommitteeAssignment> findByInspection(EvcInspection inspection);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"expert"})
    List<CommitteeAssignment> findByInspectionId(UUID inspectionId);

    List<CommitteeAssignment> findByExpertId(UUID expertId);

    void deleteByInspection(EvcInspection inspection);
}
