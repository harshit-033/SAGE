package in.gov.aicte.evc;

import in.gov.aicte.evc.entity.EvcInspection;
import in.gov.aicte.evc.entity.Institution;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InspectionValidationTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid inspection passes all bean validations")
    void testValidInspectionPassesValidation() {
        Institution inst = new Institution("1-VAL-01", "Valid Institute", "Pune", "Maharashtra", "Western");
        EvcInspection inspection = new EvcInspection(
                inst,
                3,
                3,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 20)
        );

        Set<ConstraintViolation<EvcInspection>> violations = validator.validate(inspection);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Validation fails when duration is non-positive")
    void testInvalidDurationFailsValidation() {
        Institution inst = new Institution("1-VAL-02", "Valid Institute", "Pune", "Maharashtra", "Western");
        EvcInspection zeroDuration = new EvcInspection(
                inst,
                0,
                3,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 20)
        );

        Set<ConstraintViolation<EvcInspection>> violations = validator.validate(zeroDuration);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("durationInDays"));
    }

    @Test
    @DisplayName("Validation fails when required member count is non-positive")
    void testInvalidMemberCountFailsValidation() {
        Institution inst = new Institution("1-VAL-03", "Valid Institute", "Pune", "Maharashtra", "Western");
        EvcInspection zeroMembers = new EvcInspection(
                inst,
                3,
                0,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 20)
        );

        Set<ConstraintViolation<EvcInspection>> violations = validator.validate(zeroMembers);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("requiredMemberCount"));
    }

    @Test
    @DisplayName("Validation fails when earliest start date is after latest start date")
    void testInvalidDateWindowFailsValidation() {
        Institution inst = new Institution("1-VAL-04", "Valid Institute", "Pune", "Maharashtra", "Western");
        EvcInspection invertedDates = new EvcInspection(
                inst,
                3,
                3,
                LocalDate.of(2026, 11, 25),
                LocalDate.of(2026, 11, 10)
        );

        Set<ConstraintViolation<EvcInspection>> violations = validator.validate(invertedDates);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Earliest start date must not be later than latest start date"));
    }

    @Test
    @DisplayName("End date calculation is inclusive for 3 and 4 days")
    void testInclusiveEndDateCalculation() {
        Institution inst = new Institution("1-VAL-05", "Valid Institute", "Pune", "Maharashtra", "Western");
        
        // 4 days starting 10 Nov => 10, 11, 12, 13 Nov
        EvcInspection fourDays = new EvcInspection(inst, 4, 3, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        fourDays.setScheduledStartDate(LocalDate.of(2026, 11, 10));
        assertThat(fourDays.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 13));

        // 3 days starting 10 Nov => 10, 11, 12 Nov
        EvcInspection threeDays = new EvcInspection(inst, 3, 3, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        threeDays.setScheduledStartDate(LocalDate.of(2026, 11, 10));
        assertThat(threeDays.getScheduledEndDate()).isEqualTo(LocalDate.of(2026, 11, 12));
    }
}

