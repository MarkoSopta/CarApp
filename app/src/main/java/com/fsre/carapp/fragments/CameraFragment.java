package com.fsre.carapp.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fsre.carapp.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;

public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView capturedImageView;
    private Button captureButton, retakeButton, uploadButton;
    private ImageCapture imageCapture;
    private Bitmap capturedBitmap;
    private File tempImageFile;
    private LinearLayout previewButtonsLayout;
    private ExecutorService executorService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        capturedImageView = view.findViewById(R.id.capturedImageView);
        captureButton = view.findViewById(R.id.captureButton);
        retakeButton = view.findViewById(R.id.retakeButton);
        uploadButton = view.findViewById(R.id.uploadButton);
        previewButtonsLayout = view.findViewById(R.id.previewButtonsLayout);

        executorService = Executors.newSingleThreadExecutor();

        startCamera();

        captureButton.setOnClickListener(v -> takePhoto());
        retakeButton.setOnClickListener(v -> retakePhoto());
        uploadButton.setOnClickListener(v -> uploadPhoto());

        return view;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
                preview.setSurfaceProvider(((PreviewView) getView().findViewById(R.id.previewView)).getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        if (tempImageFile != null && tempImageFile.exists()) {
            tempImageFile.delete();
        }

        tempImageFile = new File(requireContext().getExternalFilesDir(null), "temp_image.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(tempImageFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                capturedBitmap = BitmapFactory.decodeFile(tempImageFile.getAbsolutePath());
                if (capturedBitmap != null) {
                    capturedImageView.setImageBitmap(capturedBitmap);
                    toggleViews(false);
                } else {
                    Log.e(TAG, "Error decoding captured image");
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Error capturing image", exception);
            }
        });
    }

    private void retakePhoto() {
        if (tempImageFile != null && tempImageFile.exists()) {
            tempImageFile.delete();
        }
        resetFragment();
    }

    private void resetFragment() {
        capturedBitmap = null;
        capturedImageView.setImageBitmap(null);
        toggleViews(true);
        startCamera();
    }
    private void uploadPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                originalBitmap = rotateBitmapIfNeeded(originalBitmap, imageUri);
                int maxWidth = 1024;
                int maxHeight = 1024;
                capturedBitmap = scaleDownBitmap(originalBitmap, maxWidth, maxHeight);
                capturedImageView.setImageBitmap(capturedBitmap);
                toggleViews(false);
            } catch (IOException e) {
                Log.e(TAG, "Error loading selected image", e);
            }
        }
    }

    private Bitmap scaleDownBitmap(Bitmap original, int maxWidth, int maxHeight) {
        float ratio = Math.min(
                (float) maxWidth / original.getWidth(),
                (float) maxHeight / original.getHeight());
        int width = Math.round(ratio * original.getWidth());
        int height = Math.round(ratio * original.getHeight());

        return Bitmap.createScaledBitmap(original, width, height, true);
    }


    private Bitmap rotateBitmapIfNeeded(Bitmap bitmap, Uri imageUri) throws IOException {
        ExifInterface exif = new ExifInterface(requireContext().getContentResolver().openInputStream(imageUri));
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotation = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    private void toggleViews(boolean showPreview) {
        capturedImageView.setVisibility(showPreview ? View.GONE : View.VISIBLE);
        captureButton.setVisibility(showPreview ? View.VISIBLE : View.GONE);
        retakeButton.setVisibility(showPreview ? View.GONE : View.VISIBLE);
        uploadButton.setVisibility(showPreview ? View.VISIBLE : View.GONE);
        previewButtonsLayout.setVisibility(showPreview ? View.GONE : View.VISIBLE);
    }
}