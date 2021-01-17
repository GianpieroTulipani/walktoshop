package com.example.walktoshop.Seller;

import java.util.ArrayList;

public class Business{
    private String UID;
    private String name;
    private String latitude;
    private String longitude;
    private ArrayList discountUID;

    public Business(){}
    //getter
    public String getUID() {
        return UID;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public ArrayList getDiscountUID() {
        return discountUID;
    }

    public String getName() {
        return name;
    }
    //setter

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setDiscountUID(ArrayList discountUID) {
        this.discountUID = discountUID;
    }

    public void setName(String name) {
        this.name = name;
    }
}
