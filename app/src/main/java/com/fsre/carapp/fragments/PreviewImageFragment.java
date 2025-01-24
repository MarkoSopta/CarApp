package com.fsre.carapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.util.Linkify;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fsre.carapp.DashboardActivity;
import com.fsre.carapp.R;
import com.fsre.carapp.models.ApiResponse;
import com.fsre.carapp.models.User;
import com.fsre.carapp.services.ApiService;
import com.fsre.carapp.services.ImageOrientationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class PreviewImageFragment extends Fragment {

    private static final String TAG = "PreviewImageFragment";

    private ImageView previewImageView;

    private ImageButton retakeButton, sendButton, chooseFromGalleryButton;
    private TextView primaryResultTextView, secondaryResultTextView;


    private ProgressBar progressBar;

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

        progressBar = view.findViewById(R.id.progressBar);


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
            toggleButtonsVisibility(View.GONE);
            sendImageToApi(imageFile);
        });
        chooseFromGalleryButton.setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).openGallery();
            }
        });

        return view;
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
                String primaryResult = response.getPrimaryResult();
                displayResults(primaryResult, response.getSecondaryResult().getInfoLink());


                    addImageToRecentImages(imageFile);


                progressBar.setVisibility(View.GONE);
                toggleButtonsVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {
                displayResults("Error: " + e.getMessage(), "Error: " + e.getMessage());

                progressBar.setVisibility(View.GONE);
                toggleButtonsVisibility(View.VISIBLE);
            }
        });
    }

    private void addImageToRecentImages(File imageFile) {
        try {
            // Encode the file into a bitmap
            Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 256, 256, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();

            // Convert the bitmap to a Base64 string
            String imageBase64 = Base64.encodeToString(data, Base64.DEFAULT);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                db.collection("users").document(currentUser.getUid()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                // Check if the images field exists, if not create it
                                if (user.getImages() == null) {
                                    user.setImages(new ArrayList<>());
                                }
                                // Add the image to the list
                                user.addImage(imageBase64);
                                // Store the updated user object
                                db.collection("users").document(currentUser.getUid()).set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getActivity(), "Image added to recent images.", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getActivity(), "Error adding image to recent images.", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(getActivity(), "User data is null.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Error fetching user details.", Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error processing image.", Toast.LENGTH_SHORT).show();
        }
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