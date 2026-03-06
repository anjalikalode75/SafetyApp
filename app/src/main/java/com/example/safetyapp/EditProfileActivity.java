package com.example.safetyapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etUsername, etEmail;
    private Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnCancel = findViewById(R.id.btnCancel); // Make sure you have this button in XML

        // 🔹 Get logged-in user
        SharedPreferences safetyPrefs = getSharedPreferences("SafetyApp", MODE_PRIVATE);
        String username = safetyPrefs.getString("username", "");

        // 🔹 Get email from UserPrefs
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = userPrefs.getString(username + "_email", "");

        // 🔹 Show current data
        etUsername.setText(username);
        etEmail.setText(email);

        // 🔹 Save button
        btnSave.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

            // 🔹 Save updated email
            SharedPreferences.Editor editor = userPrefs.edit();
            editor.putString(newUsername + "_email", newEmail);
            editor.apply();

            // 🔹 Update logged user
            SharedPreferences.Editor safetyEditor = safetyPrefs.edit();
            safetyEditor.putString("username", newUsername);
            safetyEditor.apply();

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish(); // Go back to ProfileActivity
        });

        // 🔹 Cancel button
        btnCancel.setOnClickListener(v -> {
            // Simply close EditProfileActivity without saving
            finish();
        });
    }
}