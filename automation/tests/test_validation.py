import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app
from app.core.config import settings


@pytest.mark.asyncio
async def test_validation_failure_returns_problem_details():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        # Send empty payload violating min_length constraints
        response = await client.post(
            "/api/v1/audit/ping",
            headers={"X-Internal-Service-Token": settings.INTERNAL_SERVICE_TOKEN},
            json={"caller_service": "", "message": ""}
        )
        assert response.status_code == 422
        data = response.json()
        assert data["title"] == "Unprocessable Entity"
        assert data["status"] == 422
        assert "correlationId" in data
        assert len(data["fieldErrors"]) > 0
