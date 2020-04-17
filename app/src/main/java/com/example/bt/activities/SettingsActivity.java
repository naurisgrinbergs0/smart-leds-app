package com.example.bt.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bt.MemoryConnector;
import com.example.bt.R;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.bt.SharedServices.*;

public class SettingsActivity extends AppCompatActivity {

    private View itemNotifEvents;
    private View itemNotifEventsEnable;
    private View itemAutoReconnect;
    private View itemReboot;
    private View itemDisconnect;
    private View itemStripInfo;
    private View homeConstraintLayout2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        aSettings = this;

        InitializeFields();

        InitializeUI();

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
                MemoryConnector.setBool(SettingsActivity.this, getString(R.string.var_notif_events), sw.isChecked());

                Intent i = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(i);

                itemNotifEvents.setEnabled(sw.isChecked());
                itemNotifEvents.findViewById(R.id.itemNotifEventsText).setEnabled(sw.isChecked());
                itemNotifEvents.findViewById(R.id.itemNotifEventsPaletteIcon).setEnabled(sw.isChecked());
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

        itemReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aMain.IsConnectedDeviceNotNull()) {
                    DataTransfer.Reboot();
                    Toast.makeText(getApplicationContext(),
                            String.format(getString(R.string.toast_rebooting),
                                    aMain.service.bt.GetConnectedDevice().getName()),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        itemDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aMain.IsConnectedDeviceNotNull())
                    aMain.service.bt.Disconnect();
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
        aSettings = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    private void InitializeUI() {
        // switches
        ((Switch)itemNotifEventsEnable.findViewById(R.id.itemNotifEventsSwitch))
                .setChecked(MemoryConnector.getBool(SettingsActivity.this, getString(R.string.var_notif_events)));
        ((Switch)itemAutoReconnect.findViewById(R.id.itemAutoReconnectSwitch))
                .setChecked(MemoryConnector.getBool(SettingsActivity.this, getString(R.string.var_auto_reconnect)));

        // disabled items
        itemReboot.setEnabled(aMain.IsConnectedDeviceNotNull());
        itemReboot.findViewById(R.id.itemRebootStripText).setEnabled(aMain.IsConnectedDeviceNotNull());
        itemReboot.findViewById(R.id.itemRebootStripIcon).setEnabled(aMain.IsConnectedDeviceNotNull());

        itemDisconnect.setEnabled(aMain.IsConnectedDeviceNotNull());
        itemDisconnect.findViewById(R.id.itemDisconnectText).setEnabled(aMain.IsConnectedDeviceNotNull());

        itemNotifEvents.findViewById(R.id.itemNotifEventsText)
                .setEnabled(MemoryConnector.getBool(SettingsActivity.this, getString(R.string.var_notif_events)));
        itemNotifEvents.findViewById(R.id.itemNotifEventsPaletteIcon)
                .setEnabled(MemoryConnector.getBool(SettingsActivity.this, getString(R.string.var_notif_events)));

        // text views
        BluetoothDevice conDev = aMain.service.bt.GetConnectedDevice();
        if(conDev != null){
            ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoConnectedStripText))
                    .setText(String.format(getString(R.string.connected_to), conDev.getName()));
            ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoMacAddressText))
                    .setText(String.format(getString(R.string.mac_address), conDev.getAddress()));
            //((TextView)itemStripInfo.findViewById(R.id.itemStripInfoTimeConnectedText)).setText(aMain.service.bt.GetTimeConnected());
        }else{
            ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoConnectedStripText)).setText("");
            ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoMacAddressText))
                    .setText(getString(R.string.no_strip_currently_connected));
            ((TextView)itemStripInfo.findViewById(R.id.itemStripInfoTimeConnectedText)).setText("");
        }
    }

    private void InitializeFields() {
        itemNotifEvents = findViewById(R.id.itemNotifEvents);
        itemNotifEventsEnable = findViewById(R.id.itemNotifEventsEnable);
        itemAutoReconnect = findViewById(R.id.itemAutoReconnect);
        itemReboot = findViewById(R.id.itemReboot);
        itemDisconnect = findViewById(R.id.itemDisconnect);
        itemStripInfo = findViewById(R.id.itemStripInfo);
        homeConstraintLayout2 = findViewById(R.id.homeConstraintLayout2);
    }

    public void actionCallback(Intent intent){
        switch (intent.getAction()){
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:{
                itemReboot.findViewById(R.id.itemRebootStripText).setEnabled(false);
                itemReboot.findViewById(R.id.itemRebootStripIcon).setEnabled(false);
                itemReboot.setEnabled(false);

                itemDisconnect.setEnabled(false);
                itemDisconnect.findViewById(R.id.itemDisconnectText).setEnabled(false);
            }
        }
    }
}
