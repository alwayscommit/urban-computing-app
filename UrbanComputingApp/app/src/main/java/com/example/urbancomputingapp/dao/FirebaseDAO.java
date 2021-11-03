package com.example.urbancomputingapp.dao;

import com.example.urbancomputingapp.model.LocationData;
import com.example.urbancomputingapp.model.SensorData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDAO {

    private final DatabaseReference sensorDataReference;
    private final DatabaseReference locationDataReference;

    public FirebaseDAO() {
        FirebaseDatabase firebaseDb = FirebaseDatabase.getInstance();
        sensorDataReference = firebaseDb.getReference(SensorData.class.getSimpleName());
        locationDataReference = firebaseDb.getReference(LocationData.class.getSimpleName());
    }

    public Task<Void> recordSensorData(SensorData sensorData) {
        return sensorDataReference.push().setValue(sensorData);
    }

    public Task<Void> recordLocationData(LocationData locationData) {
        return locationDataReference.push().setValue(locationData);
    }


}
