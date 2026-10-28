from typing import Protocol, List
from abc import abstractmethod
from pydantic import BaseModel
from uuid import UUID


class NotificationMessage(BaseModel):
    recipient_id: UUID
    recipient_email: str
    recipient_phone: str
    channel: str  # EMAIL, SMS, IN_APP
    template_code: str
    template_params: dict


class NotificationDispatchResult(BaseModel):
    message_id: str
    channel: str
    delivered: bool
    error_reason: str = None


class NotificationDispatcher(Protocol):
    """
    Interface for dispatching multi-channel statutory alerts.
    """
    @abstractmethod
    async def dispatch_notification(self, message: NotificationMessage) -> NotificationDispatchResult:
        """Send notification via designated gateway channel."""
        ...
