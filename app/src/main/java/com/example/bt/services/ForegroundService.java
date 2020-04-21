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

public class ForegroundService extends Service {

    private final IBinder binder = new ForegroundServiceBinder();

    public Bluetooth bt;

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
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d("FGSERV", "Destroying");
        stopService();
        super.onDestroy();
    }

    public void PromoteToNotification(){
        startForeground(NotificationCreator.getNotificationId(),
                NotificationCreator.getNotification(getApplicationContext()));
    }

    public void DemoteFromNotification(){
        stopForeground(true);
    }

    private void stopService() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("FGSERV", "startCommand");
        return super.onStartCommand(intent, flags, startId);
    }
}
