package com.example.bt.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.bt.MemoryConnector;
import com.example.bt.R;
import com.example.bt.dialogs.DiscoverDevicesDialog;
import com.example.bt.services.Bluetooth;
import com.example.bt.services.ForegroundService;

import static com.example.bt.Animator.*;
import static com.example.bt.SharedServices.*;
import static com.example.bt.SharedServices.aMain;

public class MainActivity extends AppCompatActivity {
    private static final int TURN_BT_ON_REQUEST = 1;
    private static final int BT_PERMISSION_REQUEST = 2;
    private static final int AUDIO_PERMISSION_REQUEST = 3;

    private Button findLedStripButton;

    //private PopupWindow connectPopUpWindow;

    //private LayoutInflater inflater;

    private View settingsConstraintLayout;
    private View colorPickerConstraintLayout;
    private View musicConstraintLayout;
    private View connectedLinearLayout;

    DiscoverDevicesDialog discoverDevicesDialog;
    public ForegroundService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        InitUI();
        aMain = this; // add this activity to static var, so it can be accessed from everywhere
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MemoryConnector.setBool(this, getString(R.string.var_clean_open), true);
    }
    @Override
    protected void onResume() {
        super.onResume();
        aMain = this; // add this activity to static var, so it can be accessed from everywhere
        // bind service
        ConnectService();
    }
    @Override
    public void onPause(){
        super.onPause();
        Bluetooth.CancelDiscovery();
        if(IsServiceRunning())
            unbindService(connection);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        aMain = null; // null out activity
        service.bt.Disconnect();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case BT_PERMISSION_REQUEST: {
                if(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    findLedStrip();
                break;
            }
            case AUDIO_PERMISSION_REQUEST: {
                if(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    switchToMusicSyncActivity();
                break;
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case TURN_BT_ON_REQUEST:{
                // if bluetooth turned on - go discover
                if(Bluetooth.IsBluetoothOn())
                    findLedStrip();
                break;
            }
        }
    }

    private void findLedStrip(){
        // if permission is not granted - ask
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Permission.RequestPermission(Permission.PERMISSION_BLUETOOTH,
                    getApplicationContext(), MainActivity.this, BT_PERMISSION_REQUEST);
        }
        // if permission granted
        else {
            // if bluetooth is not on - ask to turn on
            if(!Bluetooth.IsBluetoothOn()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, TURN_BT_ON_REQUEST);
            }
            // if all ok - start discovery
            else{
                // open dialog
                FragmentManager fm = getSupportFragmentManager();
                discoverDevicesDialog.show(fm, "fragment_discover_devices");
            }
        }
    }

    private void switchToMusicSyncActivity(){
        if(ContextCompat.checkSelfPermission(this.getApplicationContext()
                , Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_REQUEST);
        }else{
            Intent intent = new Intent(MainActivity.this, MusicActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
        }
    }



    public boolean IsConnectedDeviceNotNull() {
        if(service != null)
            if(service.bt != null)
                if(service.bt.GetConnectedDevice() != null)
                    return true;
        return false;
    }
    public boolean IsBtCommNotNull() {
        if(service != null)
            if(service.bt != null)
                return true;
        return false;
    }
    public boolean IsFgServiceNotNull() {
        if(service != null)
            return true;
        return false;
    }
    public boolean IsServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void InitializeFields(){
        discoverDevicesDialog = new DiscoverDevicesDialog();

        findLedStripButton = findViewById(R.id.findLedStripButton);
        settingsConstraintLayout = findViewById(R.id.settingsConstraintLayout);
        colorPickerConstraintLayout = findViewById(R.id.colorPickerConstraintLayout);
        musicConstraintLayout = findViewById(R.id.musicConstraintLayout);
        connectedLinearLayout = findViewById(R.id.connectedLinearLayout);
    }


    private void StartEntryAnimation(){
        if(MemoryConnector.getBool(this, getString(R.string.var_clean_open))){
            // auto reconnect
            boolean autoRec =
                    MemoryConnector.getBool(MainActivity.this, getString(R.string.var_auto_reconnect))
                    && MemoryConnector.getString(MainActivity.this, getString(R.string.var_auto_reconnect_mac)) != null
                    && !IsConnectedDeviceNotNull();


            // set initial view properties
            vanish(colorPickerConstraintLayout);
            vanish(musicConstraintLayout);
            vanish(connectedLinearLayout);

            // set up initial animation values
            View logoLayout = findViewById(R.id.logoLayout);
            View findLedStripButton = findViewById(R.id.findLedStripButton);
            View motoLayout = findViewById(R.id.motoLayout);
            View settingsCl = findViewById(R.id.settingsConstraintLayout);
            View autoReconnectL = findViewById(R.id.autoReconnectLayout);

            vanish(autoReconnectL);
            // if auto reconnect enabled
            if(autoRec){
                ((TextView)autoReconnectL.findViewById(R.id.autoReconnectLayoutText))
                        .setText(String.format(getString(R.string.connecting_to),
                                MemoryConnector.getString(this, getString(R.string.var_auto_reconnect_name))));
                // set progress bar and text (connecting to ...name...)
                fadeIn(autoReconnectL, DURATION_NORMAL);
            }

            vanish(logoLayout);
            vanish(findLedStripButton);
            vanish(motoLayout);
            vanish(settingsCl);

            if(!autoRec)
                fadeIn(findLedStripButton, DURATION_NORMAL);

            fadeIn(logoLayout, DURATION_NORMAL);
            fadeIn(motoLayout, DURATION_NORMAL);
            fadeIn(settingsCl, DURATION_NORMAL);

            Drawable arrowDoubleLeftDrawable =  ((ImageView)findViewById(R.id.arrowDoubleLeftImageView)).getDrawable();
            Drawable arrowDoubleRightDrawable =  ((ImageView)findViewById(R.id.arrowDoubleRightImageView)).getDrawable();
            ((AnimatedVectorDrawable)arrowDoubleLeftDrawable).start();
            ((AnimatedVectorDrawable)arrowDoubleRightDrawable).start();
        }
    }

    private void InitUI(){
        if(MemoryConnector.getBool(this, getString(R.string.var_clean_open))){

        }
    }


    private void ConnectService(){
        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
        // check if service is running
        if(IsServiceRunning()){
            bindService(intent, connection, Context.BIND_AUTO_CREATE); // bind
        }else{
            // start service
            startService(intent); // start
            bindService(intent, connection, Context.BIND_AUTO_CREATE); // bind
        }
    }

    private void SetEventListeners(){
        settingsConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change activity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        colorPickerConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IsConnectedDeviceNotNull()){
                    // change activity
                    Intent intent = new Intent(MainActivity.this, ColorPickActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
        musicConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToMusicSyncActivity();
            }
        });


        findLedStripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findLedStrip();
            }
        });
    }

    public void postDeviceConnect(BluetoothDevice connectedDevice){
        Toast.makeText(getApplicationContext(), String.format(getString(R.string.toast_strip_connected),
                connectedDevice.getName()), Toast.LENGTH_SHORT).show();

        fadeIn(colorPickerConstraintLayout, DURATION_SHORT);
        fadeIn(musicConstraintLayout, DURATION_SHORT);
        fadeIn(connectedLinearLayout, DURATION_SHORT);

        // if this is true - means that user connected manually
        if(discoverDevicesDialog != null
                && discoverDevicesDialog.getDialog() != null
                && discoverDevicesDialog.getDialog().isShowing()) {

            //discoverDevicesDialog.postDeviceConnect(connectedDevice);
            discoverDevicesDialog.dismiss();

            // change activity
            //Intent intent = new Intent(MainActivity.this, ColorPickActivity.class);
            //startActivity(intent);
            //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        // if this is true - means that auto reconnect just took place
        else{
            vanish(findViewById(R.id.autoReconnectLayout));
            fadeIn(findLedStripButton, DURATION_SHORT);
        }

    }

    public void actionCallback(Intent intent){
        switch (intent.getAction()){
            case BluetoothDevice.ACTION_FOUND : {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(discoverDevicesDialog != null
                        && discoverDevicesDialog.getDialog() != null
                        && discoverDevicesDialog.getDialog().isShowing())
                    discoverDevicesDialog.postDeviceFound(device);
                break;
            }
            case BluetoothDevice.ACTION_ACL_CONNECTED : {
                BluetoothDevice connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                postDeviceConnect(connectedDevice);
                break;
            }
            case BluetoothDevice.ACTION_ACL_DISCONNECTED : {
                BluetoothDevice disconnectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(IsFgServiceNotNull())
                    if(disconnectedDevice.equals(service.bt.GetConnectedDevice())) {
                        service.bt.SetConnectedDevice(null);
                        fadeOut(colorPickerConstraintLayout, DURATION_SHORT);
                        fadeOut(musicConstraintLayout, DURATION_SHORT);
                        fadeOut(connectedLinearLayout, DURATION_SHORT);
                        Toast.makeText(getApplicationContext(),
                                String.format(getString(R.string.toast_strip_disconnected), disconnectedDevice.getName()),
                                Toast.LENGTH_SHORT).show();
                    }
                break;
            }
            case "action_could_not_connect":{
                // if dialog is open
                if(discoverDevicesDialog != null
                        && discoverDevicesDialog.getDialog() != null
                        && discoverDevicesDialog.getDialog().isShowing())
                    discoverDevicesDialog.actionCallback(intent);
                // if auto reconnect is going on
                else{
                    vanish(findViewById(R.id.autoReconnectLayout));
                    fadeIn(findLedStripButton, DURATION_SHORT);
                    Toast.makeText(getApplicationContext(),
                            String.format(getString(R.string.toast_could_not_auto_reconnect),
                                    MemoryConnector.getString(this, getString(R.string.var_auto_reconnect_name))), Toast.LENGTH_LONG).show();
                }
                break;
            }
            case "action_bluetooth_communication_stop" : {
                Toast.makeText(getApplicationContext(), "Bluetooth service stopped", Toast.LENGTH_LONG).show();
                break;
            }
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED : {
                if(discoverDevicesDialog != null
                        && discoverDevicesDialog.getDialog() != null
                        && discoverDevicesDialog.getDialog().isShowing())
                    discoverDevicesDialog.postDiscoveryStarted();
                break;
            }
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED : {
                if(discoverDevicesDialog != null
                        && discoverDevicesDialog.getDialog() != null
                        && discoverDevicesDialog.getDialog().isShowing())
                    discoverDevicesDialog.postDiscoveryFinished();
                break;
            }
        }
    }


    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ForegroundService.ForegroundServiceBinder binder = (ForegroundService.ForegroundServiceBinder) service;
            MainActivity.this.service = binder.getService();
            OnServiceReady();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) { }
    };

    private void OnServiceReady() {
        actionCallback(new Intent(getString(R.string.action_foreground_service_bind)));

        // initialize fields
        InitializeFields();

        // set event listeners
        SetEventListeners();

        // perform entry animation
        StartEntryAnimation();

        // auto reconnect to strip if needed
        if(MemoryConnector.getBool(this, getString(R.string.var_clean_open))) {
            String autoReconnectMac = MemoryConnector.getString(getApplicationContext(), getApplicationContext().getString(R.string.var_auto_reconnect_mac));
            if(autoReconnectMac != null &&  !IsConnectedDeviceNotNull()
            && MemoryConnector.getBool(getApplicationContext(), getApplicationContext().getString(R.string.var_auto_reconnect))){
                service.bt.Reconnect(autoReconnectMac);
            }
            MemoryConnector.setBool(this, getString(R.string.var_clean_open), false);
        }
    }
}
