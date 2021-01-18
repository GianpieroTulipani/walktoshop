package com.example.walktoshop.Seller;


import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.walktoshop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class Fragment_manageDiscounts extends FragmentActivity {
    private EditText percentage;
    private EditText description;
    private EditText quantity;
    private EditText expiringDate;
    private Button add;
    int date;
    String stringedPercentage;
    String stringedDescription;
    String stringedExpiringDate;
    String stringedQuantity;
    //DatePickerDialog.OnDateSetListener listener;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_managediscounts,container,false);
        //inizializzazione variabli
        expiringDate=(EditText)view.findViewById(R.id.expiringDate);
        description=(EditText)view.findViewById(R.id.description);
        percentage=(EditText)view.findViewById(R.id.percentage);
        quantity=(EditText)view.findViewById(R.id.disocuntsQuantity);
        add=(Button)view.findViewById(R.id.add);
        //setting date picker
        expiringDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //metodo set datepicker
                final Calendar cal=Calendar.getInstance();
                date=cal.get(Calendar.DATE);
                Log.d("data", String.valueOf(date));
                int month=cal.get(Calendar.MONTH);
                int year=cal.get(Calendar.YEAR);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    DatePickerDialog datePickerDialog=new DatePickerDialog(Fragment_manageDiscounts.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                            expiringDate.setText(date+"-"+month+"-"+year);
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
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Discount discount= new Discount();
                    discount.setDescription(stringedDescription);
                    discount.setDisocuntsQuantity(stringedQuantity);
                    discount.setPercentage(stringedPercentage);
                    db.collection("sconti").add(discount).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            //continuare con l'aggiunta
                        }
                    });
                }
            }
        });

        return view;
    }
    private boolean checkInfo(){
        stringedDescription=this.description.getText().toString().trim();
        stringedPercentage =this.percentage.getText().toString().trim();
        stringedQuantity=this.quantity.getText().toString().trim();
        return true;
    }
}
