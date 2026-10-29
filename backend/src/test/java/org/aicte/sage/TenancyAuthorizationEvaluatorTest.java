package org.aicte.sage;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.TenantIsolationException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.TenancyAuthorizationEvaluator;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.governance.repository.CaseAssignmentRepository;
import org.aicte.sage.modules.governance.repository.CommitteeAppointmentRepository;
import org.aicte.sage.modules.governance.repository.JurisdictionAssignmentRepository;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenancyAuthorizationEvaluatorTest {

    private InstitutionRepository institutionRepository;
    private JurisdictionAssignmentRepository jurisdictionRepository;
    private CaseAssignmentRepository caseAssignmentRepository;
    private CommitteeAppointmentRepository committeeAppointmentRepository;
    private AuditService auditService;
    private AuthoritativeAuthorizationService authorizationService;
    private TenancyAuthorizationEvaluator evaluator;

    private final UUID instA = UUID.randomUUID();
    private final UUID instB = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        institutionRepository = Mockito.mock(InstitutionRepository.class);
        jurisdictionRepository = Mockito.mock(JurisdictionAssignmentRepository.class);
        caseAssignmentRepository = Mockito.mock(CaseAssignmentRepository.class);
        committeeAppointmentRepository = Mockito.mock(CommitteeAppointmentRepository.class);
        auditService = Mockito.mock(AuditService.class);

        authorizationService = new AuthoritativeAuthorizationService(
                institutionRepository,
                jurisdictionRepository,
                caseAssignmentRepository,
                committeeAppointmentRepository,
                auditService
        );
        evaluator = new TenancyAuthorizationEvaluator(authorizationService);
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

    @Test
    @DisplayName("Regional Officer without jurisdiction assignment over target state should be denied")
    void testRegionalOfficerDeniedWithoutJurisdiction() {
        UUID roId = UUID.randomUUID();
        UserPrincipal regionalOfficer = new UserPrincipal(
                roId, "ro", "ro@aicte.gov.in", "pass", "Regional Officer", true,
                List.of(new SimpleGrantedAuthority("ROLE_REGIONAL_OFFICER")), Set.of()
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(regionalOfficer, null, regionalOfficer.getAuthorities());

        Institution inst = new Institution("1-100", "College in Maharashtra", "ENGG", "Maharashtra", "Mumbai");
        Mockito.when(institutionRepository.findById(instB)).thenReturn(Optional.of(inst));
        Mockito.when(jurisdictionRepository.hasJurisdictionOverState(roId, "Maharashtra")).thenReturn(false);

        assertThrows(TenantIsolationException.class, () -> evaluator.hasAccessToInstitution(auth, instB));
    }

    @Test
    @DisplayName("Regional Officer with active jurisdiction over target state should be granted access")
    void testRegionalOfficerAllowedWithJurisdiction() {
        UUID roId = UUID.randomUUID();
        UserPrincipal regionalOfficer = new UserPrincipal(
                roId, "ro", "ro@aicte.gov.in", "pass", "Regional Officer", true,
                List.of(new SimpleGrantedAuthority("ROLE_REGIONAL_OFFICER")), Set.of()
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(regionalOfficer, null, regionalOfficer.getAuthorities());

        Institution inst = new Institution("1-100", "College in Delhi", "ENGG", "Delhi", "Delhi");
        Mockito.when(institutionRepository.findById(instA)).thenReturn(Optional.of(inst));
        Mockito.when(jurisdictionRepository.hasJurisdictionOverState(roId, "Delhi")).thenReturn(true);

        assertTrue(evaluator.hasAccessToInstitution(auth, instA));
    }
}
