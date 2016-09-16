package com.github.pocmo.sensordashboard.activities;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.model.AudioData;
import com.github.pocmo.sensordashboard.ui.FlowLayout;
import com.github.pocmo.sensordashboard.ui.InstructionView;
import com.github.pocmo.sensordashboard.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Created by neerajpaliwal on 17/06/16.
 */
public class TranscriptionTestActivity extends AppCompatActivity {

    View speaker;
    TextView startEnd, performance;
    InstructionView message;
    SeekBar volumeSeekbar;
    FlowLayout flowLayout;

    private int numExercises = 0;
    private int currExercise = 0, currWordIndex = 0, currCharIndex = 0, currInputIndex = 0;
    private int correctInputCount = 0, incorrectInputCount = 0;
    private long prevTime = 0;

    private ArrayList<AudioData> exercises = new ArrayList<>();
    private List<Integer> quadrantOrder = new ArrayList<>();

    private AudioManager audioManager = null;
    private PreferenceStorage preferenceStorage;
    private BecareRemoteSensorManager mRemoteSensorManager;
    private long timeConsumed = 0;
    private boolean autoSpeakerPlay = true;
    private CountDownTimer cTimer = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        customTitleBar();
        setContentView(R.layout.activity_transcription_test);

        AppConfig.initTranscriptExercises(TranscriptionTestActivity.this);

        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(TranscriptionTestActivity.this);
        preferenceStorage = new PreferenceStorage(TranscriptionTestActivity.this);

        initTimer();
        initControls();
        initUIElements();
        initExercises();
    }

    private void initVariables(){
        currExercise = 0; currWordIndex = 0; currCharIndex = 0; currInputIndex = 0;
        correctInputCount = 0; incorrectInputCount = 0;
        prevTime = 0;
    }

    private void initTimer() {
        cTimer = new CountDownTimer(4500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished > 3000){
                    message.setMessage(currExercise == 0 ? "Get Ready..." : "Next...", false);
                }else {
                    message.setMessage(1 + millisUntilFinished/1000 + "", false);
                }
            }

            @Override
            public void onFinish() {
                populateTextForAudio();
            }
        };
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
        title.setText(R.string.exercise_transcription);

        ImageView back = (ImageView)customActionBar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getSnooker(TranscriptionTestActivity.this);
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();
            }
        });

        ImageView next = (ImageView)customActionBar.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getContrast(TranscriptionTestActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ImageView home = (ImageView) customActionBar.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.getParentActivityIntent(TranscriptionTestActivity.this);
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
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(getString(R.string.exercise_transcription), null);
        mRemoteSensorManager.startMeasurement();
        prevTime = System.currentTimeMillis();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(null, null);
        mRemoteSensorManager.stopMeasurement();
        uploadEnd();
    }

    private void initUIElements() {
        quadrantOrder.clear();
        quadrantOrder.add(1);
        quadrantOrder.add(2);
        quadrantOrder.add(3);
        quadrantOrder.add(4);
        Collections.shuffle(quadrantOrder);

        Map<Integer, View> quadrantMap = new HashMap<>();
        quadrantMap.put(1, findViewById(R.id.quarter1));
        quadrantMap.put(2, findViewById(R.id.quarter2));
        quadrantMap.put(3, findViewById(R.id.quarter3));
        quadrantMap.put(4, findViewById(R.id.quarter4));

        LinearLayout container1 = (LinearLayout)findViewById(R.id.container1);
        container1.removeAllViews();
        LinearLayout container2 = (LinearLayout)findViewById(R.id.container2);
        container2.removeAllViews();

        container1.addView(quadrantMap.get(quadrantOrder.get(0)));
        container1.addView(quadrantMap.get(quadrantOrder.get(1)));

        container2.addView(quadrantMap.get(quadrantOrder.get(2)));
        container2.addView(quadrantMap.get(quadrantOrder.get(3)));

        speaker = findViewById(R.id.speaker);
        startEnd = (TextView) findViewById(R.id.start_end);
        performance = (TextView) findViewById(R.id.tv_performance);

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.setMessage(exercises.get(currExercise).getTextRes(), false);
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), exercises.get(currExercise).getSpeechResId());
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if(timeConsumed <= 0){
                            timeConsumed = System.currentTimeMillis();
                        }
                        if(autoSpeakerPlay){
                            message.resetTimer();
                        }
                        message.setState(InstructionView.STATE.SHOW_TIMER);
                        autoSpeakerPlay = false;
                    }
                });

            }
        });

        startEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startEnd.getText().toString().equalsIgnoreCase("start")){
                    initVariables();
                    startEnd.setText("STOP");
                    findViewById(R.id.keyboard).setVisibility(View.VISIBLE);
                    flowLayout.setVisibility(View.VISIBLE);
                    cTimer.start();
                }else if(startEnd.getText().toString().equalsIgnoreCase("stop")){
                    message.setMessage("You have stopped the test", true);
                    startEnd.setText("START");
                    cTimer.cancel();
                    findViewById(R.id.keyboard).setVisibility(View.INVISIBLE);
                    flowLayout.removeAllViews();
                    flowLayout.setVisibility(View.INVISIBLE);
                    performance.setVisibility(View.INVISIBLE);
                }else{
                    finish();
                }
            }
        });
        flowLayout = (FlowLayout)findViewById(R.id.user_input);
        message = (InstructionView)findViewById(R.id.msg);
    }

    private void initControls()
    {
        try
        {
            volumeSeekbar = (SeekBar)findViewById(R.id.volume_seeker);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initExercises(){
        exercises.clear();
        numExercises = preferenceStorage.getNumTranscriptExercise();

        int temp = 0, currId;
        AudioData currExercise = null;
        Random random = new Random();
        while(temp < numExercises){
            currId = random.nextInt(AppConfig.TRANSCRIPT_EXERCISES.size());
            currExercise = AppConfig.TRANSCRIPT_EXERCISES.get(currId);
            if(!exercises.contains(currExercise)){
                exercises.add(currExercise);
                temp++;
            }
        }
        this.currExercise = 0;
        message.setInstruction(getString(R.string.transcript_instruction));
    }

    private void setPerformanceWithTime(String time){
        performance.setVisibility(View.VISIBLE);
        performance.setText(getString(R.string.transcript_performance_time, correctInputCount, incorrectInputCount, time));
    }

    private void populateTextForAudio(){
        flowLayout.removeAllViews();
        flowLayout.setClickable(true);
        correctInputCount = incorrectInputCount = 0;
        performance.setVisibility(View.INVISIBLE);
        speaker.setVisibility(View.VISIBLE);
        this.currCharIndex = 0;
        this.currWordIndex = 0;
        autoSpeakerPlay = true;
        speaker.performClick();
        AudioData currAudio = exercises.get(currExercise);
        String audioText = currAudio.getTextRes();
        FlowLayout.LayoutParams flowLP = new FlowLayout.LayoutParams(2, 2);
        View word = getLayoutInflater().inflate(R.layout.bubble_word, null);
        for(int i=0; i <= audioText.length(); i++){
            if(i == audioText.length()){
                flowLayout.addView(word, flowLP);
            }else{
                String currchar = audioText.substring(i, i + 1);
                if(currchar.equals(" ")){
                    flowLayout.addView(word, flowLP);
                    word = getLayoutInflater().inflate(R.layout.bubble_word, null);
                }else{
                    View view = getLayoutInflater().inflate(R.layout.bubble_char, null);
                    TextView textView = (TextView)view;
                    ((ViewGroup)word).addView(textView);
                }
            }
        }
    }

    private void uploadEnd(){
        long readTime = System.currentTimeMillis();
        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("endactivity", getString(R.string.exercise_transcription));
        dictionary.put("user_id", mRemoteSensorManager.getPreferenceStorage().getUserId());
        dictionary.put("session_token", mRemoteSensorManager.getPreferenceStorage().getUserId() +"_" + readTime);
        dictionary.put("date", DateUtils.formatDate(readTime));
        dictionary.put("time", DateUtils.formatTime(readTime));

        mRemoteSensorManager.uploadActivityDataAsyn(dictionary);

    }

    public void uploadUserActivityStats(CharSequence origChar, CharSequence userInput) {
        String value = "{\"orig_char\":" + origChar + ", \"user_char\":" + userInput + "}";
        long readTime = System.currentTimeMillis();
        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("activityname", getString(R.string.exercise_transcription));
        dictionary.put("seq", currInputIndex);
        dictionary.put("value",value );
        dictionary.put("dur (ms)", readTime - prevTime);
        dictionary.put("time", DateUtils.formatDateTime(readTime));
        prevTime = readTime;
        mRemoteSensorManager.uploadActivityDataAsyn(dictionary);
    }

    public void addToInput(View key){
        if(key instanceof TextView && flowLayout != null && currExercise < numExercises) {
            TextView tvKey = (TextView)key;
            String currCharinput = tvKey.getText().toString();
            String originalText = exercises.get(currExercise).getTextRes();
            String expectedChar = originalText.substring(currInputIndex, currInputIndex + 1);
            View bubbleWord = flowLayout.getChildAt(currWordIndex);
            if(bubbleWord == null)
                return;
            View bubbleChar = ((LinearLayout) bubbleWord).getChildAt(currCharIndex);
            if(bubbleChar == null)
                return;
            if(expectedChar.equalsIgnoreCase(currCharinput)){
                correctInputCount++;
                bubbleChar.setBackgroundResource(R.color.green);
            }else {
                incorrectInputCount++;
                bubbleChar.setBackgroundResource(R.color.red);
            }
            uploadUserActivityStats(expectedChar.toUpperCase(), currCharinput.toUpperCase());
            setPerformanceWithTime(message.getTimeLapsed());
            ((TextView)bubbleChar).setText(currCharinput);
            currInputIndex++;
            currCharIndex++;
            if(currInputIndex == originalText.length()){
                this.currExercise++;
                this.currInputIndex = 0;

                if(this.currExercise < this.numExercises) {
                    //populateTextForAudio();
                    flowLayout.setClickable(false);
                    message.setState(InstructionView.STATE.LARGE_TEXT);
                    cTimer.start();
                } else {
                    message.setState(InstructionView.STATE.PAUSE_TIMER);
                    message.setMessage("Congratulations!!", true);
                    startEnd.setText("DONE");
                    startEnd.setVisibility(View.VISIBLE);
                    flowLayout.setVisibility(View.GONE);
                    speaker.setVisibility(View.INVISIBLE);
                }
            }else {
                String nextExpectedChar = originalText.substring(currInputIndex, currInputIndex + 1);
                if (nextExpectedChar.equals(" ")) {
                    currCharIndex = 0;
                    currInputIndex++;
                    currWordIndex++;
                }
            }
        }
    }

}
