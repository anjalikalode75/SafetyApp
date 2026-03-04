package com.example.safetyapp;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditProfileDialog extends Dialog {

    public EditProfileDialog(Context context, TextView tvName, TextView tvEmail) {
        super(context);
        setContentView(R.layout.dialog_edit_profile);

        EditText etName = findViewById(R.id.etEditName);
        EditText etEmail = findViewById(R.id.etEditEmail);
        Button btnSave = findViewById(R.id.btnSaveProfile);

        // Pre-fill existing info
        etName.setText(tvName.getText());
        etEmail.setText(tvEmail.getText());

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (!newName.isEmpty() && !newEmail.isEmpty()) {
                tvName.setText(newName);
                tvEmail.setText(newEmail);

                // Save to SharedPreferences
                SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                prefs.edit().putString("name", newName).putString("email", newEmail).apply();

                dismiss();
            }
        });
    }
}