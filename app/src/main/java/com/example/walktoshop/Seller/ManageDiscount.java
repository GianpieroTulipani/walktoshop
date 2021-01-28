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

public class ManageDiscount extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String businessUID=null;
    private EditText description;
    private EditText quantity;
    private TextView expiringDate;
    private Button add;
    private ImageButton addDate;
    private Calendar cal;
    int date;
    long expiringDateInMillis;
    String stringedDescription;
    String todayInMills;
    String stringedQuantity;

    //DatePickerDialog.OnDateSetListener listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managediscount);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
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
        cal=Calendar.getInstance();
        this.todayInMills=getTodayInMills();
        //
        addDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //metodo set datepicker
                date=cal.get(Calendar.DATE);
                int month=cal.get(Calendar.MONTH);
                Log.d("month", String.valueOf(month));
                int year=cal.get(Calendar.YEAR);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    DatePickerDialog datePickerDialog=new DatePickerDialog(ManageDiscount.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                            month = month + 1;
                            String dateFormat = null;
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
                                date1 = new SimpleDateFormat("dd/MM/yyyy").parse(dateFormat);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Log.d("data",dateFormat);
                            expiringDate.setText(dateFormat);

                            ManageDiscount.this.expiringDateInMillis =  date1.getTime();
                        }
                    },year,month,date);
                    datePickerDialog.show();
                }else{
                    //altro modo per prendere la data
                }
            }
        });



        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInfo()){
                    Discount discount= new Discount();
                    String customDiscountUID=calculateMyCustomDiscountUID(businessUID,expiringDateInMillis);
                    discount.setUID(customDiscountUID);
                    discount.setBusinessUID(businessUID);
                    discount.setExpiringDate(String.valueOf(expiringDateInMillis));
                    discount.setState("");
                    discount.setDescription(stringedDescription);
                    discount.setDiscountsQuantity(stringedQuantity);
                    discount.setStartDiscountDate(todayInMills);
                    addDiscount(customDiscountUID,discount);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addDiscount(String customDiscountUID, Discount discount){
        db.collection("sconti").document(customDiscountUID).set(discount).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("customDiscountUID",customDiscountUID);
                    getBusiness(customDiscountUID);
                }
            }
        });
    }
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

    private boolean checkInfo(){
        stringedDescription=this.description.getText().toString().trim();
        stringedQuantity=this.quantity.getText().toString().trim();
       // Log.d("MILLIS", String.valueOf(expiringDateInMillis));
        //Log.d("MILLIS", String.valueOf(cal.getTimeInMillis()));

        if(stringedDescription.isEmpty() || stringedDescription.length()>50){
            this.description.setError( getResources().getString(R.string.InvalidDescription));
            this.description.requestFocus();
            return false;
        }else if(stringedQuantity.isEmpty()){
            this.quantity.setError( getResources().getString(R.string.numStepsEmpty));
            this.quantity.requestFocus();
            return false;
        }else if(Long.parseLong(stringedQuantity) < 2000||  Long.parseLong(stringedQuantity) > 60000){
            Toast toast = Toast.makeText(this,"inserire un numero passi che sia compreso tra 2000 e 100000",Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            this.quantity.setError( getResources().getString(R.string.numStepsNotValid));
            this.quantity.requestFocus();
            return false;
        } else if ( expiringDateInMillis < cal.getTimeInMillis() || expiringDateInMillis == 0){
            //mettere un avviso che indica di inserire correttamente la data
            this.expiringDate.setError("Data non valida");
            this.expiringDate.requestFocus();
            return false;
        }
        return true;
    }
    private String calculateMyCustomDiscountUID(String businessUID,long timeInMillis){
        String stringedTimeInMills= String.valueOf(System.currentTimeMillis());
        if(businessUID!=null && stringedTimeInMills!=null){
            String customUID=null;
            customUID= businessUID+stringedTimeInMills;
            return customUID;
        }else{
            return null;
        }
    }
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
