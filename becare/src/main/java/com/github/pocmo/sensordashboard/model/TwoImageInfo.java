package com.github.pocmo.sensordashboard.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by neerajpaliwal on 19/06/16.
 */
public class TwoImageInfo implements Parcelable {
    int contrast         = 1;
    int imageResId       = -1;
    String lefColor      = null;
    String rightColor    = null;

    public TwoImageInfo(String left, String right) {
        lefColor = left;
        rightColor = right;
    }

    public TwoImageInfo(int imageResId, int contrast) {
        this.contrast = contrast;
        this.imageResId = imageResId;
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

    public String getLefColor() {
        if(lefColor != null)
            return lefColor;
        return "image";
    }

    public void setLefColor(String lefColor) {
        this.lefColor = lefColor;
    }

    public String getRightColor() {
        if(rightColor != null)
            return rightColor;
        return "image";
    }

    public void setRightColor(String rightColor) {
        this.rightColor = rightColor;
    }

    public boolean isSimilar(){
        if(contrast != 1){
            return false;
        }
        if(lefColor != null && rightColor != null && !lefColor.equals(rightColor)){
            return false;
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(contrast);
        dest.writeInt(imageResId);
        dest.writeString(lefColor);
        dest.writeString(rightColor);
    }

    private TwoImageInfo(Parcel in){
        this.contrast = in.readInt();
        this.imageResId = in.readInt();
        this.lefColor = in.readString();
        this.rightColor = in.readString();
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