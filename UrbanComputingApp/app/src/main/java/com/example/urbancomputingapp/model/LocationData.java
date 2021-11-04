package com.example.urbancomputingapp.model;

public class LocationData {

    private String dateTime;
    private String locationTemperature;
    private String locationHumidity;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocationTemperature() {
        return locationTemperature;
    }

    public void setLocationTemperature(String locationTemperature) {
        this.locationTemperature = locationTemperature;
    }

    public String getLocationHumidity() {
        return locationHumidity;
    }

    public void setLocationHumidity(String locationHumidity) {
        this.locationHumidity = locationHumidity;
    }
}
