package org.aicte.sage.common.exception;

import java.util.UUID;

public class TenantIsolationException extends RuntimeException {
    private final UUID targetInstitutionId;
    private final String attemptedByPrincipal;

    public TenantIsolationException(UUID targetInstitutionId, String attemptedByPrincipal) {
        super(String.format("Cross-tenant access violation: Principal '%s' is not authorized to access Institution ID: '%s'",
                attemptedByPrincipal, targetInstitutionId));
        this.targetInstitutionId = targetInstitutionId;
        this.attemptedByPrincipal = attemptedByPrincipal;
    }

    public UUID getTargetInstitutionId() {
        return targetInstitutionId;
    }

    public String getAttemptedByPrincipal() {
        return attemptedByPrincipal;
    }
}
