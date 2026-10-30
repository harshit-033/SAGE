package org.aicte.sage.modules.evc.repository;

import org.aicte.sage.modules.evc.entity.ExpertAvailabilityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpertAvailabilityRepository extends JpaRepository<ExpertAvailabilityRecord, UUID> {

    @Query("SELECT r FROM ExpertAvailabilityRecord r WHERE r.expert.id IN :expertIds AND r.availableDate >= :start AND r.availableDate <= :end")
    List<ExpertAvailabilityRecord> findByExpertIdInAndAvailableDateBetween(
            @Param("expertIds") Collection<UUID> expertIds,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    List<ExpertAvailabilityRecord> findByExpertId(UUID expertId);

    void deleteByExpertIdAndAvailableDate(UUID expertId, LocalDate availableDate);
}
