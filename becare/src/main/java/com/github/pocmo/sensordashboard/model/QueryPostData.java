package com.github.pocmo.sensordashboard.model;

import com.github.pocmo.sensordashboard.AppConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by neerajpaliwal on 22/05/16.
 */
public class QueryPostData {
    @SerializedName("apikey")
    String apiKey;

    @SerializedName("GetMetadata")
    boolean getMetaData;

    @SerializedName("query")
    String query;

    public QueryPostData(String userId, String pswd){
        apiKey = AppConstant.HIVE_API_KEY;
        getMetaData = false;
        query = "RELEASE * INPOD admincmb.users INPODX default ATTACH userid = \'becare_user_id\' AND pwd = \'becare_user_pswd\' ";
        query = query.replace("becare_user_id", userId);
        query = query.replace("becare_user_pswd", pswd);
    }

    public QueryPostData(String userId){
        apiKey = AppConstant.HIVE_API_KEY;
        getMetaData = false;
        query = "RELEASE * INPOD admincmb.users INPODX default ATTACH userid = \'becare_user_id\' ";
        query = query.replace("becare_user_id", userId);
    }
}
