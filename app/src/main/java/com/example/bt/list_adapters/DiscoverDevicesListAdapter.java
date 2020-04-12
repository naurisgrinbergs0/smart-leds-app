package com.example.bt.list_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bt.R;

import java.util.ArrayList;

public class DiscoverDevicesListAdapter extends BaseAdapter {

    private ArrayList<DiscoverDevicesRowItem> singleRow;
    private LayoutInflater thisInflater;

    public DiscoverDevicesListAdapter(Context context, ArrayList<DiscoverDevicesRowItem> aRow) {

        this.singleRow = aRow;
        thisInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return singleRow.size();
    }

    @Override
    public Object getItem(int position) {
        return singleRow.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = thisInflater.inflate(R.layout.row_discover_devices, parent, false);
            TextView nameText = (TextView)convertView.findViewById(R.id.dd_name);

            DiscoverDevicesRowItem currentRow = (DiscoverDevicesRowItem)getItem(position);
            nameText.setText(currentRow.getName());
        }

        return convertView;
    }
}
