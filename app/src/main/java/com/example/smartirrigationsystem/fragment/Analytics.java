package com.example.smartirrigationsystem.fragment;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.smartirrigationsystem.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class Analytics extends Fragment {

    private LineChart lineChart;
    private LineData lineData;
    private LineDataSet lineDataSet;
    private List<Entry> entries = new ArrayList<>();
    private DatabaseReference databaseReference;

    public Analytics() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);
        lineChart = view.findViewById(R.id.lineChart);

        // Setup chart
        setupChart();

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("7LRp34SMCKM4G0clECvtREBSnag2").child("dataPlant");

        // Retrieve data from Firebase
        retrieveDataFromFirebase();

        return view;
    }

    private void setupChart() {
        lineDataSet = new LineDataSet(entries, "Soil Humidity");
        lineDataSet.setColor(ColorTemplate.getHoloBlue());
        lineDataSet.setLineWidth(2f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setCubicIntensity(0.2f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(ColorTemplate.getHoloBlue());
        lineDataSet.setFillAlpha(100);
        lineDataSet.setDrawValues(false);

        lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
    }

    private void retrieveDataFromFirebase() {
        databaseReference.limitToLast(100).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entries.clear();
                int index = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Float soilHumidity = snapshot.child("soilHumidity").getValue(Float.class);
                    if (soilHumidity != null) {
                        entries.add(new Entry(index++, soilHumidity));
                    }
                }
                lineDataSet.notifyDataSetChanged();
                lineData.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
}
