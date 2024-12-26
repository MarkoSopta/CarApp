package com.fsre.carapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fsre.carapp.R;
import com.fsre.carapp.services.ImageOrientationService;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";

    private Button captureButton;
    private ImageCapture imageCapture;
    private File tempImageFile;
    private ExecutorService executorService;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageOrientationService imageOrientationService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        captureButton = view.findViewById(R.id.captureButton);
        previewView = view.findViewById(R.id.previewView);

        executorService = Executors.newSingleThreadExecutor();
        imageOrientationService = new ImageOrientationService();

        startCamera();

        captureButton.setOnClickListener(v -> takePhoto());

        return view;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        imageCapture = new ImageCapture.Builder().build();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        tempImageFile = getTempImageFile();
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(tempImageFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                navigateToPreviewImageFragment(tempImageFile.getAbsolutePath());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Error capturing image", exception);
            }
        });
    }

    private File getTempImageFile() {
        File storageDir = requireContext().getExternalFilesDir(null);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        return new File(storageDir, "tempimg.jpg");
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
}