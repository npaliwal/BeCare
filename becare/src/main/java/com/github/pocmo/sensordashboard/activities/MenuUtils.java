package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.pocmo.sensordashboard.R;

/**
 * Created by qtxdev on 7/13/2016.
 */
public class MenuUtils{

    public static void getArmElevation(Context context) {
        Intent intent = new Intent(context, ArmElevationActivity.class);
        context.startActivity(intent);

    }

    public static void getSnooker(Context context) {
        Intent intent = new Intent(context, SnookerActivity.class);
        context.startActivity(intent);

    }

    public static void getTranscription(Context context) {
        Intent intent = new Intent(context, TranscriptionTestActivity.class);
        context.startActivity(intent);

    }

    public static void getContrast(Context context) {
        Intent intent = new Intent(context, ContrastSensitivityActivity.class);
        context.startActivity(intent);

    }

    public static void getTimedWalk(Context context) {
        Intent intent = new Intent(context, TimedWalkedActivity.class);
        context.startActivity(intent);
    }

    public static void getTools(Context context) {
        Intent intent = new Intent(context, UtilityActivity.class);
        context.startActivity(intent);
    }
}
