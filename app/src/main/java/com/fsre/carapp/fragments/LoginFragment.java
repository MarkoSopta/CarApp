package com.fsre.carapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.fsre.carapp.DashboardActivity;
import com.fsre.carapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private FirebaseAuth auth;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_LOGIN_TIMESTAMP = "loginTimestamp";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        ImageView logoImageView = view.findViewById(R.id.logoImageView);
        TextView welcomeTextView = view.findViewById(R.id.welcomeTextView);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        TextView forgotPasswordText = view.findViewById(R.id.forgotPasswordText);
        TextView registerText = view.findViewById(R.id.registerText);
        auth = FirebaseAuth.getInstance();

        Animation fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);

        logoImageView.startAnimation(fadeInAnimation);

        fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fadeInAnimation.setStartOffset(1000);
        welcomeTextView.startAnimation(fadeInAnimation);

        fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fadeInAnimation.setStartOffset(2000);
        emailEditText.startAnimation(fadeInAnimation);
        passwordEditText.startAnimation(fadeInAnimation);

        fadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fadeInAnimation.setStartOffset(3000);
        loginButton.startAnimation(fadeInAnimation);
        forgotPasswordText.startAnimation(fadeInAnimation);
        registerText.startAnimation(fadeInAnimation);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            storeLoginTimestamp();
                            Intent intent = new Intent(getActivity(), DashboardActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        return view;
    }

    private void storeLoginTimestamp() {
        SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }
}