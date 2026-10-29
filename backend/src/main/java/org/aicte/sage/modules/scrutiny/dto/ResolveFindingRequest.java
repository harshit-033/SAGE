package org.aicte.sage.modules.scrutiny.dto;

import jakarta.validation.constraints.NotBlank;

public class ResolveFindingRequest {

    @NotBlank(message = "Resolution notes are required for statutory officer override or verification")
    private String resolutionNotes;

    public ResolveFindingRequest() {}

    public ResolveFindingRequest(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}
