package com.github.pocmo.sensordashboard.activities;

import android.app.ListActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.SensorAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by neerajpaliwal on 04/05/16.
 */
public class SensorsListActivity extends ListActivity {
    private SensorManager mSensorManager;
    private SensorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_list);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        ArrayList<Sensor> deviceSensorsArr = new ArrayList<>(deviceSensors);

        adapter = new SensorAdapter(getApplicationContext(), deviceSensorsArr);
        getListView().setAdapter(adapter);
    }
}
