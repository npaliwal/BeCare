package me.smartwatches.becare.data;

/**
 * Created by neerajpaliwal on 29/04/16.
 */
public class SensorData implements  Comparable{
    private float valueX;
    private float valueY;
    private float valueZ;

    public SensorData(){
        valueX = 0f;
        valueY = 0f;
        valueZ = 0f;
    }

    public SensorData(SensorData data){
        valueX = data.valueX;
        valueY = data.valueY;
        valueZ = data.valueZ;
    }

    public float getValueX() {
        return valueX;
    }

    public void setValueX(float valueX) {
        this.valueX = valueX;
    }

    public float getValueY() {
        return valueY;
    }

    public void setValueY(float valueY) {
        this.valueY = valueY;
    }

    public float getValueZ() {
        return valueZ;
    }

    public void setValueZ(float valueZ) {
        this.valueZ = valueZ;
    }


    public void setValues(float x, float y, float z){
        valueX = x;
        valueY = y;
        valueZ = z;
    }

    public void addData(SensorData data){
        valueX += data.valueX;
        valueY += data.valueY;
        valueZ += data.valueZ;
    }

    public void setHigh(SensorData data){
        if(valueX < data.valueX)
            valueX = data.valueX;

        if(valueY < data.valueY)
            valueY = data.valueY;

        if(valueZ < data.valueZ)
            valueZ = data.valueZ;
    }

    public void setLow(SensorData data){
        if(valueX > data.valueX)
            valueX = data.valueX;

        if(valueY > data.valueY)
            valueY = data.valueY;

        if(valueZ > data.valueZ)
            valueZ = data.valueZ;
    }

    public void divideBy(int num) {
        valueX = valueX/num;
        valueY = valueY/num;
        valueZ = valueZ/num;
    }


    public String toString(){
        return "{ x:" + valueX + ", y:" + valueY + ", z:" + valueZ + "}";
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }
}
