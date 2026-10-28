package org.aicte.sage.modules.users.repository;

import org.aicte.sage.modules.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByIdentifierWithRoles(@Param("identifier") String identifier);

    @Query("SELECT u FROM User u WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:active IS NULL OR u.active = :active)")
    Page<User> searchUsers(@Param("query") String query,
                           @Param("active") Boolean active,
                           Pageable pageable);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
