#!/usr/bin/env python3
"""
SAGE — Complete Prototype Demonstration Feature List, Pitch Script & Tab Architecture PDF Generator
Refined with informative, simple, natural, and clear language for live evaluators and presenters.
"""

import os
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, KeepTogether, PageBreak, Image
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    """Two-pass canvas for dynamic 'Page X of Y' footers and running headers."""
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super(NumberedCanvas, self).showPage()
        super(NumberedCanvas, self).save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica-Bold", 8)
        self.setFillColor(colors.HexColor("#64748b"))
        
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(36, 842 - 25, "SAGE — System for Approval Governance and Evaluation")
            self.setFont("Helvetica", 8)
            self.drawRightString(595 - 36, 842 - 25, "Complete Prototype Demonstration Manual & Pitch Guide")
            self.setStrokeColor(colors.HexColor("#cbd5e1"))
            self.setLineWidth(0.6)
            self.line(36, 842 - 28, 595 - 36, 842 - 28)
            
        # Footer
        self.setFont("Helvetica", 8)
        self.drawString(36, 20, "SIH 2026 Problem Statement 26262 | AICTE APH 2024–27 Automated Governance")
        page_str = f"Page {self._pageNumber} of {page_count}"
        self.setFont("Helvetica-Bold", 8)
        self.drawRightString(595 - 36, 20, page_str)
        self.setStrokeColor(colors.HexColor("#cbd5e1"))
        self.setLineWidth(0.6)
        self.line(36, 28, 595 - 36, 28)
        
        self.restoreState()


def build_demo_guide_pdf(output_path: str):
    doc = SimpleDocTemplate(
        output_path,
        pagesize=A4,
        leftMargin=36,
        rightMargin=36,
        topMargin=36,
        bottomMargin=36
    )

    styles = getSampleStyleSheet()

    # Color Palette
    C_NAVY = colors.HexColor("#0a192f")
    C_SAFFRON = colors.HexColor("#ea580c")
    C_BLUE = colors.HexColor("#1d4ed8")
    C_GREEN = colors.HexColor("#15803d")
    C_DARK = colors.HexColor("#0f172a")
    C_SLATE = colors.HexColor("#334155")
    C_AMBER = colors.HexColor("#b45309")
    C_RED = colors.HexColor("#b91c1c")
    
    # Custom Typography Styles
    doc_title_style = ParagraphStyle(
        'MainDocTitle', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=17, leading=21, textColor=C_NAVY, spaceAfter=2
    )
    doc_subtitle_style = ParagraphStyle(
        'MainDocSubtitle', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=9, leading=12, textColor=C_SAFFRON, spaceAfter=4
    )
    doc_meta_style = ParagraphStyle(
        'MainDocMeta', parent=styles['Normal'],
        fontName='Helvetica', fontSize=8, leading=11, textColor=C_SLATE, spaceAfter=6
    )

    h1_style = ParagraphStyle(
        'H1Style', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=11, leading=14, textColor=C_NAVY, spaceBefore=8, spaceAfter=4
    )

    step_header_title = ParagraphStyle(
        'StepHeaderTitle', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=10, leading=13, textColor=colors.white
    )
    step_header_meta = ParagraphStyle(
        'StepHeaderMeta', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=7.5, leading=10, textColor=colors.HexColor("#fef08a")
    )

    dialogue_style = ParagraphStyle(
        'DialogueStyle', parent=styles['Normal'],
        fontName='Times-Roman', fontSize=8.5, leading=12, textColor=C_DARK, spaceAfter=4
    )
    speaker_title_style = ParagraphStyle(
        'SpeakerTitle', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=8, leading=10, textColor=C_SAFFRON, spaceAfter=2
    )

    cue_title_style = ParagraphStyle(
        'CueTitle', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=8, leading=10, textColor=colors.HexColor("#1e3a8a"), spaceAfter=2
    )
    cue_body_style = ParagraphStyle(
        'CueBody', parent=styles['Normal'],
        fontName='Helvetica', fontSize=7.5, leading=10, textColor=C_DARK
    )
    tab_badge_style = ParagraphStyle(
        'TabBadge', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=7.5, leading=10, textColor=C_AMBER
    )

    takeaway_title = ParagraphStyle(
        'TakeawayTitle', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=7.5, leading=9.5, textColor=C_GREEN, spaceAfter=2
    )
    takeaway_body = ParagraphStyle(
        'TakeawayBody', parent=styles['Normal'],
        fontName='Helvetica', fontSize=7, leading=9.5, textColor=colors.HexColor("#14532d")
    )

    cell_style = ParagraphStyle(
        'GridCell', parent=styles['Normal'],
        fontName='Helvetica', fontSize=7, leading=9.5, textColor=C_DARK
    )
    cell_bold = ParagraphStyle(
        'GridCellBold', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=7, leading=9.5, textColor=C_DARK
    )
    cell_header = ParagraphStyle(
        'GridHeader', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=7.5, leading=9.5, textColor=colors.white
    )
    cell_warn = ParagraphStyle(
        'GridWarn', parent=styles['Normal'],
        fontName='Helvetica', fontSize=7, leading=9.5, textColor=C_RED
    )

    story = []

    # ==========================================
    # HEADER & TRICOLOR BAR
    # ==========================================
    logo_path = "/home/Harshit/Desktop/PROJECTS/SIH 2026/AICTE/SAGE/frontend/public/sage-logo.png"
    if os.path.exists(logo_path):
        logo_img = Image(logo_path, width=44, height=44)
        title_block = [
            Paragraph("SAGE — System for Approval Governance and Evaluation", doc_title_style),
            Paragraph("Complete Prototype Demonstration Feature List, Pitch Script &amp; Tab Architecture", doc_subtitle_style),
            Paragraph("<b>Recommended Duration:</b> 9–11 Minutes &bull; <b>Objective:</b> Innovation, Automation, Traceability &amp; Governance &bull; <b>SIH 2026 PS 26262</b>", doc_meta_style),
        ]
        h_table = Table([[logo_img, title_block]], colWidths=[52, 471])
        h_table.setStyle(TableStyle([
            ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
            ('LEFTPADDING', (0,0), (-1,-1), 0),
            ('RIGHTPADDING', (0,0), (-1,-1), 0),
            ('TOPPADDING', (0,0), (-1,-1), 0),
            ('BOTTOMPADDING', (0,0), (-1,-1), 0),
        ]))
        story.append(h_table)
    else:
        story.append(Paragraph("SAGE — System for Approval Governance and Evaluation", doc_title_style))
        story.append(Paragraph("Complete Prototype Demonstration Feature List, Pitch Script &amp; Tab Architecture", doc_subtitle_style))
        story.append(Paragraph("<b>Recommended Duration:</b> 9–11 Minutes &bull; <b>Objective:</b> Innovation, Automation, Traceability &amp; Governance &bull; <b>SIH 2026 PS 26262</b>", doc_meta_style))

    # Tricolor strip
    tri = Table([["", "", ""]], colWidths=[174, 175, 174], rowHeights=[2.5])
    tri.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), colors.HexColor("#ff9933")),
        ('BACKGROUND', (1,0), (1,0), colors.HexColor("#002147")),
        ('BACKGROUND', (2,0), (2,0), colors.HexColor("#138808")),
        ('TOPPADDING', (0,0), (-1,-1), 0), ('BOTTOMPADDING', (0,0), (-1,-1), 0),
        ('LEFTPADDING', (0,0), (-1,-1), 0), ('RIGHTPADDING', (0,0), (-1,-1), 0),
    ]))
    story.append(tri)
    story.append(Spacer(1, 6))

    # ==========================================
    # PRE-STAGED BROWSER TABS MATRIX
    # ==========================================
    story.append(Paragraph("<b>PRE-STAGED BROWSER TABS (OPEN IN ADVANCE FOR 9–11 MIN DEMO)</b>", h1_style))
    story.append(Paragraph("To ensure zero downtime from repeated logins, pre-stage these 6 tabs in separate browser profiles/windows:", cell_style))
    story.append(Spacer(1, 3))

    tabs_pre_data = [
        [
            Paragraph("<b>Tab #</b>", cell_header),
            Paragraph("<b>Persona &amp; Demo Credentials</b>", cell_header),
            Paragraph("<b>Target URL &amp; Route</b>", cell_header),
            Paragraph("<b>Demonstration Focus &amp; Pre-Staged State</b>", cell_header)
        ],
        [
            Paragraph("<b>TAB 1</b>", cell_bold),
            Paragraph("<b>Landing / Public</b><br/>Ready for Step 1", cell_style),
            Paragraph("<code>http://localhost:5173/login</code>", cell_style),
            Paragraph("WCAG 2.2 AA accessibility toolbar (font scaling, high contrast) and synthetic role matrix.", cell_style)
        ],
        [
            Paragraph("<b>TAB 2</b>", cell_bold),
            Paragraph("<b>Institution Officer</b><br/><code>officer@delhi-tech.edu</code>", cell_style),
            Paragraph("<code>/dashboard</code> &rarr; <code>/applications</code><br/>Delhi Tech (PID: 1-500123498)", cell_style),
            Paragraph("Steps 2, 3 &amp; 5: Create New Course app, Document OCR extraction, SAGE AI findings, submit rectified v2 certificate.", cell_style)
        ],
        [
            Paragraph("<b>TAB 3</b>", cell_bold),
            Paragraph("<b>Regional Officer</b><br/><code>regional.north@aicte-demo.gov.in</code>", cell_style),
            Paragraph("<code>/applications</code> &rarr; <code>/applications/:id</code><br/>App ID: <code>APP-2025-DEL-0103</code>", cell_style),
            Paragraph("Steps 4 &amp; 5: Scrutiny queue, expired Fire Safety check, record deficiency with evidence, return app, re-scrutinize v2.", cell_style)
        ],
        [
            Paragraph("<b>TAB 4</b>", cell_bold),
            Paragraph("<b>Compliance Officer</b><br/><code>compliance.officer1@aicte-demo.gov.in</code>", cell_style),
            Paragraph("<code>http://localhost:5173/compliance</code><br/>Compliance Directorate Desk", cell_style),
            Paragraph("Step 6: Expiry countdowns, severity findings (INFO/WARN/CRITICAL), scheduled midnight sweeps, in-app alerts.", cell_style)
        ],
        [
            Paragraph("<b>TAB 5</b>", cell_bold),
            Paragraph("<b>EVC Coordinator</b><br/><code>evc.coord1@aicte-demo.gov.in</code>", cell_style),
            Paragraph("<code>http://localhost:5173/evc</code><br/>EVC Operations Hub", cell_style),
            Paragraph("Step 7: Expert roster, disciplines, availability, conflict-of-interest rules, Google OR-Tools CP-SAT solver execution.", cell_style)
        ],
        [
            Paragraph("<b>TAB 6</b>", cell_bold),
            Paragraph("<b>Auditor / Central Admin</b><br/><code>auditor@aicte-demo.gov.in</code>", cell_style),
            Paragraph("<code>http://localhost:5173/admin/audit</code><br/>Regulatory Audit Trail", cell_style),
            Paragraph("Step 8: Immutable audit ledger, actor/timestamp/target, SHA-256 document fingerprints, Correlation IDs.", cell_style)
        ]
    ]
    t_tabs = Table(tabs_pre_data, colWidths=[38, 145, 140, 200])
    t_tabs.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), C_NAVY),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 2.5), ('BOTTOMPADDING', (0,0), (-1,-1), 2.5),
        ('LEFTPADDING', (0,0), (-1,-1), 4), ('RIGHTPADDING', (0,0), (-1,-1), 4),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
    ]))
    story.append(t_tabs)
    story.append(Spacer(1, 6))

    # ==========================================
    # SECTION 1: CORE FEATURES TO DEMONSTRATE
    # ==========================================
    story.append(Paragraph("<b>1. CORE FEATURES TO DEMONSTRATE (INNOVATION &amp; LIMITATION SUMMARY)</b>", h1_style))
    
    features_data = [
        [
            Paragraph("<b>#</b>", cell_header),
            Paragraph("<b>Core Feature &amp; Statutory Scope</b>", cell_header),
            Paragraph("<b>Engineering Innovation</b>", cell_header),
            Paragraph("<b>Statutory Boundary / Limitation</b>", cell_header)
        ],
        [
            Paragraph("<b>1.1</b>", cell_bold),
            Paragraph("<b>Intelligent Application Lifecycle</b><br/>5 Categories: New Inst, EoA, Intake Inc, New Course, New Branch. Route-aware workflows.", cell_style),
            Paragraph("Structured, application-specific state machines instead of one generic form.", cell_style),
            Paragraph("Enforces strict permitted transitions; colleges cannot bypass procedural stages.", cell_style)
        ],
        [
            Paragraph("<b>1.2</b>", cell_bold),
            Paragraph("<b>OCR &amp; SAGE AI Document Review</b><br/>Extracts name, cert #, authority, dates, area. Shows confidence &amp; source page.", cell_style),
            Paragraph("Converts unstructured PDFs into structured reviewable data (Indra 45ms CPU inference).", cell_style),
            Paragraph("<b>Limitation:</b> OCR assists review; does not prove authenticity. Human verification required.", cell_warn)
        ],
        [
            Paragraph("<b>1.3</b>", cell_bold),
            Paragraph("<b>Document Version Control &amp; Integrity</b><br/>Preserves v1 upon replacement, links to corrective actions, SHA-256 hash.", cell_style),
            Paragraph("Traceable evidence history prevents silent replacement or tampering.", cell_style),
            Paragraph("Prior versions remain permanently archived and auditable.", cell_style)
        ],
        [
            Paragraph("<b>1.4</b>", cell_bold),
            Paragraph("<b>Automated Compliance Monitoring</b><br/>Expiry countdowns, severity tiers (INFO/WARN/CRITICAL), scheduled midnight sweeps.", cell_style),
            Paragraph("Proactive background scanning eliminates manual reminder dependency.", cell_style),
            Paragraph("<b>Limitation:</b> External SMS/email depends on gateway; in-app alerts are authoritative.", cell_warn)
        ],
        [
            Paragraph("<b>1.5</b>", cell_bold),
            Paragraph("<b>EVC Scheduling via Google OR-Tools</b><br/>CP-SAT discrete constraint solver, 3-expert panel, availability, conflict checks.", cell_style),
            Paragraph("Constraint satisfaction solves multi-expert NP-hard scheduling in &lt;400ms.", cell_style),
            Paragraph("<b>Limitation:</b> Feasible assignment under rules; does not authorize physical visit.", cell_warn)
        ],
        [
            Paragraph("<b>1.6</b>", cell_bold),
            Paragraph("<b>Deficiency &amp; Re-Scrutiny Loop</b><br/>Officer records deficiency with evidence; college resubmits; officer re-scrutinizes.", cell_style),
            Paragraph("Closed-loop traceable corrective lifecycle replaces disconnected emails.", cell_style),
            Paragraph("Strict institutional tenancy; college only sees own deficient items.", cell_style)
        ],
        [
            Paragraph("<b>1.7</b>", cell_bold),
            Paragraph("<b>APH-Aligned Scrutiny Assistance</b><br/>Rule checks: land norms (1.4), fire safety (2.1), faculty ratios (3.2), labs.", cell_style),
            Paragraph("Deterministic statutory mapping guarantees zero subjective bias.", cell_style),
            Paragraph("<b>Limitation:</b> Decision support; does not replace statutory interpretation.", cell_warn)
        ],
        [
            Paragraph("<b>1.8</b>", cell_bold),
            Paragraph("<b>RBAC &amp; Role-Specific Portals</b><br/>6 roles: Central Admin, Regional, Institution, Compliance, EVC, Auditor.", cell_style),
            Paragraph("Server-side Spring Boot security enforcement with tenant data isolation.", cell_style),
            Paragraph("Consequential actions (approve/deficiency) blocked for unprivileged roles.", cell_style)
        ],
        [
            Paragraph("<b>1.9</b>", cell_bold),
            Paragraph("<b>Audit Trail &amp; Accountability</b><br/>Audit Outbox pattern: actor, UTC timestamp, target, action, Correlation ID.", cell_style),
            Paragraph("Guarantees atomicity: business transaction rolls back if audit fails.", cell_style),
            Paragraph("Immutable, tamper-evident ledger ensures anti-corruption transparency.", cell_style)
        ],
        [
            Paragraph("<b>1.10</b>", cell_bold),
            Paragraph("<b>Alerts &amp; Central Document Registry</b><br/>Severity ranking, acknowledgement tracking, central metadata search.", cell_style),
            Paragraph("Single pane of glass for all institutional evidence with renewal tracking.", cell_style),
            Paragraph("External delivery simulated in demo environment.", cell_warn)
        ]
    ]
    t_feat = Table(features_data, colWidths=[20, 180, 160, 163])
    t_feat.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), C_NAVY),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 2), ('BOTTOMPADDING', (0,0), (-1,-1), 2),
        ('LEFTPADDING', (0,0), (-1,-1), 3), ('RIGHTPADDING', (0,0), (-1,-1), 3),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
    ]))
    story.append(t_feat)

    story.append(PageBreak())

    # ==========================================
    # HELPER FOR STEP BUILDING
    # ==========================================
    def build_step_block(step_num, title, duration, role, active_tab, target_url, inpage_tabs, screen_actions, speaker_dialogue, jury_takeaway):
        elements = []
        
        # Step Header
        header_data = [
            [
                Paragraph(f"<b>STEP {step_num}: {title.upper()}</b>", step_header_title),
                Paragraph(f"<b>DURATION:</b> {duration} &bull; <b>ROLE:</b> {role}", step_header_meta)
            ],
            [
                Paragraph(f"<b>ACTIVE TAB:</b> {active_tab} &bull; <code>{target_url}</code>", ParagraphStyle('SubUrl', parent=step_header_meta, fontName='Helvetica', fontSize=7, textColor=colors.HexColor("#e2e8f0"))),
                Paragraph("<b>REGULATORY SCOPE:</b> AICTE APH 2024–27", ParagraphStyle('SubRight', parent=step_header_meta, fontName='Helvetica', fontSize=7, alignment=2, textColor=colors.HexColor("#e2e8f0")))
            ]
        ]
        h_table = Table(header_data, colWidths=[335, 188])
        h_table.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), C_NAVY),
            ('TOPPADDING', (0,0), (-1,-1), 2.5), ('BOTTOMPADDING', (0,0), (-1,-1), 2.5),
            ('LEFTPADDING', (0,0), (-1,-1), 6), ('RIGHTPADDING', (0,0), (-1,-1), 6),
            ('LINEBELOW', (0,0), (-1,0), 0.5, colors.HexColor("#1e293b")),
        ]))
        elements.append(h_table)
        elements.append(Spacer(1, 3))

        # Cues & Tabs to be present
        cue_content = [
            Paragraph(f"<b>[TAB TO BE PRESENT &amp; SCREEN DIRECTIONS]</b>", cue_title_style),
            Paragraph(f"<b>ON-SCREEN ACTION:</b> {screen_actions}", cue_body_style),
            Spacer(1, 1),
            Paragraph(f"<b>IN-PAGE UI TABS &amp; CONTROLS:</b> {inpage_tabs}", tab_badge_style)
        ]
        c_table = Table([[cue_content]], colWidths=[523])
        c_table.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#eff6ff")),
            ('BOX', (0,0), (-1,-1), 0.75, colors.HexColor("#3b82f6")),
            ('TOPPADDING', (0,0), (-1,-1), 3), ('BOTTOMPADDING', (0,0), (-1,-1), 3),
            ('LEFTPADDING', (0,0), (-1,-1), 6), ('RIGHTPADDING', (0,0), (-1,-1), 6),
        ]))
        elements.append(c_table)
        elements.append(Spacer(1, 3))

        # Dialogue
        diag_content = [Paragraph(f"<b>PRESENTER PITCH (INFORMATIVE &amp; SIMPLE LANGUAGE):</b>", speaker_title_style)]
        for p in speaker_dialogue:
            diag_content.append(Paragraph(f'"{p}"', dialogue_style))
        d_table = Table([[diag_content]], colWidths=[523])
        d_table.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#fefce8")),
            ('LINEBEFORE', (0,0), (0,-1), 2.5, C_SAFFRON),
            ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#fde047")),
            ('TOPPADDING', (0,0), (-1,-1), 4), ('BOTTOMPADDING', (0,0), (-1,-1), 4),
            ('LEFTPADDING', (0,0), (-1,-1), 8), ('RIGHTPADDING', (0,0), (-1,-1), 6),
        ]))
        elements.append(d_table)
        elements.append(Spacer(1, 3))

        # Takeaway
        t_content = [
            Paragraph(f"<b>KEY GOVERNANCE &amp; EVALUATION TAKEAWAY:</b>", takeaway_title),
            Paragraph(f"&bull; {jury_takeaway}", takeaway_body)
        ]
        tk_table = Table([[t_content]], colWidths=[523])
        tk_table.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#f0fdf4")),
            ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#86efac")),
            ('TOPPADDING', (0,0), (-1,-1), 2.5), ('BOTTOMPADDING', (0,0), (-1,-1), 2.5),
            ('LEFTPADDING', (0,0), (-1,-1), 6), ('RIGHTPADDING', (0,0), (-1,-1), 6),
        ]))
        elements.append(tk_table)
        elements.append(Spacer(1, 6))

        return KeepTogether(elements)

    # ==========================================
    # SECTION 2: LIVE DEMONSTRATION SEQUENCE (STEPS 1 TO 9)
    # ==========================================
    story.append(Paragraph("<b>2. RECOMMENDED LIVE DEMONSTRATION SEQUENCE (9–11 MINUTES)</b>", h1_style))
    story.append(Paragraph("A single, connected institutional approval journey demonstrating innovation, automation, traceability, and governance in simple, clear language.", cell_style))
    story.append(Spacer(1, 4))

    # STEP 1
    step1 = build_step_block(
        step_num=1,
        title="Introduction & The Problem",
        duration="45 seconds (~0:00 – 0:45)",
        role="Presenter / SAGE Landing",
        active_tab="BROWSER TAB 1",
        target_url="http://localhost:5173/login",
        inpage_tabs="Top Bar: WCAG 2.2 AA Accessibility (A-, Normal, A+, High Contrast) &bull; Synthetic Demo Accounts Panel (6 Roles Grid).",
        screen_actions="Camera starts on presenter, then cuts to Tab 1. Click font scaling 'A+' then 'A-', toggle 'High Contrast'. Pan mouse over 2-column synthetic role accounts.",
        speaker_dialogue=[
            "Hello and welcome, esteemed jury members. Every year, over ten thousand engineering and management colleges across India apply to AICTE for approvals, renewals, and new courses.",
            "Historically, this meant moving mountains of paper files between colleges and regional offices. Verification took months, certificates were lost or expired unnoticed, and scheduling inspection teams required hundreds of manual phone calls.",
            "To solve this, we built SAGE — the System for Approval Governance and Evaluation. SAGE connects the entire journey into one simple, transparent digital platform: from fast document scanning and rule checks, to automatic compliance monitoring, smart inspection scheduling, and a tamper-proof audit trail. Let’s walk through the live system together."
        ],
        jury_takeaway="Establishes national regulatory scale (10,000+ colleges) in plain English and introduces the unified platform without claiming unrealistic full AI autonomy."
    )
    story.append(step1)

    # STEP 2
    step2 = build_step_block(
        step_num=2,
        title="Institution Submits an Application",
        duration="60 seconds (~0:45 – 1:45)",
        role="Institution Officer",
        active_tab="BROWSER TAB 2",
        target_url="http://localhost:5173/dashboard -> /applications",
        inpage_tabs="Dashboard Banner (PID: 1-500123498) &bull; 'New Approval Application' Button &bull; Modal: Category Selection &bull; Sub-Tabs: [Course Proposal] | [Compliance Documents & OCR] | [Workflow History].",
        screen_actions="Switch to Tab 2 (officer@delhi-tech.edu). Show Delhi Tech dashboard (PID 1-500123498). Click 'New Approval Application' -> select 'New Course' category. Enter institutional curriculum & lab details. Upload Fire Safety Certificate PDF. Click 'Submit Application'.",
        speaker_dialogue=[
            "We start on Tab 2, logged in as the officer for Delhi Institute of Advanced Engineering. Notice our dashboard: every college can only see their own records, so institutional data stays completely private and secure.",
            "When I click 'New Approval Application', SAGE doesn't give us one messy, generic form. Instead, it offers five clear pathways tailored to what the college actually needs: like adding a new branch, increasing student seats, or renewing an approval.",
            "Today, we’ll select 'New Course Introduction' for an Artificial Intelligence program. We fill in the curriculum requirements, attach our institutional certificates, and click Submit. The application instantly moves to 'Submitted' status and enters the official regulatory queue."
        ],
        jury_takeaway="Category-specific workflows replace confusing one-size-fits-all forms; college data is strictly isolated on the backend."
    )
    story.append(step2)

    story.append(PageBreak())

    # STEP 3
    step3 = build_step_block(
        step_num=3,
        title="OCR & AI-Assisted Document Review",
        duration="90 seconds (~1:45 – 3:15)",
        role="Institution Officer / Officer Review",
        active_tab="BROWSER TAB 2 (or Document Registry)",
        target_url="http://localhost:5173/applications/:id",
        inpage_tabs="Sub-Tab: [Compliance Documents & OCR (ACTIVE)] &bull; Extracted Fields Panel &bull; Confidence Scores &bull; Source-Page Context &bull; Modal: 'SAGE AI Findings (Gemini Flash)'.",
        screen_actions="In application details, open 'Compliance Documents & OCR' tab. Open the uploaded Fire Safety Certificate. Show extracted fields (Institution Name, Cert #, Issuing Authority, Expiry Date). Point out confidence values (98%), source-page context, and click the purple 'SAGE AI Findings' button to inspect advisory extraction.",
        speaker_dialogue=[
            "Now let's see how SAGE handles documents. In the past, officers had to read hundreds of PDF pages manually to find certificate numbers and dates.",
            "When Delhi Tech uploads their Fire Safety Certificate, our built-in Indra OCR engine reads the entire document on the computer in just 45 milliseconds — over ten times faster than older tools.",
            "Look at the extracted details on screen: SAGE has pulled out the college name, certificate number, issuing fire station, issue date, and expiration date, along with a 98% confidence score. Clicking 'SAGE AI Findings' opens our AI assistant report, which highlights exactly where on the page each piece of information came from.",
            "Most importantly: SAGE uses AI to save time and organize evidence, but it never rubber-stamps approvals. The human officer always does the final verification."
        ],
        jury_takeaway="Explains CPU OCR (45ms) and Google Gemini AI in simple terms; explicitly clarifies that AI assists while humans retain statutory decision authority."
    )
    story.append(step3)

    # STEP 4
    step4 = build_step_block(
        step_num=4,
        title="Regulatory Scrutiny & Deficiency Identification",
        duration="90 seconds (~3:15 – 4:45)",
        role="Regional Officer",
        active_tab="BROWSER TAB 3",
        target_url="http://localhost:5173/applications/APP-2025-DEL-0103",
        inpage_tabs="Scrutiny Queue &bull; Sub-Tab: [Automated Scrutiny Findings (ACTIVE)] &bull; Statutory Clauses (1.4 Land, 2.1 Fire Safety, 3.2 Cadre) &bull; 'Evaluate Scrutiny Rules' Button &bull; 'Advance Workflow' Dropdown.",
        screen_actions="Switch to Tab 3 (regional.north@aicte-demo.gov.in). Select prepared deficient application APP-2025-DEL-0103. Show the upload form is hidden (RBAC). Click 'Evaluate Scrutiny Rules'. Show expired Fire Safety Certificate flagged under Clause 2.1. Record deficiency reason with evidence. Advance workflow to DEFICIENT.",
        speaker_dialogue=[
            "Next, we switch to Tab 3 as the AICTE Northern Regional Officer. Notice that the document upload button is gone. In SAGE, colleges upload evidence, while officers evaluate it.",
            "We open Delhi Tech’s application and click 'Evaluate Scrutiny Rules'. In less than a second, SAGE checks the application against AICTE handbook requirements: minimum 2.5 acres of land, a 1-to-20 faculty ratio, and an active fire safety certificate.",
            "Look at the screen: Clause 2.1 is flagged in red as 'Deficient' because the Fire Safety Certificate expired on March 15th, 2025. SAGE caught the error instantly. The officer writes a clear explanation, attaches the finding, and returns the application to the college for correction. The software assists, but the human officer makes the decision."
        ],
        jury_takeaway="Clearly shows how SAGE checks handbook rules instantly; demonstrates role security (regulators cannot upload) and human decision execution."
    )
    story.append(step4)

    story.append(PageBreak())

    # STEP 5
    step5 = build_step_block(
        step_num=5,
        title="Document Correction & Version Control",
        duration="75 seconds (~4:45 – 6:00)",
        role="Institution Officer, then Regional Officer",
        active_tab="BROWSER TAB 2, then TAB 3",
        target_url="/applications/APP-2025-DEL-0103",
        inpage_tabs="Institution Desk: 'Resubmit Rectified Application' &bull; Document Version History Drawer (v1 vs v2) &bull; Regional Desk: Scrutiny Findings Re-evaluation &bull; State: RESUBMITTED.",
        screen_actions="In Tab 2 (Institution), open returned application. Show DEFICIENT status badge. Click 'Replace Document' on Fire Safety -> upload renewed certificate (v2). Open Version History drawer to show v1 preserved and v2 active. Click 'Resubmit'. Switch to Tab 3 (Regional Officer): refresh, click 'Evaluate Scrutiny Rules', show Clause 2.1 now COMPLIANT.",
        speaker_dialogue=[
            "Now we go back to Tab 2 as Delhi Tech. The college dashboard immediately alerts us that our application was returned with a deficiency.",
            "Instead of starting the entire application over or exchanging confusing emails, the college clicks 'Replace Document' on the fire certificate and uploads their renewed certificate valid through 2028.",
            "Notice our Version History drawer: Version 1 is permanently archived with its original upload date and digital fingerprint. Version 2 is now active. No document is ever silently overwritten or hidden.",
            "The college resubmits. In Tab 3, the Regional Officer clicks 'Evaluate Scrutiny Rules' again, and Clause 2.1 turns green into 'Compliant'. The entire correction loop is closed cleanly and transparently in minutes."
        ],
        jury_takeaway="Shows how version control prevents tampering, keeps an auditable record of corrections, and eliminates weeks of email back-and-forth."
    )
    story.append(step5)

    # STEP 6
    step6 = build_step_block(
        step_num=6,
        title="Automated Compliance Monitoring",
        duration="75 seconds (~6:00 – 7:15)",
        role="Compliance Officer",
        active_tab="BROWSER TAB 4",
        target_url="http://localhost:5173/compliance",
        inpage_tabs="Horizontal Tabs: [Statutory Deadlines & Expiries (ACTIVE)] | [Compliance Findings & Risk Analysis] | [Surveillance Runs & History] &bull; Button: 'Trigger Compliance Sweep'.",
        screen_actions="Switch to Tab 4 (compliance.officer1@aicte-demo.gov.in). Click through the 3 tabs. Point out expiry countdowns (e.g. 14 days remaining), grace period handling, and severity rankings (INFO, WARNING, CRITICAL). Click 'Trigger Compliance Sweep' to show background job execution. Open In-App Alerts drawer.",
        speaker_dialogue=[
            "Regulatory governance doesn't stop after an approval is granted. Certificates expire, and faculty ratios change during the year. In Tab 4, we are logged in as the AICTE Compliance Officer.",
            "SAGE runs automated background scans every midnight across all active colleges. In the Deadlines tab, countdown timers show certificates expiring soon. A document with 30 days left is marked as an orange 'Warning', while an expired document immediately escalates to a red 'Critical' alert.",
            "When I click 'Trigger Compliance Sweep', SAGE scans all institutions in seconds and sends in-app alerts so officers and colleges can take action weeks before any deadline is missed."
        ],
        jury_takeaway="Explains ongoing compliance in plain terms: midnight automatic sweeps, 30-day countdowns, and warning tiers prevent safety oversights."
    )
    story.append(step6)

    story.append(PageBreak())

    # STEP 7
    step7 = build_step_block(
        step_num=7,
        title="EVC Inspection Scheduling Using CP-SAT",
        duration="90 seconds (~7:15 – 8:45)",
        role="EVC Coordinator",
        active_tab="BROWSER TAB 5",
        target_url="http://localhost:5173/evc",
        inpage_tabs="Horizontal Tabs: [Pending Inspections (ACTIVE)] | [Confirmed Inspection Schedules] | [Accredited Experts Registry] &bull; Button: 'Run Statutory Allocation Engine'.",
        screen_actions="Switch to Tab 5 (evc.coord1@aicte-demo.gov.in). Show Accredited Experts Registry (disciplines, home states). Return to Pending Inspections. Explain the 3-expert panel rule, 2 consecutive days, Sunday barred, and zero home-state conflict. Click 'Run Statutory Allocation Engine'. Show feasible assignment roster in <400ms.",
        speaker_dialogue=[
            "When a college applies for a new course, an Expert Visit Committee must inspect the campus. Historically, assigning this panel was a logistical nightmare: you need three professors from matching disciplines, they must be free on two consecutive working days excluding Sundays, and they must have zero conflict of interest — meaning they cannot inspect a college in their home state or where they studied.",
            "Coordinating this manually on spreadsheets took weeks and often led to mistakes. In Tab 5, we integrated Google’s OR-Tools CP-SAT solver. When I click 'Run Statutory Allocation Engine', the solver tests all availability and conflict rules, and produces a fair, conflict-free panel of three accredited professors in less than 400 milliseconds!",
            "It turns weeks of manual phone calls into instant mathematical certainty, while the council retains final approval."
        ],
        jury_takeaway="Demystifies CP-SAT optimization: explains the 5 real-world rules in simple English and shows how it saves weeks of manual coordination."
    )
    story.append(step7)

    # STEP 8
    step8 = build_step_block(
        step_num=8,
        title="Audit Trail & Role-Based Access Control",
        duration="60 seconds (~8:45 – 9:45)",
        role="Regulatory Auditor / Central Admin",
        active_tab="BROWSER TAB 6",
        target_url="http://localhost:5173/admin/audit",
        inpage_tabs="Filter Controls: Principal, Event Type, Resource ID, Correlation ID Search &bull; Immutable Ledger Table (Actor, Timestamp, Action, Target, IP).",
        screen_actions="Switch to Tab 6 (auditor@aicte-demo.gov.in). Show chronological log. Filter by Principal 'officer@delhi-tech.edu'. Show document upload event with SHA-256 hash. Filter by 'regional.north' to show deficiency notice. Demonstrate Correlation ID search tracing requests end-to-end.",
        speaker_dialogue=[
            "Finally, let’s look at accountability. In Tab 6, we are logged in as the Independent Regulatory Auditor. Auditors have read-only access — they cannot change marks or edit files.",
            "Every single action in SAGE — who logged in, who uploaded a file, who flagged a deficiency, and who scheduled an inspection — is recorded in a tamper-proof digital logbook.",
            "If an audit entry fails to write, the business action is cancelled immediately. Each entry records the exact time, user email, IP address, digital file fingerprint, and a tracking ID. This ensures complete honesty, prevents tampering, and gives leadership full confidence in the system."
        ],
        jury_takeaway="Explains the audit ledger as a tamper-proof digital logbook that prevents secret modifications and ensures absolute institutional integrity."
    )
    story.append(step8)

    # STEP 9
    step9 = build_step_block(
        step_num=9,
        title="Conclusion & Governance Impact",
        duration="30 seconds (~9:45 – 10:15)",
        role="Presenter / Camera Direct",
        active_tab="ALL TABS (Return to Presenter Full Frame)",
        target_url="N/A (Speaker Direct to Camera)",
        inpage_tabs="Connected Lifecycle Recap: Application Submission &rarr; Document Intelligence &rarr; Regulatory Scrutiny &rarr; Deficiency Identification &rarr; Corrective Resubmission &rarr; Compliance Monitoring &rarr; CP-SAT Scheduling &rarr; Auditable Governance.",
        screen_actions="Cut smoothly from Tab 6 back to Full Screen Camera. Presenter speaks with calm, authoritative confidence, summarizing the complete connected workflow in one closing statement.",
        speaker_dialogue=[
            "To conclude: SAGE transforms the 200-page AICTE handbook into an easy-to-use digital platform.",
            "It takes care of the repetitive, time-consuming tasks — like reading certificates, calculating ratios, tracking expirations, and scheduling committees — while keeping all final statutory decisions firmly in the hands of authorized human officers.",
            "SAGE brings speed, clarity, and fairness to colleges, and complete transparency to regulators. Thank you, and we welcome your questions!"
        ],
        jury_takeaway="A memorable, simple closing statement that leaves evaluators with full clarity on SAGE's value and human-centered design."
    )
    story.append(step9)

    story.append(PageBreak())

    # ==========================================
    # SECTION 3: THREE HIGHEST-IMPACT INNOVATIONS
    # ==========================================
    story.append(Paragraph("<b>3. THREE HIGHEST-IMPACT INNOVATION DEMONSTRATIONS</b>", h1_style))
    story.append(Paragraph("Focus evaluators on these three architectural breakthroughs during the live walkthrough:", cell_style))
    story.append(Spacer(1, 4))

    innovations_data = [
        [
            Paragraph("<b>Innovation Pillar</b>", cell_header),
            Paragraph("<b>Demonstration Flow &amp; Concrete UI Proof</b>", cell_header),
            Paragraph("<b>Key Evaluator Message (In Plain English)</b>", cell_header)
        ],
        [
            Paragraph("<b>A. OCR + AI DOCUMENT INTELLIGENCE</b>", cell_bold),
            Paragraph(
                "1. Upload institutional certificate (PDF).<br/>"
                "2. Extract structured fields (name, cert #, dates, area).<br/>"
                "3. Display extraction confidence (98%) and source-page context.<br/>"
                "4. Identify potential mismatch / expiry flag.<br/>"
                "5. Demonstrate officer review and verification.",
                cell_style
            ),
            Paragraph(
                "<i>\"SAGE does not merely store documents. It converts institutional evidence into structured, reviewable information, helping officers identify potential discrepancies faster.\"</i>",
                cell_style
            )
        ],
        [
            Paragraph("<b>B. DEFICIENCY LOOP WITH VERSION CONTROL</b>", cell_bold),
            Paragraph(
                "1. Original document submitted (v1 archived).<br/>"
                "2. Deficiency identified under statutory clause (e.g. 2.1).<br/>"
                "3. Application returned to institution.<br/>"
                "4. Corrected document uploaded (v2 activated with new hash).<br/>"
                "5. Previous v1 retained permanently in audit drawer.<br/>"
                "6. Application re-scrutinized and cleared.",
                cell_style
            ),
            Paragraph(
                "<i>\"Every correction remains traceable. The institution can respond to a specific deficiency, while the reviewing authority can see the complete evidence history.\"</i>",
                cell_style
            )
        ],
        [
            Paragraph("<b>C. CP-SAT INSPECTION SCHEDULING</b>", cell_bold),
            Paragraph(
                "1. Show expert registry with disciplines and home states.<br/>"
                "2. Show availability and date calendars.<br/>"
                "3. Enforce conflict-of-interest rules (no home state).<br/>"
                "4. Enforce 3-expert panel, 2 consecutive days, no Sundays.<br/>"
                "5. Execute CP-SAT solver and show feasible assignment.",
                cell_style
            ),
            Paragraph(
                "<i>\"Instead of manually coordinating inspection panels, SAGE models eligibility, availability, and conflicts as constraints and uses CP-SAT to find feasible assignments.\"</i>",
                cell_style
            )
        ]
    ]
    t_innov = Table(innovations_data, colWidths=[120, 210, 193])
    t_innov.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#0f766e")),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 3), ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 4), ('RIGHTPADDING', (0,0), (-1,-1), 4),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f0fdfa")]),
    ]))
    story.append(t_innov)
    story.append(Spacer(1, 8))

    # ==========================================
    # SECTION 4: DEMO RECORDS & ACCOUNTS CHEATSHEET
    # ==========================================
    story.append(Paragraph("<b>4. DEMO RECORDS, ACCOUNTS &amp; SEED ENVIRONMENT CHEATSHEET</b>", h1_style))
    story.append(Paragraph("All demo accounts share password: <code>Password123!</code> (per DEMO_EXECUTION_GUIDE.md).", cell_style))
    story.append(Spacer(1, 3))

    accounts_data = [
        [
            Paragraph("<b>Role Persona</b>", cell_header),
            Paragraph("<b>Email / Username</b>", cell_header),
            Paragraph("<b>Scope / Jurisdiction</b>", cell_header),
            Paragraph("<b>Demo Focus &amp; Seeded Target Records</b>", cell_header)
        ],
        [
            Paragraph("<b>Institution Officer</b>", cell_bold),
            Paragraph("<code>officer@delhi-tech.edu</code>", cell_style),
            Paragraph("Delhi Tech (PID: 1-500123498)", cell_style),
            Paragraph("App: <code>APP-2025-DEL-0103</code> &bull; Corrective Action: <code>CA-2025-001</code> &bull; Expired Fire Safety NOC", cell_style)
        ],
        [
            Paragraph("<b>Regional Officer</b>", cell_bold),
            Paragraph("<code>regional.north@aicte-demo.gov.in</code>", cell_style),
            Paragraph("Northern Region (DL, UP, HR)", cell_style),
            Paragraph("Scrutiny queue, clause 1.4/2.1/3.2 evaluation, deficiency notice issuance", cell_style)
        ],
        [
            Paragraph("<b>EVC Coordinator</b>", cell_bold),
            Paragraph("<code>evc.coord1@aicte-demo.gov.in</code>", cell_style),
            Paragraph("EVC Operations Hub", cell_style),
            Paragraph("Expert matching, CP-SAT solver, 3-expert conflict-free committee allocation", cell_style)
        ],
        [
            Paragraph("<b>Compliance Officer</b>", cell_bold),
            Paragraph("<code>compliance.officer1@aicte-demo.gov.in</code>", cell_style),
            Paragraph("National Safety Surveillance", cell_style),
            Paragraph("Surveillance sweeps, expiry countdowns, severity grading, in-app alerts", cell_style)
        ],
        [
            Paragraph("<b>Regulatory Auditor</b>", cell_bold),
            Paragraph("<code>auditor@aicte-demo.gov.in</code>", cell_style),
            Paragraph("Statutory Oversight", cell_style),
            Paragraph("Immutable audit log, SHA-256 verification, Correlation ID search", cell_style)
        ],
        [
            Paragraph("<b>Central Admin</b>", cell_bold),
            Paragraph("<code>admin@aicte-demo.gov.in</code>", cell_style),
            Paragraph("Nationwide (All Tenancies)", cell_style),
            Paragraph("Global user management, policy settings, system health diagnostics", cell_style)
        ]
    ]
    t_acc = Table(accounts_data, colWidths=[100, 145, 115, 163])
    t_acc.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), C_NAVY),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 2.5), ('BOTTOMPADDING', (0,0), (-1,-1), 2.5),
        ('LEFTPADDING', (0,0), (-1,-1), 4), ('RIGHTPADDING', (0,0), (-1,-1), 4),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
    ]))
    story.append(t_acc)
    story.append(Spacer(1, 4))

    # Seed Environment Metadata Banner
    seed_meta = [
        [
            Paragraph("<b>Seeded Colleges:</b> 8 Institutions", cell_style),
            Paragraph("<b>Seeded Users:</b> 19 Role Personas", cell_style),
            Paragraph("<b>Seeded Applications:</b> 36 Lifecycle Records", cell_style),
            Paragraph("<b>Sample Certificates:</b> 72 Physical PDFs", cell_style)
        ]
    ]
    t_seed = Table(seed_meta, colWidths=[130, 131, 131, 131])
    t_seed.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#f1f5f9")),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 2), ('BOTTOMPADDING', (0,0), (-1,-1), 2),
        ('LEFTPADDING', (0,0), (-1,-1), 4), ('RIGHTPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(t_seed)

    story.append(PageBreak())

    # ==========================================
    # SECTIONS 5 & 6: TIME-PERMITTING & WHAT NOT TO SHOW
    # ==========================================
    story.append(Paragraph("<b>5. FEATURES TO SHOW IF TIME PERMITS &amp; 6. WHAT NOT TO SPEND TIME ON</b>", h1_style))

    guide_cols = [
        [
            Paragraph("<b>SHOW IF TIME PERMITS (BONUS VALUE)</b>", ParagraphStyle('BGreen', parent=cell_header, textColor=colors.HexColor("#14532d"))),
            Paragraph("<b>DO NOT SPEND DEMONSTRATION TIME ON</b>", ParagraphStyle('BRed', parent=cell_header, textColor=colors.HexColor("#7f1d1d")))
        ],
        [
            Paragraph(
                "&bull; 5 application categories &amp; route-specific workflows<br/>"
                "&bull; Full APH-aligned scrutiny checklist (Clauses 1.4, 2.1, 3.2, 4.1)<br/>"
                "&bull; Centralized Document Registry metadata search<br/>"
                "&bull; SHA-256 cryptographic document integrity hashing<br/>"
                "&bull; Document renewal and expiry tracking<br/>"
                "&bull; Compliance severity &amp; grace-period handling<br/>"
                "&bull; Undertaking follow-up &amp; alert acknowledgement<br/>"
                "&bull; Role-specific dashboard layouts &amp; tenancy isolation<br/>"
                "&bull; EVC infeasibility reporting when no panel satisfies rules",
                cell_style
            ),
            Paragraph(
                "&bull; <b>Avoid:</b> Spending excessive time on login and registration.<br/>"
                "&bull; <b>Avoid:</b> Demonstrating basic CRUD without explaining governance value.<br/>"
                "&bull; <b>Avoid:</b> Showing generic dashboard numbers without clear insight.<br/>"
                "&bull; <b>Avoid:</b> Changing profile or personal account details.<br/>"
                "&bull; <b>Avoid:</b> Explaining every single form field line-by-line.<br/>"
                "&bull; <b>Avoid:</b> Repeatedly listing programming languages &amp; frameworks.<br/>"
                "&bull; <b>Avoid:</b> Showing raw SQL or database tables unless requested.<br/>"
                "&bull; <b>Focus Strictly On:</b> The input &rarr; automated processing &rarr; generated result &rarr; officer's next action &rarr; audit trail.",
                cell_style
            )
        ]
    ]
    t_guide = Table(guide_cols, colWidths=[261, 262])
    t_guide.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), colors.HexColor("#dcfce7")),
        ('BACKGROUND', (1,0), (1,0), colors.HexColor("#fee2e2")),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 3), ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 5), ('RIGHTPADDING', (0,0), (-1,-1), 5),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white]),
    ]))
    story.append(t_guide)
    story.append(Spacer(1, 6))

    # ==========================================
    # SECTION 7: CLAIMS AND LIMITATIONS (ACCURATE LANGUAGE)
    # ==========================================
    story.append(Paragraph("<b>7. CLAIMS AND LIMITATIONS — USE ACCURATE LANGUAGE</b>", h1_style))
    story.append(Paragraph("Maintain strict statutory accuracy. Human officers retain final statutory decision authority.", cell_style))
    story.append(Spacer(1, 3))

    claims_data = [
        [
            Paragraph("<b>Prohibited Claims (DO NOT CLAIM)</b>", cell_header),
            Paragraph("<b>Approved SAGE Phrasing (USE INSTEAD)</b>", cell_header)
        ],
        [
            Paragraph("✖ Fully automated AICTE approvals", cell_warn),
            Paragraph("✔ <b>AI-assisted scrutiny with human decision authority</b>", cell_style)
        ],
        [
            Paragraph("✖ Automatic document authentication", cell_warn),
            Paragraph("✔ <b>OCR-based extraction &amp; discrepancy assistance (officer verifies)</b>", cell_style)
        ],
        [
            Paragraph("✖ Fully autonomous campus inspections", cell_warn),
            Paragraph("✔ <b>Constraint-based EVC scheduling assistance using CP-SAT</b>", cell_style)
        ],
        [
            Paragraph("✖ Live AICTE production systems integration", cell_warn),
            Paragraph("✔ <b>Prototype workflow engineered for AICTE-aligned processes</b>", cell_style)
        ],
        [
            Paragraph("✖ Guaranteed globally optimal scheduling", cell_warn),
            Paragraph("✔ <b>Mathematically feasible scheduling under configured constraints</b>", cell_style)
        ],
        [
            Paragraph("✖ Guaranteed live external SMS / email delivery", cell_warn),
            Paragraph("✔ <b>In-app alerts are live; external dispatch depends on gateway</b>", cell_style)
        ]
    ]
    t_claims = Table(claims_data, colWidths=[250, 273])
    t_claims.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#1e293b")),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 2), ('BOTTOMPADDING', (0,0), (-1,-1), 2),
        ('LEFTPADDING', (0,0), (-1,-1), 4), ('RIGHTPADDING', (0,0), (-1,-1), 4),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
    ]))
    story.append(t_claims)

    story.append(PageBreak())

    # ==========================================
    # SECTION 8: PRE-DEMO READINESS CHECKLIST
    # ==========================================
    story.append(Paragraph("<b>8. COMPLETE PRE-DEMO READINESS CHECKLIST</b>", h1_style))
    story.append(Paragraph("Verify all 18 operational items before starting the evaluation or video recording:", cell_style))
    story.append(Spacer(1, 3))

    checklist_items = [
        ["[  ]", "All six role accounts can log in successfully with password <code>Password123!</code>."],
        ["[  ]", "Institution Officer can create and submit a New Course application."],
        ["[  ]", "OCR extraction works on the selected sample certificate PDF (Delhi Fire Safety NOC)."],
        ["[  ]", "Confidence values (e.g. 98%) and source-page context are clearly visible in the UI."],
        ["[  ]", "Regional Officer can record a deficiency with reason and statutory evidence."],
        ["[  ]", "Institution Officer can upload a corrected replacement document."],
        ["[  ]", "Previous document version (v1) remains accessible in Version History."],
        ["[  ]", "Re-scrutiny works cleanly after replacement document resubmission."],
        ["[  ]", "Compliance dashboard displays expiry findings with active countdown trackers."],
        ["[  ]", "Google OR-Tools CP-SAT produces a feasible schedule for the prepared EVC scenario."],
        ["[  ]", "Audit history records relevant actions (actor, UTC timestamp, target, IP, Correlation ID)."],
        ["[  ]", "Server-side role-based access restrictions prevent unauthorized uploads or edits."],
        ["[  ]", "Multi-tenant institutional data scoping blocks cross-tenant college access."],
        ["[  ]", "Application data survives a container/process restart."],
        ["[  ]", "Uploaded documents in <code>backend/data/documents</code> survive a restart."],
        ["[  ]", "Pre-seeded demo records (<code>APP-2025-DEL-0103</code>, <code>CA-2025-001</code>) verified."],
        ["[  ]", "All 6 browser tabs pre-staged and authenticated in separate windows/profiles."],
        ["[  ]", "Backup screenshots and demonstration script PDF available in case of network issues."]
    ]

    check_data = []
    for mark, text in checklist_items:
        check_data.append([
            Paragraph(f"<b>{mark}</b>", cell_bold),
            Paragraph(text, cell_style)
        ])
    t_chk = Table(check_data, colWidths=[30, 493])
    t_chk.setStyle(TableStyle([
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#e2e8f0")),
        ('TOPPADDING', (0,0), (-1,-1), 1.8), ('BOTTOMPADDING', (0,0), (-1,-1), 1.8),
        ('LEFTPADDING', (0,0), (-1,-1), 4), ('RIGHTPADDING', (0,0), (-1,-1), 4),
        ('ROWBACKGROUNDS', (0,0), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
    ]))
    story.append(t_chk)
    story.append(Spacer(1, 8))

    # ==========================================
    # SECTION 9: FINAL NARRATIVE & CLOSING
    # ==========================================
    story.append(Paragraph("<b>9. FINAL DEMONSTRATION NARRATIVE &amp; CENTRAL MESSAGE</b>", h1_style))
    
    narrative_box = [
        [
            Paragraph("<b>THE CONNECTED 9-STEP GOVERNANCE NARRATIVE:</b>", ParagraphStyle('NTitle', parent=cell_bold, fontSize=8, textColor=C_NAVY)),
        ],
        [
            Paragraph(
                "<b>1.</b> Problem &amp; SAGE Introduction &rarr; "
                "<b>2.</b> Institution Submits Application &rarr; "
                "<b>3.</b> OCR &amp; AI-Assisted Document Review &rarr; "
                "<b>4.</b> Regional Officer Scrutiny &amp; Deficiency Notice &rarr; "
                "<b>5.</b> Document Correction &amp; Version History Retention &rarr; "
                "<b>6.</b> Regional Re-Scrutiny &rarr; "
                "<b>7.</b> Automated Compliance Monitoring &amp; Expiry Sweeps &rarr; "
                "<b>8.</b> EVC Inspection Scheduling via CP-SAT &rarr; "
                "<b>9.</b> Immutable Audit Trail &amp; Role-Based Governance.",
                cell_style
            )
        ],
        [
            Paragraph("<b>CENTRAL EVALUATOR TAKEAWAY (SUGGESTED CLOSING QUOTE):</b>", ParagraphStyle('CTitle', parent=cell_bold, fontSize=8, textColor=C_SAFFRON)),
        ],
        [
            Paragraph(
                "<i>\"SAGE is not just an online application portal. It is an approval governance platform that combines structured workflows, document intelligence, compliance automation, constraint-based inspection scheduling, and auditable human decision-making.\"</i>",
                ParagraphStyle('QuoteText', parent=dialogue_style, fontName='Times-BoldItalic', fontSize=9.5, leading=13, textColor=C_NAVY)
            )
        ]
    ]
    t_nar = Table(narrative_box, colWidths=[523])
    t_nar.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#f8fafc")),
        ('BOX', (0,0), (-1,-1), 1, C_NAVY),
        ('LINEBELOW', (0,0), (-1,0), 0.5, colors.HexColor("#cbd5e1")),
        ('LINEBELOW', (0,2), (-1,2), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 4), ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 8), ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(t_nar)

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"PDF successfully built at: {output_path}")

if __name__ == "__main__":
    out_pdf = "/home/Harshit/Desktop/PROJECTS/SIH 2026/AICTE/SAGE/docs/SAGE_COMPLETE_DEMONSTRATION_MASTER_GUIDE.pdf"
    build_demo_guide_pdf(out_pdf)
    copy_pdf = "/home/Harshit/Desktop/PROJECTS/SIH 2026/AICTE/SAGE/docs/SAGE_VIDEO_PRESENTATION_SCRIPT.pdf"
    import shutil
    shutil.copy(out_pdf, copy_pdf)
    print(f"Synced copy at: {copy_pdf}")
