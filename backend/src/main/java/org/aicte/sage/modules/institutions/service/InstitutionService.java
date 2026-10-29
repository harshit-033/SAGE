package org.aicte.sage.modules.institutions.service;

import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.exception.TenantIsolationException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.governance.repository.CaseAssignmentRepository;
import org.aicte.sage.modules.governance.repository.JurisdictionAssignmentRepository;
import org.aicte.sage.modules.institutions.dto.*;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.entity.InstitutionUser;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.aicte.sage.modules.institutions.repository.InstitutionUserRepository;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InstitutionService {

    private static final Logger log = LoggerFactory.getLogger(InstitutionService.class);

    private final InstitutionRepository institutionRepository;
    private final InstitutionUserRepository institutionUserRepository;
    private final UserRepository userRepository;
    private final JurisdictionAssignmentRepository jurisdictionRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;
    private final AuthoritativeAuthorizationService authorizationService;
    private final AuditService auditService;

    public InstitutionService(InstitutionRepository institutionRepository,
                              InstitutionUserRepository institutionUserRepository,
                              UserRepository userRepository,
                              JurisdictionAssignmentRepository jurisdictionRepository,
                              CaseAssignmentRepository caseAssignmentRepository,
                              AuthoritativeAuthorizationService authorizationService,
                              AuditService auditService) {
        this.institutionRepository = institutionRepository;
        this.institutionUserRepository = institutionUserRepository;
        this.userRepository = userRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<InstitutionDto> getInstitutionsForPrincipal(UserPrincipal principal) {
        if (principal.hasRole("ROLE_AICTE_ADMIN")) {
            return institutionRepository.findAll().stream().map(InstitutionDto::fromEntity).collect(Collectors.toList());
        }
        if (principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            List<String> states = jurisdictionRepository.findActiveStatesByUserId(principal.getId());
            if (states.contains("*")) {
                return institutionRepository.findAll().stream().map(InstitutionDto::fromEntity).collect(Collectors.toList());
            }
            if (states.isEmpty()) {
                return Collections.emptyList();
            }
            return institutionRepository.findAll().stream()
                    .filter(i -> i.getState() != null && states.stream().anyMatch(s -> s.equalsIgnoreCase(i.getState())))
                    .map(InstitutionDto::fromEntity)
                    .collect(Collectors.toList());
        }
        List<UUID> assignedInstIds = caseAssignmentRepository.findActiveInstitutionIdsByUserId(principal.getId());
        if (!assignedInstIds.isEmpty()) {
            return institutionRepository.findAllById(assignedInstIds).stream().map(InstitutionDto::fromEntity).collect(Collectors.toList());
        }
        return institutionRepository.findAllByAffiliatedUserId(principal.getId()).stream().map(InstitutionDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<InstitutionDto> getInstitutionsPaginated(
            String query, String state, String category, Boolean active,
            Pageable pageable, UserPrincipal principal) {
        
        Page<Institution> page;
        if (principal.hasRole("ROLE_AICTE_ADMIN")) {
            page = institutionRepository.searchInstitutions(query, state, category, active, pageable);
        } else if (principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            List<String> states = jurisdictionRepository.findActiveStatesByUserId(principal.getId());
            if (states.contains("*")) {
                page = institutionRepository.searchInstitutions(query, state, category, active, pageable);
            } else if (states.isEmpty()) {
                page = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else {
                List<String> lowerStates = states.stream().map(String::toLowerCase).collect(Collectors.toList());
                page = institutionRepository.searchByStates(lowerStates, query, state, category, active, pageable);
            }
        } else if (principal.hasRole("ROLE_SCRUTINY_OFFICER") || principal.hasRole("ROLE_EVC_COORDINATOR")
                || principal.hasRole("ROLE_EVC_MEMBER") || principal.hasRole("ROLE_COMPLIANCE_OFFICER")
                || principal.hasRole("ROLE_AUDITOR")) {
            List<UUID> assignedIds = caseAssignmentRepository.findActiveInstitutionIdsByUserId(principal.getId());
            if (assignedIds.isEmpty()) {
                page = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else {
                page = institutionRepository.searchByIds(assignedIds, query, state, category, active, pageable);
            }
        } else {
            page = institutionRepository.searchAffiliatedInstitutions(principal.getId(), query, state, category, active, pageable);
        }

        List<InstitutionDto> dtos = page.getContent().stream()
                .map(InstitutionDto::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.from(page, dtos);
    }

    @Transactional(readOnly = true)
    public InstitutionDto getInstitutionById(UUID id, UserPrincipal principal) {
        authorizationService.checkAccessToInstitution(principal, id, "READ");

        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", id));

        return InstitutionDto.fromEntity(institution);
    }

    public InstitutionDto createInstitution(CreateInstitutionRequest request, UserPrincipal principal, String clientIp) {
        if (institutionRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Institution with AICTE PID code '" + request.getCode() + "' already exists");
        }

        Institution entity = new Institution(
                request.getCode(),
                request.getName(),
                request.getApplicationType(),
                request.getState(),
                request.getDistrict()
        );
        entity.setActive(true);
        Institution saved = institutionRepository.save(entity);

        auditService.recordMandatoryEvent(
                "INSTITUTION_CREATED",
                principal.getUsername(),
                saved.getId(),
                "INSTITUTION",
                saved.getId().toString(),
                "CREATE",
                clientIp,
                "New technical institution registered: " + saved.getName()
        );

        return InstitutionDto.fromEntity(saved);
    }

    public InstitutionDto updateInstitution(UUID id, UpdateInstitutionRequest request, UserPrincipal principal, String clientIp) {
        authorizationService.checkUpdateInstitution(principal, id);

        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", id));

        institution.setName(request.getName());
        if (request.getCategory() != null) {
            institution.setCategory(request.getCategory());
        }
        institution.setAddressLine1(request.getAddressLine1());
        institution.setAddressLine2(request.getAddressLine2());
        institution.setCity(request.getCity());
        institution.setState(request.getState());
        institution.setDistrict(request.getDistrict());
        institution.setPincode(request.getPincode());
        institution.setOfficialEmail(request.getOfficialEmail());
        institution.setOfficialPhone(request.getOfficialPhone());
        institution.setAlternatePhone(request.getAlternatePhone());
        institution.setWebsiteUrl(request.getWebsiteUrl());
        institution.setEstablishmentYear(request.getEstablishmentYear());
        institution.setLatitude(request.getLatitude());
        institution.setLongitude(request.getLongitude());

        Institution updated = institutionRepository.save(institution);

        auditService.recordMandatoryEvent(
                "INSTITUTION_UPDATED",
                principal.getUsername(),
                updated.getId(),
                "INSTITUTION",
                updated.getId().toString(),
                "UPDATE",
                clientIp,
                "Institution profile updated: " + updated.getName()
        );

        return InstitutionDto.fromEntity(updated);
    }

    public InstitutionDto setInstitutionActiveStatus(UUID id, InstitutionStatusRequest request, UserPrincipal principal, String clientIp) {
        authorizationService.checkAccessToInstitution(principal, id, "STATUS_CHANGE");

        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", id));

        institution.setActive(request.getActive());
        Institution updated = institutionRepository.save(institution);

        auditService.recordMandatoryEvent(
                "INSTITUTION_STATUS_CHANGED",
                principal.getUsername(),
                updated.getId(),
                "INSTITUTION",
                updated.getId().toString(),
                request.getActive() ? "ACTIVATE" : "DEACTIVATE",
                clientIp,
                "Institution status set to " + (request.getActive() ? "ACTIVE" : "SUSPENDED") +
                        (request.getReason() != null ? " Reason: " + request.getReason() : "")
        );

        return InstitutionDto.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<InstitutionUserDto> getInstitutionUsers(UUID institutionId, UserPrincipal principal) {
        authorizationService.checkAccessToInstitution(principal, institutionId, "READ_USERS");

        List<InstitutionUser> list = institutionUserRepository.findAllByInstitutionId(institutionId);
        return list.stream().map(iu -> {
            User user = userRepository.findById(iu.getUserId()).orElse(null);
            return new InstitutionUserDto(
                    iu.getId(),
                    iu.getInstitutionId(),
                    iu.getUserId(),
                    user != null ? user.getUsername() : "UNKNOWN",
                    user != null ? user.getEmail() : "UNKNOWN",
                    user != null ? user.getFullName() : "UNKNOWN",
                    iu.getRoleInInstitution(),
                    iu.isPrimaryContact(),
                    iu.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    public InstitutionUserDto addInstitutionUser(UUID institutionId, AddInstitutionUserRequest request, UserPrincipal principal, String clientIp) {
        authorizationService.checkUpdateInstitution(principal, institutionId);

        if (!institutionRepository.existsById(institutionId)) {
            throw new ResourceNotFoundException("Institution", institutionId);
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        if (institutionUserRepository.existsByInstitutionIdAndUserId(institutionId, request.getUserId())) {
            throw new IllegalArgumentException("User " + user.getUsername() + " is already associated with this institution");
        }

        InstitutionUser iu = new InstitutionUser(
                institutionId,
                request.getUserId(),
                request.getRoleInInstitution(),
                request.isPrimaryContact()
        );
        InstitutionUser saved = institutionUserRepository.save(iu);

        auditService.recordMandatoryEvent(
                "INSTITUTION_USER_AFFILIATED",
                principal.getUsername(),
                institutionId,
                "INSTITUTION_USER",
                saved.getId().toString(),
                "AFFILIATE",
                clientIp,
                "Affiliated user " + user.getUsername() + " as " + request.getRoleInInstitution()
        );

        return new InstitutionUserDto(
                saved.getId(),
                saved.getInstitutionId(),
                saved.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                saved.getRoleInInstitution(),
                saved.isPrimaryContact(),
                saved.getCreatedAt()
        );
    }

    public void removeInstitutionUser(UUID institutionId, UUID userId, UserPrincipal principal, String clientIp) {
        authorizationService.checkUpdateInstitution(principal, institutionId);

        InstitutionUser iu = institutionUserRepository.findByInstitutionIdAndUserId(institutionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("InstitutionUser mapping not found for user: " + userId));

        institutionUserRepository.delete(iu);

        auditService.recordMandatoryEvent(
                "INSTITUTION_USER_REMOVED",
                principal.getUsername(),
                institutionId,
                "INSTITUTION_USER",
                iu.getId().toString(),
                "DISAFFILIATE",
                clientIp,
                "Disaffiliated user " + userId + " from institution " + institutionId
        );
    }
}
