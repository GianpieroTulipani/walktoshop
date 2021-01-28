package com.example.walktoshop.User;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.walktoshop.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class UserStatistics extends AppCompatActivity {
    String UID;
    BarChart barChart;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_statistics);
        barChart = findViewById(R.id.barChart);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        BarDataSet passiGiornalieri = new BarDataSet(passiGiornalieri(),"Passi giornalieri");
        passiGiornalieri.setColor(Color.RED);
        BarDataSet kcalGiornaliere = new BarDataSet(kcalGiornaliere(),"Kcal giornaliere");
        kcalGiornaliere.setColor(Color.GREEN);
        BarDataSet kmGiornalieri = new BarDataSet(kmGiornalieri(),"Km giornalieri");
        kmGiornalieri.setColor(Color.MAGENTA);

        BarData data = new BarData(passiGiornalieri,kcalGiornaliere,kmGiornalieri);
        barChart.setData(data);

        String[] days = new String[]{"Domenica","lunedi","martedi","mercoledi","giovedi","venerdi","sabato"};
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

        Intent intent = getIntent();
        UID = intent.getStringExtra("UID");

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

    private ArrayList<BarEntry> passiGiornalieri(){
        ArrayList<BarEntry> passiGiornalieri = new ArrayList<BarEntry>();
        passiGiornalieri.add( new BarEntry(1,800));
        passiGiornalieri.add( new BarEntry(2,1000));
        passiGiornalieri.add( new BarEntry(3,800));
        passiGiornalieri.add( new BarEntry(4,850));
        passiGiornalieri.add( new BarEntry(5,1600));
        passiGiornalieri.add( new BarEntry(6,1000));
        passiGiornalieri.add( new BarEntry(7,950));
        return passiGiornalieri;
    }
    private ArrayList<BarEntry> kcalGiornaliere(){
        ArrayList<BarEntry> kcalGiornaliere = new ArrayList<BarEntry>();
        kcalGiornaliere.add( new BarEntry(1,500));
        kcalGiornaliere.add( new BarEntry(2,1800));
        kcalGiornaliere.add( new BarEntry(3,200));
        kcalGiornaliere.add( new BarEntry(4,300));
        kcalGiornaliere.add( new BarEntry(5,3000));
        kcalGiornaliere.add( new BarEntry(6,1000));
        kcalGiornaliere.add( new BarEntry(7,450));
        return kcalGiornaliere;
    }
    private ArrayList<BarEntry> kmGiornalieri(){
        ArrayList<BarEntry> kmGiornalieri = new ArrayList<BarEntry>();
        kmGiornalieri.add( new BarEntry(1,5));
        kmGiornalieri.add( new BarEntry(2,20));
        kmGiornalieri.add( new BarEntry(3,2));
        kmGiornalieri.add( new BarEntry(4,4));
        kmGiornalieri.add( new BarEntry(5,40));
        kmGiornalieri.add( new BarEntry(6,12));
        kmGiornalieri.add( new BarEntry(7,3));
        return kmGiornalieri;
    }

    private void goHome() {
        final Intent intent = new Intent(this, UserView.class);
        User user = new User();
        intent.putExtra("UID", UID);
        startActivity(intent);
    }

    private void goToUserViewMap() {
        final Intent intent = new Intent(UserStatistics.this, UserMapView.class);
        intent.putExtra("UID", this.UID);
        startActivity(intent);
    }
}
