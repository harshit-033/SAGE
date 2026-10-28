package org.aicte.sage;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.TenantIsolationException;
import org.aicte.sage.common.security.TenancyAuthorizationEvaluator;
import org.aicte.sage.common.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenancyAuthorizationEvaluatorTest {

    private AuditService auditService;
    private TenancyAuthorizationEvaluator evaluator;

    private final UUID instA = UUID.randomUUID();
    private final UUID instB = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        auditService = Mockito.mock(AuditService.class);
        evaluator = new TenancyAuthorizationEvaluator(auditService);
    }

    @Test
    @DisplayName("Admin user should be granted access to any institution")
    void testAdminAccessAllowed() {
        UserPrincipal admin = new UserPrincipal(
                UUID.randomUUID(), "admin", "admin@aicte.gov.in", "pass", "Admin", true,
                List.of(new SimpleGrantedAuthority("ROLE_AICTE_ADMIN")), Set.of()
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        assertTrue(evaluator.hasAccessToInstitution(auth, instA));
        assertTrue(evaluator.hasAccessToInstitution(auth, instB));
    }

    @Test
    @DisplayName("Institution officer should be granted access only to their own institution")
    void testTenantAccessAllowedForOwnInstitution() {
        UserPrincipal officer = new UserPrincipal(
                UUID.randomUUID(), "officer", "officer@college.edu", "pass", "Officer", true,
                List.of(new SimpleGrantedAuthority("ROLE_INSTITUTION_OFFICER")), Set.of(instA)
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(officer, null, officer.getAuthorities());

        assertTrue(evaluator.hasAccessToInstitution(auth, instA));
    }

    @Test
    @DisplayName("Institution officer accessing another institution should throw TenantIsolationException and audit")
    void testTenantAccessDeniedForForeignInstitution() {
        UserPrincipal officer = new UserPrincipal(
                UUID.randomUUID(), "officer", "officer@college.edu", "pass", "Officer", true,
                List.of(new SimpleGrantedAuthority("ROLE_INSTITUTION_OFFICER")), Set.of(instA)
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(officer, null, officer.getAuthorities());

        assertThrows(TenantIsolationException.class, () -> evaluator.hasAccessToInstitution(auth, instB));
        Mockito.verify(auditService, Mockito.times(1))
                .recordEvent(Mockito.eq("TENANCY_VIOLATION_ATTEMPT"), Mockito.any(), Mockito.eq(instB), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}
