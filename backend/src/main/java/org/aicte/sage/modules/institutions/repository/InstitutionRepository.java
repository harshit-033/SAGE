package org.aicte.sage.modules.institutions.repository;

import org.aicte.sage.modules.institutions.entity.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT i FROM Institution i WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.code) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.city) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:state IS NULL OR :state = '' OR i.state = :state) AND " +
           "(:category IS NULL OR :category = '' OR i.category = :category) AND " +
           "(:active IS NULL OR i.active = :active)")
    Page<Institution> searchInstitutions(@Param("query") String query,
                                         @Param("state") String state,
                                         @Param("category") String category,
                                         @Param("active") Boolean active,
                                         Pageable pageable);

    @Query("SELECT i FROM Institution i WHERE i.id IN " +
           "(SELECT iu.institutionId FROM InstitutionUser iu WHERE iu.userId = :userId) AND " +
           "(:query IS NULL OR :query = '' OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.code) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.city) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:state IS NULL OR :state = '' OR i.state = :state) AND " +
           "(:category IS NULL OR :category = '' OR i.category = :category) AND " +
           "(:active IS NULL OR i.active = :active)")
    Page<Institution> searchAffiliatedInstitutions(@Param("userId") UUID userId,
                                                  @Param("query") String query,
                                                  @Param("state") String state,
                                                  @Param("category") String category,
                                                  @Param("active") Boolean active,
                                                  Pageable pageable);

    boolean existsByCode(String code);
}
