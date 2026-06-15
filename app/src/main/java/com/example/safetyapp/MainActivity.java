package com.example.safetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.SmsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView mapPreview, btnProfile, btnSettings;
    private Button btnSOS, btnSaveContact;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastX, lastY, lastZ;
    private long lastUpdate;
    private int shakeCount = 0;

    private boolean shakeEnabled = true;

    private static final int SMS_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapPreview = findViewById(R.id.mapPreview);
        btnSOS = findViewById(R.id.btnSOS);
        btnSaveContact = findViewById(R.id.btnSaveContact);
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);

        // Load shake setting
        SharedPreferences prefs = getSharedPreferences("appSettings", MODE_PRIVATE);
        shakeEnabled = prefs.getBoolean("shakeSOS", true);

        // Initialize sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (shakeEnabled) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Open Google Map
        mapPreview.setOnClickListener(v -> {
            String uri = "geo:0,0?q=Nagpur";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        });

        // SOS Button → Sends SMS to all trusted contacts
        btnSOS.setOnClickListener(v -> sendSOSMessage());

        // Open Contact Screen
        btnSaveContact.setOnClickListener(v ->
                startActivity(new Intent(this, ContactsActivity.class))
        );

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!shakeEnabled) {
            shakeCount = 0; // reset count if disabled
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 300) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > 800) {
                    shakeCount++;
                    if (shakeCount == 2) {
                        shakeCount = 0;
                        sendSOSMessage(); // Only SMS
                    }
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    private void sendSOSMessage() {
        // Check SMS permission
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
            return;
        }

        SharedPreferences prefs = getSharedPreferences("trustedContacts", MODE_PRIVATE);
        String contactsJson = prefs.getString("contacts", null);

        String message = "SOS_ALERT_!! I Need Help!!\nMy location: http://maps.google.com/?q=0,0"; // Add actual location if needed

        try {
            SmsManager smsManager = SmsManager.getDefault();

            if (contactsJson != null) {
                JSONArray jsonArray = new JSONArray(contactsJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String number = obj.getString("number");
                    smsManager.sendTextMessage(number, null, message, null, null);
                }
                Toast.makeText(this, "SOS SMS sent to all trusted contacts!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No trusted contacts found!", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse contacts.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send SOS SMS", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("appSettings", MODE_PRIVATE);
        shakeEnabled = prefs.getBoolean("shakeSOS", true);

        if (shakeEnabled) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            sensorManager.unregisterListener(this);
            shakeCount = 0; // reset count
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. Press SOS again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied. Cannot send SOS.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}