package com.example.safetyapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchShake, switchAutoSiren, switchFlash, switchDarkMode;
    private Button btnTestSOS, btnAbout;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // ✅ SharedPreferences
        preferences = getSharedPreferences("SafetyAppSettings", MODE_PRIVATE);

        // ✅ Initialize views
        switchShake = findViewById(R.id.switchShake);
        switchAutoSiren = findViewById(R.id.switchAutoSiren);
        switchFlash = findViewById(R.id.switchFlash);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        btnTestSOS = findViewById(R.id.btnTestSOS);
        btnAbout = findViewById(R.id.btnAbout);

        // ✅ Load saved values
        switchShake.setChecked(preferences.getBoolean("shake", false));
        switchAutoSiren.setChecked(preferences.getBoolean("siren", true));
        switchFlash.setChecked(preferences.getBoolean("flash", true));
        switchDarkMode.setChecked(preferences.getBoolean("darkMode", false));

        // ✅ Switch listeners
        switchShake.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("shake", isChecked).apply();
            Toast.makeText(this, "Shake SOS " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });

        switchAutoSiren.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("siren", isChecked).apply();
            Toast.makeText(this, "Siren " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        switchFlash.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("flash", isChecked).apply();
            Toast.makeText(this, "Flash " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        // 🌙 Dark Mode
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("darkMode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(this, "Dark Mode Enabled 🌙", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this, "Light Mode Enabled ☀️", Toast.LENGTH_SHORT).show();
            }
        });

        // 🚨 Test SOS
        btnTestSOS.setOnClickListener(v ->
                Toast.makeText(this, "Test SOS Triggered 🚨", Toast.LENGTH_SHORT).show()
        );

        // ℹ️ About
        btnAbout.setOnClickListener(v ->
                Toast.makeText(this, "Safety App v1.0\nMade by Anjali 💜", Toast.LENGTH_LONG).show()
        );
    }
}