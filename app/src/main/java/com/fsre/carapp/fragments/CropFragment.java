package com.fsre.carapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

        Button finishCropButton = view.findViewById(R.id.button_finish_crop);
        finishCropButton.setOnClickListener(v -> finishCrop());

        return view;
    }

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = TEMP_IMAGE_NAME;
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getContext().getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(450, 450);
        uCrop.withOptions(getCropOptions());
        uCrop.start(getContext(), this);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true); // Enable free style crop
        return options;
    }

    private void finishCrop() {
        UCrop.of(imageUri, Uri.fromFile(new File(getContext().getCacheDir(), TEMP_IMAGE_NAME)))
                .withOptions(getCropOptions())
                .start(getContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                navigateToPreviewImageFragment(resultUri.getPath());
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            navigateToGalleryFragment();
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

    private void navigateToGalleryFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new GalleryFragment())
                .addToBackStack(null)
                .commit();
    }
}