package com.github.pocmo.sensordashboard;

import android.content.Context;
import android.hardware.Sensor;

import com.github.pocmo.sensordashboard.activities.ArmElevationActivity;
import com.github.pocmo.sensordashboard.activities.ContrastSensitivityActivity;
import com.github.pocmo.sensordashboard.activities.SnookerActivity;
import com.github.pocmo.sensordashboard.activities.StroopActivity;
import com.github.pocmo.sensordashboard.activities.TimedWalkedActivity;
import com.github.pocmo.sensordashboard.activities.TranscriptionTestActivity;
import com.github.pocmo.sensordashboard.activities.UpAndGoActivity;
import com.github.pocmo.sensordashboard.model.AudioData;
import com.github.pocmo.sensordashboard.model.ContrastImageInfo;
import com.github.pocmo.sensordashboard.model.WellnessTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by neerajpaliwal on 08/05/16.
 */
public class AppConfig {
    public static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    public static final int X_CORD = 1;
    public static final int Y_CORD = 2;
    public static final int Z_CORD = 3;

    public static int DEFAULT_NUM_CONTRAST = 3;
    public static int DEFAULT_NUM_TRANSCRIPT = 3;
    public static int DEFAULT_ARM_TASK_DURATION = 30;


    public static Map<Integer, String>    MANDATORY_SENSORS  = new HashMap<>();
    public static void initSensors(){
        if(MANDATORY_SENSORS != null && MANDATORY_SENSORS.size() > 0){
            return;
        }
        MANDATORY_SENSORS.put(Sensor.TYPE_GYROSCOPE,        "Gyroscope");
        MANDATORY_SENSORS.put(Sensor.TYPE_ACCELEROMETER,    "Accelerometer");
        MANDATORY_SENSORS.put(Sensor.TYPE_STEP_COUNTER,     "Step Counter");
    };

    public static ArrayList<WellnessTask> ALL_TAKSK = new ArrayList<>();
    public static void initWellnessTasks(){
        if(ALL_TAKSK.size() > 0){
            return;
        }
        ALL_TAKSK.add(new WellnessTask(R.string.exercise_arm_elevation, R.string.description_arm_elevation, R.drawable.arm, ArmElevationActivity.class));
        ALL_TAKSK.add(new WellnessTask(R.string.exercise_ring_rect, R.string.description_ring_rect, R.drawable.snooker, SnookerActivity.class));
        ALL_TAKSK.add(new WellnessTask(R.string.exercise_transcription, R.string.description_transcription, R.drawable.transcript, TranscriptionTestActivity.class));
        ALL_TAKSK.add(new WellnessTask(R.string.exercise_contrast, R.string.description_contrast, R.drawable.contrast, ContrastSensitivityActivity.class));
        ALL_TAKSK.add(new WellnessTask(R.string.exercise_timed_walk, R.string.description_timed_walk, R.drawable.timed, TimedWalkedActivity.class));
        ALL_TAKSK.add(new WellnessTask(R.string.up_and_go, R.string.description_up_and_go, R.drawable.up, UpAndGoActivity.class));
        ALL_TAKSK.add(new WellnessTask(R.string.stroop, R.string.description_stroop_test, R.drawable.stroop, StroopActivity.class));
    }

    public enum ContrastTestType{
        SHADES,
        ITCHI_PLATE,
        PATTERN
    }

    public static ArrayList<ContrastImageInfo> CONTRAST_EXERCISES_SHADES = new ArrayList<>();
    public static ArrayList<ContrastImageInfo> CONTRAST_EXERCISES_ITCHI = new ArrayList<>();
    public static ArrayList<ContrastImageInfo> CONTRAST_EXERCISES_PATTERN = new ArrayList<>();
    public static void initContrastExercises(){
        if(CONTRAST_EXERCISES_SHADES != null && CONTRAST_EXERCISES_SHADES.size() > 0){
            if(CONTRAST_EXERCISES_ITCHI != null && CONTRAST_EXERCISES_ITCHI.size() > 0) {
                if(CONTRAST_EXERCISES_PATTERN != null && CONTRAST_EXERCISES_PATTERN.size() > 0) {
                    return;
                }
            }
        }
        CONTRAST_EXERCISES_SHADES.add(new ContrastImageInfo("gray_14", R.color.gray_14, ContrastTestType.SHADES));
        CONTRAST_EXERCISES_SHADES.add(new ContrastImageInfo("gray_16", R.color.gray_16, ContrastTestType.SHADES));
        CONTRAST_EXERCISES_SHADES.add(new ContrastImageInfo("gray_25", R.color.gray_25, ContrastTestType.SHADES));
        CONTRAST_EXERCISES_SHADES.add(new ContrastImageInfo("gray_33", R.color.gray_33, ContrastTestType.SHADES));
        CONTRAST_EXERCISES_SHADES.add(new ContrastImageInfo("gray_37", R.color.gray_37, ContrastTestType.SHADES));
        CONTRAST_EXERCISES_SHADES.add(new ContrastImageInfo("gray_42", R.color.gray_42, ContrastTestType.SHADES));
        CONTRAST_EXERCISES_SHADES.add(new ContrastImageInfo("gray_46", R.color.gray_46, ContrastTestType.SHADES));
        CONTRAST_EXERCISES_SHADES.add(new ContrastImageInfo("gray_51", R.color.gray_51, ContrastTestType.SHADES));

        CONTRAST_EXERCISES_ITCHI.add(new ContrastImageInfo("itchi_1", R.drawable.itchi_plate_1, ContrastTestType.ITCHI_PLATE));
        CONTRAST_EXERCISES_ITCHI.add(new ContrastImageInfo("itchi_2", R.drawable.itchi_plate_2, ContrastTestType.ITCHI_PLATE));
        CONTRAST_EXERCISES_ITCHI.add(new ContrastImageInfo("itchi_3", R.drawable.itchi_plate_3, ContrastTestType.ITCHI_PLATE));
        CONTRAST_EXERCISES_ITCHI.add(new ContrastImageInfo("itchi_4", R.drawable.itchi_plate_4, ContrastTestType.ITCHI_PLATE));
        CONTRAST_EXERCISES_ITCHI.add(new ContrastImageInfo("itchi_5", R.drawable.itchi_plate_5, ContrastTestType.ITCHI_PLATE));
        CONTRAST_EXERCISES_ITCHI.add(new ContrastImageInfo("itchi_6", R.drawable.itchi_plate_6, ContrastTestType.ITCHI_PLATE));
        CONTRAST_EXERCISES_ITCHI.add(new ContrastImageInfo("itchi_7", R.drawable.itchi_plate_7, ContrastTestType.ITCHI_PLATE));
        CONTRAST_EXERCISES_ITCHI.add(new ContrastImageInfo("itchi_8", R.drawable.itchi_plate_8, ContrastTestType.ITCHI_PLATE));
        CONTRAST_EXERCISES_ITCHI.add(new ContrastImageInfo("itchi_9", R.drawable.itchi_plate_9, ContrastTestType.ITCHI_PLATE));


        CONTRAST_EXERCISES_PATTERN.add(new ContrastImageInfo("pattern_1", R.drawable.pattern_1, ContrastTestType.PATTERN));
        CONTRAST_EXERCISES_PATTERN.add(new ContrastImageInfo("pattern_2", R.drawable.pattern_2, ContrastTestType.PATTERN));
        CONTRAST_EXERCISES_PATTERN.add(new ContrastImageInfo("pattern_3", R.drawable.pattern_3, ContrastTestType.PATTERN));
        CONTRAST_EXERCISES_PATTERN.add(new ContrastImageInfo("pattern_4", R.drawable.pattern_4, ContrastTestType.PATTERN));

    }

    //All wav files are created using http://www.text2speech.org/
    public static ArrayList<AudioData>    TRANSCRIPT_EXERCISES = new ArrayList<>();
    public static void initTranscriptExercises(Context context){
        if(TRANSCRIPT_EXERCISES != null && TRANSCRIPT_EXERCISES.size() > 0){
            return;
        }
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.another_day, context.getString(R.string.another_day)));
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.frankly_speaking, context.getString(R.string.frankly_speaking)));
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.keep_in_touch, context.getString(R.string.keep_in_touch)));
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.nice_to_meet_you, context.getString(R.string.transcript_greet)));
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.no_problem, context.getString(R.string.no_problem)));
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.take_a_message, context.getString(R.string.take_a_message)));
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.take_me_away, context.getString(R.string.take_me_away)));
        TRANSCRIPT_EXERCISES.add(new AudioData(R.raw.see_you_later, context.getString(R.string.see_you_later)));

    }

}
