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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.walktoshop.R;
import com.example.walktoshop.Seller.Business;
import com.example.walktoshop.User.CardView;
import com.example.walktoshop.User.UserView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

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
        final View activity = layoutInflater.inflate(R.layout.activity_sellerviewadapter,parent,false);
        //caratteristiche card di sconto
        TextView disocuntDescription= activity.findViewById(R.id.disocuntDescription);
        TextView date=activity.findViewById(R.id.date);
        //bottone eliminazione
        ImageButton deleteBusiness =activity.findViewById(R.id.deletebusiness);
        //modifica
        ImageButton editBusiness= activity.findViewById(R.id.editBusiness);
        TextView difficulty=activity.findViewById(R.id.difficulty);
        ImageButton arrow = (ImageButton) activity.findViewById(R.id.frecciaCardView);

        //bottone attivazione contapassi
        //bottone abilitazione
        if(this.discounts.get(position) != null && position>=0 && !discounts.isEmpty()){
            Discount d=this.discounts.get(position);
            //settare tutti gli attributi xml
            date.setText("Scadenza: "+d.millisecondsToDate(d.getExpiringDate()));
            disocuntDescription.setText(d.getDescription());
            String stringedGoal=d.getDiscountsQuantity();
            Log.d("string",stringedGoal+"");


            /*
            int goal= Integer.parseInt(stringedGoal);
            if(goal<5000){
                //easy
            }else if(goal>=5000 && goal<=20000){
                //medium
            }else if(goal>20000){
                //difficult
            }
            difficulty.setText("Difficulty:");*/
            //visibility
            deleteBusiness.setVisibility(View.GONE);
            editBusiness.setVisibility(View.GONE);
            if(this.usage=="sellerHome"){
                arrow.setVisibility(View.GONE);
                deleteBusiness.setVisibility(View.VISIBLE);
                editBusiness.setVisibility(View.VISIBLE);
                deleteBusiness.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("p",discounts.get(position).getUID()+" ");
                        getBusiness(position);
                    }
                });
            }else if(this.usage=="userHome"){
                arrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent intent = new Intent(getContext(), CardView.class);
                        Gson gson = new Gson();
                        String jsonDiscount = gson.toJson(d);
                        intent.putExtra("discount",jsonDiscount);
                        Log.d("json",jsonDiscount);
                        intent.putExtra("UID",UID);
                        context.startActivity(intent);
                    }
                });
                //qui si devono inserire elementi grafici tipici della vista in cui è chiamato
            }else if(this.usage=="backdropList"){
                arrow.setVisibility(View.GONE);
                editBusiness.setVisibility(View.VISIBLE);
                editBusiness.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addDiscounts(d.getUID());
                    }
                });
                //qui si devono inserire elementi grafici tipici della vista in cui è chiamato
            }
        }
        return activity;
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

    //query di aggiunta sconto
    private void addDiscounts(String discountuid){
        db.collection("utente").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document=task.getResult();
                    ArrayList<String> discountUID= (ArrayList<String>) document.get("discountUID");
                    if(discountUID==null){
                        discountUID=new ArrayList<>();
                    }
                    discountUID.add(discountuid);
                    updateUserDiscounts(discountUID);
                }
            }
        });
    }
    private void updateUserDiscounts(ArrayList<String> discountUID){
        db.collection("utente").document(UID).update("discountUID",discountUID);
    }

}
