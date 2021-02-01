package com.example.walktoshop.Model;

import java.util.ArrayList;

public class Seller {
    private String UID;
    private String email;
    private String password;
    private ArrayList<String> businessUID;
    public Seller(){};
    //getters
    public String getUID() {
        return UID;
    }


    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<String> getBusinessUID() {
        return businessUID;
    }

    //setterss
    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBusinessUID(ArrayList<String> businessUID) {
        this.businessUID = businessUID;
    }
}
