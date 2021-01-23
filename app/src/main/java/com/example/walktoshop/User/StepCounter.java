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
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class StepCounter extends Service implements Runnable{
    //definisco manager sensori, counter e detector
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    private String today=null;
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;
    private int mySteps=0;

    private SensorEventListener eventListener;
    private String UID=null;
    private static final String CHANNEL_ID="StepCounter_notification_channel";
    //accelerometer
    private Boolean stepDetectorAbsent=false;
    private double magitudePrevious=0;
    private Sensor accel;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Log.d("sensor","detector"+mStepDetectorSensor);
        if(mStepDetectorSensor==null){

            accel=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Log.d("sensor","accel"+accel);
        }
        this.today=getTodayInMills();
        Log.d("thread", String.valueOf(Looper.myLooper() == Looper.getMainLooper()));
        //ottengo riferimento al servizio SENSOR_SERVICE
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra("UID")){
            this.UID=intent.getStringExtra("UID");
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
                /*
                if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    Log.d("Step Counter Detected"," "+ value);
                } else*/
                //controllare da che versione c'è il TYPE STEP DETECTOR

                if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    StepCounter.this.mySteps++;
                    Log.d("Step Detector"," Detector:" + StepCounter.this.mySteps);
                }else if(mStepDetectorSensor==null){
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
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };
        if(mStepDetectorSensor==null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mSensorManager.registerListener(eventListener, accel,
                        SensorManager.SENSOR_DELAY_NORMAL);//
            }
        }else{
            mSensorManager.registerListener(eventListener, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
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
        getUserWalkArray();
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
                    String todayPlusSteps=today + ","+String.valueOf(StepCounter.this.mySteps);
                    if(lastWalkDate!=null){
                        //funzione per l'upload delle camminate
                        long lastWalkDateLong= Long.parseLong(lastWalkDate);
                        long todayLong= Long.parseLong(StepCounter.this.today);
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
                            int updatedSteps=StepCounter.this.mySteps+ Integer.parseInt(oldSteps);
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
                if(eventListener!=null){
                    mSensorManager.unregisterListener(eventListener, mStepCounterSensor);
                    mSensorManager.unregisterListener(eventListener, mStepDetectorSensor);
                }
                new Thread(StepCounter.this).interrupt();
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