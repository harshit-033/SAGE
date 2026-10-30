# SAGE — New Application Workflows Implementation Report
## Feature A: New Course & New Branch / Specialization Introduction Workflows

> **Document ID**: `SAGE-DOC-WF-2026-001`  
> **Regulatory Source**: AICTE Approval Process Handbook (APH) 2024–25 to 2026–27 (Chapters II, IV, and VI)  
> **Repository Branch**: `main`  
> **Status**: **Fully Implemented, Verified, and Production-Ready**

---

### 1. Executive Summary

As mandated under AICTE Approval Process Handbook (APH) 2024–27, technical institutions seeking expansion must undergo rigorous statutory scrutiny before introducing **New Courses** or **New Branches / Emerging Specializations**. Prior to this release, SAGE supported core workflows (`NEW_INSTITUTION`, `EXTENSION_OF_APPROVAL`, `INCREASE_INTAKE`). 

This implementation delivers end-to-end statutory support for:
1. **New Course Introduction (`NEW_COURSE_INTRODUCTION`)**: Full programme specification, faculty cadre ratios (Professor, Associate Professor, Assistant Professor), laboratory requirements, library volumes, and university curriculum endorsement.
2. **New Branch / Specialization Introduction (`NEW_BRANCH_INTRODUCTION`)**: Introduction of cutting-edge disciplines (e.g., Artificial Intelligence, Machine Learning, Cyber Security, Data Science, Robotics), intake cap compliance, parent programme relationship, and dedicated laboratory setup.

---

### 2. Statutory Regulatory Rules & APH Mapping

| Regulatory Dimension | APH 2024–27 Citation | Rule ID | Deterministic Logic & Criteria |
| :--- | :--- | :--- | :--- |
| **Course Faculty Cadre Ratio** | Clause 4.2, Table 4.1 | `RULE_APH_COURSE_FACULTY_RATIO` | Verifies Faculty:Student ratio (1:20 for B.Tech, 1:15 for PG) and required cadre distribution (1 Prof : 2 Assoc Prof : 6 Asst Prof per 60 intake). |
| **Course Laboratory Infrastructure** | Clause 6.3, Appendix 5 | `RULE_APH_COURSE_LABORATORY` | Validates minimum dedicated laboratories (>= 4 specialized labs for UG Engineering) with adequate carpet area and equipment stock. |
| **Curriculum & Affiliation Endorsement** | Clause 2.8, Appendix 2 | `RULE_APH_COURSE_CURRICULUM_AFFILIATION` | Checks affiliating university curriculum endorsement and statutory NOC validity. |
| **Branch Intake Cap Norms** | Clause 4.8, Table 4.3 | `RULE_APH_BRANCH_INTAKE_CAP` | Restricts new branch intake (maximum 60 or 120 seats per division) relative to parent programme capacity. |
| **Branch Dedicated Core Faculty** | Clause 4.9, Appendix 4 | `RULE_APH_BRANCH_CORE_FACULTY` | Requires minimum dedicated core faculty (>= 3 for specialized branches) in emerging technology domains. |

---

### 3. Database Architecture & Schema Enhancements

The implementation leverages two Flyway migrations committed under `backend/src/main/resources/db/`:

#### A. Schema Migration: `V14__new_course_branch_and_document_compliance.sql`
- **Table `application_course_details`**:
  ```sql
  CREATE TABLE application_course_details (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
      course_name VARCHAR(255) NOT NULL,
      programme_level VARCHAR(64) NOT NULL,
      degree_type VARCHAR(64) NOT NULL,
      course_discipline VARCHAR(128) NOT NULL,
      proposed_intake INTEGER NOT NULL,
      mode_of_delivery VARCHAR(32) DEFAULT 'FULL_TIME',
      duration_years INTEGER DEFAULT 4,
      regulatory_classification VARCHAR(64),
      proposed_department VARCHAR(255),
      existing_related_courses TEXT,
      available_classrooms INTEGER DEFAULT 0,
      laboratories_count INTEGER DEFAULT 0,
      laboratory_details TEXT,
      equipment_details TEXT,
      library_titles INTEGER DEFAULT 0,
      library_volumes INTEGER DEFAULT 0,
      library_journals INTEGER DEFAULT 0,
      professors_count INTEGER DEFAULT 0,
      associate_professors_count INTEGER DEFAULT 0,
      assistant_professors_count INTEGER DEFAULT 0,
      faculty_student_ratio VARCHAR(32),
      infrastructure_notes TEXT,
      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      CONSTRAINT uq_course_details_app UNIQUE (application_id)
  );
  CREATE INDEX idx_course_details_app_id ON application_course_details(application_id);
  ```
- **Table `application_branch_details`**:
  ```sql
  CREATE TABLE application_branch_details (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
      existing_programme VARCHAR(128) NOT NULL,
      existing_department VARCHAR(255) NOT NULL,
      proposed_branch_name VARCHAR(255) NOT NULL,
      proposed_intake INTEGER NOT NULL,
      existing_approved_intake INTEGER,
      faculty_count INTEGER DEFAULT 0,
      staff_count INTEGER DEFAULT 0,
      available_classrooms INTEGER DEFAULT 0,
      laboratory_count INTEGER DEFAULT 0,
      laboratory_requirements TEXT,
      equipment_availability TEXT,
      institution_declarations TEXT,
      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      CONSTRAINT uq_branch_details_app UNIQUE (application_id)
  );
  CREATE INDEX idx_branch_details_app_id ON application_branch_details(application_id);
  ```

#### B. Seed Data: `V15__new_course_branch_seed_data.sql`
- Registered workflow definitions: `WF_NEW_COURSE_INTRODUCTION` and `WF_NEW_BRANCH_INTRODUCTION`.
- Seeded APH scrutiny rules: `RULE_APH_COURSE_FACULTY_RATIO`, `RULE_APH_COURSE_LABORATORY`, `RULE_APH_COURSE_CURRICULUM_AFFILIATION`, `RULE_APH_BRANCH_INTAKE_CAP`, `RULE_APH_BRANCH_CORE_FACULTY`.

---

### 4. Backend Implementation Architecture

#### A. Domain Entities & Enums
- [`ApplicationCategory`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/backend/src/main/java/org/aicte/sage/modules/applications/entity/ApplicationCategory.java): Added `NEW_COURSE_INTRODUCTION` and `NEW_BRANCH_INTRODUCTION`.
- [`ApplicationCourseDetails`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/backend/src/main/java/org/aicte/sage/modules/applications/entity/ApplicationCourseDetails.java): Hibernate entity with full auditing and JSON serialization mapping.
- [`ApplicationBranchDetails`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/backend/src/main/java/org/aicte/sage/modules/applications/entity/ApplicationBranchDetails.java): Entity capturing branch specifications and parent programme correlation.

#### B. Repositories
- [`ApplicationCourseDetailsRepository`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/backend/src/main/java/org/aicte/sage/modules/applications/repository/ApplicationCourseDetailsRepository.java): Lookup by `applicationId`.
- [`ApplicationBranchDetailsRepository`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/backend/src/main/java/org/aicte/sage/modules/applications/repository/ApplicationBranchDetailsRepository.java): Lookup by `applicationId`.

#### C. Service Layer
- [`ApplicationService.java`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/backend/src/main/java/org/aicte/sage/modules/applications/service/ApplicationService.java):
  - Injected `ApplicationCourseDetailsRepository` and `ApplicationBranchDetailsRepository`.
  - Implemented `saveCourseDetails(UUID appId, CourseDetailsDto dto)` and `getCourseDetails(UUID appId)`.
  - Implemented `saveBranchDetails(UUID appId, BranchDetailsDto dto)` and `getBranchDetails(UUID appId)`.
  - Enriched `getApplication(UUID appId)` and `mapToDto(Application app)` to eagerly embed course and branch details.
- [`ScrutinyEngineService.java`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/backend/src/main/java/org/aicte/sage/modules/scrutiny/service/ScrutinyEngineService.java):
  - Added category-specific rule evaluation hooks in `evaluateApplication(UUID applicationId, UserPrincipal evaluator)`.
  - Added `evaluateNewCourseRules(Application, ScrutinyEvaluation, List<ScrutinyFinding>)`.
  - Added `evaluateNewBranchRules(Application, ScrutinyEvaluation, List<ScrutinyFinding>)`.
  - Enriched mandatory checklist verification to demand `FACULTY_LIST_QUALIFICATION` and `LABORATORY_INFRASTRUCTURE_REPORT` for course/branch applications.

#### D. REST Controllers
- [`ApplicationController.java`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/backend/src/main/java/org/aicte/sage/modules/applications/controller/ApplicationController.java):
  - `POST /api/v1/applications/{applicationId}/course-details`: Persists/updates course specification.
  - `GET /api/v1/applications/{applicationId}/course-details`: Retrieves course specification.
  - `POST /api/v1/applications/{applicationId}/branch-details`: Persists/updates branch specification.
  - `GET /api/v1/applications/{applicationId}/branch-details`: Retrieves branch specification.

---

### 5. Frontend User Experience & Components

1. **Category Filter & Selection**:
   - [`frontend/src/pages/Applications.tsx`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/frontend/src/pages/Applications.tsx): Added `NEW_COURSE_INTRODUCTION` and `NEW_BRANCH_INTRODUCTION` to category filters and the "Initiate Approval Application" modal.
   - Dynamic conditional form sections render course-specific fields (Course Name, Discipline, Degree Type, Proposed Intake, Labs, Faculty Cadre) or branch-specific fields (Parent Programme, Branch Name, Intake Cap, Core Faculty) directly during application creation.
2. **Deep Inspection & Specification Management**:
   - [`frontend/src/pages/ApplicationDetails.tsx`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/frontend/src/pages/ApplicationDetails.tsx): Added dedicated **Course Specification & Labs** or **Branch Specification & Intake** navigation tabs.
   - Live view renders intake metrics, faculty cadre ratio breakdown, laboratory setups, and infrastructure notes.
   - Institutional officers can edit specifications while application is in `DRAFT` status and immediately persist changes back to the authority database.
3. **Regulatory Document Upload Enhancements**:
   - Upload modal supports newly mandated APH document categories: `FACULTY_LIST_QUALIFICATION`, `LABORATORY_INFRASTRUCTURE_REPORT`, `EQUIPMENT_UTILIZATION_CERTIFICATE`, `BUILDING_PLAN_APPROVAL`, `FINANCIAL_AUDIT_REPORT`, `SYLLABUS_CURRICULUM_APPROVAL`, and `OTHER_SUPPORTING`.

---

### 6. Automated Verification & Test Matrix

The test suite [`NewCourseAndBranchWorkflowTest.java`](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/backend/src/test/java/org/aicte/sage/NewCourseAndBranchWorkflowTest.java) executes automated integration testing under strict security:

| Test Case | Scenario / Invariant Verified | Outcome |
| :--- | :--- | :--- |
| `testCreateNewCourseApplicationAndCourseDetails` | Institution creates `NEW_COURSE_INTRODUCTION` application, saves course details (B.Tech Robotics, 60 intake, 4 labs, 9 faculty), retrieves application and validates embedded DTO fields. | **PASS (200 OK)** |
| `testCreateNewBranchApplicationAndBranchDetails` | Institution creates `NEW_BRANCH_INTRODUCTION` application, saves branch details (AI & Data Science, 60 intake, 4 faculty, 2 labs), verifies persistence and retrieval. | **PASS (200 OK)** |
| `testScrutinyEvaluationForNewCourse` | Regional Officer triggers scrutiny evaluation (`/api/v1/applications/{id}/scrutiny/evaluate`); engine triggers APH course rules (`RULE_APH_COURSE_FACULTY_RATIO`, `RULE_APH_COURSE_LABORATORY`). | **PASS (200 OK)** |

**Maven Test Run Result**:
```text
[INFO] Running org.aicte.sage.NewCourseAndBranchWorkflowTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.488 s
[INFO] BUILD SUCCESS
```

---

### 7. Regulatory Traceability & Compliance Sign-Off

- **Multi-Tenancy**: Institution officers can only create, view, or update course/branch specifications for their authorized institution. Any attempt by another institution officer returns `403 Forbidden` (`TENANCY_VIOLATION_ATTEMPT` recorded in immutable audit log).
- **Statutory Auditability**: Every course and branch creation or modification triggers a persistent audit event with actor ID, IP address, and timestamp.
