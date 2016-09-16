package com.github.pocmo.sensordashboard.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.model.ContrastImageInfo;
import com.github.pocmo.sensordashboard.model.TwoImageInfo;
import com.github.pocmo.sensordashboard.ui.InstructionView;
import com.github.pocmo.sensordashboard.ui.TwoImageFragment;
import com.github.pocmo.sensordashboard.utils.DateUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;


/**
 * Created by neerajpaliwal on 27/04/16.
 */
public class ContrastSensitivityActivity extends AppCompatActivity {
    private static final String TAG = "ContrastSensitivity";

    private TextView performanceText, matchButton, mismatchButton, status;
    private View responseContainer;
    InstructionView message;
    private ViewPager pager;
    private View pagerContainer;
    FragmentPagerAdapter adapterViewPager;
    private int seq = 0;
    private int currentExercise = -1;
    private long prevTime =0;
    private int correctCount = 0;
    public ArrayList<TwoImageInfo> exercises = new ArrayList<>();
    private BecareRemoteSensorManager mRemoteSensorManager;
    private PreferenceStorage preferenceStorage;
    private CountDownTimer cTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTitleBar();
        setContentView(R.layout.activity_contrast_sensitivity);
        performanceText = (TextView) findViewById(R.id.tv_performance);
        pagerContainer = findViewById(R.id.pager_containter);
        message = (InstructionView)findViewById(R.id.msg);
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(ContrastSensitivityActivity.this);
        preferenceStorage = new PreferenceStorage(ContrastSensitivityActivity.this);
        AppConfig.initContrastExercises();
        initExercises();
        initViewPager();
        initButtons();
        initTimer();
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
        title.setText(R.string.exercise_contrast);

        ImageView back = (ImageView)customActionBar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getTranscription(ContrastSensitivityActivity.this);
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();
            }
        });

        ImageView next = (ImageView)customActionBar.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getTimedWalk(ContrastSensitivityActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ImageView home = (ImageView) customActionBar.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.getParentActivityIntent(ContrastSensitivityActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        ab.setCustomView(customActionBar, layout);
    }

    private void initTimer() {
        cTimer = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                message.setMessage(currentExercise == -1 ? "Get Ready..." : "Next...", false);
            }

            @Override
            public void onFinish() {
                if(currentExercise == exercises.size()-1){
                    message.setMessage("Congratulations!!", false);
                }else {
                    message.setMessage("Mark your response", false);
                }
                populateNextPage();
            }
        };
    }

    private void populateNextPage(){
        pagerContainer.setVisibility(View.VISIBLE);
        responseContainer.setVisibility(View.VISIBLE);

        if(currentExercise >= exercises.size() - 1){
            responseContainer.setVisibility(View.GONE);
            pager.setVisibility(View.GONE);
            status.setVisibility(View.VISIBLE);
            status.setText("Done");
        }else {
            if(currentExercise >= 0){
                pager.setCurrentItem(currentExercise + 1, true);
            }
            currentExercise++;
        }
    }

    private void uploadUserActivityData(boolean userMatch){
        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("activityname", getString(R.string.exercise_contrast));
        dictionary.put("seq", seq);
        dictionary.put("exercise_id", currentExercise);
        dictionary.put("left_color", exercises.get(currentExercise).getLeftImage().getId());
        dictionary.put("right_color", exercises.get(currentExercise).getRightImage().getId());
        dictionary.put("left_contrast", exercises.get(currentExercise).getLeftImage().getContrast());
        dictionary.put("user_response", userMatch ? "match" : "mismatch");
        long now = System.currentTimeMillis();
        long dur = (prevTime == 0)? 0: now - prevTime;
        prevTime = now;
        dictionary.put("dur (ms)", dur);
        dictionary.put("time", DateUtils.formatDateTime(now));
        mRemoteSensorManager.uploadActivityDataAsyn(dictionary);
        seq++;
    }

    private void initButtons(){
        responseContainer = findViewById(R.id.response_container);
        matchButton = (TextView) responseContainer.findViewById(R.id.tv_similar);
        mismatchButton = (TextView) responseContainer.findViewById(R.id.tv_different);
        status = (TextView) findViewById(R.id.tv_status);

        matchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                responseContainer.setClickable(false);
                TwoImageInfo exercise = exercises.get(currentExercise);
                if(exercise.getLeftImage() == exercise.getRightImage()){
                    correctCount++;
                }
                performanceText.setVisibility(View.VISIBLE);
                String dataShow = getString(R.string.contrast_performance, correctCount, currentExercise+1);
                performanceText.setText(dataShow);
                cTimer.start();
                uploadUserActivityData(true);
            }
        });
        mismatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                responseContainer.setClickable(false);
                TwoImageInfo exercise = exercises.get(currentExercise);
                if(exercise.getLeftImage() != exercise.getRightImage()){
                    correctCount++;
                }
                performanceText.setVisibility(View.VISIBLE);
                String dataShow = getString(R.string.contrast_performance, correctCount, currentExercise+1);
                performanceText.setText(dataShow);
                cTimer.start();
                uploadUserActivityData(false);
            }
        });
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status.getText().toString().equalsIgnoreCase("start")){
                    cTimer.start();
                    status.setText("STOP");
                }else if(status.getText().toString().equalsIgnoreCase("stop")) {
                    cTimer.cancel();
                    message.setMessage("You have stopped this task", true);
                    responseContainer.setVisibility(View.INVISIBLE);
                    status.setText("DONE");
                }else {
                    finish();
                }
            }
        });
    }

    private void initExercises(){
        exercises.clear();
        int numExercises = preferenceStorage.getNumContrastExercise(AppConfig.ContrastTestType.SHADES);
        int temp = 0, currId;
        ContrastImageInfo currExercise = null;
        Random random = new Random();
        while(temp < numExercises){
            currId = random.nextInt(AppConfig.CONTRAST_EXERCISES_SHADES.size());
            currExercise = AppConfig.CONTRAST_EXERCISES_SHADES.get(currId);
            if(exercises.size() <= temp){
                exercises.add(new TwoImageInfo());
                exercises.get(temp).setLeftImage(currExercise);
                if(random.nextBoolean()){
                    Log.d(TAG, "Putting same shade exercise for right as well");
                    exercises.get(temp).setRightImage(currExercise);
                    temp++;
                }
            }else{
                exercises.get(temp).setRightImage(currExercise);
                temp++;
            }
            message.setInstruction(getString(R.string.contrast_instruction));
        }

        numExercises += preferenceStorage.getNumContrastExercise(AppConfig.ContrastTestType.ITCHI_PLATE);
        while(temp < numExercises){
            currId = random.nextInt(AppConfig.CONTRAST_EXERCISES_ITCHI.size());
            currExercise = AppConfig.CONTRAST_EXERCISES_ITCHI.get(currId);
            if(exercises.size() <= temp){
                exercises.add(new TwoImageInfo());
                exercises.get(temp).setLeftImage(currExercise);
                if(random.nextBoolean()){
                    Log.d(TAG, "Putting same itchi exercise for right as well");
                    exercises.get(temp).setRightImage(currExercise);
                    temp++;
                }
            }else{
                exercises.get(temp).setRightImage(currExercise);
                temp++;
            }
        }

        numExercises += preferenceStorage.getNumContrastExercise(AppConfig.ContrastTestType.PATTERN);
        while(temp < numExercises){
            currId = random.nextInt(AppConfig.CONTRAST_EXERCISES_PATTERN.size());
            currExercise = AppConfig.CONTRAST_EXERCISES_PATTERN.get(currId);
            if(exercises.size() <= temp){
                exercises.add(new TwoImageInfo());
                exercises.get(temp).setLeftImage(currExercise);
                if(random.nextBoolean()){
                    Log.d(TAG, "Putting same pattern exercise for right as well");
                    exercises.get(temp).setRightImage(currExercise);
                    temp++;
                }
            }else{
                exercises.get(temp).setRightImage(currExercise);
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

    private void uploadEnd(){
        long readTime = System.currentTimeMillis();
        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("endactivity", getString(R.string.exercise_contrast));
        dictionary.put("user_id", mRemoteSensorManager.getPreferenceStorage().getUserId());
        dictionary.put("session_token", mRemoteSensorManager.getPreferenceStorage().getUserId() +"_" + readTime);
        dictionary.put("date", DateUtils.formatDate(readTime));
        dictionary.put("time", DateUtils.formatTime(readTime));

        mRemoteSensorManager.uploadActivityDataAsyn(dictionary);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(getString(R.string.exercise_contrast), null);
        mRemoteSensorManager.startMeasurement();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(null, null);
        mRemoteSensorManager.stopMeasurement();
        uploadEnd();
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
