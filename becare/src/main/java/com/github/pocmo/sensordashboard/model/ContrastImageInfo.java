package com.github.pocmo.sensordashboard.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.AppConstant;

/**
 * Created by neerajpaliwal on 19/06/16.
 */
public class ContrastImageInfo implements Parcelable {
    public String getId() {
        return id;
    }

    String id      = "";
    int contrast   = 1;
    int imageResId = -1;
    int color      = -1;

    public ContrastImageInfo(String _id, int resId, AppConfig.ContrastTestType type) {
        this.id = _id;
        if(type == AppConfig.ContrastTestType.SHADES){
            this.color = resId;
        }else {
            this.imageResId = resId;
        }
    }


    public float getContrast() {
        return contrast % 10;
    }

    public void setContrast(int contrast) {
        this.contrast = contrast;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public int getColor() {
        return color;
    }

    public void setLefColor(int clr) {
        this.color = clr;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(contrast);
        dest.writeInt(imageResId);
        dest.writeInt(color);
    }

    private ContrastImageInfo(Parcel in){
        this.contrast = in.readInt();
        this.imageResId = in.readInt();
        this.color = in.readInt();
    }
    public static final Creator<ContrastImageInfo> CREATOR = new Creator<ContrastImageInfo>() {

        @Override
        public ContrastImageInfo createFromParcel(Parcel source) {
            return new ContrastImageInfo(source);
        }

        @Override
        public ContrastImageInfo[] newArray(int size) {
            return new ContrastImageInfo[size];
        }
    };
}