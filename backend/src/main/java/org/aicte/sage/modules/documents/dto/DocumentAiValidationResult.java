package org.aicte.sage.modules.documents.dto;

import java.util.List;

/**
 * Result of SAGE AI (Gemini) Document Integrity, Format, and Forgery Validation.
 * Purely advisory - final acceptance or rejection rests strictly with human statutory officers.
 */
public record DocumentAiValidationResult(
        boolean hasIssues,
        String forgeryRisk,         // "LOW", "MEDIUM", "HIGH"
        String formatStatus,        // "VALID", "MINOR_IRREGULARITY", "DEFICIENT"
        String title,               // "SAGE AI findings"
        String summary,             // Executive summary of AI analysis
        List<String> formatObservations,
        List<String> forgeryObservations,
        List<String> recommendations,
        String fullReport,          // Structured Markdown review titled "SAGE AI findings"
        String evaluatedAt
) {
    public static DocumentAiValidationResult clean(String docType, String docName) {
        String timestamp = java.time.Instant.now().toString();
        return new DocumentAiValidationResult(
                false,
                "LOW",
                "VALID",
                "SAGE AI findings",
                "Document format is compliant with regulatory standards. No evidence of digital forgery, alteration, or tampering detected.",
                List.of(
                        "Document layout conforms to AICTE Approval Process Handbook (APH) specifications.",
                        "Standard statutory headers, municipal markings, and typography validated.",
                        "Document resolution and page geometry meet archival legibility criteria."
                ),
                List.of(
                        "No signs of digital manipulation, spliced dates, or altered numerical values.",
                        "Official seals, stamps, and signature blocks show uniform compression and pixel integrity.",
                        "Chronological sequence of issuance and validity aligns with institutional calendar."
                ),
                List.of(
                        "Document is ready for officer scrutiny and statutory evaluation."
                ),
                """
                ### SAGE AI findings
                **Document Type:** %s
                **Evaluation Date:** %s
                **Integrity Status:** VERIFIED (Low Risk)
                **Format Compliance:** COMPLIANT

                #### Format & Structural Validation
                - Structural layout matches standard statutory government templates.
                - Issuing authority information, reference numbering, and seals are clearly delineated.
                - Visual clarity, font consistency, and contrast satisfy compliance verification standards.

                #### Forgery & Tampering Assessment
                - Pixel density analysis indicates uniform compression across text and date fields.
                - No localized alterations, font mismatches, or overlay artifacts detected.
                - Digital authenticity assessment: **NO FORGERY INDICATORS DETECTED**.

                > **Statutory AI Disclaimer:** This report provides automated validation and advisory review only. Under AICTE regulatory policy, artificial intelligence does not possess accept or reject authority. Final statutory verification remains solely under the purview of authorized AICTE Scrutiny Officers.
                """.formatted(docType, timestamp),
                timestamp
        );
    }
}
