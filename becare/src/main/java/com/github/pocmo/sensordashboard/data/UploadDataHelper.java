package com.github.pocmo.sensordashboard.data;

import android.hardware.Sensor;
import android.util.Log;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.model.ActivityData;
import com.github.pocmo.sensordashboard.model.ActivityUploadData;
import com.github.pocmo.sensordashboard.model.SensorDataValue;
import com.github.pocmo.sensordashboard.model.SensorDataWrapper;
import com.github.pocmo.sensordashboard.model.SensorUploadData;
import com.github.pocmo.sensordashboard.model.UploadData_Old;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by neerajpaliwal on 06/04/16.
 */
public class UploadDataHelper {
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    String deviceId;

    String source;

    Gson gson = new Gson();

    String readTime;

    String activityName;
    String activityValue;

    SensorDataWrapper gyroMeter;

    SensorDataWrapper accelMeter;

    //Helper data
    private List<SensorDataValue> allGyroData = new ArrayList<>();
    private List<SensorDataValue> allAcceleroData = new ArrayList<>();

    public UploadDataHelper(){
        gyroMeter = new SensorDataWrapper();
        accelMeter = new SensorDataWrapper();
    }

    public void setDeviceId(String deviceId){
        this.deviceId = deviceId.trim().replaceAll(" ", "");
    }

    public void setUserActivity(String activityName, String activityValue){
        this.activityName = activityName;
        this.activityValue = activityValue;
    }

    public String getUserActivityName(){
        return activityName;
    }

    public void addDataValue(SensorDataValue data, int type){
        if(type == Sensor.TYPE_GYROSCOPE){
            allGyroData.add(data);
        }
        if(type == Sensor.TYPE_ACCELEROMETER){
            allAcceleroData.add(data);
        }
    }

    /*
        This function calculates mean, high & low in single iteration
        High: Highest of all Xs, Ys & Zs
        Low:  Lowest of all Xs, Ys & Zs
        Mean: Add all Xs and divide by num values in an interval
    */
    private void calculateMeanHighLow(){
        gyroMeter.resetMean();
        accelMeter.resetMean();

        if(allGyroData.size() > 0){
            gyroMeter.setHigh(allGyroData.get(0));
            gyroMeter.setLow(allGyroData.get(0));
        }
        for(SensorDataValue data : allGyroData){
            gyroMeter.addToMean(data);
            gyroMeter.setHigh(data);
            gyroMeter.setLow(data);
        }

        if(allAcceleroData.size() > 0){
            accelMeter.setHigh(allAcceleroData.get(0));
            accelMeter.setLow(allAcceleroData.get(0));
        }
        for(SensorDataValue data : allAcceleroData){
            accelMeter.addToMean(data);
            accelMeter.setHigh(data);
            accelMeter.setLow(data);
        }
        if(allGyroData.size() > 0)
            gyroMeter.normalizeMean(allGyroData.size());

        if(allAcceleroData.size() > 0)
            accelMeter.normalizeMean(allAcceleroData.size());
    }

    /*
       This function calculates number zero crossing in single iteration (after mean is calculated)
       If ANY OF the Xs, Ys or Zs has crossed median value then we count it as zero crossing
       e.g. median is (0f, 0f, 0f) and previous sensor data is (-1f, 1f, 1f)
       then next data = (1f, 1f, 1f) => zero crossing as X has crossed meanX value
       similarly next data = (-1f, -1f, 1f) => zero crossing as Y has crossed meanY value
   */
    private void calculateNumZeroCrossing(){
        gyroMeter.setZcCount(0);
        accelMeter.setZcCount(0);

        SensorDataValue n1=null, nPlus1=null;
        SensorDataValue meanGyroData = gyroMeter.getMean();
        for(SensorDataValue data : allGyroData){
            n1 = nPlus1;
            nPlus1 = data;
            if(n1 != null && nPlus1 != null){
                if((meanGyroData.getValueX() - n1.getValueX()) * (meanGyroData.getValueX() - nPlus1.getValueX()) < 0 ||
                        (meanGyroData.getValueY() - n1.getValueY()) * (meanGyroData.getValueY() - nPlus1.getValueY()) < 0 ||
                        (meanGyroData.getValueZ() - n1.getValueZ()) * (meanGyroData.getValueZ() - nPlus1.getValueZ()) < 0){
                    gyroMeter.increaseZeroCrossingCount();
                }
            }
        }

        SensorDataValue meanAccelData = accelMeter.getMean();
        for(SensorDataValue data : allAcceleroData){
            n1 = nPlus1;
            nPlus1 = data;
            if(n1 != null && nPlus1 != null){
                if((meanAccelData.getValueX() - n1.getValueX()) * (meanAccelData.getValueX() - nPlus1.getValueX()) < 0 ||
                        (meanAccelData.getValueY() - n1.getValueY()) * (meanAccelData.getValueY() - nPlus1.getValueY()) < 0 ||
                        (meanAccelData.getValueZ() - n1.getValueZ()) * (meanAccelData.getValueZ() - nPlus1.getValueZ()) < 0){
                    accelMeter.increaseZeroCrossingCount();
                }
            }
        }

    }

    public String getSensorUploadData(int sensorType, int cord){
        SensorDataWrapper wrapper = sensorType == Sensor.TYPE_ACCELEROMETER ? accelMeter : gyroMeter;
        int numSample = sensorType == Sensor.TYPE_ACCELEROMETER ? allAcceleroData.size() : allGyroData.size();
        String sensorName = sensorType == Sensor.TYPE_ACCELEROMETER ? "accelerometer" : "gyroscope";

        SensorUploadData data = new SensorUploadData(sensorName, wrapper, numSample, cord, readTime, deviceId);

        return gson.toJson(data, SensorUploadData.class);
    }


    public void calculateStats(long timeStamp){
        readTime = timeFormat.format(timeStamp);
        calculateMeanHighLow();
        calculateNumZeroCrossing();
    }

    public void resetStats(){
        allAcceleroData.clear();
        allGyroData.clear();
    }

    public String getUploadDataStr(long timeStamp){
//        readTime = timeFormat.format(timeStamp);
//
//        calculateMeanHighLow();
//        calculateNumZeroCrossing();
//
//        //String ret = new HiveHelper().formatUploadData(this);
//        //String ret = uploadData.toString();
//        String ret = gson.toJson(uploadData, UploadData_Old.class);
//
//        allAcceleroData.clear();
//        allGyroData.clear();
//        Log.d("upload data", ret);
//        return ret;
        return "this function is deprecated";
    }

    public String getUserActivityData() {
        ActivityUploadData data = new ActivityUploadData(activityName, deviceId, readTime, activityValue);

        return gson.toJson(data, ActivityUploadData.class);
    }
}
