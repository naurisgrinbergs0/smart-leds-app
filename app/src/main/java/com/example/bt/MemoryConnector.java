package com.example.bt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class MemoryConnector {

    private static String sharedPreferencesFileName = "shared_preferences";

    public static String getString(Context context, String name){
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        return sharedPref.getString(name, null);
    }

    public static void setString(Context context, String name, String value){
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public static boolean getBool(Context context, String name) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(name, false);
    }

    public static void setBool(Context context, String name, Boolean value) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    public static int getInt(Context context, String name) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        return sharedPref.getInt(name, 0);
    }

    public static void setInt(Context context, String name, int value) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(name, value);
        editor.apply();
    }
}
