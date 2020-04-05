package com.example.bt.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.bt.MemoryConnector;
import com.example.bt.R;
import com.example.bt.activities.MainActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static com.example.bt.SharedServices.*;

public class Bluetooth
{
    private static String UUID_STRING = "00001101-0000-1000-8000-00805f9b34fb";

    private Context context;

    private BluetoothSocket socket;
    private BluetoothDevice connectedDevice;
    private static BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    public Bluetooth(Context context){
        this.context = context;
        Create();
    }

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if(aMain != null)
                aMain.actionCallback(intent);
            if(aSettings != null)
                aSettings.actionCallback(intent);
Log.d("APP", intent.getAction());
            if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)){Log.d("APP", "Connected Bluetooth!");
                CancelDiscovery();
                connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                MemoryConnector.setString(context, context.getString(R.string.var_auto_reconnect_mac), connectedDevice.getAddress());
                MemoryConnector.setString(context, context.getString(R.string.var_auto_reconnect_name), connectedDevice.getName());
            }
            else if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
                if(connectedDevice == intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)) {
                    CancelDiscovery();
                    Disconnect();
                    connectedDevice = null;
                }
            }
        }
    };
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        context.registerReceiver(bluetoothReceiver, filter);
    }


    public static boolean IsBluetoothOn(){
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public BluetoothDevice GetConnectedDevice(){
        return connectedDevice;
    }
    public void SetConnectedDevice(BluetoothDevice device){
        connectedDevice = device;
    }

    public void Create() {
        registerReceiver();
        if(aMain != null)
            aMain.actionCallback(new Intent(context.getString(R.string.action_bluetooth_communication_start)));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void Reconnect(){
        String autoReconnectMac = MemoryConnector.getString(context, context.getString(R.string.var_auto_reconnect_mac));
        if(autoReconnectMac != null && connectedDevice == null
                && MemoryConnector.getBool(context, context.getString(R.string.var_auto_reconnect))){
            this.ConnectPaired(autoReconnectMac);
        }
    }

    public static void StartDiscovery(){
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }
    public static void CancelDiscovery(){
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
                ConnectThread connectThread = new ConnectThread(address);
                connectThread.start();
            }
        }
    }

    private class ConnectThread extends Thread{
        private String address;

        public ConnectThread(String address){
            this.address = address;
        }

        @Override
        public void run() {
            super.run();
            BluetoothDevice device = adapter.getRemoteDevice(address);
            adapter.cancelDiscovery();
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(UUID_STRING));
                try {
                    socket.connect();
                } catch (IOException e) {
                    socket.close();
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
}
