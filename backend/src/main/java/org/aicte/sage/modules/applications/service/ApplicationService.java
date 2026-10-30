package org.aicte.sage.modules.applications.service;

import org.aicte.sage.common.api.PageResponse;
import org.aicte.sage.common.api.PaginationUtils;
import org.aicte.sage.common.audit.AuditService;
import org.aicte.sage.common.exception.ResourceNotFoundException;
import org.aicte.sage.common.security.AuthoritativeAuthorizationService;
import org.aicte.sage.common.security.UserPrincipal;
import org.aicte.sage.modules.applications.dto.ApplicationDto;
import org.aicte.sage.modules.applications.dto.BranchDetailsDto;
import org.aicte.sage.modules.applications.dto.CourseDetailsDto;
import org.aicte.sage.modules.applications.dto.CreateApplicationRequest;
import org.aicte.sage.modules.applications.dto.SaveBranchDetailsRequest;
import org.aicte.sage.modules.applications.dto.SaveCourseDetailsRequest;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.applications.model.ApplicationBranchDetails;
import org.aicte.sage.modules.applications.model.ApplicationCategory;
import org.aicte.sage.modules.applications.model.ApplicationCourseDetails;
import org.aicte.sage.modules.applications.model.ApplicationState;
import org.aicte.sage.modules.applications.repository.ApplicationBranchDetailsRepository;
import org.aicte.sage.modules.applications.repository.ApplicationCourseDetailsRepository;
import org.aicte.sage.modules.applications.repository.ApplicationRepository;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.institutions.repository.InstitutionRepository;
import org.aicte.sage.modules.users.entity.User;
import org.aicte.sage.modules.users.repository.UserRepository;
import org.aicte.sage.modules.workflow.dto.TransitionRequest;
import org.aicte.sage.modules.workflow.dto.WorkflowTransitionDto;
import org.aicte.sage.modules.workflow.model.WorkflowDefinition;
import org.aicte.sage.modules.workflow.repository.WorkflowDefinitionRepository;
import org.aicte.sage.modules.workflow.service.WorkflowEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final InstitutionRepository institutionRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final UserRepository userRepository;
    private final WorkflowEngineService workflowEngineService;
    private final ApplicationCourseDetailsRepository courseDetailsRepository;
    private final ApplicationBranchDetailsRepository branchDetailsRepository;
    private final AuthoritativeAuthorizationService authService;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    public ApplicationService(
            ApplicationRepository applicationRepository,
            InstitutionRepository institutionRepository,
            WorkflowDefinitionRepository workflowDefinitionRepository,
            UserRepository userRepository,
            WorkflowEngineService workflowEngineService,
            ApplicationCourseDetailsRepository courseDetailsRepository,
            ApplicationBranchDetailsRepository branchDetailsRepository,
            AuthoritativeAuthorizationService authService,
            AuditService auditService
    ) {
        this.applicationRepository = applicationRepository;
        this.institutionRepository = institutionRepository;
        this.workflowDefinitionRepository = workflowDefinitionRepository;
        this.userRepository = userRepository;
        this.workflowEngineService = workflowEngineService;
        this.courseDetailsRepository = courseDetailsRepository;
        this.branchDetailsRepository = branchDetailsRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    public ApplicationDto createApplication(CreateApplicationRequest req, UserPrincipal principal) {
        authService.checkAccessToInstitution(principal, req.getInstitutionId(), "CREATE_APPLICATION");

        Institution institution = institutionRepository.findById(req.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Institution '" + req.getInstitutionId() + "' not found"));

        WorkflowDefinition workflowDef = workflowDefinitionRepository
                .findFirstByCategoryAndActiveTrue(req.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Active workflow definition for category '" + req.getCategory() + "' not found"));

        User creator = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User '" + principal.getId() + "' not found"));

        String appNumber = generateApplicationNumber(institution, req.getAcademicYear());

        Application application = new Application();
        application.setApplicationNumber(appNumber);
        application.setInstitution(institution);
        application.setAcademicYear(req.getAcademicYear());
        application.setCategory(req.getCategory());
        application.setWorkflowDefinition(workflowDef);
        application.setCurrentState(ApplicationState.DRAFT);
        application.setCreatedBy(creator);

        Application saved = applicationRepository.save(application);

        auditService.recordEvent(
                "APPLICATION_CREATED",
                principal.getUsername(),
                institution.getId(),
                "APPLICATION",
                saved.getId().toString(),
                "CREATE",
                null,
                "Application " + saved.getApplicationNumber() + " created for " + institution.getName()
        );

        log.info("Application {} created for institution {}", saved.getApplicationNumber(), institution.getName());
        return ApplicationDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ApplicationDto getApplication(UUID id, UserPrincipal principal) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + id + "' not found"));

        authService.checkAccessToInstitution(principal, app.getInstitution().getId(), "GET_APPLICATION");
        ApplicationDto dto = ApplicationDto.fromEntity(app);

        if (app.getCategory() == ApplicationCategory.NEW_COURSE_INTRODUCTION) {
            courseDetailsRepository.findByApplicationId(id).ifPresent(c -> dto.setCourseDetails(CourseDetailsDto.fromEntity(c)));
        } else if (app.getCategory() == ApplicationCategory.NEW_BRANCH_INTRODUCTION) {
            branchDetailsRepository.findByApplicationId(id).ifPresent(b -> dto.setBranchDetails(BranchDetailsDto.fromEntity(b)));
        }

        return dto;
    }

    public CourseDetailsDto saveCourseDetails(UUID applicationId, SaveCourseDetailsRequest req, UserPrincipal principal) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));
        authService.checkAccessToInstitution(principal, app.getInstitution().getId(), "SAVE_COURSE_DETAILS");

        if (app.getCurrentState() != ApplicationState.DRAFT) {
            throw new IllegalStateException("Course details can only be edited when application is in DRAFT state");
        }

        ApplicationCourseDetails details = courseDetailsRepository.findByApplicationId(applicationId)
                .orElseGet(() -> {
                    ApplicationCourseDetails d = new ApplicationCourseDetails();
                    d.setApplication(app);
                    return d;
                });

        details.setCourseName(req.courseName());
        details.setProgrammeLevel(req.programmeLevel());
        details.setDegreeType(req.degreeType());
        details.setCourseDiscipline(req.courseDiscipline());
        details.setProposedIntake(req.proposedIntake());
        if (req.modeOfDelivery() != null) details.setModeOfDelivery(req.modeOfDelivery());
        if (req.durationYears() != null) details.setDurationYears(req.durationYears());
        details.setRegulatoryClassification(req.regulatoryClassification());
        details.setProposedDepartment(req.proposedDepartment());
        details.setExistingRelatedCourses(req.existingRelatedCourses());
        if (req.availableClassrooms() != null) details.setAvailableClassrooms(req.availableClassrooms());
        if (req.laboratoriesCount() != null) details.setLaboratoriesCount(req.laboratoriesCount());
        details.setLaboratoryDetails(req.laboratoryDetails());
        details.setEquipmentDetails(req.equipmentDetails());
        if (req.libraryTitles() != null) details.setLibraryTitles(req.libraryTitles());
        if (req.libraryVolumes() != null) details.setLibraryVolumes(req.libraryVolumes());
        if (req.libraryJournals() != null) details.setLibraryJournals(req.libraryJournals());
        if (req.professorsCount() != null) details.setProfessorsCount(req.professorsCount());
        if (req.associateProfessorsCount() != null) details.setAssociateProfessorsCount(req.associateProfessorsCount());
        if (req.assistantProfessorsCount() != null) details.setAssistantProfessorsCount(req.assistantProfessorsCount());
        details.setFacultyStudentRatio(req.facultyStudentRatio());
        details.setInfrastructureNotes(req.infrastructureNotes());

        ApplicationCourseDetails saved = courseDetailsRepository.save(details);

        auditService.recordEvent(
                "COURSE_DETAILS_SAVED",
                principal.getUsername(),
                app.getInstitution().getId(),
                "APPLICATION_COURSE_DETAILS",
                saved.getId().toString(),
                "UPDATE",
                null,
                "Saved course details for " + saved.getCourseName() + " on application " + app.getApplicationNumber()
        );

        return CourseDetailsDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public CourseDetailsDto getCourseDetails(UUID applicationId, UserPrincipal principal) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));
        authService.checkAccessToInstitution(principal, app.getInstitution().getId(), "GET_COURSE_DETAILS");
        return courseDetailsRepository.findByApplicationId(applicationId)
                .map(CourseDetailsDto::fromEntity)
                .orElse(null);
    }

    public BranchDetailsDto saveBranchDetails(UUID applicationId, SaveBranchDetailsRequest req, UserPrincipal principal) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));
        authService.checkAccessToInstitution(principal, app.getInstitution().getId(), "SAVE_BRANCH_DETAILS");

        if (app.getCurrentState() != ApplicationState.DRAFT) {
            throw new IllegalStateException("Branch details can only be edited when application is in DRAFT state");
        }

        ApplicationBranchDetails details = branchDetailsRepository.findByApplicationId(applicationId)
                .orElseGet(() -> {
                    ApplicationBranchDetails d = new ApplicationBranchDetails();
                    d.setApplication(app);
                    return d;
                });

        details.setExistingProgramme(req.existingProgramme());
        details.setExistingDepartment(req.existingDepartment());
        details.setProposedBranchName(req.proposedBranchName());
        details.setProposedIntake(req.proposedIntake());
        if (req.existingApprovedIntake() != null) details.setExistingApprovedIntake(req.existingApprovedIntake());
        if (req.facultyCount() != null) details.setFacultyCount(req.facultyCount());
        if (req.staffCount() != null) details.setStaffCount(req.staffCount());
        if (req.availableClassrooms() != null) details.setAvailableClassrooms(req.availableClassrooms());
        if (req.laboratoryCount() != null) details.setLaboratoryCount(req.laboratoryCount());
        details.setLaboratoryRequirements(req.laboratoryRequirements());
        details.setEquipmentAvailability(req.equipmentAvailability());
        details.setInstitutionDeclarations(req.institutionDeclarations());

        ApplicationBranchDetails saved = branchDetailsRepository.save(details);

        auditService.recordEvent(
                "BRANCH_DETAILS_SAVED",
                principal.getUsername(),
                app.getInstitution().getId(),
                "APPLICATION_BRANCH_DETAILS",
                saved.getId().toString(),
                "UPDATE",
                null,
                "Saved branch details for " + saved.getProposedBranchName() + " on application " + app.getApplicationNumber()
        );

        return BranchDetailsDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BranchDetailsDto getBranchDetails(UUID applicationId, UserPrincipal principal) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application with ID '" + applicationId + "' not found"));
        authService.checkAccessToInstitution(principal, app.getInstitution().getId(), "GET_BRANCH_DETAILS");
        return branchDetailsRepository.findByApplicationId(applicationId)
                .map(BranchDetailsDto::fromEntity)
                .orElse(null);
    }

    private static final java.util.Set<String> ALLOWED_SORT_FIELDS = java.util.Set.of("createdAt", "updatedAt", "applicationNumber", "academicYear");

    @Transactional(readOnly = true)
    public PageResponse<ApplicationDto> listApplications(
            UUID requestedInstitutionId,
            ApplicationState state,
            ApplicationCategory category,
            int page,
            int size,
            UserPrincipal principal
    ) {
        Pageable pageable = PaginationUtils.validateAndBuildPageable(page, size, "createdAt", "desc", ALLOWED_SORT_FIELDS, "createdAt");

        UUID effectiveInstId = requestedInstitutionId;
        if (principal.hasRole("ROLE_INSTITUTION_OFFICER") && !principal.hasRole("ROLE_AICTE_ADMIN") && !principal.hasRole("ROLE_REGIONAL_OFFICER")) {
            // Force own institution
            effectiveInstId = principal.getAuthorizedInstitutionIds().stream().findFirst().orElse(null);
        }

        Page<Application> appPage = applicationRepository.searchApplications(effectiveInstId, state, category, pageable);
        return PageResponse.from(appPage, appPage.getContent().stream().map(ApplicationDto::fromEntity).toList());
    }

    public WorkflowTransitionDto submitApplication(UUID id, UserPrincipal principal) {
        return workflowEngineService.transitionState(
                id,
                new TransitionRequest(ApplicationState.SUBMITTED, "Initial Application Submission", "Completed document declarations"),
                principal
        );
    }

    public WorkflowTransitionDto withdrawApplication(UUID id, String reason, UserPrincipal principal) {
        return workflowEngineService.transitionState(
                id,
                new TransitionRequest(ApplicationState.WITHDRAWN, reason != null ? reason : "Application withdrawn by applicant", null),
                principal
        );
    }

    private String generateApplicationNumber(Institution inst, String academicYear) {
        String cleanYear = academicYear.replaceAll("[^0-9]", "");
        int randNum = 1000 + random.nextInt(9000);
        String prefix = inst.getCode() != null ? inst.getCode() : "INST";
        return "APP-" + cleanYear + "-" + prefix + "-" + randNum;
    }
}
