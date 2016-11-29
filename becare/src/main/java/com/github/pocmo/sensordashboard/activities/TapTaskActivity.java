package com.github.pocmo.sensordashboard.activities;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.animation.ProgressCircle;
import com.github.pocmo.sensordashboard.ui.InstructionView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by neerajpaliwal on 09/11/16.
 */
public class TapTaskActivity extends AppCompatActivity {
    private static final String TAG = "TapTaskActivity";
    private Map<String, Integer> coinFrameMap = null;

    private int chestAnimationCounter = 1, coinFlipAnimationCounter = 1;
    private CountDownTimer animationTimer;

    private ImageView chest, bronze, silver, gold;
    private TextView startEnd, performance;
    private ProgressCircle bronzeProgress, silverProgress, goldProgress;
    private InstructionView message;
    private ViewGroup container;
    private CountDownTimer cTimer = null, eTimer = null;

    private int currentExercise = 0, numBronzeCoins = 0, numSilverCoins = 0,  numGoldCoins = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customTitleBar();
        Log.i(TAG, "onCreate()");

        setContentView(R.layout.activity_tap_task);
        chest = (ImageView)findViewById(R.id.chest);
        bronze = (ImageView)findViewById(R.id.bronze);
        silver = (ImageView)findViewById(R.id.silver);
        gold = (ImageView)findViewById(R.id.gold);

        startEnd = (TextView) findViewById(R.id.start_end);
        performance = (TextView) findViewById(R.id.tv_performance);

        bronzeProgress = (ProgressCircle)findViewById(R.id.bronze_progress);
        silverProgress = (ProgressCircle)findViewById(R.id.silver_progress);
        goldProgress = (ProgressCircle)findViewById(R.id.gold_progress);

        message = (InstructionView)findViewById(R.id.msg);

        container = (ViewGroup)findViewById(R.id.container);

        animationTimer = new CountDownTimer(2000, 400){
            @Override
            public void onTick(long millisUntilFinished) {
                if (chestAnimationCounter == 1) {
                    chest.setImageResource(R.mipmap.chest3_frame2);
                }else if(chestAnimationCounter == 2){
                        chest.setImageResource(R.mipmap.chest3_frame3);
                }else if(chestAnimationCounter == 3){
                        chest.setImageResource(R.mipmap.chest3_frame2);
                }else{
                    chest.setImageResource(R.mipmap.chest3_frame1);
                }
                chestAnimationCounter++;
                if(chestAnimationCounter > 4){
                    chestAnimationCounter = 1;
                }
            }

            @Override
            public void onFinish() {
                chestAnimationCounter = 1;
            }
        };

        initCoinFrameMap();
        initUI();
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
        title.setText(R.string.exercise_tap_task);

        ImageView back = (ImageView)customActionBar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getUpAndGoActivity(TapTaskActivity.this);
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();
            }
        });

        ImageView next = (ImageView)customActionBar.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getStroop(TapTaskActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ImageView home = (ImageView) customActionBar.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.getParentActivityIntent(TapTaskActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        ab.setCustomView(customActionBar, layout);
    }

    private String getCoinType(){
        switch (currentExercise){
            case 0: return "Bronze";
            case 1: return "Silver";
            default:return "Gold";
        }
    }

    private void disableCoinClicks(){
        bronze.setClickable(false); bronzeProgress.setClickable(false);
        silver.setClickable(false); silverProgress.setClickable(false);
        gold.setClickable(false); goldProgress.setClickable(false);

        bronzeProgress.setProgress(0f);
        bronzeProgress.startAnimation(1);
        silverProgress.setProgress(0f);
        silverProgress.startAnimation(1);
        goldProgress.setProgress(0f);
        goldProgress.startAnimation(100);
    }

    private void enableCoinType(){
        switch (currentExercise){
            case 0:
                bronze.setClickable(true);
                bronzeProgress.setClickable(true);
                bronzeProgress.setProgress(1f);
                bronzeProgress.startAnimation(100);
                break;

            case 1:
                silver.setClickable(true);
                silverProgress.setClickable(true);
                silverProgress.setProgress(1f);
                silverProgress.startAnimation(100);
                break;

            default:
                gold.setClickable(true);
                goldProgress.setClickable(true);
                goldProgress.setProgress(1f);
                goldProgress.startAnimation(100);;
        }
    }

    private void initTimer() {
        cTimer = new CountDownTimer(3999, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                message.setMessage("Collect "+getCoinType()+" coins\n" + (1+ millisUntilFinished/1000), true);
            }

            @Override
            public void onFinish() {
                eTimer.start();
                enableCoinType();
            }
        };

        eTimer = new CountDownTimer(29999, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                message.setMessage("Timer : "+(1 + millisUntilFinished/1000) +" secs", false);
            }

            @Override
            public void onFinish() {
                disableCoinClicks();
                if(currentExercise == 2){
                    message.setMessage("Congratulations!!\nTotal score is " + (numGoldCoins+numSilverCoins+numBronzeCoins), true);
                }else {
                    currentExercise++;
                    cTimer.start();
                }
            }
        };
    }

    private void setPerformance(){
        performance.setVisibility(View.VISIBLE);
        performance.setText(getString(R.string.tap_performance, numBronzeCoins, numSilverCoins, numGoldCoins));
    }

    private void initUI(){
        bronzeProgress.setProgress(0f); bronzeProgress.startAnimation(1);
        silverProgress.setProgress(0f);silverProgress.startAnimation(1);
        goldProgress.setProgress(0f);goldProgress.startAnimation(1);

        View.OnClickListener bronzeClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bronzeProgress.setProgress(1f);
                //bronzeProgress.startAnimation(1000);
                animationTimer.start();
                numBronzeCoins++;
                setPerformance();
                addCoin("bronze", bronze, bronzeProgress, 1000);
            }
        };
        bronzeProgress.setOnClickListener(bronzeClick);
        bronze.setOnClickListener(bronzeClick);

        View.OnClickListener silverClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //silverProgress.setProgress(1f);
                //silverProgress.startAnimation(2000);
                animationTimer.start();
                numSilverCoins++;
                setPerformance();
                addCoin("silver", silver, silverProgress, 1000);
            }
        };
        silverProgress.setOnClickListener(silverClick);
        silver.setOnClickListener(silverClick);

        View.OnClickListener goldClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //goldProgress.setProgress(1f);
                //goldProgress.startAnimation(3000);
                animationTimer.start();
                numGoldCoins++;
                setPerformance();
                addCoin("gold", gold, goldProgress, 1000);
            }
        };
        goldProgress.setOnClickListener(goldClick);
        gold.setOnClickListener(goldClick);

        disableCoinClicks();
        startEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cTimer.start();
                startEnd.setVisibility(View.INVISIBLE);
                chest.setVisibility(View.VISIBLE);
            }
        });
        message.setInstruction(getString(R.string.taptask_instruction));
    }

    private void initCoinFrameMap(){
        if(coinFrameMap == null){
            coinFrameMap = new HashMap<>();
            coinFrameMap.put("gold_1", R.mipmap.gold9_frame1);
            coinFrameMap.put("gold_2", R.mipmap.gold9_frame2);
            coinFrameMap.put("gold_3", R.mipmap.gold9_frame3);
            coinFrameMap.put("gold_4", R.mipmap.gold9_frame4);
            coinFrameMap.put("gold_5", R.mipmap.gold9_frame5);
            coinFrameMap.put("gold_6", R.mipmap.gold9_frame6);
            coinFrameMap.put("gold_7", R.mipmap.gold9_frame7);
            coinFrameMap.put("gold_8", R.mipmap.gold9_frame8);
            coinFrameMap.put("gold_9", R.mipmap.gold9_frame9);
            coinFrameMap.put("gold_10", R.mipmap.gold9_frame10);

            coinFrameMap.put("silver_1", R.mipmap.silver9_frame1);
            coinFrameMap.put("silver_2", R.mipmap.silver9_frame2);
            coinFrameMap.put("silver_3", R.mipmap.silver9_frame3);
            coinFrameMap.put("silver_4", R.mipmap.silver9_frame4);
            coinFrameMap.put("silver_5", R.mipmap.silver9_frame5);
            coinFrameMap.put("silver_6", R.mipmap.silver9_frame6);
            coinFrameMap.put("silver_7", R.mipmap.silver9_frame7);
            coinFrameMap.put("silver_8", R.mipmap.silver9_frame8);
            coinFrameMap.put("silver_9", R.mipmap.silver9_frame9);
            coinFrameMap.put("silver_10", R.mipmap.silver9_frame10);

            coinFrameMap.put("bronze_1", R.mipmap.bronze9_frame1);
            coinFrameMap.put("bronze_2", R.mipmap.bronze9_frame2);
            coinFrameMap.put("bronze_3", R.mipmap.bronze9_frame3);
            coinFrameMap.put("bronze_4", R.mipmap.bronze9_frame4);
            coinFrameMap.put("bronze_5", R.mipmap.bronze9_frame5);
            coinFrameMap.put("bronze_6", R.mipmap.bronze9_frame6);
            coinFrameMap.put("bronze_7", R.mipmap.bronze9_frame7);
            coinFrameMap.put("bronze_8", R.mipmap.bronze9_frame8);
            coinFrameMap.put("bronze_9", R.mipmap.bronze9_frame9);
            coinFrameMap.put("bronze_10", R.mipmap.bronze9_frame10);

        }

    }

    private void addCoin(final String coinType, final View coinView, final ProgressCircle coinProgress, int duration){
        final RelativeLayout rl = (RelativeLayout) findViewById(R.id.container);
        final ImageView iv = new ImageView(this);

        int[] coinLocations = new int[2];
        coinProgress.getLocationOnScreen(coinLocations);

        int[] chestLocations = new int[2];
        chest.getLocationOnScreen(chestLocations);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(coinView.getWidth(), coinView.getHeight());
        params.leftMargin = coinLocations[0] + coinProgress.getWidth()/2;
        params.topMargin = coinLocations[1] - coinProgress.getHeight()/2;
        rl.addView(iv, params);


        final Animation animation = new TranslateAnimation(0 , chestLocations[0]-coinLocations[0] ,
                                                        0, chestLocations[1]-coinLocations[1] + chest.getHeight()/2 - coinProgress.getHeight()/2);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        iv.startAnimation(animation);

        //coinProgress.setClickable(false);
        //coinView.setClickable(false);
        CountDownTimer coinTimer = new CountDownTimer(duration, 50) {
            int animationCounter = 1;

            @Override
            public void onTick(long millisUntilFinished) {
                iv.setImageResource(coinFrameMap.get(coinType + "_" + animationCounter));
                animationCounter++;
                if(animationCounter > 10){
                    animationCounter = 1;
                }
            }

            @Override
            public void onFinish() {
                rl.removeView(iv);
                coinProgress.setProgress(0);
                coinProgress.invalidate();
                //coinProgress.setClickable(true);
                //coinView.setClickable(true);
            }
        };
        coinTimer.start();
    }

}
