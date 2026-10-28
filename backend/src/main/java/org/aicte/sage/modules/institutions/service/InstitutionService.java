package org.aicte.sage.modules.institutions.service;

import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.exception.TenantIsolationException;
import org.aicte.sage.common.security.UserPrincipal;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AuditService auditService;

    public InstitutionService(InstitutionRepository institutionRepository,
                              InstitutionUserRepository institutionUserRepository,
                              UserRepository userRepository,
                              AuditService auditService) {
        this.institutionRepository = institutionRepository;
        this.institutionUserRepository = institutionUserRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<InstitutionDto> getInstitutionsForPrincipal(UserPrincipal principal) {
        List<Institution> list;
        if (principal.hasRole("ROLE_AICTE_ADMIN") || principal.hasRole("ROLE_REGIONAL_OFFICER")
                || principal.hasRole("ROLE_AUDITOR") || principal.hasRole("ROLE_SCRUTINY_OFFICER")
                || principal.hasRole("ROLE_COMPLIANCE_OFFICER")) {
            list = institutionRepository.findAll();
        } else {
            list = institutionRepository.findAllByAffiliatedUserId(principal.getId());
        }
        return list.stream().map(InstitutionDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<InstitutionDto> getInstitutionsPaginated(
            String query, String state, String category, Boolean active,
            Pageable pageable, UserPrincipal principal) {
        
        Page<Institution> page;
        if (principal.hasRole("ROLE_AICTE_ADMIN") || principal.hasRole("ROLE_REGIONAL_OFFICER")
                || principal.hasRole("ROLE_AUDITOR") || principal.hasRole("ROLE_SCRUTINY_OFFICER")
                || principal.hasRole("ROLE_COMPLIANCE_OFFICER")) {
            page = institutionRepository.searchInstitutions(query, state, category, active, pageable);
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
        if (!principal.isAuthorizedForInstitution(id)) {
            auditService.recordEvent(
                    "TENANCY_VIOLATION_ATTEMPT",
                    principal.getUsername(),
                    id,
                    "INSTITUTION",
                    id.toString(),
                    "READ",
                    null,
                    "Unauthorized cross-tenant attempt to view institution details"
            );
            throw new TenantIsolationException(id, principal.getUsername());
        }

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

        auditService.recordEvent(
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

        auditService.recordEvent(
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
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", id));

        institution.setActive(request.getActive());
        Institution updated = institutionRepository.save(institution);

        auditService.recordEvent(
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
        if (!principal.isAuthorizedForInstitution(institutionId)) {
            throw new TenantIsolationException(institutionId, principal.getUsername());
        }

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

        auditService.recordEvent(
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
        InstitutionUser iu = institutionUserRepository.findByInstitutionIdAndUserId(institutionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("InstitutionUser mapping not found for user: " + userId));

        institutionUserRepository.delete(iu);

        auditService.recordEvent(
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
