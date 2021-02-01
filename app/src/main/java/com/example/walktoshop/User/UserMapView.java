package com.example.walktoshop.User;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.walktoshop.Login_SignUp.LogIn;
import com.example.walktoshop.Model.User;
import com.example.walktoshop.Utils.NetworkController;
import com.example.walktoshop.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class UserMapView extends AppCompatActivity implements GoogleMap.OnMarkerClickListener,OnMapReadyCallback {
    GoogleMap mMap;
    ProgressBar progressBar;
    List<LatLng> latLngs = new ArrayList<LatLng>();
    double latitude;
    double longitude;
    String city;
    String UID;
    boolean isSatellite = false;
    FloatingActionButton userMapFab;
    ArrayList<String> name = new ArrayList<String>();
    private int cache;
    public static final String NOTIFICATION_CHANNEL_ID = "notification_channel";
    SupportMapFragment mapFragment;
    FirebaseFirestore db =FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map_view);
        cache=getSharedPref();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        createNotificationChannel();
        Toast.makeText(UserMapView.this,"Apertura mappa in corso", Toast.LENGTH_LONG).show();
        Intent intent = getIntent();
        if (intent.hasExtra("UID") && intent.hasExtra("city") && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            UID = intent.getStringExtra("UID");
            city=intent.getStringExtra("city");
            latitude= intent.getDoubleExtra("latitude",0.0f);
            longitude= intent.getDoubleExtra("longitude",0.0f);
            //Log.d("city",latitude+city +longitude);
            db.collection("attivita").whereEqualTo("locality", city).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    int counter=0;
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot document : task.getResult()){
                            ArrayList<String> discounts= (ArrayList<String>) document.get("discountUID");
                            if(discounts == null ){
                                discounts = new ArrayList<String>();
                            }
                            double lat = Double.parseDouble(document.getString("latitude"));
                            double longt = Double.parseDouble(document.getString("longitude"));
                            name.add(document.getString("name"));
                            latLngs.add(new LatLng(lat, longt));
                            counter=counter+discounts.size();
                        }
                        boolean notification=writeSharedPref(counter);
                        if(notification){
                            Log.d("notification",notification+"");
                            sendNotification();
                        }
                    }
                    mapFragment.getMapAsync(UserMapView.this);

                }
            });
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        goHome();
                        break;
                    case R.id.action_statistics:
                        goUserStatistics();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(UserMapView.this)){
            networkController.connectionDialog(UserMapView.this);
        }
    }


    private void goUserStatistics() {
        final Intent intent = new Intent(this, UserStatistics.class);
        User user = new User();
        intent.putExtra("UID", UID);
        intent.putExtra("city",city);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        startActivity(intent);
    }

    private void goHome() {
        final Intent intent = new Intent(this, UserView.class);
        intent.putExtra("UID", UID);
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void OnItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_exit){
            logOut();
        }
    }

    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        final Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        userMapFab = (FloatingActionButton) findViewById(R.id.userMapFab);
        userMapFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSatellite == false){
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    isSatellite = true;
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    isSatellite = false;
                }
            }
        });

        Iterator<LatLng> iteratorLatLng = latLngs.listIterator();
        Iterator<String> iteratorName = name.listIterator();
        while(iteratorLatLng.hasNext() && iteratorName.hasNext()){
            mMap.addMarker(new MarkerOptions().position(iteratorLatLng.next()).title(iteratorName.next()));
        }
        LatLng myPlace = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 15));
        mMap.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng position= marker.getPosition();
        double latitude= position.latitude;
        double longitude= position.longitude;
        FragmentUserMapBackDrop fragment=new FragmentUserMapBackDrop();
        Bundle bundle=new Bundle();
        String businessUID=calculateMyBusinessCustomUID(latitude,longitude);
        bundle.putString("businessUID",businessUID);
        bundle.putString("UID",UID);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.coordinator, fragment).commit();
        return false;
    }
    private String calculateMyBusinessCustomUID(Double latitude,Double longitude){
        if(latitude!=null && longitude!=null){
            String customUID=null;
            customUID= String.valueOf(latitude+longitude);
            customUID =customUID.replaceAll("[^0-9]", "");
            return customUID;
        }else{
            return null;
        }
    }

    private int getSharedPref(){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("details", MODE_PRIVATE);
        if(prefs.contains("discountNumber")){
            int value=prefs.getInt("discountNumber", -1);
            return value;
        }else{
            return -1;
        }
    }
    private boolean writeSharedPref(int counter){
        Log.d("counter",counter+" "+cache);
        if(cache<0){
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("details", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("discountNumber", counter);
            editor.commit();
        }else{
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("details", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("discountNumber", counter);
            editor.apply();
            if(counter>cache){
                return true;
            }
        }
        return false;
    }
    private void sendNotification(){
        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        b.setAutoCancel(true)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_baseline_shop_24)
                .setContentTitle("Un nuovo sconto è stato aggiunto!")
                .setContentText("Clicca sui contrassegni rossi nella mappa per scoprirne altri.")
                .setDefaults(android.app.Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentInfo("Info")
                .setChannelId(NOTIFICATION_CHANNEL_ID);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,b.build());
    }
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager manager =getSystemService(NotificationManager.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notification = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"WalkToShop", NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(notification);
            }
        }
    }
}
