package com.example.walktoshop.Seller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.walktoshop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SellerView extends AppCompatActivity {
    private ListView listView;
    private String UID="ErwvRrl854W3ghKfevEuZd5On0R2";

    private ProgressBar progressBar;
    FirebaseFirestore db =FirebaseFirestore.getInstance();
    private ArrayList<Business> businessArray=new ArrayList<>();
    private ArrayList<String> businessUID =new ArrayList<>();
    private FloatingActionButton mFab;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_view);
        //View coordinatorLayout = findViewById(android.R.id.content);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        progressBar=findViewById(R.id.sellerViewProgressBar);
        progressBar.setVisibility(View.INVISIBLE);
        listView=findViewById(R.id.listView);
        Intent intent = getIntent();
        if(intent.hasExtra("UID")){
            UID=intent.getStringExtra("UID");
        }
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Snackbar.make(coordinatorLayout, R.string.snackbar_message, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE).show();*/
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSellerBusinessUID();
        Log.d("b",businessUID.toString());

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
                        getBusiness();
                    }
                }
            });
        }
    }
    private void getBusiness(){
        if(businessUID!=null && !businessUID.isEmpty()){
            progressBar.setVisibility(View.VISIBLE);
            for(int i=0;i<businessUID.size();i++){
                String b=businessUID.get(i);
                b=b.trim();
                Log.d("b",b);
                db.collection("attivita").document(b).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Business business=new Business();
                            DocumentSnapshot document= task.getResult();
                            if (document.exists()) {
                                Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d("TAG", "No such document");
                            }
                            business.setUID(document.getString("uid"));
                            business.setName(document.getString("name"));
                            business.setLatitude(document.getString("latitude"));
                            business.setLongitude(document.getString("longitude"));
                            business.setDiscountUID((ArrayList) document.get("discountUID"));
                            businessArray.add(business);
                            //Log.d("array2",business.getName());
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d("array2",businessArray.get(0).getName());
                        progressBar.setVisibility(View.GONE);
                        Log.d("array",businessArray.toString());
                        final SellerViewAdapter adapter=new SellerViewAdapter(SellerView.this,businessArray, UID);
                        listView.setAdapter(adapter);
                    }
                });
            }
        }
    }
}

