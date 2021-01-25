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
import java.util.Iterator;

public class CardView extends AppCompatActivity {
    private ProgressBar progressBar;
    private Discount d;
    private String UID=null;
    private long totalSteps=0;
    private int kilometers;
    private int kcal;
    int percentage=0;
    FirebaseFirestore db=FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        progressBar = (ProgressBar) findViewById(R.id.progerssBar);
        //progressBar.setProgress(70);
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
    protected void onStart() {
        super.onStart();
        long goal= Long.parseLong(d.getDiscountsQuantity());
        long beginDiscountDate= Long.parseLong(d.getStartDiscountDate());
        long expiringDiscountDate= Long.parseLong(d.getExpiringDate());
        getUserWalkInATimeRange(beginDiscountDate,expiringDiscountDate,goal);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void getUserWalkInATimeRange(long beginDiscountDate,long expiringDiscountDate,long goal){
        db.collection("utente").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document=task.getResult();
                    ArrayList<String> myStringedWalks= (ArrayList<String>) document.get("walk");
                    //Log.d("size",myStringedWalks.size()+"");
                    if(myStringedWalks==null){
                        myStringedWalks=new ArrayList<>();
                    }
                    Iterator it=myStringedWalks.iterator();
                    while(it.hasNext()){
                        String dateAndSteps= (String) it.next();
                        Walk walk=getWalkInfoFromString(dateAndSteps);
                        long date= Long.parseLong(walk.getDate());
                        Log.d("date", String.valueOf(date >= beginDiscountDate));
                        Log.d("date", String.valueOf(date <= expiringDiscountDate));
                        if(date>=beginDiscountDate && date<=expiringDiscountDate){
                            long walkSteps= Long.parseLong(walk.getNumberOfSteps());
                            totalSteps=totalSteps + walkSteps;
                            //dailykcal
                            //dailykm
                        }
                    }
                    if(totalSteps!=0 && goal!=0){
                        percentage=Math.round((float)(totalSteps*100)/goal) ;
                        //Log.d("percentage", percentage+"");
                        progressBar.setProgress((int)percentage);
                    }
                }
            }
        });
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
    private Walk getWalkInfoFromString(String info){
        String[] todayAndSteps =info.split(",");
        Walk walk =new Walk();
        walk.setDate(todayAndSteps[0]);
        walk.setNumberOfSteps(todayAndSteps[1]);
        return walk;
    }

}
