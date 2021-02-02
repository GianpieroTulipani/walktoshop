package com.example.walktoshop.Model;

import java.util.ArrayList;

public class User {
    private String UID;//identificatore utente sul db
    private String email;
    private ArrayList<String> discountSteps;
    private String password;
    private String height;//altezza utente utile per il calcolo dei kilometri in base alla falcata
    private String weight;//peso utente per il calcolo delle calorie in base anche al numero dei passi
    private String lastWalkDate;//ultima data di registrazione del contapassi
    private ArrayList<String> walk;//array di "camminate" quotidiane che verranno salvate sul db per le statistiche,sono stringhe di tipo dataInMillisecondi,passi
    private ArrayList<String> discountUID;//array di id degli sconti che l'utente aggiunge dal backdrop presente nella UserMapView e appaiono nella home
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

    public void setDiscountSteps(ArrayList<String> discountSteps) {
        this.discountSteps = discountSteps;
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

    public ArrayList<String> getDiscountSteps() {
        return discountSteps;
    }
}

