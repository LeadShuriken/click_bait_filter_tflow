package com.clickbait.tflow.config;

import java.net.URI;

import com.clickbait.tflow.enumerators.HttpMethodType;

public class Endpoint {
    private URI path;
    private HttpMethodType type;
    private boolean authenticated;

    public URI getPath() {
        return path;
    }

    public void setPath(URI path) {
        this.path = path;
    }

    public HttpMethodType getType() {
        return type;
    }

    public void setType(HttpMethodType type) {
        this.type = type;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

}