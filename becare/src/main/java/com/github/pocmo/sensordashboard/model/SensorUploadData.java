package com.github.pocmo.sensordashboard.model;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.AppConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by neerajpaliwal on 27/05/16.
 */
public class SensorUploadData {
    @SerializedName("sensorname")
    String sensorName;// : accelerometer | gyroscope

    @SerializedName("sensormsg")
    String sensorMsg;// : sensorname.time.coordtype

    @SerializedName("time")
    String time;

    @SerializedName("deviceid")
    String deviceId;

    @SerializedName("corrdtype")
    String corrdtype;// : xvalue | yvalue | zvalue

    @SerializedName("cnt")
    int numSample;//: samples taken every second

    @SerializedName("high")
    float high;

    @SerializedName("low")
    float low;

    @SerializedName("avg")
    float avg;

    @SerializedName("zc")
    int zeroCrossing;

    @SerializedName("vola")
    float volatility; //: volatility (standard deviation)

    @SerializedName("vector")
    float[] vector;


    public SensorUploadData(String sensorName, SensorDataWrapper wrapper, int numSample, int cord, String readTime, String device, float[] all) {

        switch (cord){
            case AppConfig.X_CORD:{
                corrdtype = "x";
                if (numSample > 0) {
                    high = wrapper.getHigh().getRoundX();
                    low = wrapper.getLow().getRoundX();
                    avg = wrapper.getMean().getRoundX();
                }

                break;
            }

            case AppConfig.Y_CORD:{
                corrdtype = "y";
                if (numSample > 0) {
                    high = wrapper.getHigh().getRoundY();
                    low = wrapper.getLow().getRoundY();
                    avg = wrapper.getMean().getRoundY();
                }

                break;
            }

            case AppConfig.Z_CORD:{
                corrdtype = "z";
                if (numSample > 0) {
                    high = wrapper.getHigh().getRoundZ();
                    low = wrapper.getLow().getRoundZ();
                    avg = wrapper.getMean().getRoundZ();
                }
                break;
            }
        }


        this.zeroCrossing = wrapper.getZcCount();
        this.volatility = 0f;

        this.sensorName = sensorName;
        this.sensorMsg = sensorName + "." + readTime + "." + corrdtype;
        this.time = readTime;
        this.deviceId = device;
        this.numSample = numSample;
        this.vector = all;

    }
}
