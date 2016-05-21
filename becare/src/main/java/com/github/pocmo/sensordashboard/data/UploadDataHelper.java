package com.github.pocmo.sensordashboard.data;

import android.hardware.Sensor;
import android.util.Log;

import com.github.pocmo.sensordashboard.model.ActivityData;
import com.github.pocmo.sensordashboard.model.SensorDataValue;
import com.github.pocmo.sensordashboard.model.UploadData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neerajpaliwal on 06/04/16.
 */
public class UploadDataHelper {
    private UploadData uploadData;

    //Helper data
    private List<SensorDataValue> allGyroData = new ArrayList<>();
    private List<SensorDataValue> allAcceleroData = new ArrayList<>();

    public UploadDataHelper(){
        this.uploadData = new UploadData();
    }

    public void setDeviceId(String deviceId){
        this.uploadData.setDeviceId(deviceId.trim().replaceAll(" ", ""));
    }

    public void setUserActivity(ActivityData activityData){
        this.uploadData.setActivityData(activityData);
    }

    public ActivityData getUserActivityData(){
        return uploadData.getActivityData();
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
        uploadData.getGyroMeter().resetMean();
        uploadData.getAccelMeter().resetMean();

        if(allGyroData.size() > 0){
            uploadData.getGyroMeter().setHigh(allGyroData.get(0));
            uploadData.getGyroMeter().setLow(allGyroData.get(0));
        }
        for(SensorDataValue data : allGyroData){
            uploadData.getGyroMeter().addToMean(data);
            uploadData.getGyroMeter().setHigh(data);
            uploadData.getGyroMeter().setLow(data);
        }

        if(allAcceleroData.size() > 0){
            uploadData.getAccelMeter().setHigh(allAcceleroData.get(0));
            uploadData.getAccelMeter().setLow(allAcceleroData.get(0));
        }
        for(SensorDataValue data : allAcceleroData){
            uploadData.getAccelMeter().addToMean(data);
            uploadData.getAccelMeter().setHigh(data);
            uploadData.getAccelMeter().setLow(data);
        }
        if(allGyroData.size() > 0)
            uploadData.getGyroMeter().normalizeMean(allGyroData.size());

        if(allAcceleroData.size() > 0)
            uploadData.getAccelMeter().normalizeMean(allAcceleroData.size());
    }

    /*
       This function calculates number zero crossing in single iteration (after mean is calculated)
       If ANY OF the Xs, Ys or Zs has crossed median value then we count it as zero crossing
       e.g. median is (0f, 0f, 0f) and previous sensor data is (-1f, 1f, 1f)
       then next data = (1f, 1f, 1f) => zero crossing as X has crossed meanX value
       similarly next data = (-1f, -1f, 1f) => zero crossing as Y has crossed meanY value
   */
    private void calculateNumZeroCrossing(){
        uploadData.getGyroMeter().setZcCount(0);
        uploadData.getAccelMeter().setZcCount(0);

        SensorDataValue n1=null, nPlus1=null;
        SensorDataValue meanGyroData = uploadData.getGyroMeter().getMean();
        for(SensorDataValue data : allGyroData){
            n1 = nPlus1;
            nPlus1 = data;
            if(n1 != null && nPlus1 != null){
                if((meanGyroData.getValueX() - n1.getValueX()) * (meanGyroData.getValueX() - nPlus1.getValueX()) < 0 ||
                        (meanGyroData.getValueY() - n1.getValueY()) * (meanGyroData.getValueY() - nPlus1.getValueY()) < 0 ||
                        (meanGyroData.getValueZ() - n1.getValueZ()) * (meanGyroData.getValueZ() - nPlus1.getValueZ()) < 0){
                    uploadData.getGyroMeter().increaseZeroCrossingCount();
                }
            }
        }

        SensorDataValue meanAccelData = uploadData.getAccelMeter().getMean();
        for(SensorDataValue data : allAcceleroData){
            n1 = nPlus1;
            nPlus1 = data;
            if(n1 != null && nPlus1 != null){
                if((meanAccelData.getValueX() - n1.getValueX()) * (meanAccelData.getValueX() - nPlus1.getValueX()) < 0 ||
                        (meanAccelData.getValueY() - n1.getValueY()) * (meanAccelData.getValueY() - nPlus1.getValueY()) < 0 ||
                        (meanAccelData.getValueZ() - n1.getValueZ()) * (meanAccelData.getValueZ() - nPlus1.getValueZ()) < 0){
                    uploadData.getAccelMeter().increaseZeroCrossingCount();
                }
            }
        }

    }

    public String getUploadDataStr(){
        calculateMeanHighLow();
        calculateNumZeroCrossing();

        //String ret = new HiveHelper().formatUploadData(this);
        //String ret = uploadData.toString();
        Gson gson = new Gson();
        String ret = gson.toJson(uploadData, UploadData.class);


        allAcceleroData.clear();
        allGyroData.clear();
        Log.d("upload data", ret);
        return ret;
    }

}
