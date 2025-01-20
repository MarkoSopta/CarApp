package com.fsre.carapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fsre.carapp.DashboardActivity;
import com.fsre.carapp.R;
import com.fsre.carapp.models.ApiResponse;
import com.fsre.carapp.services.ApiService;
import com.fsre.carapp.services.ImageOrientationService;

import java.io.File;

public class PreviewImageFragment extends Fragment {

    private static final String TAG = "PreviewImageFragment";

    private ImageView previewImageView;
<<<<<<< HEAD
    private ImageButton retakeButton, sendButton, chooseFromGalleryButton;
    private TextView primaryResultTextView, secondaryResultTextView;
=======
    private ImageButton retakeButton, sendButton, chooseFromGalleryButton, goToDashboardButton;
    private TextView primaryResultTextView, secondaryResultTextView;
    private ProgressBar progressBar;
>>>>>>> e702f30a6048b4c7370ca3ea9bd7c4528375d491
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
<<<<<<< HEAD
=======
        progressBar = view.findViewById(R.id.progressBar);

>>>>>>> e702f30a6048b4c7370ca3ea9bd7c4528375d491
        primaryResultTextView = view.findViewById(R.id.primaryResultTextView);
        secondaryResultTextView = view.findViewById(R.id.secondaryResultTextView);

        apiService = new ApiService();
        imageOrientationService = new ImageOrientationService();

        if (getArguments() != null) {
            String imagePath = getArguments().getString("imagePath");
            imageFile = new File(imagePath);
            previewBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            previewImageView.setImageBitmap(previewBitmap);
        }

        retakeButton.setOnClickListener(v -> navigateToCameraFragment());
        sendButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            sendImageToApi(imageFile);
        });
        chooseFromGalleryButton.setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).openGallery();
            }
        });

        return view;
    }

    private void navigateToCameraFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new CameraFragment())
                .addToBackStack(null)
                .commit();
    }

    private void sendImageToApi(File imageFile) {
        apiService.uploadImage(imageFile, new ApiService.ApiCallback() {
            @Override
            public void onSuccess(ApiResponse response) {
<<<<<<< HEAD
                displayResults(response.getPrimaryResult(),response.getSecondaryResult().getInfoLink());
=======
                progressBar.setVisibility(View.GONE);
                displayResults(response.getPrimaryResult(), response.getSecondaryResult().getInfoLink());
>>>>>>> e702f30a6048b4c7370ca3ea9bd7c4528375d491
            }

            @Override
            public void onFailure(Exception e) {
<<<<<<< HEAD
                displayResults("Error: " + e.getMessage(),"Error: " + e.getMessage());
=======
                progressBar.setVisibility(View.GONE);
                displayResults("Error: " + e.getMessage(), "Error: " + e.getMessage());
>>>>>>> e702f30a6048b4c7370ca3ea9bd7c4528375d491
            }
        });
    }

    private void displayResults(String primaryResult, String secondaryResult) {
        primaryResultTextView.setText(primaryResult);
        primaryResultTextView.setVisibility(View.VISIBLE);

        if (!primaryResult.equals("No car detected")) {
            secondaryResultTextView.setText("Više informacija");
            secondaryResultTextView.setVisibility(View.VISIBLE);

            secondaryResultTextView.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(secondaryResult));
                startActivity(browserIntent);
            });
        } else {
            secondaryResultTextView.setVisibility(View.GONE);
        }
    }
}