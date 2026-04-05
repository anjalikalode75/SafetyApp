package com.example.safetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
    private Button btnSaveContact, btnSOS, btnLiveTracking;
    private ImageView btnSettings, btnProfile;

    private RecyclerView recyclerContacts;

    private SharedPreferences sharedPreferences;
    private FusedLocationProviderClient fusedLocationClient;

    private List<String> contactList;
    private ContactsAdapter adapter;

    // 🔥 SHAKE VARIABLES
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float lastX, lastY, lastZ;
    private long lastTime = 0;
    private static final int SHAKE_THRESHOLD = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etContact = findViewById(R.id.etContact);
        btnSaveContact = findViewById(R.id.btnSaveContact);
        btnSOS = findViewById(R.id.btnSOS);
        btnSettings = findViewById(R.id.btnSettings);
        btnLiveTracking = findViewById(R.id.btnLiveTracking);
        btnProfile = findViewById(R.id.btnProfile);
        recyclerContacts = findViewById(R.id.recyclerContacts);

        sharedPreferences = getSharedPreferences("SafetyApp", MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 🔥 SENSOR
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        contactList = new ArrayList<>();
        recyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(this, contactList);
        recyclerContacts.setAdapter(adapter);

        loadContacts();

        btnSaveContact.setOnClickListener(v -> saveContact());
        btnSOS.setOnClickListener(v -> checkPermissionsAndSend());

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        btnLiveTracking.setOnClickListener(v ->
                startActivity(new Intent(this, LiveTrackingActivity.class)));

        // Swipe delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
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
                Toast.makeText(MainActivity.this, "Contact Deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerContacts);
    }

    // 🔥 SHAKE DETECTION
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastTime) > 100) {

                long diffTime = currentTime - lastTime;
                lastTime = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ)
                        / diffTime * 10000;

                SharedPreferences prefs = getSharedPreferences("SafetyAppSettings", MODE_PRIVATE);
                boolean shakeEnabled = prefs.getBoolean("shake", false);

                if (shakeEnabled && speed > SHAKE_THRESHOLD) {
                    Toast.makeText(MainActivity.this, "Shake Detected 🚨", Toast.LENGTH_SHORT).show();
                    checkPermissionsAndSend();
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }

    private void saveContact() {
        String newNumber = etContact.getText().toString().trim();

        if (newNumber.isEmpty()) {
            Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newNumber.length() < 10) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!contactList.contains(newNumber)) {
            contactList.add(newNumber);
            adapter.notifyDataSetChanged();
            etContact.setText("");
            updateSharedPreferences();
            Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Already Exists", Toast.LENGTH_SHORT).show();
        }
    }

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

    private void updateSharedPreferences() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < contactList.size(); i++) {
            builder.append(contactList.get(i));
            if (i != contactList.size() - 1)
                builder.append(",");
        }

        sharedPreferences.edit()
                .putString("trusted_contacts", builder.toString())
                .apply();
    }

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

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    this::handlePermissionResult
            );

    private void handlePermissionResult(@NonNull Map<String, Boolean> result) {
        if (result.getOrDefault(Manifest.permission.SEND_SMS, false)
                && result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
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

                    double lat = (location != null) ? location.getLatitude() : 0;
                    double lon = (location != null) ? location.getLongitude() : 0;

                    String message = "SOS_ALERT 🚨\n"
                            + "I need help!\n"
                            + "https://maps.google.com/?q=" + lat + "," + lon;

                    sendSMS(message);
                });
    }

    private void sendSMS(String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();

            for (String number : contactList) {
                ArrayList<String> parts = smsManager.divideMessage(message);

                smsManager.sendMultipartTextMessage(
                        number,
                        null,
                        parts,
                        null,
                        null
                );
            }

            Toast.makeText(this, "SOS Sent Successfully!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "SMS Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}