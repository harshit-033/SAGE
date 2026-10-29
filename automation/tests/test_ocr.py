import os
import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app
from app.core.config import settings

CORPUS_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "benchmark/corpus"))

@pytest.mark.asyncio
async def test_ocr_process_by_path():
    doc_path = os.path.join(CORPUS_DIR, "doc_1_fire_safety_valid.pdf")
    assert os.path.exists(doc_path), f"Corpus doc not found: {doc_path}"

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post(
            "/api/v1/ocr/process",
            json={
                "file_path": doc_path,
                "document_type": "FIRE_SAFETY_CERTIFICATE",
                "document_id": "test-doc-001"
            },
            headers={"X-Internal-Service-Token": settings.INTERNAL_SERVICE_TOKEN}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        assert data["engine"] in ["INDRA_OCR", "TESSERACT_FALLBACK"]
        assert data["pages_count"] >= 1
        fields = data["extracted_fields"]
        assert "certificate_number" in fields
        assert any(num in fields["certificate_number"]["extracted_value"] for num in ["FS-DEL-2024-8891", "DFS/MS/2024/SZ/8891"])
        assert "issue_date" in fields
        assert "expiry_date" in fields
        assert data["processing_time_ms"] > 0

@pytest.mark.asyncio
async def test_ocr_process_unauthenticated_rejected():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post(
            "/api/v1/ocr/process",
            json={
                "file_path": "/dummy/path.pdf",
                "document_type": "FIRE_SAFETY_CERTIFICATE"
            }
        )
        assert response.status_code in (401, 403, 422)

@pytest.mark.asyncio
async def test_ocr_path_traversal_forbidden():
    """
    Arbitrary filesystem access outside authorized directories must be blocked with HTTP 403.
    """
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        for evil_path in ["/etc/passwd", "../../../../../etc/shadow", "/var/log/syslog"]:
            response = await ac.post(
                "/api/v1/ocr/process",
                json={
                    "file_path": evil_path,
                    "document_type": "FIRE_SAFETY_CERTIFICATE"
                },
                headers={"X-Internal-Service-Token": settings.INTERNAL_SERVICE_TOKEN}
            )
            assert response.status_code == 403
            assert "outside authorized storage directories" in response.json().get("detail", "")

@pytest.mark.asyncio
async def test_ocr_nonexistent_file_in_allowed_directory_returns_404():
    """
    Non-existent files inside allowed directories must return 404 Not Found.
    """
    non_existent = os.path.join(CORPUS_DIR, "non_existent_file_xyz_1234.pdf")
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post(
            "/api/v1/ocr/process",
            json={
                "file_path": non_existent,
                "document_type": "FIRE_SAFETY_CERTIFICATE"
            },
            headers={"X-Internal-Service-Token": settings.INTERNAL_SERVICE_TOKEN}
        )
        assert response.status_code == 404

@pytest.mark.asyncio
async def test_ocr_upload_and_process():
    doc_path = os.path.join(CORPUS_DIR, "doc_4_anti_ragging_affidavit.pdf")
    with open(doc_path, "rb") as f:
        file_bytes = f.read()

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post(
            "/api/v1/ocr/upload-and-process",
            data={"document_type": "AFFIDAVIT_FORMAT_1"},
            files={"file": ("affidavit.pdf", file_bytes, "application/pdf")},
            headers={"X-Internal-Service-Token": settings.INTERNAL_SERVICE_TOKEN}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["success"] is True
        fields = data["extracted_fields"]
        assert "certificate_number" in fields
        assert "AFF-AR-2024-0042" in fields["certificate_number"]["extracted_value"]
        assert "deponent_name" in fields
        assert "Dr. Rajesh Sharma" in fields["deponent_name"]["extracted_value"]

@pytest.mark.asyncio
async def test_ocr_corrupt_file_handling():
    doc_path = os.path.join(CORPUS_DIR, "doc_7_corrupt.pdf")
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post(
            "/api/v1/ocr/process",
            json={
                "file_path": doc_path,
                "document_type": "GENERAL"
            },
            headers={"X-Internal-Service-Token": settings.INTERNAL_SERVICE_TOKEN}
        )
        assert response.status_code == 400
        data = response.json()
        assert "detail" in data

@pytest.mark.asyncio
async def test_ocr_benchmark_endpoint():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get(
            "/api/v1/ocr/benchmark",
            headers={"X-Internal-Service-Token": settings.INTERNAL_SERVICE_TOKEN}
        )
        assert response.status_code == 200
        data = response.json()
        assert "indra_ocr" in data
        assert "tesseract" in data
        assert "doc_1_fire_safety_valid.pdf" in data["indra_ocr"]
