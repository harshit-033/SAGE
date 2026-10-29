package org.aicte.sage.modules.documents.repository;

import org.aicte.sage.modules.documents.model.Document;
import org.aicte.sage.modules.documents.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
    List<Document> findByApplicationIdAndDocumentType(UUID applicationId, DocumentType documentType);
    Optional<Document> findBySha256Hash(String sha256Hash);
}
