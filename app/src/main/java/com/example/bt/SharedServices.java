package com.example.bt;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bt.activities.ActivityHelper;
import com.example.bt.services.ForegroundService;

import java.util.ArrayList;

public class SharedServices {

    public static class Service{
        public static String FOREGROUND = "service_foreground";

        private static android.app.Service[] services = new android.app.Service[1];
        private static String[] serviceIds = new String[1];

        public static android.app.Service Get(String id){
            for (byte i = 0; i < serviceIds.length; i++)
                if(serviceIds[i] == id)
                    return services[i];
            return null;
        }

        public static void Add(String id, android.app.Service activity){
            for (byte i = 0; i < serviceIds.length; i++)
                if(serviceIds[i] == null){
                    serviceIds[i] = id;
                    services[i] = activity;
                    return;
                }
        }

        public static void Remove(String id){
            for (byte i = 0; i < serviceIds.length; i++)
                if(serviceIds[i] == id){
                    serviceIds[i] = null;
                    services[i] = null;
                    return;
                }
        }

        public static boolean IsServiceRunning(Context context, String id) {
            android.app.Service service = Get(id);
            if(service == null)
                return false;
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo s : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (service.getClass().getName().equals(s.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class Activity{
        public static String MAIN = "activity_main";
        public static String SETTINGS = "activity_settings";
        public static String MUSIC = "activity_music";
        public static String NOTIFICATION_EVENTS = "activity_notification_events";
        public static String COLOR_PICK = "activity_color_pick";

        private static android.app.Activity[] activities = new android.app.Activity[5];
        private static String[] activityIds = new String[5];

        public static android.app.Activity Get(String id){
            for (byte i = 0; i < activityIds.length; i++)
                if(activityIds[i] == id)
                    return activities[i];
            return null;
        }

        public static void Add(String id, android.app.Activity activity){
            for (byte i = 0; i < activityIds.length; i++)
                if(activityIds[i] == null){
                    activityIds[i] = id;
                    activities[i] = activity;
                    return;
                }
        }

        public static void Remove(String id){
            for (byte i = 0; i < activityIds.length; i++)
                if(activityIds[i] == id){
                    activityIds[i] = null;
                    activities[i] = null;
                    return;
                }
        }

        public static void PassCallback(Intent intent){
            for (byte i = 0; i < activityIds.length; i++)
                if(activityIds[i] != null)
                    ((ActivityHelper)activities[i]).ActionCallback(intent);
        }
    }

    public static class DataTransfer{

        public static void SetPlainColor(int color){
            send(mergeBytes(new byte[]{ 0b00000001, (byte)',' }, colorToRgbString(color).getBytes()));
        }

        public static void SetSmoothColor(int color){
            send(mergeBytes(new byte[]{ 0b00000010, (byte)',' }, colorToRgbString(color).getBytes()));
        }

        public static void PlayNotification(int color){
            send(mergeBytes(new byte[]{ 0b00000011, (byte)',' }, colorToRgbString(color).getBytes()));
        }

        public static void SetDuration(int duration){
            send(mergeBytes(new byte[]{ 0b00000101, (byte)',' }, zeroFill(duration, 4).getBytes()));
        }

        public static void Reboot(){
            send(new byte[]{ 0b00000111 });
        }


        private static String zeroFill(int value, int numbers){
            String zeros = "0000";
            if(value >= 1000)
                return zeros.substring(0, numbers-4) + String.valueOf(value);
            if(value >= 100)
                return zeros.substring(0, numbers-3) + String.valueOf(value);
            else if(value >= 10)
                return zeros.substring(0, numbers-2) + String.valueOf(value);
            else
                return zeros.substring(0, numbers-1) + String.valueOf(value);
        }

        private static String colorToRgbString(int color){
            return String.format("%1$s,%2$s,%3$s",
                    zeroFill(Color.red(color), 3),
                    zeroFill(Color.green(color), 3),
                    zeroFill(Color.blue(color), 3));
        }

        private static void send(byte[] bytes){
            ((ForegroundService)Service.Get(Service.FOREGROUND)).bt.Send(mergeBytes(bytes, ";".getBytes()));
        }

        private static byte[] mergeBytes(byte[] ... byteArrays){
            ArrayList<Byte> allBytes = new ArrayList<>();
            for(byte[] bytes : byteArrays)
                for(byte b : bytes)
                    allBytes.add(b);
            byte[] result = new byte[allBytes.size()];
            for (int i = 0; i < result.length; i++)
                result[i] = allBytes.get(i);
            return result;
        }
    }

    public static class Permission{
        public static final String PERMISSION_BLUETOOTH = "permission_bluetooth";
        public static void RequestPermission(String permission, Context context, android.app.Activity activity, int code){
            switch (permission){
                case PERMISSION_BLUETOOTH:{
                    if(ContextCompat.checkSelfPermission(context
                            , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        // not granted
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, code);
                    }
                }
            }
        }
    }
}
