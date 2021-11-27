package com.example.urbancomputingapp.model;

public class LocationData {

    private String userId;
    private String dateTime;
    private Double locationTemperature;
    private Double locationHumidity;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocationData(){

    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Double getLocationTemperature() {
        return locationTemperature;
    }

    public void setLocationTemperature(Double locationTemperature) {
        this.locationTemperature = locationTemperature;
    }

    public Double getLocationHumidity() {
        return locationHumidity;
    }

    public void setLocationHumidity(Double locationHumidity) {
        this.locationHumidity = locationHumidity;
    }
}
