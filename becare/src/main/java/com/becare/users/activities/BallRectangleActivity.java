package com.becare.users.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.becare.users.BecareRemoteSensorManager;
import com.becare.users.R;


/**
 * Created by neerajpaliwal on 27/04/16.
 */
public class BallRectangleActivity extends AppCompatActivity {
    private static final String TAG = "BallRectangle";

    private TextView deviationText;
    private String value = null;

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
        value = "("+xPath + "," + yTouch + ") (" + xTouch + "," + yTouch +")";
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(getString(R.string.exercise_ring_rect), value);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRemoteSensorManager.startMeasurement();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(null, null);
        mRemoteSensorManager.stopMeasurement();
    }
}
