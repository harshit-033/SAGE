package org.aicte.sage.modules.applications.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_course_details")
public class ApplicationCourseDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "programme_level", nullable = false, length = 64)
    private String programmeLevel;

    @Column(name = "degree_type", nullable = false, length = 64)
    private String degreeType;

    @Column(name = "course_discipline", nullable = false, length = 128)
    private String courseDiscipline;

    @Column(name = "proposed_intake", nullable = false)
    private Integer proposedIntake;

    @Column(name = "mode_of_delivery", nullable = false, length = 64)
    private String modeOfDelivery = "REGULAR";

    @Column(name = "duration_years", nullable = false)
    private Integer durationYears = 4;

    @Column(name = "regulatory_classification", length = 64)
    private String regulatoryClassification;

    @Column(name = "proposed_department", length = 128)
    private String proposedDepartment;

    @Column(name = "existing_related_courses", columnDefinition = "TEXT")
    private String existingRelatedCourses;

    @Column(name = "available_classrooms", nullable = false)
    private Integer availableClassrooms = 0;

    @Column(name = "laboratories_count", nullable = false)
    private Integer laboratoriesCount = 0;

    @Column(name = "laboratory_details", columnDefinition = "TEXT")
    private String laboratoryDetails;

    @Column(name = "equipment_details", columnDefinition = "TEXT")
    private String equipmentDetails;

    @Column(name = "library_titles", nullable = false)
    private Integer libraryTitles = 0;

    @Column(name = "library_volumes", nullable = false)
    private Integer libraryVolumes = 0;

    @Column(name = "library_journals", nullable = false)
    private Integer libraryJournals = 0;

    @Column(name = "professors_count", nullable = false)
    private Integer professorsCount = 0;

    @Column(name = "associate_professors_count", nullable = false)
    private Integer associateProfessorsCount = 0;

    @Column(name = "assistant_professors_count", nullable = false)
    private Integer assistantProfessorsCount = 0;

    @Column(name = "faculty_student_ratio", length = 32)
    private String facultyStudentRatio;

    @Column(name = "infrastructure_notes", columnDefinition = "TEXT")
    private String infrastructureNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ApplicationCourseDetails() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getProgrammeLevel() { return programmeLevel; }
    public void setProgrammeLevel(String programmeLevel) { this.programmeLevel = programmeLevel; }

    public String getDegreeType() { return degreeType; }
    public void setDegreeType(String degreeType) { this.degreeType = degreeType; }

    public String getCourseDiscipline() { return courseDiscipline; }
    public void setCourseDiscipline(String courseDiscipline) { this.courseDiscipline = courseDiscipline; }

    public Integer getProposedIntake() { return proposedIntake; }
    public void setProposedIntake(Integer proposedIntake) { this.proposedIntake = proposedIntake; }

    public String getModeOfDelivery() { return modeOfDelivery; }
    public void setModeOfDelivery(String modeOfDelivery) { this.modeOfDelivery = modeOfDelivery; }

    public Integer getDurationYears() { return durationYears; }
    public void setDurationYears(Integer durationYears) { this.durationYears = durationYears; }

    public String getRegulatoryClassification() { return regulatoryClassification; }
    public void setRegulatoryClassification(String regulatoryClassification) { this.regulatoryClassification = regulatoryClassification; }

    public String getProposedDepartment() { return proposedDepartment; }
    public void setProposedDepartment(String proposedDepartment) { this.proposedDepartment = proposedDepartment; }

    public String getExistingRelatedCourses() { return existingRelatedCourses; }
    public void setExistingRelatedCourses(String existingRelatedCourses) { this.existingRelatedCourses = existingRelatedCourses; }

    public Integer getAvailableClassrooms() { return availableClassrooms; }
    public void setAvailableClassrooms(Integer availableClassrooms) { this.availableClassrooms = availableClassrooms; }

    public Integer getLaboratoriesCount() { return laboratoriesCount; }
    public void setLaboratoriesCount(Integer laboratoriesCount) { this.laboratoriesCount = laboratoriesCount; }

    public String getLaboratoryDetails() { return laboratoryDetails; }
    public void setLaboratoryDetails(String laboratoryDetails) { this.laboratoryDetails = laboratoryDetails; }

    public String getEquipmentDetails() { return equipmentDetails; }
    public void setEquipmentDetails(String equipmentDetails) { this.equipmentDetails = equipmentDetails; }

    public Integer getLibraryTitles() { return libraryTitles; }
    public void setLibraryTitles(Integer libraryTitles) { this.libraryTitles = libraryTitles; }

    public Integer getLibraryVolumes() { return libraryVolumes; }
    public void setLibraryVolumes(Integer libraryVolumes) { this.libraryVolumes = libraryVolumes; }

    public Integer getLibraryJournals() { return libraryJournals; }
    public void setLibraryJournals(Integer libraryJournals) { this.libraryJournals = libraryJournals; }

    public Integer getProfessorsCount() { return professorsCount; }
    public void setProfessorsCount(Integer professorsCount) { this.professorsCount = professorsCount; }

    public Integer getAssociateProfessorsCount() { return associateProfessorsCount; }
    public void setAssociateProfessorsCount(Integer associateProfessorsCount) { this.associateProfessorsCount = associateProfessorsCount; }

    public Integer getAssistantProfessorsCount() { return assistantProfessorsCount; }
    public void setAssistantProfessorsCount(Integer assistantProfessorsCount) { this.assistantProfessorsCount = assistantProfessorsCount; }

    public String getFacultyStudentRatio() { return facultyStudentRatio; }
    public void setFacultyStudentRatio(String facultyStudentRatio) { this.facultyStudentRatio = facultyStudentRatio; }

    public String getInfrastructureNotes() { return infrastructureNotes; }
    public void setInfrastructureNotes(String infrastructureNotes) { this.infrastructureNotes = infrastructureNotes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
