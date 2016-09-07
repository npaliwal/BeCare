package com.github.pocmo.sensordashboard.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.github.pocmo.sensordashboard.ui.TwoImageFragment;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
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
    private int seq = 0;
    private long prevTime =0;
    private int correctCount = 0;
    public ArrayList<TwoImageInfo> exercises = new ArrayList<>();
    private BecareRemoteSensorManager mRemoteSensorManager;
    private PreferenceStorage preferenceStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTitleBar();
        setContentView(R.layout.activity_contrast_sensitivity);
        performanceText = (TextView) findViewById(R.id.tv_performance);
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(ContrastSensitivityActivity.this);
        preferenceStorage = new PreferenceStorage(ContrastSensitivityActivity.this);
        AppConfig.initContrastExercises();
        initExercises();
        initViewPager();
        initButtons();
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

    private void setupNextPage(boolean userMatch, int index){
        if(index >= exercises.size() - 1){
            responseContainer.setVisibility(View.GONE);
            pager.setVisibility(View.GONE);
            done.setVisibility(View.VISIBLE);
        }else {
            pager.setCurrentItem(index + 1, true);
        }
        String dataShow = getString(R.string.contrast_performance, correctCount, index+1);
        performanceText.setText(dataShow);

        value = getUserActivityStats(userMatch, index);
        Gson gson = new Gson();
        Hashtable dictionary = gson.fromJson(value, Hashtable.class);
        dictionary.put("seq", seq);
        dictionary.put("activityname", "Contrast Sensitivity");
        long now = System.currentTimeMillis();
        long dur = (prevTime == 0)? 0: now - prevTime;
        prevTime = now;
        dictionary.put("dur", dur);
        mRemoteSensorManager.uploadActivityDataAsyn(dictionary);
        seq++;
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
                if(exercise.getLeftImage() == exercise.getRightImage()){
                    correctCount++;
                }
                setupNextPage(true, index);
            }
        });
        mismatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pager.getCurrentItem();
                TwoImageInfo exercise = exercises.get(index);
                if(exercise.getLeftImage() != exercise.getRightImage()){
                    correctCount++;
                }
                setupNextPage(false, index);
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
    }

    public String getUserActivityStats(boolean userMatch, int currExercise) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("exercise_id", currExercise);
            obj.put("left_color", exercises.get(currExercise).getLeftImage().getId());
            obj.put("right_color", exercises.get(currExercise).getRightImage().getId());
            obj.put("left_contrast", exercises.get(currExercise).getLeftImage().getContrast());
            obj.put("user_response", userMatch ? "match" : "mismatch");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
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
