package in.gov.aicte.evc.repository;

import in.gov.aicte.evc.entity.EvcInspection;
import in.gov.aicte.evc.entity.InspectionStatus;
import in.gov.aicte.evc.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link EvcInspection}.
 */
@Repository
public interface EvcInspectionRepository extends JpaRepository<EvcInspection, Long> {

    /**
     * Finds all inspections for a specific institution.
     *
     * @param institution the institution
     * @return list of inspections
     */
    List<EvcInspection> findByInstitution(Institution institution);

    /**
     * Finds all inspections for an institution identified by primary key.
     *
     * @param institutionId the institution ID
     * @return list of inspections
     */
    List<EvcInspection> findByInstitutionId(Long institutionId);

    /**
     * Finds all inspections with a specific status.
     *
     * @param status the inspection status
     * @return list of inspections
     */
    List<EvcInspection> findByStatus(InspectionStatus status);
}

