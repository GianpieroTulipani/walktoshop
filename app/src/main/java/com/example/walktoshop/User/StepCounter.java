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
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class StepCounter extends Service implements Runnable{
    //definisco manager sensori, counter e detector
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;
    private Button start;
    private Button finish;
    private Runnable worker;
    private SensorEventListener eventListener;
    private String UID=null;
    private static final String CHANNEL_ID="StepCounter_notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Log.d("thread", String.valueOf(Looper.myLooper() == Looper.getMainLooper()));
        //ottengo riferimento al servizio SENSOR_SERVICE
        //spegnimento da background service
        /*
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eventListener!=null){
                    Log.d("ok","ok");
                    mSensorManager.unregisterListener(eventListener, mStepCounterSensor);
                    mSensorManager.unregisterListener(eventListener, mStepDetectorSensor);
                }
                new Thread(worker).interrupt();
            }
        });*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra("UID")){
            this.UID=intent.getStringExtra("UID");
                //function
                Log.d("d",this.UID);
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

                if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    Log.d("Step Counter Detected"," "+ value);
                } /*else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    // For test only. Only allowed value is 1.0 i.e. for step taken
                    Log.d("Step Detector Detected"," " + value);
                }*/
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };
        mSensorManager.registerListener( eventListener, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(eventListener, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
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
        Log.d("des","destroy");
        if(eventListener!=null){
            mSensorManager.unregisterListener(eventListener, mStepCounterSensor);
            mSensorManager.unregisterListener(eventListener, mStepDetectorSensor);
        }
        new Thread(worker).interrupt();
    }
}