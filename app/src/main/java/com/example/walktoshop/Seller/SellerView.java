package com.example.walktoshop.Seller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.Login_SignUp.LogIn;
import com.example.walktoshop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SellerView extends AppCompatActivity {
    private ListView listView;
    private String UID=null;
    private ProgressBar progressBar;
    private Button addActivityButton;
    FirebaseFirestore db =FirebaseFirestore.getInstance();
    private ArrayList<Discount> discountArray=new ArrayList<>();
    private ArrayList<String> businessUID =new ArrayList<>();
    private ArrayList<String> discountUID = new ArrayList<>();
    private FloatingActionButton mFab;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_view);
        //View coordinatorLayout = findViewById(android.R.id.content);
        addActivityButton=(Button)findViewById(R.id.addActivityButton);
        addActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSellerMapView();
                addActivityButton.setVisibility(View.INVISIBLE);
            }
        });
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startManageDiscount();
            }
        });
        progressBar=findViewById(R.id.sellerViewProgressBar);
        listView=findViewById(R.id.listView);
        Intent intent = getIntent();
        if(intent.hasExtra("UID")){
            UID=intent.getStringExtra("UID");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSellerBusinessUID();
        if(discountUID==null){
            addActivityButton.setVisibility(View.INVISIBLE);
        }else if(discountUID.isEmpty()){
            Log.d("bi",discountUID.toString());
            addActivityButton.setVisibility(View.INVISIBLE);
            if(businessUID==null){

            }
            else if(businessUID.size()<= 0){
                Log.d("Busi", String.valueOf(businessUID.size()));
                addActivityButton.setVisibility(View.VISIBLE);
                mFab.setVisibility(View.GONE);

            }
        }else{
            //editext con su scritto non hai alcuno sconto disponibile
        }
    }


    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        return super.onOptionsItemSelected(item);
    }
    public void OnItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                break;
            case R.id.action_exit:
                logOut();
                break;
            case R.id.action_settings:
                break;
        }
    }

    private void getSellerBusinessUID(){
        if(UID!=null){
            db.collection("venditore").document(this.UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document= task.getResult();
                        businessUID= (ArrayList) document.get("businessUID");
                        //se ha delle attività le recupera
                        //Log.d("businessUID", String.valueOf(businessUID.size()));
                        if(businessUID!=null){
                            progressBar.setVisibility(View.VISIBLE);
                            getBusiness();
                            progressBar.setVisibility(View.INVISIBLE);
                        }else{
                            addActivityButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }
    }
    private void getBusiness(){
        if(businessUID!=null && !businessUID.isEmpty()){
            addActivityButton.setVisibility(View.INVISIBLE);
            mFab.setVisibility(View.VISIBLE);
            for(int i=0;i<businessUID.size();i++){
                String b=businessUID.get(i);
                Log.d("b",b);
                db.collection("attivita").document(b).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document= task.getResult();
                            if (document.exists()) {
                                //Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                                discountUID=(ArrayList) document.get("discountUID");
                                //Log.d("TAG", "uid dello sconto" + discountUID.toString());
                                if(discountUID!=null && !discountUID.isEmpty()){
                                    getDiscounts();
                                }else{
                                    final SellerViewAdapter adapter=new SellerViewAdapter(SellerView.this,discountArray, UID,businessUID,"sellerHome");
                                    listView.setAdapter(adapter);
                                }
                            } else {
                                Log.d("TAG", "No such document");
                            }
                        }
                    }
                });
            }
        }

    }
    private void getDiscounts(){
        discountArray.clear();
            for(int k=0;k<discountUID.size();k++){
                db.collection("sconti").document(discountUID.get(k)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Discount discount=new Discount();
                            DocumentSnapshot document=task.getResult();
                            discount.setUID(document.getString("uid"));
                            discount.setBusinessUID(document.getString("businessUID"));
                            discount.setState(document.getString("state"));
                            discount.setStartDiscountDate(document.getString("startDiscountDate"));
                            discount.setExpiringDate(document.getString("expiringDate"));
                            discount.setStepNumber(document.getString("stepNumber"));
                            discount.setPercentage(document.getString("percentage"));
                            discount.setDescription(document.getString("description"));
                            discount.setDiscountsQuantity(document.getString("discountsQuantity"));
                            discountArray.add(discount);
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        final SellerViewAdapter adapter=new SellerViewAdapter(SellerView.this,discountArray, UID,businessUID,"sellerHome");
                        listView.setAdapter(adapter);
                    }
                });
            }
    }
    private void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                goSellerMapView();

            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        }).setMessage(R.string.emptyBusiness);
        // Set other d
        builder.show();
    }
    private void goSellerMapView(){
        final Intent intent = new Intent(this, SellerMapView.class);
        intent.putExtra("UID",UID);
        startActivity(intent);
    }
    private void startManageDiscount(){
        final Intent intent = new Intent(this, ManageDiscount.class);
        intent.putExtra("businessUID",businessUID.get(0));
        //Log.d("u",businessUID.get(0).toString());
        startActivity(intent);
    }

    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        final Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
        finish();
    }
}

