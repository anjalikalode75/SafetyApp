package com.example.safetyapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    TextView tvUsername, tvEmail;
    Button btnEditProfile, btnLogout, btnSafetyVideos;
    ImageView ivProfile;
    RecyclerView recyclerProfileContacts;

    List<Contact> contactList;
    ContactsAdapter adapter;

    private ActivityResultLauncher<android.content.Intent> pickImageLauncher;
    private SharedPreferences safetyPrefs;
    private String username;

    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Bind Views
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnSafetyVideos = findViewById(R.id.btnSafetyVideos);
        ivProfile = findViewById(R.id.ivProfile);
        recyclerProfileContacts = findViewById(R.id.recyclerProfileContacts);

        // SharedPreferences
        safetyPrefs = getSharedPreferences("SafetyApp", MODE_PRIVATE);
        username = safetyPrefs.getString("username", "User");

        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = userPrefs.getString(username + "_email", "No Email");

        tvUsername.setText(username);
        tvEmail.setText(email);

        // Request Storage Permissions
        checkStoragePermission();

        // Load contacts
        contactList = new ArrayList<>();
        loadContacts();

        // Setup RecyclerView
        adapter = new ContactsAdapter(this, contactList);
        recyclerProfileContacts.setLayoutManager(new LinearLayoutManager(this));
        recyclerProfileContacts.setAdapter(adapter);

        // Swipe-to-delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                removeContact(pos);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerProfileContacts);

        // Edit Profile
        btnEditProfile.setOnClickListener(v ->
                startActivity(new android.content.Intent(ProfileActivity.this, EditProfileActivity.class)));

        // Logout
        btnLogout.setOnClickListener(v -> {
            safetyPrefs.edit().clear().apply();
            android.content.Intent intent = new android.content.Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Safety Videos
        btnSafetyVideos.setOnClickListener(v ->
                startActivity(new android.content.Intent(ProfileActivity.this, SafetyVideosActivity.class)));

        // Image Picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) saveProfilePhoto(selectedImage);
                    }
                }
        );

        ivProfile.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });
    }

    // ---------------- PERMISSION CHECK ----------------
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            } else {
                loadProfilePhoto();
            }
        } else {
            // Below Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                loadProfilePhoto();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadProfilePhoto();
            }
        }
    }

    // ---------------- CONTACTS ----------------
    private void loadContacts() {
        contactList.clear();
        String contacts = safetyPrefs.getString("trusted_contacts", "");
        if (!contacts.isEmpty()) {
            String[] items = contacts.split(",");
            for (String item : items) {
                if (item.contains("-")) {
                    String[] parts = item.split("-", 2);
                    String name = parts[0].trim();
                    String number = parts[1].trim();
                    contactList.add(new Contact(name, number));
                }
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void removeContact(int position) {
        contactList.remove(position);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contactList.size(); i++) {
            Contact c = contactList.get(i);
            sb.append(c.getName()).append(" - ").append(c.getNumber());
            if (i < contactList.size() - 1) sb.append(",");
        }
        safetyPrefs.edit().putString("trusted_contacts", sb.toString()).apply();
        adapter.notifyItemRemoved(position);
    }

    // ---------------- PROFILE PHOTO ----------------
    private void loadProfilePhoto() {
        String savedPhoto = safetyPrefs.getString(username + "_photo", "");
        if (!savedPhoto.isEmpty()) {
            File file = new File(savedPhoto);
            if (file.exists()) {
                ivProfile.setImageURI(Uri.fromFile(file));
            } else {
                ivProfile.setImageResource(R.drawable.ic_profile);
            }
        } else {
            ivProfile.setImageResource(R.drawable.ic_profile);
        }
    }

    private void saveProfilePhoto(Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "profile_photo.png");
            OutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.close();
            input.close();

            safetyPrefs.edit().putString(username + "_photo", file.getAbsolutePath()).apply();
            ivProfile.setImageURI(Uri.fromFile(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}