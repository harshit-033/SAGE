package org.aicte.sage.common.audit;

import org.aicte.sage.common.api.CorrelationIdFilter;
import org.aicte.sage.modules.audit.entity.AuditEvent;
import org.aicte.sage.modules.audit.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEvent(String eventType, String principal, UUID institutionId,
                            String resourceType, String resourceId, String action,
                            String clientIp, String details) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        try {
            AuditEvent event = new AuditEvent(
                    eventType,
                    principal != null ? principal : "ANONYMOUS",
                    institutionId,
                    resourceType,
                    resourceId,
                    action,
                    clientIp,
                    correlationId,
                    details
            );
            auditEventRepository.save(event);
            log.info("Audit event recorded: [{}] principal={} resource={}:{} correlationId={}",
                    eventType, principal, resourceType, resourceId, correlationId);
        } catch (Exception e) {
            log.error("Failed to persist regulatory audit event [correlationId={}]: {}", correlationId, e.getMessage());
        }
    }
}
