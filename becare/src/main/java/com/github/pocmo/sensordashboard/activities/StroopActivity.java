package com.github.pocmo.sensordashboard.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

import com.github.pocmo.sensordashboard.R;

import java.util.Random;

/**
 * Created by qtxdev on 8/9/2016.
 */
public class StroopActivity extends AppCompatActivity {
    private final int TRIALS = 14;
    private ImageView numberImg;
    private TextView message;
    private ImageView match;
    private LinearLayout button;
    private LinearLayout test;
    private LinearLayout number;
    private TextView color1, color2;
    private String[] colors = {"Green", "Blue", "Red", "Purple", "Yellow", "Black", "Gray",  "Brown"};

    private Boolean[] answers = new Boolean[TRIALS];
    private CountDownTimer cTimer = null;
    private CountDownTimer cTimer2 = null;
    private CountDownTimer ringTimer = null;
    private Animation mLoadAnimation = null;
    private Button matchButton;
    private int count = 0;
    private int answerIndex =0;
    private Ringtone ring;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        customTitleBar();
        setContentView(R.layout.activity_stroop_test);
        numberImg = (ImageView) findViewById(R.id.one);
        message = (TextView) findViewById(R.id.msg);
      //  match = (ImageView) findViewById(R.id.matchOrNo);
     //   match.setImageResource(R.drawable.yes);

        test = (LinearLayout) findViewById(R.id.test);
        number = (LinearLayout) findViewById(R.id.number);

        color1 = (TextView) findViewById(R.id.color1);
        color2 = (TextView) findViewById(R.id.color2);

        matchButton =(Button)findViewById(R.id.matchButton);

        Uri notification  = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_ALARM);
        ring = RingtoneManager.getRingtone(getApplicationContext(), notification);

        button = (LinearLayout)findViewById(R.id.button);
        Button start = (Button)findViewById(R.id.start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setVisibility(View.GONE);
                number.setVisibility(View.VISIBLE);
                numberImg.setVisibility(View.VISIBLE);
                numberImg.setImageResource(R.drawable.one);
                ValueAnimator valueAnimator1 = ValueAnimator.ofInt(0,2);
                final ValueAnimator valueAnimator2 = ValueAnimator.ofInt(0,2);
                final ValueAnimator valueAnimator3 = ValueAnimator.ofInt(0,2);
                 mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                mLoadAnimation.setDuration(1000);
                valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float animProgress =  animation.getAnimatedFraction();
                        numberImg.setAlpha(animation.getAnimatedFraction());

                    }
                });
                valueAnimator1.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        numberImg.setImageResource(R.drawable.two);
                        valueAnimator2.start();
                    }
                });


                valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float animProgress =  animation.getAnimatedFraction();
                        numberImg.setAlpha(animation.getAnimatedFraction());

                    }
                });
                valueAnimator2.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        numberImg.setImageResource(R.drawable.three);
                        valueAnimator3.start();
                     /*   test.setVisibility(View.VISIBLE);
                        color1.startAnimation(mLoadAnimation);
                        matchButton.setEnabled(false);*/
                    }
                });

                valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float animProgress =  animation.getAnimatedFraction();
                        numberImg.setAlpha(animation.getAnimatedFraction());

                    }
                });
                valueAnimator3.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                       // button.setVisibility(View.VISIBLE);
                        message.setText("00:03");
                        number.setVisibility(View.GONE);
                        test.setVisibility(View.VISIBLE);
                        for (int i=0; i<answers.length; i++)
                            answers[i] = false;
                        setWord();
                        cTimer.start();
                    }
                });

                valueAnimator1.setDuration(500);
                valueAnimator1.setRepeatCount(1);
                valueAnimator1.setRepeatMode(ValueAnimator.REVERSE);

                valueAnimator2.setDuration(500);
                valueAnimator2.setRepeatCount(1);
                valueAnimator2.setRepeatMode(ValueAnimator.REVERSE);

                valueAnimator3.setDuration(500);
                valueAnimator3.setRepeatCount(1);
                valueAnimator3.setRepeatMode(ValueAnimator.REVERSE);

                message.setTextSize(25);
                message.setText("Get Ready");
                valueAnimator1.start();

            }
        });

        cTimer = new CountDownTimer(3700, 500) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                float sec = (int) (millisUntilFinished / 1000);

                if (sec == 3 || sec == 2 || sec == 1) {
                    int d = (int)sec;
                    String str = String.format("00:0%d", d);
                    message.setText(str);
                }
            }

            public void onFinish() {//   startMeasure = false;

                if (count < TRIALS) {
                  //  setWord();
                    message.setText("Next...");
                    color1.setText("");
                    color2.setText("");
                    color1.startAnimation(mLoadAnimation);
                    color2.startAnimation(mLoadAnimation);
                    cTimer2.start();
                }
                else{
                    int correct = 0;
                    for (int i=0; i<answers.length; i++)
                        if (answers[i])
                            correct++;
                    double score = (double)correct / (double)answers.length * 100;
                    int s = (int)score;
                    String str = String.format("Congratulations!You are Done.\n Total tests:%d \n Correct answers: %d\n Score: %d", answers.length,correct, s );
                    str += "%";
                    message.setText(str);
                    message.setTextSize(17);
                    test.setVisibility(View.GONE);
                    button.setVisibility(View.VISIBLE);
                    count = 0;
                }
            }
        };

        cTimer2 = new CountDownTimer(1500, 1000) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {
                float sec = (int) (millisUntilFinished / 1000);

            }

            public void onFinish() {//   startMeasure = false;
                message.setText("00:03");
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

    public void matchTest(View view) {
        String word1 = color1.getText().toString();
        String word2 = color2.getText().toString();

        if (word1.equals("") || word2.equals(""))
            return;

        if (word1.equals(word2))
            answers[count-1] = true;
        else
            answers[count-1] = false;

    }

    public void noMatchTest(View view) {
        String word1 = color1.getText().toString();
        String word2 = color2.getText().toString();

        if (word1.equals("") || word2.equals(""))
            return;

        if (!word1.equals(word2))
            answers[count-1] = true;
        else
            answers[count-1] = false;
    }

    private void setWord()
    {
        String word1, word2, colorName1=null, colorName2=null;

        int index = randomGen();
        if (index >= colors.length)
            return;
        word1 = colors[index];

        Boolean yes = genIndexLeft();
        if (yes)
            word2 = word1;
        else {
            index = randomGen();
            while (colors[index] == word1)
                index = randomGen();
            word2 = colors[index];
        }

        //get color index for word1
        yes = genIndexLeft();
        if (yes)
            colorName1 = word1;
        else{
            while (colors[index] == word1)
                index = randomGen();
            colorName1 = colors[index];
        }

        //get color index for word2
        index = randomGen();
        while (colors[index] == colorName1)
            index = randomGen();
        colorName2 = colors[index];

        ring.play();
        ringTimer.start();

        color1.setText(word1);
        color2.setText(word2);

        setWordColor(colorName1, color1);
        setWordColor(colorName2, color2);

      //  color1.setVisibility(View.VISIBLE);
      //  color2.setVisibility(View.VISIBLE);
        color1.startAnimation(mLoadAnimation);
        color2.startAnimation(mLoadAnimation);
        count++;
    }

    private void setWordColor(String word, TextView color)
    {
        if (word == "Green")
            color.setTextColor(Color.rgb(10, 160, 92));

        if (word == "Red")
            color.setTextColor(Color.rgb(215, 58, 49));

        if (word == "Blue")
            color.setTextColor(Color.rgb(63, 81,181));

        if (word == "Yellow")
            color.setTextColor(Color.rgb(255, 193, 7));

        if (word == "Black")
            color.setTextColor(Color.rgb(0, 0, 0));

        if (word == "Purple")
            color.setTextColor(Color.rgb(156, 39, 176));

        if (word == "Brown")
            color.setTextColor(Color.rgb(153, 76, 0));

        if (word == "Gray")
            color.setTextColor(Color.rgb(110, 110, 110));
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
        Random r = new Random();
        int ran = 0;
        ran = r.nextInt(max - min + 1) + min;
        return ran;
    }
}

