package com.fsre.carapp.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fsre.carapp.models.ApiResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
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
    private static final String BASE_URL = "https://aiserver-k7yh.onrender.com/predict/";
    private ApiEndpoint apiEndpoint;

    public ApiService() {

        ConnectionPool connectionPool = new ConnectionPool(5, 5, TimeUnit.MINUTES);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectionPool(connectionPool)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // Remove trailing slash from the base URL
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiEndpoint = retrofit.create(ApiEndpoint.class);
    }

    public void uploadImage(File imageFile, ApiCallback callback) {
        try {
            // Step 1: Resize the image before compression
            Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            Bitmap resizedBitmap = resizeImage(originalBitmap, 224, 224); // Resize to 1280x720 or desired dimensions

            // Step 2: Compress the resized image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // Compress with 80% quality
            byte[] compressedImageData = outputStream.toByteArray();

            // Step 3: Create the RequestBody and MultipartBody.Part objects
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("multipart/form-data"),
                    compressedImageData
            );
            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.getName(),
                    requestFile
            );

            // Step 4: Send the compressed image to the API
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

        } catch (Exception e) {
            callback.onFailure(new Exception("Failed to compress or upload image: " + e.getMessage()));
        }
    }


    private Bitmap resizeImage(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            float aspectRatio = (float) originalWidth / (float) originalHeight;

            if (originalWidth > originalHeight) {
                newWidth = maxWidth;
                newHeight = Math.round(newWidth / aspectRatio);
            } else {
                newHeight = maxHeight;
                newWidth = Math.round(newHeight * aspectRatio);
            }
        }

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }



    public interface ApiCallback {
        void onSuccess(ApiResponse response);
        void onFailure(Exception e);
    }
}
