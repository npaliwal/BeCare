package com.github.pocmo.sensordashboard.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;

import org.json.JSONObject;


/**
 * Created by neerajpaliwal on 27/04/16.
 */
public class BallRectangleActivity extends AppCompatActivity {
    private static final String TAG = "BallRectangle";

    private TextView deviationText;

    private BecareRemoteSensorManager mRemoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ball_rect);
        deviationText = (TextView) findViewById(R.id.deviation);
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(BallRectangleActivity.this);
    }

    public void setDeviationText(int y, int deviation){
        deviationText.setText("Deviation : " + deviation);
        String data = "{\"yTouch\":"+ y +", \"xDeviation\":" + deviation + "}";
        mRemoteSensorManager.uploadActivityData(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRemoteSensorManager.getUploadData().setUserActivity("Snooker");
        mRemoteSensorManager.startMeasurement();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mRemoteSensorManager.stopMeasurement();
    }
}
