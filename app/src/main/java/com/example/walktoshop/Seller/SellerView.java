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
/*
Activity che consente al venditore di passare alla registrazione attività e possiede una listView
per consentire al venditore di vedere i propri sconti
 */
public class SellerView extends AppCompatActivity {
    private ListView listView;
    private TextView alert;
    private String UID=null;
    private ProgressBar progressBar;
    private FloatingActionButton addActivityButton;
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
        addActivityButton=(FloatingActionButton)findViewById(R.id.addBusinessFab);
        scontiAttivita = (TextView) findViewById(R.id.scontiAttivita);
        discountImage = findViewById(R.id.discountImage);
        //una volta premuto il fab button il venditore senza attività registrata viene rimandato alla SellerMap
        addActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSellerMapView();
                addActivityButton.setVisibility(View.INVISIBLE);
            }
        });
        /*
        Attraverso un gioco di visibilità il bottone di aggiunta sconti è visibile solo se il venditore ha registrato la sua attività.
        In questo caso è rimandato all'activity ManageDiscount
         */
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startManageDiscount();
                alert.setVisibility(View.INVISIBLE);
            }
        });
        progressBar=findViewById(R.id.sellerViewProgressBar);
        listView=findViewById(R.id.listView);
        /*
        Viene passato dal login o dalla registrazione via venditore appena eseguita l'id del venditore in modo da poterne eseguire le query
         */
        Intent intent = getIntent();
        if(intent.hasExtra("UID")){
            UID=intent.getStringExtra("UID");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //controllo per quanto riguarda la connessione internet
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(SellerView.this)){
            networkController.connectionDialog(SellerView.this);
        }
        //query al db che controlla se il venditore è in possesso di un 'attività e in tal caso ne prende gli sconti con delle query innestrate
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
        if(item.getItemId() == R.id.action_exit){
            logOut();
        }
    }
    /**
     Metodo che verifica se il venditore è già in possesso di un'attività in tal caso la query prosegue altrimenti
     gli viene notificata l'assenza dell'attività
     */
    private void getSellerBusinessUID(){
        if(UID!=null){
            db.collection("venditore").document(this.UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document= task.getResult();
                        businessUID= (ArrayList) document.get("businessUID");
                        //se ha delle attività le recupera
                        if(businessUID!=null){
                            progressBar.setVisibility(View.VISIBLE);
                            //in caso l'attività ci sia vengono estratti dal db i relativi sconti
                            getBusiness();
                            progressBar.setVisibility(View.INVISIBLE);
                        } else if(businessUID == null){
                            alert.setText(getResources().getString(R.string.noActivity));
                            businessUID = new ArrayList<String>();
                        }
                        if(businessUID.isEmpty()) {
                            addActivityButton.setVisibility(View.VISIBLE);
                            discountImage.setVisibility(View.GONE);
                            scontiAttivita.setVisibility(View.GONE);
                            mFab.setVisibility(View.GONE);
                            alert.setText(getResources().getString(R.string.noActivity));
                            addActivityButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }
    }
    /**
     Metodo che prende gli id degli sconti e della relativa attività per poi passarli nel ViewAdapter,inoltre sono settate alcune
     importanti informazioni quali il nome dell'attività di modo che l'interfaccia sia più user friendly per il venditore e la descrizione sconto
     */
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
                                discountUID=(ArrayList) document.get("discountUID");
                                String name = document.getString("name");
                                scontiAttivita.setText(getResources().getString(R.string.discount) + " " + name + ":");
                                if(discountUID == null){
                                    discountUID = new ArrayList<>();
                                }
                                if(discountUID!=null && !discountUID.isEmpty()){
                                    //se l'attività possiede degli sconti parte la query per l'estrazione degli uid relativi
                                    getDiscounts();
                                }else{
                                    /*
                                    altrimenti viene settato l'adapter con un array vuoto e con l'utilizzo "sellerhome" questo consentirà al ViewAdapter
                                    di essere riusabile e di effettuare al suo interno un gioco di visibilità
                                     */
                                    if(discountUID == null && discountUID.isEmpty()){
                                        addActivityButton.setVisibility(View.INVISIBLE);
                                    }
                                    if(discountArray.isEmpty()){
                                        alert.setText(getResources().getString(R.string.noDiscount));
                                        alert.setVisibility(View.VISIBLE);
                                    } else {
                                        alert.setVisibility(View.GONE);
                                    }
                                    final ViewAdapter adapter=new ViewAdapter(SellerView.this,discountArray, UID,businessUID,"sellerHome");
                                    listView.setAdapter(adapter);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * Query che per ogni uid di sconti presenti nell'attività preleva le informazioni del relativo sconto per poi settare il ViewAdapter
     * con un array di sconti pieno
     */
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
    /**
     * Metodo che rimanda all'activity SellerMapView passando l'id del venditore per effettuare le query
     */
    private void goSellerMapView(){
        final Intent intent = new Intent(this, SellerMapView.class);
        intent.putExtra("UID",UID);
        startActivity(intent);
    }
    /**
     * Metodo che riamnda all'activity manageDiscount passando l'id dell'attività per effettuare le query
     */
    private void startManageDiscount(){
        final Intent intent = new Intent(this, ManageDiscount.class);
        intent.putExtra("businessUID",businessUID.get(0));
        startActivity(intent);
    }
    /**
     * Metodo che consente al venditore di tornare al login senza autenticazione
     */
    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        final Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
        finish();
    }
}

