package com.becare.users.activities;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.becare.users.AppConfig;
import com.becare.users.BecareRemoteSensorManager;
import com.becare.users.PreferenceStorage;
import com.becare.users.R;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * Created by neerajpaliwal on 06/05/16.
 */
public class ArmElevationActivity extends AppCompatActivity implements SensorEventListener {

    ImageView  phoneArm;
    TextView  timer;
    Button startButton, stopButton;
    private BecareRemoteSensorManager mRemoteSensorManager;
    private PreferenceStorage preferenceStorage;

    private long lastUpdateTime = 0;
    private int durationTask = AppConfig.DEFAULT_ARM_TASK_DURATION;
    private int seq = 0;

    private static final float THRESHOLD = (float)11.2;  //11.2
    private static final float THRESHOLD2 = (float)13.0;
    private static final int SAMPLE_SIZE = 3;
    private static final int DOWN_SAMPLE_SIZE = 3;

    private SensorManager sensorManager;
    private Sensor accel;
    private  Sensor magnetic;

    private TextView timesText;
    private TextView deltaText;
    private TextView countdown;
    private TextView rectangle;
    private View rectangleRoot;
    Chronometer myChronometer;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float currectAzimuth = 0;
    private float baseAzimuth = 0;

    private boolean motionFound = false;
    private boolean downMotionFound = false;
    private boolean startMeasure = false;
    private boolean downSendData = false;

    private int motionCounter = 0;
    private long buttonPushTime = 0;
    private float mAccelCurrent = 0;
    private float mAccelLast = 0;
    private long lastMotionTime = 0;
    private long armDownTime = 0;
    private long armUpTime = 0;
    private long upReminder = 0;
    private CountDownTimer cTimer = null;
    private List<sensorData> sensorList = new ArrayList<>();
    private List<sensorData> downSensorList = new ArrayList<>();
    ArrayList<String> eventList = new ArrayList<String>();
    private Boolean synchUp = false;
    public ImageView arrowView = null;

    private static final float grav[] = new float[3]; // Gravity (a.k.a
    // accelerometer data)
    private static final float mag[] = new float[3]; // Magnetic
    private static final float rotation[] = new float[9]; // Rotation matrix in
    // Android format
    private static final float orientation[] = new float[3]; // azimuth, pitch,
    // roll
    private static float smoothed[] = new float[3];
    private static double floatBearing = 0;
   // private static final float ALPHA = 0.2f;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTitleBar();
        setContentView(R.layout.activity_arm_elevation);

      //  phoneArm = (ImageView) findViewById(R.id.iv_phone);
        startButton = (Button) findViewById(R.id.button_start);
        stopButton = (Button) findViewById(R.id.button_stop);
       // timer = (TextView) findViewById(R.id.timer);
        arrowView = (ImageView) findViewById(R.id.main_image_hands);
        rectangle = (TextView) findViewById(R.id.rectangle);

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
        cTimer = new CountDownTimer(40000, 1000) { // adjust the milli seconds here
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
                rectangle.setBackgroundColor(Color.rgb(255, 200,50));
            }
        };

        countdown.setText("00:40");

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasure = true;
              //  phoneArm.clearAnimation();
                startButton.setClickable(false);
                startButton.setText("Started");
                seq = -1;
                synchUp = true;
                motionFound = false;
                upReminder = 0;
                timesText.setText("0");
                armDownTime = 0;
                countdown.setText("04:00");
                deltaText.setText("");
                eventList.clear();
                cTimer.start();
                buttonPushTime = System.currentTimeMillis();
                lastMotionTime = System.currentTimeMillis();
                baseAzimuth = azimuth;
                String str= String.format("%f %f",baseAzimuth, mGeomagnetic[2]);
            //    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
              //  rectangle.setBackgroundColor(Color.rgb(10, 160,92));
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
                rectangle.setBackgroundColor(Color.rgb(255, 200,50));
                startButton.setClickable(true);
                startButton.setText("Start");

            }
        });
    }

    private void customTitleBar() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false); // disables default title on
        ab.setDisplayShowCustomEnabled(true); // enables custom view.
        ab.setDisplayShowHomeEnabled(false); // hides app icon.
        ab.setDisplayHomeAsUpEnabled(false);
        // Inflating Layout
        LayoutInflater inflater = (LayoutInflater) ab.getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        View customActionBar = inflater.inflate(R.layout.actionbar_layout, null);
        TextView title = (TextView) customActionBar.findViewById(R.id.title);
        title.setText(R.string.exercise_arm_elevation);

        ImageView back = (ImageView) customActionBar.findViewById(R.id.back);
        back.setVisibility(View.GONE);

        ImageView next = (ImageView) customActionBar.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getSnooker(ArmElevationActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ImageView home = (ImageView) customActionBar.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.getParentActivityIntent(ArmElevationActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        ab.setCustomView(customActionBar, layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRemoteSensorManager.startMeasurement();
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_FASTEST);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(null, null);
        mRemoteSensorManager.stopMeasurement();
         sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        orientationChange(event);

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

            if (downSensorList.size() ==DOWN_SAMPLE_SIZE)
                downSensorList.remove(0);
            downSensorList.add(data);

            boolean found = true;
            String motion = "";
            if (sensorList.size() ==SAMPLE_SIZE){
                for (int i=0; i<SAMPLE_SIZE; i++){
                    if (sensorList.get(i).accel < THRESHOLD) {
                        found = false;
                        break;
                    }
                }

                if (found) {
                    motionFound = true;
                    String display = deltaText.getText().toString();
                    Hashtable dictionary = new Hashtable();
                    long dur =  System.currentTimeMillis()- lastMotionTime;
                    dictionary.put("motion", dur);
                    String str=String.format("%d", motionCounter);
                    dictionary.put("counter", str);
                    dictionary.put("azimuth", azimuth);
               //     mRemoteSensorManager.uploadWalkingActivityData(dictionary);
                    lastMotionTime = System.currentTimeMillis();
                    sensorList.clear();
                }
            }
            if (downSensorList.size() ==DOWN_SAMPLE_SIZE){
                for (int i=0; i<DOWN_SAMPLE_SIZE; i++){
                    if (downSensorList.get(i).accel < THRESHOLD) {
                        found = false;
                        break;
                    }
                }

                if (found) {
                    downMotionFound = true;
                    String display = deltaText.getText().toString();
                    Hashtable dictionary = new Hashtable();
                 //   long dur = System.currentTimeMillis()- lastMotionTime;
                    dictionary.put("motion", "down");
                    String str=String.format("%d", motionCounter);
                    dictionary.put("counter", str);
                    dictionary.put("azimuth", azimuth);
                  //  mRemoteSensorManager.uploadWalkingActivityData(dictionary);
                  //  lastMotionTime = System.currentTimeMillis();
                    downSensorList.clear();
                }
            }

        }

       if (synchUp){
            Boolean found = findUp();
            if (found) {
                synchUp = false;
                eventList.add("Up");
            }
            return;
        }

       if (findUp())
           return;
        findDown();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    private void adjustArrow() {
        if (arrowView == null) {
            return;
        }


        Animation an = new RotateAnimation(-currectAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currectAzimuth = azimuth;

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        arrowView.startAnimation(an);
    }


private void orientationChange(SensorEvent event) {

    synchronized (this) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            smoothed = filter(event.values, grav, (float)0.2);
            grav[0] = smoothed[0];
            grav[1] = smoothed[1];
            grav[2] = smoothed[2];
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smoothed = filter(event.values, mag, (float)0.6);
            mag[0] = smoothed[0];
            mag[1] = smoothed[1];
            mag[2] = smoothed[2];
        }

        // Get rotation matrix given the gravity and geomagnetic matrices
        boolean success = SensorManager.getRotationMatrix(rotation, null, grav, mag);
        if (success) {

            SensorManager.getOrientation(rotation, orientation);
            floatBearing = orientation[0];

            // Convert from radians to degrees
            floatBearing = Math.toDegrees(floatBearing); // degrees east of true
            // north (180 to -180)

            // adjust to 0-360
            if (floatBearing < 0) floatBearing += 360;
            azimuth = (float)floatBearing;
          //  adjustArrow();
        }
    }
}

    private Boolean findUp(){
        String display = deltaText.getText().toString();

        float diff = azimuth - baseAzimuth;
        if (Math.abs(diff) >= 100 && motionFound ) {

            long now = System.currentTimeMillis();
            if ((now - buttonPushTime) < 100)
                return false;

            if (display.equals("Up")) {
                double dur = (System.currentTimeMillis() - armDownTime) * (1.0 / 5.0);
                upReminder = (long) dur;
                motionFound = false;

                return true;
            }
            ++seq;

            deltaText.setText("Up");
            rectangle.setBackgroundColor(Color.rgb(10, 160,92));
           // rectangle.setBackgroundColor(Color.rgb(65, 99,181));
            Hashtable dictionary = new Hashtable();
            double ratio = 4.0 / 5.0;
            //double dur = (now - armDownTime) * ratio;
            double dur = (armDownTime == 0)? 0: (now - armDownTime);
            long ms = (long) dur;

            dictionary.put("activityname", getString(R.string.exercise_arm_elevation));
            dictionary.put("arm motion", "up");
            dictionary.put("dur (millsecond)", ms);
            dictionary.put("seq", seq);
          //  dictionary.put("azimuth", azimuth);
            mRemoteSensorManager.uploadActivityDataAsyn(dictionary);

            dur = (now - armDownTime) * (1.0 / 5.0);
            upReminder = (long) dur;
            armUpTime = now;
            motionFound = false;
            downSendData = true;

            return true;
        }
        return false;
    }

    private Boolean findDown(){
        String display = deltaText.getText().toString();
        if (display.equals( ""))
            return false;

        float diff = azimuth - baseAzimuth;
        if (display.equals( "Down")){
            downMotionFound = false;
            //  motionFound = false;
            if (Math.abs(diff) <= 30 && downSendData) {
                Hashtable dictionary = new Hashtable();
                // long dur = (armDownTime - armUpTime) + upReminder;
                long now = System.currentTimeMillis();
                long dur = now - armUpTime;
                dictionary.put("activityname", getString(R.string.exercise_arm_elevation));
                dictionary.put("arm motion", "down");
                dictionary.put("dur (millsecond)", dur);
                dictionary.put("seq", seq);
            //    dictionary.put("azimuth", azimuth);
                mRemoteSensorManager.uploadActivityDataAsyn(dictionary);

                armDownTime = now;
                downSendData = false;
            }

            return true;
        }

        if (display.equals( "Up") && Math.abs(diff) <= 45 && downMotionFound){

            deltaText.setText("Down");
            rectangle.setBackgroundColor(Color.rgb(215,58,49));
            String str = String.format("%d", seq+1);
            timesText.setText(str);

            downMotionFound = false;
            return true;
        }
        return false;
    }

    public static float[] filter(float[] input, float[] prev, float ALPHA) {
        if (input == null || prev == null) throw new NullPointerException("input and prev float arrays must be non-NULL");
        if (input.length != prev.length) throw new IllegalArgumentException("input and prev must be the same length");

        for (int i = 0; i < input.length; i++) {
            prev[i] = prev[i] + ALPHA * (input[i] - prev[i]);
        }
        return prev;
    }

    public class sensorData{
        public float x;
        public float y;
        public float z;
        public long timestamp;
        public float accel;
    }
}
