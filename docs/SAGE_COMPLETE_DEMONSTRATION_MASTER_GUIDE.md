# SAGE — System for Approval Governance and Evaluation
## Feature-Focused Prototype Demonstration Master Guide & Operational Manual
**Smart India Hackathon (SIH 2026) • Problem Statement 26262 • AICTE APH 2024–27 Automated Governance**

- **Recommended Demonstration Duration:** 9–11 Minutes
- **Core Principle:** Focus on explaining visible features, screen controls, and statutory workflows in simple, natural language without theoretical buzzwords or marketing hype.

---

### Master Pre-Staged Browser Tabs Architecture (Zero Demonstration Downtime)

To ensure zero downtime from repeated logins, pre-stage these six browser tabs or use the synthetic role switch presets on `/login`:

| Tab # | Persona & Demo Credentials | Target URL & Route | Key Visible Features to Demonstrate |
| :--- | :--- | :--- | :--- |
| **TAB 1** | **Landing / Public**<br>Ready for Step 1 | `http://localhost:5173/login` | **Step 1:** WCAG 2.2 AA accessibility toolbar (font scaling A+/A-, high contrast mode) and 7 synthetic role login presets. |
| **TAB 2** | **Institution Officer**<br>`officer@delhi-tech.edu` | `http://localhost:5173/dashboard`<br>&rarr; `/applications` | **Steps 2, 3 & 4:** Delhi Tech (PID: 1-500123498), 5 tailored application pathways, Pre-Submission Compliance Gate, and Dual-Pane Document Inspector. |
| **TAB 3** | **Regional Officer**<br>`regional@aicte.gov.in` | `http://localhost:5173/applications`<br>&rarr; `/applications/:id` | **Step 5:** Official Scrutiny Console, side-by-side verification notes, and one-click 'Mark Verified Clean' / 'Flag as Deficient' actions. |
| **TAB 4** | **Compliance Officer**<br>`compliance@aicte.gov.in` | `http://localhost:5173/compliance` | **Step 6:** Live certificate countdown clocks (14d/30d), severity badges (INFO/WARN/CRITICAL), and on-demand 'Trigger Compliance Scan' button. |
| **TAB 5** | **EVC Coordinator**<br>`evc.coord@aicte.gov.in` | `http://localhost:5173/evc` | **Step 7:** Accredited Experts Registry (disciplines and states), 5 statutory scheduling rules, and one-click committee schedule generator. |
| **TAB 6** | **Central Admin / Auditor**<br>`admin@aicte.gov.in` | `http://localhost:5173/admin/audit` | **Step 8 & 9:** Searchable audit ledger, SHA-256 digital document fingerprints, Correlation IDs, and final green 'Approved' application state. |

---

## 1. Core Features at a Glance

### 1.1 Five Tailored Application Pathways
- Rather than an endless, generic application form, colleges select from 5 category-specific workflows:
  1. Introduction of New Course / Discipline (APH Clause 2.4)
  2. Extension of Approval (EoA)
  3. Increase in Approved Intake
  4. Establishment of New Technical Institution
  5. Introduction of New Branch / Specialization
- **Feature Benefit:** Each pathway automatically generates the exact statutory document checklist required by the AICTE handbook, eliminating applicant confusion.

### 1.2 Pre-Submission Compliance Gate
- Before an institution can submit an application, the system evaluates all uploaded certificates.
- The submission button remains locked until 100% of mandatory statutory documents are verified clean of defects or tampering indicators.
- **Feature Benefit:** Prevents incomplete or deficient applications from ever entering the council's regulatory queue.

### 1.3 Side-by-Side Dual-Pane Inspector
- When an officer opens any uploaded certificate, SAGE presents a split-screen view:
  - **Left Pane (52%):** Actual document stream showing the original uploaded PDF with zoom (+ / - / 100%), download, and full-tab expansion.
  - **Right Pane (48%):** Inspection console with three tabbed workspaces:
    - *Tab 1: SAGE AI Review & Integrity* (4 statutory pillars)
    - *Tab 2: Current OCR Extraction* (extracted fields table)
    - *Tab 3: Officer Scrutiny Console* (official notes & verification buttons)
- **Feature Benefit:** Scrutiny officers never have to leave the browser or manually re-type certificate numbers; verification takes seconds.

### 1.4 SAGE AI 4-Pillar Document Review & Anomaly Detection
- Evaluates documents across four statutory inspection pillars:
  1. **AI Text Generation:** Detects synthetic cadence or LLM prompt markers (ChatGPT/Claude).
  2. **AICTE Font Standards:** Confirms official typography (Times New Roman / Arial 10–12pt baseline).
  3. **APH Layout & Stamp Paper:** Verifies municipal seals, reference numbers, and the mandatory ₹100 non-judicial stamp paper denomination.
  4. **Regulatory Discrepancies:** Checks that certificate validity spans Academic Year 2024–25 and that college names match.
- **Feature Benefit:** Flags fraudulent affidavits or spliced seals with bold red alert badges, explaining exactly what is wrong so officers catch defects immediately.

### 1.5 Officer Scrutiny Console & Version Control
- Scrutiny officers enter official inspection notes and click **Mark Verified Clean** or **Flag as Deficient**.
- When an application is returned for corrections, the college uploads Version 2; Version 1 remains permanently preserved in the audit drawer with its upload timestamp and digital SHA-256 fingerprint.
- **Feature Benefit:** Ensures a clear, auditable chain of custody with zero silent document replacement.

### 1.6 Continuous Compliance Surveillance & Expiry Sweeps
- Monitors certificate validity dates year-round across all colleges in India.
- Displays live countdown clocks: certificates expiring in 30 days show an orange **Warning**, while expired NOCs escalate to red **Critical Overdue**.
- The **Trigger Compliance Scan** button scans active campuses on demand and dispatches in-app alerts.
- **Feature Benefit:** Proactive compliance prevents statutory lapses before accidents or audit failures occur.

### 1.7 EVC Inspection Scheduling Engine
- Automatically pairs three qualified peer evaluators to campus inspections in under a second.
- Enforces 5 strict statutory constraints:
  1. Exactly three accredited professors per panel (Chairperson + Subject Experts).
  2. Matching engineering and technical disciplines.
  3. Confirmed calendar availability across the 2-day visit.
  4. Consecutive 2-day inspection windows strictly excluding Sundays.
  5. Strict conflict of interest avoidance: professors cannot inspect an institution located in their home state or alma mater.
- **Feature Benefit:** Replaces weeks of manual phone calls and spreadsheet coordination with a fast, fair, and impartial schedule.

### 1.8 Immutable Regulatory Audit Trail & Correlation IDs
- Logs every consequential action across the system: user logins, document uploads, OCR extractions, scrutiny determinations, and scheduling runs.
- Features search and filter controls by Actor, Event Type, and Request Correlation ID.
- **Feature Benefit:** Provides complete non-repudiation and forensic accountability for regulatory oversight.

---

## 2. Recommended 9-Step Feature-Focused Demonstration Sequence (9–11 Minutes)

```
Step 1: Login & Accessibility Toolbar (45s, Landing / Public, Tab 1)
   ↓
Step 2: Institution Workspace & Compliance Gate (60s, Delhi Tech PID, Tab 2)
   ↓
Step 3: Side-by-Side Document & OCR Inspector (90s, Dual-Pane Modal, Tab 2)
   ↓
Step 4: SAGE AI 4-Pillar Document Review (90s, Clean vs Flagged Affidavit, Tab 2)
   ↓
Step 5: Scrutiny Officer Verification Console (75s, Scrutiny Notes & Clean Action, Tab 3)
   ↓
Step 6: Continuous Compliance Monitoring & Sweeps (75s, Countdowns & Trigger Scan, Tab 4)
   ↓
Step 7: EVC Committee Scheduling Engine (90s, Registry & 1-Click Generator, Tab 5)
   ↓
Step 8: Immutable Regulatory Audit Trail (60s, Filter & SHA-256 Fingerprints, Tab 6)
   ↓
Step 9: Final Approved Application State & Wrap-up (45s, Stepper & Certificate, Tab 6)
```

---

### STEP 1 – Portal Login & National Accessibility Toolbar
- **Duration:** 45 seconds (`0:00 – 0:45`)
- **Role / Screen:** Presenter / Public Portal (`BROWSER TAB 1: /login`)
- **Key Features on Screen:**
  - Accessibility Toolbar at the top right: `A-`, `Normal`, `A+` font scaling, and `High Contrast` mode (`Alt+H`).
  - SAGE Emblem and official Government of India / AICTE header bar.
  - One-Click Synthetic Role Switcher: 2-column grid showing all 7 roles (Central Admin, Regional Officer, Delhi Tech, Mumbai Engg, Compliance Officer, EVC Coordinator, Regulatory Auditor).
- **Presenter Pitch (Feature-Focused & Natural Language):**
  > *"Good morning, esteemed jury members. Every year, over ten thousand technical and management colleges across India apply to AICTE for approvals and renewals. In the past, this required managing mountains of paper files and endless manual coordination. To solve this, we built SAGE — the System for Approval Governance and Evaluation.
  >
  > Here on our login screen, notice the Accessibility Toolbar at the top. With one click on 'A+', users with low vision can scale up font sizes across the entire portal. Clicking 'High Contrast' switches to bold, high-contrast borders and text conforming to national WCAG guidelines.
  >
  > Below the credentials, our Role Switcher lets officers and administrators test all seven statutory roles with a single click, eliminating login delays during evaluation. Let's start as the Institution Officer."*
- **Visual Evidence:** ![Step 1 Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_01_accessibility_and_login.png)
- **Key Takeaway:** Accessible by design (WCAG 2.2 AA) with instant role switching for zero demonstration downtime.

---

### STEP 2 – Institution Workspace & Pre-Submission Compliance Gate
- **Duration:** 60 seconds (`0:45 – 1:45`)
- **Role / Screen:** Institution Officer (`BROWSER TAB 2: /applications`)
- **Credentials:** `officer@delhi-tech.edu` (Password: `Password123!`)
- **Key Features on Screen:**
  - Delhi Tech Institution Header with Permanent Institute ID (PID: `1-500123498`).
  - Action Button: `New Approval Application` opening the 5-category modal with statutory document lists.
  - Green Pre-Submission Compliance Gate banner displaying `Ready for Statutory Submission (3/3)` and progress bar `3 of 3 Statutory Documents (100%)`.
- **Presenter Pitch (Feature-Focused & Natural Language):**
  > *"Logged in as Delhi Institute of Technology, notice that institutional data is completely private — colleges can only see their own records. When I click 'New Approval Application', SAGE doesn't give us one confusing, generic form. Instead, it offers five tailored pathways: such as introducing a new course, opening a new branch, or renewing approval.
  >
  > Today, we select 'New Course Introduction'. In the application workspace, look at our green Pre-Submission Compliance Gate. Before an institution can submit, the system automatically checks that all mandatory documents — like fire safety NOCs and affidavits — are uploaded and clean. If any document is missing or invalid, the submit button is locked. This single feature prevents thousands of incomplete files from ever clogging AICTE's scrutiny queue."*
- **Visual Evidence:**
  - Category Modal: ![Step 2a Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_02a_new_course_category_modal.png)
  - Compliance Gate: ![Step 2b Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_02b_institution_compliance_gate.png)
- **Key Takeaway:** Tailored application pathways replace generic forms; pre-submission gates eliminate incomplete submissions.

---

### STEP 3 – Side-by-Side Dual-Pane Document & OCR Inspector
- **Duration:** 90 seconds (`1:45 – 3:15`)
- **Role / Screen:** Document Review Desk (`BROWSER TAB 2: /applications/:id`)
- **Key Features on Screen:**
  - `Inspect Document & AI` button on the Fire Safety Certificate row.
  - Dual-Pane Inspector Modal:
    - **Left Pane (52%):** Actual document stream rendering the PDF with zoom controls (+ / - / 100%), download button, and SHA-256 fingerprint.
    - **Right Pane (48%):** Current OCR Extraction tab showing extracted Certificate Number (`DFS/MS/2024/SZ/8891`), Issuing Fire Service, Issue Date (`2024-05-15`), Expiry Date (`2027-05-14`), and Institution Name with individual 100% confidence ratings.
- **Presenter Pitch (Feature-Focused & Natural Language):**
  > *"Now let's examine how SAGE handles documents. When an officer clicks 'Inspect Document', our Dual-Pane Inspector opens. On the left side, the officer sees the actual uploaded PDF certificate with zoom controls and file details. On the right side, under the 'Current OCR Extraction' tab, our built-in OCR engine has already read the document and extracted the certificate number, issuing fire station, issue date, and expiration date — along with 100% confidence ratings.
  >
  > The scrutiny officer doesn't need to manually re-type numbers or flip back and forth between different windows. Everything is presented side-by-side so they can verify the certificate in seconds."*
- **Visual Evidence:** ![Step 3 Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_03_dual_pane_inspector_ocr_extraction.png)
- **Key Takeaway:** Side-by-side inspection pairs original PDF with extracted statutory fields, eliminating manual data entry.

---

### STEP 4 – SAGE AI 4-Pillar Document Review & Anomaly Detection
- **Duration:** 90 seconds (`3:15 – 4:45`)
- **Role / Screen:** Scrutiny Desk (Dual-Pane Inspector Modal: `SAGE AI Review & Integrity` Tab)
- **Key Features on Screen:**
  - 4-Pillar Grid on Clean Document:
    1. `1. AI Text Generation` -> `CLEAN (0% AI)`
    2. `2. AICTE Font Norms` -> `COMPLIANT` (Times New Roman 11pt)
    3. `3. APH Format & Seal` -> `APH VERIFIED`
    4. `4. Discrepancy Detection` -> `0 DISCREPANCIES`
  - Red Flag Detection on Suspicious Affidavit:
    - Red `Alert` pill badge on the tab.
    - Bold red badge `SUSPECTED_AI` (Probable automated LLM/ChatGPT generation).
    - Bold red badge `DEFICIENT / SPLICED` (Missing ₹100 non-judicial stamp paper valuation).
  - Blue Statutory Advisory Banner: Clarifies that AI assists with fraud detection, but authorized human officers make the final determination.
- **Presenter Pitch (Feature-Focused & Natural Language):**
  > *"Next, let's switch to the 'SAGE AI Review & Integrity' tab. Every document is checked across four statutory pillars: First, AI text generation — scanning for synthetic phrasing to verify that affidavits weren't fabricated using ChatGPT. Second, AICTE font standards — ensuring official fonts like Times New Roman 10-to-12 point are used. Third, format adherence — verifying municipal seals and the mandatory 100-rupee non-judicial stamp paper. Fourth, date discrepancies — checking that dates match the active academic year.
  >
  > On a clean certificate, all four pillars show green verified badges. But look what happens when an applicant uploads an AI-generated affidavit: the system instantly highlights it in bold red as 'SUSPECTED_AI' with an alert badge, pointing out missing stamp paper denomination. Most importantly: our advisory notice clarifies that AI assists with fraud detection, but human officers make the final decision."*
- **Visual Evidence:**
  - Clean Check: ![Step 4a Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_04a_sage_ai_4_pillar_clean.png)
  - Anomaly Alerts: ![Step 4b Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_04b_sage_ai_4_pillar_flagged_alerts.png)
- **Key Takeaway:** 4-pillar AI checks catch synthetic text and missing stamp paper, while keeping human officers in legal control.

---

### STEP 5 – Regional Scrutiny Officer Verification Console
- **Duration:** 75 seconds (`4:45 – 6:00`)
- **Role / Screen:** Regional Officer (`BROWSER TAB 3: /applications/:id`)
- **Credentials:** `regional@aicte.gov.in` (Password: `Password123!`)
- **Key Features on Screen:**
  - `Officer Scrutiny & Verification` tab inside the Dual-Pane Inspector.
  - Verification Status Banner: `PENDING_REVIEW` &bull; `Compliance State: COMPLIANT` &bull; `Version: v1`.
  - Authorized Statutory Officer Determination card with Scrutiny Remarks textarea.
  - Action Buttons: `Flag as Deficient` (red outline) and `Mark Verified Clean` (solid green button).
- **Presenter Pitch (Feature-Focused & Natural Language):**
  > *"Now we log in as the AICTE Northern Regional Officer. Because this user holds official regulatory authority, the 'Officer Scrutiny & Verification' tab is fully unlocked. The officer reviews the original certificate on the left, reads the OCR and AI findings on the right, types their official scrutiny notes into the text box, and clicks 'Mark Verified Clean'.
  >
  > If the officer notices a problem, they can click 'Flag as Deficient'. SAGE immediately returns the application to the college with the exact reason. When the college uploads a corrected certificate, Version 1 remains permanently preserved in our audit drawer, and Version 2 becomes active. No document is ever silently replaced or erased."*
- **Visual Evidence:** ![Step 5 Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_05_scrutiny_officer_console.png)
- **Key Takeaway:** Authorized officer console enables official determination notes; version control keeps an unbroken audit history.

---

### STEP 6 – Continuous Compliance Monitoring & Expiry Sweeps
- **Duration:** 75 seconds (`6:00 – 7:15`)
- **Role / Screen:** Compliance Officer (`BROWSER TAB 4: /compliance`)
- **Credentials:** `compliance@aicte.gov.in` (Password: `Password123!`)
- **Key Features on Screen:**
  - Overview KPI Cards: `OPEN COMPLIANCE FINDINGS (16)`, `CRITICAL FAILURES (1)`, `URGENT STATUTORY DEADLINES (9)`, `TOTAL MONITORED RUNS (2)`.
  - Blue Action Button: `Trigger Compliance Scan`.
  - Horizontal Tabs: `Statutory Deadlines (17)`, `Compliance Findings (16)`, `Scan Audit Runs (2)`.
  - Expiry Countdown Cards: `OVERDUE (30d ago)` for expired fire safety NOCs, `OVERDUE (15d ago)`, target dates, and one-click actions (`Audit History`, `Recalculate Date`, `Resolve`).
- **Presenter Pitch (Feature-Focused & Natural Language):**
  > *"Regulatory governance doesn't end when approval is granted. Certificates expire throughout the academic year. In Tab 4, we are logged in as the AICTE Compliance Officer.
  >
  > On this dashboard, countdown timers track expiring certificates across all institutions. A certificate with 30 days remaining displays an orange 'Warning' badge, while an expired NOC triggers a red 'Critical Overdue' alert. When I click 'Trigger Compliance Scan', SAGE scans active institutions on demand and issues in-app alerts so colleges and officers can renew safety certificates weeks before deadlines pass, preventing safety lapses."*
- **Visual Evidence:** ![Step 6 Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_06_compliance_dashboard_and_sweeps.png)
- **Key Takeaway:** Automated surveillance sweeps and live countdown clocks ensure proactive compliance year-round.

---

### STEP 7 – Expert Visit Committee (EVC) Scheduling Engine
- **Duration:** 90 seconds (`7:15 – 8:45`)
- **Role / Screen:** EVC Coordinator (`BROWSER TAB 5: /evc`)
- **Credentials:** `evc.coord@aicte.gov.in` (Password: `Password123!`)
- **Key Features on Screen:**
  - KPI Cards: `PENDING SCHEDULING (4)`, `CONFIRMED SCHEDULES (4)`, `ACCREDITED EXPERTS POOL (18)`.
  - Tabs: `Pending Inspections & Scheduling (4)`, `Confirmed Inspection Schedules (4)`, `Accredited Experts Registry`.
  - Schedule Generation Controls: Solver Time Limit (10 seconds), Checkbox for Rescheduling, and `Generate Committee Schedule` button.
  - Pending Inspections Table: Mumbai Academy (2 days), Bengaluru Polytechnic (3 experts), Coastal Engineering (3 experts), Deccan Institute (3 experts).
- **Presenter Pitch (Feature-Focused & Natural Language):**
  > *"When a new course requires an on-campus inspection, an Expert Visit Committee must be assigned. In the past, coordinating this on spreadsheets took weeks of phone calls: you need three professors from matching engineering fields, they must be free on two consecutive days excluding Sundays, and they must have zero conflict of interest — meaning they cannot inspect a college in their home state or where they studied.
  >
  > In Tab 5, our EVC Coordinator opens the Panel Allocation screen. When I click 'Generate Committee Schedule', SAGE tests all availability, discipline, and conflict-of-interest rules, and produces a complete, conflict-free panel of three accredited professors in less than a second! It transforms weeks of manual coordination into an instant, fair schedule."*
- **Visual Evidence:** ![Step 7 Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_07_evc_cpsat_scheduling.png)
- **Key Takeaway:** Automated panel allocation enforces 5 statutory rules (3 experts, no Sundays, zero home-state conflict) in <1 second.

---

### STEP 8 – Immutable Regulatory Audit Trail & Correlation IDs
- **Duration:** 60 seconds (`8:45 – 9:45`)
- **Role / Screen:** Central Administrator / Auditor (`BROWSER TAB 6: /admin/audit`)
- **Credentials:** `admin@aicte.gov.in` (Password: `Password123!`)
- **Key Features on Screen:**
  - Title: `Immutable Regulatory Audit Trail`.
  - Filter Bar: Principal (Actor), Event Type, Resource Type, Correlation ID search box, and `Filter` button.
  - Audit Ledger Table:
    - UTC Timestamps
    - Event Type Badges (`AUTH_LOGIN_SUCCESS`, `AUTH_LOGOUT`, `DOCUMENT_UPLOAD`, `SCRUTINY_PASS`)
    - Principal Emails (`admin@aicte.gov.in`, `regional@aicte.gov.in`, `officer@delhi-tech.edu`)
    - Resource Target UUIDs
    - Clickable Correlation IDs (`4ee01b2a`, `98e145f5`, `5aaea73d`)
    - Audit Summaries
- **Presenter Pitch (Feature-Focused & Natural Language):**
  > *"Accountability is essential in regulatory governance. In Tab 6, we view the SAGE Immutable Audit Trail. Every single action — who logged in, who uploaded a certificate, what the OCR extracted, who marked a document clean, and who generated an inspection schedule — is permanently recorded here.
  >
  > Administrators can filter the ledger by user email, event type, or tracking ID. Each entry records the exact timestamp, user identity, IP address, and the SHA-256 digital fingerprint of every uploaded file. Even if an unauthorized user attempts to view another college's file, a tenancy violation log is created immediately. This guarantees complete honesty and non-repudiation."*
- **Visual Evidence:** ![Step 8 Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_08_immutable_audit_ledger.png)
- **Key Takeaway:** Searchable, tamper-proof audit log records every event with digital file fingerprints and Correlation IDs.

---

### STEP 9 – Final Approved Application State & Governance Impact
- **Duration:** 45 seconds (`9:45 – 10:30`)
- **Role / Screen:** Presenter / Application Details (`BROWSER TAB 2 or 6: /applications/:id`)
- **Key Features on Screen:**
  - Application Header: `APP-202425-1-500123498-8769` with solid green `Approved` badge.
  - Subtitle: `Delhi Institute of Advanced Engineering & Technology • Category: EXTENSION OF APPROVAL • Session: 2024-25`.
  - 6-Step Visual Progress Stepper: `1 Draft` &rarr; `2 Submitted` &rarr; `3 Scrutiny & OCR` &rarr; `4 Scrutiny Passed` &rarr; `5 EVC / Committee` &rarr; `6 Final Decision` (Orange active milestone).
  - Three Tabs: `Compliance Documents & OCR (10)`, `Deterministic Scrutiny Findings (5)`, `Workflow & Audit Trail (4)`.
- **Presenter Pitch (Feature-Focused & Natural Language):**
  > *"To conclude: look at Delhi Tech’s completed application on screen. It has traveled the full statutory journey: from college submission through side-by-side OCR review, 4-pillar AI fraud detection, scrutiny verification, committee scheduling, and final Council Approval. You can see the solid green 'Approved' badge, the 6-step progress stepper, and the complete audit history below.
  >
  > SAGE transforms the 200-page AICTE handbook into an intuitive digital platform. By focusing on practical features — side-by-side inspection, automated checks, live compliance countdowns, and conflict-free committee scheduling — SAGE turns a multi-month paper headache into a fast, fair, and completely transparent digital experience. Thank you, and we welcome your questions."*
- **Visual Evidence:** ![Step 9 Screenshot](file:///home/Harshit/Desktop/PROJECTS/SIH%202026/AICTE/SAGE/docs/screenshots/step_09_final_approved_application.png)
- **Key Takeaway:** Concludes on the final approved screen, proving complete end-to-end lifecycle governance.

---

## 3. Jury Defense Cheat Sheet (Clear & Practical Answers)

| Frequently Asked Question | Clear, Feature-Grounded Answer |
| :--- | :--- |
| **Q1: What happens if an applicant uploads a fake document or an AI-generated affidavit?** | SAGE's Dual-Pane Inspector automatically runs every uploaded document through our 4-Pillar Document Check. If an applicant uses ChatGPT to draft an affidavit or uploads a certificate without official stamp paper, the system flags the document in bold red with an alert badge and explains exactly what is wrong. The scrutiny officer sees this immediately and can return the file with one click. |
| **Q2: Does SAGE approve applications automatically without human oversight?** | No, absolutely not. SAGE is designed around human accountability. The software performs OCR extraction, highlights dates, checks fonts, and identifies potential tampering, but the final determination requires an authorized Scrutiny Officer to review the file, enter official notes, and click 'Mark Verified Clean' or 'Flag as Deficient'. |
| **Q3: How does the system make sure inspection committee members are completely impartial?** | Our committee allocation engine enforces strict statutory rules: it checks each professor's registered home state and alma mater, and automatically excludes them from inspecting any institution in that state. It also guarantees exactly three qualified professors per panel and strictly excludes Sundays from campus visits. |
| **Q4: Can an institution replace a bad document and erase their mistake?** | No. SAGE uses immutable document version control. When an institution uploads a corrected Version 2 certificate, the original Version 1 file remains permanently archived in the audit drawer with its upload timestamp, the officer who uploaded it, and its digital file fingerprint. The full evidence history remains visible to AICTE officers. |
| **Q5: Can one college view or tamper with another college's application data?** | No. All data access is strictly isolated at the database level by institutional identity. If a college user tries to view or alter records belonging to another institution, the system blocks the request immediately with an access denied error and creates a security violation log in the audit trail. |

---

## 4. Master Demonstration Verification Artifacts

All live visual verification screenshots are generated, verified, and preserved at:
- `docs/screenshots/step_01_accessibility_and_login.png`
- `docs/screenshots/step_02a_new_course_category_modal.png`
- `docs/screenshots/step_02b_institution_compliance_gate.png`
- `docs/screenshots/step_03_dual_pane_inspector_ocr_extraction.png`
- `docs/screenshots/step_04a_sage_ai_4_pillar_clean.png`
- `docs/screenshots/step_04b_sage_ai_4_pillar_flagged_alerts.png`
- `docs/screenshots/step_05_scrutiny_officer_console.png`
- `docs/screenshots/step_06_compliance_dashboard_and_sweeps.png`
- `docs/screenshots/step_07_evc_cpsat_scheduling.png`
- `docs/screenshots/step_08_immutable_audit_ledger.png`
- `docs/screenshots/step_09_final_approved_application.png`

**Master Printable PDF:** `docs/SAGE_FINAL_MASTER_DEMONSTRATION_GUIDE.pdf` (Synced to `docs/SAGE_COMPLETE_DEMONSTRATION_MASTER_GUIDE.pdf` and `docs/SAGE_VIDEO_PRESENTATION_SCRIPT.pdf`).
