package com.github.pocmo.sensordashboard.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by neerajpaliwal on 09/09/16.
 */
public class DateUtils {
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    public static String formatDateTime(long timeMillis){
        return dateTimeFormat.format(timeMillis);
    }

    public static String formatTime(long timeMillis){
        return timeFormat.format(timeMillis);
    }

    public static String formatDate(long timeMillis){
        return dateFormat.format(timeMillis);
    }

}
