package com.github.pocmo.sensordashboard.model;

import com.github.pocmo.sensordashboard.AppConstant;
import com.github.pocmo.sensordashboard.network.HiveHelper;
import com.google.gson.annotations.SerializedName;

/**
 * Created by neerajpaliwal on 22/05/16.
 */
public class NewUserPostData {
    @SerializedName("apikey")
    String apiKey;

    @SerializedName("comb")
    String combValue;

    @SerializedName("pod")
    String podValue;

    @SerializedName("data")
    String userData;

    @SerializedName("audience")
    String audience;

    @SerializedName("isActive")
    Boolean isActive;

    @SerializedName("serviceBranchName")
    String serviceBranchName;

    @SerializedName("podKeyName")
    String podKeyValue;

    public NewUserPostData(String userId, String pswd){
        apiKey    = AppConstant.HIVE_API_KEY;
        combValue = AppConstant.COMB_ADMIN;
        podValue  = AppConstant.POD_USER;
        podKeyValue = AppConstant.POD_USER_KEY;
        audience  = "Private";
        isActive  = true;
        serviceBranchName = "default";

        userData = "{\"userid\":\"becare_user_id\",\"pwd\":\"becare_user_pswd\"}";
        userData = userData.replace("becare_user_id", userId);
        userData = userData.replace("becare_user_pswd", pswd);
    }

    public void setupForProfile(String userId, String profileName, String userType){
        podValue  = AppConstant.POD_PROFILE;
        podKeyValue = AppConstant.POD_PROFILE_KEY;

        userData = "{\"userid\":\"becare_user_id\",\"profileName\":\"becare_profile_name\",\"userType\":\"becare_userType\"}";
        userData = userData.replace("becare_user_id", userId);
        userData = userData.replace("becare_user_pswd", profileName);
        userData = userData.replace("becare_userType", userType);
    }
}
