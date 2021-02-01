package com.example.walktoshop.Model;


public class Walk {
    private String numberOfSteps;//numero di passi della camminata
    private String date;//data di avvenimento della registrazione in millisecondi

    public Walk(){}
    //setters
    public void setDate(String date) {
        this.date = date;
    }

    public void setNumberOfSteps(String numberOfSteps) {
        this.numberOfSteps = numberOfSteps;
    }
    //getters

    public String getDate() {
        return date;
    }

    public String getNumberOfSteps() {
        return numberOfSteps;
    }
}
