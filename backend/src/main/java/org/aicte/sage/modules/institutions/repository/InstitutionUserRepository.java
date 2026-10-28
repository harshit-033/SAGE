package org.aicte.sage.modules.institutions.repository;

import org.aicte.sage.modules.institutions.entity.InstitutionUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstitutionUserRepository extends JpaRepository<InstitutionUser, UUID> {
    List<InstitutionUser> findAllByUserId(UUID userId);
    List<InstitutionUser> findAllByInstitutionId(UUID institutionId);
    Optional<InstitutionUser> findByInstitutionIdAndUserId(UUID institutionId, UUID userId);
    boolean existsByInstitutionIdAndUserId(UUID institutionId, UUID userId);
}
