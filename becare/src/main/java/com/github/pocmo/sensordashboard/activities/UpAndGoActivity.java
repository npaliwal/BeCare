package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.SimpleStepDetector;
import com.github.pocmo.sensordashboard.StepListener;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by qtxdev on 7/5/2016.
 */
public class UpAndGoActivity extends Activity implements SensorEventListener, StepListener {
    private static final float THRESHOLD = (float)11.7;
    private static final int SAMPLE_SIZE = 5;
    private final String motionMsg = "Raising/Sitting";
    private final String walkingMsg = "Walking";
    private SimpleStepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;

    private int numSteps = 0;

    private TextView stepText;

    private TextView deltaText;

    private Button start;
    private Button  stop;

    Chronometer myChronometer;
    private boolean startMeasure = false;
    private float mAccelCurrent = 0;
    private float mAccelLast = 0;
    private long lastMotionTime = 0;
    private long  lastTime;
    private List<sensorData> sensorList = new ArrayList<>();
    private BecareRemoteSensorManager becareRemoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.up_and_go);
        //    TextView myView = (TextView) findViewById(R.layout.activity_pedometer);

        stepText = (TextView) findViewById(R.id.steps);

        deltaText = (TextView) findViewById(R.id.delta);
        myChronometer = (Chronometer)findViewById(R.id.chronometer);

        start = (Button)findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numSteps = 0;
                startMeasure = true;
                Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
                stepText.setText("0");
                lastTime = SystemClock.elapsedRealtime();
                myChronometer.setBase(SystemClock.elapsedRealtime());
                myChronometer.start();
            }
        });

        stop = (Button)findViewById(R.id.stop_button);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasure = false;
                Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
                myChronometer.stop();
            }
        });
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new SimpleStepDetector();
        simpleStepDetector.registerListener(this);
        becareRemoteSensorManager = BecareRemoteSensorManager.getInstance(UpAndGoActivity.this);

        lastMotionTime = -0xffffff;
    }

    @Override
    public void onResume() {
        super.onResume();
        numSteps = 0;

        stepText.setText("0");
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        startMeasure = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        startMeasure = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (!startMeasure)
                return;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float)Math.sqrt(x*x + y*y + z*z);

            sensorData data = new sensorData();
            data.x = x;
            data.y = y;
            data.z = z;
            data.timestamp = event.timestamp;
            data.accel = mAccelCurrent;

            if (sensorList.size() ==SAMPLE_SIZE)
                sensorList.remove(0);
            sensorList.add(data);
            boolean motionFound = true;
            String motion = "";
            if (sensorList.size() ==SAMPLE_SIZE){
                for (int i=0; i<SAMPLE_SIZE; i++){
                    if (sensorList.get(i).accel < THRESHOLD) {
                        motionFound = false;
                        break;
                    }
                    // motion += String.format("%.5f,", sensorList.get(i).accel);
                    motion = motionMsg;
                    lastMotionTime = System.currentTimeMillis();
                }
                //clear samples
                if (motionFound) {
                    sensorList.clear();
                }
            }
            else
                motionFound = false;

            if (motionFound == true) {
                int n = motion.indexOf(motionMsg);
                if (n < 0) {
                    motion += " ";
                    motion += motionMsg;
                }
                deltaText.setText(motion);

                long elapsedMillis = SystemClock.elapsedRealtime() - myChronometer.getBase();
                long dur = SystemClock.elapsedRealtime() - lastTime;
                lastTime = SystemClock.elapsedRealtime();
                String durStr = String.format("%d", dur);

                Hashtable dictionary = new Hashtable();
                dictionary.put("activityName", getString(R.string.up_and_go));
                dictionary.put("elapsed time", elapsedMillis);
                dictionary.put("dur (millsecond)", durStr);
                dictionary.put("motion", motionMsg);
                becareRemoteSensorManager.uploadWalkingActivityData(dictionary);

                return;
            }

           simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);

        }
    }

    public void getTimedWalking(View view) {
        Intent intent = new Intent(this, TimedWalkedActivity.class);
        startActivity(intent);

    }

    public void getSixMinutes(View view) {
        Intent intent = new Intent(this, SixMinutesActivity.class);
        startActivity(intent);

    }

    @Override
    public void step(long timeNs) {
        if (!startMeasure)
            return;

        numSteps++;
        String str =String.format("%d", numSteps);
        stepText.setText(str);

        String text =deltaText.getText().toString();
        int n = text.indexOf(walkingMsg);
        if (n < 0) {
            text += " ";
            text += walkingMsg;
        }

        text = text.replace(motionMsg, "");
        deltaText.setText(text);

        long elapsedMillis = SystemClock.elapsedRealtime() - myChronometer.getBase();
        long dur = SystemClock.elapsedRealtime() - lastTime;
        lastTime = SystemClock.elapsedRealtime();
        String durStr = String.format("%d", dur);
        String stepsStr = String.format("%d", numSteps);

        Hashtable dictionary = new Hashtable();
        dictionary.put("activityName", getString(R.string.up_and_go));
        dictionary.put("elapsed time", elapsedMillis);
        dictionary.put("dur (millsecond)", durStr);
        dictionary.put("motion", walkingMsg);
        dictionary.put("step num", stepsStr);
        becareRemoteSensorManager.uploadWalkingActivityData(dictionary);
    }

    public class sensorData{
        public float x;
        public float y;
        public float z;
        public long timestamp;
        public float accel;
    }
}



