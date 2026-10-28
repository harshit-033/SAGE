package org.aicte.sage.common.security;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.TenantIsolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenancyEvaluator")
public class TenancyAuthorizationEvaluator {

    private final AuditService auditService;

    public TenancyAuthorizationEvaluator(AuditService auditService) {
        this.auditService = auditService;
    }

    public boolean hasAccessToInstitution(Authentication authentication, UUID institutionId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principalObj = authentication.getPrincipal();
        if (!(principalObj instanceof UserPrincipal principal)) {
            return false;
        }

        // Global administrators have cross-institution regulatory access
        if (principal.hasRole("ROLE_AICTE_ADMIN")) {
            return true;
        }

        boolean authorized = principal.isAuthorizedForInstitution(institutionId);
        if (!authorized) {
            auditService.recordEvent(
                    "TENANCY_VIOLATION_ATTEMPT",
                    principal.getUsername(),
                    institutionId,
                    "INSTITUTION",
                    institutionId != null ? institutionId.toString() : "UNKNOWN",
                    "UNAUTHORIZED_ACCESS",
                    null,
                    "Principal attempted to access institution without explicit institutional affiliation"
            );
            throw new TenantIsolationException(institutionId, principal.getUsername());
        }

        return true;
    }

    public boolean hasAccessToInstitution(Authentication authentication, String institutionIdStr) {
        try {
            UUID institutionId = UUID.fromString(institutionIdStr);
            return hasAccessToInstitution(authentication, institutionId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
