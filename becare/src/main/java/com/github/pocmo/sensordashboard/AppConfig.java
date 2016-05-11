package com.github.pocmo.sensordashboard;

import android.hardware.Sensor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by neerajpaliwal on 08/05/16.
 */
public class AppConfig {
    //Add any mandatory sensor here
    public static Map<Integer, String> MANDATORY_SENSORS = new HashMap<>();

    public static void init(){
        MANDATORY_SENSORS.put(Sensor.TYPE_GYROSCOPE,        "Gyroscope");
        MANDATORY_SENSORS.put(Sensor.TYPE_ACCELEROMETER,    "Accelerometer");
        MANDATORY_SENSORS.put(Sensor.TYPE_STEP_COUNTER,     "Step Counter");
    };

    public static final int CLIENT_CONNECTION_TIMEOUT = 15000;

}
