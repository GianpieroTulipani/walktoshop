package com.example.walktoshop.User;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.walktoshop.Model.Walk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
/*
    ServiceStepCounter è un service che viene richiamato nella UserView e implementa runnable di modo che questo possa lavorare
    su un thread separato alleggerendo il lavoro del thread principale che altrimenti sarebbe sovraccaricato di lavoro.
    Esso come dice il nome, svolge la funzione di contapassi e salvataggio dati sia su file che su db.
    Alcuni commenti sono stati volutamente lasciati per mostrare come sarebbe stato l'algoritmo implementando il codice del professor Buono
    Questo è stato commentato poichè i sensori stepDetector e stepCounter sono meno presenti rispetto all'accelerometro dalle nostre prove.
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
        //Log.d("sensor","detector"+mStepDetectorSensor);
        accel=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        /*if(accel==null){
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }*/
        Log.d("sensor","accel"+accel);
        this.today=getTodayInMills();
        Log.d("thread", String.valueOf(Looper.myLooper() == Looper.getMainLooper()));
        //ottengo riferimento al servizio SENSOR_SERVICE
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra("UID") && intent.hasExtra("myDiscountsUID")){
            this.UID=intent.getStringExtra("UID");
            this.myDiscounts=intent.getStringArrayListExtra("myDiscountsUID");
            Log.d("dis",myDiscounts.get(0));
            //function
            makeNotificationIntent();
            //crea un thread separato e fa partire il contapassi
            new Thread(this).start();
        }
        return START_NOT_STICKY;
    }
    @Override
    public void run() {
        Log.d("started","started");
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
                float x=event.values[0];
                float y=event.values[1];
                float z=event.values[2];
                double magnitude = Math.sqrt(x*x+y*y+z*z);
                double magnitudeDelta=magnitude-magitudePrevious;
                magitudePrevious=magnitude;
                if(magnitudeDelta>6){
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
        Log.d("thread", String.valueOf(Looper.myLooper() == Looper.getMainLooper()));
    }
    private void makeNotificationIntent(){
        Intent notificationIntent =new Intent(this,UserView.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        Notification notification =new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("WalkToShop")
                .setContentIntent(pendingIntent)
                .build();
        //.setContentText("Registrazione in corso")
        ;//.setSmallIcon(R.id.drawable)
        startForeground(1,notification);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //spegnimento da background service
        Log.d("des","destroy");
        updateSharedPrefDiscounts(mySteps,myDiscounts);
        getUserWalkArray();
    }
    private void updateSharedPrefDiscounts(int mySteps,ArrayList<String> myDiscounts){
        Iterator it1= myDiscounts.iterator();
        while (it1.hasNext()){
            String uid=(String) it1.next();
            int oldSteps=getSharedPrefDiscountSteps(uid);
            if(oldSteps>-1){
                oldSteps+=mySteps;
                writeSharedPrefDiscountSteps(uid,oldSteps);
            }
        }
    }
    private int getSharedPrefDiscountSteps(String discountUID){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(discountUID, MODE_PRIVATE);
        if(prefs.contains("steps")){
            int value=prefs.getInt("steps", -1);
            return value;
        }else{
            return -1;
        }
    }
    private void writeSharedPrefDiscountSteps(String discountUID,int newSteps){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(discountUID, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("steps", newSteps);
        editor.apply();
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
    //query
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
                        checkIfNewWalk(walksArray);
                    }
                }
            }
        });
    }
    //verifica che quando effettuato l'ultima registrazione e quindi se sovrascrivere le informazioni o aggiungere in un altro oggetto da salvare
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
                        Log.d("diff",todayLong + " "+lastWalkDateLong);
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
                            Log.d("updatedsteps",updatedSteps+"");
                            String todayPlusUpdatedSteps=today + ","+updatedSteps;
                            walksArray.set(lastWalkIndex,todayPlusUpdatedSteps);
                        }else{
                            walksArray.add(todayPlusSteps);
                        }
                    }else{
                        Log.d("newWalk",todayPlusSteps);
                        walksArray.add(todayPlusSteps);
                    }
                    updateWalk(walksArray);
                }
            }
        });
    }
    private void updateWalk(ArrayList<String> walksArray){
        for(int i=0;i<walksArray.size();i++){
            Log.d("walksarray",walksArray.get(i));
        }
        db.collection("utente").document(UID).update("walk",walksArray).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setLastWalkDate();
            }
        });
    }
    private void setLastWalkDate(){
        db.collection("utente").document(UID).update("lastWalkDate",today).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //forse qui va lo spegnimento e l'unregistering fare una funzione
                /*
                if(eventListener!=null && mStepDetectorSensor!=null){
                    mSensorManager.unregisterListener(eventListener, mStepDetectorSensor);
                }else if(eventListener!=null && mStepDetectorSensor==null){
                    mSensorManager.unregisterListener(eventListener, accel);
                }*/
                mSensorManager.unregisterListener(eventListener, accel);
                new Thread(ServiceStepCounter.this).interrupt();
            }
        });
    }
    private Walk getWalkInfoFromString(String info){
        String[] todayAndSteps =info.split(",");
        Walk walk =new Walk();
        walk.setDate(todayAndSteps[0]);
        walk.setNumberOfSteps(todayAndSteps[1]);
        return walk;
    }
}