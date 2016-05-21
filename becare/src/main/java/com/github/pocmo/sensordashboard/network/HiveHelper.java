package com.github.pocmo.sensordashboard.network;

import android.util.Log;

import com.github.pocmo.sensordashboard.AppConstant;
import com.github.pocmo.sensordashboard.model.UploadData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by neerajpaliwal on 18/05/16.
 */
public class HiveHelper {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    public static String INSERT_URL = "http://hivedemo.qtxsystems.net/Api/DataApi.svc/Insert";
    public static String QUERY_URL = "http://hivedemo.qtxsystems.net/Api/DataApi.svc/ExecuteHiveql";

    public String formatUploadData(UploadData uploadData){
        String ret = "{" +
                "\"apikey\" : \""+ AppConstant.HIVE_API_KEY + "\"," +
                "\"comb\" : \""+ AppConstant.COMB_DEVICE + "\"," +
                "\"pod\" : \"" + AppConstant.POD_READING + "\"," +
                "\"data\" : \"" + formatSensorStats(uploadData) + "\"," +
                "\"audience\" : \"Private\"," +
                "\"isActive\" : \"true\"," +
                "\"serviceBranchName\" : \"default\"," +
                "\"podKeyName\" : \"" + AppConstant.POD_KEY_READING + "\"" +
                "}";
        return ret;
    }

    public String formatSensorStats(UploadData uploadData){
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTimeInMillis());
        String time = timeFormat.format(cal.getTimeInMillis());

        String gyro = uploadData.getGyroMeter().toString();
        String acelro = uploadData.getAccelMeter().toString();

        String ret = "{" +
                "\\\"deviceId\\\":\\\"" + uploadData.getDeviceId() + "\\\"," +
                "\\\"activityType\\\":\\\"" + uploadData.getActivityData().toString() + "\\\"," +
                "\\\"readDate\\\":\\\"" + date + "\\\"," +
                "\\\"readTime\\\":\\\"" + time + "\\\"," +
                "\\\"gyroMeter\\\":\\\"" + gyro + "\\\"," +
                "\\\"accelMeter\\\":\\\"" +  acelro + "\\\"}";

        Log.d("formatSensorStats", ret);
        return ret;
    }

    public String userQeryString(String userId){
        String ret = "{\"apikey\":\"" + AppConstant.HIVE_API_KEY + "\"," +
                "\"query\":\"RELEASE * INPOD " + AppConstant.COMB_ADMIN + "." + AppConstant.POD_USER +
                " INPODX default ATTACH userid = \'" + userId + "\' \", \"GetMetadata\":false}";
        return ret;
    }
}
