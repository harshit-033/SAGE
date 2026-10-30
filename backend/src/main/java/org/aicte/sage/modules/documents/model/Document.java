package org.aicte.sage.modules.documents.model;

import jakarta.persistence.*;
import org.aicte.sage.modules.applications.model.Application;
import org.aicte.sage.modules.institutions.entity.Institution;
import org.aicte.sage.modules.users.entity.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "document_name")
    private String documentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_category", length = 64)
    private DocumentCategory documentCategory = DocumentCategory.GENERAL;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 64)
    private DocumentVerificationStatus verificationStatus = DocumentVerificationStatus.PENDING_REVIEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", length = 64)
    private DocumentComplianceStatus complianceStatus = DocumentComplianceStatus.COMPLIANT;

    @Column(name = "version_number")
    private Integer versionNumber = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_version_id")
    private Document previousVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_document_id")
    private Document rootDocument;

    @Column(name = "officer_comments", columnDefinition = "TEXT")
    private String officerComments;

    @Column(name = "clarification_text", columnDefinition = "TEXT")
    private String clarificationText;

    @Column(name = "ai_analysis_report", columnDefinition = "TEXT")
    private String aiAnalysisReport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 64)
    private DocumentType documentType;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "file_path", nullable = false, length = 512)
    private String filePath;

    @Column(name = "content_type", nullable = false, length = 128)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "sha256_hash", nullable = false, length = 64)
    private String sha256Hash;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", nullable = false, length = 32)
    private OcrStatus ocrStatus = OcrStatus.PENDING;

    @Column(name = "ocr_engine", length = 32)
    private String ocrEngine;

    @Column(name = "overall_confidence")
    private Double overallConfidence;

    @Column(name = "is_uncertain", nullable = false)
    private boolean uncertain = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Document() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getStoredFilename() { return storedFilename; }
    public void setStoredFilename(String storedFilename) { this.storedFilename = storedFilename; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

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

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getDocumentName() { return documentName != null ? documentName : originalFilename; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public DocumentCategory getDocumentCategory() { return documentCategory; }
    public void setDocumentCategory(DocumentCategory documentCategory) { this.documentCategory = documentCategory; }

    public Instant getValidFrom() { return validFrom; }
    public void setValidFrom(Instant validFrom) { this.validFrom = validFrom; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public DocumentVerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(DocumentVerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public DocumentComplianceStatus getComplianceStatus() { return complianceStatus; }
    public void setComplianceStatus(DocumentComplianceStatus complianceStatus) { this.complianceStatus = complianceStatus; }

    public Integer getVersionNumber() { return versionNumber != null ? versionNumber : 1; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public Document getPreviousVersion() { return previousVersion; }
    public void setPreviousVersion(Document previousVersion) { this.previousVersion = previousVersion; }

    public Document getRootDocument() { return rootDocument; }
    public void setRootDocument(Document rootDocument) { this.rootDocument = rootDocument; }

    public String getOfficerComments() { return officerComments; }
    public void setOfficerComments(String officerComments) { this.officerComments = officerComments; }

    public String getClarificationText() { return clarificationText; }
    public void setClarificationText(String clarificationText) { this.clarificationText = clarificationText; }

    public String getAiAnalysisReport() { return aiAnalysisReport; }
    public void setAiAnalysisReport(String aiAnalysisReport) { this.aiAnalysisReport = aiAnalysisReport; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
