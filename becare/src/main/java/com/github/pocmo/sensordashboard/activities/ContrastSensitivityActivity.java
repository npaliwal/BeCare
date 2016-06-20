package com.github.pocmo.sensordashboard.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.model.TwoImageInfo;
import com.github.pocmo.sensordashboard.ui.TwoImageFragment;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by neerajpaliwal on 27/04/16.
 */
public class ContrastSensitivityActivity extends AppCompatActivity {
    private static final String TAG = "ContrastSensitivity";

    private TextView performanceText, matchButton, mismatchButton, done;
    private View responseContainer;
    private ViewPager pager;
    FragmentPagerAdapter adapterViewPager;
    private String value = null;

    private int numExercises = 0;
    private int correctCount = 0;
    public ArrayList<TwoImageInfo> exercises = new ArrayList<>();
    private BecareRemoteSensorManager mRemoteSensorManager;
    private PreferenceStorage preferenceStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contrast_sensitivity);
        performanceText = (TextView) findViewById(R.id.tv_performance);
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(ContrastSensitivityActivity.this);
        preferenceStorage = new PreferenceStorage(ContrastSensitivityActivity.this);
        AppConfig.initContrastExercises();
        initExercises();
        initViewPager();
        initButtons();
    }

    public void setPerformance(int total){
        String dataShow = getString(R.string.contrast_performance, correctCount, total);
        performanceText.setText(dataShow);
        value = "";//TODO: Info about image patch and user response
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(getString(R.string.exercise_contrast), value);
    }

    private void setupNextPage(int index){
        setPerformance(index + 1);
        if(index >= exercises.size() - 1){
            responseContainer.setVisibility(View.GONE);
            pager.setVisibility(View.GONE);
            done.setVisibility(View.VISIBLE);
        }else {
            pager.setCurrentItem(index + 1, true);
        }
    }

    private void initButtons(){
        responseContainer = findViewById(R.id.response_container);
        matchButton = (TextView) responseContainer.findViewById(R.id.tv_similar);
        mismatchButton = (TextView) responseContainer.findViewById(R.id.tv_different);
        done = (TextView) findViewById(R.id.tv_done);

        matchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pager.getCurrentItem();
                TwoImageInfo exercise = exercises.get(index);
                if(exercise.isSimilar()){
                    correctCount++;
                }
                setupNextPage(index);
            }
        });
        mismatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pager.getCurrentItem();
                TwoImageInfo exercise = exercises.get(index);
                if(!exercise.isSimilar()){
                    correctCount++;
                }
                setupNextPage(index);
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initExercises(){
        exercises.clear();
        numExercises = preferenceStorage.getNumContrastExercise();

        int temp = 0, currId;
        TwoImageInfo currExercise = null;
        Random random = new Random();
        while(temp < numExercises){
            currId = random.nextInt(AppConfig.CONTRAST_EXERCISES.size());
            currExercise = AppConfig.CONTRAST_EXERCISES.get(currId);
            if(!exercises.contains(currExercise)){
                exercises.add(currExercise);
                temp++;
            }
        }
    }

    private void initViewPager(){
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), exercises);
        pager.setAdapter(adapterViewPager);
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

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        ArrayList<TwoImageInfo> exercises = null;

        public MyPagerAdapter(FragmentManager fragmentManager, ArrayList<TwoImageInfo> exercises) {
            super(fragmentManager);
            this.exercises = exercises;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return exercises.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return TwoImageFragment.newInstance(exercises.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }

}
