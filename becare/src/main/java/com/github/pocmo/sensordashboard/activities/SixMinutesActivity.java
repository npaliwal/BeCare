package com.github.pocmo.sensordashboard.activities;


import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.SimpleStepDetector;
import com.github.pocmo.sensordashboard.StepListener;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

/**
 * Created by qtxdev on 7/5/2016.
 */
public class  SixMinutesActivity extends Activity implements SensorEventListener, StepListener {


    private static final String TAG = "AndroidCompassActivity";

    private static TextView text = null;

    private SimpleStepDetector simpleStepDetector;
    private SensorManager sensorManager;

    private int numSteps = 0;
    private TextView countdown;
    private TextView stepText;
    private TextView distanceText;
    private TextView speedText;
    private Button start;
    private Button  stop;
    private Spinner spinner;
    private boolean startMeasure = false;

    private Sensor gsensor;
    private Sensor msensor;
    private float[] mGravity = new float[3];

    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float currectAzimuth = 0;

    // compass arrow to rotate
    public ImageView arrowView = null;
    private long startTime = 0;
    private CountDownTimer cTimer = null;
    private long lastTime = 0;
    private BecareRemoteSensorManager becareRemoteSensorManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        setContentView(R.layout.six_minutes_walking);
        LinearLayout rl = (LinearLayout)findViewById(R.id.sixMinutesWalking);
        rl.setBackgroundResource(R.drawable.wallpaper7);

        spinner = (Spinner) findViewById(R.id.spinner_6min);

        stepText = (TextView) findViewById(R.id.steps_6min);
        distanceText = (TextView) findViewById(R.id.distance_6min);

        speedText = (TextView) findViewById(R.id.speed_6min);
        countdown = (TextView) findViewById(R.id.countdown);

        start = (Button)findViewById(R.id.start_button_6min);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepText.setText("0");
                distanceText.setText("0");
                speedText.setText("0");
                numSteps = 0;
                startMeasure = true;
                startTime =System.currentTimeMillis();
                lastTime = System.currentTimeMillis();
                cTimer.start();
                Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
            }
        });

        stop = (Button)findViewById(R.id.stop_button_6min);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numSteps = 0;
                startMeasure = false;

                Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
            }
        });
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        becareRemoteSensorManager = BecareRemoteSensorManager.getInstance(SixMinutesActivity.this);

        simpleStepDetector = new SimpleStepDetector();
        simpleStepDetector.registerListener(this);

        arrowView = (ImageView) findViewById(R.id.main_image_hands);

        cTimer = new CountDownTimer(360000, 1000) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                countdown.setText(String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds));
            }

            public void onFinish() {
                startMeasure = false;
                countdown.setText("Done");
            }
        };

        countdown.setText("06:00");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
        sensorManager.registerListener(this, gsensor,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, msensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
        startMeasure = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");

        sensorManager.registerListener(this, gsensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, msensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        //    sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_FASTEST);
        becareRemoteSensorManager.startMeasurement();
    //    becareRemoteSensorManager.getUploadDataHelper().setUserActivity(getString(R.string.six_minutes_walk), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        startMeasure = false;
        becareRemoteSensorManager.stopMeasurement();
     //   becareRemoteSensorManager.getUploadDataHelper().setUserActivity(null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }

        compassChange(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void getTimedWalking(View view) {
        Intent intent = new Intent(this, TimedWalkedActivity.class);
        startActivity(intent);

    }

    public void getTwentyFiveStep(View view) {
        Intent intent = new Intent(this, TwentyFiveStepsActivity.class);
        startActivity(intent);

    }

    public void getUpAndGo(View view) {
        Intent intent = new Intent(this, UpAndGoActivity.class);
        startActivity(intent);

    }

    private void adjustArrow() {
        if (arrowView == null) {
            Log.i(TAG, "arrow view is not set");
            return;
        }

        Log.i(TAG, "will set rotation from " + currectAzimuth + " to "
                + azimuth);

        Animation an = new RotateAnimation(-currectAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currectAzimuth = azimuth;

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        arrowView.startAnimation(an);
    }

    private void compassChange(SensorEvent event) {
        final float alpha = 0.97f;

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                mGravity[0] = alpha * mGravity[0] + (1 - alpha)
                        * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha)
                        * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha)
                        * event.values[2];

                // mGravity = event.values;

                // Log.e(TAG, Float.toString(mGravity[0]));
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // mGeomagnetic = event.values;

                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
                        * event.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
                        * event.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
                        * event.values[2];
                // Log.e(TAG, Float.toString(event.values[0]));

            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                    mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // Log.d(TAG, "azimuth (rad): " + azimuth);
                azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                azimuth = (azimuth + 360) % 360;
                // Log.d(TAG, "azimuth (deg): " + azimuth);
                adjustArrow();
            }
        }
    }

    @Override
    public void step(long timeNs) {
        if (!startMeasure)
            return;

        numSteps++;

        String countDownStr = String.format("%d", numSteps);
        stepText.setText(countDownStr);
        String str = String.valueOf(spinner.getSelectedItem());
        double height = Double.parseDouble(str);
        double footLength = height * 0.414;
        double feet = numSteps * footLength;
        double miles = feet /5280;
        String milesStr = String.format("%.5f", miles);
        String feetStr = String.format("%.5f", feet);

        distanceText.setText(milesStr);

        long elapsedMillis = System.currentTimeMillis() - startTime;
        double sec = elapsedMillis / 1000.0;
        double speed = miles / sec;
        String speedStr = String.format("%.5f", speed);
        speedText.setText(speedStr);

        long countdownTimer = elapsedMillis;//360000 - elapsedMillis;
        String countdownTimerStr = String.format("%d", countdownTimer);
        String stepsStr = String.format("%d", numSteps);
        String heightStr = String.format("%.1f", (float)height);
        long dur = System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();

        Hashtable dictionary = new Hashtable();
        dictionary.put("activityName", getString(R.string.six_minutes_walk));
        dictionary.put("countdown time", countdownTimerStr);
        dictionary.put("speed (miles/sec)", speedStr);
        dictionary.put("distance (miles)", milesStr);
        dictionary.put("dur",  dur);
        dictionary.put("step number",  stepsStr);
        dictionary.put("height",  heightStr);
        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);
    }
}

