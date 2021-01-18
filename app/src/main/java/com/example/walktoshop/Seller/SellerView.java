package com.example.walktoshop.Seller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentTransaction;

import com.example.walktoshop.R;
import com.example.walktoshop.User.FragmentUserMapBackDrop;
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
    private String UID=null;

    private ProgressBar progressBar;
    FirebaseFirestore db =FirebaseFirestore.getInstance();
    private ArrayList<Business> businessArray=new ArrayList<>();
    private ArrayList<String> businessUID =new ArrayList<>();
    private FloatingActionButton mFab;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_view);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSellerMapView();
            }
        });
        progressBar=findViewById(R.id.sellerViewProgressBar);
        progressBar.setVisibility(View.INVISIBLE);
        listView=findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.d("SELLERVIEW","sei dentro l'onclick");
            }
        });
        Intent intent = getIntent();
        if(intent.hasExtra("UID")){
            UID=intent.getStringExtra("UID");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSellerBusinessUID();

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
                        //se ha delle attività le recupera
                        if(businessUID.size()>0 && businessUID!=null && !businessUID.isEmpty()){
                            getBusiness();
                        }else{
                            dialog();
                        }
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
                            business.setOwnerUID(UID);
                            business.setUID(document.getString("uid"));
                            business.setLocality(document.getString("locality"));
                            business.setName(document.getString("name"));
                            business.setLatitude(document.getString("latitude"));
                            business.setLongitude(document.getString("longitude"));
                            business.setDiscountUID((ArrayList) document.get("discountUID"));
                            businessArray.add(business);
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        final SellerViewAdapter adapter=new SellerViewAdapter(SellerView.this,businessArray, UID,businessUID);
                        listView.setAdapter(adapter);
                    }
                });
            }
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
    private void startDiscountFragment(){

    }
}

