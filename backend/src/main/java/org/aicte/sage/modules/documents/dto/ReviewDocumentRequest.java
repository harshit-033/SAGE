package org.aicte.sage.modules.documents.dto;

import jakarta.validation.constraints.NotNull;
import org.aicte.sage.modules.documents.model.DocumentComplianceStatus;
import org.aicte.sage.modules.documents.model.DocumentVerificationStatus;

public record ReviewDocumentRequest(
        @NotNull(message = "Verification status is required")
        DocumentVerificationStatus verificationStatus,

        DocumentComplianceStatus complianceStatus,

        String officerComments
) {}
