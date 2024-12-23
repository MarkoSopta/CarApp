package com.fsre.carapp.fragments;

import android.content.Intent;
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
import androidx.fragment.app.FragmentTransaction;

import com.fsre.carapp.MainActivity;
import com.fsre.carapp.R;
import com.fsre.carapp.fragments.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterFragment extends Fragment {

    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        auth = FirebaseAuth.getInstance();

        EditText emailEditText = view.findViewById(R.id.emailEditText);
        EditText passwordEditText = view.findViewById(R.id.passwordEditText);
        EditText confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        Button registerButton = view.findViewById(R.id.registerButton);


        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty() && password.isEmpty())  {
                Toast.makeText(getActivity(), "Please enter the correct credentials.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                    if (isNewUser) {
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(getActivity(), createUserTask -> {
                                    if (createUserTask.isSuccessful()) {
                                        startActivity(new Intent(getActivity(), MainActivity.class));
                                        getActivity().finish();
                                    } else {
                                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getActivity(), "Email is already registered.", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
        });
        return view;
    }
}