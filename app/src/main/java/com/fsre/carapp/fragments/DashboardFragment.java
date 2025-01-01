package com.fsre.carapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fsre.carapp.R;

public class DashboardFragment extends Fragment {

    private static final int PICK_IMAGE = 1;
    private Button uploadImageButton;
    private Button captureButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        captureButton = view.findViewById(R.id.captureButton);

        uploadImageButton.setOnClickListener(v -> openGallery());
        captureButton.setOnClickListener(v -> navigateToCameraFragment());

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