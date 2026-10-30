package org.aicte.sage.modules.documents.dto;

import org.aicte.sage.modules.documents.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentDto {

    private UUID id;
    private UUID applicationId;
    private UUID institutionId;
    private String institutionName;
    private DocumentType documentType;
    private String documentName;
    private DocumentCategory documentCategory;
    private String originalFilename;
    private String contentType;
    private Long fileSizeBytes;
    private String sha256Hash;
    private OcrStatus ocrStatus;
    private String ocrEngine;
    private Double overallConfidence;
    private boolean uncertain;
    private Instant validFrom;
    private Instant expiryDate;
    private DocumentVerificationStatus verificationStatus;
    private DocumentComplianceStatus complianceStatus;
    private Integer versionNumber;
    private UUID previousVersionId;
    private UUID rootDocumentId;
    private String officerComments;
    private String clarificationText;
    private Integer findingsCount = 0;
    private Integer openActionsCount = 0;
    private String expiryIndicator;
    private Long daysUntilExpiry;
    private List<ExtractedFieldDto> extractedFields = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public DocumentDto() {}

    public static DocumentDto fromEntity(Document doc) {
        DocumentDto dto = new DocumentDto();
        dto.setId(doc.getId());
        if (doc.getApplication() != null) {
            dto.setApplicationId(doc.getApplication().getId());
        }
        if (doc.getInstitution() != null) {
            dto.setInstitutionId(doc.getInstitution().getId());
            dto.setInstitutionName(doc.getInstitution().getName());
        }
        dto.setDocumentType(doc.getDocumentType());
        dto.setDocumentName(doc.getDocumentName());
        dto.setDocumentCategory(doc.getDocumentCategory());
        dto.setOriginalFilename(doc.getOriginalFilename());
        dto.setContentType(doc.getContentType());
        dto.setFileSizeBytes(doc.getFileSizeBytes());
        dto.setSha256Hash(doc.getSha256Hash());
        dto.setOcrStatus(doc.getOcrStatus());
        dto.setOcrEngine(doc.getOcrEngine());
        dto.setOverallConfidence(doc.getOverallConfidence());
        dto.setUncertain(doc.isUncertain());
        dto.setValidFrom(doc.getValidFrom());
        dto.setExpiryDate(doc.getExpiryDate());
        dto.setVerificationStatus(doc.getVerificationStatus());
        dto.setComplianceStatus(doc.getComplianceStatus());
        dto.setVersionNumber(doc.getVersionNumber());
        if (doc.getPreviousVersion() != null) {
            dto.setPreviousVersionId(doc.getPreviousVersion().getId());
        }
        if (doc.getRootDocument() != null) {
            dto.setRootDocumentId(doc.getRootDocument().getId());
        } else {
            dto.setRootDocumentId(doc.getId());
        }
        dto.setOfficerComments(doc.getOfficerComments());
        dto.setClarificationText(doc.getClarificationText());

        // Compute expiry indicator and days
        if (doc.getExpiryDate() == null) {
            dto.setExpiryIndicator("NO_EXPIRY");
            dto.setDaysUntilExpiry(null);
        } else {
            Instant now = Instant.now();
            long days = Duration.between(now, doc.getExpiryDate()).toDays();
            dto.setDaysUntilExpiry(days);
            if (days < 0) {
                dto.setExpiryIndicator("EXPIRED");
            } else if (days <= 30) {
                dto.setExpiryIndicator("EXPIRING_SOON");
            } else {
                dto.setExpiryIndicator("VALID");
            }
        }

        dto.setCreatedAt(doc.getCreatedAt());
        dto.setUpdatedAt(doc.getUpdatedAt());
        return dto;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public UUID getInstitutionId() { return institutionId; }
    public void setInstitutionId(UUID institutionId) { this.institutionId = institutionId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public DocumentCategory getDocumentCategory() { return documentCategory; }
    public void setDocumentCategory(DocumentCategory documentCategory) { this.documentCategory = documentCategory; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }

    public OcrStatus getOcrStatus() { return ocrStatus; }
    public void setOcrStatus(OcrStatus ocrStatus) { this.ocrStatus = ocrStatus; }

    public String getOcrEngine() { return ocrEngine; }
    public void setOcrEngine(String ocrEngine) { this.ocrEngine = ocrEngine; }

    public Double getOverallConfidence() { return overallConfidence; }
    public void setOverallConfidence(Double overallConfidence) { this.overallConfidence = overallConfidence; }

    public boolean isUncertain() { return uncertain; }
    public void setUncertain(boolean uncertain) { this.uncertain = uncertain; }

    public Instant getValidFrom() { return validFrom; }
    public void setValidFrom(Instant validFrom) { this.validFrom = validFrom; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public DocumentVerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(DocumentVerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public DocumentComplianceStatus getComplianceStatus() { return complianceStatus; }
    public void setComplianceStatus(DocumentComplianceStatus complianceStatus) { this.complianceStatus = complianceStatus; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public UUID getPreviousVersionId() { return previousVersionId; }
    public void setPreviousVersionId(UUID previousVersionId) { this.previousVersionId = previousVersionId; }

    public UUID getRootDocumentId() { return rootDocumentId; }
    public void setRootDocumentId(UUID rootDocumentId) { this.rootDocumentId = rootDocumentId; }

    public String getOfficerComments() { return officerComments; }
    public void setOfficerComments(String officerComments) { this.officerComments = officerComments; }

    public String getClarificationText() { return clarificationText; }
    public void setClarificationText(String clarificationText) { this.clarificationText = clarificationText; }

    public Integer getFindingsCount() { return findingsCount; }
    public void setFindingsCount(Integer findingsCount) { this.findingsCount = findingsCount; }

    public Integer getOpenActionsCount() { return openActionsCount; }
    public void setOpenActionsCount(Integer openActionsCount) { this.openActionsCount = openActionsCount; }

    public String getExpiryIndicator() { return expiryIndicator; }
    public void setExpiryIndicator(String expiryIndicator) { this.expiryIndicator = expiryIndicator; }

    public Long getDaysUntilExpiry() { return daysUntilExpiry; }
    public void setDaysUntilExpiry(Long daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }

    public List<ExtractedFieldDto> getExtractedFields() { return extractedFields; }
    public void setExtractedFields(List<ExtractedFieldDto> extractedFields) { this.extractedFields = extractedFields; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
