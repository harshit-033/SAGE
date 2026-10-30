package org.aicte.sage.modules.applications.repository;

import org.aicte.sage.modules.applications.model.ApplicationBranchDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationBranchDetailsRepository extends JpaRepository<ApplicationBranchDetails, UUID> {
    Optional<ApplicationBranchDetails> findByApplicationId(UUID applicationId);
    boolean existsByApplicationId(UUID applicationId);
}
