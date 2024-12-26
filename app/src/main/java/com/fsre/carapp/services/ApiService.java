package com.fsre.carapp.services;

import com.fsre.carapp.models.ApiResponse;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    // Correct base URL without trailing slash
    private static final String BASE_URL = "https://f61c-178-236-84-140.ngrok-free.app/predict/";
    private ApiEndpoint apiEndpoint;

    public ApiService() {
        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // Remove trailing slash from the base URL
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiEndpoint = retrofit.create(ApiEndpoint.class);
    }

    public void uploadImage(File imageFile, ApiCallback callback) {
        // Correct form data key is "image" (based on your Flask server code)
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        // Ensure the correct endpoint is used
        Call<ApiResponse> call = apiEndpoint.uploadImage(body);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, retrofit2.Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(new Exception("Upload failed with response code " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onFailure(new Exception(t));
            }
        });
    }

    public interface ApiCallback {
        void onSuccess(ApiResponse response);
        void onFailure(Exception e);
    }
}
