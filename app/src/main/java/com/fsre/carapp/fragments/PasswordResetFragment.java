package com.fsre.carapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.fsre.carapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetFragment extends Fragment {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_reset, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        resetPasswordButton = view.findViewById(R.id.resetPasswordButton);
        auth = FirebaseAuth.getInstance();

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter your email.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Error sending password reset email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        return view;
    }
}