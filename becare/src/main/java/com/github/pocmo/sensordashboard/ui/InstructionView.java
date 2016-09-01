package com.github.pocmo.sensordashboard.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by neerajpaliwal on 27/08/16.
 */
public class InstructionView extends TextView {
    private long timeLapsed = 0, startTime = 0;
    private String instruction = "";
    private String message = "";

    public enum STATE {
        SHOW_INSTRUCTION,
        SHOW_TIMER,
        PAUSE_TIMER,
        LARGE_TEXT,
        SMALL_TEXT
    }

    STATE state = STATE.SHOW_INSTRUCTION;

    public InstructionView(Context context) {
        super(context);
    }

    public InstructionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InstructionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void resetTimer(){
        timeLapsed = 0;
        startTime = System.currentTimeMillis();
    }

    public void setState(STATE state){
        this.state =state;
    }

    public STATE getState(){
        return state;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public void setMessage(String message, boolean isSmall) {
        this.message = message;
        if(isSmall)
            this.state = STATE.SMALL_TEXT;
        else
            this.state = STATE.LARGE_TEXT;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (state){
            case SHOW_INSTRUCTION: //Instruction
                this.setTextSize(18);
                this.setText(instruction);
                break;

            case LARGE_TEXT:
                this.setTextSize(24);
                this.setText(message);
                break;


            case SMALL_TEXT:
                this.setTextSize(18);
                this.setText(message);
                break;


            case SHOW_TIMER:
                this.setTextSize(24);
                timeLapsed = System.currentTimeMillis() - this.startTime;
                this.setText(formatTime(timeLapsed));
                break;

            case PAUSE_TIMER:
                this.setTextSize(24);
                this.setText(formatTime(timeLapsed));
                break;
        }
    }

    private String formatTime(long millis) {

        String output = "00:00:00";
        try {
            long seconds = millis / 1000;
            long minutes = seconds / 60;
            long hours = seconds / 3600;
            long days = seconds / (3600 * 24);

            seconds = seconds % 60;
            minutes = minutes % 60;
            hours = hours % 24;
            days = days % 30;

            String sec = String.valueOf(seconds);
            String min = String.valueOf(minutes);
            String hur = String.valueOf(hours);
            String day = String.valueOf(days);

            if (seconds < 10)
                sec = "0" + seconds;
            if (minutes < 10)
                min = "0" + minutes;
            if (hours < 10)
                hur = "0" + hours;
            if (days < 10)
                day = "0" + days;

            output = hur + ":" + min + ":" + sec;
            if(days > 0) {
                output = day + "D " + output;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }
}
