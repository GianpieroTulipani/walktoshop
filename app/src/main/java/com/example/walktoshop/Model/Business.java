package com.example.walktoshop.Model;

import android.util.Log;

import java.util.ArrayList;
/*
    Classe che indica l'attività di cui il venditore è in possesso che registrerà sulla mappa e verrà scritta sul database
 */
public class Business{
    private String UID=null;//id utile per le query al db
    private String ownerUID=null;//id del proprietario
    private String name=null;//nome dell'attività
    private String latitude=null;
    private String longitude=null;
    private String locality=null;//città dell'attività
    private ArrayList discountUID=null;//array di id degli sconti erogati dall'attività

    public Business(){}

    //getter
    public String getOwnerUID() {
        return ownerUID;
    }

    public String getUID() {
        return UID;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLocality() {
        return locality;
    }

    public ArrayList getDiscountUID() {
        return discountUID;
    }

    public String getName() {
        return name;
    }

    //setter
    public void setOwnerUID(String ownerUID) {
        this.ownerUID = ownerUID;
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

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
