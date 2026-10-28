package org.aicte.sage.modules.institutions.service;

import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.exception.TenantIsolationException;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.institutions.dto.CreateInstitutionRequest;
import org.aicte.sage.modules.institutions.dto.InstitutionDto;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final AuditService auditService;

    public InstitutionService(InstitutionRepository institutionRepository, AuditService auditService) {
        this.institutionRepository = institutionRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<InstitutionDto> getInstitutionsForPrincipal(UserPrincipal principal) {
        List<Institution> list;
        if (principal.hasRole("ROLE_AICTE_ADMIN") || principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            list = institutionRepository.findAll();
        } else {
            list = institutionRepository.findAllByAffiliatedUserId(principal.getId());
        }
        return list.stream().map(InstitutionDto::fromEntity).collect(Collectors.toList());
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
}
