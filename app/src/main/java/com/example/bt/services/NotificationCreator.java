package com.example.bt.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.bt.R;
import com.example.bt.activities.MainActivity;

public class NotificationCreator {

    public static final String NOTIFICATION_CHANNEL_ID = "notification_channel";
    private static final int NOTIFICATION_ID = 1094;

    private static Notification notification;

    public static Notification getNotification(Context context) {

        if(notification == null) {
            // set up intents
            Intent openAppIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, 0);

            // notification builder
            initNotificationChannel(context);
            notification = buildServiceNotification(context);
        }
        return notification;
    }

    private static void initNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "AppServicep", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(nc);
        }
    }

    private static Notification buildServiceNotification(Context context){
        // set up intents
        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, 0);

        // notification builder
        return new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("LED service running")
                .setContentText("Tap to open application")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.icon_pointlight)
                .build();
    }

    public static int getNotificationId() {
        return NOTIFICATION_ID;
    }
}