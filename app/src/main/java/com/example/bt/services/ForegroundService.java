package com.example.bt.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.bt.R;
import com.example.bt.activities.MainActivity;

public class ForegroundService extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "notification_channel";

    private final IBinder binder = new ForegroundServiceBinder();

    public Bluetooth bt;

    public static boolean instanceRunning = false;


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ForegroundServiceBinder extends Binder {
        public ForegroundService getService() {
            return ForegroundService.this;
        }
    }

    @Override
    public void onCreate() {
        bt = new Bluetooth(getApplicationContext());
        instanceRunning = true;
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopService();
        super.onDestroy();
    }

    public void PromoteToNotification(){
        initNotificationChannel();
        startForeground(1990, buildServiceNotification());
    }

    private void stopService() {
        stopForeground(true);
        stopSelf();
    }

    private void initNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "AppServicep", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(nc);
        }
    }

    public Notification buildServiceNotification(){
        // set up intents
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, openAppIntent, 0);

        // notification builder
        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("LED service running")
            .setContentText("Tap to open application")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.icon_pointlight)
            .build();
    }
}
