package com.example.smartirrigationsystem.fragment;

import com.example.smartirrigationsystem.entity.PredictionRequest;
import com.example.smartirrigationsystem.entity.PredictionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import okhttp3.ResponseBody;

public interface ApiService {
    @POST("/prediction")
    Call<ResponseBody> getPrediction(@Body PredictionRequest request);
}

