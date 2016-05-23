package com.github.pocmo.sensordashboard.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by neerajpaliwal on 22/05/16.
 */
public class UserQueryResponse {


    @SerializedName("TotalRowCount")
    int userCount;

    @SerializedName("RowList")
    List<UserData> allUsers;



    public static class UserData{
        @SerializedName("userid")
        String userId;

        @SerializedName("pwd")
        String userPswd;

        public String getUserBecareId() {
            return userBecareId;
        }

        public String getUserPswd() {
            return userPswd;
        }

        public String getUserId() {
            return userId;
        }

        @SerializedName("_ID")
        String userBecareId;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public List<UserData> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<UserData> allUsers) {
        this.allUsers = allUsers;
    }

}
