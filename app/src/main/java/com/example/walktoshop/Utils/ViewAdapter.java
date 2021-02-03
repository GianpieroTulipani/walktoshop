package com.example.walktoshop.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.walktoshop.Model.Discount;
import com.example.walktoshop.R;
import com.example.walktoshop.User.CardView;
import com.example.walktoshop.User.ServiceStepCounter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;
/**
 * Questo adapter è usato sia nella userHome, sia nel backdrop e sia dal seller motivo per cui è stata messa in una cartella separata dalle altre
 * Essa possiede un usage ovvero un tipo di utilizza che abitlita la visibilità di alcuni bottoni e testi piuttosto che altri
 * in base alle necessità e di conseguenza vengono effettuare query diverse al db.
 */
public class ViewAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<Discount> discounts;
    private String UID;
    private ArrayList<String> businessUID;
    private String usage;
    FirebaseFirestore db =FirebaseFirestore.getInstance();

    /**
     * Il costruttore dell'adapter richiede un contesto,un'array di oggetti di tipo sconto di cui settarne le informazioni nella card,
     * l'id dell'attività(per utilizzo seller altrimenti è null),e il tipo di utilizzo
     * @param context
     * @param discounts
     * @param UID
     * @param businessUID
     * @param usage
     */
    public ViewAdapter(Context context, ArrayList<Discount> discounts, String UID, ArrayList businessUID, String usage) {
        super(context, R.layout.activity_viewadapter);
        this.context=context;
        this.discounts=discounts;
        this.UID = UID;
        this.usage= usage;
        this.businessUID = businessUID;

    }
    /*
    Metodi di cui va fatto necessariamente overriding per il funzionamento dell'adapter
     */
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
        final View activity = layoutInflater.inflate(R.layout.activity_viewadapter,parent,false);
        /*
        Sono qui inizializzati elementi comuni nell'xml,sia per l'uso user,seller e backdrop
         */
        TextView date = activity.findViewById(R.id.date);
        TextView disocuntDescription= activity.findViewById(R.id.disocuntDescription);
        ImageButton deleteDiscount =activity.findViewById(R.id.deleteDiscount);
        TextView difficulty = activity.findViewById(R.id.difficulty);
        ImageView difficultyColor = activity.findViewById(R.id.difficultyColor);
        ImageButton arrow = (ImageButton) activity.findViewById(R.id.arrow);
        ImageView addDiscount = (ImageView) activity.findViewById(R.id.addButton);
        View card = activity.findViewById(R.id.card);

        /*
        A seguito se vi sono sconti dall'array passato
         */
        if(this.discounts.get(position) != null && position>=0 && !discounts.isEmpty()){
            Discount d=this.discounts.get(position);
            //viene settata la data di scadenza
            date.setText("scadenza: "+d.millisecondsToDate(d.getExpiringDate()));
            disocuntDescription.setText(d.getDescription());
            //viene settato il goal
            String stringedGoal=d.getDiscountsQuantity();
            //in base al goal sono stati selezionati degli standard di difficoltà consultando siti di fitness
            if(stringedGoal != null){
                int goal= Integer.parseInt(stringedGoal);
                if(goal<5000){
                    difficulty.setText(R.string.easy);
                }else if(goal>=5000 && goal<=20000){
                    difficulty.setText(R.string.mediumDifficulty);
                    difficultyColor.setImageResource(R.drawable.ic_yellow);
                }else if(goal>20000){
                    difficulty.setText(R.string.difficult);
                    difficultyColor.setImageResource(R.drawable.ic_red);
                }
            }
            deleteDiscount.setVisibility(View.GONE);
            /*
            ViewAdapter lato seller
             */
            if(this.usage=="sellerHome"){
                arrow.setVisibility(View.GONE);
                addDiscount.setVisibility(View.GONE);
                deleteDiscount.setVisibility(View.VISIBLE);
                //controllo che la data non sia già scaduta
                if(d != null && d.getExpiringDate() != null){
                    if(Long.parseLong(d.getExpiringDate()) < Calendar.getInstance().getTimeInMillis()){
                        date.setText("Scaduto");
                    }
                }
                //bottone di eliminazione sconto
                deleteDiscount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //metodo che chiama con query innestate l'eliminazione dello sconto nel db
                        getBusiness(position);
                    }
                });
                /*
                View Adapter per l'user
                 */
            }else if(this.usage=="userHome"){

                arrow.setVisibility(View.VISIBLE);
                addDiscount.setVisibility(View.GONE);
                //query per settare le informazioni dell'attività dello sconto nell'adapter
                getBusinessName(d, d.getDescription(), activity);
                //se viene letto il file precedentemente scritto con chiave UID+uidsconto allora è completato e viene mostrato a schermo
                if(getSharedPrefDiscountState(d.getUID()) == true){
                    date.setText("Completato");
                }
                //freccia per i dettagli sconto in cui si passa l'oggetto di tipo sconto tramite intent
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
                /*
                ViewAdapter  utilizzo backdrop
                 */
            }else if(this.usage=="backdropList"){
                arrow.setVisibility(View.GONE);
                addDiscount.setVisibility(View.VISIBLE);
                /*
                Bottone di aggiunta sconto che una volta premuto termina il contapassi se attivo poichè darebbe problemi di inconsistenza con il db
                inizializza lo sconto sul db a passi 0 e aggiunge lo sconto tra quelli della home utente
                 */
                addDiscount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveDiscountSteps(d);
                        killServiceIfRunning();
                        addDiscounts(d.getUID());
                        card.setVisibility(View.GONE);
                        Toast toast =  Toast.makeText(getContext(),getContext().getResources().getString(R.string.addedDiscount),Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        //funzione che sava in shared pref
                    }
                });
            }
        }
        return activity;
    }

    /**
     * Metodo che scrive un file che indicherà che il relativo sconto sarà completato,il file scritto avrà chiave univoca
     * @param discountUID
     * @return
     */
    private boolean getSharedPrefDiscountState(String discountUID) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(UID + discountUID, MODE_PRIVATE);
        if(sharedPreferences.contains("c")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Metodo che inizializza lo sconto aggiunto a 0 passi e lo aggiunge al db
     * @param d
     */
    private void saveDiscountSteps(Discount d){
        db.collection("utente").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        ArrayList<String> discountSteps = (ArrayList<String>) document.get("discountSteps");
                        discountSteps.add(d.getUID()+",0");
                        uploadDiscountSteps(discountSteps);
                    }
                }
            }
        });
    }

    /**
     * Query al db innestata in saveDiscountSteps che sovrascrive l'array di sconti una volta aggiunto lo sconto a quelli precedentemente in possesso
     * dell'utente
     * @param dsp
     */
    private void uploadDiscountSteps(ArrayList<String> dsp) {
        db.collection("utente").document(UID).update("discountSteps",dsp);
    }
    /**
     * Query di eliminazione dallo sconto dal db richiama query innestate
     */
    private void deleteDiscount(int position){
        //getBusiness(position);
        db.collection("sconti").document(discounts.get(position).getUID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast toast = Toast.makeText(getContext(),getContext().getResources().getString(R.string.deletedDiscount),Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                ViewAdapter.this.discounts.remove(position);
                ViewAdapter.this.notifyDataSetChanged();
            }
        });
    }

    /**
     * Query che preleva gli sconti dell'attività per elinarne lo sconto selezionato e successivamente sovrascriverlo tramite uptateBusiness
     * @param position
     */
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
                    Log.d("discountUID", discountUID.size()+" ");
                    updateBusiness(discountUID,position);
                }else{
                    Log.d("non successo","non successo");
                }
            }
        });
    }

    /**
     * Query sulle informazioni dell'attività che al termine setta a schermo una serie di caratteristiche
     * come il nome dell'attività e la descrizione dello sconto
     * @param d
     * @param description
     * @param activity
     */
    private void getBusinessName(Discount d, String description, View activity){
        db.collection("attivita").document(d.getBusinessUID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        String name = document.getString("name");
                        TextView disocuntDescription= activity.findViewById(R.id.disocuntDescription);
                        disocuntDescription.setText(name + ": " + "\n" + description);
                    }
                }
            }
        });
    }

    /**
     * Query al db che sovrascrive l'array di sconti aggiornato
     * @param discountUID
     * @param position
     */
    private void updateBusiness(ArrayList<String> discountUID,int position)
    {
        db.collection("attivita").document(businessUID.get(0)).update("discountUID",discountUID).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {   //effettiva elimnazione sconto dal db
                    deleteDiscount(position);
                }
            }
        });
    }

    /**
     * Query che passato  l'uid dello sconto lo aggiunge a quelli già presenti nella home utente
     * @param discountuid
     */
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
                    //sovrascrive gli sconti dell'utente con il nuovo array con un nuovo sconto aggiunto
                    updateUserDiscounts(discountUID);
                }
            }
        });
    }

    /**
     * Query che sovrascrive  l'array di sconti passato sul db
     * @param discountUID
     */
    private void updateUserDiscounts(ArrayList<String> discountUID){
        db.collection("utente").document(UID).update("discountUID",discountUID);
    }

    /**
     * Metodo che verifica se il service del contapassi è attivo per poi terminarlo
     */
    private void killServiceIfRunning(){
        if(isMyServiceRunning(ServiceStepCounter.class) == true){
            Intent intent =new Intent(getContext(), ServiceStepCounter.class);
            Toast.makeText(getContext(),"Contapassi disattivato",Toast.LENGTH_SHORT).show();
            getContext().stopService(intent);
        }
    }

    /**
     * Metodo che verifica se il service del contapassi è in esecuzione
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
