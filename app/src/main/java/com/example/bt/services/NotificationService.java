package com.example.bt.services;

import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.bt.MemoryConnector;
import com.example.bt.R;
import com.example.bt.SharedServices;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationService extends NotificationListenerService {

    public static boolean LISTENER_CONNECTED = false;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("FG", sbn.getPackageName());
        if(!LISTENER_CONNECTED
        || !MemoryConnector.getBool(this, this.getString(R.string.var_notif_events)))
            return;

        super.onNotificationPosted(sbn);
        // get color - package relations from file
        JSONObject colorsJson = MemoryConnector.readJsonFromFile(getApplicationContext(),
                this.getString(R.string.file_name));
        if(colorsJson != null){
            String packageName = sbn.getPackageName();

            if(colorsJson.has(packageName)) {
                try {
                    int color = colorsJson.getInt(packageName);
                    Log.d("FG", String.valueOf(color));
                    SharedServices.DataTransfer.PlayNotification(color);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        LISTENER_CONNECTED = true;
        Log.d("FG", "onListenerConnected: ");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        LISTENER_CONNECTED = false;
        Log.d("FG", "onListenerDisconnected: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("FG", "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }
}
