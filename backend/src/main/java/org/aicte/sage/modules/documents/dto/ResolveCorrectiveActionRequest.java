package org.aicte.sage.modules.documents.dto;

import org.aicte.sage.modules.documents.model.CorrectiveActionStatus;

public record ResolveCorrectiveActionRequest(
        CorrectiveActionStatus status,
        String officerNotes
) {}
