package org.aicte.sage.modules.governance.repository;

import org.aicte.sage.modules.governance.entity.CommitteeAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommitteeAppointmentRepository extends JpaRepository<CommitteeAppointment, UUID> {

    List<CommitteeAppointment> findAllByUserIdAndActiveTrue(UUID userId);

    @Query("SELECT c.committeeType FROM CommitteeAppointment c " +
           "WHERE c.userId = :userId AND c.active = true " +
           "AND (c.validFrom IS NULL OR c.validFrom <= CURRENT_TIMESTAMP) " +
           "AND (c.validUntil IS NULL OR c.validUntil >= CURRENT_TIMESTAMP)")
    List<String> findActiveCommitteeTypesByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(c) > 0 FROM CommitteeAppointment c " +
           "WHERE c.userId = :userId AND c.committeeType = :committeeType AND c.active = true " +
           "AND (c.validFrom IS NULL OR c.validFrom <= CURRENT_TIMESTAMP) " +
           "AND (c.validUntil IS NULL OR c.validUntil >= CURRENT_TIMESTAMP)")
    boolean hasValidAppointment(@Param("userId") UUID userId, @Param("committeeType") String committeeType);
}
