package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.ui.BallBounces;
import com.github.pocmo.sensordashboard.ui.PathSegment;
import com.github.pocmo.sensordashboard.ui.RingArea;
import com.github.pocmo.sensordashboard.utils.FileUtils;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neerajpaliwal on 01/07/16.
 */
public class SnookerActivity extends Activity {

    BallBounces ball;
    TextView pathBuilderButton;
    private BecareRemoteSensorManager mRemoteSensorManager;
    private PreferenceStorage preferenceStorage;
    private boolean addingPathPoints = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snooker);
        preferenceStorage = new PreferenceStorage(SnookerActivity.this);
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(SnookerActivity.this);

        ball = (BallBounces)findViewById(R.id.arena);
        pathBuilderButton = (TextView)findViewById(R.id.tv_path_build);
        pathBuilderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addingPathPoints){
                    addingPathPoints= false;
                    pathBuilderButton.setText("Create New Path");
                    ball.setPathBuildMode(1);
                }else {
                    addingPathPoints = true;
                    pathBuilderButton.setText("Save");
                    ball.setPathBuildMode(2);
                }
            }
        });

        boolean pathBuilder = preferenceStorage.isSnookerPathBuildMode();
        if(pathBuilder){
            pathBuilderButton.setVisibility(View.VISIBLE);
        }
        ball.setPathBuildMode(pathBuilder? 1: 0);
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
