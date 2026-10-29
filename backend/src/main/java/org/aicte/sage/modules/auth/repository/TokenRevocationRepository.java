package org.aicte.sage.modules.auth.repository;

import org.aicte.sage.modules.auth.entity.TokenRevocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface TokenRevocationRepository extends JpaRepository<TokenRevocation, UUID> {

    boolean existsByTokenId(String tokenId);

    void deleteAllByExpiresAtBefore(Instant now);
}
