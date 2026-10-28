from typing import Protocol, Any, Dict, Optional
from abc import abstractmethod
from pydantic import BaseModel
from datetime import datetime


class JobContext(BaseModel):
    job_id: str
    job_type: str
    correlation_id: str
    created_at: datetime
    payload: Dict[str, Any]


class JobResult(BaseModel):
    job_id: str
    success: bool
    result_data: Optional[Dict[str, Any]] = None
    error_message: Optional[str] = None
    completed_at: datetime


class BackgroundWorker(Protocol):
    """
    Clean interface for future background asynchronous workers.
    Concrete implementations (e.g. Celery, Redis Queue, or ARQ) will be introduced in subsequent phases.
    """
    @abstractmethod
    async def enqueue_job(self, context: JobContext) -> str:
        """Enqueue an asynchronous task for background processing."""
        ...

    @abstractmethod
    async def get_job_status(self, job_id: str) -> JobResult:
        """Retrieve current completion status of an enqueued job."""
        ...
