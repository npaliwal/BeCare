package com.github.pocmo.sensordashboard.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.ui.BallBounces;


/**
 * Created by neerajpaliwal on 01/07/16.
 */
public class SnookerActivity extends AppCompatActivity {

    BallBounces ball;
    TextView pathBuilderButton;
    private BecareRemoteSensorManager mRemoteSensorManager;
    private PreferenceStorage preferenceStorage;
    private boolean addingPathPoints = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTitleBar();
        setContentView(R.layout.activity_snooker);
        preferenceStorage = new PreferenceStorage(SnookerActivity.this);
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(SnookerActivity.this);
        mRemoteSensorManager.setSnookerSeq(0);

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
        title.setText(R.string.exercise_ring_rect);

        ImageView back = (ImageView)customActionBar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getArmElevation(SnookerActivity.this);
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();
            }
        });

        ImageView next = (ImageView)customActionBar.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getTranscription(SnookerActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        ab.setCustomView(customActionBar, layout);
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
