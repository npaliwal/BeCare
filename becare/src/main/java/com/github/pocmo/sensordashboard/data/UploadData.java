package com.github.pocmo.sensordashboard.data;

import android.hardware.*;
import android.hardware.Sensor;
import android.util.Log;

import com.github.pocmo.sensordashboard.network.HiveHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by neerajpaliwal on 06/04/16.
 */
public class UploadData {
    //Helper data
    private SensorData lastGyroData, highGyroData, lowGyroData, meanGyroData, medianGyroData;
    private SensorData lastAccelData, highAccelData, lowAccelData, meanAccelData, medianAccelData;
    private List<SensorData> allGyroData = new ArrayList<>();
    private List<SensorData> allAcceleroData = new ArrayList<>();

    //Stats to be uploaded
    private String deviceId;
    private String userActivity="NA";
    private int numGyroZeroCrossing=0;
    private int numAcceleroZeroCrossing=0;


    public UploadData(){
        this.deviceId = "unknown";
        meanGyroData = new SensorData();
        meanAccelData = new SensorData();
    }

    public void setDeviceId(String deviceId){
        this.deviceId = deviceId.trim().replaceAll(" ", "");
    }

    public String getDeviceId(){
        return this.deviceId;
    }

    public void setUserActivity(String activity){
        this.userActivity = activity;
    }

    public String getUserActivity(){
        return this.userActivity;
    }

    public void addDataPoint(SensorData data, int type){
        if(type == Sensor.TYPE_GYROSCOPE){
            this.lastGyroData = data;
            allGyroData.add(data);
        }
        if(type == Sensor.TYPE_ACCELEROMETER){
            this.lastAccelData = data;
            allAcceleroData.add(data);
        }
    }

    private void calculateMedian(){
        Collections.sort(allAcceleroData);
        Collections.sort(allGyroData);

        int numAccelData = allAcceleroData.size();
        lowAccelData  = allAcceleroData.get(0);
        highAccelData = allAcceleroData.get(numAccelData - 1);
        medianAccelData = allAcceleroData.get((numAccelData + 1) / 2);

        int numGyroData = allGyroData.size();
        lowGyroData  = allGyroData.get(0);
        highGyroData = allGyroData.get(numGyroData - 1);
        medianGyroData = allGyroData.get((numGyroData + 1)/2);
    }

    /*
        This function calculates mean, high & low in single iteration
        High: Highest of all Xs, Ys & Zs
        Low:  Lowest of all Xs, Ys & Zs
        Mean: Add all Xs and divide by num values in an interval
    */
    private void calculateMeanHighLow(){
        if(allGyroData.size() > 0){
            highGyroData = new SensorData(allGyroData.get(0));
            lowGyroData = new SensorData(allGyroData.get(0));
        }
        for(SensorData data : allGyroData){
            meanGyroData.addData(data);
            highGyroData.setHigh(data);
            lowGyroData.setLow(data);
        }

        if(allAcceleroData.size() > 0){
            highAccelData = new SensorData(allAcceleroData.get(0));
            lowAccelData = new SensorData(allAcceleroData.get(0));
        }
        for(SensorData data : allAcceleroData){
            meanAccelData.addData(data);
            highAccelData.setHigh(data);
            lowAccelData.setLow(data);
        }
        if(allGyroData.size() > 0)
            meanGyroData.divideBy(allGyroData.size());
        else
            meanGyroData.setValues(0f, 0f, 0f);

        if(allAcceleroData.size() > 0)
            meanAccelData.divideBy(allAcceleroData.size());
        else
            meanAccelData.setValues(0f,0f,0f);
    }

    /*
       This function calculates number zero crossing in single iteration (after mean is calculated)
       If ANY OF the Xs, Ys or Zs has crossed median value then we count it as zero crossing
       e.g. median is (0f, 0f, 0f) and previous sensor data is (-1f, 1f, 1f)
       then next data = (1f, 1f, 1f) => zero crossing as X has crossed meanX value
       similarly next data = (-1f, -1f, 1f) => zero crossing as Y has crossed meanY value
   */
    private void calculateNumZeroCrossing(){
        numGyroZeroCrossing = numAcceleroZeroCrossing = 0;
        SensorData n1=null, nPlus1=null;
        for(SensorData data : allGyroData){
            n1 = nPlus1;
            nPlus1 = data;
            if(n1 != null && nPlus1 != null){
                if((meanGyroData.getValueX() - n1.getValueX()) * (meanGyroData.getValueX() - nPlus1.getValueX()) < 0 ||
                        (meanGyroData.getValueY() - n1.getValueY()) * (meanGyroData.getValueY() - nPlus1.getValueY()) < 0 ||
                        (meanGyroData.getValueZ() - n1.getValueZ()) * (meanGyroData.getValueZ() - nPlus1.getValueZ()) < 0){
                    numGyroZeroCrossing++;
                }
            }
        }

        for(SensorData data : allAcceleroData){
            n1 = nPlus1;
            nPlus1 = data;
            if(n1 != null && nPlus1 != null){
                if((meanAccelData.getValueX() - n1.getValueX()) * (meanAccelData.getValueX() - nPlus1.getValueX()) < 0 ||
                        (meanAccelData.getValueY() - n1.getValueY()) * (meanAccelData.getValueY() - nPlus1.getValueY()) < 0 ||
                        (meanAccelData.getValueZ() - n1.getValueZ()) * (meanAccelData.getValueZ() - nPlus1.getValueZ()) < 0){
                    numAcceleroZeroCrossing++;
                }
            }
        }

    }

    public String getGyroFormatData(){
        return  "{high:" + highGyroData.toString() + ", " +
                "low:" + lowGyroData.toString() + ", " +
                "mean:" + meanGyroData.toString() + ", " +
                "zcCount :" + numGyroZeroCrossing+"}";
    }

    public String getAcceleroFormatData(){
        return "{high:" + highAccelData.toString() + ", " +
                "low:" + lowAccelData.toString() + ", " +
                "mean:" + meanAccelData.toString() + ", " +
                "zcCount :" + numAcceleroZeroCrossing+"}";
    }

    public String getUploadData(){
        calculateMeanHighLow();
        calculateNumZeroCrossing();

        String ret = new HiveHelper().formatUploadData(this);

        numAcceleroZeroCrossing = numGyroZeroCrossing = 0;
        allAcceleroData.clear();
        allGyroData.clear();
        return ret;
    }

}
