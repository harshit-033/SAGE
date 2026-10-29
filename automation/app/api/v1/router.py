from fastapi import APIRouter
from app.api.v1.endpoints import health, audit, ocr

api_router = APIRouter(prefix="/api/v1")
api_router.include_router(health.router)
api_router.include_router(audit.router)
api_router.include_router(ocr.router)

