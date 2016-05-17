package com.github.pocmo.sensordashboard;

import android.content.Context;
import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.ArrayList;

/**
 * Created by neerajpaliwal on 04/05/16.
 */
public class SensorAdapter extends ArrayAdapter<Sensor> {

    public SensorAdapter(Context context, ArrayList<Sensor> allSensors) {
        super(context, 0, allSensors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Sensor sensor = getItem(position);
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_sensor_name, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.sensor_name);
            viewHolder.vendor = (TextView) convertView.findViewById(R.id.sensor_vendor);
            viewHolder.version = (TextView) convertView.findViewById(R.id.sensor_version);
            viewHolder.streamRate = (TextView) convertView.findViewById(R.id.sensor_stream);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(sensor.getName());
        viewHolder.version.setText("version:"+sensor.getVersion());
        viewHolder.vendor.setText("by "+sensor.getVendor());
        viewHolder.streamRate.setText("Stream Rate: "+sensor.getMinDelay());
        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView name, version, streamRate, vendor;
    }
}
