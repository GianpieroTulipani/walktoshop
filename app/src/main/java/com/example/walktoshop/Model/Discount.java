package com.example.walktoshop.Model;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Discount {
    private String UID;
    private String businessUID;
    private String expiringDate;
    private String startDiscountDate;
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


    public void setExpiringDate(String expiringDate) {
        this.expiringDate = expiringDate;
    }

    public void setStartDiscountDate(String startDiscountDate) {
        this.startDiscountDate = startDiscountDate;
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

    public String getDescription() {
        return description;
    }

    public String getDiscountsQuantity() {
        return discountsQuantity;
    }
    //riconversione data
    public String millisecondsToDate(String milliseconds){
        if(milliseconds!=null){
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            long longMilliSeconds= Long.parseLong(milliseconds);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(longMilliSeconds);
            return formatter.format(calendar.getTime());
        }
        return "";
    }

    public String getStartDiscountDate() {
        return startDiscountDate;
    }
}
