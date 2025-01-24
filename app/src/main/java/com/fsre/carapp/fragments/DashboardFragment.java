package com.fsre.carapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.database.Cursor;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fsre.carapp.R;
import com.fsre.carapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class DashboardFragment extends Fragment {

    private static final int PICK_IMAGE = 1;
    private Button uploadImageButton;
    private Button captureButton;
    private GridLayout recentImagesLayout;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        recentImagesLayout = view.findViewById(R.id.recentImagesLayout);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        captureButton = view.findViewById(R.id.captureButton);

        uploadImageButton.setOnClickListener(v -> openGallery());
        captureButton.setOnClickListener(v -> navigateToCameraFragment());

        if (currentUser != null) {
            fetchRecentImages();
        }

        return view;
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            navigateToCropFragment(imageUri);
        }
    }

    private void fetchRecentImages() {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getImages() != null) {
                        List<String> images = user.getImages();
                        displayRecentImages(images);
                    } else {
                        Toast.makeText(getActivity(), "No recent images found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error fetching recent images.", Toast.LENGTH_SHORT).show());
    }

    private void displayRecentImages(List<String> images) {
        recentImagesLayout.removeAllViews();
        int start = Math.max(images.size() - 6, 0);
        int imageSize = getResources().getDisplayMetrics().widthPixels / 3;

        for (int i = start; i < images.size(); i++) {
            String imageBase64 = images.get(i);
            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(decodedByte, imageSize, imageSize, false);

            ImageView imageView = new ImageView(getActivity());
            imageView.setImageBitmap(resizedBitmap);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            params.setMargins(4, 4, 4, 4); // Optional: Add margins between images
            imageView.setLayoutParams(params);

            imageView.setOnLongClickListener(v -> {
                showPopupMenu(v, imageBase64);
                return true;
            });

            recentImagesLayout.addView(imageView);
        }
    }

    private void showPopupMenu(View view, String imageBase64) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.getMenuInflater().inflate(R.menu.image_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.delete_image) {
                deleteImage(imageBase64);
                return true;
            } else if (itemId == R.id.crop_image) {
                cropImage(imageBase64);
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void deleteImage(String imageBase64) {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        user.removeImage(imageBase64);
                        db.collection("users").document(currentUser.getUid()).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getActivity(), "Image deleted successfully.", Toast.LENGTH_SHORT).show();
                                    fetchRecentImages();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error deleting image.", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error fetching user details.", Toast.LENGTH_SHORT).show());
    }

    private void cropImage(String imageBase64) {
        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Uri imageUri = getImageUri(decodedByte);
        navigateToCropFragment(imageUri);
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    public void navigateToCropFragment(Uri imageUri) {
        String imagePath = getRealPathFromURI(imageUri);
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", imagePath);
        CropFragment cropFragment = new CropFragment();
        cropFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, cropFragment)
                .addToBackStack(null)
                .commit();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void navigateToCameraFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new CameraFragment())
                .addToBackStack(null)
                .commit();
    }
}