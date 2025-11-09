package com.fsre.carapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.fsre.carapp.DashboardActivity;
import com.fsre.carapp.R;
import com.fsre.carapp.models.ApiResponse;
import com.fsre.carapp.services.ApiService;

import java.io.File;

public class PreviewImageFragment extends Fragment {

    private static final String TAG = "PreviewImageFragment";
    private static final long DEBOUNCE_DELAY_MS = 500;

    private ImageView previewImageView;
    private ImageButton retakeButton, sendButton, chooseFromGalleryButton;
    private TextView primaryResultTextView, secondaryResultTextView;
    private ProgressBar progressBar;

    private File imageFile;
    private ApiService apiService;
    private boolean isUploading = false;

    private String cachedPrimaryResult;
    private String cachedSecondaryResult;

    private Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable retakeRunnable, sendRunnable, galleryRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview_image, container, false);

        previewImageView = view.findViewById(R.id.previewImageView);
        retakeButton = view.findViewById(R.id.retakeButton);
        sendButton = view.findViewById(R.id.sendButton);
        chooseFromGalleryButton = view.findViewById(R.id.chooseFromGalleryButton);
        progressBar = view.findViewById(R.id.progressBar);
        primaryResultTextView = view.findViewById(R.id.primaryResultTextView);
        secondaryResultTextView = view.findViewById(R.id.secondaryResultTextView);

        apiService = new ApiService();

        if (getArguments() != null) {
            String imagePath = getArguments().getString("imagePath");
            imageFile = new File(imagePath);

            Glide.with(this)
                    .load(imageFile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .signature(new ObjectKey(System.currentTimeMillis()))
                    .centerInside()
                    .into(previewImageView);
        }

        if (cachedPrimaryResult != null) {
            displayResults(cachedPrimaryResult, cachedSecondaryResult);
            toggleButtonsVisibility(View.VISIBLE);
        }

        retakeRunnable = this::navigateToCameraFragment;
        sendRunnable = () -> {
            if (isUploading) return;

            isUploading = true;
            progressBar.setVisibility(View.VISIBLE);
            toggleButtonsVisibility(View.GONE);
            sendImageToApi(imageFile);
        };
        galleryRunnable = () -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).openGallery();
            }
        };

        retakeButton.setOnClickListener(v -> debounceClick(retakeRunnable));
        sendButton.setOnClickListener(v -> debounceClick(sendRunnable));
        chooseFromGalleryButton.setOnClickListener(v -> debounceClick(galleryRunnable));

        return view;
    }

    private void debounceClick(Runnable action) {
        debounceHandler.removeCallbacks(action);
        debounceHandler.postDelayed(action, DEBOUNCE_DELAY_MS);
    }

    private void toggleButtonsVisibility(int visibility) {
        retakeButton.setVisibility(visibility);
        sendButton.setVisibility(visibility);
        chooseFromGalleryButton.setVisibility(visibility);
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
                isUploading = false;
                cachedPrimaryResult = response.getPrimaryResult();
                cachedSecondaryResult = response.getSecondaryResult().getInfoLink();
                displayResults(cachedPrimaryResult, cachedSecondaryResult);
                progressBar.setVisibility(View.GONE);
                toggleButtonsVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {
                isUploading = false;
                String errorMessage = getErrorMessage(e);
                displayResults(errorMessage, "");
                progressBar.setVisibility(View.GONE);
                toggleButtonsVisibility(View.VISIBLE);
            }
        });
    }

    private String getErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("timeout")) {
                return "Connection timeout. Check your internet.";
            } else if (message.contains("UnknownHost")) {
                return "No internet connection.";
            }
        }
        return "Error: " + message;
    }

    private void displayResults(String primaryResult, String secondaryResult) {
        primaryResultTextView.setText(primaryResult);
        primaryResultTextView.setVisibility(View.VISIBLE);

        if (!primaryResult.equals("No car detected") && !primaryResult.startsWith("Error")) {
            secondaryResultTextView.setText("Više informacija");
            secondaryResultTextView.setVisibility(View.VISIBLE);

            secondaryResultTextView.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(secondaryResult));
                startActivity(browserIntent);
            });
        } else {
            secondaryResultTextView.setVisibility(View.GONE);
            secondaryResultTextView.setOnClickListener(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        debounceHandler.removeCallbacks(retakeRunnable);
        debounceHandler.removeCallbacks(sendRunnable);
        debounceHandler.removeCallbacks(galleryRunnable);

        if (previewImageView != null) {
            Glide.with(this).clear(previewImageView);
        }
    }
}
