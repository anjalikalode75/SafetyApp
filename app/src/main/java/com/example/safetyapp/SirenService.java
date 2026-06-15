package com.example.safetyapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class SirenService extends Service {

    private MediaPlayer mediaPlayer;
    private CameraManager cameraManager;
    private String cameraId;

    @Override
    public void onCreate() {
        super.onCreate();

        // 🔊 Initialize siren
        mediaPlayer = MediaPlayer.create(this, R.raw.siren_sound);

        // 🔦 Initialize camera flash
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // ⚠️ Foreground service for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Siren_Channel";
            NotificationChannel channel = new NotificationChannel(channelId, "Siren Service", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("SOS Alert Received")
                    .setContentText("Flash & Siren are active")
                    .setSmallIcon(R.drawable.ic_alert)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            startForeground(1, builder.build());
        }
    }

    @Override
    public int onStartCommand(@Nullable android.content.Intent intent, int flags, int startId) {
        triggerAlert();
        return START_NOT_STICKY;
    }

    private void triggerAlert() {
        // 🔊 Start siren
        if (!mediaPlayer.isPlaying()) mediaPlayer.start();

        // 🔦 Turn on flash
        try {
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // ⏱ Stop after 10 seconds
        new Handler().postDelayed(this::stopAlert, 10000);
    }

    private void stopAlert() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
        if (mediaPlayer != null) mediaPlayer.release();

        try {
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
        }
        try {
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(android.content.Intent intent) {
        return null;
    }
}