package ru.qa.blogapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshTokenRequest {

    @JsonProperty("refresh_token")
    private String refreshToken;

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
