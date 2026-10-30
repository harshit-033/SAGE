package in.gov.aicte.evc.repository;

import in.gov.aicte.evc.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Institution}.
 */
@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    /**
     * Finds an institution by its unique AICTE identifier.
     *
     * @param aicteId the AICTE identifier
     * @return optional containing the institution if found
     */
    Optional<Institution> findByAicteId(String aicteId);

    /**
     * Checks if an institution exists with the given AICTE identifier.
     *
     * @param aicteId the AICTE identifier
     * @return true if exists, false otherwise
     */
    boolean existsByAicteId(String aicteId);
}
