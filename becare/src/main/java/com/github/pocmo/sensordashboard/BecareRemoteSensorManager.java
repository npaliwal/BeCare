package com.github.pocmo.sensordashboard;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.SparseArray;

import com.github.pocmo.sensordashboard.data.Sensor;
import com.github.pocmo.sensordashboard.data.SensorDataPoint;
import com.github.pocmo.sensordashboard.data.SensorNames;
import com.github.pocmo.sensordashboard.data.TagData;
import com.github.pocmo.sensordashboard.data.UploadDataHelper;
import com.github.pocmo.sensordashboard.events.BusProvider;
import com.github.pocmo.sensordashboard.events.NewSensorEvent;
import com.github.pocmo.sensordashboard.events.SensorUpdatedEvent;
import com.github.pocmo.sensordashboard.events.TagAddedEvent;
import com.github.pocmo.sensordashboard.model.ActivityUploadData;
import com.github.pocmo.sensordashboard.model.SensorDataValue;
import com.github.pocmo.sensordashboard.network.ClientSocketManager;
import com.github.pocmo.sensordashboard.shared.ClientPaths;
import com.github.pocmo.sensordashboard.shared.DataMapKeys;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * This class is equivalent to RemoteSensorMAnager of mobile module
 */


public class BecareRemoteSensorManager {
    private static final String TAG = "RemoteSensorMgr";

    private static BecareRemoteSensorManager instance;

    private ExecutorService executorService;
    private SparseArray<Sensor> sensorMapping;
    private ArrayList<Sensor> sensors;
    private SensorNames sensorNames;
    private UploadDataHelper uploadDataHelper;
    private UploadDataHelper uploadMobileDataHelper;
    private GoogleApiClient googleApiClient;
    private PreferenceStorage preferenceStorage;
    private ClientSocketManager socketManager;
    private int SnookerSeq = -1;

    private LinkedList<TagData> tags = new LinkedList<>();

    public static synchronized BecareRemoteSensorManager getInstance(Context context) {
        if (instance == null) {
            instance = new BecareRemoteSensorManager(context.getApplicationContext());
        }

        return instance;
    }

    private BecareRemoteSensorManager(Context context) {
        this.sensorMapping = new SparseArray<Sensor>();
        this.sensors = new ArrayList<Sensor>();
        this.sensorNames = new SensorNames();
        this.uploadDataHelper       = new UploadDataHelper();
        this.uploadMobileDataHelper = new UploadDataHelper();
        this.preferenceStorage = new PreferenceStorage(context.getApplicationContext());
        socketManager = new ClientSocketManager(preferenceStorage);

        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();

        this.executorService = Executors.newCachedThreadPool();
    }

    public ClientSocketManager getSocketManager(){
        return socketManager;
    }

    public UploadDataHelper getUploadDataHelper(){
        return uploadDataHelper;
    }

    public UploadDataHelper getUploadMobileDataHelper(){
        return uploadMobileDataHelper;
    }

    public List<Sensor> getSensors() {
        return (List<Sensor>) sensors.clone();
    }

    public Sensor getSensor(long id) {
        return sensorMapping.get((int) id);
    }

    private Sensor createSensor(int id) {
        Sensor sensor = new Sensor(id, sensorNames.getName(id));

        sensors.add(sensor);
        sensorMapping.append(id, sensor);

        BusProvider.postOnMainThread(new NewSensorEvent(sensor));

        return sensor;
    }

    private Sensor getOrCreateSensor(int id) {
        Sensor sensor = sensorMapping.get(id);

        if (sensor == null) {
            sensor = createSensor(id);
        }

        return sensor;
    }

    public synchronized void addMobileSensorData(int sensorType, int accuracy, long timestamp, float[] values) {
        Sensor sensor = getOrCreateSensor(sensorType);

        // TODO: We probably want to pull sensor data point objects from a pool here
        SensorDataPoint dataPoint = new SensorDataPoint(timestamp, accuracy, values);
        uploadMobileDataHelper.addDataValue(new SensorDataValue(values), sensorType);

        sensor.addDataPoint(dataPoint);
        BusProvider.postOnMainThread(new SensorUpdatedEvent(sensor, dataPoint));
    }

    public synchronized void addWearSensorData(int sensorType, int accuracy, long timestamp, float[] values) {
        Sensor sensor = getOrCreateSensor(sensorType);

        // TODO: We probably want to pull sensor data point objects from a pool here
        SensorDataPoint dataPoint = new SensorDataPoint(timestamp, accuracy, values);
        uploadDataHelper.addDataValue(new SensorDataValue(values), sensorType);

        sensor.addDataPoint(dataPoint);
        BusProvider.postOnMainThread(new SensorUpdatedEvent(sensor, dataPoint));
    }

    public LinkedList<TagData> getTags() {
        return (LinkedList<TagData>) tags.clone();
    }

    private boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = googleApiClient.blockingConnect(AppConfig.CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

    public void filterBySensorId(final int sensorId) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                filterBySensorIdInBackground(sensorId);
            }
        });
    }



    private void filterBySensorIdInBackground(final int sensorId) {
        Log.d(TAG, "filterBySensorId(" + sensorId + ")");

        if (validateConnection()) {
            PutDataMapRequest dataMap = PutDataMapRequest.create("/filter");

            dataMap.getDataMap().putInt(DataMapKeys.FILTER, sensorId);
            dataMap.getDataMap().putLong(DataMapKeys.TIMESTAMP, System.currentTimeMillis());

            PutDataRequest putDataRequest = dataMap.asPutDataRequest();
            Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    Log.d(TAG, "Filter by sensor " + sensorId + ": " + dataItemResult.getStatus().isSuccess());
                }
            });
        }
    }

    public void startMeasurement() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                controlMeasurementInBackground(ClientPaths.START_MEASUREMENT);
            }
        });
    }

    public void stopMeasurement() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                controlMeasurementInBackground(ClientPaths.STOP_MEASUREMENT);
            }
        });
    }

    public void getNodes(ResultCallback<NodeApi.GetConnectedNodesResult> pCallback) {
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(pCallback);
    }

    private void controlMeasurementInBackground(final String path) {
        if (validateConnection()) {
            List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();

            Log.d(TAG, "Sending to nodes: " + nodes.size());

            for (Node node : nodes) {
                Log.i(TAG, "add node " + node.getDisplayName());
           //     uploadDataHelper.setDeviceId(node.getDisplayName());
                Wearable.MessageApi.sendMessage(
                        googleApiClient, node.getId(), path, null
                ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        Log.d(TAG, "controlMeasurementInBackground(" + path + "): " + sendMessageResult.getStatus().isSuccess());
                    }
                });
            }
        } else {
            Log.w(TAG, "No connection possible");
        }
    }


    public void uploadAllWearSensorData(int seq) {
        try {
            Log.d(TAG, "upload data tring upload");
            if(uploadDataHelper.getUserActivityName() != null) {
                uploadDataHelper.setSeqNumber(seq);
                Log.d(TAG, "upload data tring activity data not null");
                String dataX = uploadDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_ACCELEROMETER, AppConfig.X_CORD);
                String dataY = uploadDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_ACCELEROMETER, AppConfig.Y_CORD);
                String dataZ = uploadDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_ACCELEROMETER, AppConfig.Z_CORD);
                String data = "";

                if (dataX != "")
                    data = dataX + "\n" +dataY +  "\n" + dataZ;

                dataX = uploadDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_GYROSCOPE, AppConfig.X_CORD);
                dataY = uploadDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_GYROSCOPE, AppConfig.Y_CORD);
                dataZ = uploadDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_GYROSCOPE, AppConfig.Z_CORD);

                if (dataX != "")
                    data = data + "\n" + dataX + "\n" +dataY +  "\n" + dataZ;

                socketManager.pushDataAsyncronously(data);
            }
        }catch (Exception e){
            Log.d(TAG, "upload data failed : " + e.getMessage());
        }
    }

    public void uploadAllMobileSensorData(int seq) {
        try {
            Log.d(TAG, "upload data tring upload");
            if(uploadMobileDataHelper.getUserActivityName() != null) {
                uploadMobileDataHelper.setSeqNumber(seq);
                Log.d(TAG, "upload data tring activity data not null");
                String dataX = uploadMobileDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_ACCELEROMETER, AppConfig.X_CORD);
                String dataY = uploadMobileDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_ACCELEROMETER, AppConfig.Y_CORD);
                String dataZ = uploadMobileDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_ACCELEROMETER, AppConfig.Z_CORD);
                String data = "";

                if (dataX != "")
                    data = dataX + "\n" +dataY +  "\n" + dataZ;

                dataX = uploadMobileDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_GYROSCOPE, AppConfig.X_CORD);
                dataY = uploadMobileDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_GYROSCOPE, AppConfig.Y_CORD);
                dataZ = uploadMobileDataHelper.getSensorUploadData(android.hardware.Sensor.TYPE_GYROSCOPE, AppConfig.Z_CORD);

                if (dataX != "")
                    data = data + "\n" + dataX + "\n" +dataY +  "\n" + dataZ;

                socketManager.pushDataAsyncronously(data);
            }
        }catch (Exception e){
            Log.d(TAG, "upload data failed : " + e.getMessage());
        }
    }

    public void uploadActivityData(int seq, long dur) {
        try {
            Log.d(TAG, "upload data string upload");
            if(uploadDataHelper.getUserActivityName() != null && uploadDataHelper.getUserActivityData(seq, dur) != null) {
                Log.d(TAG, "upload data string activity data not null");
                uploadDataHelper.setSeqNumber(seq);
                socketManager.pushDataAsyncronously(uploadDataHelper.getUserActivityData(seq, dur));

            }
        }catch (Exception e){
            Log.d(TAG, "upload data failed : " + e.getMessage());
        }
    }

    public void setSnookerSeq(int seq){
        SnookerSeq = seq;
    }

    public int getSnookerSeq()
    {
        return SnookerSeq;
    }

    public void uploadActivityDataInstantly(String activityValue){
        try {
            Log.d(TAG, "upload data string upload " +activityValue);
            socketManager.pushDataAsyncronously(uploadDataHelper.getUserActivityData(activityValue));

        }catch (Exception e){
            Log.d(TAG, "upload data failed : " + e.getMessage());
        }
    }

    public void calculateMobileStats(long currTime){
        uploadMobileDataHelper.calculateStats(currTime);
        uploadDataHelper.calculateStats(currTime);
    }

    public void resetMobileStats(){
        uploadMobileDataHelper.resetStats();
    }

    public void calculateWearStats(long currTime){
        uploadDataHelper.calculateStats(currTime);
    }

    public void resetWearStats(){
        uploadDataHelper.resetStats();
    }

    public GoogleApiClient getGoogleApiClient()
    {
        return googleApiClient;
    }
}

