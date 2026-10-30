package org.aicte.sage.modules.applications.repository;

import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Optional<Application> findByApplicationNumber(String applicationNumber);

    Page<Application> findByInstitutionId(UUID institutionId, Pageable pageable);

    Page<Application> findByCurrentState(ApplicationState state, Pageable pageable);

    Page<Application> findByInstitutionIdAndCurrentState(UUID institutionId, ApplicationState state, Pageable pageable);

    Page<Application> findByAssignedToUserId(UUID userId, Pageable pageable);

    long countByInstitutionIdAndAcademicYearAndCategory(
            UUID institutionId,
            String academicYear,
            ApplicationCategory category
    );

    @Query("SELECT a FROM Application a WHERE (:institutionId IS NULL OR a.institution.id = :institutionId) " +
           "AND (:state IS NULL OR a.currentState = :state) " +
           "AND (:category IS NULL OR a.category = :category)")
    Page<Application> searchApplications(
            @Param("institutionId") UUID institutionId,
            @Param("state") ApplicationState state,
            @Param("category") ApplicationCategory category,
            Pageable pageable
    );

    @Query("SELECT a FROM Application a WHERE a.institution.id IN :institutionIds " +
           "AND (:state IS NULL OR a.currentState = :state) " +
           "AND (:category IS NULL OR a.category = :category)")
    Page<Application> searchApplicationsByInstitutionIds(
            @Param("institutionIds") java.util.Collection<UUID> institutionIds,
            @Param("state") ApplicationState state,
            @Param("category") ApplicationCategory category,
            Pageable pageable
    );

    @Query("SELECT a FROM Application a WHERE LOWER(a.institution.state) IN :states " +
           "AND (:institutionId IS NULL OR a.institution.id = :institutionId) " +
           "AND (:state IS NULL OR a.currentState = :state) " +
           "AND (:category IS NULL OR a.category = :category)")
    Page<Application> searchApplicationsByStates(
            @Param("states") java.util.Collection<String> states,
            @Param("institutionId") UUID institutionId,
            @Param("state") ApplicationState state,
            @Param("category") ApplicationCategory category,
            Pageable pageable
    );
}
