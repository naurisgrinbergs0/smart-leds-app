package com.example.bt.services;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;

import static com.example.bt.SharedServices.DataTransfer;

public class NotificationService extends NotificationListenerService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        String color = null;

        String app = sbn.getPackageName();
        switch (app){
            case "com.whatsapp" : {
                if(sbn.getTag() == null)
                    color = "#00ff00";
                break;
            }
            case "com.snapchat" : {
                if(sbn.getTag() == null)
                    color = "#ffee00";
                break;
            }
        }

        if(color != null){
            DataTransfer.PlayNotification(Color.parseColor(color));
        }
    }
}
