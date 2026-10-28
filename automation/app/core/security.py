from fastapi import Header, HTTPException, status
from app.core.config import settings


async def verify_internal_service_token(
    x_internal_service_token: str = Header(..., description="Internal inter-service security token")
) -> str:
    """
    Enforces inter-service machine authentication between Spring Boot and FastAPI.
    Fails closed if the token does not match the configured secret.
    """
    if x_internal_service_token != settings.INTERNAL_SERVICE_TOKEN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Forbidden: Invalid internal service token"
        )
    return x_internal_service_token
