package org.aicte.sage.modules.documents.dto;

public record DocumentComplianceSummaryDto(
        long totalDocuments,
        long verifiedDocuments,
        long pendingReviewDocuments,
        long expiringSoonDocuments,
        long expiredDocuments,
        long deficientDocuments,
        long pendingActionsCount
) {}
