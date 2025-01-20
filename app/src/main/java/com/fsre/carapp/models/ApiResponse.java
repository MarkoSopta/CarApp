package com.fsre.carapp.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("primary_result")
    private String primaryResult;
<<<<<<< HEAD

    @SerializedName("secondary_result")
    private SecondaryResult secondaryResult;

=======
    @SerializedName("secondary_result")
    private SecondaryResult secondaryResult;

>>>>>>> e702f30a6048b4c7370ca3ea9bd7c4528375d491
    public String getPrimaryResult() {
        return primaryResult;
    }

<<<<<<< HEAD
=======

>>>>>>> e702f30a6048b4c7370ca3ea9bd7c4528375d491
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