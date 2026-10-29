package org.aicte.sage.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenancyEvaluator")
public class TenancyAuthorizationEvaluator {

    private final AuthoritativeAuthorizationService authorizationService;

    public TenancyAuthorizationEvaluator(AuthoritativeAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public boolean hasAccessToInstitution(Authentication authentication, UUID institutionId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principalObj = authentication.getPrincipal();
        if (!(principalObj instanceof UserPrincipal principal)) {
            return false;
        }

        boolean authorized = authorizationService.canAccessInstitution(principal, institutionId);
        if (!authorized) {
            authorizationService.checkAccessToInstitution(principal, institutionId, "UNAUTHORIZED_ACCESS");
        }
        return true;
    }

    public boolean canUpdateInstitution(Authentication authentication, UUID institutionId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principalObj = authentication.getPrincipal();
        if (!(principalObj instanceof UserPrincipal principal)) {
            return false;
        }

        boolean authorized = authorizationService.canUpdateInstitution(principal, institutionId);
        if (!authorized) {
            authorizationService.checkUpdateInstitution(principal, institutionId);
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
