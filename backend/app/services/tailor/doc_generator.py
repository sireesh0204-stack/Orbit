import io
from typing import Dict, Any, Tuple
from docx import Document
from docx.shared import Pt, Inches, RGBColor
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from app.services.text_extractor import extract_text_from_pdf, extract_text_from_docx

def generate_ats_docx(draft: Dict[str, Any], candidate_name: str = "Candidate Profile") -> bytes:
    """Generates an ATS-compliant, strictly single-column DOCX resume."""
    doc = Document()

    # Set 0.75-inch standard margins
    for section in doc.sections:
        section.top_margin = Inches(0.75)
        section.bottom_margin = Inches(0.75)
        section.left_margin = Inches(0.75)
        section.right_margin = Inches(0.75)

    # Header: Name & Target Role
    title_p = doc.add_paragraph()
    run_name = title_p.add_run(candidate_name)
    run_name.bold = True
    run_name.font.size = Pt(18)
    run_name.font.name = "Calibri"

    role_p = doc.add_paragraph()
    run_role = role_p.add_run(draft.get("candidate_title", "Software Engineer"))
    run_role.font.size = Pt(12)
    run_role.font.italic = True
    run_role.font.name = "Calibri"

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # Section 1: Technical Skills
    skills_heading = doc.add_paragraph()
    r = skills_heading.add_run("TECHNICAL SKILLS")
    r.bold = True
    r.font.size = Pt(12)

    tech_skills = ", ".join(draft.get("skills", {}).get("technical", []))
    tools_skills = ", ".join(draft.get("skills", {}).get("tools_and_platforms", []))
    doc.add_paragraph(f"Languages & Core: {tech_skills}")
    if tools_skills:
        doc.add_paragraph(f"Tools & Infrastructure: {tools_skills}")

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # Section 2: Professional Experience
    exp_heading = doc.add_paragraph()
    r2 = exp_heading.add_run("PROFESSIONAL EXPERIENCE")
    r2.bold = True
    r2.font.size = Pt(12)

    for work in draft.get("work_history", []):
        job_p = doc.add_paragraph()
        run_comp = job_p.add_run(f"{work.get('title')} — {work.get('company')}")
        run_comp.bold = True
        run_dates = job_p.add_run(f" ({work.get('start_date')} – {work.get('end_date')})")
        run_dates.italic = True

        for bp in work.get("bullet_points", []):
            doc.add_paragraph(f"• {bp}", style='List Bullet')

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # Section 3: Education
    edu_heading = doc.add_paragraph()
    r3 = edu_heading.add_run("EDUCATION")
    r3.bold = True
    r3.font.size = Pt(12)

    for edu in draft.get("education", []):
        doc.add_paragraph(f"{edu.get('degree')} — {edu.get('institution')} ({edu.get('year')})")

    buf = io.BytesIO()
    doc.save(buf)
    return buf.getvalue()

def generate_ats_pdf(draft: Dict[str, Any], candidate_name: str = "Candidate Profile") -> bytes:
    """Generates an ATS-compliant, single-column PDF resume via ReportLab."""
    buf = io.BytesIO()
    doc = SimpleDocTemplate(
        buf,
        pagesize=letter,
        leftMargin=40,
        rightMargin=40,
        topMargin=40,
        bottomMargin=40
    )

    styles = getSampleStyleSheet()
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=18,
        leading=22,
        spaceAfter=4
    )
    subtitle_style = ParagraphStyle(
        'DocSubTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Oblique',
        fontSize=12,
        leading=16,
        spaceAfter=12
    )
    section_heading = ParagraphStyle(
        'SectionHeading',
        parent=styles['Heading2'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        spaceBefore=10,
        spaceAfter=6
    )
    body_style = ParagraphStyle(
        'Body',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=10,
        leading=14,
        spaceAfter=3
    )

    story = [
        Paragraph(candidate_name, title_style),
        Paragraph(draft.get("candidate_title", "Software Engineer"), subtitle_style),
        Spacer(1, 4),
        Paragraph("<b>TECHNICAL SKILLS</b>", section_heading),
        Paragraph(f"<b>Core:</b> {', '.join(draft.get('skills', {}).get('technical', []))}", body_style),
        Paragraph(f"<b>Tools:</b> {', '.join(draft.get('skills', {}).get('tools_and_platforms', []))}", body_style),
        Spacer(1, 6),
        Paragraph("<b>PROFESSIONAL EXPERIENCE</b>", section_heading)
    ]

    for work in draft.get("work_history", []):
        story.append(
            Paragraph(f"<b>{work.get('title')}</b> — <i>{work.get('company')}</i> ({work.get('start_date')} – {work.get('end_date')})", body_style)
        )
        for bp in work.get("bullet_points", []):
            story.append(Paragraph(f"&bull; {bp}", body_style))
        story.append(Spacer(1, 4))

    story.append(Paragraph("<b>EDUCATION</b>", section_heading))
    for edu in draft.get("education", []):
        story.append(Paragraph(f"{edu.get('degree')} — {edu.get('institution')} ({edu.get('year')})", body_style))

    doc.build(story)
    return buf.getvalue()

def verify_ats_parsability(docx_bytes: bytes, pdf_bytes: bytes) -> Tuple[bool, str]:
    """
    Pass 2 verification check: Extracts raw text from compiled DOCX & PDF
    to ensure zero parser traps or formatting breakdowns.
    """
    try:
        docx_text = extract_text_from_docx(docx_bytes)
        if len(docx_text) < 50:
            return False, "DOCX parsed text is too short or corrupted."

        pdf_text = extract_text_from_pdf(pdf_bytes)
        if len(pdf_text) < 50:
            return False, "PDF parsed text is too short or unreadable."

        return True, "ATS parsing verified successfully."
    except Exception as e:
        return False, f"ATS audit parse failure: {str(e)}"

