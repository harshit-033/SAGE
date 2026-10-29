import os
import json
import shutil
import tempfile
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File, Form, Header
from pydantic import BaseModel
from app.core.security import verify_internal_service_token
from app.services.ocr_service import ocr_engine, OcrProcessResult

router = APIRouter(prefix="/ocr", tags=["Document Intelligence & OCR"])

BENCHMARK_FILE = os.path.abspath(
    os.path.join(os.path.dirname(__file__), "../../../../tests/benchmark/benchmark_results.json")
)

class OcrPathRequest(BaseModel):
    file_path: str
    document_type: str = "GENERAL"
    document_id: Optional[str] = None

@router.post("/process", response_model=OcrProcessResult)
async def process_document_by_path(
    payload: OcrPathRequest,
    authenticated: bool = Depends(verify_internal_service_token)
):
    """
    Process a local stored document by path using Indra OCR with Tesseract fallback.
    Extracts structured fields, bounding boxes, reading order, confidence scores, and uncertainty.
    """
    result = ocr_engine.process(
        file_path=payload.file_path,
        document_type=payload.document_type,
        document_id=payload.document_id
    )
    if not result.success:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=result.error_message or "OCR processing failed"
        )
    return result

@router.post("/upload-and-process", response_model=OcrProcessResult)
async def upload_and_process_document(
    document_type: str = Form(...),
    document_id: Optional[str] = Form(None),
    file: UploadFile = File(...),
    authenticated: bool = Depends(verify_internal_service_token)
):
    """
    Direct upload & OCR processing endpoint.
    Saves file to secure temporary location, runs OCR analysis, and cleans up.
    """
    allowed_extensions = {".pdf", ".png", ".jpg", ".jpeg", ".tiff", ".webp"}
    ext = os.path.splitext(file.filename or "")[1].lower()
    if ext not in allowed_extensions:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Unsupported file format '{ext}'. Allowed: {list(allowed_extensions)}"
        )

    with tempfile.NamedTemporaryFile(suffix=ext, delete=False) as tmp:
        try:
            shutil.copyfileobj(file.file, tmp)
            tmp_path = tmp.name
        finally:
            tmp.close()

    try:
        result = ocr_engine.process(
            file_path=tmp_path,
            document_type=document_type,
            document_id=document_id
        )
        return result
    finally:
        if os.path.exists(tmp_path):
            try:
                os.remove(tmp_path)
            except OSError:
                pass

@router.get("/benchmark")
async def get_ocr_benchmark_metrics(
    authenticated: bool = Depends(verify_internal_service_token)
):
    """
    Returns verified empirical benchmark comparison between Indra OCR and Tesseract baseline.
    """
    if not os.path.exists(BENCHMARK_FILE):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Benchmark metrics not yet generated"
        )
    with open(BENCHMARK_FILE, "r") as f:
        return json.load(f)
