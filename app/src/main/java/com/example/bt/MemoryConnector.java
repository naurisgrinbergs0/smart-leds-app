package com.example.bt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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

    public static int getInt(Context context, String name, int defVal) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        return sharedPref.getInt(name, defVal);
    }

    public static void setInt(Context context, String name, int value) {
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(name, value);
        editor.apply();
    }


    public static void writeJsonToFile(Context context, String fileName, JSONObject data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data.toString());
            outputStreamWriter.close();
        }
        catch (Exception e) {}
    }


    public static JSONObject readJsonFromFile(Context context, String fileName) {
        // check if file exists
        File file = context.getFileStreamPath(fileName);
        if(file == null || !file.exists()) {
            return null;
        }

        // try to get string and return json
        String ret = "";
        try {
            InputStream inputStream = context.openFileInput(fileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
                return new JSONObject(ret);
            }
        }
        catch (Exception e) {}
        return null;
    }
}
