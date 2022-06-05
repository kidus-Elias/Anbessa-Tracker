package com.example.bustrackingsystem;

import androidx.annotation.NonNull;

public class Driver {
    String busNum;
    String Transit;
    String pNum;
    Double longitude,latitude;
    Boolean shareLocation;
    Float speed;

    public void setShareLocation(Boolean shareLocation) {
        this.shareLocation = shareLocation;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setBusNum(String busNum) {
        this.busNum = busNum;
    }

    public void setpNum(String pNum) {
        this.pNum = pNum;
    }

    public void setTransit(String transit) {
        Transit = transit;
    }

    public String getBusNum() {
        return busNum;
    }

    public String getTransit() {
        return Transit;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Boolean getShareLocation() {
        return shareLocation;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getpNum() {
        return pNum;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Float getSpeed() {
        return speed;
    }

    public Driver(String p, String busNum, String Transit, double lat, double lng, Boolean sl,Float speed)
    {   this.shareLocation=sl;
        this.latitude=lat;
        this.longitude=lng;
        this.pNum=p;
       this.busNum=busNum;
       this.Transit=Transit;
       this.speed=speed;
    }
    public  Driver()
    {

    }
    @NonNull
    @Override
    public String toString(){
        return "Bus number:" +this.getBusNum() +"\n"+"Transit: "+ this.Transit;
    }
}
