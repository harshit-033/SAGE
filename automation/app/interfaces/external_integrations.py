from typing import Protocol, Optional
from abc import abstractmethod
from pydantic import BaseModel


class ExternalInstitutionRecord(BaseModel):
    pid: str
    official_name: str
    affiliated_university: str
    approved_intake: int


class AicteMasterSyncService(Protocol):
    """
    Interface for synchronizing reference data with AICTE national databases and portals.
    """
    @abstractmethod
    async def fetch_institution_record(self, pid: str) -> Optional[ExternalInstitutionRecord]:
        """Query official AICTE registry for institutional master records."""
        ...
