package org.aicte.sage.modules.documents.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class DocumentOcrClient {

    private static final Logger log = LoggerFactory.getLogger(DocumentOcrClient.class);

    private final RestClient restClient;
    private final String serviceToken;

    public DocumentOcrClient(
            @Value("${sage.automation.service-url:http://localhost:8000}") String automationServiceUrl,
            @Value("${sage.security.internal.service-token:sage_internal_dev_service_secret_token_12345}") String serviceToken
    ) {
        this.serviceToken = serviceToken;
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .build();
        this.restClient = RestClient.builder()
                .requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient))
                .baseUrl(automationServiceUrl)
                .build();
    }

    public record ExtractedFieldResponse(
            @JsonProperty("field_key") String fieldKey,
            @JsonProperty("extracted_value") String extractedValue,
            @JsonProperty("normalized_value") String normalizedValue,
            @JsonProperty("confidence_score") Double confidenceScore,
            @JsonProperty("is_uncertain") boolean uncertain,
            @JsonProperty("source_page") Integer sourcePage,
            @JsonProperty("bounding_box") List<Double> boundingBox,
            @JsonProperty("raw_text_snippet") String rawTextSnippet
    ) {}

    public record OcrResponse(
            boolean success,
            String engine,
            @JsonProperty("document_id") String documentId,
            @JsonProperty("document_type") String documentType,
            @JsonProperty("pages_count") int pagesCount,
            @JsonProperty("extracted_fields") Map<String, ExtractedFieldResponse> extractedFields,
            @JsonProperty("overall_confidence") Double overallConfidence,
            @JsonProperty("is_uncertain") boolean uncertain,
            @JsonProperty("processing_time_ms") double processingTimeMs,
            @JsonProperty("detected_deficiencies") List<String> detectedDeficiencies,
            @JsonProperty("error_message") String errorMessage
    ) {}

    public Optional<OcrResponse> processDocument(String filePath, String documentType, String documentId) {
        log.info("Sending document {} to Automation OCR service: {}", documentId, filePath);

        Map<String, String> requestBody = Map.of(
                "file_path", filePath,
                "document_type", documentType,
                "document_id", documentId != null ? documentId : ""
        );

        try {
            OcrResponse response = restClient.post()
                    .uri("/api/v1/ocr/process")
                    .header("X-Internal-Service-Token", serviceToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(OcrResponse.class);

            if (response != null && response.success()) {
                log.info("OCR successful for {} via engine {}: {} pages in {}ms",
                        documentId, response.engine(), response.pagesCount(), response.processingTimeMs());
                return Optional.of(response);
            } else {
                log.warn("OCR service reported failure for {}: {}", documentId,
                        response != null ? response.errorMessage() : "Null response");
                return Optional.ofNullable(response);
            }
        } catch (Exception e) {
            log.error("Failed to invoke Automation OCR service for {}: {}", documentId, e.getMessage());
            return Optional.empty();
        }
    }
}
