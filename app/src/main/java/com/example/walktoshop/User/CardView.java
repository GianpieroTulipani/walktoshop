package com.example.walktoshop.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.Model.Discount;
import com.example.walktoshop.R;
import com.example.walktoshop.Utils.NetworkController;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Activity che  effettuando alcune query mostra a schermo i dettagli della card una volta aperta dell'utente,attraverso un gioco di visibilità
 * ciò sarà possibile solo nella userView
 */
public class CardView extends AppCompatActivity {
    private ProgressBar progressBar;
    private Discount d;
    private String UID=null;
    private long totalSteps=0;//passi totali dell'utente
    private TextView goalStepRatio;
    private TextView kcal;
    private TextView kilometers;
    private TextView code;
    private String userWeight=null;
    private String userHeight=null;
    private ImageButton shareButton;//tasto di condivisione
    private String locality=null;
    private String name=null;
    int percentage=0;//percentuale di completamento
    private TextView discountTitle;
    private TextView title;
    String discountCode=null;
    FirebaseFirestore db=FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);
        getSupportActionBar().setTitle(R.string.discount_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //inizializzazione elementi presenti nel layout activity_cardview.xml
        progressBar = (ProgressBar) findViewById(R.id.progerssBar);
        goalStepRatio= findViewById(R.id.goalStepsRatio);
        shareButton=findViewById(R.id.shareButton);
        kcal=findViewById(R.id.kcal);
        code=findViewById(R.id.code);
        discountTitle = (TextView) findViewById(R.id.discount);
        kilometers = findViewById(R.id.kilometers);
        title=findViewById(R.id.title);
        /*
        Viene ricevuto l'oggetto di tipo discount dal viewAdapter una volta premuta la freccia di modo che alcuni dettagli
        possano essere già settati a schermo nel layout e altri calcolati con altri dati richiesti tramite query al db
         */
        Intent intent=getIntent();
        if(intent.hasExtra("discount") && intent.hasExtra("UID")){
            Gson gson = new Gson();
            String jsonDiscount=intent.getStringExtra("discount");
            Log.d("d",jsonDiscount);
            this.d = gson.fromJson(jsonDiscount, Discount.class);
            this.UID = intent.getStringExtra("UID");
        }
        //il codice sconto è univoco e calcolato concatenando l'id utente e l'id sconto
        discountCode=UID+d.getUID();
        //il codice apparirà solo al 100% per ora al suo posto è mostrata la descrizione dello sconto
        code.setText(d.getDescription());
        //bottone per la condivisione
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                    Viene qui chiamato il metodo che consente la condivisione di uno sconto qualora al 100%
                 */
                shareDiscount();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //controllo per quanto riguarda la connessione con relativo dialog
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(CardView.this)){
            networkController.connectionDialog(CardView.this);
        }
        /*
        query al db per richiede informazioni da mostrare a schermo riguardo nome attvità e località
         */
        getBusinessInfo(d.getBusinessUID());
        /*
        query di richiesta informazioni personali dell'utente inserite durante il signUp quali altezza e peso per il calcolo
         */
        getUserInfo();
    }

    /**
     * Metodo che scrive nello sharedpref lo stato dello sconto (completato true)
     * di modo tale che possa poi essere letto nel ViewAdapter per settare a schermo lo stato Completato dello sconto
     * @param discountUID
     */
    private void writeInSharedPref( String discountUID) {
        SharedPreferences sharedPreferences = getSharedPreferences(UID + discountUID, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("c", true);
        editor.apply();
    }

    /**
     * Metodo che dato l'id dell'attività ne prende città e nome da settare nella card
     * @param businessUID
     */
    private void getBusinessInfo(String businessUID){
        db.collection("attivita").document(businessUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document= task.getResult();
                    CardView.this.locality=document.getString("locality");
                    CardView.this.name=document.getString("name");
                    title.setText(name);
                }
            }
        });
    }

    /**
     * Metodo che consente la chiusura della card senza che l'activity venga aggiunta nello stack delle activity
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            //l'activity viene forzata nel terminare per evitare memory leeks
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Metodo che effettua una query al db per ottenere altezza e peso utente e aggiorna la cardView con i dati appena ricevuti
     */
    private void getUserInfo(){
        db.collection("utente").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document= task.getResult();
                    CardView.this.userWeight=document.getString("weight");
                    CardView.this.userHeight=document.getString("height");
                    ArrayList<String> discountSteps = (ArrayList<String>) document.get("discountSteps");
                    Iterator<String> it = discountSteps.iterator();
                    while (it.hasNext()){
                        Discount dis = getDiscountInfoFromString(it.next());
                        if(dis.getUID().equals(CardView.this.d.getUID())){
                            updateUI(dis.getDiscountsQuantity());
                        }
                    }
                }
            }
        });
    }
    /**
     *Metodo richiamato nella query asincrona al db di firebase ,che dati i passi relativi allo sconto di cui si è aperta la card
     * vengono calcolati alcuni dati: effettuate percentuali in base al goal da raggiungere e calcolati i km e le kcal per poi essere mostrate
     * @param newSteps
     */
    private void updateUI(String newSteps){
        long goal= Long.parseLong(d.getDiscountsQuantity());
        CardView.this.totalSteps=Integer.parseInt(newSteps);
        if(totalSteps!=0 && goal!=0){
            percentage=Math.round((float)(totalSteps*100)/goal);
            if(percentage>=100){
                goalStepRatio.setText(goal+"/"+goal);
                discountTitle.setText(getResources().getString(R.string.hereDiscount));
                code.setText(discountCode);

                progressBar.setProgress((int)percentage);//viene settata la percentuale anche nella progress bar per dare un feed di avanzamento
                float km=calculateKilometers(Integer.parseInt(CardView.this.userHeight), goal);
                kilometers.setText(km+" Km");
                int calories=calculateKcal(Integer.parseInt(CardView.this.userWeight),goal);
                kcal.setText(calories+" Kcal");
                //viene scritto che lo sconto è completato su file
                writeInSharedPref(d.getUID());
                shareButton.setVisibility(View.VISIBLE);
            }else{
                goalStepRatio.setText(totalSteps+"/"+goal);

                progressBar.setProgress((int)percentage);//viene settata la percentuale anche nella progress bar per dare un feed di avanzamento
                float km=calculateKilometers(Integer.parseInt(CardView.this.userHeight), totalSteps);
                kilometers.setText(km+" Km");
                int calories=calculateKcal(Integer.parseInt(CardView.this.userWeight),totalSteps);
                kcal.setText(calories+" Kcal");
            }
        }
    }

    /**
     * N.B Gli array possono essere solo di tipo string se attributi di un oggetto da scrivere per cui per salvare le info degli sconti si è
     * pensato di inserire per ogni sconto una stringa di tipo UID,numeropassi .
     * Metodo che data una stringa la suddivide in due sottostringhe riguardanti uid sconto e numero passi e restituisce un oggetto riempito di
     * tipo sconto
     * @param info
     * @return
     */
    private Discount getDiscountInfoFromString(String info){
        Discount d = new Discount();
        String[] uidAndSteps =info.split(",");
        d.setUID(uidAndSteps[0]);
        d.setDiscountsQuantity(uidAndSteps[1]);
        return d;
    }

    /**
     * Metodo che dato in input l'altezza dell'utente stima una falcata da moltiplicare poi al numero di passi,restituendo così
     * i kilometri fatti
     * @param height
     * @param steps
     * @return
     */
    private float calculateKilometers(int height,long steps){
        float meters;
        if(height<170){
            meters=Math.round((float)600*steps/1000);
        }else{
            meters=Math.round((float)700*steps/1000);
        }
        float kilometers=meters/1000;
        return kilometers;
    }

    /**
     * Metodo che dato in input il peso utente e il numero di passi stima le calorie bruciate
     * @param weight
     * @param steps
     * @return
     */
    private int calculateKcal(int weight,long steps){
        int kcal;
        kcal= (int) Math.round((float)weight*0.0005*steps);
        return kcal;
    }

    /**
     * Metodo che mostra un chooser all'utente delle app di messaggistica presenti sul device in modo da consentire la condivisione
     * dello sconto tramite l'app che egli desidera
     */
    private void shareDiscount(){
        String message=name+", "+locality+"\n"+d.getDescription()+"\n"+R.string.share+"\n"+discountCode+"\n";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
}
