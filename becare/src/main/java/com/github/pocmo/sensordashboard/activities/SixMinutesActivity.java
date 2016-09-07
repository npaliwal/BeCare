package com.github.pocmo.sensordashboard.activities;


import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.SimpleStepDetector;
import com.github.pocmo.sensordashboard.StepListener;

import java.util.Hashtable;

/**
 * Created by qtxdev on 7/5/2016.
 */
public class  SixMinutesActivity extends AppCompatActivity implements SensorEventListener, StepListener {


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
    private ImageView walkImage;
 //   AnimationDrawable walkAnimation;
    private long startTime = 0;
    private CountDownTimer cTimer, imageTimer;
    private long lastTime = 0;
    private int imageCounter = 1;
    private BecareRemoteSensorManager becareRemoteSensorManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTitleBar();
        Log.i(TAG, "onCreate()");

        setContentView(R.layout.six_minutes_walking);
      //  LinearLayout rl = (LinearLayout)findViewById(R.id.sixMinutesWalking);
     //   rl.setBackgroundResource(R.drawable.wallpaper7);

        spinner = (Spinner) findViewById(R.id.spinner_6min);
        walkImage = (ImageView) findViewById(R.id.img_6mon);
      //  walkImage.setBackgroundResource(R.drawable.six_minutes_walking_filling);
     //   walkAnimation = (AnimationDrawable) walkImage.getBackground();

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
                lastTime = 0;
                cTimer.start();
                imageCounter = 1;
                imageTimer.start();

              //  Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
              //  walkAnimation.start();

            }
        });

        stop = (Button)findViewById(R.id.stop_button_6min);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numSteps = 0;
                startMeasure = false;
             //   walkAnimation.stop();
            //    Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
                imageCounter = -0xFFFFFF;
                imageTimer.cancel();
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
           //     if (!walkAnimation.isRunning())
              //      walkAnimation.start();
            }

            public void onFinish() {
                startMeasure = false;
                countdown.setText("Done");
                imageTimer.cancel();
            }
        };

        imageTimer = new CountDownTimer(360000, 150) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {

               switch (imageCounter){
                   case 1:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0001);
                       break;
                   case 2:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0002);
                       break;
                   case 3:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0003);
                       break;
                   case 4:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0004);
                       break;
                   case 5:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0005);
                       break;
                   case 6:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0006);
                       break;
                   case 7:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0007);
                       break;
                   case 8:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0008);
                       break;
                   case 9:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0009);
                       break;
                   case 10:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0010);
                       break;
                   case 11:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0011);
                       break;
                   case 12:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0012);
                       break;
                   case 13:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0013);
                       break;
                   case 14:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0014);
                       break;
                   case 15:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0015);
                       break;
                   case 16:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0016);
                       break;
                   case 17:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0017);
                       break;
                   case 18:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0018);
                       break;
                   case 19:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0019);
                       break;
                   case 20:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0020);
                       break;
                   case 21:
                       walkImage.setImageResource(R.mipmap.walk12_frame_0021);
                       break;

               }
                imageCounter++;
                if (imageCounter > 21)
                    imageCounter = 1;
            }

            public void onFinish() {

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

    private void customTitleBar(){
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false); // disables default title on
        ab.setDisplayShowCustomEnabled(true); // enables custom view.
        ab.setDisplayShowHomeEnabled(false); // hides app icon.
        ab.setDisplayHomeAsUpEnabled(false);
        // Inflating Layout
        LayoutInflater inflater = (LayoutInflater) ab.getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        View customActionBar = inflater.inflate(R.layout.actionbar_layout, null);
        TextView title = (TextView) customActionBar.findViewById(R.id.title);
        title.setText(R.string.exercise_timed_walk);

        ImageView back = (ImageView)customActionBar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getTwentyFiveStepsActivity(SixMinutesActivity.this);
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();
            }
        });


        ImageView home = (ImageView) customActionBar.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.getParentActivityIntent(SixMinutesActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        ab.setCustomView(customActionBar, layout);
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
        long dur = (lastTime == 0) ? 0 : System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();

        Hashtable dictionary = new Hashtable();
        dictionary.put("activityname", getString(R.string.six_minutes_walk));
        dictionary.put("countdown time", countdownTimerStr);
        dictionary.put("speed (miles/sec)", speedStr);
        dictionary.put("distance (miles)", milesStr);
        dictionary.put("dur",  dur);
        dictionary.put("step number",  stepsStr);
        dictionary.put("height",  heightStr);
        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);
    }
}

