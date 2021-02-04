package com.example.walktoshop.User;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.walktoshop.Model.Discount;
import com.example.walktoshop.Model.Walk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.ListIterator;

/*
    ServiceStepCounter è un service che viene richiamato nella UserView e implementa runnable di modo che questo possa lavorare
    su un thread separato alleggerendo il lavoro del thread principale che altrimenti sarebbe sovraccaricato di lavoro.
    Esso come dice il nome, svolge la funzione di contapassi e salvataggio dati sia su file che su db.
    Alcuni commenti sono stati volutamente lasciati per mostrare come sarebbe stato l'algoritmo implementando il codice del professor Buono
    Questo è stato commentato poichè i sensori stepDetector e stepCounter sono meno presenti rispetto all'accelerometro sugli smartphone, dalle nostre prove.
 */
public class ServiceStepCounter extends Service implements Runnable{
    //definisco manager sensori, counter e detector
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    private String today=null;
    private SensorManager mSensorManager;
    //private Sensor mStepDetectorSensor;
    private int mySteps = 0;//passi che verranno aggiornati quando sarà acceso lo step-counter
    private ArrayList<String> myDiscounts;//array di sconti
    private SensorEventListener eventListener;
    private String UID=null;//uid utente
    private static final String CHANNEL_ID="StepCounter_notification_channel";//costante per la creazione del background channel
    //accelerometer
    private double magitudePrevious=0; 
    private Sensor accel;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        /*if(accel==null){
            //spesso assente negli smartphone testati per cui è stato disattivato,avere 2 tipi di rilevazioni avrebbe portato ad una maggiore inconsistenza tra dispositivi
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }*/
        Log.d("sensor","accel"+accel);
        this.today=getTodayInMills();
        //Log.d("thread", String.valueOf(Looper.myLooper() == Looper.getMainLooper()));// stampa di verifica su che thread sta lavorando
        //ottengo riferimento al servizio SENSOR_SERVICE
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
        ricevo l'array di sconti che viene passato dalla home e l'ide dell'utente per poter fare le query e salvare i dati
         */
        if(intent.hasExtra("UID") && intent.hasExtra("myDiscountsUID")){
            this.UID=intent.getStringExtra("UID");
            this.myDiscounts=intent.getStringArrayListExtra("myDiscountsUID");
            //Log.d("dis",myDiscounts.get(0));
            //creazione background notification channel
            makeNotificationIntent();
            //crea un thread separato e fa partire il contapassi
            new Thread(this).start();
        }
        return START_NOT_STICKY;
    }
    @Override
    public void run() {
        eventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                float[] values = event.values;
                int value = -1;

                if (values.length > 0) {
                    value = (int) values[0];
                }

                //controllare da che versione c'è il TYPE STEP DETECTOR
                 /*if(accel!=null){
                    float x=event.values[0];
                    float y=event.values[1];
                    float z=event.values[2];
                    double magnitude = Math.sqrt(x*x+y*y+z*z);
                    double magnitudeDelta=magnitude-magitudePrevious;
                    magitudePrevious=magnitude;
                    if(magnitudeDelta>6){
                        StepCounter.this.mySteps++;
                        Log.d("Accel"," Accelerometer:" + StepCounter.this.mySteps);
                    }
                 }else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    StepCounter.this.mySteps++;
                    Log.d("Step Detector"," Detector:" + StepCounter.this.mySteps);
                }*/
                /*
                    presupponendo che sia posto in tasca
                 */
                float x=event.values[0];
                float y=event.values[1];
                float z=event.values[2];
                double magnitude = Math.sqrt(x*x+y*y+z*z); //creo la velocità angolare del dispositivo sfruttando i valori ottenuti dall'accelerometro lungo l'asse x,y,z
                double magnitudeDelta=magnitude-magitudePrevious;//sottraggo a questa quella precedente
                magitudePrevious=magnitude;//quella precedente è inizlizzata alla nuova velocità angolare e così ad ogni rilevazione
                /*
                 se il delta è superiore a 7(valore di sensibilità provato empiricamente) allora è passo altrimenti la rilevazione continua
                 */
                if(magnitudeDelta>7){
                    ServiceStepCounter.this.mySteps++;
                    Log.d("Accel"," Accelerometer:" + ServiceStepCounter.this.mySteps);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mSensorManager.registerListener(eventListener, accel,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        /*
        if(accel!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mSensorManager.registerListener(eventListener, accel,
                        SensorManager.SENSOR_DELAY_NORMAL);//
            }
        }else{
            mSensorManager.registerListener(eventListener, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }*/
        //Log.d("thread", String.valueOf(Looper.myLooper() == Looper.getMainLooper()));
    }
    private void makeNotificationIntent(){
        Intent notificationIntent =new Intent(this,UserView.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        Notification notification =new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("WalkToShop")
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Nell'onDestroy del service vengono sempre salvati i dati sia, relativi ai passi per il completamento dello sconto e sia
     * al fine di statistiche giornaliere
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //spegnimento da background service,oppure qualora il service vada in onDestroy
        //vengono allora aggiornati i passi di tutti gli sconti in possesso
        getDiscountSteps(mySteps);
        //vengono scritti sul db i passi effettuati giornalmente e la data per le statistiche
        getUserWalkArray();
    }
    /**
    Metodo che  prende il numero dei passi dello sconto  per poi aggiornarlo attraverso query innestate
     */
    private void getDiscountSteps(int mySteps){
       db.collection("utente").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
           @Override
           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
               if(task.isSuccessful()){
                   DocumentSnapshot document = task.getResult();
                   if(document.exists()){
                       ArrayList<String> discountSteps = (ArrayList<String>) document.get("discountSteps");
                       updateDiscountSteps(mySteps, discountSteps);
                   }
               }
           }
       });
    }
    /**
        Metodo che una volta aggiornati i passi sovrascrive questi sul database
     */
    private void updateDiscountSteps(int mySteps, ArrayList<String> dsp){
        Iterator<String> it1 = dsp.iterator();
        ArrayList<String> newDsp = new ArrayList<>();
        int newSteps;
        while (it1.hasNext()){
            Discount d = getDiscountInfoFromString(it1.next());
            newSteps = Integer.parseInt(d.getDiscountsQuantity()) + mySteps;
            newDsp.add(d.getUID() + "," + newSteps);
        }
        db.collection("utente").document(UID).update("discountSteps",newDsp);
    }

    /**
     * Metodo che data una stringa scritta sul db è in grado di dividerla in due sottostringhe contenenti l'uid dello sconto
     * e il numero di passi,restituendo un oggetto di tipo discount riempito con questi attributi
     * @param info
     * @return
     */
    private Discount getDiscountInfoFromString(String info){
        Discount d = new Discount();
        String[] uidAndSteps =info.split(",");
        d.setUID(uidAndSteps[0]);
        d.setDiscountsQuantity(uidAndSteps[1]);
        return d;
    }
    /**
    Metodo che restituisce il giorno attuale in millisecondi
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
    /**
    Metodo che prende le camminate già fatte dall'utente,se nuova la aggiunge all'array altrimenti viene verificata se la data corrisponde ad oggi e
    eventualmente aggiornati i passi(caso di più rilevazioni in una stessa giornata)
     */
    private void getUserWalkArray(){
        db.collection("utente").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document=task.getResult();
                    ArrayList<String> walksArray= (ArrayList) document.get("walk");
                    if(walksArray!=null){
                        for(int i=0;i<walksArray.size();i++){
                            Log.d("walksArray",walksArray.get(i));
                        }
                        //verifica se la camminata deve essere aggiunta all'array o sovrasctitta con i passi aggiornati
                        checkIfNewWalk(walksArray);
                    }
                }
            }
        });
    }
    /**
    verifica l'ultima registrazione effettuata e quindi se sovrascrivere le informazioni nell'array  o  aggiungere la camminata,q
    questo metodo verifica l'ultima data in cui è avvenuta la rilevazione prendendola dal db
     */
    private void checkIfNewWalk(ArrayList<String> walksArray){
        db.collection("utente").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document= task.getResult();
                    String lastWalkDate=document.getString("lastWalkDate");
                    String todayPlusSteps=today + ","+String.valueOf(ServiceStepCounter.this.mySteps);
                    if(lastWalkDate!=null){
                        //funzione per l'upload delle camminate
                        long lastWalkDateLong= Long.parseLong(lastWalkDate);
                        long todayLong= Long.parseLong(ServiceStepCounter.this.today);
                        //l'ha fatta oggi?
                        if(lastWalkDateLong>=todayLong){
                            int lastWalkIndex;
                            if(walksArray.size()>0){
                                lastWalkIndex =walksArray.size()-1;
                            }else{
                                lastWalkIndex =0;
                            }
                            Log.d("last", String.valueOf(lastWalkIndex));
                            String oldWalkInfo= walksArray.get(lastWalkIndex);
                            //funzione sconcatena
                            Walk oldWalk=getWalkInfoFromString(oldWalkInfo);
                            String oldSteps=oldWalk.getNumberOfSteps();
                            //Log.d("oldsteps",oldSteps);
                            int updatedSteps= ServiceStepCounter.this.mySteps+ Integer.parseInt(oldSteps);
                            String todayPlusUpdatedSteps=today + ","+updatedSteps;
                            walksArray.set(lastWalkIndex,todayPlusUpdatedSteps);
                        }else{
                            walksArray.add(todayPlusSteps);
                        }
                    }else{
                        walksArray.add(todayPlusSteps);
                    }
                    //aggiorna la camminata
                    updateWalk(walksArray);
                }
            }
        });
    }

    /**
     * Metodo che sovrascrive l'array aggiornato di camminate sul db per le statistiche
     * @param walksArray
     */
    private void updateWalk(ArrayList<String> walksArray){
        db.collection("utente").document(UID).update("walk",walksArray).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //aggiorna la data di rilevazione essendo appena terminata una
                setLastWalkDate();
            }
        });
    }

    /**
     * Metodo che sovrascrive l'ultima data di registrazione ed essendo l'ultima query termina il thread e stacca l'accelerometro
     */
    private void setLastWalkDate(){
        db.collection("utente").document(UID).update("lastWalkDate",today).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //essendo l'ultima azione da compiere viene staccato l'accelerometro e interrotto il thread
                //tutto questo avviene nell'oncomplete poichè firebase asincrono
                mSensorManager.unregisterListener(eventListener, accel);
                new Thread(ServiceStepCounter.this).interrupt();
            }
        });
    }
    /**
    Metodo che prende la stringa salvata sul db e converte la parte prima della virgola nella data e dopo la virgola in passi.
    I dati vengono poi racchiusi in un oggetto walk
     */
    private Walk getWalkInfoFromString(String info){
        String[] todayAndSteps =info.split(",");
        Walk walk =new Walk();
        walk.setDate(todayAndSteps[0]);
        walk.setNumberOfSteps(todayAndSteps[1]);
        return walk;
    }
}