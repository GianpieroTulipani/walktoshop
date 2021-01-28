package com.example.walktoshop.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.walktoshop.Login_SignUp.LogIn;
import com.example.walktoshop.R;
import com.example.walktoshop.Seller.Discount;
import com.example.walktoshop.Seller.SellerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Iterator;


public class UserView extends AppCompatActivity {
    FirebaseFirestore db =FirebaseFirestore.getInstance();
    private TextView alert;
    private ListView homeListview;
    private static final String CHANNEL_ID="StepCounter_notification_channel";
    private String userUID=null;
    private ArrayList<Discount> myDiscounts= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);

        alert=findViewById(R.id.alert);
        alert.setVisibility(View.GONE);
        homeListview= findViewById(R.id.homeListView);
        //setting del channel per quando partirà il service
        createNotificationChannel();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_map:
                        goToUserViewMap();
                        break;
                    case R.id.action_statistics:
                        goUserStatistics();
                        break;
                    case R.id.action_notification:
                        break;
                }
                return true;
            }
        });
        Intent intent =getIntent();
        if(intent.hasExtra("UID")){
            this.userUID = intent.getStringExtra("UID");
        }
    }

    private void goUserStatistics() {
        final Intent intent = new Intent(UserView.this, UserStatistics.class);
        intent.putExtra("UID", this.userUID);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserDiscounts();
    }

    private void getUserDiscounts(){
        db.collection("utente").document(userUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document= task.getResult();
                    ArrayList<String> discountUID = (ArrayList) document.get("discountUID");
                    if(discountUID!=null){
                        getMyDiscounts(discountUID);
                    }else  if(discountUID==null){
                        alert.setVisibility(View.VISIBLE);
                        alert.setText("Nessuno sconto attivato");
                    }else if(discountUID.isEmpty()){
                        alert.setVisibility(View.VISIBLE);
                        alert.setText("Nessuno sconto attivato");
                    }
                }
            }
        });
    }
    private void getMyDiscounts(ArrayList discountUID){
        if(!discountUID.isEmpty()){
            Iterator it =discountUID.iterator();
            while(it.hasNext()){
                String uid= (String) it.next();
                myDiscounts.clear();
                db.collection("sconti").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document= task.getResult();
                            Discount discount=new Discount();
                            discount.setUID(document.getString("uid"));
                            discount.setExpiringDate(document.getString("expiringDate"));
                            discount.setBusinessUID(document.getString("businessUID"));
                            discount.setDiscountsQuantity(document.getString("discountsQuantity"));
                            discount.setStartDiscountDate(document.getString("startDiscountDate"));
                            discount.setState(document.getString("state"));
                            discount.setDescription(document.getString("description"));
                            discount.setStepNumber(document.getString("stepNumber"));
                            discount.setBusinessUID(document.getString("businessUID"));
                            myDiscounts.add(discount);
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        final SellerViewAdapter adapter=new SellerViewAdapter(UserView.this,myDiscounts, userUID,null,"userHome");
                        homeListview.setAdapter(adapter);
                    }
                });
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                }else if(grantResults[0]==PackageManager.PERMISSION_DENIED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(UserView.this,Manifest.permission.ACCESS_FINE_LOCATION)){
                        //dialog in cui spiego
                        new AlertDialog.Builder(UserView.this)
                                .setTitle("Permission")
                                .setMessage("Denying permission you can't use geo-localization")
                                .setNeutralButton("ok",null)
                                .show();
                    }
                }
                return;
        }
    }

    private void goToUserViewMap() {
        final Intent intent = new Intent(UserView.this, UserMapView.class);
        intent.putExtra("UID", this.userUID);
        startActivity(intent);

    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        return super.onOptionsItemSelected(item);
    }
    private  void startStepCounter(){
        if(userUID!=null  ){
            Intent intent =new Intent(this,StepCounter.class);
            if(isMyServiceRunning(StepCounter.class) == false){
                Toast.makeText(this,"Contapassi attivato",Toast.LENGTH_SHORT).show();
                intent.putExtra("UID",userUID);
                startService(intent);
            }else{
                Toast.makeText(this,"Contapassi disattivato",Toast.LENGTH_SHORT).show();
                stopService(intent);
            }
        }
    }
    public void OnItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.stepcounter:
                startStepCounter();
                break;
            case R.id.action_exit:
                logOut();
                break;
            case R.id.action_settings:
                break;
        }
    }

    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        final Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
        finish();
    }
    private void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "StepCounter_notification_channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager =getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
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
