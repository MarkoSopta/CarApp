package com.fsre.carapp.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fsre.carapp.MainActivity;
import com.fsre.carapp.R;
import com.fsre.carapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText firstnameEditText;
    private EditText lastnameEditText;
    private EditText emailEditText;
    private EditText dobEditText;
    private Button saveButton;
    private Button uploadImageButton;
    private Button deleteAccountButton;
    private ImageView profileImageView;
    private TextView accountCreationDateTextView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private final Calendar calendar = Calendar.getInstance();
    private String profileImageUrl;
    private Button changePasswordButton;
    private EditText newPasswordEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firstnameEditText = view.findViewById(R.id.firstnameEditText);
        lastnameEditText = view.findViewById(R.id.lastnameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        dobEditText = view.findViewById(R.id.dobEditText);
        saveButton = view.findViewById(R.id.saveButton);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);

        profileImageView = view.findViewById(R.id.profileImageView);
        accountCreationDateTextView = view.findViewById(R.id.accountCreationDateTextView);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            emailEditText.setText(currentUser.getEmail());
            fetchUserDetails();
        }

        changePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString();
            if (newPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter a new password.", Toast.LENGTH_SHORT).show();
                return;
            }
            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Password updated successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Error updating password.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dobEditText.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        uploadImageButton.setOnClickListener(v -> openFileChooser());

        saveButton.setOnClickListener(v -> {
            final String firstname = firstnameEditText.getText().toString();
            final String lastname = lastnameEditText.getText().toString();
            final String email = currentUser.getEmail();
            final String dob = dobEditText.getText().toString();
            final String profileImageUrl = this.profileImageUrl;

            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                String updatedFirstname = firstname.isEmpty() ? user.getFirstname() : firstname;
                                String updatedLastname = lastname.isEmpty() ? user.getLastname() : lastname;
                                String updatedDob = dob.isEmpty() ? user.getDob() : dob;
                                String updatedProfileImageUrl = profileImageUrl == null ? user.getProfileImageUrl() : profileImageUrl;

                                User updatedUser = new User(updatedFirstname, updatedLastname, email, updatedDob, updatedProfileImageUrl);
                                db.collection("users").document(currentUser.getUid()).set(updatedUser)
                                        .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Profile updated successfully.", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error updating profile.", Toast.LENGTH_SHORT).show());
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error fetching user details.", Toast.LENGTH_SHORT).show());
        });

        deleteAccountButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        currentUser.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        db.collection("users").document(currentUser.getUid()).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(getActivity(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                                    startActivity(intent);
                                                    getActivity().finish();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error deleting account.", Toast.LENGTH_SHORT).show());
                                    } else {
                                        Toast.makeText(getActivity(), "Error deleting account.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return view;
    }

    private void fetchUserDetails() {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            firstnameEditText.setText(user.getFirstname());
                            lastnameEditText.setText(user.getLastname());
                            emailEditText.setText(user.getEmail());
                            dobEditText.setText(user.getDob());
                            accountCreationDateTextView.setText("Account Creation Date: " + user.getDate().toString());

                            if (user.getProfileImageUrl() != null) {
                                byte[] decodedString = Base64.decode(user.getProfileImageUrl(), Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                profileImageView.setImageBitmap(decodedByte);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error fetching user details.", Toast.LENGTH_SHORT).show());
    }

    private final DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateLabel();
    };

    private void updateLabel() {
        String myFormat = "dd/MM/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMAN);
        dobEditText.setText(sdf.format(calendar.getTime()));
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            profileImageUrl = Base64.encodeToString(data, Base64.DEFAULT);
            Toast.makeText(getActivity(), "Image uploaded successfully.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error uploading image.", Toast.LENGTH_SHORT).show();
        }
    }
}