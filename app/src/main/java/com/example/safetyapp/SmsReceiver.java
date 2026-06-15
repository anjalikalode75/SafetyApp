package com.example.safetyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = bundle.getString("format");

                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage sms;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                        } else {
                            sms = SmsMessage.createFromPdu((byte[]) pdu);
                        }

                        String message = sms.getMessageBody();

                        // ✅ Check SOS keyword
                        if (message != null && message.contains("🚨 SOS ALERT!! I need help! My location: ")) {
                            Toast.makeText(context, "SOS Alert Received!", Toast.LENGTH_SHORT).show();

                            // Start siren + flash on trusted contact's phone
                            Intent serviceIntent = new Intent(context, SirenService.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent);
                            } else {
                                context.startService(serviceIntent);
                            }
                        }
                    }
                }
            }
        }
    }
}