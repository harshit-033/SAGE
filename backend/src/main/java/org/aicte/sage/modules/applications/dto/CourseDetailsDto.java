package org.aicte.sage.modules.applications.dto;

import org.aicte.sage.modules.applications.model.ApplicationCourseDetails;

import java.util.UUID;

public record CourseDetailsDto(
        UUID id,
        UUID applicationId,
        String courseName,
        String programmeLevel,
        String degreeType,
        String courseDiscipline,
        Integer proposedIntake,
        String modeOfDelivery,
        Integer durationYears,
        String regulatoryClassification,
        String proposedDepartment,
        String existingRelatedCourses,
        Integer availableClassrooms,
        Integer laboratoriesCount,
        String laboratoryDetails,
        String equipmentDetails,
        Integer libraryTitles,
        Integer libraryVolumes,
        Integer libraryJournals,
        Integer professorsCount,
        Integer associateProfessorsCount,
        Integer assistantProfessorsCount,
        String facultyStudentRatio,
        String infrastructureNotes
) {
    public static CourseDetailsDto fromEntity(ApplicationCourseDetails entity) {
        if (entity == null) return null;
        return new CourseDetailsDto(
                entity.getId(),
                entity.getApplication().getId(),
                entity.getCourseName(),
                entity.getProgrammeLevel(),
                entity.getDegreeType(),
                entity.getCourseDiscipline(),
                entity.getProposedIntake(),
                entity.getModeOfDelivery(),
                entity.getDurationYears(),
                entity.getRegulatoryClassification(),
                entity.getProposedDepartment(),
                entity.getExistingRelatedCourses(),
                entity.getAvailableClassrooms(),
                entity.getLaboratoriesCount(),
                entity.getLaboratoryDetails(),
                entity.getEquipmentDetails(),
                entity.getLibraryTitles(),
                entity.getLibraryVolumes(),
                entity.getLibraryJournals(),
                entity.getProfessorsCount(),
                entity.getAssociateProfessorsCount(),
                entity.getAssistantProfessorsCount(),
                entity.getFacultyStudentRatio(),
                entity.getInfrastructureNotes()
        );
    }
}
