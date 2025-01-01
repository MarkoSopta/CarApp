package com.fsre.carapp.fragments;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
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
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector tapGestureDetector;
    private ImageOrientationService imageOrientationService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        captureButton = view.findViewById(R.id.captureButton);
        previewView = view.findViewById(R.id.previewView);
        imageOrientationService = new ImageOrientationService();

        executorService = Executors.newSingleThreadExecutor();
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        tapGestureDetector = new GestureDetector(getContext(), new TapListener());

        captureButton.setOnClickListener(v -> takePhoto());

        startCamera();

        return view;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        imageCapture = new ImageCapture.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        tempImageFile = getTempImageFile();

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(tempImageFile).build();
        imageCapture.takePicture(outputOptions, executorService, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri imageUri = Uri.fromFile(tempImageFile);
                navigateToCropFragment(imageUri);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    private File getTempImageFile() {
        File storageDir = getContext().getCacheDir();
        return new File(storageDir, "temp_image.jpg");
    }

    private void navigateToCropFragment(Uri imageUri) {
        String imagePath = imageUri.getPath();
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", imagePath);
        CropFragment cropFragment = new CropFragment();
        cropFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, cropFragment)
                .addToBackStack(null)
                .commit();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (camera != null) {
                float currentZoomRatio = camera.getCameraInfo().getZoomState().getValue().getZoomRatio();
                float delta = detector.getScaleFactor();
                camera.getCameraControl().setZoomRatio(currentZoomRatio * delta);
            }
            return true;
        }
    }

    private class TapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (camera != null) {
                MeteringPointFactory factory = previewView.getMeteringPointFactory();
                MeteringPoint point = factory.createPoint(e.getX(), e.getY());
                FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                        .addPoint(point, FocusMeteringAction.FLAG_AE) // Optional: add AE metering
                        .build();
                camera.getCameraControl().startFocusAndMetering(action);
            }
            return true;
        }
    }
}