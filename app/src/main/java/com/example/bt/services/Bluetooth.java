package com.example.bt.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.bt.MemoryConnector;
import com.example.bt.R;
import com.example.bt.SharedServices;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

import static com.example.bt.SharedServices.*;
import static com.example.bt.SharedServices.Activity.*;

public class Bluetooth
{
    private static String UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB";

    private Context context;

    private BluetoothSocket socket;
    private BluetoothDevice connectedDevice;
    private LocalDateTime timeConnected;
    private static BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    private String autoReconnectMacAddressPendingSave;

    public Bluetooth(Context context){
        Log.d("APP", "creating bt instance");
        this.context = context;
        Create();
    }

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver(){

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            ActionCallback(intent);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void ActionCallback(Intent intent) {
        PassCallback(intent);

        Log.d("APP", "intent action: " + intent.getAction());
        if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
            connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            timeConnected = LocalDateTime.now();

            // if auto reconnect mac address ir pending for save
            if(autoReconnectMacAddressPendingSave != null){
                MemoryConnector.setString(context, context.getString(R.string.var_auto_reconnect_mac), connectedDevice.getAddress());
                MemoryConnector.setString(context, context.getString(R.string.var_auto_reconnect_name), connectedDevice.getName());
                autoReconnectMacAddressPendingSave = null;
            }
        }
        else if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            if(connectedDevice != null){
                if(connectedDevice.equals(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))) {
                    Disconnect();
                    connectedDevice = null;
                    timeConnected = null;
                }
            }
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(context.getString(R.string.action_could_not_connect));

        Log.d("APP", "registering receiver");
        context.registerReceiver(bluetoothReceiver, filter);
    }


    public static boolean IsBluetoothOn(){
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public BluetoothDevice GetConnectedDevice(){
        return connectedDevice;
    }
    public LocalDateTime GetTimeConnected(){
        return timeConnected;
    }

    public void Create() {
        registerReceiver();
        PassCallback(new Intent(context.getString(R.string.action_bluetooth_communication_start)));
    }
/*
    public void Destroy() {
        CancelDiscovery();
        Disconnect();

        if(aMain != null)
            aMain.actionCallback(new Intent(Resources.getSystem().getString(R.string.action_bluetooth_communication_end)));

        context.unregisterReceiver(bluetoothReceiver);
    }
*/

    public void Connect(String address){
        ConnectThread connectThread = new ConnectThread(address);
        connectThread.start();
    }
    public void Disconnect(){
        try {
            if(socket != null)
                socket.close();
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void Reconnect(String address){
        this.ConnectPaired(address);
    }

    public static void StartDiscovery(){
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }
    public static void CancelDiscovery(){
        if(BluetoothAdapter.getDefaultAdapter().isDiscovering())
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    }

    public void Send(byte[] data){
        SendThread sendThread = new SendThread(data);
        sendThread.start();
    }


    private void ConnectPaired(String address){
        Set<BluetoothDevice> bluetoothDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : bluetoothDevices){
            if(device.getAddress().equals(address)){
                Log.d("APP", "reconnecting (starting 'connect' thread)");
                ConnectThread connectThread = new ConnectThread(address);
                connectThread.start();
            }
        }
    }

    public void SetAutoReconnectMacAddressPendingSave(String autoReconnectMacAddressPendingSave) {
        this.autoReconnectMacAddressPendingSave = autoReconnectMacAddressPendingSave;
    }

    private class ConnectThread extends Thread{
        private String address;

        public ConnectThread(String address){
            this.address = address;
        }

        @Override
        public void run() {
            super.run();
            CancelDiscovery();
            BluetoothDevice device = adapter.getRemoteDevice(address);
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(UUID_STRING));
                try {
                    socket.connect();Log.d("APP", "socket connected");
                } catch (Exception e) {
                    socket.close(); Log.d("APP", "socket closed: " + e.getMessage());
                    context.sendBroadcast(new Intent(context.getString(R.string.action_could_not_connect)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private class SendThread extends Thread{
        private byte[] data;

        public SendThread(byte[] data){
            this.data = data;
        }

        @Override
        public void run() {
            super.run();
            try {
                if(socket != null){
                    OutputStream outputStream = socket.getOutputStream();
                    try {
                        outputStream.write(data);
                    } catch (IOException e) {
                        outputStream.close();
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean IsConnectedDeviceNotNull() {
        ForegroundService service = (ForegroundService) Service.Get(Service.FOREGROUND);
        if(service != null)
            if(service.bt != null)
                if(service.bt.GetConnectedDevice() != null)
                    return true;
        return false;
    }
}
