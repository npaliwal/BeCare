package com.github.pocmo.sensordashboard_old.events;

import com.github.pocmo.sensordashboard_old.data.Sensor;

public class SensorRangeEvent {
    private Sensor sensor;

    public SensorRangeEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
