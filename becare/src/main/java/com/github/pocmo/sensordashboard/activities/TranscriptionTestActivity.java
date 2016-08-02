package com.github.pocmo.sensordashboard.activities;

import android.content.Context;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.model.AudioData;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;


/**
 * Created by neerajpaliwal on 17/06/16.
 */
public class TranscriptionTestActivity extends AppCompatActivity {

    View speaker;
    TextView submit, header;
    EditText input;
    SeekBar volumeSeekbar;

    private int currKeyCount = 0;
    private int numExercises = 0;
    private int currExercise = 0;
    private long prevTime = 0;

    private ArrayList<AudioData> exercises = new ArrayList<>();

    private AudioManager audioManager = null;
    private PreferenceStorage preferenceStorage;
    private BecareRemoteSensorManager mRemoteSensorManager;

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
        initExercises();
        initUIElements();
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
                if(TextUtils.isEmpty(s) || before > count)
                    return;
                CharSequence keys = s.subSequence(start + before, start + count);
                int keysCount = keys.length();
                while (keysCount > 0) {
                    String value = getUserActivityStats(keys.subSequence(keysCount - 1, keysCount));
                    Gson gson = new Gson();
                    Hashtable dictionary = gson.fromJson(value, Hashtable.class);
                    double seq = (double)dictionary.get("seq");
                    dictionary.put("seq", (int)seq);
                    dictionary.put("activityname", "Transcription Test");
                    long now = System.currentTimeMillis();
                    long dur = (prevTime == 0) ? 0: now - prevTime;
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
        submit = (TextView) findViewById(R.id.submit_input);
        header = (TextView) findViewById(R.id.exercise_header);

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), exercises.get(currExercise).getSpeechResId());
                mp.start();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currExercise <= numExercises) {
                    currExercise++;
                    header.setText("Audio #" + (1 + currExercise));
                    if (currExercise == numExercises) {
                        speaker.setVisibility(View.GONE);
                        input.setVisibility(View.GONE);
                        header.setVisibility(View.GONE);
                        submit.setText("Done");
                        currExercise++;
                    }
                } else {
                    finish();
                }
                input.setText(null);
                currKeyCount = 0;
                prevTime = 0;
            }
        });
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
        if(key instanceof TextView) {
            TextView tvKey = (TextView)key;
            input.setText(input.getText().toString() + tvKey.getText());
            input.setSelection(input.getText().length());
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
            input.setText(input.getText().subSequence(0, input.getText().length()-1));
            input.setSelection(input.getText().length());
        }
    }
}
