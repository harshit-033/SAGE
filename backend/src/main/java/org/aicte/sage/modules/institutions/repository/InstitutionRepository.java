package org.aicte.sage.modules.institutions.repository;

import org.aicte.sage.modules.institutions.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, UUID> {
    Optional<Institution> findByCode(String code);

    @Query("SELECT i FROM Institution i WHERE i.id IN " +
           "(SELECT iu.institutionId FROM InstitutionUser iu WHERE iu.userId = :userId)")
    List<Institution> findAllByAffiliatedUserId(@Param("userId") UUID userId);

    boolean existsByCode(String code);
}
