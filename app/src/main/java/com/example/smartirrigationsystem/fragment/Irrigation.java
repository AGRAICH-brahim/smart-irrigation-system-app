package com.example.smartirrigationsystem.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.smartirrigationsystem.R;
import com.example.smartirrigationsystem.RetrofitClient;
import com.example.smartirrigationsystem.entity.PredictionRequest;
import com.example.smartirrigationsystem.entity.PredictionResponse;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Irrigation extends Fragment {

    private Switch irrigationSwitch;
    private Switch pompeSwitch;
    private androidx.cardview.widget.CardView manualModeCard;
    private DatabaseReference databaseReference;
    private String userId = "7LRp34SMCKM4G0clECvtREBSnag2"; // Replace with dynamic user ID
    private static final String BASE_URL = "https://d979-34-74-50-231.ngrok-free.app";
    private static final String TAG = "Irrigation";

    public Irrigation() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_irrigation, container, false);

        // Initialize UI elements
        irrigationSwitch = rootView.findViewById(R.id.irrigationSwitch);
        pompeSwitch = rootView.findViewById(R.id.pompeSwitch);
        manualModeCard = rootView.findViewById(R.id.manualModeCard);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Lire l'état de pumpControl depuis Firebase et définir l'état du Switch
        databaseReference.child("Users").child(userId).child("pumpControl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String pumpControl = dataSnapshot.getValue(String.class);
                    if ("auto".equals(pumpControl)) {
                        irrigationSwitch.setChecked(true);
                        manualModeCard.setVisibility(View.GONE);
                        fetchDataAndCallAPI();
                    } else if ("manual".equals(pumpControl)) {
                        irrigationSwitch.setChecked(false);
                        manualModeCard.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });

        // Set the switch listener to show/hide the manual mode card
        irrigationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Auto mode
                manualModeCard.setVisibility(View.GONE);
                databaseReference.child("Users").child(userId).child("pumpControl").setValue("auto");

                // Fetch the latest data and call the API
                fetchDataAndCallAPI();
            } else {
                // Manual mode
                manualModeCard.setVisibility(View.VISIBLE);
                databaseReference.child("Users").child(userId).child("pumpControl").setValue("manual");
            }
        });

        // Set the switch listener for manual pump control
        pompeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Turn on the pump
                databaseReference.child("Users").child(userId).child("pumpState").setValue("on");
            } else {
                // Turn off the pump
                databaseReference.child("Users").child(userId).child("pumpState").setValue("off");
            }
        });

        return rootView;
    }

    private void fetchDataAndCallAPI() {
        databaseReference.child("Users").child(userId).child("dataPlant").orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        float temperature = snapshot.child("temperature").getValue(Float.class);
                        int soilMoisture = snapshot.child("soilHumidity").getValue(Integer.class);
                        int pressure = 1013; // Valeur fixe de la pression

                        PredictionRequest request = new PredictionRequest(temperature, pressure, soilMoisture);

                        callPredictionAPI(request);
                    }
                } else {
                    Toast.makeText(getContext(), "No data available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callPredictionAPI(PredictionRequest request) {
        ApiService apiService = RetrofitClient.getClient(BASE_URL).create(ApiService.class);
        Call<ResponseBody> call = apiService.getPrediction(request);

        // Afficher les détails de la requête
        String jsonRequest = new Gson().toJson(request);
        Log.d(TAG, "Request: " + jsonRequest);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String prediction = response.body().string();
                        Toast.makeText(getContext(), "Prediction: " + prediction, Toast.LENGTH_SHORT).show();
                        // You can now use this prediction as needed
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read response body", e);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "API Response Failed: " + errorBody);
                        Toast.makeText(getContext(), "API Response Failed: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed", t);
                Toast.makeText(getContext(), "API Call Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
