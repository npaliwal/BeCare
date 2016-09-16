package com.github.pocmo.sensordashboard.activities;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.SimpleStepDetector;
import com.github.pocmo.sensordashboard.StepListener;
import com.github.pocmo.sensordashboard.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;


/**
 * Created by qtxdev on 7/5/2016.
 */
public class  TwentyFiveStepsActivity extends AppCompatActivity implements SensorEventListener, StepListener {
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
    private long lastStepTime = 0;
    private ImageView walkImage;
    private CountDownTimer  imageTimer;
    private int imageCounter = 2;
    private BecareRemoteSensorManager becareRemoteSensorManager;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTitleBar();
        setContentView(R.layout.twenty_five_steps);

        walkImage = (ImageView) findViewById(R.id.img_25_steps1);
        LinearLayout rl = (LinearLayout)findViewById(R.id.twentyFiveSteps);
      //  rl.setBackgroundResource(R.drawable.wallpaper7);

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
                lastStepTime = 0;
                startTime = System.currentTimeMillis();
           //     Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
                imageCounter=2;
                imageTimer.start();
            }
        });

        stop = (Button)findViewById(R.id.stop_button);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numSteps = 0;
                startMeasure = false;
                myChronometer.stop();
                imageTimer.cancel();
             //   Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
            }
        });


        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        becareRemoteSensorManager = BecareRemoteSensorManager.getInstance(TwentyFiveStepsActivity.this);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new SimpleStepDetector();
        simpleStepDetector.registerListener(this);

        imageTimer = new CountDownTimer(160000, 150) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {

                switch (imageCounter){

                    case 2:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0002);
                        break;
                    case 3:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0003);
                        break;
                    case 4:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0004);
                        break;
                    case 5:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0005);
                        break;
                    case 6:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0006);
                        break;
                    case 7:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0007);
                        break;
                    case 8:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0008);
                        break;
                    case 9:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0009);
                        break;
                    case 10:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0010);
                        break;
                    case 11:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0011);
                        break;
                    case 12:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0012);
                        break;
                    case 13:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0013);
                        break;
                    case 14:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0014);
                        break;
                    case 15:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0015);
                        break;
                    case 16:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0016);
                        break;
                    case 17:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0017);
                        break;
            /*        case 18:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0018);
                        break;
                    case 19:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0019);
                        break;
                    case 20:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0020);
                        break;
                    case 21:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0021);
                        break;
                    case 22:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0022);
                        break;
                    case 23:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0023);
                        break;
                    case 24:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0024);
                        break;
                    case 25:
                        walkImage.setImageResource(R.mipmap.walk7_frame_0025);
                        break;*/

                }
                imageCounter++;
                if (imageCounter > 17)
                    imageCounter = 2;
            }

            public void onFinish() {

            }
        };
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
        uploadEnd();
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
        back.setVisibility(View.GONE);

        ImageView next = (ImageView)customActionBar.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getSixMinutesActivity(TwentyFiveStepsActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ImageView home = (ImageView) customActionBar.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.getParentActivityIntent(TwentyFiveStepsActivity.this);
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

    public void getSixMinutes(View view) {
        Intent intent = new Intent(this, SixMinutesActivity.class);
        startActivity(intent);

    }

    private void setImage()
    {
        /*
        switch (numSteps) {
            case 2:
                walkImage.setImageResource(R.drawable.walk7_frame_0025);
                break;
            case 3:
                walkImage.setImageResource(R.drawable.walk7_frame_0024);
                break;
            case 4:
                walkImage.setImageResource(R.drawable.walk7_frame_0023);
                break;
            case 5:
                walkImage.setImageResource(R.drawable.walk7_frame_0022);
                break;
            case 6:
                walkImage.setImageResource(R.drawable.walk7_frame_0021);
                break;
            case 7:
                walkImage.setImageResource(R.drawable.walk7_frame_0020);
                break;
            case 8:
                walkImage.setImageResource(R.drawable.walk7_frame_0019);
                break;
            case 9:
                walkImage.setImageResource(R.drawable.walk7_frame_0018);
                break;
            case 10:
                walkImage.setImageResource(R.drawable.walk7_frame_0017);
                break;
            case 11:
                walkImage.setImageResource(R.drawable.walk7_frame_0016);
                break;
            case 12:
                walkImage.setImageResource(R.drawable.walk7_frame_0015);
                break;
            case 13:
                walkImage.setImageResource(R.drawable.walk7_frame_0014);
                break;
            case 14:
                walkImage.setImageResource(R.drawable.walk7_frame_0013);
                break;
            case 15:
                walkImage.setImageResource(R.drawable.walk7_frame_0012);
                break;
            case 16:
                walkImage.setImageResource(R.drawable.walk7_frame_0011);
                break;
            case 17:
                walkImage.setImageResource(R.drawable.walk7_frame_0010);
                break;
            case 18:
                walkImage.setImageResource(R.drawable.walk7_frame_0009);
                break;
            case 19:
                walkImage.setImageResource(R.drawable.walk7_frame_0008);
                break;
            case 20:
                walkImage.setImageResource(R.drawable.walk7_frame_0007);
                break;
            case 21:
                walkImage.setImageResource(R.drawable.walk7_frame_0006);
                break;
            case 22:
                walkImage.setImageResource(R.drawable.walk7_frame_0005);
                break;

            case 23:
                walkImage.setImageResource(R.drawable.walk7_frame_0004);
                break;
            case 24:
                walkImage.setImageResource(R.drawable.walk7_frame_0003);
                break;
        }
*/
 /*       int d = numSteps % 2;
        if (d > 0)
            walkImage.setImageResource(R.drawable.walk7_frame_0002);
        else
            walkImage.setImageResource(R.drawable.walk7_frame_0014);*/
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

        long now = SystemClock.elapsedRealtime();
        long elapsedMillis = (lastStepTime == 0) ? 0: now - lastStepTime;
        lastStepTime = now;

        double sec = (double)elapsedMillis / 1000.0;
        double speed = feet / sec;
        String speedStr = String.format("%.5f", speed);
        speedText.setText(speedStr);

        String stepsStr = String.format("%d", numSteps);
        String heightStr = String.format("%.1f", (float)height);
      //  long currTime = System.currentTimeMillis();
       // long dur = currTime - startTime;

        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("activityname", getString(R.string.twenty_five_steps));
        dictionary.put("step number",  stepsStr);
        dictionary.put("height",  heightStr);
        dictionary.put("speed (ft/sec)", speedStr);
        dictionary.put("distance (ft)", feetStr);
        dictionary.put("dur (ms)", elapsedMillis);
        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);

     //   setImage();

        if (countDown <= 0) {
            startMeasure = false;
            myChronometer.stop();
            imageTimer.cancel();
        }
    }

    private void uploadEnd(){
        long readTime = System.currentTimeMillis();
        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("endactivity", getString(R.string.twenty_five_steps));
        dictionary.put("user_id", becareRemoteSensorManager.getPreferenceStorage().getUserId());
        dictionary.put("session_token", becareRemoteSensorManager.getPreferenceStorage().getUserId() +"_" + readTime);
        dictionary.put("date", DateUtils.formatDate(readTime));
        dictionary.put("time", DateUtils.formatTime(readTime));

        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);

    }
}