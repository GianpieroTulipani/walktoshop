package com.example.walktoshop.Seller;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.example.walktoshop.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.List;

public class SellerMapView extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static final String API_KEY = "AIzaSyBrbjgwm3CB6qBhWaa3cMrRV3Ek9XW0cPc";
    SearchView search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_map_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        search = findViewById(R.id.search_bar);
        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),API_KEY);
        }
        PlacesClient client=Places.createClient(this);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String location=search.getQuery().toString();
                List<Address> addresses=null;
                if(location!=null || !location.trim().equals("")){
                    Geocoder geocoder=new Geocoder(SellerMapView.this);
                    try{
                        addresses=geocoder.getFromLocationName(location,1);
                    }catch(Exception e){e.printStackTrace();}
                    Address addr=addresses.get(0);
                    Log.d("place",addr.getLatitude()+"--"+addr.getLongitude()+"--"+addr.getLocality());
                    LatLng place=new LatLng(addr.getLatitude(),addr.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(place).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place,10));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("Info",marker.getPosition().toString()+marker.getTitle().toString());
        Toast.makeText(this,marker.getPosition().toString()+marker.getTitle().toString(), Toast.LENGTH_SHORT).show();
        return false;
    }
}
