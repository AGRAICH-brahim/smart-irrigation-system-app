package com.example.smartirrigationsystem.entity;
public class IrrigationHistory {
    private String parentName;
    private String startTime;
    private String status;
    private String stopTime;

    public IrrigationHistory() {
        // Constructeur par défaut requis pour Firebase
    }

    public IrrigationHistory(String parentName, String startTime, String status, String stopTime) {
        this.parentName = parentName;
        this.startTime = startTime;
        this.status = status;
        this.stopTime = stopTime;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }
}
