package com.github.pocmo.sensordashboard.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by neerajpaliwal on 19/06/16.
 */
public class TwoImageInfo implements  Parcelable{
    ContrastImageInfo leftImage = null;
    ContrastImageInfo rightImage = null;


    public TwoImageInfo(){
        leftImage = rightImage = null;
    }
    public ContrastImageInfo getRightImage() {
        return rightImage;
    }

    public void setRightImage(ContrastImageInfo rightImage) {
        this.rightImage = rightImage;
    }

    public ContrastImageInfo getLeftImage() {
        return leftImage;
    }

    public void setLeftImage(ContrastImageInfo leftImage) {
        this.leftImage = leftImage;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(leftImage, flags);
        dest.writeParcelable(rightImage, flags);

    }

    private TwoImageInfo(Parcel in){
        this.leftImage = in.readParcelable(ContrastImageInfo.class.getClassLoader());
        this.rightImage = in.readParcelable(ContrastImageInfo.class.getClassLoader());

    }
    public static final Parcelable.Creator<TwoImageInfo> CREATOR = new Parcelable.Creator<TwoImageInfo>() {

        @Override
        public TwoImageInfo createFromParcel(Parcel source) {
            return new TwoImageInfo(source);
        }

        @Override
        public TwoImageInfo[] newArray(int size) {
            return new TwoImageInfo[size];
        }
    };
}