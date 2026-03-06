package com.example.safetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private EditText etContact;
    private Button btnSaveContact, btnSOS, btnSiren;
    private LinearLayout profileContainer;
    private RecyclerView recyclerContacts;

    private SharedPreferences sharedPreferences;
    private MediaPlayer mediaPlayer;
    private FusedLocationProviderClient fusedLocationClient;

    private List<ContactModel> contactList;
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvWelcome = findViewById(R.id.tvWelcome);
        etContact = findViewById(R.id.etContact);
        btnSaveContact = findViewById(R.id.btnSaveContact);
        btnSOS = findViewById(R.id.btnSOS);
        btnSiren = findViewById(R.id.btnSiren);
        profileContainer = findViewById(R.id.profileContainer);
        recyclerContacts = findViewById(R.id.recyclerContacts);

        sharedPreferences = getSharedPreferences("SafetyApp", MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvWelcome.setText("Stay Safe 💜");

        contactList = new ArrayList<>();
        recyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(this, contactList);
        recyclerContacts.setAdapter(adapter);

        loadContacts();

        // UPDATED BUTTON LISTENER
        btnSaveContact.setOnClickListener(v -> {
            saveContact();
            hideKeyboard();   // ✅ keyboard hides after saving
        });

        btnSOS.setOnClickListener(v -> checkPermissionsAndSend());
        btnSiren.setOnClickListener(v -> toggleSiren());

        profileContainer.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class))
        );

        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        contactList.remove(position);
                        adapter.notifyItemRemoved(position);
                        updateSharedPreferences();

                        Toast.makeText(MainActivity.this,
                                "Contact Deleted",
                                Toast.LENGTH_SHORT).show();
                    }
                };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerContacts);
    }

    // HIDE KEYBOARD METHOD
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void saveContact() {

        String newNumber = etContact.getText().toString().trim();

        if (newNumber.length() < 10) {
            Toast.makeText(this, "Enter valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean exists = false;

        for (ContactModel c : contactList) {
            if (c.getPhone().equals(newNumber)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            contactList.add(new ContactModel("", newNumber));
            adapter.notifyItemInserted(contactList.size() - 1);
            etContact.setText("");
            updateSharedPreferences();

            Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Contact Already Exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadContacts() {

        contactList.clear();

        String contacts = sharedPreferences.getString("trusted_contacts", "");

        if (!contacts.isEmpty()) {

            String[] numbers = contacts.split(",");

            for (String number : numbers) {
                contactList.add(new ContactModel("", number.trim()));
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void updateSharedPreferences() {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < contactList.size(); i++) {
            builder.append(contactList.get(i).getPhone());

            if (i != contactList.size() - 1)
                builder.append(",");
        }

        sharedPreferences.edit()
                .putString("trusted_contacts", builder.toString())
                .apply();
    }

    private void toggleSiren() {

        try {

            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.siren);
                mediaPlayer.setLooping(true);
            }

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnSiren.setText("🔊 Start Siren");
            } else {
                mediaPlayer.start();
                btnSiren.setText("⛔ Stop Siren");
            }

        } catch (Exception e) {

            Toast.makeText(this,
                    "Siren Error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    this::handlePermissionResult
            );

    private void checkPermissionsAndSend() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            sendSOS();

        } else {

            permissionLauncher.launch(new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION
            });
        }
    }

    private void handlePermissionResult(@NonNull Map<String, Boolean> result) {

        Boolean smsGranted = result.getOrDefault(Manifest.permission.SEND_SMS, false);
        Boolean locationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);

        if (smsGranted && locationGranted) {
            sendSOS();
        } else {
            Toast.makeText(this, "Permissions Required!", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    })
    private void sendSOS() {

        if (contactList.isEmpty()) {
            Toast.makeText(this, "No Trusted Contacts Found", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    double latitude = 0.0;
                    double longitude = 0.0;

                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }

                    String message = "🚨 EMERGENCY ALERT 🚨\n\n"
                            + "I need immediate help.\n\n"
                            + "My Location:\n"
                            + "https://www.google.com/maps?q="
                            + latitude + "," + longitude;

                    sendSMS(message);
                });
    }

    private void sendSMS(String message) {

        try {

            SmsManager smsManager = SmsManager.getDefault();

            for (ContactModel c : contactList) {

                smsManager.sendMultipartTextMessage(
                        c.getPhone(),
                        null,
                        smsManager.divideMessage(message),
                        null,
                        null
                );
            }

            Toast.makeText(this,
                    "SOS Sent Successfully!",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            Toast.makeText(this,
                    "SMS Failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}