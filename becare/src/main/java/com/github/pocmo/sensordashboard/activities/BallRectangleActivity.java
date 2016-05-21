package com.github.pocmo.sensordashboard.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.model.ActivityData;
import com.github.pocmo.sensordashboard.model.SnookerData;


/**
 * Created by neerajpaliwal on 27/04/16.
 */
public class BallRectangleActivity extends AppCompatActivity {
    private static final String TAG = "BallRectangle";

    private TextView deviationText;
    private SnookerData data = new SnookerData();

    private BecareRemoteSensorManager mRemoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ball_rect);
        deviationText = (TextView) findViewById(R.id.deviation);
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(BallRectangleActivity.this);
    }

    public void setDeviationText(int yTouch, int xTouch, int xPath){
        String dataShow = "yTouch: "+yTouch + ", xTouch: " + xTouch + ", xPath: " + xPath;
        deviationText.setText(dataShow);
        data.setValues(xTouch, yTouch, xPath);
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRemoteSensorManager.startMeasurement();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(null);
        mRemoteSensorManager.stopMeasurement();
    }
}
