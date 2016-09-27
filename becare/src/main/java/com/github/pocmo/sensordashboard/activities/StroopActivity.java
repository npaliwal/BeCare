package com.github.pocmo.sensordashboard.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.utils.DateUtils;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Created by qtxdev on 8/9/2016.
 */
public class StroopActivity extends AppCompatActivity {
    private final int TRIALS = 10;
    private LinearLayout colorFrame;
    private TextView message;
    private ImageView match;
    private LinearLayout button;
    private LinearLayout test;
    private LinearLayout number;
    private TextView color1, color2;
    private String[] colors = {"Red", "Blue", "Yellow", "Green", "Gray", "Purple", "Black",  "White"};
    private String[] currentColors = new String[TRIALS];
    private String[] currentWords = new String[TRIALS];
    private int[] answers = new int[TRIALS]; //0--no answer, 1--yes, 2--no
    private CountDownTimer cTimer = null;
    private CountDownTimer cTimer2 = null;
    private CountDownTimer ringTimer = null;
    private Animation mLoadAnimation = null;
    private Button matchButton, stopButton;
    private int count = 0;
    private int answerIndex =0;
    private Ringtone ring;
    Random random = null;
    private BecareRemoteSensorManager becareRemoteSensorManager;
    private wordData currentData = new wordData();
    private long currTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        customTitleBar();
        setContentView(R.layout.activity_stroop_test);
        colorFrame = (LinearLayout) findViewById(R.id.colorFrame);
        message = (TextView) findViewById(R.id.msg);
      //  match = (ImageView) findViewById(R.id.matchOrNo);
     //   match.setImageResource(R.drawable.yes);

        test = (LinearLayout) findViewById(R.id.test);
        number = (LinearLayout) findViewById(R.id.number);

        color1 = (TextView) findViewById(R.id.color1);
     //   color2 = (TextView) findViewById(R.id.color2);

     //   matchButton =(Button)findViewById(R.id.matchButton);

        Uri notification  = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_ALARM);
        ring = RingtoneManager.getRingtone(getApplicationContext(), notification);

        button = (LinearLayout)findViewById(R.id.button);
        Button start = (Button)findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setVisibility(View.GONE);
             //   number.setVisibility(View.VISIBLE);
                test.setVisibility(View.VISIBLE);

                ValueAnimator valueAnimator1 = ValueAnimator.ofInt(0,2);
                final ValueAnimator valueAnimator2 = ValueAnimator.ofInt(0,2);
                final ValueAnimator valueAnimator3 = ValueAnimator.ofInt(0,2);
                 mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                mLoadAnimation.setDuration(1000);




                message.setTextSize(20);
                message.setText("Get Ready.\nKnow the color buttons.\n 00:05");
                cTimer2.start();

            }
        });

        stopButton = (Button)findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         cTimer.cancel();
                                         cTimer2.cancel();
                                         String str = String.format(" You have stopped the test." );
                                         message.setText(str);
                                         message.setTextSize(17);
                                         test.setVisibility(View.GONE);
                                         button.setVisibility(View.VISIBLE);
                                         stopButton.setVisibility(View.GONE);
                                         color1.setVisibility(View.GONE);
                                         count = 0;
                                     }
                                 });
        cTimer = new CountDownTimer(4000, 500) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                float sec = (int) (millisUntilFinished / 1000);

                if (sec == 3 || sec == 2 || sec == 1) {
                    int d = (int)sec;
                    String str = String.format("What color is the word?\n00:0%d", d);
                    message.setText(str);
                }


            }

            public void onFinish() {//   startMeasure = false;

                if (count < TRIALS) {
                    if (!currentData.uploaded) {
                        long dur = System.currentTimeMillis() - currTime;
                        currTime = System.currentTimeMillis();
                        upload("no answer", count-1,  (int)dur);
                    }
                    message.setText("Next...");

                    color1.startAnimation(mLoadAnimation);
                    message.setText("00:03");
                    setWord();
                    cTimer.start();

                }
                else{
                    int correct = 0;
                    for (int i=0; i<answers.length; i++)
                        if (answers[i] == 1)
                            correct++;
                    double score = (double)correct / (double)answers.length * 100;
                    int s = (int)score;
                    String str = String.format(" You have completed the test.\n Total tests:%d \n Correct answers: %d\n Score: %d", answers.length,correct, s );
                    str += "%";
                    message.setText(str);
                    message.setTextSize(17);
                    test.setVisibility(View.GONE);
                    button.setVisibility(View.VISIBLE);
                    stopButton.setVisibility(View.GONE);
                    color1.setVisibility(View.GONE);
                    count = 0;
                }
            }
        };

        cTimer2 = new CountDownTimer(5700, 1000) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                float sec = (int) (millisUntilFinished / 1000);

                if (sec == 5 || sec == 4 || sec == 3 || sec == 2 || sec == 1) {
                    int d = (int)sec;
                    String str = String.format("Get Ready.\n Know the color buttons.\n00:0%d", d);
                    message.setText(str);
                }
            }

            public void onFinish() {//   startMeasure = false;
                message.setText("00:03");

                color1.setVisibility(View.VISIBLE);
                test.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.VISIBLE);
                for (int i=0; i<answers.length; i++) {
                    answers[i] = 0;
                    currentColors[i] = "";
                    currentWords[i] = "";
                }
                random =new Random();
                setWord();
                cTimer.start();
            }

        };

        ringTimer = new CountDownTimer(500, 500) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                float sec = (int) (millisUntilFinished / 1000);

            }

            public void onFinish() {//   startMeasure = false;
               ring.stop();
            }

        };

        becareRemoteSensorManager = BecareRemoteSensorManager.getInstance(StroopActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cTimer.cancel();
        cTimer2.cancel();
        uploadEnd();
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
        title.setText(R.string.stroop);

        ImageView back = (ImageView)customActionBar.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuUtils.getUpAndGo(StroopActivity.this);
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();
            }
        });

        ImageView next = (ImageView)customActionBar.findViewById(R.id.next);
        next.setVisibility(View.GONE);

        ImageView home = (ImageView) customActionBar.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.getParentActivityIntent(StroopActivity.this);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        });
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        ab.setCustomView(customActionBar, layout);
    }

    public void getTwentyFiveStep(View view) {
        Intent intent = new Intent(this, TwentyFiveStepsActivity.class);
        startActivity(intent);

    }

    public void getSixMinutes(View view) {
        Intent intent = new Intent(this, SixMinutesActivity.class);
        startActivity(intent);

    }

    public void redColor(View view) {

        if (answers[count-1] != 0) // have answered
            return;

        if (currentData.color1.equals("Red"))
            answers[count-1] = 1;
        else
            answers[count-1] = 2;

        long dur = System.currentTimeMillis() - currTime;
        currTime = System.currentTimeMillis();
        upload("Red", count-1, (int)dur);
    }

    public void blueColor(View view) {

        if (answers[count-1] != 0) // have answered
            return;

        if (currentData.color1.equals("Blue"))
            answers[count-1] = 1;
        else
            answers[count-1] = 2;

        long dur = System.currentTimeMillis() - currTime;
        currTime = System.currentTimeMillis();
        upload("Blue", count-1, (int)dur);
    }

    public void yellowColor(View view) {

        if (answers[count-1] != 0) // have answered
            return;

        if (currentData.color1.equals("Yellow"))
            answers[count-1] = 1;
        else
            answers[count-1] = 2;

        long dur = System.currentTimeMillis() - currTime;
        currTime = System.currentTimeMillis();
        upload("Yellow", count-1, (int)dur);
    }

    public void greenColor(View view) {

        if (answers[count-1] != 0) // have answered
            return;

        if (currentData.color1.equals("Green"))
            answers[count-1] = 1;
        else
            answers[count-1] = 2;

        long dur = System.currentTimeMillis() - currTime;
        currTime = System.currentTimeMillis();
        upload("Green", count-1, (int)dur);
    }

    public void grayColor(View view) {

        if (answers[count-1] != 0) // have answered
            return;

        if (currentData.color1.equals("Gray"))
            answers[count-1] = 1;
        else
            answers[count-1] = 2;

        long dur = System.currentTimeMillis() - currTime;
        currTime = System.currentTimeMillis();
        upload("Gray", count-1, (int)dur);
    }

    public void purpleColor(View view) {

        if (answers[count-1] != 0) // have answered
            return;

        if (currentData.color1.equals("Purple"))
            answers[count-1] = 1;
        else
            answers[count-1] = 2;

        long dur = System.currentTimeMillis() - currTime;
        currTime = System.currentTimeMillis();
        upload("Purple", count-1, (int)dur);
    }

    public void blackColor(View view) {

        if (answers[count-1] != 0) // have answered
            return;

        if (currentData.color1.equals("Black"))
            answers[count-1] = 1;
        else
            answers[count-1] = 2;

        long dur = System.currentTimeMillis() - currTime;
        currTime = System.currentTimeMillis();
        upload("Black", count-1, (int)dur);
    }

    public void whiteColor(View view) {

        if (answers[count-1] != 0) // have answered
            return;

        if (currentData.color1.equals("White"))
            answers[count-1] = 1;
        else
            answers[count-1] = 2;

        long dur = System.currentTimeMillis() - currTime;
        currTime = System.currentTimeMillis();
        upload("White", count-1, (int)dur);
    }


    public void noMatchTest(View view) {
   /*     String word1 = color1.getText().toString();
        String word2 = color2.getText().toString();

        if (word1.equals("") || word2.equals(""))
            return;

        if (answers[count-1] != 0) // have answered
            return;

        if (!word1.equals(word2))
            answers[count-1] = 1;
        else
            answers[count-1] = 2;

        long dur = System.currentTimeMillis() - currTime;
        currTime = System.currentTimeMillis();
        upload("no", count-1, (int)dur);*/
    }

    private void setWord()
    {
        String word1,  colorName1=null, colorName2=null;

        int index =0;

        int sec = Calendar.getInstance().get(Calendar.SECOND);
        //set color
        for (int i=0; i<sec; i++)
            index = randomGen();

        if (index >= colors.length)
            return;
        colorName1 = colors[index];

        Boolean found = false;
        for (int i=0; i<count; i++)
            if (currentColors[i].equals(colorName1))
                found = true;
        if (found)
            index = randomGen();
        found = false;
        for (int i=0; i<count; i++)
            if (currentColors[i].equals(colorName1))
                found = true;
        if (found)
            index = randomGen();
        colorName1 = colors[index];
        currentColors[count] = colorName1;

        //set color
        for (int i=0; i<sec; i++)
            index = randomGen();

        if (index >= colors.length)
            return;
        word1 = colors[index];

        //set word
        for (int i=0; i<sec; i++)
            index = randomGen();

        if (index >= colors.length)
            return;
        word1 = colors[index];

        found = false;
        for (int i=0; i<count; i++)
            if (currentWords[i].equals(word1))
                found = true;
        if (found)
            index = randomGen();
        found = false;
        for (int i=0; i<count; i++)
            if (currentWords[i].equals(word1))
                found = true;
        if (found)
            index = randomGen();
        word1 = colors[index];
        currentWords[count] = word1;

        ring.play();
        ringTimer.start();

        color1.setText(word1);
        setWordColor(colorName1, color1);

        currentData.color1 = colorName1;
        currentData.word1 = word1;
        currentData.uploaded = false;

        count++;
    }

    private void setWordColor(String word, TextView color)
    {
        if (word == "Green") {
            color.setTextColor(Color.rgb(10, 160, 92));

        }

        if (word == "Red") {
            color.setTextColor(Color.rgb(215, 58, 49));

        }

        if (word == "Blue") {
            color.setTextColor(Color.rgb(63, 81, 181));

        }

        if (word == "Yellow") {
            color.setTextColor(Color.rgb(255, 193, 7));

        }

        if (word == "Black") {
            color.setTextColor(Color.rgb(0, 0, 0));

        }

        if (word == "Purple") {
            color.setTextColor(Color.rgb(156, 39, 176));

        }

        if (word == "Gray") {
            color.setTextColor(Color.rgb(130, 130, 130));

        }

        if (word == "White") {
            color.setTextColor(Color.rgb(255, 255, 255));

        }
    }

    private Boolean genIndexLeft()
    {
        int leftTrial = 0;
        for (int i=0; i<10; i++) {
            int r = randomGen();
            if (r <4)
                leftTrial++;
        }
        if (leftTrial >= 5)
            return true;

        return false;
    }

    private int randomGen()
    {
        int min = 0;
        int max = colors.length-1;
    //    Random r = new Random();
        int ran = 0;
        ran = random.nextInt(max - min + 1) + min;
        return ran;
    }

    private void upload(String answer, int seq, int dur){
        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("activityname", getString(R.string.stroop));
        dictionary.put("seq", seq);
        dictionary.put("word", currentData.word1);
        dictionary.put("color", currentData.color1);
        dictionary.put("answer",  answer);
        dictionary.put("dur",  dur);

        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);
        currentData.uploaded = true;
    }

    private void uploadEnd(){
        long readTime = System.currentTimeMillis();
        LinkedHashMap dictionary = new LinkedHashMap();
        dictionary.put("endactivity", getString(R.string.stroop));
        dictionary.put("user_id", becareRemoteSensorManager.getPreferenceStorage().getUserId());
        dictionary.put("session_token", becareRemoteSensorManager.getPreferenceStorage().getUserId() +"_" + readTime);
        dictionary.put("date", DateUtils.formatDate(readTime));
        dictionary.put("time", DateUtils.formatTime(readTime));

        becareRemoteSensorManager.uploadActivityDataAsyn(dictionary);

    }

    public class wordData{
        public String word1;
        public String color1;
        public Boolean uploaded;
    }
}

