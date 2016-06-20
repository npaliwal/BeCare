package com.github.pocmo.sensordashboard.model;

/**
 * Created by neerajpaliwal on 19/06/16.
 */
public class AudioData {
    private int speechResId;
    private String textRes;

    public AudioData(int sResId, String tRes) {
        this.speechResId = sResId;
        this.textRes = tRes;
    }

    public String getTextRes() {
        return textRes;
    }

    public void setTextResId(String textRes) {
        this.textRes = textRes;
    }

    public int getSpeechResId() {
        return speechResId;
    }

    public void setSpeechResId(int speechResId) {
        this.speechResId = speechResId;
    }
}
