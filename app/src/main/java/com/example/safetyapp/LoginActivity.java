package com.example.safetyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etName, etPassword;
    Button btnLogin;
    TextView tvRegisterLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        btnLogin.setOnClickListener(v -> {

            // 🔹 Hide Keyboard
            hideKeyboard();

            String username = etName.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Enter both name and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get saved data
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

            String savedPassword = prefs.getString(username, null);
            String savedEmail = prefs.getString(username + "_email", null);

            if (savedPassword == null) {
                Toast.makeText(LoginActivity.this, "User not found! Please register.", Toast.LENGTH_SHORT).show();
            }
            else if (!savedPassword.equals(password)) {
                Toast.makeText(LoginActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
            }
            else {

                // Save logged-in user
                SharedPreferences userPrefs = getSharedPreferences("SafetyApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = userPrefs.edit();

                editor.putString("username", username);
                editor.putString("email", savedEmail);
                editor.apply();

                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                // Open Main Screen
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tvRegisterLink.setOnClickListener(v -> {

            // 🔹 Hide Keyboard
            hideKeyboard();

            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });
    }

    // 🔹 Method to hide keyboard
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}