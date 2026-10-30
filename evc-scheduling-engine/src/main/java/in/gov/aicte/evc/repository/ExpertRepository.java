package in.gov.aicte.evc.repository;

import in.gov.aicte.evc.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Expert}.
 */
@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {

    /**
     * Finds all active experts eligible for committee assignment.
     *
     * @return list of active experts
     */
    List<Expert> findByActiveTrue();

    /**
     * Finds experts by their academic or professional discipline (case-insensitive).
     *
     * @param discipline academic or technical discipline
     * @return list of matching experts
     */
    List<Expert> findByDisciplineIgnoreCase(String discipline);
}
