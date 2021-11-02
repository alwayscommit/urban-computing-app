package com.example.urbancomputingapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView temperatureText;
    private TextView pressureText;
    private TextView brightnessText;
    private SensorManager sensorManager;
    private List<Sensor> mySensors;
    //temperature - 33172002, 33172003,
    //brightness - 65541
    //ambient light - 5
    //baro - 6
    private final List<Float> temperatureList = new ArrayList<Float>();
    private final List<Float> brightnessList = new ArrayList<Float>();
    private final List<Float> pressureList = new ArrayList<Float>();
    private Button saveTemperature;
    private Button savePressure;
    private Button saveBrightness;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temperatureText = findViewById(R.id.temperatureText);
        pressureText = findViewById(R.id.pressureText);
        brightnessText = findViewById(R.id.brightnessText);
        saveTemperature = findViewById(R.id.saveTemperature);
        saveTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeTemperatureToCSV();
            }
        });

        saveBrightness = findViewById(R.id.saveBrightness);
        saveBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeBrightnessToCSV();
            }
        });

        savePressure = findViewById(R.id.savePressure);
        savePressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writePressureToCSV();
            }
        });
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mySensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;
        if (sensor.getType() == 33172003) {
            temperatureText.setText(String.valueOf(event.values[0]));
            temperatureList.add(event.values[0]);
        } else if (sensor.getType() == 6) {
            pressureText.setText(String.valueOf(event.values[0]));
            pressureList.add(event.values[0]);
        } else if (sensor.getType() == 5) {
            brightnessText.setText(String.valueOf(event.values[0]));
            brightnessList.add(event.values[0]);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(33172003), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(6), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(5), SensorManager.SENSOR_DELAY_FASTEST);
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

    /*private void printSpecificSensor(int typeAmbientTemperature) {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            textView.setText("This device has a temperature sensor");
        } else {
            textView.setText(sensorManager.getDefaultSensor(33172002).toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void printSensorData() {
        for (Sensor sensor : mySensors) {
            textView.setText(textView.getText() + "\n" + sensor.getName() + sensor.getType());
        }
    }*/
}