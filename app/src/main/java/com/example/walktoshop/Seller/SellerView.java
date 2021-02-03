package com.example.walktoshop.Seller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.Login_SignUp.LogIn;
import com.example.walktoshop.Model.Discount;
import com.example.walktoshop.Utils.NetworkController;
import com.example.walktoshop.R;
import com.example.walktoshop.Utils.ViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SellerView extends AppCompatActivity {
    private ListView listView;
    private TextView alert;
    private String UID=null;
    private ProgressBar progressBar;
    private FloatingActionButton addActivityButton;
    private ImageButton editDiscount;
    private ImageButton deleteDiscount;
    FirebaseFirestore db =FirebaseFirestore.getInstance();
    private ArrayList<Discount> discountArray=new ArrayList<>();
    private ArrayList<String> businessUID =new ArrayList<>();
    private ArrayList<String> discountUID = new ArrayList<>();
    private FloatingActionButton mFab;
    private  ImageView discountImage;
    private TextView scontiAttivita;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_view);
        alert = (TextView) findViewById(R.id.alertSeller);
        //View coordinatorLayout = findViewById(android.R.id.content);
        addActivityButton=(FloatingActionButton)findViewById(R.id.addBusinessFab);
        scontiAttivita = (TextView) findViewById(R.id.scontiAttivita);
        discountImage = findViewById(R.id.discountImage);
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
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(SellerView.this)){
            networkController.connectionDialog(SellerView.this);
        }
        getSellerBusinessUID();
        if(businessUID == null){
            businessUID = new ArrayList<String>();
        }else if(businessUID.size()<= 0){
            addActivityButton.setVisibility(View.VISIBLE);
            discountImage.setVisibility(View.GONE);
            scontiAttivita.setVisibility(View.GONE);
            alert.setText("Nessuna attività registrata");
            addActivityButton.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.GONE);
        }else if(discountUID==null){
            addActivityButton.setVisibility(View.INVISIBLE);
        }else if(discountUID.isEmpty()){
            addActivityButton.setVisibility(View.INVISIBLE);
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
        if(item.getItemId() == R.id.action_exit){
            logOut();
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
                            alert.setText("Nessuna attività registrata");
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
            scontiAttivita.setVisibility(View.VISIBLE);
            discountImage.setVisibility(View.VISIBLE);
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
                                String name = document.getString("name");
                                scontiAttivita.setText( "Sconti " + name + ":");
                                //Log.d("TAG", "uid dello sconto" + discountUID.toString());
                                if(discountUID!=null && !discountUID.isEmpty()){
                                    getDiscounts();
                                }else{
                                    alert.setText("Nessuno sconto disponibile");
                                    alert.setVisibility(View.VISIBLE);
                                    final ViewAdapter adapter=new ViewAdapter(SellerView.this,discountArray, UID,businessUID,"sellerHome");
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
                            discount.setStartDiscountDate(document.getString("startDiscountDate"));
                            discount.setExpiringDate(document.getString("expiringDate"));
                            discount.setDescription(document.getString("description"));
                            discount.setDiscountsQuantity(document.getString("discountsQuantity"));
                            discountArray.add(discount);
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        final ViewAdapter adapter=new ViewAdapter(SellerView.this,discountArray, UID,businessUID,"sellerHome");
                        listView.setAdapter(adapter);
                    }
                });
            }
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

