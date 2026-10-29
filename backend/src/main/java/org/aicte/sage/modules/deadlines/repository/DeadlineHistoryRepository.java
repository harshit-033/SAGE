package org.aicte.sage.modules.deadlines.repository;

import org.aicte.sage.modules.deadlines.entity.DeadlineHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeadlineHistoryRepository extends JpaRepository<DeadlineHistory, UUID> {
    List<DeadlineHistory> findByDeadlineIdOrderByCreatedAtDesc(UUID deadlineId);
}
