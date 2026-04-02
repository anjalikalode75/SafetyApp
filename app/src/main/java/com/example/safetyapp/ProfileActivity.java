package com.example.safetyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    TextView tvUsername, tvEmail;
    Button btnEditProfile, btnLogout;

    // ✅ NEW BUTTON
    Button btnSafetyVideos;

    ImageView ivProfile;
    RecyclerView recyclerProfileContacts;

    ArrayList<String> contactList;
    ContactsAdapter adapter;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private SharedPreferences safetyPrefs;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Bind Views
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnSafetyVideos = findViewById(R.id.btnSafetyVideos); // ✅ NEW

        ivProfile = findViewById(R.id.ivProfile);
        recyclerProfileContacts = findViewById(R.id.recyclerProfileContacts);

        // RecyclerView Setup
        contactList = new ArrayList<>();
        recyclerProfileContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(this, contactList);
        recyclerProfileContacts.setAdapter(adapter);

        // SharedPreferences
        safetyPrefs = getSharedPreferences("SafetyApp", MODE_PRIVATE);
        username = safetyPrefs.getString("username", "User");

        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = userPrefs.getString(username + "_email", "No Email");

        tvUsername.setText(username);
        tvEmail.setText(email);

        loadProfilePhoto();
        loadContacts();

        // Edit Profile
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));

        // Logout
        btnLogout.setOnClickListener(v -> {
            safetyPrefs.edit().clear().apply();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // ✅ OPEN SAFETY VIDEOS
        btnSafetyVideos.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SafetyVideosActivity.class));
        });

        // Image Picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            saveProfilePhoto(selectedImage);
                        }
                    }
                }
        );

        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });
    }

    // Load Contacts
    private void loadContacts() {
        String contacts = safetyPrefs.getString("trusted_contacts", "");

        contactList.clear();

        if (!contacts.isEmpty()) {
            String[] numbers = contacts.split(",");
            for (String number : numbers) {
                contactList.add(number.trim());
            }
        }

        adapter.notifyDataSetChanged();
    }

    // Load Profile Photo
    private void loadProfilePhoto() {
        String savedPhoto = safetyPrefs.getString(username + "_photo", "");

        if (!savedPhoto.isEmpty()) {
            try {
                ivProfile.setImageURI(Uri.parse(savedPhoto));
            } catch (Exception e) {
                ivProfile.setImageResource(R.drawable.ic_profile);
            }
        } else {
            ivProfile.setImageResource(R.drawable.ic_profile);
        }
    }

    // Save Profile Photo
    private void saveProfilePhoto(Uri uri) {
        safetyPrefs.edit().putString(username + "_photo", uri.toString()).apply();
        ivProfile.setImageURI(uri);
    }
}