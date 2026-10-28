package org.aicte.sage.modules.institutions.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateInstitutionRequest {

    @NotBlank(message = "Institution name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 64, message = "Category must not exceed 64 characters")
    private String category;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 64)
    private String state;

    @NotBlank(message = "District is required")
    @Size(max = 64)
    private String district;

    @Size(max = 10)
    private String pincode;

    @Email(message = "Official email must be valid")
    @Size(max = 128)
    private String officialEmail;

    @Size(max = 20)
    private String officialPhone;

    @Size(max = 20)
    private String alternatePhone;

    @Size(max = 255)
    private String websiteUrl;

    private Integer establishmentYear;

    private Double latitude;

    private Double longitude;

    public UpdateInstitutionRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
