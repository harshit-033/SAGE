import os
import json
import math
import cv2
import numpy as np
from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors

CORPUS_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "corpus"))
os.makedirs(CORPUS_DIR, exist_ok=True)

ground_truth = {}

def create_fire_safety_pdf():
    filepath = os.path.join(CORPUS_DIR, "doc_1_fire_safety_valid.pdf")
    c = canvas.Canvas(filepath, pagesize=letter)
    c.setFont("Helvetica-Bold", 16)
    c.drawCentredString(300, 750, "GOVERNMENT OF NCT OF DELHI")
    c.setFont("Helvetica-Bold", 14)
    c.drawCentredString(300, 730, "HEADQUARTERS: DELHI FIRE SERVICE: NEW DELHI")
    c.setFont("Helvetica", 10)
    c.drawString(50, 700, "Ref No: DFS/MS/2024/SZ/8891")
    c.drawString(400, 700, "Date of Issue: 15/05/2024")
    c.setFont("Helvetica-Bold", 12)
    c.drawCentredString(300, 660, "FIRE SAFETY CERTIFICATE / NOC")
    c.setFont("Helvetica", 11)
    text = (
        "Certified that Delhi Institute of Technology comprised of Basement, Ground plus Three Upper floors "
        "has complied with the fire prevention and fire safety requirements in accordance with Rule 33 of Delhi Fire Service Rules. "
        "The building was inspected by authorized officers on 10/05/2024 in the presence of the Principal."
    )
    t = c.beginText(50, 620)
    t.setFont("Helvetica", 11)
    t.setTextOrigin(50, 620)
    for line in [
        "Certified that Delhi Institute of Technology comprised of Ground plus Three floors",
        "has complied with fire safety requirements in accordance with Rule 33 of Delhi Fire Service Rules.",
        "Certificate Number: FS-DEL-2024-8891",
        "Issuing Authority: Delhi Fire Services",
        "Issue Date: 2024-05-15",
        "Valid Until: 2027-05-14",
        "Institution Name: Delhi Institute of Technology",
        "Inspection Status: Satisfactory and Compliant with AICTE Fire Safety Norms"
    ]:
        t.textLine(line)
    c.drawText(t)
    c.drawString(50, 480, "Issued on 15/05/2024 at New Delhi by Chief Fire Officer.")
    c.save()

    ground_truth["doc_1_fire_safety_valid.pdf"] = {
        "document_type": "FIRE_SAFETY_CERTIFICATE",
        "certificate_number": "FS-DEL-2024-8891",
        "issuing_authority": "Delhi Fire Services",
        "issue_date": "2024-05-15",
        "expiry_date": "2027-05-14",
        "institution_name": "Delhi Institute of Technology",
        "expected_lines_contain": ["Certificate Number: FS-DEL-2024-8891", "Delhi Fire Services", "2027-05-14"]
    }

def create_building_occupancy_pdf():
    filepath = os.path.join(CORPUS_DIR, "doc_2_building_occupancy.pdf")
    c = canvas.Canvas(filepath, pagesize=letter)
    c.setFont("Helvetica-Bold", 16)
    c.drawCentredString(300, 750, "MUNICIPAL CORPORATION OF GREATER MUMBAI")
    c.setFont("Helvetica-Bold", 13)
    c.drawCentredString(300, 725, "BUILDING PROPOSAL DEPARTMENT - SPECIAL CELL")
    c.setFont("Helvetica-Bold", 12)
    c.drawCentredString(300, 690, "BUILDING OCCUPANCY AND COMPLETION CERTIFICATE")
    t = c.beginText(50, 640)
    t.setFont("Helvetica", 11)
    for line in [
        "This is to certify that the institutional building erected by Mumbai College of Engineering and Technology",
        "at Survey No. 402/1A, Bandra Kurla Complex, Mumbai has been completed in accordance with approved plans.",
        "Certificate Number: BCC-MUM-2023-4120",
        "Issuing Authority: Municipal Corporation of Greater Mumbai",
        "Total Built-up Area: 12500 Sq. Meters",
        "Carpet Area: 10800 Sq. Meters",
        "Issue Date: 2023-08-20",
        "Institution Name: Mumbai College of Engineering and Technology",
        "Permitted Use: Technical and Higher Education Campus"
    ]:
        t.textLine(line)
    c.drawText(t)
    c.drawString(50, 460, "Authorized Signatory: Executive Engineer (Building Proposal)")
    c.save()

    ground_truth["doc_2_building_occupancy.pdf"] = {
        "document_type": "OCCUPANCY_CERTIFICATE",
        "certificate_number": "BCC-MUM-2023-4120",
        "issuing_authority": "Municipal Corporation of Greater Mumbai",
        "built_up_area_sqm": "12500",
        "issue_date": "2023-08-20",
        "institution_name": "Mumbai College of Engineering and Technology",
        "expected_lines_contain": ["BCC-MUM-2023-4120", "12500 Sq. Meters", "Mumbai College of Engineering and Technology"]
    }

def create_land_table_pdf():
    filepath = os.path.join(CORPUS_DIR, "doc_3_land_registration_table.pdf")
    doc = SimpleDocTemplate(filepath, pagesize=letter)
    elements = []
    styles = getSampleStyleSheet()
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Heading1'],
        alignment=1,
        fontSize=15,
        spaceAfter=12
    )
    elements.append(Paragraph("REGISTRATION AND STAMPS DEPARTMENT - GOVERNMENT OF TAMIL NADU", title_style))
    elements.append(Paragraph("<b>LAND REGISTRATION TITLE DEED & SURVEY SCHEDULE</b>", styles['Normal']))
    elements.append(Spacer(1, 10))

    intro_text = (
        "Document Registered under Section 17 of Indian Registration Act. "
        "Title Deed Number: LND-TN-2022-7712. "
        "Issuing Authority: Sub-Registrar Chennai Central. "
        "Registered in favor of: Chennai Institute of Science and Technology. "
        "Registration Date: 2022-11-10. Total Contiguous Land Area: 5.50 Acres."
    )
    elements.append(Paragraph(intro_text, styles['Normal']))
    elements.append(Spacer(1, 15))

    data = [
        ['Survey No.', 'Village / Taluk', 'Classification', 'Area (Acres)', 'Status'],
        ['142/1', 'Kancheepuram', 'Patta Dry Land', '2.25', 'Clear Title'],
        ['142/2', 'Kancheepuram', 'Patta Dry Land', '1.75', 'Clear Title'],
        ['143/1', 'Kancheepuram', 'Patta Dry Land', '1.50', 'Clear Title'],
        ['Total', 'Single Contiguous Parcel', 'Educational Use', '5.50', 'Verified Contiguous']
    ]
    t = Table(data, colWidths=[80, 140, 100, 80, 100])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#0B4F6C')),
        ('TEXTCOLOR', (0,0), (-1,0), colors.whitesmoke),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
        ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
        ('BOTTOMPADDING', (0,0), (-1,0), 6),
        ('GRID', (0,0), (-1,-1), 1, colors.grey),
        ('BACKGROUND', (0,-1), (-1,-1), colors.HexColor('#F4F6F9')),
        ('FONTNAME', (0,-1), (-1,-1), 'Helvetica-Bold')
    ]))
    elements.append(t)
    doc.build(elements)

    ground_truth["doc_3_land_registration_table.pdf"] = {
        "document_type": "LAND_REGISTRATION",
        "certificate_number": "LND-TN-2022-7712",
        "issuing_authority": "Sub-Registrar Chennai Central",
        "land_area_acres": "5.50",
        "issue_date": "2022-11-10",
        "institution_name": "Chennai Institute of Science and Technology",
        "expected_lines_contain": ["LND-TN-2022-7712", "5.50", "Kancheepuram"]
    }

def create_anti_ragging_pdf():
    filepath = os.path.join(CORPUS_DIR, "doc_4_anti_ragging_affidavit.pdf")
    c = canvas.Canvas(filepath, pagesize=letter)
    c.setFont("Helvetica-Bold", 14)
    c.drawCentredString(300, 750, "BEFORE THE NOTARY PUBLIC: NEW DELHI")
    c.setFont("Helvetica-Bold", 12)
    c.drawCentredString(300, 725, "STATUTORY UNDERTAKING ON ANTI-RAGGING (AICTE FORMAT-1)")
    t = c.beginText(50, 680)
    t.setFont("Helvetica", 11)
    for line in [
        "I, Dr. Rajesh Sharma, Chairman/Secretary of Delhi Technical Education Trust,",
        "solemnly affirm and declare as follows pursuant to AICTE Regulations F.No.37-3/Legal/AICTE/2009:",
        "1. That our institution Delhi Institute of Technology strictly enforces zero tolerance against ragging.",
        "2. That Anti-Ragging Committee and Squad have been constituted as per APH Annexure 16.",
        "3. That 24x7 toll-free helpline notices are prominently displayed across campus.",
        "Document Number: AFF-AR-2024-0042",
        "Deponent: Dr. Rajesh Sharma",
        "Execution Date: 2024-06-10",
        "Verification Status: Attested by Advocate and Notary Public"
    ]:
        t.textLine(line)
    c.drawText(t)
    c.drawString(50, 480, "Sworn before me on 10th June 2024 at New Delhi.")
    c.drawString(400, 450, "Notary Seal & Signature")
    c.save()

    ground_truth["doc_4_anti_ragging_affidavit.pdf"] = {
        "document_type": "AFFIDAVIT_FORMAT_1",
        "certificate_number": "AFF-AR-2024-0042",
        "deponent_name": "Dr. Rajesh Sharma",
        "issue_date": "2024-06-10",
        "institution_name": "Delhi Institute of Technology",
        "expected_lines_contain": ["AFF-AR-2024-0042", "Dr. Rajesh Sharma", "zero tolerance against ragging"]
    }

def create_scanned_skewed_image():
    # Render an image with text, apply 5-degree rotation and slight noise
    img = np.full((1200, 900), 255, dtype=np.uint8)
    cv2.putText(img, "OFFICE OF THE CHIEF FIRE OFFICER: U.P. FIRE SERVICE", (60, 100), cv2.FONT_HERSHEY_SIMPLEX, 0.7, 0, 2)
    cv2.putText(img, "FIRE SAFETY COMPLIANCE CERTIFICATE", (150, 160), cv2.FONT_HERSHEY_SIMPLEX, 0.9, 0, 2)
    cv2.putText(img, "Certificate Number: FS-UP-2024-9901", (80, 250), cv2.FONT_HERSHEY_SIMPLEX, 0.8, 0, 2)
    cv2.putText(img, "Issuing Authority: UP Fire Service Lucknow", (80, 310), cv2.FONT_HERSHEY_SIMPLEX, 0.75, 0, 2)
    cv2.putText(img, "Institution Name: Lucknow Institute of Advanced Studies", (80, 370), cv2.FONT_HERSHEY_SIMPLEX, 0.75, 0, 2)
    cv2.putText(img, "Date of Issue: 2024-01-10", (80, 430), cv2.FONT_HERSHEY_SIMPLEX, 0.75, 0, 2)
    cv2.putText(img, "Valid Until: 2026-01-09", (80, 490), cv2.FONT_HERSHEY_SIMPLEX, 0.75, 0, 2)
    cv2.putText(img, "Inspection Result: Meets National Building Code Part 4 Norms", (80, 560), cv2.FONT_HERSHEY_SIMPLEX, 0.65, 0, 2)
    
    # Skew/rotate by 4 degrees
    h, w = img.shape
    center = (w // 2, h // 2)
    M = cv2.getRotationMatrix2D(center, 4.0, 1.0)
    rotated = cv2.warpAffine(img, M, (w, h), borderValue=255)
    
    # Add mild noise
    noise = np.random.normal(0, 5, (h, w)).astype(np.int16)
    noisy_img = np.clip(rotated.astype(np.int16) + noise, 0, 255).astype(np.uint8)
    
    filepath = os.path.join(CORPUS_DIR, "doc_5_scanned_skewed_fire_safety.png")
    cv2.imwrite(filepath, noisy_img)

    ground_truth["doc_5_scanned_skewed_fire_safety.png"] = {
        "document_type": "FIRE_SAFETY_CERTIFICATE",
        "certificate_number": "FS-UP-2024-9901",
        "issuing_authority": "UP Fire Service Lucknow",
        "issue_date": "2024-01-10",
        "expiry_date": "2026-01-09",
        "institution_name": "Lucknow Institute of Advanced Studies",
        "expected_lines_contain": ["FS-UP-2024-9901", "2026-01-09", "UP Fire Service"]
    }

def create_multipage_pdf():
    filepath = os.path.join(CORPUS_DIR, "doc_6_multipage_board_resolution.pdf")
    c = canvas.Canvas(filepath, pagesize=letter)
    # Page 1
    c.setFont("Helvetica-Bold", 14)
    c.drawCentredString(300, 750, "GOVERNING BODY RESOLUTION - PAGE 1 OF 2")
    c.setFont("Helvetica", 11)
    c.drawString(50, 700, "Resolution Number: GBR-DEL-2024-08")
    c.drawString(50, 675, "Date of Meeting: 2024-03-25")
    c.drawString(50, 650, "Institution: Delhi Institute of Technology")
    c.drawString(50, 620, "Resolved that the institution shall apply for AICTE Extension of Approval for 2024-25.")
    c.drawString(50, 590, "Resolved further that Dr. Rajesh Sharma is authorized to submit all compliance forms.")
    c.showPage()
    # Page 2
    c.setFont("Helvetica-Bold", 14)
    c.drawCentredString(300, 750, "SIGNATORIES AND VERIFICATION - PAGE 2 OF 2")
    c.setFont("Helvetica", 11)
    c.drawString(50, 700, "Signed by all members of the Governing Body on 2024-03-25:")
    c.drawString(50, 660, "1. Dr. Rajesh Sharma - Member Secretary")
    c.drawString(50, 630, "2. Prof. S. K. Gupta - AICTE Nominee")
    c.drawString(50, 600, "3. Smt. Anita Verma - State Government Nominee")
    c.drawString(50, 550, "Resolution Ratified Unanimously.")
    c.save()

    ground_truth["doc_6_multipage_board_resolution.pdf"] = {
        "document_type": "BOARD_RESOLUTION",
        "certificate_number": "GBR-DEL-2024-08",
        "meeting_date": "2024-03-25",
        "institution_name": "Delhi Institute of Technology",
        "pages_count": 2,
        "expected_lines_contain": ["GBR-DEL-2024-08", "AICTE Nominee", "State Government Nominee"]
    }

def create_corrupt_file():
    filepath = os.path.join(CORPUS_DIR, "doc_7_corrupt.pdf")
    with open(filepath, "wb") as f:
        f.write(b"NOT_A_VALID_PDF_HEADER_JUST_GARBAGE_BYTES_123456789")

if __name__ == "__main__":
    create_fire_safety_pdf()
    create_building_occupancy_pdf()
    create_land_table_pdf()
    create_anti_ragging_pdf()
    create_scanned_skewed_image()
    create_multipage_pdf()
    create_corrupt_file()
    
    with open(os.path.join(CORPUS_DIR, "ground_truth.json"), "w") as f:
        json.dump(ground_truth, f, indent=2)
    print("Corpus generated successfully with 7 documents and ground_truth.json!")
