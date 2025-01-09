package com.fsre.carapp.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fsre.carapp.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class CropFragment extends Fragment {
    private static final String TEMP_IMAGE_NAME = "temp_image.jpg";
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crop, container, false);
        if (getArguments() != null) {
            String imagePath = getArguments().getString("imagePath");
            imageUri = Uri.fromFile(new File(imagePath));
            startCrop(imageUri);
        }
        return view;
    }

    private void startCrop(Uri uri) {
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getContext().getCacheDir(), TEMP_IMAGE_NAME)));
        uCrop.withOptions(getCropOptions());
        uCrop.start(getContext(), this);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true); // Allow user to manipulate crop rectangle
        options.setHideBottomControls(false); // Show bottom controls for image manipulation
        options.setCompressionQuality(80);
        options.setMaxBitmapSize(1024);
        return options;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                navigateToPreviewImageFragment(resultUri.getPath());
            }
        } else if (resultCode == UCrop.RESULT_ERROR || resultCode == getActivity().RESULT_CANCELED) {
            navigateToDashboardFragment();
        }
    }

    private void navigateToPreviewImageFragment(String imagePath) {
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", imagePath);
        PreviewImageFragment previewImageFragment = new PreviewImageFragment();
        previewImageFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, previewImageFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToDashboardFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new DashboardFragment())
                .commit();
    }
}