package org.aicte.sage.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetails {
    private URI type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private String correlationId;
    private Instant timestamp;
    private List<FieldErrorDetail> fieldErrors;

    public ProblemDetails() {
        this.timestamp = Instant.now();
    }

    public ProblemDetails(URI type, String title, int status, String detail, String instance, String correlationId) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.correlationId = correlationId;
        this.timestamp = Instant.now();
    }

    public static class FieldErrorDetail {
        private String field;
        private Object rejectedValue;
        private String message;

        public FieldErrorDetail() {}

        public FieldErrorDetail(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldErrorDetail> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
