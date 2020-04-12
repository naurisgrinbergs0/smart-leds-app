package com.example.bt.list_adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class AllAppsRowItem {
    private String name;
    private Drawable icon;
    private int color;
    private String packageName;

    public void setName(String name){
        this.name = name;
    }

    public void setIcon(Drawable icon){
        this.icon = icon;
    }

    public void setColor(int color){
        this.color = color;
    }

    public void setPackageName(String packageName){
        this.packageName = packageName;
    }

    public String getName(){
        return this.name;
    }

    public Drawable getIcon(){
        return this.icon;
    }

    public int getColor(){
        return this.color;
    }

    public String getPackageName(){
        return this.packageName;
    }
}
