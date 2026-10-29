package org.aicte.sage.modules.notifications.repository;

import org.aicte.sage.modules.notifications.entity.AlertDelivery;
import org.aicte.sage.modules.notifications.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlertDeliveryRepository extends JpaRepository<AlertDelivery, UUID> {

    List<AlertDelivery> findByAlertId(UUID alertId);

    @Query("SELECT d FROM AlertDelivery d WHERE d.deliveryStatus = :status AND d.nextRetryAt <= :cutoff")
    List<AlertDelivery> findPendingRetries(
            @Param("status") DeliveryStatus status,
            @Param("cutoff") Instant cutoff);
}
