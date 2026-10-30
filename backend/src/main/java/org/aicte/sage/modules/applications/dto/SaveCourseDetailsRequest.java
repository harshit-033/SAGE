package org.aicte.sage.modules.applications.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveCourseDetailsRequest(
        @NotBlank(message = "Course name is required")
        String courseName,

        @NotBlank(message = "Programme level is required (e.g. UNDERGRADUATE, POSTGRADUATE, DIPLOMA)")
        String programmeLevel,

        @NotBlank(message = "Degree type is required (e.g. B.Tech, M.Tech, MBA, MCA)")
        String degreeType,

        @NotBlank(message = "Course discipline is required")
        String courseDiscipline,

        @NotNull(message = "Proposed intake is required")
        @Min(value = 1, message = "Proposed intake must be at least 1")
        Integer proposedIntake,

        String modeOfDelivery,

        @NotNull(message = "Duration in years is required")
        @Min(value = 1, message = "Duration must be at least 1 year")
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
) {}
