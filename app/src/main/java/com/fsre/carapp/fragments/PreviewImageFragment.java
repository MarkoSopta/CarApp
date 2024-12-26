package com.fsre.carapp.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fsre.carapp.R;
import com.fsre.carapp.models.ApiResponse;
import com.fsre.carapp.services.ApiService;
import com.fsre.carapp.services.ImageOrientationService;

import java.io.File;
import java.io.IOException;

public class PreviewImageFragment extends Fragment {

    private static final String TAG = "PreviewImageFragment";

    private ImageView previewImageView;
    private Button retakeButton, sendButton, chooseFromGalleryButton;
    private TextView resultTextView;
    private Bitmap previewBitmap;
    private File imageFile;
    private ApiService apiService;
    private ImageOrientationService imageOrientationService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview_image, container, false);
        previewImageView = view.findViewById(R.id.previewImageView);
        retakeButton = view.findViewById(R.id.retakeButton);
        sendButton = view.findViewById(R.id.sendButton);
        chooseFromGalleryButton = view.findViewById(R.id.chooseFromGalleryButton);
        resultTextView = view.findViewById(R.id.resultTextView);

        apiService = new ApiService();
        imageOrientationService = new ImageOrientationService();

        if (getArguments() != null) {
            String imagePath = getArguments().getString("imagePath");
            imageFile = new File(imagePath);
            try {
                previewBitmap = imageOrientationService.getCorrectlyOrientedBitmap(imageFile);
                previewImageView.setImageBitmap(previewBitmap);
            } catch (IOException e) {
                Log.e(TAG, "Error loading image", e);
            }
        }

        retakeButton.setOnClickListener(v -> navigateToCameraFragment());
        sendButton.setOnClickListener(v -> sendImageToApi(imageFile));
        chooseFromGalleryButton.setOnClickListener(v -> navigateToGalleryFragment());

        return view;
    }

    private void navigateToCameraFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new CameraFragment())
                .addToBackStack(null)
                .commit();
    }

    private void navigateToGalleryFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new GalleryFragment())
                .addToBackStack(null)
                .commit();
    }

    private void sendImageToApi(File imageFile) {
        apiService.uploadImage(imageFile, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(ApiResponse response) {
                new Handler(Looper.getMainLooper()).post(() -> displayResult(response.getResult()));
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error uploading image", e);
            }
        });
    }

    private void displayResult(String result) {
        resultTextView.setText(result);
        resultTextView.setVisibility(View.VISIBLE);
    }
}