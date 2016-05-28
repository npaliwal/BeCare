package com.github.pocmo.sensordashboard.model;

/**
 * Created by neerajpaliwal on 21/05/16.
 */
public class SensorDataWrapper {

    private SensorDataValue high;

    private SensorDataValue low;

    private SensorDataValue mean;

    private int zcCount;

    public SensorDataWrapper(){
        high = new SensorDataValue();
        high.setValueX(-0xffffff);
        high.setValueY(-0xffffff);
        high.setValueZ(-0xffffff);
        low = new SensorDataValue();
        low.setValueX(0xffffff);
        low.setValueY(0xffffff);
        low.setValueZ(0xffffff);
        mean = new SensorDataValue();
        zcCount = 0;
    }

    public SensorDataValue getHigh() {
        return high;
    }

    public void setHigh(SensorDataValue currData) {
        if(currData.getValueX() > high.getValueX())
            this.high.setValueX(currData.getValueX());

        if(currData.getValueY() > high.getValueY())
            this.high.setValueY(currData.getValueY());

        if(currData.getValueZ() > high.getValueZ())
            this.high.setValueZ(currData.getValueZ());
    }

    public SensorDataValue getLow() {
        return low;
    }

    public void setLow(SensorDataValue currData) {
        if (currData.getValueX() < low.getValueX())
            this.low.setValueX(currData.getValueX());

        if(currData.getValueY() < low.getValueY())
            this.low.setValueY(currData.getValueY());

        if(currData.getValueZ() < low.getValueZ())
            this.low.setValueZ(currData.getValueZ());
    }

    public SensorDataValue getMean() {
        return mean;
    }

    public void resetMean(){
        this.mean.setValues(0f, 0f, 0f);
    }

    public void addToMean(SensorDataValue currData) {
        this.mean.setValueX(mean.getValueX() + currData.getValueX());
        this.mean.setValueY(mean.getValueY() + currData.getValueY());
        this.mean.setValueZ(mean.getValueZ() + currData.getValueZ());
    }

    public void setMean(float x, float y, float z){
        this.mean.setValueX(x);
        this.mean.setValueY(y);
        this.mean.setValueZ(z);
    }
    public void normalizeMean(int numPoints){
        this.mean.setValueX(mean.getValueX() / numPoints);
        this.mean.setValueY(mean.getValueY() / numPoints);
        this.mean.setValueZ(mean.getValueZ() / numPoints);
    }

    public int increaseZeroCrossingCount() {
        return zcCount += 1;
    }

    public void setZcCount(int zcCount) {
        this.zcCount = zcCount;
    }

    public int getZcCount(){
        return zcCount;
    }

    public void reset()
    {
        high.setValueX(-0xffffff);
        high.setValueY(-0xffffff);
        high.setValueZ(-0xffffff);
        low.setValueX(0xffffff);
        low.setValueY(0xffffff);
        low.setValueZ(0xffffff);
        zcCount = 0;
    }

}
