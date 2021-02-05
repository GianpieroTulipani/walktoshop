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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walktoshop.Login_SignUp.LogIn;
import com.example.walktoshop.Utils.NetworkController;
import com.example.walktoshop.R;
import com.example.walktoshop.Model.Discount;
import com.example.walktoshop.Utils.ViewAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Activity che mostra all'utente gli sconti aggiunti dalla mappa e gli da la possibilità di attivare un contapassi che in assenza di sconti
 * registra i passi nelle statistiche altrimenti è mostrata un progressione degli sconti nella home fino a raggiungimento del rispettivo
 * goal.Qui inoltre avviene anche la geolocalizzazione poichè impossibile in UserMapView,si è riscontrato infatti che il caricamento della
 * mappa e la geolocalizzazioe insieme avrebbe causato un drastico abbassamento delle performance con eventuale crash del sistema in particolare
 * a causa di problemi di asincronia tra geolocalizzazione e caricamento mappa.
 * Per questo motivo latitudine,longitudine e città vengo poi passate via intent alla userMapView
 */
public class UserView extends AppCompatActivity {
    FirebaseFirestore db =FirebaseFirestore.getInstance();
    private TextView alert;
    private ListView homeListview;
    private static final String CHANNEL_ID="StepCounter_notification_channel";
    private String userUID=null;
    double latitude=0;
    double longitude=0;
    FloatingActionButton stepcounterFab;
    String city=null;
    LocationManager service;
    LocationListener locationListener;
    FusedLocationProviderClient fusedLocationClient;
    private ArrayList<String> uidDiscount=new ArrayList<>();
    private ArrayList<Discount> myDiscounts= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        alert=findViewById(R.id.alert);
        alert.setVisibility(View.GONE);
        stepcounterFab = (FloatingActionButton) findViewById(R.id.stepcounterFab);
        //se l'utente usa per la prima volta l'app si apre un dialog esplicativo poi si accende il contapassi premendo questo bottone
        //se il contapassi è già attivato viene disattivato
        stepcounterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getSharedPrefDialog() == false){
                    dialog();
                    writeSharedPrefDialog();
                }
                startStepCounter();
            }
        });
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        homeListview= findViewById(R.id.homeListView);
        //localizzazione utente
        localizeUser();
        createNotificationChannel();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_map:
                        if(city==null && latitude==0 && longitude==0){
                            Toast.makeText(UserView.this,getResources().getString(R.string.loading),Toast.LENGTH_SHORT).show();
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
        /*
        Viene preso l'uid utente dalla registrazione o dal login per effettuare le query richieste
         */
        Intent intent =getIntent();
        if(intent.hasExtra("UID")){
            this.userUID = intent.getStringExtra("UID");
        }
    }

    /**
     * Metodo per la geolocalizzazione utente che setta latitudine longitudine e città in cui è localizzato
     */
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
            //richiesta permessi
            ActivityCompat.requestPermissions(UserView.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }
    /*
        Metodo che passa il controllo all'activity UserStatistics passando anche le informazioni relative a città,latitudine e longitudione
        di modo che l'utente non debba ogni volta andare nella home per geolocalizzarsi
     */
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
        //controllo connessione ad internet
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(UserView.this)){
            networkController.connectionDialog(UserView.this);
        }
        //query che prende gli sconti aggiunti nella home se esistenti
        getUserDiscounts();
    }

    /**
     *  Query che prende gli sconti aggiunti nella home se esistenti
     */
    private void getUserDiscounts(){
        db.collection("utente").document(userUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document= task.getResult();
                    ArrayList<String> discountUID = (ArrayList) document.get("discountUID");
                    if(discountUID!=null){
                        UserView.this.uidDiscount=discountUID;
                        //se vi sono sconti questa query prende le info relative a ciascuno sconto che verrà poi passato nel viewAdapter
                        getMyDiscounts(discountUID);
                    }else  if(discountUID==null){
                        alert.setVisibility(View.VISIBLE);
                        alert.setText(R.string.noDiscountActive);
                    }else if(discountUID.isEmpty()){
                        alert.setVisibility(View.VISIBLE);
                        alert.setText(R.string.noDiscountActive);
                    }
                }
            }
        });
    }

    /**
     * Query al db che prende gli oggetti di tipo sconto e se uno sconto è stato eliminato dal venditore viene rimosso da quelli
     * in possesso dell'utente e sovrascritto il nuovo array di id di sconti in modo consistente
     * @param discountUID
     */
    private void getMyDiscounts(ArrayList discountUID){
        if(!discountUID.isEmpty()){
            int k=0;
            Iterator it =discountUID.iterator();
            while(it.hasNext()){
                String uid= (String) it.next();
                myDiscounts.clear();
                int finalK = k;
                db.collection("sconti").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document= task.getResult();
                            if(document.exists()){
                                Discount discount=new Discount();
                                discount.setUID(document.getString("uid"));
                                discount.setExpiringDate(document.getString("expiringDate"));
                                discount.setBusinessUID(document.getString("businessUID"));
                                discount.setDiscountsQuantity(document.getString("discountsQuantity"));
                                discount.setStartDiscountDate(document.getString("startDiscountDate"));
                                discount.setDescription(document.getString("description"));
                                /*
                                L'utente quando lo sconto scade non lo vede più,dovrà essere eliminato dal relativo venditore
                                che l'ha pubblicato
                                 */
                                if(Long.parseLong(discount.getExpiringDate()) > Calendar.getInstance().getTimeInMillis()) {
                                    myDiscounts.add(discount);
                                }
                            }else{
                                discountUID.remove(finalK);
                            }
                            setUpdatedArray(discountUID);
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        //viene settato l 'adapter e passati gli sconti e il tipo di utilizzo
                        final ViewAdapter adapter=new ViewAdapter(UserView.this,myDiscounts, userUID,null,"userHome");
                        homeListview.setAdapter(adapter);
                    }
                });
                k++;
            }
        }
    }


    /**
     * Metodo che sovrascrive l'array passato sul db
     * @param discountUID
     */
    private void setUpdatedArray(ArrayList<String> discountUID){
        db.collection("utente").document(UserView.this.userUID).update("discountUID",discountUID);
    }

    /**
     * Metodo che sposta il controllo all'activity userMapView inviandogli le informazioni necessarie via intent
     */
    private void goToUserViewMap() {
        Toast toast = Toast.makeText(getApplicationContext(),getResources().getString(R.string.loading),Toast.LENGTH_SHORT);
        toast.show();
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

    /**
     * Metodo che attiva il service dello step counter se esso non è già attivo altrimenti lo termina
     */
    private  void startStepCounter(){
        if(userUID!=null  ){
            Intent intent =new Intent(this, ServiceStepCounter.class);
            if(isMyServiceRunning(ServiceStepCounter.class) == false){
                Toast.makeText(this,"Contapassi attivato",Toast.LENGTH_SHORT).show();
                intent.putExtra("UID",userUID);
                intent.putStringArrayListExtra("myDiscountsUID",uidDiscount);
                startService(intent);
            }else{
                Toast.makeText(this,"Contapassi disattivato",Toast.LENGTH_SHORT).show();
                stopService(intent);
            }
        }
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

    /**
     * Metodo che disattiva il contapassi se attivo
     */
    private void killServiceIfRunning(){
        if(isMyServiceRunning(ServiceStepCounter.class) == true){
            Intent intent =new Intent(this, ServiceStepCounter.class);
            Toast.makeText(this,"Contapassi disattivato",Toast.LENGTH_SHORT).show();
            stopService(intent);
        }
    }

    /**
     * Metodo che restituisce trye se il service del contapassi è attivo altrimenti false
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo che quando all'utente viene nuovamente richiesto il permesso di geolocalizzazione se negato mostra un dialog esplicativo
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 44){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                localizeUser();
            }else if(grantResults[0]==PackageManager.PERMISSION_DENIED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(UserView.this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    //dialog in cui spiego
                    new AlertDialog.Builder(UserView.this)
                            .setTitle(R.string.permission)
                            .setMessage(R.string.denyLocalization)
                            .setNeutralButton("ok",null)
                            .show();
                }
            }
        }else if(requestCode==10){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                askGPSpermission();
            }else if(grantResults[0]==PackageManager.PERMISSION_DENIED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(UserView.this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    //dialog in cui spiego
                    new AlertDialog.Builder(UserView.this)
                            .setTitle(R.string.permission)
                            .setMessage(R.string.denyLocalization)
                            .setNeutralButton("ok",null)
                            .show();
                }
            }
        }
    }

    /**
     * I metodi di geolocalizzazione sono 2 poichè un metodo funzionava sull'emulatore e non sul device e viceversa per cui per ragioni di
     * affidabilità sono stati usati entrambi in caso uno dei due fallisca essendo molto delicato il sistema
     */
    private void getUserPosition() {
        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude=location.getLatitude();
                longitude=location.getLongitude();
                try {
                    Geocoder geocoder=new Geocoder(UserView.this);
                    List<Address> addresses=new ArrayList<>();
                    addresses=geocoder.getFromLocation(latitude,longitude,1);
                    String country=addresses.get(0).getCountryName();
                    city=addresses.get(0).getLocality();
                    goToUserViewMap();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            /**
             * Metodo che in caso di rifiuto permessi effettua il popup di un dialog
             * @param provider
             */
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

    /**
     * Metodo di richiesta permessi,se accettati si prosegue con la geolocalizzazione
     */
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

    private void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        }).setMessage(R.string.DialogFirstTime);
        // Set other d
        builder.show();
    }

    /**
     * Metodo che consente di capire se è la prima volta che un utente va nella home
     * @return
     */
    private boolean getSharedPrefDialog(){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("dialog" + userUID, MODE_PRIVATE);
        if(prefs.contains("State")){
            return true;
        }else{
            return false;
        }
    }

    private void writeSharedPrefDialog(){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("dialog" + userUID, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("State", true);
        editor.commit();
    }
}
