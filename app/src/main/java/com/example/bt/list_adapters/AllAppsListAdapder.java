package com.example.bt.list_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bt.R;
import com.example.bt.views.ColorField;

import java.util.ArrayList;

public class AllAppsListAdapder extends ArrayAdapter<AllAppsRowItem> {

    private ArrayList<AllAppsRowItem> singleRow;
    private LayoutInflater thisInflater;

    public AllAppsListAdapder(Context context, ArrayList<AllAppsRowItem> aRow) {
        super(context, 0, aRow);
        this.singleRow = aRow;
        thisInflater = ( LayoutInflater.from(context) );
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get row item
        AllAppsRowItem currentRow = getItem(position);
        // if existing view is not being reused - inflate
        if(convertView == null){
            convertView = thisInflater.inflate(R.layout.all_apps_row, parent, false);
        }
        // get views
        ImageView iconImage = (ImageView) convertView.findViewById(R.id.aa_icon);
        TextView nameText = (TextView)convertView.findViewById(R.id.aa_name);
        ColorField colorField = (ColorField)convertView.findViewById(R.id.aa_color_field);
        // set data
        nameText.setText(currentRow.getName());
        iconImage.setImageDrawable(currentRow.getIcon());
        colorField.SetColor(currentRow.getColor());

        return convertView;
    }
}
