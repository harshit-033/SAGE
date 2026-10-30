package in.gov.aicte.evc.repository;

import in.gov.aicte.evc.entity.CommitteeAssignment;
import in.gov.aicte.evc.entity.EvcInspection;
import in.gov.aicte.evc.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link CommitteeAssignment}.
 */
@Repository
public interface CommitteeAssignmentRepository extends JpaRepository<CommitteeAssignment, Long> {

    /**
     * Finds all committee assignments for a specific inspection.
     *
     * @param inspection the inspection
     * @return list of assignments
     */
    List<CommitteeAssignment> findByInspection(EvcInspection inspection);

    /**
     * Finds all committee assignments for an inspection identified by primary key.
     *
     * @param inspectionId the inspection ID
     * @return list of assignments
     */
    List<CommitteeAssignment> findByInspectionId(Long inspectionId);

    /**
     * Finds all committee assignments for a specific expert.
     *
     * @param expert the expert
     * @return list of assignments
     */
    List<CommitteeAssignment> findByExpert(Expert expert);

    /**
     * Finds all committee assignments for an expert identified by primary key.
     *
     * @param expertId the expert ID
     * @return list of assignments
     */
    List<CommitteeAssignment> findByExpertId(Long expertId);

    /**
     * Finds a specific assignment for an inspection and expert.
     *
     * @param inspectionId the inspection ID
     * @param expertId the expert ID
     * @return optional containing the assignment if found
     */
    Optional<CommitteeAssignment> findByInspectionIdAndExpertId(Long inspectionId, Long expertId);

    /**
     * Checks if an assignment already exists for the given inspection and expert.
     *
     * @param inspectionId the inspection ID
     * @param expertId the expert ID
     * @return true if exists, false otherwise
     */
    boolean existsByInspectionIdAndExpertId(Long inspectionId, Long expertId);
}

