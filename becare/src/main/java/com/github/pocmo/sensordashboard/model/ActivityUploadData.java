package com.github.pocmo.sensordashboard.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by neerajpaliwal on 27/05/16.
 */
public class ActivityUploadData {
    @SerializedName("activityname")
    String activityName;

    @SerializedName("activitymsg")
    String activityMsg;//: activityname.time

    @SerializedName("time")
    String time;

    @SerializedName("deviceid")
    String deviceId;

    @SerializedName("value")
    String activityValue;

    public ActivityUploadData(String activity, String deviceId, String time, String value){
        this.time = time;
        this.deviceId = deviceId;
        this.activityValue = value;
        this.activityName = activity;
        this.activityMsg = activity + "." + time;
    }
}
