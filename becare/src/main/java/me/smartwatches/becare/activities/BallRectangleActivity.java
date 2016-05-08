package me.smartwatches.becare.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.RemoteSensorManager;

import java.util.ArrayList;
import java.util.List;

import me.smartwatches.becare.R;
import me.smartwatches.becare.ui.PathSegment;
import me.smartwatches.becare.ui.RingArea;

/**
 * Created by neerajpaliwal on 27/04/16.
 */
public class BallRectangleActivity extends AppCompatActivity {
    private static final String TAG = "BallRectangle";

    private TextView deviationText;
    private RemoteSensorManager mRemoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ball_rect);
        deviationText = (TextView) findViewById(R.id.deviation);
        mRemoteSensorManager = RemoteSensorManager.getInstance(BallRectangleActivity.this);

    }

    public void setDeviationText(int deviation){
        deviationText.setText("Deviation : " + deviation);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
