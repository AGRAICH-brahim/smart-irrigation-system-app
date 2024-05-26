package com.example.smartirrigationsystem.entity;

import com.google.gson.annotations.SerializedName;

public class PredictionRequest {
    @SerializedName("temperature")
    private double temperature;

    @SerializedName("pressure")
    private double pressure;

    @SerializedName("soilmiosture") // Correction de l'orthographe
    private double soilmiosture;

    public PredictionRequest(double temperature, double pressure, double soilmiosture) {
        this.temperature = temperature;
        this.pressure = pressure;
        this.soilmiosture = soilmiosture;
    }

    // Getters and setters if needed
    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getSoilmiosture() {
        return soilmiosture;
    }

    public void setSoilmiosture(double soilmiosture) {
        this.soilmiosture = soilmiosture;
    }
}
