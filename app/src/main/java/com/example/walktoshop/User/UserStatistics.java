package com.example.walktoshop.User;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.example.walktoshop.Login_SignUp.LogIn;
import com.example.walktoshop.NetworkController.NetworkController;
import com.example.walktoshop.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS;

public class UserStatistics extends AppCompatActivity {
    FirebaseFirestore db =FirebaseFirestore.getInstance();
    String UID;
    BarChart barChart;
    BarChart kcalBarChart;
    BarChart kmBarChart;
    double latitude;
    double longitude;
    String city;
    BottomNavigationView bottomNavigationView;
    private ArrayList<BarEntry> dailySteps=new ArrayList<>();
    private ArrayList<BarEntry> dailyKm=new ArrayList<>();
    private ArrayList<BarEntry> dailyKcal= new ArrayList<BarEntry>();
    private ArrayList<String> days = new ArrayList<>();
    private float stepsAverage = 0;
    private float kcalAverage = 0;
    private float kmAverage = 0;
    private TextView stepsAverageText;
    private TextView kcalAverageText;
    private TextView report;
    private TextView report1;
    private TextView report2;
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
        kcalBarChart = findViewById(R.id.barChartKcal);
        kmBarChart = findViewById(R.id.barChartKm);
        report = findViewById(R.id.report);
        report1 = findViewById(R.id.report1);
        report2 = findViewById(R.id.report2);

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
                }
                return true;
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void OnItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_exit){
            logOut();
        }
    }

    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        final Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
        finish();
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
                        for (int i=0;i<7;i++){
                            if(totalNumberOfUserWalks-i>=0){
                                String walkInfo=stringedWalks.get(totalNumberOfUserWalks-i);
                                Walk walk= getWalkInfoFromString(walkInfo);
                                UserStatistics.this.days.add(millisecondsToDate(walk.getDate()));
                                long steps= Long.parseLong((walk.getNumberOfSteps()));
                                float meters= calculateKilometers(userHeight,steps)*1000;//trasformare in metri per il grafico
                                int kcal= calculateKcal(userWeight,steps);
                                Log.d("index",days.get(i)+" "+steps);

                                UserStatistics.this.dailySteps.add(new BarEntry(i,steps));
                                UserStatistics.this.dailyKm.add(new BarEntry(i,meters));
                                UserStatistics.this.dailyKcal.add(new BarEntry(i,kcal));
                                numberOfUserWalks++;
                            }

                        }
                        if(stringedWalks.size() == 1){
                            report.setText("prima rilevazione relativa ai passi giornalieri");
                            report1.setText("prima rilevazione relativa alle Kcal giornaliere");
                            report2.setText("prima rilevazione relativa ai Km giornalieri");
                        } else if(stringedWalks.size() <= 7){
                            report.setText("ultime "+stringedWalks.size()+" rilevazioni relative ai passi giornalieri");
                            report1.setText("ultime "+stringedWalks.size()+" rilevazioni relative alle Kcal giornaliere");
                            report2.setText("ultime "+stringedWalks.size()+" rilevazioni relative ai Km giornalieri");
                        } else if(stringedWalks.size() > 7){
                            report.setText("ultime 7 rilevazioni relative ai passi giornalieri");
                            report1.setText("ultime 7 rilevazioni relative alle Kcal giornaliere");
                            report2.setText("ultime 7 rilevazioni relative ai Km giornalieri");
                        }

                        String[] daysArray = new String[UserStatistics.this.days.size()];
                        daysArray = UserStatistics.this.days.toArray(daysArray);
                        setBarChart(dailyKm,dailyKcal,dailySteps,daysArray);
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
    private void setBarChart(ArrayList<BarEntry> dailyKm, ArrayList<BarEntry> dailyKcal, ArrayList<BarEntry> dailySteps, String[] days){

        BarDataSet steps = new BarDataSet(dailySteps,"Passi giornalieri");
        steps.setColors(MATERIAL_COLORS);
        steps.setValueTextColor(Color.BLACK);
        steps.setValueTextSize(12f);

        BarDataSet kcal = new BarDataSet(dailyKcal,"Kcal giornaliere");
        kcal.setColors(ColorTemplate.MATERIAL_COLORS);
        kcal.setValueTextColor(Color.BLACK);
        kcal.setValueTextSize(12f);

        BarDataSet km = new BarDataSet(dailyKm,"Metri giornalieri");
        km.setColors(ColorTemplate.MATERIAL_COLORS);
        km.setValueTextColor(Color.BLACK);
        km.setValueTextSize(12f);

        BarData barDataKcal = new BarData(kcal);
        kcalBarChart.setData(barDataKcal);
        kcalBarChart.animate();
        XAxis xAxisKcal = kcalBarChart.getXAxis();
        xAxisKcal.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxisKcal.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisKcal.setGranularity(1);
        xAxisKcal.setGranularityEnabled(true);
        kcalBarChart.setDragEnabled(true);
        kcalBarChart.setVisibleXRangeMaximum(3);
        kcalBarChart.invalidate();

        BarData data = new BarData(steps);
        barChart.setData(data);
        kcalBarChart.animate();
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setVisibleXRangeMaximum(3);
        barChart.invalidate();

        BarData barDataKm = new BarData(km);
        kmBarChart.setData(barDataKm);
        kmBarChart.animate();
        XAxis xAxisKm = kmBarChart.getXAxis();
        xAxisKm.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxisKm.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisKm.setGranularity(1);
        xAxisKm.setGranularityEnabled(true);
        kmBarChart.setDragEnabled(true);
        kmBarChart.setVisibleXRangeMaximum(3);
        kmBarChart.invalidate();
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
