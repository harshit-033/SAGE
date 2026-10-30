# SAGE — New Features Step-by-Step Demonstration Guide

> **Target Features**: New Course/Branch Workflows + Document Compliance & Required Actions  
> **Environment**: Local Development (`./run.sh` or Docker)  
> **Audience**: AICTE Officers, System Evaluators, Hackathon Judges, and Demonstration Teams

---

### Demonstration Credentials Reference

| Role | Username / Email | Password | Primary Demonstration Focus |
| :--- | :--- | :--- | :--- |
| **Institution Officer (Delhi)** | `officer@delhi-tech.edu` | `password123` | Create New Course/Branch apps, upload documents, respond to actions. |
| **Institution Officer (Mumbai)** | `officer@mumbai-engg.edu` | `password123` | Multi-tenancy cross-institution boundary isolation test. |
| **Regional Officer** | `regional@aicte.gov.in` | `password123` | Scrutiny evaluation, document review, clarification, action resolution. |
| **Central Admin** | `admin@aicte.gov.in` | `password123` | Master governance, audit logs, distributed locks, system sweeps. |

---

### Demo Scenario 1: New Course Introduction Workflow (`NEW_COURSE_INTRODUCTION`)

#### Step 1: Login as Institution Officer
1. Open `http://localhost:5173/login`.
2. Enter email: `officer@delhi-tech.edu`, password: `password123`.
3. System redirects to the **Institution Dashboard**.

#### Step 2: Initiate Application
1. Click **Applications** in the left sidebar (or top navigation).
2. Click the orange **+ Initiate Approval Application** button.
3. Select Category: **New Course Introduction (`NEW_COURSE_INTRODUCTION`)**.
4. Notice how the modal dynamically expands to reveal the **APH Course Specification Form**:
   - **Course Name**: Enter `B.Tech in Robotics and Autonomous Systems`
   - **Programme Level**: Select `UNDERGRADUATE`
   - **Degree Type**: Select `BACHELOR_OF_TECHNOLOGY`
   - **Discipline**: Select `ENGINEERING_AND_TECHNOLOGY`
   - **Proposed Department**: `Department of Mechanical & Mechatronics Engineering`
   - **Proposed Intake**: `60`
   - **Duration (Years)**: `4`
   - **Dedicated Laboratories Setup**: `4`
   - **Professors**: `1`
   - **Associate Professors**: `2`
   - **Assistant Professors**: `6`
   - **Classrooms Available**: `3`
   - **Laboratory Setup Details**: `Equipped with KUKA industrial robot arms, ROS workstations, and sensor interfacing kits.`
5. Click **Create Application**.
6. The application is created in `DRAFT` status and appears in the applications list.

#### Step 3: Inspect Specification Tab & In-Place Editing
1. Click **View Details** on the newly created application.
2. Notice the dedicated **Course Specification & Labs** tab.
3. View the 4 summary metric tiles:
   - Proposed Intake: `60 Seats`
   - Duration & Delivery: `4 Years (REGULAR)`
   - Laboratories Setup: `4 Dedicated Labs`
   - Faculty Identified: `9 Faculty Members`
4. Click **Edit Specification** (available in `DRAFT` status).
5. Modify `Laboratories Setup` to `5` and click **Save Course Details**.
6. Observe the immediate update and query invalidation without page reload.

#### Step 4: Upload Required APH Course Documents
1. Switch to the **Compliance Documents & OCR** tab.
2. Select Document Type: **Faculty Cadre & Qualification Roster (APH 2024)**.
3. Select sample PDF or scan file and click **Upload & Scrutinize**.
4. Repeat for **Laboratory Setup & Carpet Area Report**.
5. Switch to the application header and click **Submit Application**. The status transitions to `SUBMITTED`.

#### Step 5: Officer Review & Deterministic Scrutiny
1. Log out and log in as `regional@aicte.gov.in` / `password123`.
2. Navigate to **Applications** and open the submitted application.
3. Click **Evaluate Scrutiny Rules**.
4. Switch to the **Deterministic Scrutiny Findings** tab.
5. Inspect the newly evaluated rules:
   - `RULE_APH_COURSE_FACULTY_RATIO` (APH Clause 4.2, Table 4.1): Verifies 1:20 ratio and cadre distribution.
   - `RULE_APH_COURSE_LABORATORY` (APH Clause 6.3, Appendix 5): Verifies >= 4 dedicated laboratories.

---

### Demo Scenario 2: New Branch / Specialization Workflow (`NEW_BRANCH_INTRODUCTION`)

#### Step 1: Create Branch Application
1. Login as `officer@delhi-tech.edu`.
2. In **Applications**, click **+ Initiate Approval Application**.
3. Select Category: **New Branch / Specialization Introduction (`NEW_BRANCH_INTRODUCTION`)**.
4. Complete the dynamic Branch specification form:
   - **Existing Programme**: `ENGINEERING_AND_TECHNOLOGY`
   - **Existing Department**: `Computer Science and Engineering`
   - **Proposed Branch / Specialization**: `Artificial Intelligence and Data Science`
   - **Proposed Branch Intake**: `60`
   - **Existing Parent Intake**: `120`
   - **Core Faculty Allocated**: `4`
   - **Specialized Laboratories**: `2`
5. Click **Create Application**.

#### Step 2: Verify Branch Specification Card
1. Open the application details view.
2. Notice the **Branch Specification & Intake** tab displaying parent-branch intake correlation and dedicated faculty.

---

### Demo Scenario 3: Institution Document Compliance Tracking Hub

#### Step 1: Open Document Registry
1. Login as `officer@delhi-tech.edu`.
2. Click **Documents & Compliance** in the left sidebar (URL: `/documents-compliance`).
3. Observe the 6 executive summary cards:
   - **Total Documents Registry**: Total institutional records.
   - **Verified Valid**: Documents approved by AICTE officers.
   - **Pending Verification**: Awaiting regional officer scrutiny.
   - **Expiring in 30 Days**: Warning pill for impending expirations.
   - **Expired Certificates**: Critical attention required.
   - **Open Corrective Actions**: Remediation backlog.

#### Step 2: Upload Standalone Regulatory Certificate
1. Click **+ Upload Document** in the top-right header.
2. Fill the form:
   - **Document Name**: `Building Fire Safety NOC 2026-2027`
   - **Document Category**: `MANDATORY_REGULATORY`
   - **Document Type**: `FIRE_SAFETY_CERTIFICATE`
   - **Valid From**: `2026-01-01`
   - **Expiry Date**: Select a date 20 days in the future to demonstrate the `EXPIRING_SOON` badge.
   - Select a valid PDF file.
3. Click **Upload & Register Document**.
4. The document appears in the registry with an **Expiring Soon** badge.

#### Step 3: Explore the 6-Tab Deep Dive Modal
1. Click on the document row (or the eye icon).
2. The detailed drawer opens:
   - **Information**: Displays SHA-256 fingerprint, file size, validity dates, verification status.
   - **Secure Preview**: Embedded preview with direct secure download link.
   - **OCR Extracted Fields**: Shows extracted certificate numbers, issuing authority, and confidence scores.
   - **Findings**: Displays any linked scrutiny observations.
   - **Actions**: Shows linked corrective action items.
   - **Version History**: Shows `Version 1` as the active base version.

#### Step 4: Upload Immutable Replacement Version (v2)
1. In the document row or inside the modal, click **Replace / Renew**.
2. Select a renewed certificate file.
3. Enter replacement reason: `Annual renewal certificate issued by Delhi Fire Services Department.`
4. Click **Upload Replacement Version**.
5. Switch to the **Version History** tab in the modal:
   - Observe **Version 2** is now the latest active document.
   - **Version 1** is preserved as an immutable historic node with parent reference.

---

### Demo Scenario 4: Required Actions Module & Officer Review

#### Step 1: Officer Review & Deficiency Generation
1. Login as `regional@aicte.gov.in`.
2. Open `/documents-compliance`.
3. Locate a pending document and click **Review**.
4. In the Officer Review Modal:
   - Choose Decision: **Reject with Required Corrective Action**.
   - Enter Officer Comments: `The submitted certificate lacks the official seal of the Chief Fire Officer.`
5. Click **Submit Review Decision**.
6. The document transitions to `REJECTED`, and an `OPEN` item is automatically created in `document_corrective_actions`.

#### Step 2: Institution Remediation in Required Actions
1. Log out and login as `officer@delhi-tech.edu`.
2. Click **Required Actions** in the sidebar (URL: `/required-actions`).
3. Notice the newly opened action item: `The submitted certificate lacks the official seal...`.
4. Click **Submit Response & Rectification**.
5. Enter institutional explanation: `Affixed official seal obtained from Municipal Headquarters; updated certificate uploaded as version 2.`
6. Click **Submit Response**.
7. Status advances to `SUBMITTED_FOR_REVIEW`.

#### Step 3: Officer Resolution Sign-off
1. Switch back to `regional@aicte.gov.in`.
2. Navigate to `/required-actions`.
3. Click **Review & Resolve** on the submitted item.
4. Set Decision to **RESOLVED (Mark Deficiency Rectified)**.
5. Enter resolution note: `Inspected revised stamped document; verified authentic.`
6. Click **Confirm Resolution**. The deficiency is closed and verified.

---

### Demo Scenario 5: Multi-Tenant Security & Tenant Boundary Proof

#### Step 1: Unauthorized Cross-Tenant Request Test
1. Log in as `officer@mumbai-engg.edu` (Mumbai Engineering College).
2. Attempt to open a document belonging to Delhi Institute (`/api/v1/documents/{delhiDocId}`).
3. The server immediately responds with:
   ```json
   {
     "status": 403,
     "title": "Forbidden",
     "detail": "Cross-tenant access violation: Principal is not authorized to access Institution ID"
   }
   ```
4. A permanent statutory audit log event `TENANCY_VIOLATION_ATTEMPT` is recorded with correlation ID, IP address, and timestamp.
