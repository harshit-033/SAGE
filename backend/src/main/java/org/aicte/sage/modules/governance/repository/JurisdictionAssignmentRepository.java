package org.aicte.sage.modules.governance.repository;

import org.aicte.sage.modules.governance.entity.JurisdictionAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JurisdictionAssignmentRepository extends JpaRepository<JurisdictionAssignment, UUID> {

    List<JurisdictionAssignment> findAllByUserIdAndActiveTrue(UUID userId);

    @Query("SELECT j.state FROM JurisdictionAssignment j WHERE j.userId = :userId AND j.active = true")
    List<String> findActiveStatesByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(j) > 0 FROM JurisdictionAssignment j " +
           "WHERE j.userId = :userId AND j.active = true " +
           "AND (LOWER(j.state) = LOWER(:state) OR j.state = '*')")
    boolean hasJurisdictionOverState(@Param("userId") UUID userId, @Param("state") String state);
}
