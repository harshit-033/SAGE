package org.aicte.sage.modules.deadlines.repository;

import org.aicte.sage.modules.deadlines.entity.Deadline;
import org.aicte.sage.modules.deadlines.entity.DeadlineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeadlineRepository extends JpaRepository<Deadline, UUID> {

    Page<Deadline> findByInstitutionId(UUID institutionId, Pageable pageable);

    Page<Deadline> findByInstitutionIdAndStatus(UUID institutionId, DeadlineStatus status, Pageable pageable);

    Page<Deadline> findByStatus(DeadlineStatus status, Pageable pageable);

    Optional<Deadline> findByEntityTypeAndEntityIdAndDeadlineCode(String entityType, UUID entityId, String deadlineCode);

    List<Deadline> findByStatusIn(List<DeadlineStatus> statuses);

    @Query("SELECT d FROM Deadline d WHERE d.status IN :statuses AND d.targetDate <= :cutoff")
    List<Deadline> findActiveDeadlinesDueBefore(
            @Param("statuses") List<DeadlineStatus> statuses,
            @Param("cutoff") Instant cutoff);

    long countByInstitutionIdAndStatus(UUID institutionId, DeadlineStatus status);

    long countByStatus(DeadlineStatus status);
}
