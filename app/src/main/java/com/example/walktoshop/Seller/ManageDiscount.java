package com.example.walktoshop.Seller;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.Model.Discount;
import com.example.walktoshop.Utils.NetworkController;
import com.example.walktoshop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;
/*
Activity che consentirà all'utente di effettuare l'operazione di aggiunta di uno sconto inserendo informazioni come una breve descrizione
dello sconto in questione ,il numero di passi compreso in un range (2000-60000) che il venditore dovrà raggiungere e una data di scadenza
successivamente una volta soddisfatti tutti questi requisiti l'utente potrà aggiungere uno sconto relativo alla propria attività
 */
public class ManageDiscount extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();//istanziazione db
    private String businessUID=null;//id dell'attività del venditore
    private EditText description;
    private EditText quantity;
    private TextView expiringDate;//data di scadenza che verrà mostrata a schermo per ogni sconto qualora non scaduta
    private Button add;
    private ImageButton addDate;
    private Calendar cal;//istanziazione calendario che servirà dopo per effettuare calcoli sulla scadenza
    private String dateEditText;
    private String[] separetedDate;
    private boolean fromEditText;
    int date;
    long expiringDateInMillis;//data di scadenza in millisecondi
    String stringedDescription;
    String todayInMills;
    String stringedQuantity;

    //DatePickerDialog.OnDateSetListener listener;
    /*
    Salvataggio dello stato quando un venditore sta riempiendo i campi,ad esempio in caso di rotazione dello schermo...
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("description", description.getText().toString());
        outState.putString("quantity", quantity.getText().toString());
        outState.putString("expiringDate", expiringDate.getText().toString());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managediscount);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //Viene ricevuto l'uid dell'attività del venditore ,inviato tramite intent da SellerView in modo da poter fare le query sul db
        Intent intent=getIntent();
        if(intent.hasExtra("businessUID")){
            this.businessUID=intent.getStringExtra("businessUID");
        }
        //inizializzazione variabli
        addDate=(ImageButton)findViewById(R.id.addDate);
        expiringDate=(TextView) findViewById(R.id.expiringDate);
        description=(EditText)findViewById(R.id.description);
        quantity=(EditText)findViewById(R.id.disocuntsQuantity);
        add=(Button)findViewById(R.id.add);
        //setting date picker

        if(savedInstanceState != null){
            description.setText(savedInstanceState.getString("description"));
            quantity.setText(savedInstanceState.getString("quantity"));
            expiringDate.setText(savedInstanceState.getString("expiringDate"));
        }

        cal=Calendar.getInstance();
        this.todayInMills=getTodayInMills();
        expiringDate.setEnabled(false);
        //Aggiunta data
        addDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date=cal.get(Calendar.DATE);
                int month=cal.get(Calendar.MONTH);
                int year=cal.get(Calendar.YEAR);
                /*
                Viene usato un datepicker qualora l'sdk version sia superiore a quella richiesta in tal caso vengono effettuati controlli sulla data
                di modo che un venditore non possa mettere una data antecedente a quella di oggi.L'inserimento è dunque controllato anche per utenti
                di versioni di sdk più basse,in questo caso infatti è richiesa l'inserimento della data in modo manuale con i relativi controlli
                 */
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    expiringDate.setEnabled(false);
                    fromEditText = false;
                    DatePickerDialog datePickerDialog=new DatePickerDialog(ManageDiscount.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                            month = month + 1;
                            String dateFormat = null;
                            //controlli sulla formattazione della data a schermo
                            if(month < 10 ){
                                if (date < 10){
                                    dateFormat="0"+date+"/"+"0"+month+"/"+year;
                                } else {
                                    dateFormat=date+"/"+"0"+month+"/"+year;
                                }

                            } else {
                                if (date < 10){
                                    dateFormat = "0"+date+"/"+month+"/"+year;
                                } else{
                                    dateFormat = date+"/"+month+"/"+year;
                                }
                            }

                            Date date1 = null;
                            try {
                                //la data in stringa viene poi formattata
                                date1 = new SimpleDateFormat("dd/MM/yyyy").parse(dateFormat);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            expiringDate.setText(dateFormat);
                            //la data è poi trasformata in millisecondi per una questione di comodità nei calcoli
                            ManageDiscount.this.expiringDateInMillis =  date1.getTime();
                        }
                    },year,month,date);
                    datePickerDialog.show();
                }else{
                    Toast toast = Toast.makeText(ManageDiscount.this,getResources().getString(R.string.notSupportedVersion),Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    expiringDate.setEnabled(true);
                    expiringDate.setHint(getResources().getString(R.string.expiringDialog));
                    fromEditText = true;
                }
            }
        });


        //Aggiunta sconto
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Dopo essere stati controllati gli input viene creato un oggetto ,riempito e settato sul db
                 */
                if(checkInfo()){
                    Discount discount= new Discount();
                    String customDiscountUID=calculateMyCustomDiscountUID(businessUID);
                    discount.setUID(customDiscountUID);
                    discount.setBusinessUID(businessUID);
                    discount.setExpiringDate(String.valueOf(expiringDateInMillis));
                    discount.setDescription(stringedDescription);
                    discount.setDiscountsQuantity(stringedQuantity);
                    discount.setStartDiscountDate(todayInMills);
                    addDiscount(customDiscountUID,discount);//query di aggiunta sconto sul db
                    Toast toast = Toast.makeText(getApplicationContext(),getResources().getString((R.string.correctInfo)),Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //controllo che internet sia sempre presente altrimenti dialog di avvertenza con eventuale refresh dell'activity in caso di riconnessione
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(ManageDiscount.this)){
            networkController.connectionDialog(ManageDiscount.this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    /**
    Query di aggiunta al db che richiama una query all'attività  per l'inserimento dello sconto anche nella tabella attività
     */
    private void addDiscount(String customDiscountUID, Discount discount){
        db.collection("sconti").document(customDiscountUID).set(discount).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    getBusiness(customDiscountUID);
                }
            }
        });
    }
    /**
    Query che inserisce lo sconto anche nella tabella attività
     */
    private void getBusiness(String customDiscountUID){
        db.collection("attivita").document(businessUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document=task.getResult();
                    ArrayList<String> discountUID= (ArrayList<String>) document.get("discountUID");
                    if(discountUID == null){
                        discountUID =new ArrayList<String>();
                    }
                    discountUID.add(customDiscountUID);
                    updateBusiness(discountUID);
                }
            }
        });
    }
    /**
    Query che sovrascrive i vecchi sconti erogati dall'attività con i precedenti e in piu lo sconto appena aggiunto
     */
    private void updateBusiness(ArrayList<String> discountUID){
        db.collection("attivita").document(businessUID).update("discountUID",discountUID).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("caricamento","caricamento effettuato correttamente");
                }else{
                    Log.d("caricamento","caricamento non effettuato correttamente");
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
            }
        });
    }
    /**
    Metoodo che controlla che la data anche se inserita manualmente in caso di versioni che non supportino il datepicker sia corretta
     */
    private boolean checkDateFormat(){
        String stringDate = dateEditText;
         Pattern DATE_PATTERN = Pattern.compile("([0-9]{2})/([0-9]{2})/([0-9]{4})");
        separetedDate = stringDate.split("/");
        Date dateEditText = null;

        try {
             dateEditText = new SimpleDateFormat("dd/MM/yyyy").parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(stringDate.isEmpty()){
            this.expiringDate.setError(getResources().getString(R.string.EmptyDate));
            this.expiringDate.requestFocus();
            return false;
        }else if(!DATE_PATTERN.matcher(stringDate).matches()){
             Toast toast = Toast.makeText(ManageDiscount.this,getResources().getString(R.string.dateFormat),Toast.LENGTH_LONG);
             toast.setGravity(Gravity.CENTER, 0, 0);
             toast.show();
             this.expiringDate.setError(getResources().getString(R.string.InvalidDateFormat));
             this.expiringDate.requestFocus();
             return false;
         }else if(Integer.parseInt(separetedDate[0]) <= 00 || Integer.parseInt(separetedDate[0]) > 31){
            this.expiringDate.setError(getResources().getString(R.string.NotADay));
            this.expiringDate.requestFocus();
            return false;
        }else if(Integer.parseInt(separetedDate[1]) <= 00 || Integer.parseInt(separetedDate[1]) > 12){
            this.expiringDate.setError(getResources().getString(R.string.NotAMonth));
            this.expiringDate.requestFocus();
            return false;
        }else if(Integer.parseInt(separetedDate[2]) < Calendar.getInstance().get(Calendar.YEAR)){
            this.expiringDate.setError(getResources().getString(R.string.NotAYearFree));
            this.expiringDate.requestFocus();
            return false;
        }else if(dateEditText.getTime() <= Calendar.getInstance().getTimeInMillis()){
            this.expiringDate.setError(getResources().getString(R.string.InvalidDate));
            this.expiringDate.requestFocus();
            return false;
        }

         ManageDiscount.this.expiringDateInMillis =  dateEditText.getTime();
         return true;
    }

/**
Metodo che controlla sintatticamente che tutte le informazioni fornite dal venditore siano corrette
 */

    private boolean checkInfo(){
        stringedDescription=this.description.getText().toString().trim();
        stringedQuantity=this.quantity.getText().toString().trim();
        if(fromEditText)
        dateEditText = expiringDate.getText().toString().trim();

        if(stringedDescription.isEmpty() || stringedDescription.length()>50){
            this.description.setError( getResources().getString(R.string.InvalidDescription));
            this.description.requestFocus();
            return false;
        }else if(stringedQuantity.isEmpty()){
            this.quantity.setError( getResources().getString(R.string.numStepsEmpty));
            this.quantity.requestFocus();
            return false;
        }else if(Long.parseLong(stringedQuantity) < 2000 ||  Long.parseLong(stringedQuantity) > 60000){
            // i passi vengono definiti in un certo range di modo che il venditore non possa inserirne troppi o troppo pochi
            Toast toast = Toast.makeText(this,getResources().getString(R.string.stepRange),Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            this.quantity.setError( getResources().getString(R.string.numStepsNotValid));
            this.quantity.requestFocus();
            return false;
        } else if ((expiringDateInMillis <= cal.getTimeInMillis() || expiringDateInMillis == 0) && !fromEditText){
            //mettere un avviso che indica di inserire correttamente la data
            this.expiringDate.setError(getResources().getString(R.string.invalidDate));
            this.expiringDate.requestFocus();
            return false;
        }else if(fromEditText && !checkDateFormat()){

            return false;
        }

        return true;
    }
    /**
    Metodo che calcola un id univoco per poi settare lo sconto sul db l'id
     */
    private String calculateMyCustomDiscountUID(String businessUID){
        String stringedTimeInMills= String.valueOf(System.currentTimeMillis());
        if(businessUID!=null && stringedTimeInMills!=null){
            String customUID=null;
            //l'id è composto dall'id dell'attività concatenato con l'orario in millisecondi corrente
            customUID= businessUID+stringedTimeInMills;
            return customUID;
        }else{
            return null;
        }
    }
    /**
    Metodo che restitusce la data di oggi in millisecondi
     */
    private String getTodayInMills(){
        Calendar cal = Calendar.getInstance();
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date  = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year, month, date);
        long todayMillis2 = cal.getTimeInMillis();
        return String.valueOf(todayMillis2);
    }
}
