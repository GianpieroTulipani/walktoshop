package com.example.walktoshop.User;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.R;
import com.example.walktoshop.Seller.Discount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;

public class CardView extends AppCompatActivity {
    private ProgressBar progressBar;
    private Discount d;
    private String UID=null;
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        progressBar = (ProgressBar) findViewById(R.id.progerssBar);
        progressBar.setProgress(70);
        Intent intent=getIntent();
        if(intent.hasExtra("discount") && intent.hasExtra("UID")){
            Log.d("s","ecco");
            Gson gson = new Gson();
            String jsonDiscount=intent.getStringExtra("discount");
            this.d= gson.fromJson(jsonDiscount, Discount.class);
            this.UID = intent.getStringExtra("UID");
        }


    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        killServiceIfRunning();
    }
    private void killServiceIfRunning(){
        if(isMyServiceRunning(StepCounter.class) == true){
            Intent intent =new Intent(this,StepCounter.class);
            Toast.makeText(this,"Contapassi disattivato",Toast.LENGTH_SHORT).show();
            stopService(intent);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
