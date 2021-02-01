package com.example.walktoshop.Model;

import java.util.ArrayList;

public class Seller {
    private String UID;//identificatore venditore
    private String email;
    private String password;
    private ArrayList<String> businessUID;//array di id di attività in possesso del venditore (è un array in ottica di previsione al cambiamento in caso di più attività)

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

    public void setBusinessUID(ArrayList<String> businessUID) {
        this.businessUID = businessUID;
    }
}
