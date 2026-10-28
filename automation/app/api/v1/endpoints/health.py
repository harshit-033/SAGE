from fastapi import APIRouter, status
from datetime import datetime, timezone
import httpx
from app.core.config import settings

router = APIRouter(prefix="/health", tags=["System Health"])


@router.get("", status_code=status.HTTP_200_OK)
async def liveness_probe():
    """
    Liveness probe verifying that the Python Automation service process is responsive.
    """
    return {
        "status": "UP",
        "service": "sage-automation",
        "version": settings.APP_VERSION,
        "environment": settings.ENVIRONMENT,
        "timestamp": datetime.now(timezone.utc).isoformat()
    }


@router.get("/ready", status_code=status.HTTP_200_OK)
async def readiness_probe():
    """
    Readiness probe verifying that the automation engine can communicate with the backend.
    """
    backend_status = "UNKNOWN"
    try:
        async with httpx.AsyncClient(timeout=2.0) as client:
            resp = await client.get(f"{settings.BACKEND_SERVICE_URL}/api/v1/health")
            backend_status = "UP" if resp.status_code == 200 else "DEGRADED"
    except Exception:
        backend_status = "UNREACHABLE"

    return {
        "status": "UP",
        "service": "sage-automation",
        "backend": {
            "url": settings.BACKEND_SERVICE_URL,
            "status": backend_status
        },
        "timestamp": datetime.now(timezone.utc).isoformat()
    }
