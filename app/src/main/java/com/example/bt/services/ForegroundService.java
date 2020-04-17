package com.example.bt.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.bt.MemoryConnector;
import com.example.bt.R;
import com.example.bt.SharedServices;
import com.example.bt.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class ForegroundService extends NotificationListenerService {

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
        Log.d("FGSERV", "Creating");
        bt = new Bluetooth(getApplicationContext());
        instanceRunning = true;
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d("FGSERV", "Destroying");
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


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        // get color - package relations from file
        JSONObject colorsJson = MemoryConnector.readJsonFromFile(getApplicationContext(),
                this.getString(R.string.file_name));
        String packageName = sbn.getPackageName();
        Log.d("FGSERV", packageName);

        if(colorsJson.has(packageName)) {
            try {
                int color = colorsJson.getInt(packageName);
                Log.d("FGSERV", String.valueOf(color));
                SharedServices.DataTransfer.PlayNotification(color);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d("FGSERV", "listenerConnected");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d("FGSERV", "listenerDisconnected");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("FGSERV", "startCommand");
        return super.onStartCommand(intent, flags, startId);
    }
}
