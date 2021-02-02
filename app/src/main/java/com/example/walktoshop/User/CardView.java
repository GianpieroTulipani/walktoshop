package com.example.walktoshop.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.Model.Discount;
import com.example.walktoshop.R;
import com.example.walktoshop.Utils.NetworkController;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

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
    private TextView discountTitle;
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
        discountTitle = (TextView) findViewById(R.id.discount);
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
        long beginDiscountDate= Long.parseLong(d.getStartDiscountDate());
        long expiringDiscountDate= Long.parseLong(d.getExpiringDate());
        getBusinessInfo(d.getBusinessUID());
        getUserInfo();
        //getUserWalkInATimeRange(beginDiscountDate,expiringDiscountDate,goal,d);

    }
    private void updateUI(String newSteps){
        long goal= Long.parseLong(d.getDiscountsQuantity());
        CardView.this.totalSteps=Integer.parseInt(newSteps);
        Log.d("totalSteps",totalSteps+"");
        if(totalSteps!=0 && goal!=0){
            percentage=Math.round((float)(totalSteps*100)/goal);
            if(percentage>=100){
                goalStepRatio.setText(goal+"/"+goal);
                discountTitle.setText("Ecco il tuo codice sconto:");
                code.setText(discountCode);
                shareButton.setVisibility(View.VISIBLE);
            }else{
                goalStepRatio.setText(totalSteps+"/"+goal);
            }
            progressBar.setProgress((int)percentage);
            Log.d("peso altezzo",userWeight+" "+userHeight);
            float km=calculateKilometers(Integer.parseInt(CardView.this.userHeight), totalSteps);
            kilometers.setText(km+" Km");
            int calories=calculateKcal(Integer.parseInt(CardView.this.userWeight),totalSteps);
            kcal.setText(calories+" Kcal");
        }
    }

    private void getBusinessInfo(String businessUID){
        db.collection("attivita").document(businessUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document= task.getResult();
                    CardView.this.locality=document.getString("locality");
                    CardView.this.name=document.getString("name");
                    title.setText(name);
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
                    ArrayList<String> discountSteps = (ArrayList<String>) document.get("discountSteps");
                    Iterator<String> it = discountSteps.iterator();
                    while (it.hasNext()){
                        Discount dis = getDiscountInfoFromString(it.next());
                        if(dis.getUID().equals(CardView.this.d.getUID())){
                            updateUI(dis.getDiscountsQuantity());
                        }
                    }
                }
            }
        });
    }
    private Discount getDiscountInfoFromString(String info){
        Discount d = new Discount();
        String[] uidAndSteps =info.split(",");
        d.setUID(uidAndSteps[0]);
        d.setDiscountsQuantity(uidAndSteps[1]);
        return d;
    }

    private float calculateKilometers(int height,long steps){
        float meters;
        Log.d("hei",height+" "+steps);
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
