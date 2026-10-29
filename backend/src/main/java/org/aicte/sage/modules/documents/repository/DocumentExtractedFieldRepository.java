package org.aicte.sage.modules.documents.repository;

import org.aicte.sage.modules.documents.model.DocumentExtractedField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentExtractedFieldRepository extends JpaRepository<DocumentExtractedField, UUID> {
    List<DocumentExtractedField> findByDocumentId(UUID documentId);
    List<DocumentExtractedField> findByDocumentApplicationId(UUID applicationId);
    void deleteByDocumentId(UUID documentId);
}
