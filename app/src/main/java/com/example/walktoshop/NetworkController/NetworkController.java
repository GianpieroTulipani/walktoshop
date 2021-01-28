package com.example.walktoshop.NetworkController;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

public class NetworkController {
    public NetworkController(){}

    public boolean isConnected(AppCompatActivity app){
        ConnectivityManager connectivityManager= (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if((wifiConnection != null && wifiConnection.isConnected()) || (mobileConnection!=null && mobileConnection.isConnected())){
            return true;
        }else{
            return false;
        }
    }
    public void connectionDialog(AppCompatActivity app){
        AlertDialog.Builder builder = new AlertDialog.Builder(app);
        builder.setMessage("Connessione internet troppo debole o assente.")
                .setCancelable(false)
                .setPositiveButton("Connetti", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        app.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }).setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //restart Activity
                app.finish();
                app.startActivity(app.getIntent());
            }
        }).show();
    }
}
