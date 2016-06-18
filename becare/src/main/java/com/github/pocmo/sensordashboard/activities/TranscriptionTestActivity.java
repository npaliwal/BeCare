package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created by neerajpaliwal on 17/06/16.
 */
@ContentView(R.layout.activity_transcription_test)
public class TranscriptionTestActivity extends RoboActivity {

    @InjectView(R.id.speaker)
    View speaker;

    @InjectView(R.id.submit_input)
    TextView submit;

    @InjectView(R.id.et_user_input)
    EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.transcript_1);
                mp.start();
            }
        });
    }

}
