package org.aicte.sage.modules.applications.repository;

import org.aicte.sage.modules.applications.model.ApplicationCourseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationCourseDetailsRepository extends JpaRepository<ApplicationCourseDetails, UUID> {
    Optional<ApplicationCourseDetails> findByApplicationId(UUID applicationId);
    boolean existsByApplicationId(UUID applicationId);
}
