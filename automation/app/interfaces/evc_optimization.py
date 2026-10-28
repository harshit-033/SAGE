from typing import Protocol, List
from abc import abstractmethod
from pydantic import BaseModel
from uuid import UUID


class ExpertCandidate(BaseModel):
    expert_id: UUID
    name: str
    discipline: str
    latitude: float
    longitude: float
    excluded_institution_ids: List[UUID]


class InspectionTarget(BaseModel):
    institution_id: UUID
    latitude: float
    longitude: float
    application_type: str


class OptimizationPlan(BaseModel):
    panel_id: UUID
    target_institution_id: UUID
    assigned_expert_ids: List[UUID]
    estimated_travel_distance_km: float


class EvcOptimizationService(Protocol):
    """
    Interface for Expert Visit Committee panel allocation and geographic routing.
    """
    @abstractmethod
    async def optimize_inspection_panels(
        self,
        targets: List[InspectionTarget],
        experts: List[ExpertCandidate]
    ) -> List[OptimizationPlan]:
        """Compute conflict-free, geographically optimal inspection panels."""
        ...
