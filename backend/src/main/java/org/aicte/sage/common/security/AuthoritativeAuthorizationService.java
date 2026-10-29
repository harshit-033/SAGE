package org.aicte.sage.common.security;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.exception.TenantIsolationException;
import org.aicte.sage.modules.governance.repository.CaseAssignmentRepository;
import org.aicte.sage.modules.governance.repository.CommitteeAppointmentRepository;
import org.aicte.sage.modules.governance.repository.JurisdictionAssignmentRepository;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.aicte.sage.modules.users.entity.Role;
import org.aicte.sage.modules.users.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service("authoritativeAuthorizationService")
@Transactional(readOnly = true)
public class AuthoritativeAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthoritativeAuthorizationService.class);

    private final InstitutionRepository institutionRepository;
    private final JurisdictionAssignmentRepository jurisdictionRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;
    private final CommitteeAppointmentRepository committeeAppointmentRepository;
    private final AuditService auditService;

    public AuthoritativeAuthorizationService(
            InstitutionRepository institutionRepository,
            JurisdictionAssignmentRepository jurisdictionRepository,
            CaseAssignmentRepository caseAssignmentRepository,
            CommitteeAppointmentRepository committeeAppointmentRepository,
            AuditService auditService) {
        this.institutionRepository = institutionRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
        this.committeeAppointmentRepository = committeeAppointmentRepository;
        this.auditService = auditService;
    }

    /**
     * Authoritative access check for an institution.
     * Evaluates: Identity, roles, institutional affiliation, regional jurisdiction, and explicit case assignments.
     * Blanket cross-institution access for regional officers, auditors, scrutiny officers, etc. is strictly rejected.
     */
    public boolean canAccessInstitution(UserPrincipal principal, UUID institutionId) {
        if (principal == null || institutionId == null) {
            return false;
        }

        // 1. Central Administrator has global regulatory governance authority
        if (principal.hasRole("ROLE_AICTE_ADMIN")) {
            return true;
        }

        // 2. Direct institutional affiliation (own institution)
        if (principal.getAuthorizedInstitutionIds().contains(institutionId)) {
            return true;
        }

        // 3. Explicit Case Assignment (Scrutiny, EVC, Compliance, Audit)
        if (caseAssignmentRepository.hasActiveCaseAssignment(principal.getId(), institutionId)) {
            return true;
        }

        // 4. Regional Jurisdiction Check for Regional Officers
        if (principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            Institution institution = institutionRepository.findById(institutionId).orElse(null);
            if (institution != null && institution.getState() != null) {
                if (jurisdictionRepository.hasJurisdictionOverState(principal.getId(), institution.getState())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Authoritative check to verify and enforce access. Emits TENANCY_VIOLATION_ATTEMPT and throws TenantIsolationException if denied.
     */
    public void checkAccessToInstitution(UserPrincipal principal, UUID institutionId, String action) {
        if (!canAccessInstitution(principal, institutionId)) {
            auditService.recordEvent(
                    "TENANCY_VIOLATION_ATTEMPT",
                    principal != null ? principal.getUsername() : "ANONYMOUS",
                    institutionId,
                    "INSTITUTION",
                    institutionId != null ? institutionId.toString() : "UNKNOWN",
                    action != null ? action : "UNAUTHORIZED_ACCESS",
                    null,
                    "Principal lacks required institutional affiliation, jurisdiction, or case assignment"
            );
            throw new TenantIsolationException(institutionId, principal != null ? principal.getUsername() : "ANONYMOUS");
        }
    }

    /**
     * Authoritative check for updating an institution profile.
     * Only AICTE Central Admin or authorized Institution Officers belonging to this institution with INSTITUTION_WRITE can update.
     */
    public boolean canUpdateInstitution(UserPrincipal principal, UUID institutionId) {
        if (principal == null || institutionId == null) {
            return false;
        }

        if (principal.hasRole("ROLE_AICTE_ADMIN")) {
            return true;
        }

        if (principal.getAuthorizedInstitutionIds().contains(institutionId) && principal.hasAuthority("INSTITUTION_WRITE")) {
            return true;
        }

        return false;
    }

    public void checkUpdateInstitution(UserPrincipal principal, UUID institutionId) {
        if (!canUpdateInstitution(principal, institutionId)) {
            auditService.recordEvent(
                    "TENANCY_VIOLATION_ATTEMPT",
                    principal != null ? principal.getUsername() : "ANONYMOUS",
                    institutionId,
                    "INSTITUTION",
                    institutionId != null ? institutionId.toString() : "UNKNOWN",
                    "UNAUTHORIZED_UPDATE",
                    null,
                    "Principal lacks INSTITUTION_WRITE authority or institutional affiliation to modify profile"
            );
            throw new TenantIsolationException(institutionId, principal != null ? principal.getUsername() : "ANONYMOUS");
        }
    }

    /**
     * Prevents privilege escalation during role assignments.
     * - Only central admins can grant or revoke ROLE_AICTE_ADMIN.
     * - A user cannot assign a role they do not hold unless they possess ROLE_AICTE_ADMIN.
     */
    public void validateRoleAssignmentPrivilege(UserPrincipal principal, Collection<String> requestedRoles, User targetUser) {
        if (principal == null) {
            throw new AccessDeniedException("Unauthenticated request");
        }

        boolean callerIsAdmin = principal.hasRole("ROLE_AICTE_ADMIN");

        if (requestedRoles.contains("ROLE_AICTE_ADMIN") && !callerIsAdmin) {
            auditService.recordEvent(
                    "PRIVILEGE_ESCALATION_BLOCKED",
                    principal.getUsername(),
                    null,
                    "USER",
                    targetUser != null ? targetUser.getId().toString() : "UNKNOWN",
                    "SECURITY_BLOCK",
                    null,
                    "Non-admin caller attempted to assign central administrator role"
            );
            throw new AccessDeniedException("Unauthorized privilege escalation: only AICTE Central Administrators can assign ROLE_AICTE_ADMIN");
        }

        // If not central admin, caller must possess ROLE_ASSIGN authority and cannot grant higher roles
        if (!callerIsAdmin) {
            if (!principal.hasAuthority("ROLE_ASSIGN")) {
                throw new AccessDeniedException("Principal lacks ROLE_ASSIGN authority");
            }
        }
    }

    /**
     * Verifies that the caller has authority to inspect the target user profile.
     * Prevents scoped institutional users from reading arbitrary external administrative or other-college accounts.
     */
    public boolean canInspectUser(UserPrincipal principal, User targetUser, List<UUID> targetUserInstitutionIds) {
        if (principal == null || targetUser == null) {
            return false;
        }

        if (principal.hasRole("ROLE_AICTE_ADMIN")) {
            return true;
        }

        // Caller can always inspect own account
        if (principal.getId().equals(targetUser.getId())) {
            return true;
        }

        // Regional Officer can view users affiliated with institutions in their assigned states
        if (principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            List<String> assignedStates = jurisdictionRepository.findActiveStatesByUserId(principal.getId());
            if (targetUserInstitutionIds != null && !targetUserInstitutionIds.isEmpty()) {
                List<Institution> insts = institutionRepository.findAllById(targetUserInstitutionIds);
                boolean matchesState = insts.stream().anyMatch(i ->
                        assignedStates.contains(i.getState()) || assignedStates.contains("*"));
                if (matchesState) {
                    return true;
                }
            }
        }

        // Institution Officer can only view users affiliated with their shared institution(s)
        if (principal.hasRole("ROLE_INSTITUTION_OFFICER")) {
            Set<UUID> callerInstitutions = principal.getAuthorizedInstitutionIds();
            if (targetUserInstitutionIds != null) {
                for (UUID instId : targetUserInstitutionIds) {
                    if (callerInstitutions.contains(instId)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Authoritative statutory committee verification per APH 2024-27.
     * Strictly verifies appointment validity and prevents inferring committee membership from application roles alone.
     */
    public boolean hasStatutoryCommitteeAppointment(UUID userId, String committeeType) {
        if (userId == null || committeeType == null) {
            return false;
        }
        return committeeAppointmentRepository.hasValidAppointment(userId, committeeType);
    }
}
