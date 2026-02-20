package com.example.safetyapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
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

    private EditText etContact;
    private Button btnSaveContact, btnSOS, btnSiren;
    private RecyclerView recyclerContacts;

    private SharedPreferences sharedPreferences;
    private MediaPlayer mediaPlayer;
    private FusedLocationProviderClient fusedLocationClient;

    private List<String> contactList;
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // INIT VIEWS
        etContact = findViewById(R.id.etContact);
        btnSaveContact = findViewById(R.id.btnSaveContact);
        btnSOS = findViewById(R.id.btnSOS);
        btnSiren = findViewById(R.id.btnSiren);
        recyclerContacts = findViewById(R.id.recyclerContacts);

        sharedPreferences = getSharedPreferences("SafetyApp", MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // INIT LIST
        contactList = new ArrayList<>();
        recyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(contactList, this);
        recyclerContacts.setAdapter(adapter);

        loadContacts();

        // BUTTONS
        btnSaveContact.setOnClickListener(v -> saveContact());
        btnSOS.setOnClickListener(v -> checkPermissionsAndSend());
        btnSiren.setOnClickListener(v -> toggleSiren());

        // SWIPE TO DELETE
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // we don’t support drag & drop
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                String removedContact = contactList.get(position);

                // Remove from list
                contactList.remove(position);
                adapter.notifyItemRemoved(position);

                // Update SharedPreferences
                updateSharedPreferences();

                Toast.makeText(MainActivity.this,
                        "Contact Deleted: " + removedContact,
                        Toast.LENGTH_SHORT).show();
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerContacts);
    }

    // SAVE CONTACT
    private void saveContact() {
        String newNumber = etContact.getText().toString().trim();

        if (newNumber.length() < 10) {
            Toast.makeText(this, "Enter valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!contactList.contains(newNumber)) {
            contactList.add(newNumber);
            adapter.notifyItemInserted(contactList.size() - 1);
            etContact.setText("");

            updateSharedPreferences();

            Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Contact Already Exists", Toast.LENGTH_SHORT).show();
        }
    }

    // LOAD CONTACTS
    private void loadContacts() {
        contactList.clear();
        String contacts = sharedPreferences.getString("trusted_contacts", "");
        if (!contacts.isEmpty()) {
            String[] numbers = contacts.split(",");
            for (String number : numbers) {
                contactList.add(number.trim());
            }
        }
        adapter.notifyDataSetChanged();
    }

    // UPDATE SHARED PREFERENCES
    private void updateSharedPreferences() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < contactList.size(); i++) {
            builder.append(contactList.get(i));
            if (i != contactList.size() - 1) builder.append(",");
        }
        sharedPreferences.edit().putString("trusted_contacts", builder.toString()).apply();
    }

    // CHECK PERMISSIONS
    private void checkPermissionsAndSend() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            sendSOS();
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    this::handlePermissionResult);

    private void handlePermissionResult(@NonNull Map<String, Boolean> result) {
        Boolean smsGranted = result.getOrDefault(Manifest.permission.SEND_SMS, false);
        Boolean locationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);

        if (smsGranted && locationGranted) {
            sendSOS();
        } else {
            Toast.makeText(this, "Permissions Required!", Toast.LENGTH_SHORT).show();
        }
    }

    // SEND SOS
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void sendSOS() {
        String contacts = sharedPreferences.getString("trusted_contacts", "");
        if (contacts.isEmpty()) {
            Toast.makeText(this, "No Trusted Contacts Found", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            double latitude = 0.0;
            double longitude = 0.0;

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            String message = "🚨 EMERGENCY ALERT 🚨\n" +
                    "I need immediate help.\n" +
                    "My Location:\n" +
                    "https://www.google.com/maps?q=" + latitude + "," + longitude;

            sendSMS(contacts, message);
        });
    }

    private void sendSMS(String contacts, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String[] numbers = contacts.split(",");
            for (String number : numbers) {
                smsManager.sendMultipartTextMessage(number.trim(), null,
                        smsManager.divideMessage(message), null, null);
            }
            Toast.makeText(this, "SOS Sent Successfully!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // SIREN
    private void toggleSiren() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.siren);
                if (mediaPlayer == null) {
                    Toast.makeText(this, "Add siren.mp3 in res/raw", Toast.LENGTH_SHORT).show();
                    return;
                }
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
            Toast.makeText(this, "Siren Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
}