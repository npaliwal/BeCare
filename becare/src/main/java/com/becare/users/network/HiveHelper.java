package com.becare.users.network;

import android.util.Log;

import com.becare.users.AppConstant;
import com.becare.users.model.UploadData_Old;


/**
 * Created by neerajpaliwal on 18/05/16.
 */
public class HiveHelper {


    public static String INSERT_URL = "http://hivedemo.qtxsystems.net/Api/DataApi.svc/Insert";
    public static String QUERY_URL = "http://hivedemo.qtxsystems.net/Api/DataApi.svc/ExecuteHiveql";
    public static String HIVE_ROOT = "http://hivedemo.qtxsystems.net";

    public String formatUploadData(UploadData_Old uploadData){
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

    public String formatSensorStats(UploadData_Old uploadData){
          String gyro = uploadData.getGyroMeter().toString();
        String acelro = uploadData.getAccelMeter().toString();

        String ret = "{" +
                "\\\"deviceId\\\":\\\"" + uploadData.getDeviceId() + "\\\"," +
                "\\\"activityType\\\":\\\"" + uploadData.getActivityData().toString() + "\\\"," +
                "\\\"readDate\\\":\\\"" + uploadData.getReadDate() + "\\\"," +
                "\\\"readTime\\\":\\\"" + uploadData.getReadTime() + "\\\"," +
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
