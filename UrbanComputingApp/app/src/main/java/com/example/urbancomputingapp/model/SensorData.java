package com.example.urbancomputingapp.model;

public class SensorData {

    private String userId;
    private String dateTime;
    private Double temperature;
    private Double ambientLight;
    private Integer humidity;

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setAmbientLight(Double ambientLight) {
        this.ambientLight = ambientLight;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getAmbientLight() {
        return ambientLight;
    }
}
