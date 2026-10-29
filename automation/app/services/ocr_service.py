import os
import re
import time
import uuid
import logging
from typing import Dict, Any, List, Optional, Tuple
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)

class BoundingBox(BaseModel):
    x_min: float
    y_min: float
    x_max: float
    y_max: float

class ExtractedField(BaseModel):
    field_key: str
    extracted_value: str
    normalized_value: Optional[str] = None
    confidence_score: float = Field(default=1.0, ge=0.0, le=1.0)
    is_uncertain: bool = False
    source_page: int = 1
    bounding_box: Optional[List[float]] = None
    raw_text_snippet: Optional[str] = None

class OcrPageResult(BaseModel):
    page_number: int
    text: str
    markdown: Optional[str] = None
    rotation: int = 0
    skew: float = 0.0
    lines_count: int = 0
    avg_confidence: float = 1.0

class OcrProcessResult(BaseModel):
    success: bool
    engine: str
    document_id: Optional[str] = None
    document_type: str
    pages_count: int = 0
    pages: List[OcrPageResult] = []
    extracted_fields: Dict[str, ExtractedField] = {}
    overall_confidence: float = 1.0
    is_uncertain: bool = False
    processing_time_ms: float = 0.0
    detected_deficiencies: List[str] = []
    error_message: Optional[str] = None

class BaseOcrAdapter:
    def process_file(self, file_path: str) -> Tuple[List[Dict[str, Any]], str]:
        raise NotImplementedError

class IndraOcrAdapter(BaseOcrAdapter):
    def __init__(self, threads: int = 2):
        self.threads = threads
        self._converter = None

    def _get_converter(self):
        if self._converter is None:
            import indra_ocr
            self._converter = indra_ocr.load(threads=self.threads)
        return self._converter

    def process_file(self, file_path: str) -> Tuple[List[Dict[str, Any]], str]:
        conv = self._get_converter()
        raw_pages = list(conv.convert(file_path))
        page_results = []
        for p_idx, page in raw_pages:
            lines_data = []
            for line in getattr(page, "lines", []):
                lines_data.append({
                    "text": line.text,
                    "box": list(line.box) if hasattr(line, "box") else None,
                    "conf": float(line.conf) if (hasattr(line, "conf") and line.conf is not None) else 1.0
                })
            page_results.append({
                "page_number": p_idx + 1,
                "text": getattr(page, "text", "") or "",
                "markdown": getattr(page, "markdown", "") or "",
                "rotation": int(getattr(page, "rotation", 0) or 0),
                "skew": float(getattr(page, "skew", 0.0) or 0.0),
                "lines": lines_data
            })
        return page_results, "INDRA_OCR"

class TesseractOcrAdapter(BaseOcrAdapter):
    def _process_image_with_tesseract(self, pil_image_or_cv2) -> Tuple[str, List[Dict[str, Any]]]:
        import pytesseract
        data = pytesseract.image_to_data(pil_image_or_cv2, output_type=pytesseract.Output.DICT)

        lines_dict: Dict[Tuple[int, int, int], Dict[str, Any]] = {}
        for i in range(len(data["text"])):
            w_text = data["text"][i].strip()
            conf = float(data["conf"][i])
            if conf < 0:
                continue
            if not w_text:
                continue
            key = (int(data["block_num"][i]), int(data["par_num"][i]), int(data["line_num"][i]))
            if key not in lines_dict:
                lines_dict[key] = {
                    "words": [],
                    "confs": [],
                    "left": int(data["left"][i]),
                    "top": int(data["top"][i]),
                    "right": int(data["left"][i]) + int(data["width"][i]),
                    "bottom": int(data["top"][i]) + int(data["height"][i])
                }
            lines_dict[key]["words"].append(w_text)
            lines_dict[key]["confs"].append(conf / 100.0)
            lines_dict[key]["right"] = max(lines_dict[key]["right"], int(data["left"][i]) + int(data["width"][i]))
            lines_dict[key]["bottom"] = max(lines_dict[key]["bottom"], int(data["top"][i]) + int(data["height"][i]))
            lines_dict[key]["left"] = min(lines_dict[key]["left"], int(data["left"][i]))
            lines_dict[key]["top"] = min(lines_dict[key]["top"], int(data["top"][i]))

        lines = []
        full_text_lines = []
        for key in sorted(lines_dict.keys()):
            item = lines_dict[key]
            line_str = " ".join(item["words"])
            full_text_lines.append(line_str)
            line_conf = round(sum(item["confs"]) / len(item["confs"]), 3) if item["confs"] else 0.0
            box = [item["left"], item["top"], item["right"], item["bottom"]]
            lines.append({
                "text": line_str,
                "box": box,
                "conf": line_conf
            })

        text_result = "\n".join(full_text_lines)
        return text_result, lines

    def process_file(self, file_path: str) -> Tuple[List[Dict[str, Any]], str]:
        import pypdfium2
        import cv2

        page_results = []
        if file_path.lower().endswith(".pdf"):
            pdf = pypdfium2.PdfDocument(file_path)
            for p_idx in range(len(pdf)):
                page = pdf[p_idx]
                pil_image = page.render(scale=2).to_pil()
                text, lines = self._process_image_with_tesseract(pil_image)
                page_results.append({
                    "page_number": p_idx + 1,
                    "text": text,
                    "markdown": text,
                    "rotation": 0,
                    "skew": 0.0,
                    "lines": lines
                })
        else:
            img = cv2.imread(file_path)
            if img is None:
                raise ValueError("Could not read image file")
            text, lines = self._process_image_with_tesseract(img)
            page_results.append({
                "page_number": 1,
                "text": text,
                "markdown": text,
                "rotation": 0,
                "skew": 0.0,
                "lines": lines
            })
        return page_results, "TESSERACT_FALLBACK"

class DocumentIntelligenceEngine:
    def __init__(self):
        self.indra_adapter = IndraOcrAdapter(threads=2)
        self.tesseract_adapter = TesseractOcrAdapter()

    def _normalize_date(self, raw_date_str: str) -> Optional[str]:
        if not raw_date_str:
            return None
        cleaned = raw_date_str.strip().replace(".", "-").replace("/", "-")
        # Match YYYY-MM-DD
        m = re.search(r"(\d{4})-(\d{1,2})-(\d{1,2})", cleaned)
        if m:
            y, mth, d = m.groups()
            return f"{y}-{int(mth):02d}-{int(d):02d}"
        # Match DD-MM-YYYY
        m2 = re.search(r"(\d{1,2})-(\d{1,2})-(\d{4})", cleaned)
        if m2:
            d, mth, y = m2.groups()
            return f"{y}-{int(mth):02d}-{int(d):02d}"
        return cleaned

    def extract_structured_fields(self, pages_data: List[Dict[str, Any]], doc_type: str) -> Dict[str, ExtractedField]:
        fields: Dict[str, ExtractedField] = {}
        all_lines: List[Tuple[int, Dict[str, Any]]] = []
        full_text = ""
        for p in pages_data:
            p_num = p["page_number"]
            full_text += f"\n--- Page {p_num} ---\n" + p["text"]
            for l in p.get("lines", []):
                all_lines.append((p_num, l))

        # Certificate / Registration Number extraction patterns
        cert_no_patterns = [
            r"(?:Certificate\s+(?:Number|No|NOC\s+No)[:\s]+)([A-Za-z0-9\-_/]+)",
            r"(?:Title\s+Deed\s+Number[:\s]+)([A-Za-z0-9\-_/]+)",
            r"(?:Document\s+Number[:\s]+)([A-Za-z0-9\-_/]+)",
            r"(?:Resolution\s+Number[:\s]+)([A-Za-z0-9\-_/]+)",
            r"(?:Ref\s+No[:\s]+)([A-Za-z0-9\-_/]+)",
            r"(?:Number[:\s]+)([A-Z]{2,4}-[A-Z]{2,4}-[0-9]{4}-[0-9]{4})"
        ]
        for p_num, line in all_lines:
            text = line["text"]
            for pat in cert_no_patterns:
                m = re.search(pat, text, re.IGNORECASE)
                if m and "certificate_number" not in fields:
                    val = m.group(1).strip()
                    fields["certificate_number"] = ExtractedField(
                        field_key="certificate_number",
                        extracted_value=val,
                        normalized_value=val,
                        confidence_score=line.get("conf", 1.0),
                        source_page=p_num,
                        bounding_box=line.get("box"),
                        raw_text_snippet=text
                    )
                    break

        # Issuing Authority patterns
        auth_patterns = [
            r"(?:Issuing\s+Authority[:\s]+)(.+)",
            r"(HEADQUARTERS[:\s]+DELHI\s+FIRE\s+SERVICE[^\n]*)",
            r"(MUNICIPAL\s+CORPORATION\s+OF\s+GREATER\s+MUMBAI)",
            r"(REGISTRATION\s+AND\s+STAMPS\s+DEPARTMENT[^\n]*)",
            r"(OFFICE\s+OF\s+THE\s+CHIEF\s+FIRE\s+OFFICER[^\n]*)"
        ]
        for p_num, line in all_lines:
            text = line["text"]
            for pat in auth_patterns:
                m = re.search(pat, text, re.IGNORECASE)
                if m and "issuing_authority" not in fields:
                    val = m.group(1).strip()
                    fields["issuing_authority"] = ExtractedField(
                        field_key="issuing_authority",
                        extracted_value=val,
                        normalized_value=val,
                        confidence_score=line.get("conf", 1.0),
                        source_page=p_num,
                        bounding_box=line.get("box"),
                        raw_text_snippet=text
                    )
                    break

        # Issue Date patterns
        issue_date_patterns = [
            r"(?:Issue\s+Date|Date\s+of\s+Issue|Registration\s+Date|Date\s+of\s+Meeting|Execution\s+Date)[:\s]+(\d{1,4}[/\-\.]\d{1,2}[/\-\.]\d{2,4})",
            r"(?:Date[:\s]+)(\d{1,4}[/\-\.]\d{1,2}[/\-\.]\d{2,4})"
        ]
        for p_num, line in all_lines:
            text = line["text"]
            for pat in issue_date_patterns:
                m = re.search(pat, text, re.IGNORECASE)
                if m and "issue_date" not in fields:
                    raw_val = m.group(1).strip()
                    fields["issue_date"] = ExtractedField(
                        field_key="issue_date",
                        extracted_value=raw_val,
                        normalized_value=self._normalize_date(raw_val),
                        confidence_score=line.get("conf", 1.0),
                        source_page=p_num,
                        bounding_box=line.get("box"),
                        raw_text_snippet=text
                    )
                    break

        # Expiry / Valid Until patterns
        expiry_patterns = [
            r"(?:Valid\s+Until|Expiry\s+Date|Valid\s+Up\s+To)[:\s]+(\d{1,4}[/\-\.]\d{1,2}[/\-\.]\d{2,4})"
        ]
        for p_num, line in all_lines:
            text = line["text"]
            for pat in expiry_patterns:
                m = re.search(pat, text, re.IGNORECASE)
                if m and "expiry_date" not in fields:
                    raw_val = m.group(1).strip()
                    fields["expiry_date"] = ExtractedField(
                        field_key="expiry_date",
                        extracted_value=raw_val,
                        normalized_value=self._normalize_date(raw_val),
                        confidence_score=line.get("conf", 1.0),
                        source_page=p_num,
                        bounding_box=line.get("box"),
                        raw_text_snippet=text
                    )
                    break

        # Institution Name patterns
        inst_patterns = [
            r"(?:Institution\s+Name[:\s]+)(.+)",
            r"(?:Certified\s+that\s+)([A-Za-z\s]+)(?:comprised|has)",
            r"(?:erected\s+by\s+)([A-Za-z\s]+)(?:at\s+Survey)",
            r"(?:favor\s+of[:\s]+)([A-Za-z\s]+)(?:\.\s+Registration)",
            r"(?:institution\s+)([A-Za-z\s]+)(?:strictly\s+enforces)"
        ]
        for p_num, line in all_lines:
            text = line["text"]
            for pat in inst_patterns:
                m = re.search(pat, text, re.IGNORECASE)
                if m and "institution_name" not in fields:
                    val = m.group(1).strip()
                    fields["institution_name"] = ExtractedField(
                        field_key="institution_name",
                        extracted_value=val,
                        normalized_value=val,
                        confidence_score=line.get("conf", 1.0),
                        source_page=p_num,
                        bounding_box=line.get("box"),
                        raw_text_snippet=text
                    )
                    break

        # Land area / Carpet area patterns
        area_patterns = [
            r"(\d+(?:\.\d+)?)\s*(?:Acres|Acre)",
            r"(\d+(?:\.\d+)?)\s*(?:Sq\.\s*Meters|Sq\.M|Sqm)"
        ]
        for p_num, line in all_lines:
            text = line["text"]
            m_acre = re.search(r"(\d+(?:\.\d+)?)\s*(?:Acres|Acre)", text, re.IGNORECASE)
            if m_acre and "land_area_acres" not in fields:
                val = m_acre.group(1)
                fields["land_area_acres"] = ExtractedField(
                    field_key="land_area_acres",
                    extracted_value=val,
                    normalized_value=val,
                    confidence_score=line.get("conf", 1.0),
                    source_page=p_num,
                    bounding_box=line.get("box"),
                    raw_text_snippet=text
                )
            m_built = re.search(r"(\d+(?:\.\d+)?)\s*(?:Sq\.\s*Meters|Sq\.M|Sqm)", text, re.IGNORECASE)
            if m_built and "built_up_area_sqm" not in fields:
                val = m_built.group(1)
                fields["built_up_area_sqm"] = ExtractedField(
                    field_key="built_up_area_sqm",
                    extracted_value=val,
                    normalized_value=val,
                    confidence_score=line.get("conf", 1.0),
                    source_page=p_num,
                    bounding_box=line.get("box"),
                    raw_text_snippet=text
                )

        # Deponent / Signatory
        deponent_patterns = [
            r"(?:I,\s+)(Dr\.\s+[A-Za-z\s]+|Prof\.\s+[A-Za-z\s]+|Shri\s+[A-Za-z\s]+)",
            r"(?:Deponent[:\s]+)(.+)"
        ]
        for p_num, line in all_lines:
            text = line["text"]
            for pat in deponent_patterns:
                m = re.search(pat, text, re.IGNORECASE)
                if m and "deponent_name" not in fields:
                    val = m.group(1).strip()
                    fields["deponent_name"] = ExtractedField(
                        field_key="deponent_name",
                        extracted_value=val,
                        normalized_value=val,
                        confidence_score=line.get("conf", 1.0),
                        source_page=p_num,
                        bounding_box=line.get("box"),
                        raw_text_snippet=text
                    )
                    break

        # Meeting Date patterns (Board Resolution)
        meeting_patterns = [
            r"(?:Date\s+of\s+Meeting|Meeting\s+Date)[:\s]+(\d{1,4}[/\-\.]\d{1,2}[/\-\.]\d{2,4})"
        ]
        for p_num, line in all_lines:
            text = line["text"]
            for pat in meeting_patterns:
                m = re.search(pat, text, re.IGNORECASE)
                if m and "meeting_date" not in fields:
                    raw_val = m.group(1).strip()
                    fields["meeting_date"] = ExtractedField(
                        field_key="meeting_date",
                        extracted_value=raw_val,
                        normalized_value=self._normalize_date(raw_val),
                        confidence_score=line.get("conf", 1.0),
                        source_page=p_num,
                        bounding_box=line.get("box"),
                        raw_text_snippet=text
                    )
                    break

        # Multiline / wrapped text fallbacks across page text
        if "certificate_number" not in fields:
            m_cn = re.search(r"(?:Certificate\s+Number|Title\s+Deed\s+Number|Resolution\s+Number|Document\s+Number)[:\s]+([A-Za-z0-9\-_/]+)", full_text, re.IGNORECASE)
            if m_cn:
                val = m_cn.group(1).strip()
                fields["certificate_number"] = ExtractedField(
                    field_key="certificate_number",
                    extracted_value=val,
                    normalized_value=val,
                    confidence_score=0.95,
                    source_page=1,
                    raw_text_snippet=m_cn.group(0)
                )

        if "issuing_authority" not in fields:
            m_ia = re.search(r"(?:Issuing\s+Authority[:\s]+)([^.\n]+)", full_text, re.IGNORECASE)
            if m_ia:
                val = m_ia.group(1).strip()
                fields["issuing_authority"] = ExtractedField(
                    field_key="issuing_authority",
                    extracted_value=val,
                    normalized_value=val,
                    confidence_score=0.95,
                    source_page=1,
                    raw_text_snippet=m_ia.group(0)
                )

        if "institution_name" not in fields:
            m_in = re.search(r"(?:Institution\s+Name|Institution)[:\s]+([^.\n]+)", full_text, re.IGNORECASE)
            if not m_in:
                m_in = re.search(r"(?:Registered\s+in\s+favor\s+of[:\s]+)([\s\S]+?)\.\s*Registration", full_text, re.IGNORECASE)
            if m_in:
                val = " ".join(m_in.group(1).split()).strip()
                fields["institution_name"] = ExtractedField(
                    field_key="institution_name",
                    extracted_value=val,
                    normalized_value=val,
                    confidence_score=0.95,
                    source_page=1,
                    raw_text_snippet=m_in.group(0)
                )

        if "land_area_acres" not in fields:
            m_la = re.search(r"(\d+(?:\.\d+)?)\s*(?:Acres|Acre)", full_text, re.IGNORECASE)
            if m_la:
                val = m_la.group(1).strip()
                fields["land_area_acres"] = ExtractedField(
                    field_key="land_area_acres",
                    extracted_value=val,
                    normalized_value=val,
                    confidence_score=0.95,
                    source_page=1,
                    raw_text_snippet=m_la.group(0)
                )

        if "meeting_date" not in fields:
            m_md = re.search(r"(?:Date\s+of\s+Meeting|Meeting\s+Date)[:\s]+(\d{1,4}[/\-\.]\d{1,2}[/\-\.]\d{2,4})", full_text, re.IGNORECASE)
            if m_md:
                raw_val = m_md.group(1).strip()
                fields["meeting_date"] = ExtractedField(
                    field_key="meeting_date",
                    extracted_value=raw_val,
                    normalized_value=self._normalize_date(raw_val),
                    confidence_score=0.95,
                    source_page=1,
                    raw_text_snippet=m_md.group(0)
                )

        return fields

    def process(self, file_path: str, document_type: str, document_id: Optional[str] = None) -> OcrProcessResult:
        t0 = time.time()
        if not os.path.exists(file_path):
            return OcrProcessResult(
                success=False,
                engine="NONE",
                document_id=document_id,
                document_type=document_type,
                error_message=f"File not found: {file_path}",
                processing_time_ms=0.0
            )

        # Attempt Indra OCR first, fallback to Tesseract if fails
        engine_used = "INDRA_OCR"
        try:
            pages_data, engine_used = self.indra_adapter.process_file(file_path)
        except Exception as e:
            logger.warning(f"Indra OCR failed for {file_path}: {e}. Falling back to Tesseract.")
            try:
                pages_data, engine_used = self.tesseract_adapter.process_file(file_path)
            except Exception as e2:
                elapsed_ms = round((time.time() - t0) * 1000, 2)
                return OcrProcessResult(
                    success=False,
                    engine="FAILED",
                    document_id=document_id,
                    document_type=document_type,
                    error_message=f"Both OCR engines failed: Indra: {e}; Tesseract: {e2}",
                    processing_time_ms=elapsed_ms
                )

        elapsed_ms = round((time.time() - t0) * 1000, 2)
        extracted = self.extract_structured_fields(pages_data, document_type)

        page_objs = []
        all_confs = []
        for p in pages_data:
            lines = p.get("lines", [])
            confs = [l.get("conf", 1.0) for l in lines if l.get("conf") is not None]
            avg_c = round(sum(confs) / len(confs), 3) if confs else 1.0
            all_confs.extend(confs)
            page_objs.append(OcrPageResult(
                page_number=p["page_number"],
                text=p["text"],
                markdown=p.get("markdown"),
                rotation=p.get("rotation", 0),
                skew=p.get("skew", 0.0),
                lines_count=len(lines),
                avg_confidence=avg_c
            ))

        overall_conf = round(sum(all_confs) / len(all_confs), 3) if all_confs else 1.0
        is_uncertain = overall_conf < 0.70 or ("certificate_number" not in extracted)

        deficiencies = []
        if is_uncertain:
            deficiencies.append("LOW_CONFIDENCE_OR_MISSING_CERTIFICATE_NUMBER")
        if document_type == "FIRE_SAFETY_CERTIFICATE" and "expiry_date" not in extracted:
            deficiencies.append("FIRE_SAFETY_EXPIRY_DATE_NOT_EXTRACTED")

        return OcrProcessResult(
            success=True,
            engine=engine_used,
            document_id=document_id,
            document_type=document_type,
            pages_count=len(page_objs),
            pages=page_objs,
            extracted_fields=extracted,
            overall_confidence=overall_conf,
            is_uncertain=is_uncertain,
            processing_time_ms=elapsed_ms,
            detected_deficiencies=deficiencies
        )

# Global singleton
ocr_engine = DocumentIntelligenceEngine()
