package org.aicte.sage.modules.applications.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_branch_details")
public class ApplicationBranchDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Column(name = "existing_programme", nullable = false, length = 128)
    private String existingProgramme;

    @Column(name = "existing_department", nullable = false, length = 128)
    private String existingDepartment;

    @Column(name = "proposed_branch_name", nullable = false)
    private String proposedBranchName;

    @Column(name = "proposed_intake", nullable = false)
    private Integer proposedIntake;

    @Column(name = "existing_approved_intake", nullable = false)
    private Integer existingApprovedIntake = 0;

    @Column(name = "faculty_count", nullable = false)
    private Integer facultyCount = 0;

    @Column(name = "staff_count", nullable = false)
    private Integer staffCount = 0;

    @Column(name = "available_classrooms", nullable = false)
    private Integer availableClassrooms = 0;

    @Column(name = "laboratory_count", nullable = false)
    private Integer laboratoryCount = 0;

    @Column(name = "laboratory_requirements", columnDefinition = "TEXT")
    private String laboratoryRequirements;

    @Column(name = "equipment_availability", columnDefinition = "TEXT")
    private String equipmentAvailability;

    @Column(name = "institution_declarations", columnDefinition = "TEXT")
    private String institutionDeclarations;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ApplicationBranchDetails() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public String getExistingProgramme() { return existingProgramme; }
    public void setExistingProgramme(String existingProgramme) { this.existingProgramme = existingProgramme; }

    public String getExistingDepartment() { return existingDepartment; }
    public void setExistingDepartment(String existingDepartment) { this.existingDepartment = existingDepartment; }

    public String getProposedBranchName() { return proposedBranchName; }
    public void setProposedBranchName(String proposedBranchName) { this.proposedBranchName = proposedBranchName; }

    public Integer getProposedIntake() { return proposedIntake; }
    public void setProposedIntake(Integer proposedIntake) { this.proposedIntake = proposedIntake; }

    public Integer getExistingApprovedIntake() { return existingApprovedIntake; }
    public void setExistingApprovedIntake(Integer existingApprovedIntake) { this.existingApprovedIntake = existingApprovedIntake; }

    public Integer getFacultyCount() { return facultyCount; }
    public void setFacultyCount(Integer facultyCount) { this.facultyCount = facultyCount; }

    public Integer getStaffCount() { return staffCount; }
    public void setStaffCount(Integer staffCount) { this.staffCount = staffCount; }

    public Integer getAvailableClassrooms() { return availableClassrooms; }
    public void setAvailableClassrooms(Integer availableClassrooms) { this.availableClassrooms = availableClassrooms; }

    public Integer getLaboratoryCount() { return laboratoryCount; }
    public void setLaboratoryCount(Integer laboratoryCount) { this.laboratoryCount = laboratoryCount; }

    public String getLaboratoryRequirements() { return laboratoryRequirements; }
    public void setLaboratoryRequirements(String laboratoryRequirements) { this.laboratoryRequirements = laboratoryRequirements; }

    public String getEquipmentAvailability() { return equipmentAvailability; }
    public void setEquipmentAvailability(String equipmentAvailability) { this.equipmentAvailability = equipmentAvailability; }

    public String getInstitutionDeclarations() { return institutionDeclarations; }
    public void setInstitutionDeclarations(String institutionDeclarations) { this.institutionDeclarations = institutionDeclarations; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
