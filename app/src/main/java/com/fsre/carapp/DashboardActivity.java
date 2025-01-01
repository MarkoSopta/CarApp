package com.fsre.carapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.database.Cursor;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.fsre.carapp.fragments.CameraFragment;
import com.fsre.carapp.fragments.CropFragment;
import com.fsre.carapp.fragments.DashboardFragment;
import com.fsre.carapp.fragments.ProfileFragment;
import com.google.android.material.navigation.NavigationView;

public class DashboardActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                } else if (id == R.id.nav_logout) {
                    signOut();
                } else if (id == R.id.nav_dashboard) {
                    selectedFragment = new DashboardFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, selectedFragment)
                            .addToBackStack(null)
                            .commit();
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        checkAndLoadDefaultFragment();
    }

    private void checkAndLoadDefaultFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getFragments().isEmpty()) {
            findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, new DashboardFragment())
                    .commit();
        }
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            navigateToCropFragment(imageUri);
        }
    }

    private void navigateToCropFragment(Uri imageUri) {
        String imagePath = getRealPathFromURI(imageUri);
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", imagePath);
        CropFragment cropFragment = new CropFragment();
        cropFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, cropFragment)
                .addToBackStack(null)
                .commit();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void signOut() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}