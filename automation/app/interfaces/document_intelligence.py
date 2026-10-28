from typing import Protocol, Dict, Any, List
from abc import abstractmethod
from pydantic import BaseModel
from uuid import UUID


class DocumentVerificationRequest(BaseModel):
    document_id: UUID
    document_type: str  # LAND_DOCUMENT, FIRE_SAFETY, BUILDING_COMPLETION, AFFIDAVIT
    storage_uri: str
    sha256_hash: str


class DocumentVerificationResult(BaseModel):
    document_id: UUID
    is_valid: bool
    confidence_score: float
    extracted_metadata: Dict[str, Any]
    detected_deficiencies: List[str]


class DocumentIntelligenceService(Protocol):
    """
    Interface for document parsing, OCR, and AICTE compliance validation.
    """
    @abstractmethod
    async def verify_document(self, request: DocumentVerificationRequest) -> DocumentVerificationResult:
        """Analyze and extract structured compliance entities from institutional PDFs."""
        ...
