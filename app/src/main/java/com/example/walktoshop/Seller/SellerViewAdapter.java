package com.example.walktoshop.Seller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.walktoshop.R;

import java.util.ArrayList;
import java.util.List;

public class SellerViewAdapter extends ArrayAdapter {
    Context context;
    ArrayList<Business> business;
    public SellerViewAdapter(Context context, ArrayList<Business> business) {
        super(context, R.layout.activity_sellerviewadapter);
        this.context=context;
        this.business=business;

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
        final View activity_shop =layoutInflater.inflate(R.layout.activity_sellerviewadapter,parent,false);
        TextView myTitle=activity_shop.findViewById(R.id.text);
        TextView myDescription=activity_shop.findViewById(R.id.description);
        if(this.business.get(position)!=null && position>=0){
            Business b=this.business.get(position);
            myTitle.setText(b.getName());
            myDescription.setText("vuota");
        }
        return activity_shop;
    }
}
