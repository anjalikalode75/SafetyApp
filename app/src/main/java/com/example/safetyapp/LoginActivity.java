package com.example.safetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etName;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.etName);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = etName.getText().toString();

                if (username.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Enter your name", Toast.LENGTH_SHORT).show();
                } else {

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                }
            }
        });
    }
}