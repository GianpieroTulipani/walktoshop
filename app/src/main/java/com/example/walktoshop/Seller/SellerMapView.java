package com.example.walktoshop.Seller;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellerMapView extends FragmentActivity implements OnMapReadyCallback {

    FirebaseFirestore db=FirebaseFirestore.getInstance();
    private GoogleMap mMap;
    private String UID = null;
    private static final String API_KEY = "AIzaSyBrbjgwm3CB6qBhWaa3cMrRV3Ek9XW0cPc";
    SearchView search;
    String location;
    boolean isExisting = false;
    List<LatLng> latLngs = new ArrayList<LatLng>();
    Business business=new Business();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_map_view);
        Intent intent = getIntent();
        if(intent.hasExtra("UID")){
            UID=intent.getStringExtra("UID");
//            Log.d("uid",UID);
            SellerMapView.this.business.setOwnerUID(UID);
        }
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
                location=search.getQuery().toString();
                List<Address> addresses=null;
                if(location!=null || !location.trim().equals("")){
                    Geocoder geocoder=new Geocoder(SellerMapView.this);
                    try{
                        addresses=geocoder.getFromLocationName(location,1);
                        if(addresses.isEmpty()){
                            dialog();
                        }
                        Log.d("ADDRESS", String.valueOf(addresses));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    if (addresses!=null && !addresses.isEmpty()){
                        Address addr=addresses.get(0);
                        verifyBusiness(addr);
                        Log.d("isExisting", String.valueOf(isExisting));

                    }
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

    private void verifyBusiness(Address addr) {

        double latitude = addr.getLatitude();
        double longitude = addr.getLongitude();
        LatLng placeLatLng = new LatLng(latitude,longitude);
        String locality = addr.getLocality();
        Log.d("PLACE", latitude+"-"+longitude+"-"+locality);

        db.collection("attivita").whereEqualTo("locality", locality).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        double lat = Double.parseDouble(document.getString("latitude"));
                        double longt = Double.parseDouble(document.getString("longitude"));
                        Log.d("DB-PLACE", lat+"-"+longt);
                        if(latitude == lat && longitude == longt){
                            isExisting = true;
                            break;
                        }
                    }
                    if(isExisting == true){
                        AlertDialog.Builder builder = new AlertDialog.Builder(SellerMapView.this);
                        // Add the buttons
                        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).setMessage("Attività già eistente inseriscine un altra");
                        // Set other d
                        builder.show();
                        isExisting = false;
                    } else {
                        Log.d("place",addr.getLatitude()+"--"+addr.getLongitude()+"--"+addr.getLocality());
                        LatLng place=new LatLng(addr.getLatitude(),addr.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(place).title(location));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place,15));
                        Toast.makeText(SellerMapView.this, R.string.addBusinessDialog, Toast.LENGTH_LONG).show();

                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                if(SellerMapView.this.location.contains(",")){
                                    String[] res = SellerMapView.this.location.split("[,]", 0);
                                    SellerMapView.this.location = res[0];
                                }
                                SellerMapView.this.business.setName(SellerMapView.this.location);
                                SellerMapView.this.business.setLongitude(String.valueOf(addr.getLongitude()));
                                SellerMapView.this.business.setLatitude(String.valueOf(addr.getLatitude()));
                                SellerMapView.this.business.setLocality(addr.getLocality());
                                SellerMapView.this.business.setUID(calculateMyBusinessCustomUID(addr.getLatitude(),addr.getLongitude()));
                                Log.d("customuid",SellerMapView.this.business.getUID());
                                setBusiness(SellerMapView.this.business);
                                return false;
                            }
                        });
                        //inizalizzazione oggetto da scrivere
                    }

                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng italy = new LatLng(43.06103001266056, 12.882105287940128);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(italy));
    }

    private void setBusiness(Business business){

        db.collection("attivita").document(business.getUID()).set(this.business).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    getSeller(business.getUID());
                }
            }
        });
    }
    private void getSeller(String businessCustomUID){
        db.collection("venditore").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> businessUID = (ArrayList<String>) document.get("businessUID");
                    if(businessUID == null)
                    {
                        businessUID = new ArrayList<>();
                    }
                    businessUID.add(businessCustomUID);
                    Log.d("op",businessUID.toString());
                    updateSeller(businessUID);
                }
            }
        });
    }
    private void updateSeller(ArrayList<String> businessUID){
        db.collection("venditore").document(UID).update("businessUID",businessUID).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Log.d("venditore","successo");
                    finish();
                }
            }
        });
    }
    private void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        }).setMessage(R.string.businessNotFound);
        // Set other d
        builder.show();
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
}
