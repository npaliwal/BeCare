package com.becare.users.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by neerajpaliwal on 27/05/16.
 */
public class ActivityUploadData {
    @SerializedName("activityname")
    String activityName;

  //  @SerializedName("activitymsg")
 //   String activityMsg;//: activityname.time

    @SerializedName("time")
    String time;

    @SerializedName("device")
    String device;

    @SerializedName("value")
    String activityValue;

    @SerializedName("seq")
    Integer sequenceNumber;

    @SerializedName("duration")
    long duration;

    public ActivityUploadData(String activity, String deviceId, String time, String value, Integer seq, long dur){
        this.time = time;
        this.device = deviceId;
        this.activityValue = value;
        this.activityName = activity;
     //   this.activityMsg = activity + "." + time;
        this.sequenceNumber = seq;
        duration = dur;
    }
}
