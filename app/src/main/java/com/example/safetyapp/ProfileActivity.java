package com.example.safetyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    TextView tvProfileName, tvProfileEmail;
    ImageView ivProfile;
    Button btnLogout, btnEditProfile;
    RecyclerView recyclerContacts;
    List<ContactModel> contactList;
    ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        ivProfile = findViewById(R.id.ivProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        recyclerContacts = findViewById(R.id.recyclerProfileContacts);

        // Load username & email
        String username = getIntent().getStringExtra("username");
        if (username == null) username = "User";

        SharedPreferences prefs = getSharedPreferences("SafetyApp", MODE_PRIVATE);
        String email = prefs.getString("email", "user@example.com");

        tvProfileName.setText(username);
        tvProfileEmail.setText(email);

        // Load trusted contacts
        contactList = new ArrayList<>();
        String contacts = prefs.getString("trusted_contacts", "");
        if (!contacts.isEmpty()) {
            String[] numbers = contacts.split(",");
            for (String number : numbers) {
                contactList.add(new ContactModel("", number.trim())); // empty name
            }
        }

        // Setup RecyclerView
        adapter = new ContactAdapter(this, contactList);
        recyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        recyclerContacts.setAdapter(adapter);

        // Logout
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Edit profile
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }
}