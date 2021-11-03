package com.example.urbancomputingapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.urbancomputingapp.dao.FirebaseDAO;
import com.example.urbancomputingapp.model.LocationData;
import com.example.urbancomputingapp.model.SensorData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //temperature - 33172002, 33172003,
    //brightness - 65541
    //ambient light - 5
    //baro - 6
    private final List<Float> temperatureList = new ArrayList<Float>();
    private final List<Float> brightnessList = new ArrayList<Float>();
    private final List<Float> pressureList = new ArrayList<Float>();
    private TextView temperatureText;
    private TextView pressureText;
    private TextView brightnessText;
    private SensorManager sensorManager;
    private List<Sensor> mySensors;
    private Button saveTemperature;
    private Button savePressure;
    private Button saveBrightness;
    private Button locationButton;
    private TextView locationText;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Calendar c1 = null;
    private Calendar c2 = null;
    private int startTime = 0;
    private int endTime = 0;
    private FirebaseDAO firebaseDAO = new FirebaseDAO();
    private static final String CONSUMER_KEY = BuildConfig.CONSUMER_KEY;
    private static final Integer INTERVAL = BuildConfig.INTERVAL;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        c1 = Calendar.getInstance();
        startTime = c1.get(Calendar);
        System.out.println("Start Time 1 :: " + startTime);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temperatureText = findViewById(R.id.temperatureText);
        pressureText = findViewById(R.id.pressureText);
        brightnessText = findViewById(R.id.brightnessText);
        saveTemperature = findViewById(R.id.saveTemperature);
        saveTemperature.setOnClickListener(v -> writeTemperatureToCSV());
        locationButton = findViewById(R.id.locationButton);
        locationText = findViewById(R.id.locationText);
        saveBrightness = findViewById(R.id.saveBrightness);
        saveBrightness.setOnClickListener(v -> writeBrightnessToCSV());

        savePressure = findViewById(R.id.savePressure);
        savePressure.setOnClickListener(v -> writePressureToCSV());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mySensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                } else {
                    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                        Location location = task.getResult();
                        if (location != null) {
                            getLocationData(location.getLatitude(), location.getLongitude());
//                          locationText.setText("Latitude: " + location.getLatitude() + "Longitude: " + location.getLongitude());
                        }
                    });
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getLocationData(Double latitude, Double longitude) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.openweathermap.org/data/2.5/weather?units=metric&lat="+latitude+"&lon="+longitude+"&appid="+CONSUMER_KEY;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    LocationData locationData = new LocationData();
                    locationData.setDateTime(LocalDateTime.now().toString());
                    locationData.setLocationData(response);
                    firebaseDAO.recordLocationData(locationData);
                    locationText.setText("Response is: "+ response.toString());
                }, error -> locationText.setText("That didn't work!"));
        queue.add(stringRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        //phone temp
        if (sensor.getType() == 33172003) {
            temperatureText.setText(String.valueOf(event.values[0]));
            temperatureList.add(event.values[0]);
            if (createEntry()) {
                recordSensorData();
            }
            //ambient light
        } else if (sensor.getType() == INTERVAL) {
            brightnessText.setText(String.valueOf(event.values[0]));
            brightnessList.add(event.values[0]);
            if (createEntry()) {
                recordSensorData();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void recordSensorData() {
        SensorData sensorData = new SensorData();
        sensorData.setDateTime(LocalDateTime.now().toString());
        sensorData.setTemperature(temperatureText.getText().toString());
        sensorData.setAmbientLight(brightnessText.getText().toString());
        firebaseDAO.recordSensorData(sensorData);
    }

    private Boolean createEntry() {
        c2 = Calendar.getInstance();
        endTime = c2.get(Calendar.SECOND);
        System.out.println("Start time: " + startTime);
        System.out.println("End time: " + endTime);
        int secondsElapsed = endTime - startTime;
        System.out.println(secondsElapsed);
        if (secondsElapsed == 5) {
            c1 = Calendar.getInstance();
            startTime = c1.get(Calendar.SECOND);
            System.out.println("New start time::" + startTime);
            endTime = 0;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(33172003), 30000000);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(6), 30000000);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(5), 30000000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void writeTemperatureToCSV() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/comma-separated-values");
        intent.putExtra(Intent.EXTRA_TITLE, "Temperature.csv");
        startActivityForResult(intent, 1);
    }

    private void writePressureToCSV() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/comma-separated-values");
        intent.putExtra(Intent.EXTRA_TITLE, "Pressure.csv");
        startActivityForResult(intent, 2);
    }

    private void writeBrightnessToCSV() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/comma-separated-values");
        intent.putExtra(Intent.EXTRA_TITLE, "Brightness.csv");
        startActivityForResult(intent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    os.write("Temperature,".getBytes());
                    os.write(System.getProperty("line.separator").getBytes());
                    for (Float temp : temperatureList) {
                        os.write((temp.toString() + ",").getBytes());
                        os.write(System.getProperty("line.separator").getBytes());
                    }
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    os.write("Pressure,".getBytes());
                    os.write(System.getProperty("line.separator").getBytes());
                    for (Float temp : pressureList) {
                        os.write((temp.toString() + ",").getBytes());
                        os.write(System.getProperty("line.separator").getBytes());
                    }
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    os.write("Brightness,".getBytes());
                    os.write(System.getProperty("line.separator").getBytes());
                    for (Float temp : brightnessList) {
                        os.write((temp.toString() + ",").getBytes());
                        os.write(System.getProperty("line.separator").getBytes());
                    }
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}