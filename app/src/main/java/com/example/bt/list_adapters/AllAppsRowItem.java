package com.example.bt.list_adapters;

import android.graphics.drawable.Drawable;

public class AllAppsRowItem {
    private  String name;
    private Drawable icon;

    public void setName(String name){
        this.name = name;
    }

    public void setIcon(Drawable icon){
        this.icon = icon;
    }

    public String getName(){
        return this.name;
    }

    public Drawable getIcon(){
        return this.icon;
    }
}
