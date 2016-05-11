package me.smartwatches.becare.data;

import android.util.Log;

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
    private String date;
    private String time;
    private String userActivity;
    private int numGyroZeroCrossing=0;
    private int numAcceleroZeroCrossing=0;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    public UploadData(){
        this.deviceId = "unknown";
        meanGyroData = new SensorData();
        meanAccelData = new SensorData();
    }

    public void setDeviceId(String deviceId){
        this.deviceId = deviceId.trim().replaceAll(" ", "");
    }

    public void setUserActivity(String activity){
        this.userActivity = activity;
    }

    public void update(SensorData g, SensorData a){
        Calendar cal = Calendar.getInstance();
        this.date = dateFormat.format(cal.getTimeInMillis());
        this.time = timeFormat.format(cal.getTimeInMillis());

        if(g != null){
            this.lastGyroData = g;
            allGyroData.add(g);
        }
        if(a != null) {
            this.lastAccelData = a;
            allAcceleroData.add(a);
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

    private String getGyroFormatData(){
        return  "{high:" + highGyroData.toString() + ", " +
                "low:" + lowGyroData.toString() + ", " +
                "mean:" + meanGyroData.toString() + ", " +
                "zcCount :" + numGyroZeroCrossing+"}";
    }

    private String getAcceleroFormatData(){
        return "{high:" + highAccelData.toString() + ", " +
                "low:" + lowAccelData.toString() + ", " +
                "mean:" + meanAccelData.toString() + ", " +
                "zcCount :" + numAcceleroZeroCrossing+"}";
    }

    public String getUploadData(){
        calculateMeanHighLow();
        calculateNumZeroCrossing();

        String ret = "{" +
                "\"apikey\" : \"oNm5GQT3HY47uL8JAFeiyZgpR\"," +
                "\"comb\" : \"devicecmb\"," +
                "\"pod\" : \"readings\"," +
                "\"data\" : \"" + formatSensorStats() + "\"," +
                "\"audience\" : \"Private\"," +
                "\"isActive\" : \"true\"," +
                "\"serviceBranchName\" : \"default\"," +
                "\"podKeyName\" : \"7ZTVQS\"" +

                "}";
        return ret;
    }

    public String formatSensorStats(){
        String gyro = lastGyroData == null ? "NA" : getGyroFormatData();
        String acelro = lastAccelData == null ? "NA" : getAcceleroFormatData();
        String ret = "{" +
                "\\\"deviceId\\\":\\\"" + deviceId + "\\\"," +
                "\\\"activityType\\\":\\\"" + userActivity + "\\\"," +
                "\\\"readDate\\\":\\\"" + date + "\\\"," +
                "\\\"readTime\\\":\\\"" + time + "\\\"," +
                "\\\"gyroMeter\\\":\\\"" + gyro + "\\\"," +
                "\\\"accelMeter\\\":\\\"" +  acelro + "\\\"}";

        Log.d("UploadData", ret);
        numAcceleroZeroCrossing = numGyroZeroCrossing = 0;
        allAcceleroData.clear();
        allGyroData.clear();
        return ret;
    }
}
