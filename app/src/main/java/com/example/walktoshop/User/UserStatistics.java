package com.example.walktoshop.User;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.walktoshop.NetworkController.NetworkController;
import com.example.walktoshop.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class UserStatistics extends AppCompatActivity {
    FirebaseFirestore db =FirebaseFirestore.getInstance();
    String UID;
    BarChart barChart;
    double latitude;
    double longitude;
    String city;
    BottomNavigationView bottomNavigationView;
    private ArrayList<BarEntry> dailySteps=new ArrayList<>();
    private ArrayList<BarEntry> dailyKm=new ArrayList<>();
    private ArrayList<BarEntry> dailyKcal= new ArrayList<>();
    private ArrayList<String> days = new ArrayList<>();
    private float stepsAverage = 0;
    private float kcalAverage = 0;
    private float kmAverage = 0;
    private TextView stepsAverageText;
    private TextView kcalAverageText;
    private TextView kilometersAverageText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_statistics);
        Intent intent = getIntent();
        if (intent.hasExtra("UID") && intent.hasExtra("city") && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            UID = intent.getStringExtra("UID");
            city = intent.getStringExtra("city");
            latitude = intent.getDoubleExtra("latitude", 0.0f);
            longitude = intent.getDoubleExtra("longitude", 0.0f);
        }
        barChart = findViewById(R.id.barChart);
        stepsAverageText =findViewById(R.id.stepsAvg);
        kcalAverageText= findViewById(R.id.kcalAvg);
        kilometersAverageText=findViewById(R.id.kilometersAvg);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        goHome();
                        break;
                    case R.id.action_map:
                        goToUserViewMap();
                        break;
                    case R.id.action_notification:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(UserStatistics.this)){
            networkController.connectionDialog(UserStatistics.this);
        }
        Log.d("connection state",networkController.isConnected(UserStatistics.this)+"");
        getDailyWalk();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killServiceIfRunning();
    }
    private void killServiceIfRunning(){
        if(isMyServiceRunning(StepCounter.class) == true){
            Intent intent =new Intent(this,StepCounter.class);
            Toast.makeText(this,"Contapassi disattivato",Toast.LENGTH_SHORT).show();
            stopService(intent);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void getDailyWalk(){
        db.collection("utente").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document= task.getResult();
                    ArrayList<String> stringedWalks= (ArrayList<String>) document.get("walk");
                    int userHeight= Integer.parseInt(document.getString("height"));
                    int userWeight= Integer.parseInt(document.getString("weight"));
                    if(stringedWalks!=null){
                        int numberOfUserWalks=0;
                        int totalNumberOfUserWalks= stringedWalks.size()-1;
                        for (int i=0;i<6;i++){
                            if(totalNumberOfUserWalks-i>=0){
                                String walkInfo=stringedWalks.get(totalNumberOfUserWalks-i);
                                Walk walk= getWalkInfoFromString(walkInfo);
                                UserStatistics.this.days.add(millisecondsToDate(walk.getDate()));
                                long steps= Long.parseLong((walk.getNumberOfSteps()));
                                float meters= calculateKilometers(userHeight,steps)*1000;//trasformare in metri per il grafico
                                int kcal= calculateKcal(userWeight,steps);
                                Log.d("index",days.get(i)+" "+steps);
                                stepsAverage= stepsAverage + steps;
                                kcalAverage= kcalAverage + kcal;
                                kmAverage =kmAverage + meters;
                                UserStatistics.this.dailySteps.add(new BarEntry(i,steps));
                                UserStatistics.this.dailyKm.add(new BarEntry(i,meters));
                                UserStatistics.this.dailyKcal.add(new BarEntry(i,kcal));
                                numberOfUserWalks++;
                            }
                        }
                        stepsAverage=Math.round((float) stepsAverage/numberOfUserWalks);
                        kcalAverage=Math.round((float) kcalAverage/numberOfUserWalks);
                        kmAverage=(float) (kmAverage/numberOfUserWalks)/1000;//riporto in km
                        String[] daysArray = new String[UserStatistics.this.days.size()];
                        daysArray = UserStatistics.this.days.toArray(daysArray);
                        setBarChart(dailyKm,dailyKcal,dailySteps,daysArray);
                        //set medie
                        stepsAverageText.setText("Media Passi:"+(int) stepsAverage);
                        kcalAverageText.setText("Media calorie:"+(int) kcalAverage);
                        kilometersAverageText.setText("Media kilometri:"+(int) kmAverage);

                        Log.d("average","walks"+numberOfUserWalks+"steps"+ stepsAverage +"kcal"+kcalAverage+"km"+kmAverage);

                    }
                }
            }
        });
    }

    private void goHome() {
        final Intent intent = new Intent(this, UserView.class);
        User user = new User();
        intent.putExtra("UID", UID);
        startActivity(intent);
    }

    private void goToUserViewMap() {
        final Intent intent = new Intent(UserStatistics.this, UserMapView.class);
        intent.putExtra("UID", UID);
        intent.putExtra("city",city);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        startActivity(intent);
    }
    private void setBarChart(ArrayList<BarEntry> dailyKm,ArrayList<BarEntry> dailyKcal,ArrayList<BarEntry> dailySteps, String[] days){
        BarDataSet steps = new BarDataSet(dailySteps,"Passi giornalieri");
        steps.setColor(Color.RED);
        BarDataSet kcal = new BarDataSet(dailyKcal,"Kcal giornaliere");
        kcal.setColor(Color.GREEN);
        BarDataSet km = new BarDataSet(dailyKm,"Metri giornalieri");
        km.setColor(Color.MAGENTA);

        BarData data = new BarData(steps,kcal,km);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);

        barChart.setDragEnabled(true);
        barChart.setVisibleXRangeMaximum(1);

        float barSpace = 0.12f;
        float groupSpace = 0.16f;
        data.setBarWidth(0.16f);

        barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setAxisMaximum(0+barChart.getBarData().getGroupWidth(groupSpace, barSpace)*7);
        barChart.getAxisLeft().setAxisMinimum(0);

        barChart.groupBars(0, groupSpace, barSpace);
        barChart.invalidate();
    }
    private Walk getWalkInfoFromString(String info){
        String[] todayAndSteps =info.split(",");
        Walk walk =new Walk();
        walk.setDate(todayAndSteps[0]);
        walk.setNumberOfSteps(todayAndSteps[1]);
        return walk;
    }
    private float calculateKilometers(int height,long steps){
        float meters;
        if(height<170){
            meters=Math.round((float)600*steps/1000);
        }else{
            meters=Math.round((float)700*steps/1000);
        }
        float kilometers=meters/1000;
        Log.d("km",kilometers+"");
        return kilometers;
    }
    private int calculateKcal(int weight,long steps){
        int kcal;
        Log.d("weight",weight+"");
        kcal= (int) Math.round((float)weight*0.0005*steps);
        Log.d("kcal",kcal+"");
        return kcal;
    }
    private String millisecondsToDate(String milliseconds){
        if(milliseconds!=null){
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            long longMilliSeconds= Long.parseLong(milliseconds);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(longMilliSeconds);
            return formatter.format(calendar.getTime());
        }
        return "";
    }
    //check

}
