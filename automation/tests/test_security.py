import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app
from app.core.config import settings


@pytest.mark.asyncio
async def test_internal_ping_unauthenticated_rejected():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        # Missing X-Internal-Service-Token header
        response = await client.post(
            "/api/v1/audit/ping",
            json={"caller_service": "sage-backend", "message": "Health sync ping"}
        )
        assert response.status_code == 422 or response.status_code == 403


@pytest.mark.asyncio
async def test_internal_ping_invalid_token_forbidden():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.post(
            "/api/v1/audit/ping",
            headers={"X-Internal-Service-Token": "invalid_wrong_token"},
            json={"caller_service": "sage-backend", "message": "Health sync ping"}
        )
        assert response.status_code == 403
        assert response.json()["detail"] == "Forbidden: Invalid internal service token"


@pytest.mark.asyncio
async def test_internal_ping_valid_token_success():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.post(
            "/api/v1/audit/ping",
            headers={"X-Internal-Service-Token": settings.INTERNAL_SERVICE_TOKEN},
            json={"caller_service": "sage-backend", "message": "Health sync ping"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "ACKNOWLEDGED"
        assert "Echo from sage-backend: Health sync ping" in data["echo"]
