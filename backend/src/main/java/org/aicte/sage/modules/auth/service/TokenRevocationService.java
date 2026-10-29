package org.aicte.sage.modules.auth.service;

import org.aicte.sage.modules.auth.entity.TokenRevocation;
import org.aicte.sage.modules.auth.repository.TokenRevocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenRevocationService {

    private static final Logger log = LoggerFactory.getLogger(TokenRevocationService.class);

    private final TokenRevocationRepository tokenRevocationRepository;
    private final Set<String> revokedTokenCache = ConcurrentHashMap.newKeySet();

    public TokenRevocationService(TokenRevocationRepository tokenRevocationRepository) {
        this.tokenRevocationRepository = tokenRevocationRepository;
    }

    @Transactional
    public void revokeToken(String tokenId, UUID userId, Instant expiresAt, String reason) {
        if (tokenId == null || tokenId.isBlank()) {
            return;
        }
        revokedTokenCache.add(tokenId);

        try {
            if (!tokenRevocationRepository.existsByTokenId(tokenId)) {
                Instant expiry = expiresAt != null ? expiresAt : Instant.now().plusSeconds(86400);
                TokenRevocation revocation = new TokenRevocation(tokenId, userId, expiry, reason);
                tokenRevocationRepository.save(revocation);
                log.info("Server-side token revocation registered for jti={} user={}", tokenId, userId);
            }
        } catch (Exception e) {
            log.warn("Failed to persist token revocation record to DB, cached in-memory: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public boolean isTokenRevoked(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }
        if (revokedTokenCache.contains(tokenId)) {
            return true;
        }
        boolean exists = tokenRevocationRepository.existsByTokenId(tokenId);
        if (exists) {
            revokedTokenCache.add(tokenId);
        }
        return exists;
    }
}
