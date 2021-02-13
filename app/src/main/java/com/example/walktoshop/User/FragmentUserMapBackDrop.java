package com.example.walktoshop.User;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.walktoshop.R;
import com.example.walktoshop.Model.Discount;
import com.example.walktoshop.Utils.ViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;

public class FragmentUserMapBackDrop extends Fragment {

    BottomSheetBehavior sheetBehavior;
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    private ListView backdropListview;
    private ArrayList<Discount> discountArray= new ArrayList<>();
    private ArrayList<String> businessUID=new ArrayList<>();
    private String UID;
    private ArrayList<String> disUID = new ArrayList<String>();
    private TextView discountDescription;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //viene fatto un inflate del fragment all'interno del layout della activity
        View coordinatorLayout = (CoordinatorLayout)inflater.inflate(R.layout.fragment_map_backdrop, container, false);
        backdropListview = coordinatorLayout.findViewById(R.id.backdropListView);
        discountDescription= coordinatorLayout.findViewById(R.id.discountDescription);

        ImageView filterIcon = coordinatorLayout.findViewById(R.id.filterIcon);
        LinearLayout contentLayout = coordinatorLayout.findViewById(R.id.contentLayout);
        //vengono prese le informazioni passate cliccando sul marker
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            businessUID.add(bundle.getString("businessUID"));
            this.UID=bundle.getString("UID");
            getUserDiscount();
        }

        sheetBehavior = BottomSheetBehavior.from(contentLayout);//permette al linear layout all'interno di un coordinator layout di ottenere il comportamento di una bottomSheetView
        sheetBehavior.setFitToContents(false);//fa in modo che l'altezza del backdrop sia contenuta nella grandezza del contenitore
        sheetBehavior.setHideable(false);//evita che il backdrop sia completamente oscurato
        sheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);//inizialmente il  backdrop parte a metà della sua massima altezza


        //quando si clicca sull'ImageView c'è un listener che sull'onClick chiama un metodo
        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFilters();
            }
        });
        return coordinatorLayout;
    }


    /**
     * esegue una query per prendere dalla collection utente, in corrispondenza dell'identificatore univoco ottenuto nell'onCreate tutti
     * gli sconti corrispondenti a quell'utente
     */
    private void getUserDiscount() {
        db.collection("utente").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> userDisUID = (ArrayList<String>) document.get("discountUID");
                    Log.d("disocunt uidArray",userDisUID.size()+"");
                    if (userDisUID!= null){
                        getBusiness(userDisUID);
                    } else{
                        final ViewAdapter adapter=new ViewAdapter(getContext(),discountArray, UID,businessUID,"backdropList");
                        backdropListview.setAdapter(adapter);
                    }
                    Log.d("disUID", String.valueOf(userDisUID));
                }
            }
        });
    }

    /**
     * questo metodo prende in input gli sconti corrispondenti all'UID dell'utente ottenuto nell'onCreate, successivamente esegue una query
     * per ottenere dalla attività selezionata dall'utente nel backdrop, corrispondente ad un determinato business UID tutti gli sconti corrispondenti ad essa.
     * @param userDisUID
     */
    private void getBusiness(ArrayList<String> userDisUID) {
        db.collection("attivita").document(businessUID.get(0)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> discountUID = (ArrayList<String>) document.get("discountUID");
                    Log.d("disBusiness", String.valueOf(discountUID));
                    if(discountUID!=null){
                        Log.d("discount",discountUID.size()+" ");
                        Log.d("business",businessUID.get(0));
                        discountDescription.setText(R.string.discount_list);
                        if(!discountUID.isEmpty()){
                            getDiscounts(discountUID, userDisUID);
                        }else{
                            discountDescription.setText(R.string.no_discount_available);
                            final ViewAdapter adapter=new ViewAdapter(getContext(),discountArray, UID,businessUID,"backdropList");
                            backdropListview.setAdapter(adapter);
                        }
                    }else{
                        discountDescription.setText(R.string.no_discount_available);
                    }
                }
            }
        });
    }

    /**
     * questo metodo prende in input gli sconti relativi ad una determinata attività selezionata dal backdrop, e gli sconti relativi all'utente
     * che sta utilizzando l'applicazione e verifica all'inizio che non ci siano all'interno dell' backdrop sconti già presenti all'interno della home, se ci sono
     * essi vengono eliminati dal backdrop in modo tale da non essere più selezionabili e poi successivamente caricati in un ArrayList di tipo discount
     * che successivamente sarà caricato all'interno del backdrop.
     * @param discountUID
     * @param userDisUID
     */
    private void getDiscounts(ArrayList<String> discountUID, ArrayList<String> userDisUID){

        for(int i=0; i<discountUID.size(); i++){
            if(!userDisUID.contains(discountUID.get(i))){
                disUID.add(discountUID.get(i));
            }
        }
        for(int k=0;k<disUID.size();k++){
            Log.d("dis",disUID.get(k).toString());
            int finalK = k;
            db.collection("sconti").document(disUID.get(k)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Discount discount=new Discount();
                        DocumentSnapshot document=task.getResult();
                        if(document.exists()){
                            discount.setUID(document.getString("uid"));
                            discount.setBusinessUID(document.getString("businessUID"));
                            discount.setExpiringDate(document.getString("expiringDate"));
                            discount.setDescription(document.getString("description"));
                            discount.setDiscountsQuantity(document.getString("discountsQuantity"));
                            if(discount!=null){
                                if(Long.parseLong(discount.getExpiringDate()) > Calendar.getInstance().getTimeInMillis()){
                                    discountArray.add(discount);
                                } else {
                                    disUID.remove(finalK);
                                    if(disUID.isEmpty() || disUID == null){
                                        discountDescription.setText(R.string.no_discount_available);
                                    }
                                }
                            }
                        }
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    final ViewAdapter adapter=new ViewAdapter(getContext(),discountArray, UID,businessUID,"backdropList");
                    backdropListview.setAdapter(adapter);

                }
            });
        }
        if(disUID.isEmpty() || disUID == null){
            discountDescription.setText(R.string.no_discount_available);
        }
    }

    /**
     * quuesto metodo cambia lo stato del backdrop quando viene cliccata l'image view facendolo passare da esteso o metà esteso ad invisibile.
     */
    private void toggleFilters() {
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setHideable(true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            sheetBehavior.setHideable(true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

}

