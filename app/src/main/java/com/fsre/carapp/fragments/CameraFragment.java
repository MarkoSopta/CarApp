package com.fsre.carapp.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

    private Camera camera;
    private CameraControl cameraControl;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private File tempImageFile;
    private ExecutorService executorService;
    private ProcessCameraProvider cameraProvider;
    private ImageOrientationService imageOrientationService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        previewView = view.findViewById(R.id.previewView);
        Button captureButton = view.findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> takePhoto());

        gestureDetector = new GestureDetector(getContext(), new TapListener());
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        previewView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });

        executorService = Executors.newSingleThreadExecutor();
        imageOrientationService = new ImageOrientationService();

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
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        imageCapture = new ImageCapture.Builder().build();

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        cameraControl = camera.getCameraControl();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        tempImageFile = new File(getContext().getCacheDir(), "temp_image.jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(tempImageFile).build();

        imageCapture.takePicture(outputFileOptions, executorService, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                File correctedImageFile = imageOrientationService.correctImageOrientation(tempImageFile);
                if (correctedImageFile != null) {
                    Uri imageUri = Uri.fromFile(correctedImageFile);
                    navigateToCropFragment(imageUri);
                } else {
                    Log.e(TAG, "Failed to correct image orientation");
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    private void navigateToCropFragment(Uri imageUri) {
        if (imageUri != null) {
            String imagePath = imageUri.getPath();
            Bundle bundle = new Bundle();
            bundle.putString("imagePath", imagePath);
            CropFragment cropFragment = new CropFragment();
            cropFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, cropFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Log.e(TAG, "Image URI is null");
        }
    }

    private class TapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (camera != null) {
                MeteringPointFactory factory = previewView.getMeteringPointFactory();
                MeteringPoint point = factory.createPoint(e.getX(), e.getY());
                FocusMeteringAction action = new FocusMeteringAction.Builder(point).build();
                cameraControl.startFocusAndMetering(action);
            }
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (camera != null) {
                float currentZoomRatio = camera.getCameraInfo().getZoomState().getValue().getZoomRatio();
                float delta = detector.getScaleFactor();
                cameraControl.setZoomRatio(currentZoomRatio * delta);
            }
            return true;
        }
    }
}