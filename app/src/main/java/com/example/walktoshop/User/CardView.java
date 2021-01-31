package com.example.walktoshop.User;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.NetworkController.NetworkController;
import com.example.walktoshop.R;
import com.example.walktoshop.Seller.Discount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CardView extends AppCompatActivity {
    private ProgressBar progressBar;
    private Discount d;
    private String UID=null;
    private long totalSteps=0;
    private TextView goalStepRatio;
    private TextView kcal;
    private TextView kilometers;
    private TextView code;
    private String userWeight=null;
    private String userHeight=null;
    private ImageButton shareButton;
    private String locality=null;
    private String name=null;
    int percentage=0;
    private TextView title;
    String discountCode=null;
    FirebaseFirestore db=FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        progressBar = (ProgressBar) findViewById(R.id.progerssBar);
        goalStepRatio= findViewById(R.id.goalStepsRatio);
        shareButton=findViewById(R.id.shareButton);
        kcal=findViewById(R.id.kcal);
        code=findViewById(R.id.code);
        kilometers = findViewById(R.id.kilometers);
        title=findViewById(R.id.title);

        Intent intent=getIntent();
        if(intent.hasExtra("discount") && intent.hasExtra("UID")){
            Gson gson = new Gson();
            String jsonDiscount=intent.getStringExtra("discount");
            Log.d("d",jsonDiscount);
            this.d = gson.fromJson(jsonDiscount, Discount.class);

            this.UID = intent.getStringExtra("UID");
        }
        discountCode=UID+d.getUID();
        code.setText(d.getDescription());
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareDiscount();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(CardView.this)){
            networkController.connectionDialog(CardView.this);
        }
        long goal= Long.parseLong(d.getDiscountsQuantity());
        long beginDiscountDate= Long.parseLong(d.getStartDiscountDate());
        long expiringDiscountDate= Long.parseLong(d.getExpiringDate());
        getBusinessInfo(d.getBusinessUID());
        getUserInfo();
        getUserWalkInATimeRange(beginDiscountDate,expiringDiscountDate,goal,d);
    }
    private void getBusinessInfo(String businessUID){
        db.collection("attivita").document(businessUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document= task.getResult();
                    CardView.this.locality=document.getString("locality");
                    CardView.this.name=document.getString("name");
                    title.setText(name+", "+locality);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void getUserInfo(){
        db.collection("utente").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document= task.getResult();
                    CardView.this.userWeight=document.getString("weight");
                    CardView.this.userHeight=document.getString("height");
                }
            }
        });
    }
    private void getUserWalkInATimeRange(long beginDiscountDate,long expiringDiscountDate,long goal,Discount d){
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
                        percentage=Math.round((float)(totalSteps*100)/goal);
                        if(percentage>=100){
                            goalStepRatio.setText(goal+"/"+goal);
                            code.setText("Ecco il tuo codice sconto: "+discountCode);
                            if(d.getState()!="completed"){
                                changeDiscountState(d);
                            }
                        }else{
                            goalStepRatio.setText(totalSteps+"/"+goal);
                        }
                        progressBar.setProgress((int)percentage);

                        float km=calculateKilometers(Integer.parseInt(CardView.this.userHeight),totalSteps);
                        kilometers.setText(km+" Km");
                        int calories=calculateKcal(Integer.parseInt(CardView.this.userWeight),totalSteps);
                        kcal.setText(calories+" Kcal");
                    }
                }
            }
        });
    }
    private void changeDiscountState(Discount d){
        db.collection("sconti").document(d.getUID()).update("state","completed");
    }

    private Walk getWalkInfoFromString(String info){
        String[] todayAndSteps =info.split(",");
        Walk walk =new Walk();
        walk.setDate(todayAndSteps[0]);
        walk.setNumberOfSteps(todayAndSteps[1]);
        return walk;
    }
    private float calculateKilometers(int height,long steps){
        float meters;
        if(height<170){
            meters=Math.round((float)600*steps/1000);
        }else{
            meters=Math.round((float)700*steps/1000);
        }
        float kilometers=meters/1000;
        Log.d("km",kilometers+"");
        return kilometers;
    }
    private int calculateKcal(int weight,long steps){
        int kcal;
        Log.d("weight",weight+"");
        kcal= (int) Math.round((float)weight*0.0005*steps);
        Log.d("kcal",kcal+"");
        return kcal;
    }
    private void shareDiscount(){
        String message=name+", "+locality+"\n"+d.getDescription()+"\n"+"Ecco il codice sconto che ti è stato regalato:\n"+discountCode+"\n";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }



}
