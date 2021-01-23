package com.example.walktoshop.User;

import java.util.HashMap;

public class Walk extends HashMap {
    private String numberOfSteps;
    private String date;

    public Walk(){}

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
