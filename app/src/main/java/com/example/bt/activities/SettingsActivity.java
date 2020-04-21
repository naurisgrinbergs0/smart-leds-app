package com.example.bt.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bt.MemoryConnector;
import com.example.bt.R;
import com.example.bt.SharedServices;
import com.example.bt.services.Bluetooth;
import com.example.bt.services.ForegroundService;
import com.example.bt.services.NotificationCreator;
import com.example.bt.services.NotificationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.example.bt.SharedServices.*;
import static com.example.bt.services.Bluetooth.*;

public class SettingsActivity extends ActivityHelper {

    private View itemNotifEvents;
    private View itemNotifEventsEnable;
    private View itemAutoReconnect;
    private View itemReboot;
    private View itemDisconnect;
    private View itemStripInfo;
    private View homeConstraintLayout2;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Activity.Add(Activity.SETTINGS, this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitializeFields();

        InitializeUI();

        SetEventListeners();
    }

    private void SetEventListeners() {
        // event listeners
        homeConstraintLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        itemNotifEventsEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sw = v.findViewById(R.id.itemNotifEventsSwitch);
                sw.toggle();
                notificationEventsEnable(sw);
            }
        });
        itemNotifEventsEnable.findViewById(R.id.itemNotifEventsSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationEventsEnable((Switch) v);
            }
        });


        itemNotifEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openNotifEvents = new Intent(SettingsActivity.this, NotificationEventsActivity.class);
                startActivity(openNotifEvents);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });


        itemAutoReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sw = v.findViewById(R.id.itemAutoReconnectSwitch);
                sw.toggle();
                MemoryConnector.setBool(SettingsActivity.this, getString(R.string.var_auto_reconnect), sw.isChecked());
            }
        });
        itemAutoReconnect.findViewById(R.id.itemAutoReconnectSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemoryConnector.setBool(SettingsActivity.this, getString(R.string.var_auto_reconnect), ((Switch)v).isChecked());
            }
        });

        itemReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsConnectedDeviceNotNull()) {
                    DataTransfer.Reboot();
                    Toast.makeText(getApplicationContext(),
                            String.format(getString(R.string.toast_rebooting),
                                    ((ForegroundService)Service.Get(Service.FOREGROUND)).bt.GetConnectedDevice().getName()),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        itemDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IsConnectedDeviceNotNull()) {
                    itemDisconnect.setEnabled(false);
                    itemDisconnect.findViewById(R.id.itemDisconnectText).setEnabled(false);
                    itemDisconnect.findViewById(R.id.PB_disconnectDevice).setVisibility(View.VISIBLE);
                    ((ForegroundService) Service.Get(Service.FOREGROUND)).bt.Disconnect();
                }
            }
        });

        itemStripInfo.setOnClickListener(null);
    }

    private void goBack() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Activity.Remove(Activity.SETTINGS);

        if(timeUpdater != null) {
            if (!timeUpdater.isCancelled())
                timeUpdater.cancel();
            timeUpdater = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void InitializeUI() {
        // switches
        ((Switch)itemNotifEventsEnable.findViewById(R.id.itemNotifEventsSwitch))
                .setChecked(MemoryConnector.getBool(SettingsActivity.this, getString(R.string.var_notif_events)));
        ((Switch)itemAutoReconnect.findViewById(R.id.itemAutoReconnectSwitch))
                .setChecked(MemoryConnector.getBool(SettingsActivity.this, getString(R.string.var_auto_reconnect)));

        // disabled items
        itemReboot.setEnabled(IsConnectedDeviceNotNull());
        itemReboot.findViewById(R.id.itemRebootStripText).setEnabled(IsConnectedDeviceNotNull());
        itemReboot.findViewById(R.id.itemRebootStripIcon).setEnabled(IsConnectedDeviceNotNull());

        itemDisconnect.setEnabled(IsConnectedDeviceNotNull());
        itemDisconnect.findViewById(R.id.itemDisconnectText).setEnabled(IsConnectedDeviceNotNull());
        itemDisconnect.findViewById(R.id.PB_disconnectDevice).setVisibility(View.GONE);

        itemNotifEvents.findViewById(R.id.itemNotifEventsText)
                .setEnabled(MemoryConnector.getBool(SettingsActivity.this, getString(R.string.var_notif_events)));
        itemNotifEvents.findViewById(R.id.itemNotifEventsPaletteIcon)
                .setEnabled(MemoryConnector.getBool(SettingsActivity.this, getString(R.string.var_notif_events)));

        // text views
        BluetoothDevice conDev = ((ForegroundService)Service.Get(Service.FOREGROUND)).bt.GetConnectedDevice();
        if(conDev != null){
            ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoConnectedStripText))
                    .setText(String.format(getString(R.string.connected_to), conDev.getName()));
            ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoMacAddressText))
                    .setText(String.format(getString(R.string.mac_address), conDev.getAddress()));
            timeUpdater = new TimeUpdater();
            timeUpdater.execute();
        }else{
            ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoConnectedStripText)).setText("");
            ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoMacAddressText))
                    .setText(getString(R.string.no_strip_currently_connected));
            ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoTimeConnectedText)).setText("");
        }
    }


    private class TimeUpdater extends AsyncTask {
        private boolean execute = true;
        @Override
        protected Object doInBackground(Object[] objects) {
            while (execute){
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);

            ForegroundService service = (ForegroundService) Service.Get(Service.FOREGROUND);
            if(service != null){
                LocalDateTime timeConnected = service.bt.GetTimeConnected();
                LocalDateTime timeNow = LocalDateTime.now();

                String connectedString = "";
                if(timeConnected != null){
                    Duration duration = Duration.between(timeConnected, timeNow);
                    connectedString = String.format(getString(R.string.time_connected)
                            ,String.format("%02d:%02d:%02d",
                                duration.toHours(),
                                (duration.toMinutes() % 60),
                                ((duration.toMillis() / 1000) % 60)));
                }

                ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoTimeConnectedText))
                        .setText(connectedString);
            }
        }

        public void cancel() {
            execute = false;
        }
    };
    private TimeUpdater timeUpdater;


    private void InitializeFields() {
        itemNotifEvents = findViewById(R.id.itemNotifEvents);
        itemNotifEventsEnable = findViewById(R.id.itemNotifEventsEnable);
        itemAutoReconnect = findViewById(R.id.itemAutoReconnect);
        itemReboot = findViewById(R.id.itemReboot);
        itemDisconnect = findViewById(R.id.itemDisconnect);
        itemStripInfo = findViewById(R.id.itemStripInfo);
        homeConstraintLayout2 = findViewById(R.id.homeConstraintLayout2);
    }

    public void ActionCallback(Intent intent){
        switch (intent.getAction()){
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:{
                if(timeUpdater != null) {
                    if (!timeUpdater.isCancelled())
                        timeUpdater.cancel();
                    timeUpdater = null;
                }

                itemReboot.findViewById(R.id.itemRebootStripText).setEnabled(false);
                itemReboot.findViewById(R.id.itemRebootStripIcon).setEnabled(false);
                itemReboot.setEnabled(false);

                itemDisconnect.findViewById(R.id.PB_disconnectDevice).setVisibility(View.GONE);

                ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoConnectedStripText)).setText("");
                ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoMacAddressText))
                        .setText(getString(R.string.no_strip_currently_connected));
                ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoTimeConnectedText)).setText("");
            }
        }
    }

    private void notificationEventsEnable(Switch sw){
        if(!NotificationService.LISTENER_CONNECTED){
            new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                    .setTitle("Notification access")
                    .setMessage("Please enable notification access for Smart Lights to continue!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .show();
        }
        MemoryConnector.setBool(SettingsActivity.this, getString(R.string.var_notif_events), sw.isChecked());
        itemNotifEvents.setEnabled(sw.isChecked());
        itemNotifEvents.findViewById(R.id.itemNotifEventsText).setEnabled(sw.isChecked());
        itemNotifEvents.findViewById(R.id.itemNotifEventsPaletteIcon).setEnabled(sw.isChecked());
    }
}
