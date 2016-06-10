package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.ui.TwoImageFragment;

import java.util.ArrayList;


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

    private int correctCount = 0;
    public static ArrayList<TwoImageInfo> exercises = new ArrayList<>();
    private BecareRemoteSensorManager mRemoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contrast_sensitivity);
        performanceText = (TextView) findViewById(R.id.tv_performance);
        initExercises();
        initViewPager();
        initButtons();
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(ContrastSensitivityActivity.this);
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
                TwoImageInfo exercise = exercises.get(index / exercises.size());
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

    private void initViewPager(){
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapterViewPager);
    }

    private void initExercises(){
        exercises.clear();
        exercises.add(new TwoImageInfo("#FFffc1", "#AA0000"));
        exercises.add(new TwoImageInfo("#c2c2c2", "#d3d3d3"));
        exercises.add(new TwoImageInfo("#e1e1e1", "#c2c2c2"));
        exercises.add(new TwoImageInfo(R.drawable.becare, 1));
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
        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return exercises.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return TwoImageFragment.newInstance(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }

    public static class TwoImageInfo{
        int contrast         = 1;
        int imageResId       = -1;
        String lefColor      = null;
        String rightColor    = null;

        public TwoImageInfo(String left, String right) {
            lefColor = left;
            rightColor = right;
        }

        public TwoImageInfo(int imageResId, int contrast) {
            this.contrast = contrast;
            this.imageResId = imageResId;
        }


        public float getContrast() {
            return contrast % 10;
        }

        public void setContrast(int contrast) {
            this.contrast = contrast;
        }

        public int getImageResId() {
            return imageResId;
        }

        public void setImageResId(int imageResId) {
            this.imageResId = imageResId;
        }

        public String getLefColor() {
            return lefColor;
        }

        public void setLefColor(String lefColor) {
            this.lefColor = lefColor;
        }

        public String getRightColor() {
            return rightColor;
        }

        public void setRightColor(String rightColor) {
            this.rightColor = rightColor;
        }

        public boolean isSimilar(){
            if(contrast != 1){
                return false;
            }
            if(lefColor != null && rightColor != null && !lefColor.equals(rightColor)){
                return false;
            }
            return true;
        }

    }
}
