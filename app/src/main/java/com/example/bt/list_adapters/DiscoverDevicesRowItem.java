package com.example.bt.list_adapters;

public class DiscoverDevicesRowItem {
    private  String text;
    private  String macAddress;

    public void setName(String text){
        this.text = text;
    }

    public void setMacAddress(String macAddress){
        this.macAddress = macAddress;
    }

    public String getName(){
        return this.text;
    }

    public String getMacAddress(){
        return this.macAddress;
    }
}
