package com.becare.users.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.becare.users.AppConfig;
import com.becare.users.BecareRemoteSensorManager;
import com.becare.users.PreferenceStorage;
import com.becare.users.R;
import com.becare.users.model.AudioData;
import com.becare.users.ui.FlowLayout;
import com.becare.users.ui.InstructionView;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Created by neerajpaliwal on 17/06/16.
 */
public class TranscriptionTestActivity extends AppCompatActivity {

    View speaker;
    TextView startEnd, header, performance;
    InstructionView message;
    EditText input;
    SeekBar volumeSeekbar;
    FlowLayout flowLayout;

    private int currKeyCount = 0;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        customTitleBar();
        setContentView(R.layout.activity_transcription_test);

        AppConfig.initTranscriptExercises(TranscriptionTestActivity.this);

        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(TranscriptionTestActivity.this);
        preferenceStorage = new PreferenceStorage(TranscriptionTestActivity.this);

        initControls();
        initUIElements();
        initExercises();
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
    }


    @Override
    protected void onPause() {
        super.onPause();
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(null, null);
        mRemoteSensorManager.stopMeasurement();
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

        input = (EditText)findViewById(R.id.et_user_input);
        //input.setRawInputType(InputType.TYPE_CLASS_TEXT);
        input.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int inType = input.getInputType(); // backup the input type
                input.setInputType(InputType.TYPE_NULL); // disable soft input
                input.onTouchEvent(event); // call native handler
                input.setInputType(inType); // restore input type
                return true; // consume touch even
            }
        });
        input.setTextIsSelectable(true);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s) || before > count)
                    return;
                CharSequence keys = s.subSequence(start + before, start + count);
                int keysCount = keys.length();
                while (keysCount > 0) {
                    String value = getUserActivityStats(keys.subSequence(keysCount - 1, keysCount));
                    Gson gson = new Gson();
                    Hashtable dictionary = gson.fromJson(value, Hashtable.class);
                    double seq = (double) dictionary.get("seq");
                    dictionary.put("seq", (int) seq);
                    dictionary.put("activityname", "Transcription Test");
                    long now = System.currentTimeMillis();
                    long dur = (prevTime == 0) ? 0 : now - prevTime;
                    dictionary.put("dur", dur);
                    mRemoteSensorManager.uploadActivityDataAsyn(dictionary);
                    keysCount--;
                    currKeyCount++;
                    prevTime = now;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        speaker = findViewById(R.id.speaker);
        startEnd = (TextView) findViewById(R.id.start_end);
        header = (TextView) findViewById(R.id.exercise_header);
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
                if(message.getState() == InstructionView.STATE.SHOW_INSTRUCTION){
                    startEnd.setVisibility(View.GONE);
                    flowLayout.setVisibility(View.VISIBLE);
                    populateTextForAudio();
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

    private void populateTextForAudio(){
        flowLayout.removeAllViews();
        performance.setVisibility(View.VISIBLE);
        correctInputCount = incorrectInputCount = 0;
        performance.setText(getString(R.string.transcript_performance, correctInputCount, incorrectInputCount));
        speaker.setVisibility(View.VISIBLE);
        this.currCharIndex = 0;
        this.currWordIndex = 0;
        autoSpeakerPlay = true;
        speaker.performClick();
        AudioData currAudio = exercises.get(currExercise);
        String audioText = currAudio.getTextRes();
        FlowLayout.LayoutParams flowLP = new FlowLayout.LayoutParams(5, 5);
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

    public String getUserActivityStats(CharSequence currChar) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("exercise_id", currExercise);
            obj.put("audio_id", exercises.get(currExercise).getTextRes());
            obj.put("key", currChar);
            obj.put("seq", currKeyCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
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
            performance.setText(getString(R.string.transcript_performance, correctInputCount, incorrectInputCount));
            ((TextView)bubbleChar).setText(currCharinput);
            currInputIndex++;
            currCharIndex++;
            if(currInputIndex == originalText.length()){
                this.currExercise++;
                this.currInputIndex = 0;

                if(this.currExercise < this.numExercises) {
                    populateTextForAudio();
                }else {
                    message.setState(InstructionView.STATE.PAUSE_TIMER);
                    startEnd.setText("END");
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

    public void addSpaceToInput(View key){
        if(key instanceof TextView) {
            input.setText(input.getText().toString() + " ");
            input.setSelection(input.getText().length());

        }
    }

    public void removeFromInput(View key){
        if(key instanceof TextView) {
            if(!TextUtils.isEmpty(input.getText().toString())) {
                input.setText(input.getText().subSequence(0, input.getText().length() - 1));
                input.setSelection(input.getText().length());
            }
        }
    }

}
