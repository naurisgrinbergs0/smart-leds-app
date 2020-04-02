package com.example.bt.list_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bt.R;

import java.util.ArrayList;

public class AllAppsListAdapder extends BaseAdapter {

    private ArrayList<AllAppsRowItem> singleRow;
    private LayoutInflater thisInflater;

    public AllAppsListAdapder(Context context, ArrayList<AllAppsRowItem> aRow) {

        this.singleRow = aRow;
        thisInflater = ( LayoutInflater.from(context) );
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
            convertView = thisInflater.inflate(R.layout.all_apps_row, parent, false);
            ImageView iconImage = (ImageView) convertView.findViewById(R.id.aa_icon);
            TextView nameText = (TextView)convertView.findViewById(R.id.aa_name);

            AllAppsRowItem currentRow = (AllAppsRowItem) getItem(position);
            nameText.setText(currentRow.getName());
            iconImage.setImageDrawable(currentRow.getIcon());
        }

        return convertView;
    }
}
