package com.example.walktoshop.Model;

import java.util.ArrayList;

public class User {
    private String UID;
    private String email;
    private String password;
    private String height;//cm
    private String weight;//kg
    private String lastWalkDate;
    private ArrayList<String> walk;
    private ArrayList<String> discountUID;
    public User(){}
    //setters

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setDiscountUID(ArrayList<String> discountUID) { this.discountUID = discountUID;
    }


    //getters
    public String getUID() {
        return UID;
    }


    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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

    public ArrayList<String> getDiscountUID() { return discountUID; }
}

