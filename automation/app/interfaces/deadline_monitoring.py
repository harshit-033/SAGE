from typing import Protocol, List
from abc import abstractmethod
from pydantic import BaseModel
from datetime import datetime
from uuid import UUID


class DeadlineItem(BaseModel):
    institution_id: UUID
    milestone_name: str
    statutory_deadline: datetime
    alert_level: str  # T_MINUS_30, T_MINUS_15, T_MINUS_7, T_MINUS_1
    is_overdue: bool


class DeadlineMonitoringService(Protocol):
    """
    Interface for regulatory deadline calculation and escalation monitoring.
    """
    @abstractmethod
    async def evaluate_deadlines(self, reference_time: datetime) -> List[DeadlineItem]:
        """Compute upcoming statutory deadlines and overdue applications."""
        ...
