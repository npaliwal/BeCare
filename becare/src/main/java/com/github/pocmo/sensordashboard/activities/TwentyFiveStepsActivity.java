package com.github.pocmo.sensordashboard.activities;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.SimpleStepDetector;
import com.github.pocmo.sensordashboard.StepListener;

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Locale;


/**
 * Created by qtxdev on 7/5/2016.
 */
public class  TwentyFiveStepsActivity extends Activity implements SensorEventListener, StepListener {
    private SimpleStepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;

    private int numSteps = 0;
    private TextView heightText;
    private TextView stepText;
    private TextView distanceFeetText;
    private TextView speedText;
    private Button start;
    private Button  stop;
    private Spinner spinner;
    Chronometer myChronometer;
    private boolean startMeasure = false;
    private int countDown = 0;
    private long startTime=0;
    private BecareRemoteSensorManager becareRemoteSensorManager;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twenty_five_steps);

        ImageView mImgSample = (ImageView) findViewById(R.id.img_25_steps1);
        ImageView mImgSample2 = (ImageView) findViewById(R.id.img_25_steps2);
        LinearLayout rl = (LinearLayout)findViewById(R.id.twentyFiveSteps);
        rl.setBackgroundResource(R.drawable.wallpaper7);

        spinner = (Spinner) findViewById(R.id.spinner);

        stepText = (TextView) findViewById(R.id.steps_25);
        distanceFeetText = (TextView) findViewById(R.id.distance_25);

        speedText = (TextView) findViewById(R.id.speed_25);
        myChronometer = (Chronometer)findViewById(R.id.chronometer);

        start = (Button)findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                countDown = 25;
                stepText.setText("25");
                distanceFeetText.setText("0");
                speedText.setText("0");
                myChronometer.setBase(SystemClock.elapsedRealtime());
                myChronometer.start();
                numSteps = 0;
                startMeasure = true;
                startTime = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
            }
        });

        stop = (Button)findViewById(R.id.stop_button);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numSteps = 0;
                startMeasure = false;
                myChronometer.stop();
                Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
            }
        });


        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        becareRemoteSensorManager = BecareRemoteSensorManager.getInstance(TwentyFiveStepsActivity.this);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new SimpleStepDetector();
        simpleStepDetector.registerListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        numSteps = 0;
        stepText.setText("0");
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        becareRemoteSensorManager.startMeasurement();
        becareRemoteSensorManager.getUploadDataHelper().setUserActivity(getString(R.string.twenty_five_steps), null);
    }

    @Override
    public void onStop() {
        super.onStop();
        startMeasure = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        startMeasure = false;
        sensorManager.unregisterListener(this);
        becareRemoteSensorManager.stopMeasurement();
        becareRemoteSensorManager.getUploadDataHelper().setUserActivity(null, null);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
        countDown = 25 - numSteps;
        String countDownStr = String.format("%d", countDown);
        stepText.setText(countDownStr);
        String str = String.valueOf(spinner.getSelectedItem());
        double height = Double.parseDouble(str);
        double footLength = height * 0.414;
        double feet = numSteps * footLength;
        String feetStr = String.format("%.5f", feet);
        distanceFeetText.setText(feetStr);

        long elapsedMillis = SystemClock.elapsedRealtime() - myChronometer.getBase();
        double sec = (double)elapsedMillis / 1000.0;
        double speed = feet / sec;
        String speedStr = String.format("%.5f", speed);
        speedText.setText(speedStr);

        String stepsStr = String.format("%d", numSteps);
        String heightStr = String.format("%.1f", (float)height);
      //  long currTime = System.currentTimeMillis();
       // long dur = currTime - startTime;

        Hashtable dictionary = new Hashtable();
        dictionary.put("activityName", getString(R.string.twenty_five_steps));
        dictionary.put("dur (ms)", elapsedMillis);
        dictionary.put("speed (ft/sec)", speedStr);
        dictionary.put("distance (ft)", feetStr);
        dictionary.put("step number",  stepsStr);
        dictionary.put("height",  heightStr);
        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);

        if (countDown <= 0) {
            startMeasure = false;
            myChronometer.stop();
        }
    }

}