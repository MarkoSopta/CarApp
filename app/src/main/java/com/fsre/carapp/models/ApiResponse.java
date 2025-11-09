package com.fsre.carapp.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("primary_result")
    private String primaryResult;

    @SerializedName("secondary_result")
    private SecondaryResult secondaryResult;

    // Add getters with null safety
    public String getPrimaryResult() {
        return primaryResult != null ? primaryResult : "No result";
    }

    public SecondaryResult getSecondaryResult() {
        return secondaryResult != null ? secondaryResult : new SecondaryResult();
    }

    // Add validation method
    public boolean isValid() {
        return primaryResult != null && !primaryResult.isEmpty();
    }

    public static class SecondaryResult {
        @SerializedName("info_link")
        private String infoLink;

        public String getInfoLink() {
            return infoLink;
        }

        public boolean hasInfoLink() {
            return infoLink != null && !infoLink.isEmpty();
        }
    }
}
