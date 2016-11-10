package com.github.pocmo.sensordashboard.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.media.Image;
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
import com.github.pocmo.sensordashboard.animation.AnimatedSprite;
import com.github.pocmo.sensordashboard.ui.InstructionView;

import java.util.ArrayList;
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
    private InstructionView message;
    private ViewGroup container;


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

    private void initUI(){
        bronze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationTimer.start();
                addCoin("bronze", bronze);
            }
        });
        gold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationTimer.start();
                addCoin("gold", gold);
            }
        });
        silver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animationTimer.start();
                addCoin("silver", silver);
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

    private void addCoin(final String coinType, View coinView){
        final RelativeLayout rl = (RelativeLayout) findViewById(R.id.container);
        final ImageView iv = new ImageView(this);

        int[] coinLocations = new int[2];
        coinView.getLocationOnScreen(coinLocations);

        int[] chestLocations = new int[2];
        chest.getLocationOnScreen(chestLocations);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(coinView.getWidth(), coinView.getHeight());
        params.leftMargin = coinLocations[0];
        params.topMargin = container.getHeight() - 2*coinView.getHeight();
        rl.addView(iv, params);


        final Animation animation = new TranslateAnimation(0 , chestLocations[0]-coinLocations[0] ,
                                                        0, chestLocations[1]-coinLocations[1] + chest.getHeight()/2);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        iv.startAnimation(animation);

        CountDownTimer coinTimer = new CountDownTimer(1000, 50) {
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
            }
        };
        coinTimer.start();
    }

}
