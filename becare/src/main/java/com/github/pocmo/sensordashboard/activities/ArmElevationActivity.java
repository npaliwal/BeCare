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
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;

/**
 * Created by neerajpaliwal on 06/05/16.
 */
public class ArmElevationActivity extends AppCompatActivity implements SensorEventListener {

    ImageView watchArm, phoneArm;
    TextView startButton, timer;

    private SensorManager mSensorManager;
    private boolean mStartReading = false;

    private BecareRemoteSensorManager mRemoteSensorManager;
    private PreferenceStorage preferenceStorage;
    private Sensor mAccelero, mGyro, mGravity;

    private long lastUpdateTime = 0;
    private int durationTask = AppConfig.DEFAULT_ARM_TASK_DURATION;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arm_elevation);
        watchArm = (ImageView) findViewById(R.id.iv_watch);
        phoneArm = (ImageView) findViewById(R.id.iv_phone);
        startButton = (TextView) findViewById(R.id.button_start);
        timer = (TextView) findViewById(R.id.timer);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        preferenceStorage = new PreferenceStorage(getApplicationContext());
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(ArmElevationActivity.this);

        mAccelero = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        durationTask = preferenceStorage.getArmElevationTaskDuration();

        final Animation mAnimation = new RotateAnimation(-80f, 80f, -15f, 50f);
        mAnimation.setDuration(3000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        watchArm.setAnimation(mAnimation);


        final Animation mRevAnimation = new RotateAnimation(80f, -80f, -15f, 50f);
        mRevAnimation.setDuration(3000);
        mRevAnimation.setRepeatCount(-1);
        mRevAnimation.setRepeatMode(Animation.REVERSE);
        mRevAnimation.setInterpolator(new LinearInterpolator());
        phoneArm.startAnimation(mRevAnimation);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartReading = true;
                phoneArm.clearAnimation();
                watchArm.clearAnimation();
                lastUpdateTime = System.currentTimeMillis();
                mRemoteSensorManager.getUploadDataHelper().setUserActivity(getString(R.string.exercise_arm_elevation), null);
                startButton.setClickable(false);
                startButton.setText("Reading Sensors ...");
                new CountDownTimer(durationTask*1000 + 4000, 1000) {
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
                        mStartReading = false;
                        phoneArm.startAnimation(mRevAnimation);
                        watchArm.startAnimation(mAnimation);
                        timer.setText("WELL DONE !!");
                        startButton.setText("RESTART");
                        startButton.setClickable(true);
                    }
                }.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRemoteSensorManager.startMeasurement();

        if(mAccelero != null)
            mSensorManager.registerListener(this, mAccelero, SensorManager.SENSOR_DELAY_NORMAL);
        if(mGyro != null)
            mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
        if(mGravity != null)
            mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(null, null);
        mRemoteSensorManager.stopMeasurement();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(!mStartReading){
            return;
        }
        mRemoteSensorManager.addMobileSensorData(event.sensor.getType(), event.accuracy, event.timestamp, event.values);
        long currTime = System.currentTimeMillis();
        if((currTime - lastUpdateTime) >= 3000) {
            lastUpdateTime = currTime;

            mRemoteSensorManager.calculateMobileStats(currTime);
            mRemoteSensorManager.uploadAllMobileSensorData();
//            mRemoteSensorManager.uploadActivityData();//Already uploaded from wear
            mRemoteSensorManager.resetMobileStats();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
