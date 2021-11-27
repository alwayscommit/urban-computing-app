package com.example.urbancomputingapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.urbancomputingapp.dao.FirebaseDAO;
import com.example.urbancomputingapp.model.LocationData;
import com.example.urbancomputingapp.model.SensorData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
temperature - 33172002, 33172003,
brightness - 65541
ambient light - 5*/
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String CONSUMER_KEY = BuildConfig.CONSUMER_KEY;
    private final List<Float> temperatureList = new ArrayList<Float>();
    private final List<Float> brightnessList = new ArrayList<Float>();
    private final FirebaseDAO firebaseDAO = new FirebaseDAO();
    private final Handler handler = new Handler();
    private final int delay = 5000;
    private Runnable runnable;
    private TextView temperatureText;
    private TextView brightnessText;
    private TextView humidityText;
    private TextView temperatureSample;
    private TextView brightnessSample;
    private TextView humiditySample;
    private TextView locationHumiditySample;
    private TextView locationTempSample;
    private TextView lightResult;
    private TextView humidityResult;
    private TextView temperatureResult;
    private SensorManager sensorManager;
    private Button saveTemperature;
    private Button saveBrightness;
    private Button senseButton;
    private TextView locationTemperature;
    private TextView locationHumidity;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String deviceId;

    private Spinner plantSpinnner;
    private Map<String, String> plantNames;
    private DatabaseReference dbRef;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        plantSpinnner = findViewById(R.id.plantSpinnner);

        temperatureText = findViewById(R.id.temperatureText);
        brightnessText = findViewById(R.id.brightnessText);
        humidityText = findViewById(R.id.humidityText);

        temperatureSample = findViewById(R.id.temperatureSample);
        brightnessSample = findViewById(R.id.brightnessSample);
        humiditySample = findViewById(R.id.humiditySample);
        locationHumiditySample = findViewById(R.id.locationHumiditySample);
        locationTempSample = findViewById(R.id.locationTempSample);

        lightResult = findViewById(R.id.lightResult);
        humidityResult = findViewById(R.id.humidityResult);
        temperatureResult = findViewById(R.id.temperatureResult);
        saveTemperature = findViewById(R.id.saveTemperature);
        saveTemperature.setOnClickListener(v -> writeTemperatureToCSV());
        senseButton = findViewById(R.id.senseButton);
        locationTemperature = findViewById(R.id.locationTemperature);
        locationHumidity = findViewById(R.id.locationHumidity);
        saveBrightness = findViewById(R.id.saveBrightness);
        saveBrightness.setOnClickListener(v -> writeBrightnessToCSV());

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        senseButton.setOnClickListener(v -> invokeCloudFunction());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("plant_collection").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                plantNames = new HashMap<>();
                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    String plantName = childSnap.child("name").getValue(String.class);
                    plantNames.put(plantName, childSnap.getKey());
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, new ArrayList<>(plantNames.keySet()));
                arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                plantSpinnner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void invokeCloudFunction() {
        senseLocationData();
        String selectedPlant = plantSpinnner.getSelectedItem().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", deviceId);
        data.put("plantId", plantNames.get(selectedPlant));
        FirebaseFunctions functions = FirebaseFunctions.getInstance();
        functions.useEmulator("10.0.2.2", 5001);
        functions.getHttpsCallable("checkPlantSuitability").call(data)
                .continueWith(task -> {
                    Map<String, String> result = (Map<String,String>) task.getResult().getData();
                    return result;
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(task.getResult().get("lightResult").equals("true")){
                            lightResult.setTextColor(Color.parseColor("#64f595"));
                        } else {
                            lightResult.setTextColor(Color.parseColor("#f56942"));
                        }
                        lightResult.setText(task.getResult().get("lightText"));
                        if(task.getResult().get("humidityResult").equals("true")){
                            humidityResult.setTextColor(Color.parseColor("#64f595"));
                        } else {
                            humidityResult.setTextColor(Color.parseColor("#f56942"));
                        }
                        humidityResult.setText(task.getResult().get("humidityText"));
                        if(task.getResult().get("tempResult").equals("true")){
                            temperatureResult.setTextColor(Color.parseColor("#64f595"));
                        } else {
                            temperatureResult.setTextColor(Color.parseColor("#f56942"));
                        }
                        temperatureResult.setText(task.getResult().get("tempText"));
                    } else {
                        lightResult.setText("Server is unable to analyze plant suitability at the moment.");
                        Exception e = task.getException();
                        if (e instanceof FirebaseFunctionsException) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void senseLocationData() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        } else {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getLocationData(location.getLatitude(), location.getLongitude());
                    }
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getLocationData(Double latitude, Double longitude) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.openweathermap.org/data/2.5/weather?units=metric&lat=" + latitude + "&lon=" + longitude + "&appid=" + CONSUMER_KEY;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        LocationData locationData = parseJSON(response);
                        firebaseDAO.recordLocationData(locationData);
                        locationHumidity.setText(locationData.getLocationHumidity().toString());
                        locationHumiditySample.setText(locationData.getLocationHumidity().toString());
                        locationTemperature.setText(locationData.getLocationTemperature().toString());
                        locationTempSample.setText(locationData.getLocationTemperature().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> locationTemperature.setText("That didn't work!"));
        queue.add(stringRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private LocationData parseJSON(String response) throws JSONException {
        LocationData locationData = new LocationData();
        JSONObject locationJSON = new JSONObject(response);
        JSONObject main = locationJSON.getJSONObject("main");
        Double temperature = main.getDouble("temp");
        Double humidity = main.getDouble("humidity");
        locationData.setUserId(deviceId);
        locationData.setLocationHumidity(humidity);
        locationData.setLocationTemperature(temperature);
        locationData.setDateTime(LocalDateTime.now().toString());
        return locationData;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == 33172003) {
            temperatureText.setText(String.valueOf(event.values[0]));
            temperatureList.add(event.values[0]);
        } else if (sensor.getType() == 5) {
            brightnessText.setText(String.valueOf(event.values[0]));
            if(isEmpty(brightnessText)){
                Double light = getRandomLight();
                brightnessSample.setText(String.valueOf(light.intValue()));
            } else {
                brightnessSample.setText(brightnessText.getText());
            }
            if(isEmpty(humidityText)){
                humiditySample.setText(getRandomHumidity().toString());
            } else {
                humiditySample.setText(humidityText.getText());
            }
            brightnessList.add(event.values[0]);
        }
        if(isEmpty(temperatureText)){
            Double temp = getRandomTemperature();
            temperatureSample.setText(String.valueOf(temp.intValue()));
        } else {
            temperatureSample.setText(temperatureText.getText());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void recordSensorData() {
        SensorData sensorData = new SensorData();
        sensorData.setUserId(deviceId);
        sensorData.setDateTime(LocalDateTime.now().toString());

        Double temperature = isEmpty(temperatureText) ? getRandomTemperature() : Double.parseDouble(temperatureText.getText().toString());
        Double ambientLight = isEmpty(brightnessText) ? getRandomLight() : Double.parseDouble(brightnessText.getText().toString());
        Integer humidity = isEmpty(humidityText) ? getRandomHumidity() : Integer.parseInt(humidityText.getText().toString());

        sensorData.setTemperature(temperature);
        sensorData.setAmbientLight(ambientLight);
        sensorData.setHumidity(humidity);
        firebaseDAO.recordSensorData(sensorData);
    }

    private Integer getRandomHumidity() {
        return new Random().nextInt((60 - 30) + 1) + 30;
    }

    private boolean isEmpty(TextView text) {
        return text.getText().equals("") || text.getText().equals("0.0");
    }

    private Double getRandomLight() {
        return (new Random().nextDouble() * (2500.0 - 100.0)) + 100.0;
    }

    private Double getRandomTemperature() {
        return (new Random().nextDouble() * (25.0 - 10.0)) + 10.0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        handler.postDelayed(runnable = () -> {
            handler.postDelayed(runnable, delay);
            recordSensorData();
        }, delay);
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(33172003), 3000000);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(5), 3000000);
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

    private void writeBrightnessToCSV() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/comma-separated-values");
        intent.putExtra(Intent.EXTRA_TITLE, "Brightness.csv");
        startActivityForResult(intent, 2);
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