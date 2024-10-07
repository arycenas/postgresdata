package com.training.postgresdata.request;

public class TokenRequest {

    private String token;

    public TokenRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
