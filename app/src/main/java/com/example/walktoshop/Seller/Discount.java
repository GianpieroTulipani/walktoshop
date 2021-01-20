package com.example.walktoshop.Seller;

public class Discount {
    private String UID;
    private String businessUID;
    private String state;
    private String expiringDate;
    private String stepNumber;
    private String percentage;
    private String description;
    private String discountsQuantity;
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

    public void setBusinessUID(String businessUID) {
        this.businessUID = businessUID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setExpiringDate(String expiringDate) {
        this.expiringDate = expiringDate;
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

    public void setDiscountsQuantity(String discountsQuantity) {
        this.discountsQuantity = discountsQuantity;
    }

    //getters
    public String getUID() {
        return UID;
    }

    public String getBusinessUID() {
        return businessUID;
    }

    public String getExpiringDate() {
        return expiringDate;
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

    public String getDiscountsQuantity() {
        return discountsQuantity;
    }
}
