package com.example.walktoshop.Seller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.walktoshop.R;
import com.example.walktoshop.Seller.Business;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellerViewAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<Discount> discounts;
    private String UID;
    private ArrayList<String> businessUID;
    private String usage;
    FirebaseFirestore db =FirebaseFirestore.getInstance();


    public SellerViewAdapter(Context context, ArrayList<Discount> discounts, String UID,ArrayList businessUID,String usage) {
        super(context, R.layout.activity_sellerviewadapter);
        this.context=context;
        this.discounts=discounts;
        this.UID = UID;
        this.usage= usage;
        this.businessUID = businessUID;
    }
    @Override
    public int getCount() {
        return discounts.size();
    }
    @Override
    public Object getItem(int position) {
        return discounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getViewTypeCount() {
        if(getCount() > 0){
            return getCount();
        }else{
            return super.getViewTypeCount();
        }
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater=(LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View activity_business = layoutInflater.inflate(R.layout.activity_sellerviewadapter,parent,false);
        //caratteristiche card di sconto
        TextView myTitle = activity_business.findViewById(R.id.text);

        TextView myDescription = activity_business.findViewById(R.id.description);
        //bottone eliminazione
        Button deletebusiness = activity_business.findViewById(R.id.deletebusiness);
        //bottone attivazione contapassi

        //bottone abilitazione

        if(this.discounts.get(position) != null && position>=0 && !discounts.isEmpty()){
            Discount d=this.discounts.get(position);
            Log.d("nome",discounts.get(position).getDescription() + position);
            myTitle.setText(d.getDescription());
            myDescription.setText("vuota");
            deletebusiness.setVisibility(View.GONE);
            if(this.usage=="sellerHome"){
                deletebusiness.setVisibility(View.VISIBLE);
                deletebusiness.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("p",discounts.get(position).getUID()+" ");
                        getBusiness(position);
                    }
                });
            }
        }
        return activity_business;
    }

    private void deleteDiscount(int position){
        //getBusiness(position);
        db.collection("sconti").document(discounts.get(position).getUID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("delete", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("delete", "Error deleting document", e);
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                SellerViewAdapter.this.discounts.remove(position);
                SellerViewAdapter.this.notifyDataSetChanged();
            }
        });
    }

    private void getBusiness(int position)
    {
        db.collection("attivita").document(businessUID.get(0)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {   Log.d("position",position+" ");
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> discountUID = (ArrayList<String>) document.get("discountUID");
                    discountUID.remove(position);
                    Log.d("disocuntUID", discountUID.size()+" ");
                    updateBusiness(discountUID,position);
                }else{
                    Log.d("non successo","non successo");
                }
            }
        });
    }

    private void updateBusiness(ArrayList<String> discountUID,int position)
    {
        db.collection("attivita").document(businessUID.get(0)).update("discountUID",discountUID).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    deleteDiscount(position);
                }
            }
        });
    }

}
