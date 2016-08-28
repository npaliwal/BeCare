package com.becare.users.activities;

import android.content.Context;
import android.content.Intent;

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

    public static void getUpAndGo(Context context) {
        Intent intent = new Intent(context, UpAndGoActivity.class);
        context.startActivity(intent);
    }

    public static void getStroop(Context context) {
        Intent intent = new Intent(context, StroopActivity.class);
        context.startActivity(intent);
    }

    public static void getTwentyFiveStepsActivity(Context context) {
        Intent intent = new Intent(context, TwentyFiveStepsActivity.class);
        context.startActivity(intent);
    }

    public static void getSixMinutesActivity(Context context) {
        Intent intent = new Intent(context, SixMinutesActivity.class);
        context.startActivity(intent);
    }

    public static void getUpAndGoActivity(Context context) {
        Intent intent = new Intent(context, UpAndGoActivity.class);
        context.startActivity(intent);
    }

    public static void getTools(Context context) {
        Intent intent = new Intent(context, UtilityActivity.class);
        context.startActivity(intent);
    }
}
