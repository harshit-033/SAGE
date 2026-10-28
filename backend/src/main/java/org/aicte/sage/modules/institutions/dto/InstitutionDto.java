package org.aicte.sage.modules.institutions.dto;

import org.aicte.sage.modules.institutions.entity.Institution;

import java.time.Instant;
import java.util.UUID;

public class InstitutionDto {
    private UUID id;
    private String code;
    private String name;
    private String applicationType;
    private String category;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String district;
    private String pincode;
    private String officialEmail;
    private String officialPhone;
    private String alternatePhone;
    private String websiteUrl;
    private Integer establishmentYear;
    private Double latitude;
    private Double longitude;
    private String status;
    private boolean active;
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
        dto.setCategory(entity.getCategory());
        dto.setAddressLine1(entity.getAddressLine1());
        dto.setAddressLine2(entity.getAddressLine2());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setDistrict(entity.getDistrict());
        dto.setPincode(entity.getPincode());
        dto.setOfficialEmail(entity.getOfficialEmail());
        dto.setOfficialPhone(entity.getOfficialPhone());
        dto.setAlternatePhone(entity.getAlternatePhone());
        dto.setWebsiteUrl(entity.getWebsiteUrl());
        dto.setEstablishmentYear(entity.getEstablishmentYear());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setStatus(entity.getStatus());
        dto.setActive(entity.isActive());
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getOfficialEmail() {
        return officialEmail;
    }

    public void setOfficialEmail(String officialEmail) {
        this.officialEmail = officialEmail;
    }

    public String getOfficialPhone() {
        return officialPhone;
    }

    public void setOfficialPhone(String officialPhone) {
        this.officialPhone = officialPhone;
    }

    public String getAlternatePhone() {
        return alternatePhone;
    }

    public void setAlternatePhone(String alternatePhone) {
        this.alternatePhone = alternatePhone;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public Integer getEstablishmentYear() {
        return establishmentYear;
    }

    public void setEstablishmentYear(Integer establishmentYear) {
        this.establishmentYear = establishmentYear;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
