package com.example.bt.dialogs;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.bt.R;
import com.example.bt.list_adapters.DiscoverDevicesListAdapter;
import com.example.bt.list_adapters.DiscoverDevicesRowItem;
import com.example.bt.services.Bluetooth;

import java.util.ArrayList;

import static com.example.bt.Animator.*;
import static com.example.bt.SharedServices.*;

public class DiscoverDevicesDialog extends DialogFragment {

    private ArrayList<BluetoothDevice> availableDeviceList;

    private ListView discoverDevicesListView;
    private Button scanDevicesButton;
    private Button scanDoneButton;
    private ProgressBar PB_discoverDevices;
    private View noDevicesLayout;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.discover_devices, null);
        builder.setView(view);

        setupFields(view);
        setupEventListeners();

        discoverDevices();

        return builder.create();
    }

    private void setupFields(View parent) {
        availableDeviceList = new ArrayList<BluetoothDevice>();

        discoverDevicesListView = parent.findViewById(R.id.discoverDevicesListView);
        scanDevicesButton = parent.findViewById(R.id.scanButton);
        scanDoneButton = parent.findViewById(R.id.homeConstraintLayout);
        PB_discoverDevices = parent.findViewById(R.id.PB_discoverDevices);
        noDevicesLayout = parent.findViewById(R.id.noDevicesLayout);
    }

    private void setupEventListeners() {
        discoverDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // if already connected
                if(!availableDeviceList.get((int)id).equals(aMain.service.bt.GetConnectedDevice()))
                    aMain.service.bt.Connect(availableDeviceList.get(position).getAddress());
                else
                    aMain.service.bt.Disconnect();

                // add progress bar to list
                appear(view.findViewById(R.id.PB_connectDevice));
            }
        });

        scanDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverDevices();
            }
        });

        scanDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bluetooth.CancelDiscovery();
                dismiss();
            }
        });
    }

    private void discoverDevices(){
        DiscoverDevicesListAdapter discoverDevicesListAdapter =
                new DiscoverDevicesListAdapter(getContext(), new ArrayList<DiscoverDevicesRowItem>());
        discoverDevicesListView.setAdapter(discoverDevicesListAdapter);

        if(availableDeviceList.size() != 0)
            availableDeviceList.clear();
        Bluetooth.StartDiscovery();
    }

    public void postDeviceConnect(BluetoothDevice connectedDevice){
        /*
        ((TextView)discoverDevicesListView.getChildAt(availableDeviceList.indexOf(connectedDevice)).findViewById(R.id.dd_name))
                .setTextColor(Color.parseColor("#058a00"));
        ((ProgressBar)discoverDevicesListView.getChildAt(availableDeviceList.indexOf(connectedDevice)).findViewById(R.id.PB_connectDevice))
                .setVisibility(View.INVISIBLE);
        dismiss();
        */
    }

    public void postDeviceFound(BluetoothDevice device) {
        if(!availableDeviceList.contains(device)){
            vanish(noDevicesLayout);
            availableDeviceList.add(device);

            // create row item list to display
            ArrayList<DiscoverDevicesRowItem> discoverDevicesRowItems = new ArrayList<DiscoverDevicesRowItem>();
            for(BluetoothDevice d : availableDeviceList){
                DiscoverDevicesRowItem ri = new DiscoverDevicesRowItem();
                ri.setName(d.getName());
                ri.setMacAddress(d.getAddress());
                discoverDevicesRowItems.add(ri);
            }

            DiscoverDevicesListAdapter discoverDevicesListAdapter = new DiscoverDevicesListAdapter(getContext(), discoverDevicesRowItems);
            discoverDevicesListView.setAdapter(discoverDevicesListAdapter);
        }
    }

    public void postDiscoveryFinished(){
        vanish(PB_discoverDevices);
        scanDevicesButton.setEnabled(true);
        scanDevicesButton.setEnabled(true);
    }

    public void postDiscoveryStarted() {
        appear(PB_discoverDevices);
        scanDevicesButton.setEnabled(false);
    }
}
