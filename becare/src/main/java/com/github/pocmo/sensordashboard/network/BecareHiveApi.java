package com.github.pocmo.sensordashboard.network;

import com.github.pocmo.sensordashboard.model.QueryPostData;
import com.github.pocmo.sensordashboard.model.UserQueryResponse;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by neerajpaliwal on 22/05/16.
 */
public interface BecareHiveApi {

    @POST("/Api/DataApi.svc/ExecuteHiveql")
    public void getUsers(@Body QueryPostData queryData, Callback<UserQueryResponse> response);
}
