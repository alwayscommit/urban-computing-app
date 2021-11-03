package com.example.urbancomputingapp.model;

import java.time.LocalDateTime;

public class SensorData {

    private String dateTime;
    private String temperature;
    private String ambientLight;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(String ambientLight) {
        this.ambientLight = ambientLight;
    }
}
