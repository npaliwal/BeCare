package com.github.pocmo.sensordashboard.model;

/**
 * Created by neerajpaliwal on 19/06/16.
 */
public class AudioData {
    private int speechResId;
    private int textResId;

    public AudioData(int sResId, int tResId) {
        this.speechResId = sResId;
        this.textResId = tResId;
    }

    public int getTextResId() {
        return textResId;
    }

    public void setTextResId(int textResId) {
        this.textResId = textResId;
    }

    public int getSpeechResId() {
        return speechResId;
    }

    public void setSpeechResId(int speechResId) {
        this.speechResId = speechResId;
    }
}
