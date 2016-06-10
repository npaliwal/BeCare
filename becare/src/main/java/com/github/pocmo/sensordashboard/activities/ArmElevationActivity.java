package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.style.IconMarginSpan;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;

/**
 * Created by neerajpaliwal on 06/05/16.
 */
public class ArmElevationActivity extends Activity implements SensorEventListener {

    ImageView watchArm, phoneArm;
    TextView startButton, timer;

    private BecareRemoteSensorManager mRemoteSensorManager;
    private SensorManager mSensorManager;
    private Sensor mAccelero, mGyro, mGravity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arm_elevation);
        watchArm = (ImageView) findViewById(R.id.iv_watch);
        phoneArm = (ImageView) findViewById(R.id.iv_phone);
        startButton = (TextView) findViewById(R.id.button_start);
        timer = (TextView) findViewById(R.id.timer);

        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(ArmElevationActivity.this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelero = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        Animation mAnimation = new RotateAnimation(-80f, 80f, -15f, 50f);
        mAnimation.setDuration(5000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        watchArm.setAnimation(mAnimation);


        Animation mRevAnimation = new RotateAnimation(80f, -80f, -15f, 50f);
        mRevAnimation.setDuration(5000);
        mRevAnimation.setRepeatCount(-1);
        mRevAnimation.setRepeatMode(Animation.REVERSE);
        mRevAnimation.setInterpolator(new LinearInterpolator());
        phoneArm.startAnimation(mRevAnimation);


        //Animation armTurn = AnimationUtils.loadAnimation(this, R.anim.arm_elevate);
        //Animation armRevTurn = AnimationUtils.loadAnimation(this, R.anim.arm_rev_elevate);
        //watchArm.startAnimation(armTurn);
        //phoneArm.startAnimation(armRevTurn);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
