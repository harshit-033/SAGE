package org.aicte.sage.modules.documents.repository;

import org.aicte.sage.modules.documents.model.Document;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.aicte.sage.modules.documents.model.DocumentCategory;
import org.aicte.sage.modules.documents.model.DocumentComplianceStatus;
import org.aicte.sage.modules.documents.model.DocumentVerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
    List<Document> findByApplicationIdAndDocumentType(UUID applicationId, DocumentType documentType);
    List<Document> findByInstitutionId(UUID institutionId);
    Optional<Document> findBySha256Hash(String sha256Hash);

    List<Document> findByRootDocumentIdOrderByVersionNumberDesc(UUID rootDocumentId);

    @Query("SELECT d FROM Document d WHERE " +
           "(:institutionId IS NULL OR d.institution.id = :institutionId) AND " +
           "(:applicationId IS NULL OR d.application.id = :applicationId) AND " +
           "(:category IS NULL OR d.documentCategory = :category) AND " +
           "(:verificationStatus IS NULL OR d.verificationStatus = :verificationStatus) AND " +
           "(:complianceStatus IS NULL OR d.complianceStatus = :complianceStatus) AND " +
           "(:search IS NULL OR LOWER(d.originalFilename) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.documentName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY d.createdAt DESC")
    Page<Document> searchDocuments(
            @Param("institutionId") UUID institutionId,
            @Param("applicationId") UUID applicationId,
            @Param("category") DocumentCategory category,
            @Param("verificationStatus") DocumentVerificationStatus verificationStatus,
            @Param("complianceStatus") DocumentComplianceStatus complianceStatus,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT d FROM Document d WHERE " +
           "d.institution.id IN :institutionIds AND " +
           "(:applicationId IS NULL OR d.application.id = :applicationId) AND " +
           "(:category IS NULL OR d.documentCategory = :category) AND " +
           "(:verificationStatus IS NULL OR d.verificationStatus = :verificationStatus) AND " +
           "(:complianceStatus IS NULL OR d.complianceStatus = :complianceStatus) AND " +
           "(:search IS NULL OR LOWER(d.originalFilename) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.documentName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY d.createdAt DESC")
    Page<Document> searchDocumentsByInstitutionIds(
            @Param("institutionIds") java.util.Collection<UUID> institutionIds,
            @Param("applicationId") UUID applicationId,
            @Param("category") DocumentCategory category,
            @Param("verificationStatus") DocumentVerificationStatus verificationStatus,
            @Param("complianceStatus") DocumentComplianceStatus complianceStatus,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT d FROM Document d WHERE " +
           "LOWER(d.institution.state) IN :states AND " +
           "(:institutionId IS NULL OR d.institution.id = :institutionId) AND " +
           "(:applicationId IS NULL OR d.application.id = :applicationId) AND " +
           "(:category IS NULL OR d.documentCategory = :category) AND " +
           "(:verificationStatus IS NULL OR d.verificationStatus = :verificationStatus) AND " +
           "(:complianceStatus IS NULL OR d.complianceStatus = :complianceStatus) AND " +
           "(:search IS NULL OR LOWER(d.originalFilename) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.documentName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY d.createdAt DESC")
    Page<Document> searchDocumentsByStates(
            @Param("states") java.util.Collection<String> states,
            @Param("institutionId") UUID institutionId,
            @Param("applicationId") UUID applicationId,
            @Param("category") DocumentCategory category,
            @Param("verificationStatus") DocumentVerificationStatus verificationStatus,
            @Param("complianceStatus") DocumentComplianceStatus complianceStatus,
            @Param("search") String search,
            Pageable pageable
    );

    long countByInstitutionId(UUID institutionId);
    long countByInstitutionIdAndVerificationStatus(UUID institutionId, DocumentVerificationStatus verificationStatus);
    long countByInstitutionIdAndComplianceStatus(UUID institutionId, DocumentComplianceStatus complianceStatus);

    @Query("SELECT d FROM Document d WHERE d.expiryDate IS NOT NULL AND d.expiryDate <= :threshold AND d.complianceStatus != 'EXPIRED'")
    List<Document> findExpiringDocuments(@Param("threshold") Instant threshold);

    @Query("SELECT d FROM Document d WHERE d.expiryDate IS NOT NULL AND d.expiryDate < :now AND d.complianceStatus != 'EXPIRED'")
    List<Document> findExpiredDocuments(@Param("now") Instant now);
}
