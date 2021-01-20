package com.example.walktoshop.User;

import java.util.ArrayList;

public class User {
    private String UID;
    private String username;
    private String email;
    private String password;
    private String latitude;
    private String longitude;
    private String height;//cm
    private String weight;//kg
    private int falcata;
    private ArrayList<String> walkUID;
    private ArrayList<String> disocuntUID;
    /*
   o	UID
o	Email
o	Password
o	Latitude
o	Longitude
o	Altezza
o	Peso
o	Falcata
o	CamminataUID[]
o	ScontiUID[]

    */
    public User(){}
    //setters

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
    public void setFalcata(int falcata) {
        this.falcata = falcata;
    }

    public void setWalkUID(ArrayList<String> walkUID) {
        this.walkUID = walkUID;
    }

    public void setDisocuntUID(ArrayList<String> disocuntUID) {
        this.disocuntUID = disocuntUID;
    }

    //getters
    public String getUID() {
        return UID;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getHeight() {
        return height;
    }

    public String getWeight() {
        return weight;
    }

    public int getFalcata() {
        return falcata;
    }

    public ArrayList<String> getWalkUID() {
        return walkUID;
    }

    public ArrayList<String> getDisocuntUID() {
        return disocuntUID;
    }
}

