package in.gov.aicte.evc.repository;

import in.gov.aicte.evc.entity.Expert;
import in.gov.aicte.evc.entity.ExpertAvailabilityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Spring Data JPA repository for {@link ExpertAvailabilityRecord}.
 */
@Repository
public interface ExpertAvailabilityRepository extends JpaRepository<ExpertAvailabilityRecord, Long> {

    /**
     * Finds all availability records for a given expert entity.
     *
     * @param expert the expert
     * @return list of availability records
     */
    List<ExpertAvailabilityRecord> findByExpert(Expert expert);

    /**
     * Finds all availability records for an expert identified by primary key.
     *
     * @param expertId the expert primary key
     * @return list of availability records
     */
    List<ExpertAvailabilityRecord> findByExpertId(Long expertId);

    /**
     * Finds availability records for an expert within a specific date range (inclusive).
     *
     * @param expertId the expert primary key
     * @param startDate start of date range (inclusive)
     * @param endDate end of date range (inclusive)
     * @return list of availability records
     */
    List<ExpertAvailabilityRecord> findByExpertIdAndAvailableDateBetween(Long expertId, LocalDate startDate, LocalDate endDate);

    /**
     * Batch queries availability records for multiple experts within a specific date range (inclusive).
     * Used by the scheduling orchestration service to load horizon availability in a single efficient query.
     *
     * @param expertIds collection of expert primary keys
     * @param startDate start of date range (inclusive)
     * @param endDate end of date range (inclusive)
     * @return list of availability records
     */
    List<ExpertAvailabilityRecord> findByExpertIdInAndAvailableDateBetween(Collection<Long> expertIds, LocalDate startDate, LocalDate endDate);

    /**
     * Checks if an availability record already exists for the given expert and date.
     *
     * @param expertId the expert primary key
     * @param availableDate the calendar date
     * @return true if record exists, false otherwise
     */
    boolean existsByExpertIdAndAvailableDate(Long expertId, LocalDate availableDate);

    /**
     * Deletes all availability records for a given expert.
     *
     * @param expertId the expert primary key
     * @return count of deleted records
     */
    long deleteByExpertId(Long expertId);

    /**
     * Deletes a specific availability date for an expert.
     *
     * @param expertId the expert primary key
     * @param availableDate the date to delete
     * @return count of deleted records
     */
    long deleteByExpertIdAndAvailableDate(Long expertId, LocalDate availableDate);
}

