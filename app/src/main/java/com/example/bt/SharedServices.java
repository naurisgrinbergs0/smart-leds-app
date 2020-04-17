package com.example.bt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bt.activities.MainActivity;
import com.example.bt.activities.SettingsActivity;

import java.util.ArrayList;

public class SharedServices {
    public static MainActivity aMain;
    public static SettingsActivity aSettings;

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
            aMain.service.bt.Send(mergeBytes(bytes, ";".getBytes()));
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
        public static void RequestPermission(String permission, Context context, Activity activity, int code){
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
