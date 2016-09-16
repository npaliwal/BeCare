package com.github.pocmo.sensordashboard.activities;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
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

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.SimpleStepDetector;
import com.github.pocmo.sensordashboard.StepListener;
import com.github.pocmo.sensordashboard.utils.DateUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by qtxdev on 7/5/2016.
 */
public class UpAndGoActivity extends AppCompatActivity implements SensorEventListener, StepListener {
    private static final float STAND_THRESHOLD = (float) 11.7;
    private static final float SIT_THRESHOLD = (float) 11.3;
    private static final int SAMPLE_SIZE = 5;
    private static final int SAMPLE_SIZE2 = 5;
    private final String raisingMsg = "Standing";
    private final String sittingMsg = "Sitting";
    private final String walkingMsg = "Walking";
    private final String walkingBackMsg = "Walking back";
    private final String turningMsg = "Turning";
    private final String sittingBackMsg = "Sitting back";
    private final String stopMsg = "stop";
    private SimpleStepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel, magnetic;
    private CountDownTimer cTimer, sitTimer, standTimer, walkTimer, walkBackTimer, turnTimer, sitBackTimer;
    private int numSteps = 0;
    private String currMotion;
    private TextView stepText, message;

    private TextView deltaText;
    ImageView stand, walk, sit, turn, walkBack, sitBack;
    private ImageView arrowView;
    private Button reset;
    private Button stop, start;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float currectAzimuth = 0;
    private float lastAzimuth = 0;
    private long turningTime = 0;
    private Boolean walkOneSecond = false;

    private static final float grav[] = new float[3]; // Gravity (a.k.a
    // accelerometer data)
    private static final float mag[] = new float[3]; // Magnetic
    private static final float rotation[] = new float[9]; // Rotation matrix in
    // Android format
    private static final float orientation[] = new float[3]; // azimuth, pitch,
    // roll
    private static float smoothed[] = new float[3];
    private static double floatBearing = 0;

    Chronometer myChronometer;
    private boolean startMeasure = false;
    private boolean synchStartEvent = false;
    private float mAccelCurrent = 0;
    private float mAccelLast = 0;
    private long lastMotionTime = 0;
    private long lastTime;
    private Boolean turned = false;
    private List<sensorData> sensorList = new ArrayList<>();
    private List<sensorData> sensorList2 = new ArrayList<>();

    private List<eventData> standingEventList = new ArrayList<>();
    private List<eventData> sittingEventList = new ArrayList<>();
    private BecareRemoteSensorManager becareRemoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTitleBar();
        setContentView(R.layout.up_and_go);
        sit = (ImageView) findViewById(R.id.sit);
        stand = (ImageView) findViewById(R.id.stand);
        walk = (ImageView) findViewById(R.id.walk);
        turn = (ImageView) findViewById(R.id.turn);
        walkBack = (ImageView) findViewById(R.id.walk_back);
        sitBack = (ImageView) findViewById(R.id.sit_back);

        arrowView = (ImageView) findViewById(R.id.main_image_hands);

        sit.setTag("green");
        stand.setTag("purple");
        walk.setTag("purple");
        turn.setTag("purple");
        walkBack.setTag("purple");
        sitBack.setTag("purple");
        //   qImageView.setImageResource(R.drawable.sit_green);
        stepText = (TextView) findViewById(R.id.steps);

        deltaText = (TextView) findViewById(R.id.delta);
        message = (TextView) findViewById(R.id.message);
        myChronometer = (Chronometer) findViewById(R.id.chronometer);


        reset = (Button) findViewById(R.id.reset_button);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* myChronometer.stop();
                startMeasure = false;
                showMessage("Stopped");
                standingEventList.clear();
                sittingEventList.clear();*/
                stopApp();
                //   resetImageColor();
            }

        });

        start = (Button) findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numSteps = 0;
               // startMeasure = true;

                //    Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
                currMotion = sittingMsg;
                stepText.setText("0");
                deltaText.setText(currMotion);
                lastTime = SystemClock.elapsedRealtime();
                myChronometer.setBase(SystemClock.elapsedRealtime());
                myChronometer.start();
                lastAzimuth = 0;
                synchStartEvent = true;
                resetImageColor();
                showMessage("Started");
                standingEventList.clear();
                sittingEventList.clear();
                sitTimer.start();
                turned = false;
                walkOneSecond = false;
            }
        });

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        simpleStepDetector = new SimpleStepDetector();
        simpleStepDetector.registerListener(this);
        becareRemoteSensorManager = BecareRemoteSensorManager.getInstance(UpAndGoActivity.this);

        lastMotionTime = -0xffffff;

        currMotion = sittingMsg;
        resetImageColor();
        deltaText.setText(currMotion);
        //  lastTime = SystemClock.elapsedRealtime();

        //  new AsyncTaskRunner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        int corePoolSize = 60;
        int maximumPoolSize = 80;
        int keepAliveTime = 10;

        //  BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(maximumPoolSize);
        //  Executor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
        //   new AsyncTaskRunner().executeOnExecutor(threadPoolExecutor);

        cTimer = new CountDownTimer(3000000, 700) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                long dur = SystemClock.elapsedRealtime() - lastTime;
                if (dur > 700 && startMeasure && (currMotion.equals(raisingMsg) || currMotion.equals(sittingBackMsg))) {
                    //   sendStopMsg(dur);
                    //  lastTime =  SystemClock.elapsedRealtime();
                    //  deltaText.setText("");
                }

            }

            public void onFinish() {
                //   startMeasure = false;

            }
        }.start();

        sitTimer = new CountDownTimer(300, 300) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {//   startMeasure = false;
                lastTime = SystemClock.elapsedRealtime();
                currMotion = raisingMsg;
                startMeasure = true;
            }
        };

        standTimer = new CountDownTimer(900, 900) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                float sec = (int) (millisUntilFinished / 1000);
            }

            public void onFinish() {//   startMeasure = false;

                long dur;
                dur = SystemClock.elapsedRealtime() - lastTime;
                lastTime = SystemClock.elapsedRealtime();
                String durStr = String.format("%d", dur);

                LinkedHashMap dictionary = new LinkedHashMap();
                dictionary.put("activityname", getString(R.string.up_and_go));
                dictionary.put("motion", raisingMsg);
                dictionary.put("dur (ms)", durStr);
                becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);

                currMotion = walkingMsg;
                walkTimer.start();
            }
        };

        walkTimer = new CountDownTimer(1400, 1000) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                float sec = (int) (millisUntilFinished / 1000);

            }

            public void onFinish() {//   startMeasure = false;
                walkOneSecond = true;
            }
        };

        walkBackTimer = new CountDownTimer(400, 400) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                float sec = (int) (millisUntilFinished / 1000);

            }

            public void onFinish() {//   startMeasure = false;
                currMotion = walkingBackMsg;
            }
        };

        turnTimer = new CountDownTimer(800, 800) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {//   startMeasure = false;
                long dur;
                dur = SystemClock.elapsedRealtime() - lastTime;
                lastTime = SystemClock.elapsedRealtime();
                String durStr = String.format("%d", dur);

                LinkedHashMap dictionary = new LinkedHashMap();
                dictionary.put("activityname", getString(R.string.up_and_go));
                dictionary.put("motion", turningMsg);
                dictionary.put("dur (ms)", durStr);
                becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);

                currMotion = walkingBackMsg;
            }
        };

        sitBackTimer = new CountDownTimer(300, 300) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {//   startMeasure = false;
                currMotion = sittingBackMsg;

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        numSteps = 0;

        stepText.setText("0");
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        startMeasure = false;
        uploadEnd();
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
        if (!startMeasure)
            return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (currMotion.equals(raisingMsg) || currMotion.equals(walkingBackMsg)) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                mAccelLast = mAccelCurrent;
                mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);

                sensorData data = new sensorData();
                data.x = x;
                data.y = y;
                data.z = z;
                data.timestamp = event.timestamp;
                data.accel = mAccelCurrent;

                if (sensorList.size() == SAMPLE_SIZE)
                    sensorList.remove(0);
                sensorList.add(data);

                boolean motionFound = true;
                String motion = "";
                float threshold = (currMotion.equals(raisingMsg)) ? STAND_THRESHOLD : SIT_THRESHOLD;

                if (sensorList.size() == SAMPLE_SIZE) {
                    for (int i = 0; i < SAMPLE_SIZE; i++) {
                        if (sensorList.get(i).accel < threshold) {
                            motionFound = false;
                            break;
                        }
                        // motion += String.format("%.5f,", sensorList.get(i).accel);
                        //    motion = motionMsg;
                        lastMotionTime = System.currentTimeMillis();
                    }
                    //clear samples
                    if (motionFound) {
                        sensorList.clear();
                    }
                } else
                    motionFound = false;

                if (motionFound == true) {
                    if (currMotion.equals(raisingMsg)) {
                        String screenMotion = deltaText.getText().toString();
                        motion = raisingMsg;
                        eventData d = new eventData();
                        d.name = raisingMsg;
                        d.timestamp = SystemClock.elapsedRealtime();
                        standingEventList.add(d);
                        if (screenMotion.equals(raisingMsg))
                            return;
                        deltaText.setText(motion);
                        setImageColor(raisingMsg);
                        standTimer.start();

                    } else if (currMotion == walkingBackMsg) {
                        eventData d = new eventData();
                        d.name = sittingBackMsg;
                        d.timestamp = SystemClock.elapsedRealtime();

                        if (sittingEventList.size() >= 1)
                            if (SystemClock.elapsedRealtime() - sittingEventList.get(sittingEventList.size() - 1).timestamp > 200) {
                                sittingEventList.clear();
                                sittingEventList.add(d);
                                return;
                            }

                        sittingEventList.add(d);

                        if (!turned)
                            return;

                        motion = sittingBackMsg;
                        currMotion = sittingBackMsg;
                        deltaText.setText(motion);

                        long startTime = sittingEventList.get(0).timestamp;
                        setImageColor(sittingBackMsg);
                        showMessage("You have finished the test. You can repeat.");
                        myChronometer.stop();
                        startMeasure = false;
                        //   sendMsg("event length", sittingEventList.size());
                        long dur;
                        dur = SystemClock.elapsedRealtime() - lastTime + 700;
                        lastTime = SystemClock.elapsedRealtime();
                        String durStr = String.format("%d", dur);

                        LinkedHashMap dictionary = new LinkedHashMap();
                        dictionary.put("activityname", getString(R.string.up_and_go));
                        dictionary.put("motion", motion);
                        dictionary.put("dur (ms)", durStr);
                        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);
                    } else
                        return;
/*
                    long elapsedMillis = SystemClock.elapsedRealtime() - myChronometer.getBase();
                    long dur;
                    dur = SystemClock.elapsedRealtime() - lastTime;
                    lastTime = SystemClock.elapsedRealtime();
                    String durStr = String.format("%d", dur);

                    Hashtable dictionary = new Hashtable();
                    dictionary.put("activityname", getString(R.string.up_and_go));
                    dictionary.put("dur (ms)", durStr);
                    dictionary.put("motion", motion);
                    becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);
                    setImageColor(motion);
                    deltaText.setText(currMotion);
*/
                    return;
                }
            }


            if (currMotion.equals(raisingMsg) && standingEventList.size() == 0)
                return;

            simpleStepDetector.updateAccel(event.timestamp, event.values[0], event.values[1], event.values[2]);
        }

        Boolean isTurning = detectTurning(event);

      //  if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !isTurning)
       //     if (currMotion.equals(walkingMsg) || currMotion.equals(walkingBackMsg))
       //         simpleStepDetector.updateAccel(event.timestamp, event.values[0], event.values[1], event.values[2]);
    }

    private Boolean detectTurning(SensorEvent event) {
        readAzimuth(event);

        if (lastAzimuth == 0)
            lastAzimuth = azimuth;

        if (Math.abs(azimuth - lastAzimuth) > 120) {
            if (!currMotion.equals(walkingMsg))
                return false;

            if ( currMotion.equals(walkingMsg) && !walkOneSecond ) {

                stopApp();
                showMessage("You need to walk at least one second.");
                return true;
            }
            //turn only on walking forward
            currMotion = turningMsg;
            setImageColor(turningMsg);
            deltaText.setText(turningMsg);

            turningTime = SystemClock.elapsedRealtime();
            long dur = turningTime - lastTime;
            lastAzimuth = azimuth;
            //   lastTime = SystemClock.elapsedRealtime();
            //  sendMsg(turningMsg, dur);
            turnTimer.start();
            return true;
        } else if (Math.abs(azimuth - lastAzimuth) > 80) {
            if (!currMotion.equals(walkingBackMsg))
                return false;

            //turn only on walking forward
            //     setImageColor(turningMsg);
          //  deltaText.setText(turningMsg);
            turned = true;
            turningTime = SystemClock.elapsedRealtime();
            long dur = turningTime - lastTime;
            float diff = azimuth - lastAzimuth;
            lastAzimuth = azimuth;
            //   lastTime = SystemClock.elapsedRealtime();

            // dur = (long)diff;
            //  sendMsg(turningMsg, dur);
            return false;
        }
       /*     else if ( dur <= 700 && turningTime>0)
            {
                currMotion = turningMsg;
                setImageBlue(turningMsg);
                deltaText.setText(turningMsg);
                lastAzimuth = azimuth;
                sendMsg(turningMsg, System.currentTimeMillis() - lastTime);
            }*/
        return false;
        // adjustArrow();
    }

    private void stopApp(){
        myChronometer.stop();
        startMeasure = false;
        showMessage("Stopped");
        standingEventList.clear();
        sittingEventList.clear();
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
        title.setText(R.string.up_and_go);

        ImageView back = (ImageView)customActionBar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getTimedWalk(UpAndGoActivity.this);
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();
            }
        });

        ImageView next = (ImageView)customActionBar.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getStroop(UpAndGoActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });


        ImageView home = (ImageView) customActionBar.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.getParentActivityIntent(UpAndGoActivity.this);
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

    private void readAzimuth(SensorEvent event) {

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                smoothed = filter(event.values, grav, (float)0.1);
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
             //   adjustArrow();


            }
        }

    }

    private void adjustArrow() {

        Animation an = new RotateAnimation(-currectAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currectAzimuth = azimuth;

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        arrowView.startAnimation(an);
    }

    private void sendMsg(String motion, long dur)
    {
        String durStr = String.format("%d", dur);

        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("activityname", getString(R.string.up_and_go));
        dictionary.put("motion", motion);
        dictionary.put("dur (ms)", durStr);

        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);
    }

    @Override
    public void step(long timeNs) {

        if (!startMeasure)
            return;

        if (!currMotion.equals(walkingMsg) && !currMotion.equals(walkingBackMsg))
            return;

        long elapsedMillis = SystemClock.elapsedRealtime() - myChronometer.getBase();
        long dur = SystemClock.elapsedRealtime() - lastTime;
        if (dur < 200)
            return;

        lastTime = SystemClock.elapsedRealtime();
        numSteps++;

        String str =String.format("%d", numSteps);
        stepText.setText(str);
        deltaText.setText(currMotion);

        String durStr = String.format("%d", dur);
        String stepsStr = String.format("%d", numSteps);
        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("activityname", getString(R.string.up_and_go));
        dictionary.put("motion", currMotion);
        dictionary.put("step num", stepsStr);
        dictionary.put("dur (ms)", durStr);
        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);
        setImageColor(currMotion);
    }

    private void sendStopMsg(long dur)
    {
        deltaText.setText("stop");

        String durStr = String.format("%d", dur);
        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("activityname", getString(R.string.up_and_go));
        dictionary.put("motion", "stop");
        dictionary.put("dur (ms)", durStr);
        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);
      //  lastTime = SystemClock.elapsedRealtime();

        setImageColor(stopMsg);
     /*   if (!stand.getTag().equals("green")) {
            stand.setImageResource(R.drawable.stand_green);
            stand.setTag("green");
        }
        if (sit.getTag().equals("green")) {
            sit.setImageResource(R.drawable.sit);
            sit.setTag("purple");
        }
        if (walk.getTag().equals("green")) {
            walk.setImageResource(R.drawable.walking);
            walk.setTag("purple");
        }*/
    }

    void setImageColor(String motion)
    {
        if (motion.equals(sittingMsg) ){
            if (!sit.getTag().equals("green")) {
                sit.setImageResource(R.drawable.sit_green);
                sit.setTag("green");
            }
            if (stand.getTag().equals("green")) {
                stand.setImageResource(R.drawable.stand);
                stand.setTag("purple");
            }
            if (walk.getTag().equals("green")) {
                walk.setImageResource(R.drawable.walking);
                walk.setTag("purple");
            }
            if (turn.getTag().equals("green")) {
                turn.setImageResource(R.drawable.return1);
                turn.setTag("purple");
            }
            if (walkBack.getTag().equals("green")) {
                walkBack.setImageResource(R.drawable.walking_back);
                walkBack.setTag("purple");
            }
            if (sitBack.getTag().equals("green")) {
                sitBack.setImageResource(R.drawable.sit);
                sitBack.setTag("purple");
            }
        }
        if (motion.equals(raisingMsg)) {
            if (!stand.getTag().equals("green")) {
                stand.setImageResource(R.drawable.stand_green);
                stand.setTag("green");
            }
            if (sit.getTag().equals("green")) {
                sit.setImageResource(R.drawable.sit);
                sit.setTag("purple");
            }
            if (walk.getTag().equals("green")) {
                walk.setImageResource(R.drawable.walking);
                walk.setTag("purple");
            }
            if (turn.getTag().equals("green")) {
                turn.setImageResource(R.drawable.return1);
                turn.setTag("purple");
            }
            if (walkBack.getTag().equals("green")) {
                walkBack.setImageResource(R.drawable.walking_back);
                walkBack.setTag("purple");
            }
            if (sitBack.getTag().equals("green")) {
                sitBack.setImageResource(R.drawable.sit);
                sitBack.setTag("purple");
            }
        }

        if (motion.equals(walkingMsg)) {
            if (stand.getTag().equals("green")) {
                stand.setImageResource(R.drawable.stand);
                stand.setTag("purple");
            }
            if (sit.getTag().equals("green")) {
                sit.setImageResource(R.drawable.sit);
                sit.setTag("purple");
            }
            if (turn.getTag().equals("green")) {
                turn.setImageResource(R.drawable.return1);
                turn.setTag("purple");
            }
            if (walkBack.getTag().equals("green")) {
                walkBack.setImageResource(R.drawable.walking_back);
                walkBack.setTag("purple");
            }
            if (sitBack.getTag().equals("green")) {
                sitBack.setImageResource(R.drawable.sit);
                sitBack.setTag("purple");
            }
            if (!walk.getTag().equals("green")) {
                walk.setImageResource(R.drawable.walk_green);
                walk.setTag("green");
            }

        }

        if (motion.equals(turningMsg)) {
            if (stand.getTag().equals("green")) {
                stand.setImageResource(R.drawable.stand);
                stand.setTag("purple");
            }
            if (sit.getTag().equals("green")) {
                sit.setImageResource(R.drawable.sit);
                sit.setTag("purple");
            }
            if (walk.getTag().equals("green")) {
                walk.setImageResource(R.drawable.walking);
                walk.setTag("purple");
            }
            if (walkBack.getTag().equals("green")) {
                walkBack.setImageResource(R.drawable.walking_back);
                walkBack.setTag("purple");
            }
            if (sitBack.getTag().equals("green")) {
                sitBack.setImageResource(R.drawable.sit);
                sitBack.setTag("purple");
            }
            if (!turn.getTag().equals("green")) {
                turn.setImageResource(R.drawable.return1_green);
                turn.setTag("green");
            }
        }
        if (motion.equals(walkingBackMsg)) {
            if (stand.getTag().equals("green")) {
                stand.setImageResource(R.drawable.stand);
                stand.setTag("purple");
            }
            if (sit.getTag().equals("green")) {
                sit.setImageResource(R.drawable.sit);
                sit.setTag("purple");
            }
            if (walk.getTag().equals("green")) {
                walk.setImageResource(R.drawable.walking);
                walk.setTag("purple");
            }
            if (!walkBack.getTag().equals("green")) {
                walkBack.setImageResource(R.drawable.walking_back_green);
                walkBack.setTag("green");
            }
            if (sitBack.getTag().equals("green")) {
                sitBack.setImageResource(R.drawable.sit);
                sitBack.setTag("purple");
            }
            if (turn.getTag().equals("green")) {
                turn.setImageResource(R.drawable.return1);
                turn.setTag("purple");
            }
        }
        if (motion.equals(sittingBackMsg)) {
            if (stand.getTag().equals("green")) {
                stand.setImageResource(R.drawable.stand);
                stand.setTag("purple");
            }
            if (sit.getTag().equals("green")) {
                sit.setImageResource(R.drawable.sit);
                sit.setTag("purple");
            }
            if (walk.getTag().equals("green")) {
                walk.setImageResource(R.drawable.walking);
                walk.setTag("purple");
            }
            if (walkBack.getTag().equals("green")) {
                walkBack.setImageResource(R.drawable.walking_back);
                walkBack.setTag("purple");
            }
            if (!sitBack.getTag().equals("green")) {
                sitBack.setImageResource(R.drawable.sit_green);
                sitBack.setTag("green");
            }
            if (turn.getTag().equals("green")) {
                turn.setImageResource(R.drawable.return1);
                turn.setTag("purple");
            }
        }
    }

    void resetImageColor()
    {
        sit.setImageResource(R.drawable.sit_green);
        sit.setTag("green");

        stand.setImageResource(R.drawable.stand);
        stand.setTag("purple");

        walk.setImageResource(R.drawable.walking);
        walk.setTag("purple");

        turn.setImageResource(R.drawable.return1);
        turn.setTag("purple");

        walkBack.setImageResource(R.drawable.walking_back);
        walkBack.setTag("purple");

        sitBack.setImageResource(R.drawable.sit);
        sitBack.setTag("purple");

    }

    private void showMessage(String msg)
    {
        message.setText(msg);
        if (!msg.equals(""))
            message.setBackgroundColor(Color.rgb(255, 200,50));
        else
            message.setBackgroundColor(Color.rgb(222, 220,222));
    }

    private void uploadEnd(){
        long readTime = System.currentTimeMillis();
        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("endactivity", getString(R.string.up_and_go));
        dictionary.put("user_id", becareRemoteSensorManager.getPreferenceStorage().getUserId());
        dictionary.put("session_token", becareRemoteSensorManager.getPreferenceStorage().getUserId() +"_" + readTime);
        dictionary.put("date", DateUtils.formatDate(readTime));
        dictionary.put("time", DateUtils.formatTime(readTime));

        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);

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

    public class eventData{
        public String name;
        public long timestamp;
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {

        TextView txtView = (TextView)findViewById(R.id.delta);

        @Override
        protected Void doInBackground(Void... param) {

/*
            try {

                  Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();

            }*/
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            try {

                    txtView.setText("stop");
                    Thread.sleep(100);

            } catch (InterruptedException e) {
                e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();

            }

        }


        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onProgressUpdate(Void... test) {


        }
    }
}



