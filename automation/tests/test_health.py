import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app


@pytest.mark.asyncio
async def test_liveness_probe():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/api/v1/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "UP"
        assert data["service"] == "sage-automation"
        assert "X-Correlation-ID" in response.headers


@pytest.mark.asyncio
async def test_readiness_probe():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/api/v1/health/ready")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "UP"
        assert "backend" in data


@pytest.mark.asyncio
async def test_correlation_id_echo():
    test_id = "test-corr-id-12345"
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/api/v1/health", headers={"X-Correlation-ID": test_id})
        assert response.status_code == 200
        assert response.headers.get("X-Correlation-ID") == test_id
