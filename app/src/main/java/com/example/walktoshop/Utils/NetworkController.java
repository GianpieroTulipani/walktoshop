package com.example.walktoshop.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.R;

/*
    La classe network controller contiene i metodi che vengono richiamati nell'on start di ogni attività per controllare se internet è presente
 */
public class NetworkController {
    public NetworkController(){}
    //verifica se l'app è connessa ad internet sia tramite wifi che tramite connessione dati
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
    //in caso non sia connesso parte un dialog che chiede di riconnettersi riportando l'utente alle impostazioni di rete altrimenti avviene il refresh dell'activity in corso
    public void connectionDialog(AppCompatActivity app){
        AlertDialog.Builder builder = new AlertDialog.Builder(app);
        builder.setMessage(R.string.internetFail)
                .setCancelable(false)
                .setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        app.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }).setNegativeButton(R.string.Undo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //restart Activity
                app.finish();
                app.startActivity(app.getIntent());
            }
        }).show();
    }
}
