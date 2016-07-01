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
import java.util.Arrays;
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

    int seq = -1;

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
        float totalX = 0;
        float totalY = 0;
        float totalZ = 0;
        for(SensorDataValue data : allGyroData){
            gyroMeter.addToMean(data);
            gyroMeter.setHigh(data);
            gyroMeter.setLow(data);
            totalX += data.getValueX();
            totalY += data.getValueY();
            totalZ += data.getValueZ();
        }
        if(allGyroData.size() > 0) {
            float gavgX = totalX / allGyroData.size();
            float gavgY = totalY / allGyroData.size();
            float gavgZ = totalZ / allGyroData.size();
            gyroMeter.setMean(gavgX, gavgY, gavgZ);
        }
        if(allAcceleroData.size() > 0){
            accelMeter.setHigh(allAcceleroData.get(0));
            accelMeter.setLow(allAcceleroData.get(0));
        }

        totalX = 0;
        totalY = 0;
        totalZ = 0;
        for(SensorDataValue data : allAcceleroData){
            accelMeter.addToMean(data);
            accelMeter.setHigh(data);
            accelMeter.setLow(data);
            totalX += data.getValueX();
            totalY += data.getValueY();
            totalZ += data.getValueZ();
        }
        if(allAcceleroData.size() > 0) {
            float aavgX = totalX / allAcceleroData.size();
            float aavgY = totalY / allAcceleroData.size();
            float aavgZ = totalZ / allAcceleroData.size();
            accelMeter.setMean(aavgX, aavgY, aavgZ);
        }
    }

    private void calculateStd(){
        float avgX = gyroMeter.getMean().getValueX();
        float avgY = gyroMeter.getMean().getValueY();
        float avgZ = gyroMeter.getMean().getValueZ();
        float stdX = 0;
        float stdY = 0;
        float stdZ = 0;
        for(SensorDataValue data : allGyroData){
            stdX += Math.pow((data.getValueX() - avgX), 2);
            stdY += Math.pow((data.getValueY() - avgY), 2);
            stdZ += Math.pow((data.getValueZ() - avgZ), 2);
        }
        double volaX = (allGyroData.size() > 0)? Math.sqrt(stdX/allGyroData.size()): 0;
        double volaY =(allGyroData.size() > 0)? Math.sqrt(stdY/allGyroData.size()) : 0;
        double volaZ = (allGyroData.size() > 0)? Math.sqrt(stdZ/allGyroData.size()): 0;
        gyroMeter.setStd((float) volaX, (float) volaY, (float) volaZ);

        avgX = accelMeter.getMean().getValueX();
        avgY = accelMeter.getMean().getValueY();
        avgZ = accelMeter.getMean().getValueZ();
        stdX = 0;
        stdY = 0;
        stdZ = 0;
        for(SensorDataValue data : allAcceleroData){
            stdX += Math.pow((data.getValueX() - avgX), 2);
            stdY += Math.pow((data.getValueY() - avgY), 2);
            stdZ += Math.pow((data.getValueZ() - avgZ), 2);
        }
        volaX = (allAcceleroData.size() > 0)? Math.sqrt(stdX/allAcceleroData.size()):0;
        volaY = (allAcceleroData.size() > 0)? Math.sqrt(stdY/allAcceleroData.size()): 0;
        volaZ = (allAcceleroData.size() > 0)? Math.sqrt(stdZ/allAcceleroData.size()): 0;
        accelMeter.setStd((float)volaX, (float)volaY, (float)volaZ);
    }

    /*
       This function calculates number zero crossing in single iteration (after mean is calculated)
       If ANY OF the Xs, Ys or Zs has crossed median value then we count it as zero crossing
       e.g. median is (0f, 0f, 0f) and previous sensor data is (-1f, 1f, 1f)
       then next data = (1f, 1f, 1f) => zero crossing as X has crossed meanX value
       similarly next data = (-1f, -1f, 1f) => zero crossing as Y has crossed meanY value
   */
    private void calculateNumZeroCrossing(){
        if (allGyroData.size() <2)
            gyroMeter.setZcCount(0, 0, 0);
        else {
            float preX = allGyroData.get(0).getValueX();
            float preY = allGyroData.get(0).getValueY();
            float preZ = allGyroData.get(0).getValueZ();
            int zctX = 0;
            int zctY = 0;
            int zctZ = 0;
            float avgY = gyroMeter.getMean().getValueY();
            for (int i = 1; i < allGyroData.size(); i++) {
                SensorDataValue data = allGyroData.get(i);
                float currX = data.getValueX();
                float currY = data.getValueY();
                float currZ = data.getValueZ();
                if (currX > gyroMeter.getMean().getValueX() && preX < gyroMeter.getMean().getValueX())
                    zctX++;
                else if (currX < gyroMeter.getMean().getValueX() && preX > gyroMeter.getMean().getValueX())
                    zctX++;

                if (currY > avgY && preY < avgY)
                    zctY++;
                else if (currY < avgY && preY  > avgY)
                    zctY++;
                if (currZ > gyroMeter.getMean().getValueZ() && preZ < gyroMeter.getMean().getValueZ())
                    zctZ++;
                else if (currZ < gyroMeter.getMean().getValueZ() && preZ > gyroMeter.getMean().getValueZ())
                    zctZ++;
                preX = currX;
                preY = currY;
                preZ = currZ;
            }
            gyroMeter.setZcCount(zctX, zctY, zctZ);
        }

        if (allAcceleroData.size() <2)
            accelMeter.setZcCount(0, 0, 0);
        else {
            float preX = allAcceleroData.get(0).getValueX();
            float preY = allAcceleroData.get(0).getValueY();
            float preZ = allAcceleroData.get(0).getValueZ();
            int zctX = 0;
            int zctY = 0;
            int zctZ = 0;
            for (int i = 1; i < allAcceleroData.size(); i++) {
                SensorDataValue data = allAcceleroData.get(i);
                float currX = data.getValueX();
                float currY = data.getValueY();
                float currZ = data.getValueZ();
                if (currX > accelMeter.getMean().getValueX() && preX < accelMeter.getMean().getValueX())
                    zctX++;
                else if (currX < accelMeter.getMean().getValueX() && preX > accelMeter.getMean().getValueX())
                    zctX++;
                if (currY > accelMeter.getMean().getValueY() && preY < accelMeter.getMean().getValueY())
                    zctY++;
                else if (currY < accelMeter.getMean().getValueY() && preY > accelMeter.getMean().getValueY())
                    zctY++;
                if (currZ > accelMeter.getMean().getValueZ() && preZ < accelMeter.getMean().getValueZ())
                    zctZ++;
                else if (currZ < gyroMeter.getMean().getValueZ() && preZ > gyroMeter.getMean().getValueZ())
                    zctZ++;
               preX = currX;
                preY = currY;
                preZ = currZ;
            }
            accelMeter.setZcCount(zctX, zctY, zctZ);
        }
    }

    public String getSensorUploadData(int sensorType, int cord){
        SensorDataWrapper wrapper = sensorType == Sensor.TYPE_ACCELEROMETER ? accelMeter : gyroMeter;
        int numSample = sensorType == Sensor.TYPE_ACCELEROMETER ? allAcceleroData.size() : allGyroData.size();
        String sensorName = sensorType == Sensor.TYPE_ACCELEROMETER ? "accelerometer" : "gyroscope";
        if (numSample <=0)
            return "";

        float[] gX =null;
        float[] gY =null;
        float[] gZ =null;
        float[] vector = null;
        if (sensorType == Sensor.TYPE_GYROSCOPE) {
            int len = allGyroData.size();
            if (len > 0) {
                gX = new float[len];
                gY = new float[len];
                gZ = new float[len];
                int i = 0;
                for (SensorDataValue data : allGyroData) {
                    gX[i] = data.getRoundX();
                    gY[i] = data.getRoundY();
                    gZ[i] = data.getRoundZ();
                    i++;
                }

                if (cord == AppConfig.X_CORD)
                    vector = gX;
                if (cord == AppConfig.Y_CORD)
                    vector = gY;
                if (cord == AppConfig.Z_CORD)
                    vector = gZ;
            }
        }
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            int len = allAcceleroData.size();
            if (len > 0) {
                gX = new float[len];
                gY = new float[len];
                gZ = new float[len];
                int i = 0;
                for (SensorDataValue data : allAcceleroData) {
                    gX[i] = data.getRoundX();
                    gY[i] = data.getRoundY();
                    gZ[i] = data.getRoundZ();
                    i++;
                }

                if (cord == AppConfig.X_CORD)
                    vector = gX;
                if (cord == AppConfig.Y_CORD)
                    vector = gY;
                if (cord == AppConfig.Z_CORD)
                    vector = gZ;
            }
        }
        SensorUploadData data = new SensorUploadData(sensorName, wrapper, numSample, cord, readTime, deviceId, vector, seq);

        return gson.toJson(data, SensorUploadData.class);
    }


    public void calculateStats(long timeStamp){
        readTime = timeFormat.format(timeStamp);
        calculateMeanHighLow();
        calculateStd();
        calculateNumZeroCrossing();
        seq++;
    }

    public void resetStats(){
        allAcceleroData.clear();
        allGyroData.clear();
        gyroMeter.reset();
        accelMeter.reset();
    }

    @Deprecated
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
        ActivityUploadData data = new ActivityUploadData(activityName, deviceId, readTime, activityValue, seq);

        return gson.toJson(data, ActivityUploadData.class);
    }

    public String getUserActivityData(String activityVal) {
        readTime = timeFormat.format(System.currentTimeMillis());

        ActivityUploadData data = new ActivityUploadData(activityName, deviceId, readTime, activityVal, seq);

        return gson.toJson(data, ActivityUploadData.class);
    }

    public void resetSeuenceCounter(){
        seq = -1;
    }
}
