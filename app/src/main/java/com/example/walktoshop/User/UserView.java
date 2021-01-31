package com.example.walktoshop.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walktoshop.Login_SignUp.LogIn;
import com.example.walktoshop.NetworkController.NetworkController;
import com.example.walktoshop.R;
import com.example.walktoshop.Seller.Discount;
import com.example.walktoshop.Seller.SellerViewAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class UserView extends AppCompatActivity {
    FirebaseFirestore db =FirebaseFirestore.getInstance();
    private TextView alert;
    private ListView homeListview;
    private static final String CHANNEL_ID="StepCounter_notification_channel";
    private String userUID=null;
    double latitude=0;
    double longitude=0;
    ImageView userImage;
    String city=null;
    LocationManager service;
    LocationListener locationListener;
    FusedLocationProviderClient fusedLocationClient;
    private ArrayList<Discount> myDiscounts= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        alert=findViewById(R.id.alert);
        alert.setVisibility(View.GONE);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        homeListview= findViewById(R.id.homeListView);
        //setting del channel per quando partirà il service
        localizeUser();
        createNotificationChannel();


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        break;
                    case R.id.action_map:
                        if(city==null || latitude==0 || longitude==0){
                            Toast.makeText(UserView.this,"Rilevamento della posizione in corso",Toast.LENGTH_SHORT).show();
                            askGPSpermission();
                        }else{
                            goToUserViewMap();
                        }
                        break;
                    case R.id.action_statistics:
                        goUserStatistics();
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
    private void localizeUser(){
        if(ActivityCompat.checkSelfPermission(UserView.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Task<Location> task = fusedLocationClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                        Log.d("longlat",longitude+"-"+latitude);
                        Geocoder geocoder=new Geocoder(UserView.this);
                        List<Address> addresses=new ArrayList<>();
                        try {
                            addresses=geocoder.getFromLocation(latitude,longitude,1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        city=addresses.get(0).getLocality();
                        Log.d("city",city);
                    }
                }
            });
        } else {

            ActivityCompat.requestPermissions(UserView.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private void goUserStatistics() {
        final Intent intent = new Intent(UserView.this, UserStatistics.class);
        intent.putExtra("UID", this.userUID);
        intent.putExtra("city",city);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(UserView.this)){
            networkController.connectionDialog(UserView.this);
        }
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

    private void goToUserViewMap() {
        final Intent intent = new Intent(UserView.this, UserMapView.class);
        intent.putExtra("UID", this.userUID);
        intent.putExtra("city",city);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 44){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }
        }else if(requestCode==10){
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
        }
    }
    //debug mode
    private void getUserPosition() {
        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude=location.getLatitude();
                longitude=location.getLongitude();
                Log.d("coordinates",latitude+"\n"+longitude);
                try {
                    Geocoder geocoder=new Geocoder(UserView.this);
                    List<Address> addresses=new ArrayList<>();
                    addresses=geocoder.getFromLocation(latitude,longitude,1);
                    String country=addresses.get(0).getCountryName();
                    city=addresses.get(0).getLocality();
                    goToUserViewMap();
                    //Log.d("city",city);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                new AlertDialog.Builder(UserView.this).setTitle("GPS dialog").setMessage("Do you want to turn on gps?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }).setNegativeButton("Go Back", null).show();
            }
        };

    }

    private void askGPSpermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET},10);
            }
            return;
        }
        getUserPosition();
        service.requestLocationUpdates("gps", 500, 1000, locationListener);
    }

}
