package com.github.pocmo.sensordashboard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.github.pocmo.sensordashboard.R;

/**
 * Created by neerajpaliwal on 12/07/16.
 */
public class TimedWalkedActivity extends AppCompatActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setContentView(new BallBounce(this));
     //   requestWindowFeature(Window.FEATURE_NO_TITLE);
        //set up full screen
     /*   getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        setContentView(R.layout.activity_timed_walk);
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.walkingLayout);
        rl.setBackgroundResource(R.drawable.robot);
        //setHasOptionsMenu(true);

    }

    public void getTwentyFiveStep(View view) {
        Intent intent = new Intent(this, TwentyFiveStepsActivity.class);
        startActivity(intent);

    }

    public void getSixMinutes(View view) {
        Intent intent = new Intent(this, SixMinutesActivity.class);
        startActivity(intent);

    }

    public void getUpAndGo(View view) {
        Intent intent = new Intent(this, UpAndGoActivity.class);
        startActivity(intent);

    }
}
