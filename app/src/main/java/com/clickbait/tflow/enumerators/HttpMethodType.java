package com.clickbait.tflow.enumerators;

public enum HttpMethodType {

    GET("GET"), POST("POST");

    HttpMethodType(String value) {
        this.value = value;
    }

    private String value;

    @Override
    public String toString() {
        return value;
    }
}