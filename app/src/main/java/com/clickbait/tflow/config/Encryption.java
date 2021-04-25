package com.clickbait.tflow.config;

public class Encryption {
    private String authHeader;
    private String prefix;
    private JWTConfig jwtConfig;

    public JWTConfig getJwtConfig() {
        return jwtConfig;
    }

    public void setJwtConfig(JWTConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
