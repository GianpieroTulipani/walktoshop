package com.example.walktoshop.Seller;

import android.content.Context;
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
    private ArrayList<Business> business;
    private String UID;
    private ArrayList<String> businessUID;
    FirebaseFirestore db =FirebaseFirestore.getInstance();


    public SellerViewAdapter(Context context, ArrayList<Business> business, String UID,ArrayList businessUID) {
        super(context, R.layout.activity_sellerviewadapter);
        this.context=context;
        this.business=business;
        this.UID = UID;
        this.businessUID = businessUID;
    }
    @Override
    public int getCount() {
        if(business.size()<1){
            return 1;
        }else{
            return business.size();
        }

    }
    @Override
    public Object getItem(int position) {
        return business.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getViewTypeCount() {
        return getCount();
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
        TextView myTitle = activity_business.findViewById(R.id.text);
        TextView myDescription = activity_business.findViewById(R.id.description);
        Button deletebusiness = activity_business.findViewById(R.id.deletebusiness);

        if(this.business.get(position) != null && position>=0){
            Business b=this.business.get(position);
            myTitle.setText(b.getName());
            myDescription.setText("vuota");
        }
        deletebusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pos", String.valueOf(position));
                deleteBusiness(position);
            }
        });
        return activity_business;
    }

    private void deleteBusiness(int position){
        db.collection("attivita").document(business.get(position).getUID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getSeller(position);
                        Log.d("delete", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("delete", "Error deleting document", e);
                    }
                });
    }

    private void getSeller(int position)
    {
        db.collection("venditore").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
             if(task.isSuccessful())
             {
                 DocumentSnapshot document = task.getResult();
                 ArrayList<String> businessUID = (ArrayList<String>) document.get("businessUID");
                 businessUID.remove(position);
                 SellerViewAdapter.this.business.remove(position);
                 SellerViewAdapter.this.notifyDataSetChanged();
                 Log.d("venditore",businessUID.toString());
                 updateSeller(businessUID);
             }
            }
        });
    }

    private void updateSeller(ArrayList<String> businessUID)
    {
        db.collection("venditore").document(UID).update("businessUID",businessUID).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Log.d("venditore","eliminato dall'array");
                }
            }
        });
    }
}
