package com.github.pocmo.sensordashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.github.pocmo.sensordashboard.activities.StartingActivity;

/**
 * Created by neerajpaliwal on 17/05/16.
 */
public class PreferenceStorage {
    private static final String SOCKET_IP = "custom_socket_ip";
    private static final String SOCKET_PORT = "custom_socket_port";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public PreferenceStorage(){
        sharedPreferences = StartingActivity.getInstance().getSharedPreferences(AppConstant.BECARE_FILE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setSocketInfo(String ip, int port){
        editor.putString(PreferenceStorage.SOCKET_IP, ip).commit();
        editor.putInt(PreferenceStorage.SOCKET_PORT, port).commit();
    }

    public String getSocketIp(){
        return sharedPreferences.getString(PreferenceStorage.SOCKET_IP, null);
    }

    public int getSocketPort(){
        return sharedPreferences.getInt(PreferenceStorage.SOCKET_PORT, -1);
    }
}
