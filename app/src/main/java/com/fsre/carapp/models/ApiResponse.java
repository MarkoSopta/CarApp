package com.fsre.carapp.models;

public class ApiResponse {
    private String result;

    public ApiResponse() {
    }

    public ApiResponse(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}