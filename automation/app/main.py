import uuid
from datetime import datetime, timezone
from fastapi import FastAPI, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from app.api.v1.router import api_router
from app.core.config import settings

app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="SAGE Automation, Document Intelligence & EVC Optimization Engine",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Restrictive CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.get_cors_origins(),
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    allow_headers=["*"],
    expose_headers=["X-Correlation-ID"]
)


@app.middleware("http")
async def correlation_id_middleware(request: Request, call_next):
    correlation_id = request.headers.get("X-Correlation-ID") or str(uuid.uuid4())
    request.state.correlation_id = correlation_id
    response = await call_next(request)
    response.headers["X-Correlation-ID"] = correlation_id
    return response


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    correlation_id = getattr(request.state, "correlation_id", str(uuid.uuid4()))
    errors = []
    for err in exc.errors():
        loc = ".".join(str(p) for p in err.get("loc", []))
        errors.append({
            "field": loc,
            "message": err.get("msg", "Validation error"),
            "type": err.get("type", "value_error")
        })

    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "type": "https://api.aicte.gov.in/errors/validation-failed",
            "title": "Unprocessable Entity",
            "status": 422,
            "detail": "Request payload validation failed",
            "instance": request.url.path,
            "correlationId": correlation_id,
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "fieldErrors": errors
        }
    )


app.include_router(api_router)


@app.get("/")
async def root():
    return {
        "service": "sage-automation",
        "status": "UP",
        "docs": "/docs"
    }
