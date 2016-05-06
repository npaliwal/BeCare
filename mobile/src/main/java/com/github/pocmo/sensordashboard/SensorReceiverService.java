package com.github.pocmo.sensordashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.github.pocmo.sensordashboard.data.SensorDataPoint;
import com.github.pocmo.sensordashboard.database.DataEntry;
import com.github.pocmo.sensordashboard.network.DataUploadService;
import com.github.pocmo.sensordashboard.shared.DataMapKeys;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Arrays;

import io.realm.Realm;

public class SensorReceiverService extends WearableListenerService {
    private static final String TAG = "SensorReceiverService";
    public static final String PREFS = "serverUpdatedPrefs";
    public static final String PREFS_LAST_UPDATED_TIME = "last_updated";
    public static final String POST_BODY = "post_body";

    private long lastUpdateTime = 0;



    private RemoteSensorManager sensorManager;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = RemoteSensorManager.getInstance(this);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        Log.i(TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        Log.i(TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged()");

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.startsWith("/sensors/")) {
                    unpackSensorData(
                        Integer.parseInt(uri.getLastPathSegment()),
                        DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }
            }
        }
    }

    private void unpackSensorData(int sensorType, DataMap dataMap) {
        int accuracy = dataMap.getInt(DataMapKeys.ACCURACY);
        long timestamp = dataMap.getLong(DataMapKeys.TIMESTAMP);
        float[] values = dataMap.getFloatArray(DataMapKeys.VALUES);

        Log.d(TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));

        sensorManager.addSensorData(sensorType, accuracy, timestamp, values);

        if(System.currentTimeMillis() - lastUpdateTime > 60000){
            lastUpdateTime = System.currentTimeMillis();
            Log.d(TAG, "starting upload service");
            Intent intent = new Intent(this, DataUploadService.class);
            intent.putExtra(POST_BODY, sensorManager.getUploadData().getUploadData());
            startService(intent);
        }
    }
}
