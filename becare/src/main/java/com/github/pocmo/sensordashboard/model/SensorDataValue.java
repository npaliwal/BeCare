package com.github.pocmo.sensordashboard.model;


/**
 * Created by neerajpaliwal on 21/05/16.
 */
public class SensorDataValue {
    private float x;

    private float y;

    private float z;


    public SensorDataValue(){
        x = 0f;
        y = 0f;
        z = 0f;
    }

    public SensorDataValue(SensorDataValue data){
        x = data.x;
        y = data.y;
        z = data.z;
    }
    public SensorDataValue(float values[]){
        x = values[0];
        y = values[1];
        z = values[2];
    }

    public float getValueX() {
        return x;
    }

    public void setValueX(float valueX) {
        this.x = valueX;
    }

    public float getValueY() {
        return y;
    }

    public void setValueY(float valueY) {
        this.y = valueY;
    }

    public float getValueZ() {
        return z;
    }

    public void setValueZ(float valueZ) {
        this.z = valueZ;
    }


    public void setValues(float x, float y, float z){
        x = x;
        y = y;
        z = z;
    }
}
