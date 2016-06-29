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
    private static final String USER_ID = "becare_user_id";
    private static final String SKIP_REG = "becare_skip_registration";
    private static final String NUM_CONTRAST_EXERCISE = "num_contrast_exercise";
    private static final String NUM_TRASNCRIPT_EXERCISE = "num_transcript_exercise";
    private static final String ARM_TASK_DURATION = "arm_task_duration";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public PreferenceStorage(Context appContext){
        sharedPreferences = appContext.getSharedPreferences(AppConstant.BECARE_FILE_NAME, Context.MODE_PRIVATE);
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

    public String getUserId() {
        return sharedPreferences.getString(PreferenceStorage.USER_ID, null);
    }

    public void setUserId(String userId) {
        editor.putString(PreferenceStorage.USER_ID, userId).commit();
    }

    public boolean getSkipReg() {
        return sharedPreferences.getBoolean(PreferenceStorage.SKIP_REG, false);
    }

    public void setSkipReg(boolean skipReg) {
        editor.putBoolean(PreferenceStorage.SKIP_REG, skipReg).commit();
    }

    public boolean isLoggedIn(){
        if(getUserId() != null){
            return true;
        }
        return getSkipReg();
    }

    public int getNumContrastExercise() {
        return sharedPreferences.getInt(PreferenceStorage.NUM_CONTRAST_EXERCISE, AppConfig.DEFAULT_NUM_CONTRAST);
    }

    public void setNumContrastExercise(int numExercise) {
        editor.putInt(PreferenceStorage.NUM_CONTRAST_EXERCISE, numExercise).commit();
    }

    public int getNumTranscriptExercise() {
        return sharedPreferences.getInt(PreferenceStorage.NUM_TRASNCRIPT_EXERCISE, AppConfig.DEFAULT_NUM_TRANSCRIPT);
    }

    public void setNumTranscriptExercise(int numExercise) {
        editor.putInt(PreferenceStorage.NUM_TRASNCRIPT_EXERCISE, numExercise).commit();
    }

    public int getArmElevationTaskDuration() {
        return sharedPreferences.getInt(PreferenceStorage.ARM_TASK_DURATION, -1);
    }

    public void setArmElevationTaskDuration(int duration) {
        editor.putInt(PreferenceStorage.ARM_TASK_DURATION, duration).commit();
    }
}
