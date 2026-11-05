#!/usr/bin/env python3
"""
SAGE - Video Presentation Script PDF Generator
Generates a broadcast-ready, high-aesthetic executive PDF of the complete presentation script
with visual cues, director instructions, exact verbatim dialogue, pre-staged browser tabs,
and in-page UI tab navigation references.
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
            self.drawRightString(595 - 36, 842 - 25, "Official Video Presentation & Live Demo Master Script")
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


def build_pdf(output_path: str):
    doc = SimpleDocTemplate(
        output_path,
        pagesize=A4,
        leftMargin=36,
        rightMargin=36,
        topMargin=36,
        bottomMargin=36
    )

    styles = getSampleStyleSheet()

    # Custom Color Palette
    C_NAVY = colors.HexColor("#0a192f")
    C_SAFFRON = colors.HexColor("#ea580c")
    C_BLUE = colors.HexColor("#1d4ed8")
    C_GREEN = colors.HexColor("#15803d")
    C_DARK = colors.HexColor("#0f172a")
    C_SLATE = colors.HexColor("#334155")
    
    # Custom Typography Styles
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=18,
        leading=22,
        textColor=C_NAVY,
        spaceAfter=3
    )
    
    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=13,
        textColor=C_SAFFRON,
        spaceAfter=6
    )

    section_heading_style = ParagraphStyle(
        'SectionHeading',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=11,
        leading=14,
        textColor=C_NAVY,
        spaceAfter=4
    )

    act_title_style = ParagraphStyle(
        'ActTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=11,
        leading=14,
        textColor=colors.white
    )

    act_meta_style = ParagraphStyle(
        'ActMeta',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8,
        leading=10,
        textColor=colors.HexColor("#fef08a")
    )

    dialogue_style = ParagraphStyle(
        'DialogueText',
        parent=styles['Normal'],
        fontName='Times-Roman',
        fontSize=9,
        leading=12.5,
        textColor=C_DARK,
        spaceAfter=5
    )

    speaker_label_style = ParagraphStyle(
        'SpeakerLabel',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8,
        leading=10,
        textColor=C_SAFFRON,
        spaceAfter=3
    )

    cue_text_style = ParagraphStyle(
        'CueText',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8,
        leading=10.5,
        textColor=colors.HexColor("#1e3a8a"),
        spaceAfter=2
    )

    action_text_style = ParagraphStyle(
        'ActionText',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=7.5,
        leading=10.5,
        textColor=colors.HexColor("#0f172a")
    )

    tabs_callout_style = ParagraphStyle(
        'TabsCallout',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=7.5,
        leading=10,
        textColor=colors.HexColor("#b45309")
    )

    takeaway_title_style = ParagraphStyle(
        'TakeawayTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8,
        leading=10,
        textColor=C_GREEN,
        spaceAfter=2
    )

    takeaway_body_style = ParagraphStyle(
        'TakeawayBody',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=7.5,
        leading=10,
        textColor=colors.HexColor("#14532d")
    )

    table_cell_style = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=7,
        leading=9.5,
        textColor=C_DARK
    )

    table_cell_bold = ParagraphStyle(
        'TableCellBold',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=7,
        leading=9.5,
        textColor=C_DARK
    )

    table_header_style = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=7.5,
        leading=9.5,
        textColor=colors.white
    )

    story = []

    # ==========================================
    # COVER / HEADER SECTION
    # ==========================================
    logo_path = "/home/Harshit/Desktop/PROJECTS/SIH 2026/AICTE/SAGE/frontend/public/sage-logo.png"
    has_logo = os.path.exists(logo_path)

    if has_logo:
        logo_img = Image(logo_path, width=48, height=48)
        title_block = [
            Paragraph("SAGE — System for Approval Governance and Evaluation", title_style),
            Paragraph("Official Video Presentation, Director Cues & Tab Navigation Master Script &bull; SIH 2026 PS 26262", subtitle_style),
        ]
        header_table = Table([[logo_img, title_block]], colWidths=[58, 465])
        header_table.setStyle(TableStyle([
            ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
            ('LEFTPADDING', (0,0), (-1,-1), 0),
            ('RIGHTPADDING', (0,0), (-1,-1), 0),
            ('TOPPADDING', (0,0), (-1,-1), 0),
            ('BOTTOMPADDING', (0,0), (-1,-1), 0),
        ]))
        story.append(header_table)
    else:
        story.append(Paragraph("SAGE — System for Approval Governance and Evaluation", title_style))
        story.append(Paragraph("Official Video Presentation, Director Cues & Tab Navigation Master Script &bull; SIH 2026 PS 26262", subtitle_style))

    # Tricolor accent bar
    tricolor_table = Table([["", "", ""]], colWidths=[174, 175, 174], rowHeights=[2.5])
    tricolor_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), colors.HexColor("#ff9933")),
        ('BACKGROUND', (1,0), (1,0), colors.HexColor("#002147")),
        ('BACKGROUND', (2,0), (2,0), colors.HexColor("#138808")),
        ('TOPPADDING', (0,0), (-1,-1), 0),
        ('BOTTOMPADDING', (0,0), (-1,-1), 0),
        ('LEFTPADDING', (0,0), (-1,-1), 0),
        ('RIGHTPADDING', (0,0), (-1,-1), 0),
    ]))
    story.append(tricolor_table)
    story.append(Spacer(1, 8))

    # Studio Recording Setup & Metadata
    studio_box = [
        [
            Paragraph("<b>Target Duration:</b> ~12 to 14 Minutes", table_cell_style),
            Paragraph("<b>Video Format:</b> 1080p 60fps (Full Screen + Facecam PIP)", table_cell_style),
            Paragraph("<b>Statutory Baseline:</b> AICTE APH 2024–27", table_cell_style)
        ],
        [
            Paragraph("<b>Audio Standard:</b> 48kHz Crisp Broadcast Audio", table_cell_style),
            Paragraph("<b>Visual PIP Position:</b> Top-Right 22% Screen Width", table_cell_style),
            Paragraph("<b>Pacing:</b> 130–145 wpm Authoritative Cadence", table_cell_style)
        ]
    ]
    studio_table = Table(studio_box, colWidths=[174, 174, 175])
    studio_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#f8fafc")),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#e2e8f0")),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(studio_table)
    story.append(Spacer(1, 8))

    # Pre-Staged Browser Tabs Architecture Table
    story.append(Paragraph("<b>MASTER TAB PRE-STAGING ARCHITECTURE (OPEN BEFORE RECORDING)</b>", section_heading_style))
    story.append(Paragraph("To ensure seamless live transitions without logging in and out during recording, pre-stage these 6 browser tabs in advance (using separate browser profiles or tabs):", table_cell_style))
    story.append(Spacer(1, 4))

    tabs_setup_data = [
        [
            Paragraph("<b>Tab #</b>", table_header_style),
            Paragraph("<b>Pre-Authenticated Persona &amp; Credentials</b>", table_header_style),
            Paragraph("<b>Target URL &amp; View</b>", table_header_style),
            Paragraph("<b>Pre-Staged UI State &amp; Focus Area</b>", table_header_style)
        ],
        [
            Paragraph("<b>TAB 1</b>", table_cell_bold),
            Paragraph("<b>Unauthenticated / Public</b><br/>Ready for Login Walkthrough", table_cell_style),
            Paragraph("<code>http://localhost:5173/login</code>", table_cell_style),
            Paragraph("Demonstrates WCAG 2.2 AA accessibility toolbar (A+, A-, High Contrast) and synthetic 2-column role grid.", table_cell_style)
        ],
        [
            Paragraph("<b>TAB 2</b>", table_cell_bold),
            Paragraph("<b>Institution Officer (Delhi)</b><br/><code>officer@delhi-tech.edu</code>", table_cell_style),
            Paragraph("<code>http://localhost:5173/dashboard</code><br/>&rarr; <code>/applications</code>", table_cell_style),
            Paragraph("Delhi Tech dashboard (PID 1-500123498), 'New Approval Application' button, Course Proposal &amp; OCR tabs, SAGE AI findings modal.", table_cell_style)
        ],
        [
            Paragraph("<b>TAB 3</b>", table_cell_bold),
            Paragraph("<b>Regional Officer (Northern)</b><br/><code>regional@aicte.gov.in</code>", table_cell_style),
            Paragraph("<code>http://localhost:5173/applications</code><br/>&rarr; <code>/applications/:id</code>", table_cell_style),
            Paragraph("Scrutiny evaluation queue, 'Evaluate Scrutiny Rules' (Clauses 1.4, 2.1, 3.2, 4.1), Advance Workflow to SCRUTINY_DEFICIENT.", table_cell_style)
        ],
        [
            Paragraph("<b>TAB 4</b>", table_cell_bold),
            Paragraph("<b>EVC Coordinator</b><br/><code>evc.coord@aicte.gov.in</code>", table_cell_style),
            Paragraph("<code>http://localhost:5173/evc</code>", table_cell_style),
            Paragraph("Pending Inspections tab, Confirmed Schedules tab, Accredited Experts tab, Google OR-Tools CP-SAT discrete solver execution.", table_cell_style)
        ],
        [
            Paragraph("<b>TAB 5</b>", table_cell_bold),
            Paragraph("<b>Compliance Officer</b><br/><code>compliance@aicte.gov.in</code>", table_cell_style),
            Paragraph("<code>http://localhost:5173/compliance</code>", table_cell_style),
            Paragraph("Statutory Deadlines tab, Compliance Findings tab, Surveillance Runs tab, continuous background surveillance and severity levels.", table_cell_style)
        ],
        [
            Paragraph("<b>TAB 6</b>", table_cell_bold),
            Paragraph("<b>Regulatory Auditor</b><br/><code>auditor@aicte.gov.in</code>", table_cell_style),
            Paragraph("<code>http://localhost:5173/admin/audit</code>", table_cell_style),
            Paragraph("Immutable Regulatory Audit Ledger, Audit Outbox pattern, non-repudiation timestamps, Correlation ID tracing.", table_cell_style)
        ]
    ]

    tabs_setup_table = Table(tabs_setup_data, colWidths=[42, 145, 145, 191])
    tabs_setup_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), C_NAVY),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
    ]))
    story.append(tabs_setup_table)
    story.append(Spacer(1, 10))

    # Helper function for Act Section
    def build_act_flowable(act_num, title, duration, visual_mode, target_tab, screen_action, inpage_tabs, speaker, dialogue_paras, takeaways_title, takeaways_items):
        elements = []
        
        # Act Header Table
        header_data = [
            [
                Paragraph(f"<b>ACT {act_num}: {title.upper()}</b>", act_title_style),
                Paragraph(f"<b>DURATION:</b> {duration} &bull; <b>MODE:</b> {visual_mode}", act_meta_style)
            ],
            [
                Paragraph(f"<b>ACTIVE BROWSER TAB:</b> {target_tab}", ParagraphStyle('SubTab', parent=act_meta_style, fontName='Helvetica', fontSize=7, textColor=colors.HexColor("#e2e8f0"))),
                Paragraph("<b>PRODUCTION QUALITY:</b> Broadcast 1080p Clean", ParagraphStyle('SubRight', parent=act_meta_style, fontName='Helvetica', fontSize=7, alignment=2, textColor=colors.HexColor("#e2e8f0")))
            ]
        ]
        h_table = Table(header_data, colWidths=[335, 188])
        h_table.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), C_NAVY),
            ('TOPPADDING', (0,0), (-1,-1), 3),
            ('BOTTOMPADDING', (0,0), (-1,-1), 3),
            ('LEFTPADDING', (0,0), (-1,-1), 7),
            ('RIGHTPADDING', (0,0), (-1,-1), 7),
            ('LINEBELOW', (0,0), (-1,0), 0.5, colors.HexColor("#1e293b")),
        ]))
        elements.append(h_table)
        elements.append(Spacer(1, 4))

        # Director Cues & Tab Navigation Box
        cue_content = [
            Paragraph(f"<b>[DIRECTOR &amp; SCREEN DIRECTIONS]</b> &bull; Visual Mode: <b>{visual_mode}</b>", cue_text_style),
            Paragraph(f"<b>ACTION ON SCREEN:</b> {screen_action}", action_text_style),
            Spacer(1, 2),
            Paragraph(f"<b>IN-PAGE UI TABS TO NAVIGATE:</b> {inpage_tabs}", tabs_callout_style)
        ]
        cue_table = Table([[cue_content]], colWidths=[523])
        cue_table.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#eff6ff")),
            ('BOX', (0,0), (-1,-1), 0.75, colors.HexColor("#3b82f6")),
            ('TOPPADDING', (0,0), (-1,-1), 4),
            ('BOTTOMPADDING', (0,0), (-1,-1), 4),
            ('LEFTPADDING', (0,0), (-1,-1), 7),
            ('RIGHTPADDING', (0,0), (-1,-1), 7),
        ]))
        elements.append(cue_table)
        elements.append(Spacer(1, 4))

        # Dialogue Box
        diag_content = [Paragraph(f"<b>SPEAKER ({speaker}):</b>", speaker_label_style)]
        for p in dialogue_paras:
            diag_content.append(Paragraph(f'"{p}"', dialogue_style))
        diag_table = Table([[diag_content]], colWidths=[523])
        diag_table.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#fefce8")),
            ('LINEBEFORE', (0,0), (0,-1), 2.5, C_SAFFRON),
            ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#fde047")),
            ('TOPPADDING', (0,0), (-1,-1), 5),
            ('BOTTOMPADDING', (0,0), (-1,-1), 5),
            ('LEFTPADDING', (0,0), (-1,-1), 9),
            ('RIGHTPADDING', (0,0), (-1,-1), 7),
        ]))
        elements.append(diag_table)
        elements.append(Spacer(1, 4))

        # Takeaways Box
        takeaways_text = " &bull; ".join(takeaways_items)
        take_content = [
            Paragraph(f"<b>KEY JURY TAKEAWAYS ({takeaways_title}):</b>", takeaway_title_style),
            Paragraph(f"&bull; {takeaways_text}", takeaway_body_style)
        ]
        take_table = Table([[take_content]], colWidths=[523])
        take_table.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#f0fdf4")),
            ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#86efac")),
            ('TOPPADDING', (0,0), (-1,-1), 3),
            ('BOTTOMPADDING', (0,0), (-1,-1), 3),
            ('LEFTPADDING', (0,0), (-1,-1), 7),
            ('RIGHTPADDING', (0,0), (-1,-1), 7),
        ]))
        elements.append(take_table)
        elements.append(Spacer(1, 6))

        return KeepTogether(elements)

    # ==========================================
    # ACT 1: THE HOOK & STATUTORY CHALLENGE
    # ==========================================
    act1 = build_act_flowable(
        act_num=1,
        title="The Hook & Statutory Challenge",
        duration="~0:00 – 1:30",
        visual_mode="FACECAM FULL SCREEN",
        target_tab="N/A (Speaker Direct to Camera — No Browser Visible Yet)",
        screen_action="Eye-level camera framing, warm professional key lighting. Presenter speaks with high energy, confidence, and authoritative cadence directly into the lens. Introduce the national scale of AICTE technical education governance.",
        inpage_tabs="None (Presenter Full Frame). Prepare to cut to Tab 1 on transition cue.",
        speaker="Presenter (Direct to Camera)",
        dialogue_paras=[
            "Namaste, esteemed jury members and evaluators. Every single academic year, more than ten thousand technical and management institutions across India apply to the All India Council for Technical Education (AICTE) for new approvals, extension of approvals, intake expansions, and emerging branch introductions.",
            "Under the statutory AICTE Approval Process Handbook (APH 2024–27), this governance process has historically involved thousands of physical paper dossiers, months of manual verification, vulnerability to human error, and massive logistical gridlocks in scheduling Expert Visit Committees.",
            "Today, we are proud to introduce SAGE — the System for Approval Governance and Evaluation. SAGE is an end-to-end, multi-tenant regulatory automation platform engineered from the ground up to solve SIH 2026 Problem Statement 26262. It delivers explainable AI document intelligence, deterministic statutory scrutiny with zero human bias, mathematical committee panel optimization using Google OR-Tools CP-SAT discrete constraint solvers, continuous compliance surveillance, and an immutable regulatory audit trail. Let me take you inside the live system."
        ],
        takeaways_title="Problem & Vision",
        takeaways_items=[
            "Addresses high-volume national statutory process across 10,000+ colleges",
            "Directly solves SIH PS 26262 and enforces AICTE APH 2024–27 regulatory norms",
            "Replaces weeks of manual paper scrutiny with automated mathematical guarantees"
        ]
    )
    story.append(act1)

    story.append(PageBreak())

    # ==========================================
    # ACT 2: PORTAL LOGIN & MULTI-TENANT PERSONAS
    # ==========================================
    act2 = build_act_flowable(
        act_num=2,
        title="Portal Login & Multi-Tenant Personas",
        duration="~1:30 – 3:30",
        visual_mode="SCREEN SHARE + FACECAM PIP (Top-Right)",
        target_tab="BROWSER TAB 1 &bull; http://localhost:5173/login",
        screen_action="Switch to Screen Share with Facecam PIP in top-right. Highlight the National Accessibility Bar: click font scaling 'A+' then 'A-', then toggle 'High Contrast' mode on/off. Move mouse across the 2-column synthetic role grid displaying all 6 statutory roles.",
        inpage_tabs="Top Bar: Accessibility Font Scaling ('A-', 'Normal', 'A+') &bull; High Contrast Toggle &bull; Synthetic Demo Accounts Panel: Central AICTE Admin, Regional Officer, Institution Officer (Delhi & Mumbai), Compliance Officer, EVC Coordinator, Regulatory Auditor.",
        speaker="Presenter",
        dialogue_paras=[
            "Welcome to the official SAGE portal. Notice immediately our AICTE-inspired Government Design System. Across the top, we have an integrated National Accessibility Bar adhering to WCAG 2.2 AA standards: users can scale fonts on the fly or toggle High Contrast mode for visually impaired officers.",
            "Underneath, in our synthetic demonstration panel, you can see the six distinct statutory personas that power the AICTE governance ecosystem: The Central AICTE Administrator with nationwide regulatory jurisdiction; The Regional Officer, scoped strictly to their regional directorate; Institution Officers representing colleges like Delhi Tech or Mumbai Academy; The Compliance Officer, overseeing continuous fire safety NOCs; The EVC Coordinator, allocating peer review rosters; and The Independent Statutory Auditor.",
            "Every single persona has strict Role-Based Access Control. Institutional applicants can submit applications and upload documents, while regulatory officers can only evaluate, scrutinize, and audit. Let's switch to our authenticated institution tab."
        ],
        takeaways_title="Security & Accessibility",
        takeaways_items=[
            "WCAG 2.2 AA accessibility toolbar (font scaling & high contrast) built-in",
            "Multi-tenant data isolation strictly enforced by Spring Boot security",
            "All 6 statutory roles covered with instant 1-click demonstration credentials"
        ]
    )
    story.append(act2)
    story.append(Spacer(1, 4))

    # ==========================================
    # ACT 3: INSTITUTION DOSSIER & SAGE AI OCR
    # ==========================================
    act3 = build_act_flowable(
        act_num=3,
        title="Institution Dossier & SAGE AI OCR",
        duration="~3:30 – 6:30",
        visual_mode="SCREEN SHARE + FACECAM PIP (Top-Right)",
        target_tab="BROWSER TAB 2 &bull; http://localhost:5173/dashboard -> /applications",
        screen_action="Switch to Tab 2 (authenticated as Delhi Tech Officer). Point out Permanent Institute ID (PID: 1-500123498) and Active Standing banner. Navigate to 'Approval Applications' -> Click 'New Approval Application' -> Choose 'New Course Introduction'. Open application details. Click 'Compliance Documents & OCR' tab. Click the purple 'SAGE AI findings' button on Fire Safety Certificate to open the Google Gemini Flash advisory audit report modal. Point out 45ms Indra OCR speed.",
        inpage_tabs="Application Detail Sub-Tabs: [Tab 1: Course Proposal] &bull; [Tab 2: Compliance Documents & OCR (ACTIVE)] &bull; [Tab 3: Scrutiny Findings] &bull; [Tab 4: Workflow History & State Transitions]. Modal: 'SAGE AI Advisory Findings (Google Gemini Flash)'.",
        speaker="Presenter",
        dialogue_paras=[
            "We are now authenticated as the authorized signatory for Delhi Institute of Advanced Engineering. Notice how the interface adapts: the top banner shows our Permanent Institute ID, Category, and Active Standing. Because of our multi-tenant data isolation, this officer can only see Delhi Institute records. Attempting to access Mumbai records is blocked at both the React router and Spring Boot security layer with an automatic TENANCY_VIOLATION audit log.",
            "Let's click on Approval Applications. Notice this prominent orange button: 'New Approval Application'. This button is only visible to applicant colleges—regulators do not see it. SAGE supports all five versioned APH workflow types: EoA, Intake Increase, New Courses, Emerging Branches, and New Institutions.",
            "Now let's switch to the 'Compliance Documents & OCR' tab. When a college uploads a document, it passes through binary magic-byte validation, SHA-256 cryptographic hashing, and our local Indra OCR engine, which processes the PDF directly on the CPU in just 45 to 55 milliseconds per page—over 10 times faster than legacy Tesseract.",
            "Simultaneously, our integrated SAGE AI powered by Google Gemini Flash performs an automated advisory audit. Look at this modal: SAGE AI has verified Document Type Validity, confirmed Delhi Fire Service issuing authority, validated a 98% concordance match with institutional records, detected expiration dates through March 2027, and verified zero digital tampering. I will now click 'Submit Application'. The application immediately transitions from DRAFT to SUBMITTED."
        ],
        takeaways_title="Document AI & Innovation",
        takeaways_items=[
            "CPU-optimized Indra OCR runs at 45–55 ms/page (10x faster than Tesseract)",
            "Google Gemini Flash AI performs instant advisory validation on certificates",
            "Cryptographic SHA-256 hashing and magic-byte validation prevent spoofed files"
        ]
    )
    story.append(act3)

    story.append(PageBreak())

    # ==========================================
    # ACT 4: REGIONAL SCRUTINY & APH RULE ENGINE
    # ==========================================
    act4 = build_act_flowable(
        act_num=4,
        title="Regional Directorate & APH Scrutiny",
        duration="~6:30 – 8:45",
        visual_mode="SCREEN SHARE + FACECAM PIP (Top-Right)",
        target_tab="BROWSER TAB 3 &bull; http://localhost:5173/applications/:id",
        screen_action="Switch to Tab 3 (authenticated as Regional Officer, Northern Region). Open Delhi Tech application. Point out that the 'Upload Regulatory Document' form is completely hidden (RBAC). Click 'Evaluate Scrutiny Rules' button. Switch to 'Automated Scrutiny Findings' tab. Review passed and deficient clauses. Click 'Advance Workflow' dropdown -> select 'SCRUTINY_DEFICIENT'. Explain the rectified document upload lifecycle (v1 to v2).",
        inpage_tabs="Application Detail Sub-Tabs: [Tab 1: Course Proposal] &bull; [Tab 2: Compliance Documents & OCR] &bull; [Tab 3: Automated Scrutiny Findings (ACTIVE)] &bull; [Tab 4: Workflow History (v1 &rarr; v2 State Machine)]. Header Action Buttons: 'Evaluate Scrutiny Rules', 'Advance Workflow'.",
        speaker="Presenter",
        dialogue_paras=[
            "We are now authenticated as the AICTE Regional Officer for the Northern Region. Observe how the portal transformed: the institutional creation button is gone because regional officers do not create applications; they scrutinize them.",
            "Notice the security boundaries: The 'Upload Regulatory Document' form is completely hidden from the Regional Officer. Instead, the officer has two powerful governance controls: 'Evaluate Scrutiny Rules' and 'Advance Workflow'.",
            "Let's click 'Evaluate Scrutiny Rules'. In less than 200 milliseconds, SAGE's Deterministic APH Scrutiny Engine evaluated the application against statutory clauses: Clause 1.4 for land area norms (minimum 2.5 acres); Clause 2.1 for fire safety NOC validity; Clause 3.2 for 1:20 faculty cadre ratios; and Clause 4.1 for title deed concordance.",
            "If an institution has an expired certificate, SAGE marks the rule as DEFICIENT and cites the exact clause. The Regional Officer can click 'Advance Workflow' and transition the state to SCRUTINY_DEFICIENT. When the college logs back in, they are instantly presented with a 'Resubmit Rectified Application' button. When they upload the renewed certificate, SAGE preserves the immutable version history from v1 to v2 and advances back to RESUBMITTED. Zero physical visits, zero lost paperwork, complete statutory transparency!"
        ],
        takeaways_title="Deterministic Scrutiny",
        takeaways_items=[
            "Zero human bias: rule engine strictly maps to APH 2024–27 clauses",
            "Automated deficiency notices with statutory clause-level citations",
            "Immutable versioning (v1 -> v2) for all rectified replacement documents"
        ]
    )
    story.append(act4)
    story.append(Spacer(1, 4))

    # ==========================================
    # ACT 5: EVC SOLVER VIA GOOGLE OR-TOOLS CP-SAT
    # ==========================================
    act5 = build_act_flowable(
        act_num=5,
        title="EVC Solver via Google OR-Tools CP-SAT",
        duration="~8:45 – 10:45",
        visual_mode="SCREEN SHARE + FACECAM PIP (Top-Right)",
        target_tab="BROWSER TAB 4 &bull; http://localhost:5173/evc",
        screen_action="Switch to Tab 4 (authenticated as EVC Coordinator). Point out the 3 horizontal navigation tabs: 'Pending Inspections', 'Confirmed Inspection Schedules', and 'Accredited Experts Registry'. Click the 'Accredited Experts Registry' tab to show professor specializations, home states, and conflict tracking. Return to 'Pending Inspections' and click 'Run Statutory Allocation Engine'. Point out the mathematical CP-SAT solver execution.",
        inpage_tabs="EVC Module Navigation Tabs: [Tab 1: Pending Inspections (ACTIVE)] &bull; [Tab 2: Confirmed Inspection Schedules] &bull; [Tab 3: Accredited Experts Registry]. Action Button: 'Run Statutory Allocation Engine' / 'Generate Conflict-Free Schedule'.",
        speaker="Presenter",
        dialogue_paras=[
            "Now let's examine one of the most technologically advanced components of SAGE: The Expert Visit Committee (EVC) Allocation Engine. Under AICTE rules, whenever an institution applies for new approvals, an Expert Visit Committee must physically inspect the campus.",
            "Historically, assigning inspection committees was a monumental logistical headache: thousands of professors across India; strict conflict-of-interest rules where an expert cannot inspect a college in their home state or alma mater; inspections must run for exactly 2 consecutive working days; Sundays and holidays are strictly barred; and each committee must contain exactly 3 distinct active peer experts.",
            "Instead of manual guesswork, we integrated the Google OR-Tools CP-SAT constraint optimization solver directly into our Spring Boot backend. Let's click 'Generate Conflict-Free Schedule'.",
            "In under 400 milliseconds, the CP-SAT integer programming solver formulated a discrete constraint satisfaction problem with hundreds of Boolean variables and generated a mathematically proven, conflict-free inspection schedule: 3 distinct peer evaluators assigned per panel; consecutive two-day windows with all Sundays excluded; zero institutional conflicts of interest; and full availability validation. When confirmed, appointments are committed atomically and email notices are dispatched. This replaces weeks of manual phone calls with mathematical certainty!"
        ],
        takeaways_title="Discrete Constraint Optimization",
        takeaways_items=[
            "Google OR-Tools CP-SAT integer programming solver embedded in backend",
            "Enforces 3-member active expert roster, 2-day consecutive window, and Sunday exclusion",
            "Mathematically guarantees zero conflict of interest across all peer experts"
        ]
    )
    story.append(act5)

    story.append(PageBreak())

    # ==========================================
    # ACT 6: SURVEILLANCE & IMMUTABLE AUDIT LEDGER
    # ==========================================
    act6 = build_act_flowable(
        act_num=6,
        title="Surveillance & Immutable Audit Ledger",
        duration="~10:45 – 12:30",
        visual_mode="SCREEN SHARE + FACECAM PIP (Top-Right)",
        target_tab="BROWSER TABS 5 & 6 &bull; /compliance &rarr; /admin/audit",
        screen_action="First, show Tab 5 (Compliance Officer): click through the 3 tabs ('Statutory Deadlines', 'Compliance Findings', 'Surveillance Runs'). Click 'Trigger Compliance Sweep' button and highlight severity levels (INFO, WARNING, CRITICAL). Then smoothly switch to Tab 6 (Regulatory Auditor at /admin/audit): demonstrate the immutable ledger, filter by Correlation ID, and show the SHA-256 document fingerprint.",
        inpage_tabs="Compliance Module Tabs: [Tab 1: Statutory Deadlines &amp; Expiries] &bull; [Tab 2: Compliance Findings &amp; Risk Analysis] &bull; [Tab 3: Continuous Surveillance Runs &amp; Execution History]. Audit Viewer Controls: Filter by Principal, Event Type, Correlation ID Search.",
        speaker="Presenter",
        dialogue_paras=[
            "AICTE governance doesn't end when an approval letter is issued. SAGE includes an Automated Continuous Compliance Engine. Our distributed background scheduler runs automated surveillance sweeps across all active institutions: tracking upcoming certificate expiration dates, detecting cadre ratio drift, and automatically classifying deficiencies into INFO, WARNING, and CRITICAL severity.",
            "Finally, let's look at statutory oversight. We are now in Tab 6, logged in as the Independent Regulatory Auditor. Notice this persona's portal: they cannot edit applications or change marks. They have an unrestricted view of the Immutable Regulatory Audit Ledger.",
            "Every single consequential action in SAGE—every login, every document upload with its SHA-256 hash, every scrutiny score, and every EVC committee assignment—is committed atomically in an Audit Outbox pattern. If the audit event fails to write to PostgreSQL, the business transaction rolls back! Each entry records the exact UTC timestamp, user principal, client IP address, action classification, and a unique Correlation ID that traces requests end-to-end. This ensures complete non-repudiation and anti-corruption integrity."
        ],
        takeaways_title="Audit & Governance",
        takeaways_items=[
            "Continuous compliance sweeps run in background via distributed scheduler locks",
            "Audit Outbox pattern: state changes roll back if audit persistence fails",
            "Correlation IDs trace transactions across React, Spring Boot, and FastAPI"
        ]
    )
    story.append(act6)
    story.append(Spacer(1, 4))

    # ==========================================
    # ACT 7: ARCHITECTURE, CLOUD & CLOSING
    # ==========================================
    act7 = build_act_flowable(
        act_num=7,
        title="Technology Stack, Cloud & Closing",
        duration="~12:30 – 14:00",
        visual_mode="FACECAM FULL SCREEN",
        target_tab="N/A (Speaker Direct to Camera — Full Frame Return)",
        screen_action="Switch cleanly from Screen Share back to Facecam Full Screen. The presenter delivers an inspiring, high-impact concluding pitch. Recite the comprehensive test pass metrics (175/175 tests), cloud architecture readiness on Oracle ARM64 Ampere A1, and the final transformative vision of SAGE for national technical education governance.",
        inpage_tabs="None (Presenter Full Frame). Camera framing eye-level, energetic, concluding posture.",
        speaker="Presenter (Closing to Camera)",
        dialogue_paras=[
            "To summarize the engineering behind SAGE: Core Backend built on Java 21 LTS and Spring Boot 3.4 with 24 relational tables managed by Flyway migrations, stateless Bearer JWTs with server-side token revocation, and Google OR-Tools CP-SAT; Automation & OCR built on Python 3.12 and FastAPI utilizing high-speed CPU-optimized Indra OCR, Tesseract fallback, and Google Gemini Flash AI; Frontend built on React 19 and Vite 8 delivering an accessible, government-themed single-page application with responsive role-specific dashboards.",
            "We have verified 175 passing tests—101 Java unit and integration tests, 15 Python automation tests, and 59 Vitest frontend tests with 100% pass rate. Furthermore, SAGE is packaged via Docker Compose with automated Caddy HTTPS reverse proxying, automated daily database backup scripts, and is fully audited and optimized for Oracle Cloud Infrastructure (OCI) ARM64 Ampere A1 server deployment on the Always Free tier.",
            "SAGE transforms the AICTE Approval Process Handbook from a dense, 200-page regulatory manual into an automated, explainable, and tamper-resistant digital reality. It empowers institutions with speed and clarity, while giving regulators unprecedented transparency, fairness, and accountability. Thank you very much. We are now eager and ready to answer any questions from the esteemed evaluation panel!"
        ],
        takeaways_title="Architectural Excellence",
        takeaways_items=[
            "175 / 175 passing tests across Java, Python, and TypeScript suites",
            "OCI Always Free ARM64 (Ampere A1) production verified with Caddy HTTPS",
            "Complete enterprise-grade solution ready for national regulatory staging"
        ]
    )
    story.append(act7)

    story.append(PageBreak())

    # ==========================================
    # APPENDIX: DEMO QUICK REFERENCE CHEATSHEET
    # ==========================================
    story.append(Paragraph("<b>APPENDIX: DEMONSTRATION CREDENTIALS, ROLES &amp; ARCHITECTURE CHEATSHEET</b>", section_heading_style))
    story.append(Paragraph("Use this quick reference during live jury presentations or video recording. All demo accounts use password: <code>Password123!</code>", table_cell_style))
    story.append(Spacer(1, 6))

    roles_data = [
        [
            Paragraph("<b>Role Persona</b>", table_header_style),
            Paragraph("<b>Email / Username</b>", table_header_style),
            Paragraph("<b>Scope / Jurisdiction</b>", table_header_style),
            Paragraph("<b>Pre-Staged Tab &amp; Key Demonstration Focus</b>", table_header_style)
        ],
        [
            Paragraph("<b>Central AICTE Admin</b>", table_cell_bold),
            Paragraph("<code>admin@aicte.gov.in</code>", table_cell_style),
            Paragraph("Nationwide (All Tenancies)", table_cell_style),
            Paragraph("User &amp; role management, global diagnostics, complete audit log", table_cell_style)
        ],
        [
            Paragraph("<b>Regional Officer</b>", table_cell_bold),
            Paragraph("<code>regional@aicte.gov.in</code>", table_cell_style),
            Paragraph("Northern Region (DL, UP, HR)", table_cell_style),
            Paragraph("<b>Tab 3:</b> Scrutiny evaluation, APH rule checks, advance workflow state", table_cell_style)
        ],
        [
            Paragraph("<b>Institution Officer (Delhi)</b>", table_cell_bold),
            Paragraph("<code>officer@delhi-tech.edu</code>", table_cell_style),
            Paragraph("Delhi Tech (PID: 1-500123498)", table_cell_style),
            Paragraph("<b>Tab 2:</b> Create application, upload PDF certificate, SAGE AI findings, submit", table_cell_style)
        ],
        [
            Paragraph("<b>Institution Officer (Mumbai)</b>", table_cell_bold),
            Paragraph("<code>officer@mumbai-engg.edu</code>", table_cell_style),
            Paragraph("Mumbai Engg (PID: 1-500987654)", table_cell_style),
            Paragraph("Demonstrates multi-tenancy &amp; cross-institutional access denial", table_cell_style)
        ],
        [
            Paragraph("<b>Compliance Officer</b>", table_cell_bold),
            Paragraph("<code>compliance@aicte.gov.in</code>", table_cell_style),
            Paragraph("Compliance Directorate", table_cell_style),
            Paragraph("<b>Tab 5:</b> Continuous surveillance sweeps, NOC expiry tracking, cadre ratios", table_cell_style)
        ],
        [
            Paragraph("<b>EVC Coordinator</b>", table_cell_bold),
            Paragraph("<code>evc.coord@aicte.gov.in</code>", table_cell_style),
            Paragraph("Inspection Operations Hub", table_cell_style),
            Paragraph("<b>Tab 4:</b> OR-Tools CP-SAT discrete solver, 3-expert committee generation", table_cell_style)
        ],
        [
            Paragraph("<b>Regulatory Auditor</b>", table_cell_bold),
            Paragraph("<code>auditor@aicte.gov.in</code>", table_cell_style),
            Paragraph("Statutory Oversight", table_cell_style),
            Paragraph("<b>Tab 6:</b> Immutable tamper-evident audit trail, Correlation ID search", table_cell_style)
        ]
    ]

    role_table = Table(roles_data, colWidths=[105, 130, 110, 178])
    role_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), C_NAVY),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f8fafc")]),
    ]))
    story.append(role_table)
    story.append(Spacer(1, 8))

    # Architecture & Cloud Reference Box
    arch_data = [
        [
            Paragraph("<b>Core Technology</b>", table_header_style),
            Paragraph("<b>Version / Framework</b>", table_header_style),
            Paragraph("<b>Key Governance &amp; Algorithmic Capabilities</b>", table_header_style)
        ],
        [
            Paragraph("<b>Backend Engine</b>", table_cell_bold),
            Paragraph("Java 21 LTS &bull; Spring Boot 3.4", table_cell_style),
            Paragraph("24 relational tables, Flyway migrations, stateless JWT with revocation, OR-Tools CP-SAT solver", table_cell_style)
        ],
        [
            Paragraph("<b>Document AI &amp; OCR</b>", table_cell_bold),
            Paragraph("Python 3.12 &bull; FastAPI", table_cell_style),
            Paragraph("Indra OCR (45ms CPU inference), Tesseract fallback, Google Gemini Flash advisory audit", table_cell_style)
        ],
        [
            Paragraph("<b>Frontend Single Page App</b>", table_cell_bold),
            Paragraph("React 19 &bull; Vite 8 &bull; TypeScript", table_cell_style),
            Paragraph("WCAG 2.2 AA accessibility toolbar, role-specific dynamic dashboards, zero information leakage", table_cell_style)
        ],
        [
            Paragraph("<b>Cloud &amp; Infrastructure</b>", table_cell_bold),
            Paragraph("OCI Ampere A1 (ARM64) &bull; Docker", table_cell_style),
            Paragraph("Caddy HTTPS reverse proxy, daily automated PostgreSQL backup scripts, 175/175 tests verified", table_cell_style)
        ]
    ]
    arch_table = Table(arch_data, colWidths=[105, 130, 288])
    arch_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#064e3b")),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f0fdf4")]),
    ]))
    story.append(arch_table)
    story.append(Spacer(1, 8))

    # Statutory Citations & Emergency Cheatsheet Card
    statutory_data = [
        [
            Paragraph("<b>Key AICTE APH 2024–27 Statutory Citations</b>", table_header_style),
            Paragraph("<b>Live Demo Port &amp; Emergency Recovery Cheatsheet</b>", table_header_style)
        ],
        [
            Paragraph(
                "&bull; <b>Clause 1.4:</b> Land &amp; Built-up Area norms (Min 2.5 acres urban / 5.0 acres rural)<br/>"
                "&bull; <b>Clause 2.1:</b> Fire Safety &amp; Structural Stability NOC validity verification<br/>"
                "&bull; <b>Clause 3.2:</b> Faculty Cadre Ratio (1:20 UG Engg; Professor : Assoc : Asst = 1:2:6)<br/>"
                "&bull; <b>Clause 4.1:</b> Title Deed, Non-encumbrance &amp; Revenue concordance check<br/>"
                "&bull; <b>Chapter II Sec 2.14:</b> EVC 3-member panel, 2 consecutive days, Sunday barred",
                table_cell_style
            ),
            Paragraph(
                "&bull; <b>Frontend App:</b> <code>http://localhost:5173</code> (React 19 / Vite 8)<br/>"
                "&bull; <b>Backend API:</b> <code>http://localhost:8080/swagger-ui.html</code> (Spring Boot 3.4)<br/>"
                "&bull; <b>Automation &amp; OCR:</b> <code>http://localhost:8000/docs</code> (FastAPI / Indra OCR)<br/>"
                "&bull; <b>Reset Demo Seed Data:</b> <code>./scripts/reset-demo.sh &amp;&amp; ./scripts/seed-demo.sh</code><br/>"
                "&bull; <b>Run Full Smoke Test:</b> <code>./scripts/deployment-smoke-test.sh</code>",
                table_cell_style
            )
        ]
    ]
    statutory_table = Table(statutory_data, colWidths=[261, 262])
    statutory_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), C_NAVY),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94a3b8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.HexColor("#f8fafc")]),
    ]))
    story.append(statutory_table)

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"PDF successfully generated at: {output_path}")

if __name__ == "__main__":
    out_pdf = "/home/Harshit/Desktop/PROJECTS/SIH 2026/AICTE/SAGE/docs/SAGE_VIDEO_PRESENTATION_SCRIPT.pdf"
    build_pdf(out_pdf)
