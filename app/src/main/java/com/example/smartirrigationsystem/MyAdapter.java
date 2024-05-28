package com.example.smartirrigationsystem;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartirrigationsystem.R;
import com.example.smartirrigationsystem.entity.IrrigationHistory;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<IrrigationHistory> historyList;

    public MyAdapter(List<IrrigationHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        IrrigationHistory history = historyList.get(position);
        holder.parentName.setText(history.getParentName());
        holder.startTime.setText(history.getStartTime());
        holder.status.setText(history.getStatus());
        holder.stopTime.setText(history.getStopTime());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView parentName, startTime, status, stopTime;

        public MyViewHolder(View view) {
            super(view);
            parentName = view.findViewById(R.id.parentName);
            startTime = view.findViewById(R.id.departTime);
            status = view.findViewById(R.id.status);
            stopTime = view.findViewById(R.id.endTime);
        }
    }
}
