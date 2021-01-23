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
    private String lastWalkDate;
    private ArrayList<String> walk;
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

    public void setLastWalkDate(String lastWalkDate) {
        this.lastWalkDate = lastWalkDate;
    }

    public void setWalk(ArrayList walk) {
        this.walk = walk;
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

    public String getLastWalkDate() {
        return lastWalkDate;
    }

    public ArrayList getWalk() {
        return walk;
    }

    public ArrayList<String> getDisocuntUID() {
        return disocuntUID;
    }
}

