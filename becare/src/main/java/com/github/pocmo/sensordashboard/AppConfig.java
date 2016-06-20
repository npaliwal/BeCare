package com.github.pocmo.sensordashboard;

import android.hardware.Sensor;


import com.github.pocmo.sensordashboard.model.AudioData;
import com.github.pocmo.sensordashboard.model.TwoImageInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by neerajpaliwal on 08/05/16.
 */
public class AppConfig {
    public static Map<Integer, String>    MANDATORY_SENSORS  = new HashMap<>();
    public static ArrayList<TwoImageInfo> CONTRAST_EXERCISES = new ArrayList<>();
    public static ArrayList<AudioData>    TRANSCRIPT_EXERCISES = new ArrayList<>();

    public static void initSensors(){
        if(MANDATORY_SENSORS != null && MANDATORY_SENSORS.size() > 0){
            return;
        }
        MANDATORY_SENSORS.put(Sensor.TYPE_GYROSCOPE,        "Gyroscope");
        MANDATORY_SENSORS.put(Sensor.TYPE_ACCELEROMETER,    "Accelerometer");
        MANDATORY_SENSORS.put(Sensor.TYPE_STEP_COUNTER,     "Step Counter");
    };


    public static void initContrastExercises(){
        if(CONTRAST_EXERCISES != null && CONTRAST_EXERCISES.size() > 0){
            return;
        }
        CONTRAST_EXERCISES.add(new TwoImageInfo("#FFffc1", "#AA0000"));
        CONTRAST_EXERCISES.add(new TwoImageInfo("#c2c2c2", "#d3d3d3"));
        CONTRAST_EXERCISES.add(new TwoImageInfo("#e1e1e1", "#c2c2c2"));
        CONTRAST_EXERCISES.add(new TwoImageInfo(R.drawable.becare, 3));
    }

    public static void initTranscriptExercises(){
        if(TRANSCRIPT_EXERCISES != null && TRANSCRIPT_EXERCISES.size() > 0){
            return;
        }
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.transcript_1, R.string.transcript_1));
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.transcript_1, R.string.transcript_1));
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.transcript_1, R.string.transcript_1));
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.transcript_1, R.string.transcript_1));

    }

    public static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    public static final int X_CORD = 1;
    public static final int Y_CORD = 2;
    public static final int Z_CORD = 3;

    public static int DEFAULT_NUM_CONTRAST = 3;
    public static int DEFAULT_NUM_TRANSCRIPT = 3;
}
