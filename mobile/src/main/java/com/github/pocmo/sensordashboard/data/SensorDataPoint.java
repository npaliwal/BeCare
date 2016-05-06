package com.github.pocmo.sensordashboard.data;

public class SensorDataPoint implements  Comparable{
    private long timestamp;
    private float[] values;
    private int accuracy;

    public SensorDataPoint(long timestamp, int accuracy, float[] values) {
        this.timestamp = timestamp;
        this.accuracy = accuracy;
        this.values = values;
    }

    public float[] getValues() {
        return values;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public String getValueString(){
        return values[0] + ", " + values[1] + ", " + values[2];
    }

    @Override
    public int compareTo(Object another) {
        SensorDataPoint p1 = this;
        SensorDataPoint p2 = (SensorDataPoint)another;
        if(p2 == null || p2.values == null){
            return -1;
        }
        // if last names are the same compare first names
        float p1Float =     p1.values[0]*p1.values[0] +
                p1.values[1]*p1.values[1] +
                p1.values[2]*p1.values[2];

        float p2Float =     p2.values[0]*p2.values[0] +
                p2.values[1]*p2.values[1] +
                p2.values[2]*p2.values[2];
        if(p1Float > p2Float){
            return -1;
        }else if(p1Float < p2Float){
            return 1;
        }else{
            return 0;
        }
    }
}
