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

    // Fixed: Remove duplicate /predict from base URL
    private static final String BASE_URL = "https://aiserver-k7yh.onrender.com/";

    // Singleton instance
    private static ApiService instance;
    private final ApiEndpoint apiEndpoint;

    // Private constructor for singleton
    public ApiService() {
        // Optimized connection pool - fewer connections, longer keepalive
        ConnectionPool connectionPool = new ConnectionPool(
            3,  // Max idle connections (reduced from 5)
            30, // Keep alive duration (increased from 5)
            TimeUnit.SECONDS
        );

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // Reduced from 60
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(true)  // Auto-retry on failure
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiEndpoint = retrofit.create(ApiEndpoint.class);
    }

    // Singleton getter
    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public void uploadImage(File imageFile, ApiCallback callback) {
        try {
                  Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            if (originalBitmap == null) {
                callback.onFailure(new Exception("Failed to decode image file"));
                return;
            }

            Bitmap resizedBitmap = resizeImage(originalBitmap, 1024, 1024);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream); // Increased quality from 80 to 85
            byte[] compressedImageData = outputStream.toByteArray();

            // Recycle bitmaps to free memory
            originalBitmap.recycle();
            resizedBitmap.recycle();

            // Step 3: Create multipart request
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/jpeg"),  // Changed from multipart/form-data
                    compressedImageData
            );

            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.getName(),
                    requestFile
            );

            // Step 4: Send request
            Call<ApiResponse> call = apiEndpoint.uploadImage(body);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, retrofit2.Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onSuccess(response.body());
                    } else {
                        String errorMsg = "Upload failed: " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                errorMsg += " - " + response.errorBody().string();
                            } catch (Exception ignored) {}
                        }
                        callback.onFailure(new Exception(errorMsg));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    callback.onFailure(new Exception("Network error: " + t.getMessage()));
                }
            });

        } catch (OutOfMemoryError e) {
            callback.onFailure(new Exception("Image too large - out of memory"));
        } catch (Exception e) {
            callback.onFailure(new Exception("Failed to process image: " + e.getMessage()));
        }
    }

    private Bitmap resizeImage(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        // Skip resizing if already smaller
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return originalBitmap;
        }

        float aspectRatio = (float) originalWidth / (float) originalHeight;
        int newWidth;
        int newHeight;

        if (originalWidth > originalHeight) {
            newWidth = maxWidth;
            newHeight = Math.round(newWidth / aspectRatio);
        } else {
            newHeight = maxHeight;
            newWidth = Math.round(newHeight * aspectRatio);
        }

        // Use FILTER_BITMAP for better quality
        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    public interface ApiCallback {
        void onSuccess(ApiResponse response);
        void onFailure(Exception e);
    }
}