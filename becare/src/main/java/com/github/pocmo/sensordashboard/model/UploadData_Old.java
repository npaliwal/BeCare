package com.github.pocmo.sensordashboard.model;


/**
 * Created by neerajpaliwal on 21/05/16.
 */
public class UploadData_Old {
    String deviceId;

    String source;

    String readTime;

    String readDate;

    ActivityData activityData;

    SensorDataWrapper gyroMeter;

    SensorDataWrapper accelMeter;

    public UploadData_Old(){
        gyroMeter = new SensorDataWrapper();
        accelMeter = new SensorDataWrapper();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getReadTime() {
        return readTime;
    }

    public void setReadTime(String readTime) {
        this.readTime = readTime;
    }

    public String getReadDate() {
        return readDate;
    }

    public void setReadDate(String readDate) {
        this.readDate = readDate;
    }

    public ActivityData getActivityData() {
        return activityData;
    }

    public void setActivityData(ActivityData activityData) {
        this.activityData = activityData;
    }

    public SensorDataWrapper getGyroMeter() {
        return gyroMeter;
    }

    public void setGyroMeter(SensorDataWrapper gyroMeter) {
        this.gyroMeter = gyroMeter;
    }

    public SensorDataWrapper getAccelMeter() {
        return accelMeter;
    }

    public void setAccelMeter(SensorDataWrapper accelMeter) {
        this.accelMeter = accelMeter;
    }


}
