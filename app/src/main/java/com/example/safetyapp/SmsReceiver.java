package com.example.safetyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import androidx.core.content.ContextCompat;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            Bundle bundle = intent.getExtras();

            if (bundle != null) {

                Object[] pdus = (Object[]) bundle.get("pdus");

                for (Object pdu : pdus) {

                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    String message = sms.getMessageBody();

                    if (message.contains("HELP_ALERT")) {

                        Intent sirenIntent = new Intent(context, SirenService.class);

                        ContextCompat.startForegroundService(context, sirenIntent);
                    }
                }
            }
        }
    }
}