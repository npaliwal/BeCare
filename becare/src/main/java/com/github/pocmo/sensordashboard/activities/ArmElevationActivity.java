package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.style.IconMarginSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by neerajpaliwal on 06/05/16.
 */
public class ArmElevationActivity extends AppCompatActivity implements SensorEventListener {

    ImageView  phoneArm;
    TextView startButton, stopButton, timer;
    private BecareRemoteSensorManager mRemoteSensorManager;
    private PreferenceStorage preferenceStorage;

    private long lastUpdateTime = 0;
    private int durationTask = AppConfig.DEFAULT_ARM_TASK_DURATION;
    private int seq = 0;

    private static final float THRESHOLD = (float)11.3;
    private static final int SAMPLE_SIZE = 5;

    private SensorManager sensorManager;
    private Sensor accel;
    private  Sensor magnetic;
    private final float magStart[] = new float[3]; // Magnetic
    private final float magUp[] = new float[3];
    private final float magDown[] = new float[3];
    private float smoothed[] = new float[3];

    private TextView timesText;

    private TextView deltaText;
    private TextView countdown;
    Chronometer myChronometer;

    private float[] mGravity = new float[3];

    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;

    private float startAzimuth = 0;
    private float upAzimuth = 0;
    private boolean motionFound = false;
    private boolean startMeasure = false;
    private boolean armDown = false;
    private int motionCounter = 0;
    private float mAccelCurrent = 0;
    private float mAccelLast = 0;
    private long lastMotionTime = 0;
    private long lastEventTime = 0;
    private long armDownTime = 0;
    private long armUpTime = 0;
    private long upReminder = 0;
    private CountDownTimer cTimer = null;
    private List<sensorData> sensorList = new ArrayList<>();
    public ImageView arrowView = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arm_elevation);

      //  phoneArm = (ImageView) findViewById(R.id.iv_phone);
        startButton = (TextView) findViewById(R.id.button_start);
        stopButton = (TextView) findViewById(R.id.button_stop);
        timer = (TextView) findViewById(R.id.timer);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        preferenceStorage = new PreferenceStorage(getApplicationContext());
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(ArmElevationActivity.this);

        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        countdown = (TextView) findViewById(R.id.countdown);
        durationTask = preferenceStorage.getArmElevationTaskDuration();
/*
        final Animation mAnimation = new RotateAnimation(-80f, 80f, -15f, 50f);
        mAnimation.setDuration(3000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        watchArm.setAnimation(mAnimation);

*/
        timesText = (TextView) findViewById(R.id.steps);


        deltaText = (TextView) findViewById(R.id.delta);
        myChronometer = (Chronometer)findViewById(R.id.chronometer);
/*
        final Animation mRevAnimation = new RotateAnimation(80f, -80f, -15f, 50f);
        mRevAnimation.setDuration(3000);
        mRevAnimation.setRepeatCount(-1);
        mRevAnimation.setRepeatMode(Animation.REVERSE);
        mRevAnimation.setInterpolator(new LinearInterpolator());
        phoneArm.startAnimation(mRevAnimation);

*/
        cTimer = new CountDownTimer(60000, 1000) { // adjust the milli seconds here
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
                startButton.setText("Start");
                startButton.setClickable(true);
            }
        };

        countdown.setText("01:00");

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasure = true;
              //  phoneArm.clearAnimation();
                lastEventTime = System.currentTimeMillis();
                startButton.setClickable(false);
                startButton.setText("Started");
                seq = -1;
                armDown = false;
                upAzimuth = 0;
                startAzimuth = 0;
                motionFound = false;
                upReminder = 0;
                timesText.setText("0");
                armDownTime = System.currentTimeMillis();
                countdown.setText("01:00");
                cTimer.start();
          /*      new CountDownTimer(durationTask*1000 + 3000, 1000) {
                    int timeSec  = 0;
                    public void onTick(long millisUntilFinished) {
                        timeSec = (int) (millisUntilFinished/1000);
                        if(timeSec > durationTask+2){
                            timer.setText("GET");
                        }else if(timeSec > durationTask+1){
                            timer.setText("GET SET");
                        }else if(timeSec > durationTask){
                            timer.setText("GET SET GO !!");
                        } else{
                            timer.setText("Time : " + timeSec);
                        }

                    }

                    public void onFinish() {
                        startMeasure = false;
                        phoneArm.startAnimation(mRevAnimation);

                        timer.setText("WELL DONE !!");
                        startButton.setText("RESTART");
                        startButton.setClickable(true);
                    }
                }.start();
                */
            }
        });


        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasure = false;
                Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
                startButton.setClickable(true);
                startButton.setText("Start");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_FASTEST);


    }

    @Override
    protected void onPause() {
        super.onPause();
     //   mRemoteSensorManager.getUploadDataHelper().setUserActivity(null, null);
      //  mRemoteSensorManager.stopMeasurement();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!startMeasure)
            return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
            boolean found = true;
            String motion = "";
            if (sensorList.size() ==SAMPLE_SIZE){
                for (int i=0; i<SAMPLE_SIZE; i++){
                    if (sensorList.get(i).accel < THRESHOLD) {
                        found = false;
                        break;
                    }
                    // motion += String.format("%.5f,", sensorList.get(i).accel);
                    motion = "raising/sitting";
                    lastMotionTime = System.currentTimeMillis();
                }

                if (found) {
                    motionFound = true;


                }
                //clear samples
                if (found) {
                    sensorList.clear();
                }
            }

        }

        orientationChange(event);

        if (startAzimuth == 0)
            startAzimuth = azimuth;

        if (upAzimuth==0 && motionFound ){
            if (Math.abs(azimuth - startAzimuth) >= 200){
                ++seq;
                motionFound = false;
                upAzimuth = azimuth;
                deltaText.setText("Up");

                long now = System.currentTimeMillis();
                Hashtable dictionary = new Hashtable();
                double ratio = 4.0/5.0;
                double dur = (now - armDownTime) * ratio;
                long ms = (long)dur;

                dictionary.put("activityName", getString(R.string.exercise_arm_elevation));
                dictionary.put("arm motion","up");
                dictionary.put("dur (millsecond)", ms);
                dictionary.put("seq", seq);
                mRemoteSensorManager.uploadWalkingActivityData(dictionary);
                armUpTime = now;
                armDown = false;
                dur = (now - armDownTime) * (1.0/5.0);
                upReminder = (long)dur;
                return;
            }
        }


        if (upAzimuth != 0 && Math.abs(azimuth - upAzimuth) >= 190){
            String display = deltaText.getText().toString();
        /*    if (display == "Down"){
                upAzimuth = 0;
                startAzimuth = azimuth;
                armDownTime = System.currentTimeMillis();// + 500;
            }
            else {*/
                deltaText.setText("Down");
                upAzimuth = 0;
                startAzimuth = azimuth;
                String str = String.format("%d", seq+1);
                timesText.setText(str);
                armDownTime = System.currentTimeMillis();//+ 800;

            if (!armDown) {

                Hashtable dictionary = new Hashtable();
                long dur = (armDownTime - armUpTime) + upReminder;

                dictionary.put("activityName", getString(R.string.exercise_arm_elevation));
                dictionary.put("arm motion", "down");
                dictionary.put("dur (millsecond)", dur);
                dictionary.put("seq", seq);
                mRemoteSensorManager.uploadWalkingActivityData(dictionary);
                //  lastEventTime = now;
                armDown = true;
            }
            return;
        }

   /*     if (armDown ){
            long now = System.currentTimeMillis();
            Hashtable dictionary = new Hashtable();
            long dur = (now - armDownTime);
            if (dur >= 100) {
                dur = now - armUpTime;
                dictionary.put("activityName", getString(R.string.exercise_arm_elevation));
                dictionary.put("arm motion", "down");
                dictionary.put("dur (millsecond)", dur);
                dictionary.put("seq", seq);
                mRemoteSensorManager.uploadWalkingActivityData(dictionary);
                armDown = false;
            }
          //  armDownTime = now;
        }
        */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void orientationChange(SensorEvent event) {
        final float alpha = 0.98f;

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                mGravity[0] = alpha * mGravity[0] + (1 - alpha)
                        * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha)
                        * event.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha)
                        * event.values[2];
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
                //   adjustArrow();
            }
        }
    }

    public class sensorData{
        public float x;
        public float y;
        public float z;
        public long timestamp;
        public float accel;
    }
}
