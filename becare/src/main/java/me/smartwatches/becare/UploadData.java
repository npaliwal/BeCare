package me.smartwatches.becare;

import android.util.Log;

import com.github.pocmo.sensordashboard.data.SensorDataPoint;

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
    private SensorDataPoint lastGyroData, highGyroData, lowGyroData, medianGyroData;
    private SensorDataPoint lastAccelData, highAccelData, lowAccelData, medianAccelData;
    private List<SensorDataPoint> allGyroData = new ArrayList<SensorDataPoint>();
    private List<SensorDataPoint> allAcceleroData = new ArrayList<SensorDataPoint>();

    //Stats to be uploaded
    private String deviceId;
    private String date;
    private String time;
    private String userActivity;
    private int numGyroZeroCrossing=0;
    private int numAcceleroZeroCrossing=0;
    private float meanGyro[] = {0f, 0f, 0f};
    private float highGyro[] = {0f, 0f, 0f};
    private float lowGyro[] = {0f, 0f, 0f};
    private float meanAccelero[] = {0f, 0f, 0f};
    private float highAccelero[] = {0f, 0f, 0f};
    private float lowAccelero[] = {0f, 0f, 0f};

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    public UploadData(){
        this.deviceId = "unknown";
    }

    public void setDeviceId(String deviceId){
        this.deviceId = deviceId.trim().replaceAll(" ", "");
    }

    public void update(SensorDataPoint g, SensorDataPoint a, String userActivity){
        Calendar cal = Calendar.getInstance();
        this.date = dateFormat.format(cal.getTimeInMillis());
        this.time = timeFormat.format(cal.getTimeInMillis());
        this.userActivity = userActivity;

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
        medianAccelData = allAcceleroData.get((numAccelData + 1)/2);

        int numGyroData = allGyroData.size();
        lowGyroData  = allGyroData.get(0);
        highGyroData = allGyroData.get(numGyroData - 1);
        medianGyroData = allGyroData.get((numGyroData + 1)/2);
    }

    private void calculateMean(){
        if(allGyroData.size() > 0){
            highGyro[0] = lowGyro[0] = allGyroData.get(0).getValues()[0];
            highGyro[1] = lowGyro[1] = allGyroData.get(0).getValues()[1];
            highGyro[2] = lowGyro[2] = allGyroData.get(0).getValues()[2];
        }
        for(SensorDataPoint data : allGyroData){
            meanGyro[0] += data.getValues()[0];
            meanGyro[1] += data.getValues()[1];
            meanGyro[2] += data.getValues()[2];
            if(data.getValues()[0] > highGyro[0])
                highGyro[0] = data.getValues()[0];
            if(data.getValues()[0] < lowGyro[0])
                lowGyro[0] = data.getValues()[0];

            if(data.getValues()[1] > highGyro[1])
                highGyro[1] = data.getValues()[1];
            if(data.getValues()[1] < lowGyro[1])
                lowGyro[1] = data.getValues()[1];

            if(data.getValues()[2] > highGyro[2])
                highGyro[2] = data.getValues()[2];
            if(data.getValues()[2] < lowGyro[2])
                lowGyro[2] = data.getValues()[2];
        }

        if(allAcceleroData.size() > 0){
            highAccelero[0] = lowAccelero[0] = allAcceleroData.get(0).getValues()[0];
            highAccelero[1] = lowAccelero[0] = allAcceleroData.get(0).getValues()[1];
            highAccelero[2] = lowAccelero[0] = allAcceleroData.get(0).getValues()[2];
        }
        for(SensorDataPoint data : allAcceleroData){
            meanAccelero[0] += data.getValues()[0];
            meanAccelero[1] += data.getValues()[1];
            meanAccelero[2] += data.getValues()[2];

            if(data.getValues()[0] > highAccelero[0])
                highAccelero[0] = data.getValues()[0];
            if(data.getValues()[0] < lowAccelero[0])
                lowAccelero[0] = data.getValues()[0];

            if(data.getValues()[0] > highAccelero[0])
                highAccelero[0] = data.getValues()[0];
            if(data.getValues()[0] < lowAccelero[0])
                lowAccelero[0] = data.getValues()[0];
        }
        meanGyro[0] = allGyroData.size() > 0 ? meanGyro[0] / allGyroData.size() : 0f;
        meanGyro[1] = allGyroData.size() > 0 ? meanGyro[1] / allGyroData.size() : 0f;
        meanGyro[2] = allGyroData.size() > 0 ? meanGyro[2] / allGyroData.size() : 0f;

        meanAccelero[0] = allAcceleroData.size() > 0 ? meanAccelero[0] / allAcceleroData.size() : 0f;
        meanAccelero[1] = allAcceleroData.size() > 0 ? meanAccelero[1] / allAcceleroData.size() : 0f;
        meanAccelero[2] = allAcceleroData.size() > 0 ? meanAccelero[2] / allAcceleroData.size() : 0f;
    }

    private void calculateNumZeroCrossing(){
        numAcceleroZeroCrossing = numAcceleroZeroCrossing = 0;
        SensorDataPoint n1=null, nPlus1=null;
        for(SensorDataPoint data : allGyroData){
            n1 = nPlus1;
            nPlus1 = data;
            if(n1 != null && nPlus1 != null){
                if((meanGyro[0] - n1.getValues()[0]) * (meanGyro[0] - nPlus1.getValues()[0]) < 0 ||
                        (meanGyro[1] - n1.getValues()[1]) * (meanGyro[1] - nPlus1.getValues()[1]) < 0 ||
                        (meanGyro[2] - n1.getValues()[2]) * (meanGyro[2] - nPlus1.getValues()[2]) < 0){
                    numGyroZeroCrossing++;
                }
            }
        }

        for(SensorDataPoint data : allAcceleroData){
            n1 = nPlus1;
            nPlus1 = data;
            if(n1 != null && nPlus1 != null){
                if((meanAccelero[0] - n1.getValues()[0]) * (meanAccelero[0] - nPlus1.getValues()[0]) < 0 ||
                        (meanAccelero[1] - n1.getValues()[1]) * (meanAccelero[1] - nPlus1.getValues()[1]) < 0 ||
                        (meanAccelero[2] - n1.getValues()[2]) * (meanAccelero[2] - nPlus1.getValues()[2]) < 0){
                    numAcceleroZeroCrossing++;
                }
            }
        }

    }

    private String getGyroFormatData(){
        return  "{high:{x:" + highGyro[0] + ", y:" + highGyro[1] + ", z:" + highGyro[2] + "}, " +
                "low:{x:" + lowGyro[0] + ", y:" + lowGyro[1] + ", z:" + lowGyro[2] + "}, " +
                "mean:{x:" + meanGyro[0] + ", y:" + meanGyro[1] + ", z:" + meanGyro[2] + "}, " +
                "zcCount :" + numGyroZeroCrossing+"}";
    }

    private String getAcceleroFormatData(){
        return "{high:{x:" + highAccelero[0] + ", y:" + highAccelero[1] + ", z:" + highAccelero[2] + "}, " +
                "low:{x:" + lowAccelero[0] + ", y:" + lowAccelero[1] + ", z:" + lowAccelero[2] + "}, " +
                "mean:{x:" + meanAccelero[0] + ", y:" + meanAccelero[1] + ", z:" + meanAccelero[2] + "}, " +
                "zcCount :" + numAcceleroZeroCrossing+"}";
    }

    public String getUploadData(){
        String ret = "{" +
                "\"apikey\" : \"keELl8zcUZ4d2pI9RMBqsYbCD\"," +
                "\"comb\" : \"devicecmb\"," +
                "\"pod\" : \"readings\"," +
                "\"data\" : \"" + sensorDataStr() + "\"," +
                "\"audience\" : \"Private\"," +
                "\"isActive\" : \"true\"," +
                "\"serviceBranchName\" : \"default\"," +
                "\"podKeyName\" : \"Q4ELCS\"" +

                "}";
        return ret;
    }

    public String sensorDataStr(){
        calculateMean();
        calculateNumZeroCrossing();

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
