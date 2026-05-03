package ru.qa.blogapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthTokens {
    private String token;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public AuthTokens() {
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
