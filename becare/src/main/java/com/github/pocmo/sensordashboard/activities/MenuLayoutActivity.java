package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.pocmo.sensordashboard.R;

/**
 * Created by qtxdev on 7/13/2016.
 */
public class MenuLayoutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu_layout);
    }

    public void getArmElevation(View view) {
        Intent intent = new Intent(this, ArmElevationActivity.class);
        startActivity(intent);

    }

    public void getSnooker(View view) {
        Intent intent = new Intent(this, SnookerActivity.class);
        startActivity(intent);

    }

    public void getTranscription(View view) {
        Intent intent = new Intent(this, TranscriptionTestActivity.class);
        startActivity(intent);

    }

    public void getContrast(View view) {
        Intent intent = new Intent(this, ContrastSensitivityActivity.class);
        startActivity(intent);

    }

    public void getTimedWalk(View view) {
        Intent intent = new Intent(this, TimedWalkedActivity.class);
        startActivity(intent);
    }

    public void getTools(View view) {
        Intent intent = new Intent(this, UtilityActivity.class);
        startActivity(intent);
    }
}
