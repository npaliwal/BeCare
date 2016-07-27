package com.github.pocmo.sensordashboard.model;

import com.github.pocmo.sensordashboard.activities.ArmElevationActivity;

/**
 * Created by admin on 27/07/16.
 */
public class WellnessTask {
    private int nameResId;
    private int iconResId;
    private int descriptionResId;
    private Class<?> clazzName;

    public WellnessTask(int nId, int dId, int iconId, Class<?> clazz) {
        this.nameResId = nId;
        this.descriptionResId = dId;
        this.iconResId = iconId;
        this.clazzName = clazz;
    }

    public int getNameResId() {
        return nameResId;
    }

    public void setNameResId(int nameResId) {
        this.nameResId = nameResId;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getDescription() {
        return descriptionResId;
    }

    public void setDescription(int description) {
        this.descriptionResId = description;
    }

    public Class<?> getClazzName() {
        return clazzName;
    }

    public void setClazzName(Class<?> clazzName) {
        this.clazzName = clazzName;
    }


}
