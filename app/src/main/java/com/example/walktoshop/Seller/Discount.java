package com.example.walktoshop.Seller;

public class Discount {
    private String UID;
    private String state;
    private String stepNumber;
    private String percentage;
    private String description;
    private String disocuntsQuantity;
    //private Date expiring date
       /*
    o	UID
o	Codice(per renderlo unico uidsconto+uidUtente)
o	Stato
o	NPassiCompletamentoSconto
o	GiornodiScadenza

     */
    public Discount(){};
    //setters

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setStepNumber(String stepNumber) {
        this.stepNumber = stepNumber;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisocuntsQuantity(String disocuntsQuantity) {
        this.disocuntsQuantity = disocuntsQuantity;
    }

    //getters
    public String getUID() {
        return UID;
    }

    public String getState() {
        return state;
    }

    public String getStepNumber() {
        return stepNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getPercentage() {
        return percentage;
    }

    public String getDisocuntsQuantity() {
        return disocuntsQuantity;
    }
}
