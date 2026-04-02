package com.example.safetyapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class SirenService extends Service {

    private MediaPlayer mediaPlayer;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;
    private Handler handler = new Handler();

    private static final String CHANNEL_ID = "SOS_CHANNEL";

    private Runnable flashRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (cameraManager != null && cameraId != null) {
                    isFlashOn = !isFlashOn;
                    cameraManager.setTorchMode(cameraId, isFlashOn);
                }
                handler.postDelayed(this, 300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // 🔊 Siren Sound
        mediaPlayer = MediaPlayer.create(this, R.raw.siren_sound);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
        }

        // 🔦 Flash setup
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            if (cameraManager != null && cameraManager.getCameraIdList().length > 0) {
                cameraId = cameraManager.getCameraIdList()[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔥 Notification Channel (MANDATORY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SOS Emergency Alert",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Emergency SOS Alerts");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 🔥 STRONG NOTIFICATION (FIXED)
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("🚨 SOS ALERT")
                .setContentText("Emergency alert is active!")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .build();

        startForeground(1, notification);

        // 🔊 Start Siren
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

        // 🔦 Start Flash blinking
        handler.post(flashRunnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 🔊 Stop siren
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }

        // 🔦 Stop flash
        try {
            handler.removeCallbacks(flashRunnable);
            if (cameraManager != null && cameraId != null) {
                cameraManager.setTorchMode(cameraId, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}