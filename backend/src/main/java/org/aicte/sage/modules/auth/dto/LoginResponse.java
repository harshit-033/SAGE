package org.aicte.sage.modules.auth.dto;

public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresInMs;
    private UserProfileDto user;

    public LoginResponse() {}

    public LoginResponse(String accessToken, long expiresInMs, UserProfileDto user) {
        this.accessToken = accessToken;
        this.expiresInMs = expiresInMs;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }

    public UserProfileDto getUser() {
        return user;
    }

    public void setUser(UserProfileDto user) {
        this.user = user;
    }
}
