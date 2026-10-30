package org.aicte.sage.modules.evc.repository;

import org.aicte.sage.modules.evc.entity.EvcInspection;
import org.aicte.sage.modules.evc.entity.InspectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvcInspectionRepository extends JpaRepository<EvcInspection, UUID> {

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"institution", "application"})
    Optional<EvcInspection> findById(UUID id);

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"institution", "application"})
    List<EvcInspection> findAllById(Iterable<UUID> ids);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"institution", "application"})
    List<EvcInspection> findByStatus(InspectionStatus status);

    List<EvcInspection> findByInstitutionId(UUID institutionId);

    Page<EvcInspection> findByInstitutionId(UUID institutionId, Pageable pageable);

    Optional<EvcInspection> findByApplicationId(UUID applicationId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"institution", "application"})
    @Query("SELECT i FROM EvcInspection i WHERE i.status = :status AND i.institution.id IN :institutionIds")
    List<EvcInspection> findByStatusAndInstitutionIdIn(
            @Param("status") InspectionStatus status,
            @Param("institutionIds") Collection<UUID> institutionIds
    );

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"institution", "application"})
    @Query("SELECT i FROM EvcInspection i WHERE i.institution.id IN :institutionIds")
    Page<EvcInspection> findByInstitutionIdIn(
            @Param("institutionIds") Collection<UUID> institutionIds,
            Pageable pageable
    );
}
