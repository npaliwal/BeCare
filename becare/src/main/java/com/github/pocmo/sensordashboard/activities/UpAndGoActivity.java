package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by qtxdev on 7/5/2016.
 */
public class UpAndGoActivity extends Activity implements SensorEventListener, StepListener {
    private static final float THRESHOLD = (float)11.7;
    private static final float THRESHOLD2 = (float)11.2;
    private static final int SAMPLE_SIZE = 5;
    private static final int SAMPLE_SIZE2 = 5;
    private final String raisingMsg = "Raising";
    private final String sittingMsg = "Sitting";
    private final String walkingMsg = "Walking";
    private SimpleStepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private CountDownTimer cTimer = null;
    private int numSteps = 0;

    private TextView stepText;

    private TextView deltaText;

    private Button start;
    private Button  stop;

    Chronometer myChronometer;
    private boolean startMeasure = false;
    private boolean synchStartEvent = false;
    private float mAccelCurrent = 0;
    private float mAccelLast = 0;
    private long lastMotionTime = 0;
    private long  lastTime;
    private long haltStart = 0;
    private List<sensorData> sensorList = new ArrayList<>();
    private List<sensorData> sensorList2 = new ArrayList<>();
    private List<eventData> eventList = new ArrayList<>();
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
                synchStartEvent = true;
                Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
                stepText.setText("0");
                deltaText.setText("");
                lastTime = SystemClock.elapsedRealtime();
                myChronometer.setBase(SystemClock.elapsedRealtime());
                myChronometer.start();
              //  cTimer.start();
            }
        });

        stop = (Button)findViewById(R.id.stop_button);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasure = false;
                eventList.clear();
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

      //  new AsyncTaskRunner().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        int corePoolSize = 60;
        int maximumPoolSize = 80;
        int keepAliveTime = 10;

      //  BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(maximumPoolSize);
      //  Executor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
     //   new AsyncTaskRunner().executeOnExecutor(threadPoolExecutor);

        cTimer = new CountDownTimer(3000000, 500) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                long dur = SystemClock.elapsedRealtime() - lastTime;
               if ( dur > 500)
                   if (startMeasure && !synchStartEvent)
                       sendStopMsg(dur);

            }

            public void onFinish() {
             //   startMeasure = false;

            }
        }.start();
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
            if (sensorList.size() == SAMPLE_SIZE) {
                for (int i = 0; i < SAMPLE_SIZE; i++) {
                    if (sensorList.get(i).accel < THRESHOLD) {
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
                if (synchStartEvent) {
                    motion = raisingMsg;
                    deltaText.setText(motion);
                    eventData d = new eventData();
                    d.name = raisingMsg;
                    d.timestamp = SystemClock.elapsedRealtime();
                    eventList.add(d);
                } else {
                    motion = sittingMsg;
                    String delta = deltaText.getText().toString();
                    if (delta == walkingMsg) {
                        for (int i = eventList.size()-1; i >= 0; i--) {
                            long dur = SystemClock.elapsedRealtime() - eventList.get(i).timestamp;
                            if (dur < 1000)
                                return;

                        }
                        deltaText.setText(motion);
                        eventData d = new eventData();
                        d.name = sittingMsg;
                        d.timestamp = SystemClock.elapsedRealtime();
                        eventList.add(d);
                    } else {
                        //already in motion
                        long now = SystemClock.elapsedRealtime();
                        long dur = now - lastTime;
                        lastTime = now;

                        return;
                    }
                }


                long elapsedMillis = SystemClock.elapsedRealtime() - myChronometer.getBase();
                long dur;
                dur = SystemClock.elapsedRealtime() - lastTime;
                lastTime = SystemClock.elapsedRealtime();
                String durStr = String.format("%d", dur);

                if (motion.equals(raisingMsg))
                    durStr = "0";
                Hashtable dictionary = new Hashtable();
                dictionary.put("activityname", getString(R.string.up_and_go));
                dictionary.put("dur (ms)", durStr);
                dictionary.put("motion", motion);
                becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);
                haltStart = lastTime;

               if (synchStartEvent) {
                   synchStartEvent = false;
                 //  haltStart = SystemClock.elapsedRealtime();
               }

               return;
            }

            if (synchStartEvent)
                return;

            String step =stepText.getText().toString();
            if (step.equals("0") && (SystemClock.elapsedRealtime() - lastTime) < 350)
                return;

           Boolean foundStep =simpleStepDetector.updateAccel2(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
            if (foundStep)
                processStep();

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

    }


    private void processStep() {
        if (!startMeasure)
            return;

        long elapsedMillis = SystemClock.elapsedRealtime() - myChronometer.getBase();
        long dur = SystemClock.elapsedRealtime() - lastTime;
        if (dur < 200)
            return;

        lastTime = SystemClock.elapsedRealtime();
        numSteps++;

        String str =String.format("%d", numSteps);
        stepText.setText(str);
        deltaText.setText(walkingMsg);

        String durStr = String.format("%d", dur);
        String stepsStr = String.format("%d", numSteps);
        Hashtable dictionary = new Hashtable();
        dictionary.put("activityname", getString(R.string.up_and_go));
        dictionary.put("dur (ms)", durStr);
        dictionary.put("motion", walkingMsg);
        dictionary.put("step num", stepsStr);
        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);

     //   eventData d = new eventData();
     //   d.name = walkingMsg;
   //     d.timestamp = SystemClock.elapsedRealtime();
    //    eventList.add(d);
    }

    private void sendStopMsg(long dur)
    {
        deltaText.setText("stop");

        String durStr = String.format("%d", dur);
        Hashtable dictionary = new Hashtable();
        dictionary.put("activityname", getString(R.string.up_and_go));
        dictionary.put("dur (ms)", durStr);
        dictionary.put("motion", "stop");
        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);
        lastTime = SystemClock.elapsedRealtime();
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



