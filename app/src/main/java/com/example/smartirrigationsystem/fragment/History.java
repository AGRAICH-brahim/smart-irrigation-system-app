package com.example.smartirrigationsystem.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartirrigationsystem.R;
import com.example.smartirrigationsystem.MyAdapter;
import com.example.smartirrigationsystem.entity.IrrigationHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class History extends Fragment {

    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private List<IrrigationHistory> historyList;
    private DatabaseReference databaseReference;
    private String userId = "7LRp34SMCKM4G0clECvtREBSnag2"; // Remplacez par l'ID utilisateur dynamique si nécessaire

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = rootView.findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        historyList = new ArrayList<>();
        myAdapter = new MyAdapter(historyList);
        recyclerView.setAdapter(myAdapter);

        // Initialiser la référence de Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("irrigationHistory");

        // Lire les données de Firebase et les ajouter à la liste
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                historyList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String parentName = snapshot.getKey();
                    IrrigationHistory history = snapshot.getValue(IrrigationHistory.class);
                    history.setParentName(parentName);
                    historyList.add(history);
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
        return rootView;
    }
}
