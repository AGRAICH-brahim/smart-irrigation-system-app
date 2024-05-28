package com.example.smartirrigationsystem.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Irrigation extends Fragment {

    private Switch irrigationSwitch;
    private Switch pompeSwitch;
    private androidx.cardview.widget.CardView manualModeCard;
    private androidx.cardview.widget.CardView autoModeCard;
    private TextView autoModeApiResponseText;
    private TextView ManualModeApiResponseText;
    private DatabaseReference databaseReference;
    private String userId = "7LRp34SMCKM4G0clECvtREBSnag2"; // Replace with dynamic user ID
    private static final String BASE_URL = "https://d979-34-74-50-231.ngrok-free.app";
    private static final String TAG = "Irrigation";
    private int irrigationCount = 0;

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
        autoModeCard = rootView.findViewById(R.id.autoModeCard);
        autoModeApiResponseText = rootView.findViewById(R.id.autoModeApiResponseText);
        ManualModeApiResponseText = rootView.findViewById(R.id.ManualModeApiResponseText);

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
                        autoModeCard.setVisibility(View.VISIBLE); // Afficher la carte auto mode
                        fetchDataAndCallAPI();
                    } else if ("manual".equals(pumpControl)) {
                        irrigationSwitch.setChecked(false);
                        manualModeCard.setVisibility(View.VISIBLE);
                        autoModeCard.setVisibility(View.GONE); // Cacher la carte auto mode
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });

        // Lire le compteur d'irrigation depuis Firebase
        databaseReference.child("Users").child(userId).child("irrigationCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    irrigationCount = dataSnapshot.getValue(Integer.class);
                } else {
                    irrigationCount = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch irrigation count", Toast.LENGTH_SHORT).show();
            }
        });

        // Set the switch listener to show/hide the manual mode card
        irrigationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Auto mode
                manualModeCard.setVisibility(View.GONE);
                autoModeCard.setVisibility(View.VISIBLE); // Afficher la carte auto mode
                databaseReference.child("Users").child(userId).child("pumpControl").setValue("auto");

                // Fetch the latest data and call the API
                fetchDataAndCallAPI();
            } else {
                // Manual mode
                manualModeCard.setVisibility(View.VISIBLE);
                autoModeCard.setVisibility(View.GONE); // Cacher la carte auto mode
                databaseReference.child("Users").child(userId).child("pumpControl").setValue("manual");
            }
        });

        // Set the switch listener for manual pump control
        pompeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Turn on the pump and record the start time
                String startTime = getCurrentTime();
                irrigationCount++;
                Map<String, Object> irrigationData = new HashMap<>();
                irrigationData.put("startTime", startTime);
                irrigationData.put("status", "on");
                databaseReference.child("Users").child(userId).child("irrigationHistory").child("irrigation" + irrigationCount).setValue(irrigationData);
                databaseReference.child("Users").child(userId).child("pumpState").setValue("on");
                databaseReference.child("Users").child(userId).child("irrigationCount").setValue(irrigationCount);
            } else {
                // Turn off the pump and record the stop time
                String stopTime = getCurrentTime();
                databaseReference.child("Users").child(userId).child("irrigationHistory").child("irrigation" + irrigationCount).child("stopTime").setValue(stopTime);
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
                        String desciption = "";
                        String description = "";
                        Toast.makeText(getContext(), "Prediction: " + prediction, Toast.LENGTH_SHORT).show();
                        // Mettre à jour le TextView avec la réponse de l'API

                        switch (prediction) {
                            case "\"Very Dry\"":
                                desciption = "In a very dry soil condition, the ground is parched and cracked, lacking moisture entirely. Plants struggle to survive, showing signs of wilting and browning. The air is often hot and arid, exacerbating the dryness.";
                                break;
                            case "\"Dry\"":
                                desciption = "Dry soil has minimal moisture, causing it to feel crumbly and loose. While some hardy plants might persist, many show stress, with leaves curling and yellowing. The environment feels warm, with occasional dusty winds stirring the earth.";
                                break;
                            case "\"Wet\"":
                                desciption = "Wet soil is saturated with moisture, appearing dark and heavy. It supports robust plant growth, with vibrant green leaves and healthy roots. The air is humid, and the ground often feels spongy to the touch, retaining water well.";
                                break;
                            case "\"Very Wet\"":
                                desciption = "In very wet conditions, the soil is waterlogged, often forming puddles on the surface. Plants may suffer from root rot due to excessive moisture. The air is damp and cool, with a persistent sense of sogginess underfoot.";
                                break;
                            default:
                                desciption = "Unknown soil condition.";
                                break;
                        }

                        switch (prediction) {
                            case "\"Very Dry\"":
                                description = "Your soil is very dry , it's crucial to implement strategies to conserve moisture and protect plants from dehydration. Consider deep watering techniques and mulching to retain soil moisture. Additionally, providing shade and shelter can help mitigate the harsh effects of the hot, arid air.";
                                break;
                            case "\"Dry\"":
                                description = " When dealing with dry soil, prioritize water conservation practices and select drought-tolerant plant species. Implementing drip irrigation systems and using organic mulches can help maintain soil moisture levels. Be attentive to signs of plant stress, and consider providing temporary shade during particularly warm periods to alleviate environmental pressure.";
                                break;
                            case "\"Wet\"":
                                description = "In wet soil conditions, focus on promoting good drainage to prevent waterlogging and potential root rot. Incorporate organic matter into the soil to improve its structure and encourage better aeration. Select plants that thrive in moist environments and monitor for signs of nutrient leaching.";
                                break;
                            case "\"Very Wet\"":
                                description = "In areas with very wet soil, prioritize drainage solutions such as installing French drains or raised beds to prevent waterlogging. Choose plants that can tolerate excess moisture or consider container gardening to control water levels more effectively. Periodically aerate the soil to improve oxygenation and reduce the risk of root diseases caused by prolonged saturation.";
                                break;
                            default:
                                description = "Unknown soil condition.";
                                break;
                        }
                        System.out.println(prediction);
                        autoModeApiResponseText.setText(desciption);
                        ManualModeApiResponseText.setText(description);
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
                Toast.makeText(getContext(), "API Call Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
