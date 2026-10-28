package org.aicte.sage.modules.users.repository;

import org.aicte.sage.modules.users.entity.User;
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

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
