from fastapi import APIRouter, Depends, status
from pydantic import BaseModel, Field
from datetime import datetime, timezone
from app.core.security import verify_internal_service_token

router = APIRouter(prefix="/audit", tags=["Internal Audit"])


class PingRequest(BaseModel):
    caller_service: str = Field(..., min_length=1, max_length=64)
    message: str = Field(..., min_length=1, max_length=255)


class PingResponse(BaseModel):
    echo: str
    status: str
    received_at: str


@router.post(
    "/ping",
    response_model=PingResponse,
    status_code=status.HTTP_200_OK,
    dependencies=[Depends(verify_internal_service_token)]
)
async def internal_service_ping(request: PingRequest):
    """
    Authenticated inter-service RPC check between Spring Boot and FastAPI.
    Requires valid X-Internal-Service-Token header.
    """
    return PingResponse(
        echo=f"Echo from {request.caller_service}: {request.message}",
        status="ACKNOWLEDGED",
        received_at=datetime.now(timezone.utc).isoformat()
    )
