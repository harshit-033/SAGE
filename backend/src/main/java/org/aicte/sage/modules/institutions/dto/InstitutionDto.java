package org.aicte.sage.modules.institutions.dto;

import org.aicte.sage.modules.institutions.entity.Institution;

import java.time.Instant;
import java.util.UUID;

public class InstitutionDto {
    private UUID id;
    private String code;
    private String name;
    private String applicationType;
    private String state;
    private String district;
    private String status;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public InstitutionDto() {}

    public static InstitutionDto fromEntity(Institution entity) {
        InstitutionDto dto = new InstitutionDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setApplicationType(entity.getApplicationType());
        dto.setState(entity.getState());
        dto.setDistrict(entity.getDistrict());
        dto.setStatus(entity.getStatus());
        dto.setVersion(entity.getVersion());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
