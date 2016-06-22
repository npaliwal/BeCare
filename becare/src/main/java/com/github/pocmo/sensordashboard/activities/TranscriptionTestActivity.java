package com.github.pocmo.sensordashboard.activities;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.model.AudioData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by neerajpaliwal on 17/06/16.
 */
public class TranscriptionTestActivity extends AppCompatActivity {

    View speaker;
    TextView submit, header;
    EditText input;
    SeekBar volumeSeekbar;


    private int numExercises = 0;
    private int currExercise = 0;
    private long startTimeForExercise = -1;

    private ArrayList<AudioData> exercises = new ArrayList<>();

    private AudioManager audioManager = null;
    private PreferenceStorage preferenceStorage;
    private BecareRemoteSensorManager mRemoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_transcription_test);

        AppConfig.initTranscriptExercises(TranscriptionTestActivity.this);

        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(TranscriptionTestActivity.this);
        preferenceStorage = new PreferenceStorage(TranscriptionTestActivity.this);

        initControls();
        initExercises();
        initUIElements();
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
                    String value = getUserActivityStats();
                    mRemoteSensorManager.uploadActivityDataInstantly(value);
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
                startTimeForExercise = System.currentTimeMillis();
                input.setText(null);
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


            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onStopTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                {
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
        startTimeForExercise = System.currentTimeMillis();

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

    public String getUserActivityStats() {
        long timeTaken = System.currentTimeMillis() - startTimeForExercise;
        JSONObject obj = new JSONObject();
        try {
            obj.put("exercise_id", exercises.get(currExercise).getTextRes());
            obj.put("time_taken", timeTaken);
            obj.put("user_input", input.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
