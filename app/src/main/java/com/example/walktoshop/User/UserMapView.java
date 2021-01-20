package com.example.walktoshop.User;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.example.walktoshop.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class UserMapView extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    GoogleMap mMap;
    ProgressBar progressBar;
    List<LatLng> latLngs = new ArrayList<LatLng>();
    LocationManager service;
    LocationListener locationListener;
    double latitude;
    double longitude;
    String city;
    FirebaseFirestore db =FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map_view);
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0.0f);
        longitude = intent.getDoubleExtra("longitude", 0.0f);
        city = intent.getStringExtra("city");
        Log.d("city",city);
        getBusinessOnMap();
        Log.d("Dimension", String.valueOf(latLngs.size()));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(UserMapView.this);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        goHome();
                        break;
                    case R.id.action_statistics:
                        break;
                    case R.id.action_notification:
                        break;
                }
                return true;
            }
        });
    }

    //peppe devi fare qui le query per riempire l'ArrayList, usa l'iterator
    @Override
    protected void onStart() {
        super.onStart();

    }
    private void getBusinessOnMap(){
        db.collection("attivita").whereEqualTo("locality", city).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        double lat = Double.parseDouble(document.getString("latitude"));
                        Log.d("latitude", String.valueOf(lat));
                        double longt = Double.parseDouble(document.getString("longitude"));
                        Log.d("longitude", String.valueOf(longt));
                        latLngs.add(new LatLng(lat, longt));
                        Log.d("LatLng", String.valueOf(latLngs));
                        Log.d("Attività", String.valueOf(document.getData()));
                    }
                }
            }
        });
    }

    public void goHome() {
        final Intent intent = new Intent(this, UserView.class);
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
        switch (item.getItemId()) {
            case R.id.action_search:
                break;
            case R.id.action_exit:
                break;
            case R.id.action_settings:
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Iterator<LatLng> iterator = latLngs.listIterator();
        while(iterator.hasNext()){
            Log.d("Iterator", String.valueOf(iterator.next().latitude)+"-"+iterator.next().longitude);
            mMap.addMarker(new MarkerOptions().position(iterator.next()));
        }
        LatLng myPlace = new LatLng(latitude, longitude);
        //mMap.addMarker(new MarkerOptions().position(italy).title("I'm here"));
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(italy));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 15));
        mMap.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new FragmentUserMapBackDrop()).commit();
        return false;
    }
}
