package com.fsre.carapp.services;

import com.fsre.carapp.models.ApiResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiEndpoint {
    @Multipart
    @POST("/predict")
    Call<ApiResponse> uploadImage(@Part MultipartBody.Part file);
}