package com.fsre.carapp.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("primary_result")
    private String primaryResult;

    @SerializedName("secondary_result")
    private SecondaryResult secondaryResult;

    public String getPrimaryResult() {
        return primaryResult;
    }

    public SecondaryResult getSecondaryResult() {
        return secondaryResult;
    }

    public static class SecondaryResult {
        @SerializedName("info_link")
        private String infoLink;

        public String getInfoLink() {
            return infoLink;
        }
    }
}