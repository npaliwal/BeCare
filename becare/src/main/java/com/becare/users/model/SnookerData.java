package com.becare.users.model;


/**
 * Created by neerajpaliwal on 21/05/16.
 */
public class SnookerData extends ActivityData {
    int xTouch;

    int yTouch;

    int xPath;

    public SnookerData(){
        super("Snooker");
    }

    public void setValues(int xTouch, int yTouch, int xPath){
        this.xPath = xPath;
        this.xTouch = xTouch;
        this.yTouch = yTouch;
    }
}
