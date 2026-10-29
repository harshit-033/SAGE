package org.aicte.sage.modules.documents.dto;

import org.aicte.sage.modules.documents.model.Document;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.documents.model.OcrStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentDto {

    private UUID id;
    private UUID applicationId;
    private UUID institutionId;
    private DocumentType documentType;
    private String originalFilename;
    private String contentType;
    private Long fileSizeBytes;
    private String sha256Hash;
    private OcrStatus ocrStatus;
    private String ocrEngine;
    private Double overallConfidence;
    private boolean uncertain;
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
        }
        dto.setDocumentType(doc.getDocumentType());
        dto.setOriginalFilename(doc.getOriginalFilename());
        dto.setContentType(doc.getContentType());
        dto.setFileSizeBytes(doc.getFileSizeBytes());
        dto.setSha256Hash(doc.getSha256Hash());
        dto.setOcrStatus(doc.getOcrStatus());
        dto.setOcrEngine(doc.getOcrEngine());
        dto.setOverallConfidence(doc.getOverallConfidence());
        dto.setUncertain(doc.isUncertain());
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

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

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

    public List<ExtractedFieldDto> getExtractedFields() { return extractedFields; }
    public void setExtractedFields(List<ExtractedFieldDto> extractedFields) { this.extractedFields = extractedFields; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
