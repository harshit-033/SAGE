package org.aicte.sage.modules.documents.dto;

import org.aicte.sage.modules.documents.model.DocumentExtractedField;

import java.util.UUID;

public class ExtractedFieldDto {

    private UUID id;
    private String fieldKey;
    private String extractedValue;
    private String normalizedValue;
    private Double confidenceScore;
    private boolean uncertain;
    private Integer sourcePage;
    private String boundingBoxJson;
    private String rawTextSnippet;

    public ExtractedFieldDto() {}

    public static ExtractedFieldDto fromEntity(DocumentExtractedField entity) {
        ExtractedFieldDto dto = new ExtractedFieldDto();
        dto.setId(entity.getId());
        dto.setFieldKey(entity.getFieldKey());
        dto.setExtractedValue(entity.getExtractedValue());
        dto.setNormalizedValue(entity.getNormalizedValue());
        dto.setConfidenceScore(entity.getConfidenceScore());
        dto.setUncertain(entity.isUncertain());
        dto.setSourcePage(entity.getSourcePage());
        dto.setBoundingBoxJson(entity.getBoundingBoxJson());
        dto.setRawTextSnippet(entity.getRawTextSnippet());
        return dto;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getExtractedValue() { return extractedValue; }
    public void setExtractedValue(String extractedValue) { this.extractedValue = extractedValue; }

    public String getNormalizedValue() { return normalizedValue; }
    public void setNormalizedValue(String normalizedValue) { this.normalizedValue = normalizedValue; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public boolean isUncertain() { return uncertain; }
    public void setUncertain(boolean uncertain) { this.uncertain = uncertain; }

    public Integer getSourcePage() { return sourcePage; }
    public void setSourcePage(Integer sourcePage) { this.sourcePage = sourcePage; }

    public String getBoundingBoxJson() { return boundingBoxJson; }
    public void setBoundingBoxJson(String boundingBoxJson) { this.boundingBoxJson = boundingBoxJson; }

    public String getRawTextSnippet() { return rawTextSnippet; }
    public void setRawTextSnippet(String rawTextSnippet) { this.rawTextSnippet = rawTextSnippet; }
}
