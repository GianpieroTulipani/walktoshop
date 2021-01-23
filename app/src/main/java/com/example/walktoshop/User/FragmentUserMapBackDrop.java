package com.example.walktoshop.User;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.example.walktoshop.R;
import com.example.walktoshop.Seller.Discount;
import com.example.walktoshop.Seller.SellerView;
import com.example.walktoshop.Seller.SellerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FragmentUserMapBackDrop extends Fragment {

    BottomSheetBehavior sheetBehavior;
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    private ListView backdropListview;
    private ArrayList<Discount> discountArray= new ArrayList<>();
    private ArrayList<String> businessUID=new ArrayList<>();
    private String UID;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View coordinatorLayout = (CoordinatorLayout)inflater.inflate(R.layout.fragment_map_backdrop, container, false);
        backdropListview = coordinatorLayout.findViewById(R.id.backdropListView);
       /* backdropListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent intent = new Intent(getActivity(), CardView.class);
                startActivity(intent);
            }
        });*/
        ImageView filterIcon = coordinatorLayout.findViewById(R.id.filterIcon);
        LinearLayout contentLayout = coordinatorLayout.findViewById(R.id.contentLayout);
        //vengono prese le informazioni passate cliccando sul marker
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            businessUID.add(bundle.getString("businessUID"));
            this.UID=bundle.getString("UID");
            getBusiness();
        }
        sheetBehavior = BottomSheetBehavior.from(contentLayout);
        sheetBehavior.setFitToContents(false);
        sheetBehavior.setHideable(false);//evita che il backdrop sia completamente oscurato
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);//inizialmente il  backdrop parte esteso

        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFilters();
            }
        });
        return coordinatorLayout;
    }

    private void getBusiness()
    {
        db.collection("attivita").document(businessUID.get(0)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> discountUID = (ArrayList<String>) document.get("discountUID");
                    if(discountUID!=null){
                        Log.d("discount",discountUID.size()+" ");
                        if(!discountUID.isEmpty()){
                            getDiscounts(discountUID);
                        }else{
                            final SellerViewAdapter adapter=new SellerViewAdapter(getContext(),discountArray, UID,businessUID,"backdropList");
                            backdropListview.setAdapter(adapter);
                        }
                    }
                }else{
                    Log.d("non successo","non successo");
                }
            }
        });
    }

    private void getDiscounts(ArrayList<String> discountUID){
        discountArray.clear();
        for(int k=0;k<discountUID.size();k++){
            Log.d("dis",discountUID.get(k).toString());
            db.collection("sconti").document(discountUID.get(k)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Discount discount=new Discount();
                        DocumentSnapshot document=task.getResult();
                        discount.setUID(document.getString("uid"));
                        discount.setBusinessUID(document.getString("businessUID"));
                        discount.setState(document.getString("state"));
                        discount.setExpiringDate(document.getString("expiringDate"));
                        discount.setStepNumber(document.getString("stepNumber"));
                        discount.setDescription(document.getString("description"));
                        discount.setDiscountsQuantity(document.getString("discountsQuantity"));
                        discountArray.add(discount);
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    final SellerViewAdapter adapter=new SellerViewAdapter(getContext(),discountArray, UID,businessUID,"backdropList");
                    backdropListview.setAdapter(adapter);
                }
            });
        }
    }



    private void toggleFilters() {
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setHideable(true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            sheetBehavior.setHideable(true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }
    //killa il service se attivo
    @Override
    public void onDestroy() {
        super.onDestroy();
        killServiceIfRunning();
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void killServiceIfRunning(){
        if(isMyServiceRunning(StepCounter.class) == true){
            Intent intent =new Intent(getActivity(),StepCounter.class);
            Toast.makeText(getActivity(),"Contapassi disattivato",Toast.LENGTH_SHORT).show();
            getActivity().stopService(intent);
        }
    }
}

