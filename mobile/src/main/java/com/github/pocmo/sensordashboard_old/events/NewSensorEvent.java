package com.github.pocmo.sensordashboard_old.events;

import com.github.pocmo.sensordashboard_old.data.Sensor;

public class NewSensorEvent {
    private Sensor sensor;

    public NewSensorEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
