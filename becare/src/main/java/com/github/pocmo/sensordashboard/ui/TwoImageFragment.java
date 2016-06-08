package com.github.pocmo.sensordashboard.ui;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.activities.ContrastSensitivityActivity;
import com.github.pocmo.sensordashboard.utils.ImageUtils;

/**
 * Created by neerajpaliwal on 07/06/16.
 */
public class TwoImageFragment extends Fragment {
    ImageView leftPatch, rightPatch;
    TextView header;

    private static String EXTRA_EXERCISE_ID     = "exercise_id";

    public static TwoImageFragment newInstance(int index) {
        TwoImageFragment fragmentFirst = new TwoImageFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_EXERCISE_ID, index);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    ContrastSensitivityActivity.TwoImageInfo exercise = null;
    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int index = getArguments().getInt(EXTRA_EXERCISE_ID, -1);
        exercise = ContrastSensitivityActivity.exercises.get(index);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two_image, container, false);
        leftPatch = (ImageView)view.findViewById(R.id.image_left);
        rightPatch = (ImageView)view.findViewById(R.id.image_right);
        header = (TextView)view.findViewById(R.id.test_header);

        if(exercise != null){
            if(exercise.getImageResId() > -1){
                header.setText(R.string.contrast_test_contrast);
                leftPatch.setScaleType(ImageView.ScaleType.FIT_XY);
                leftPatch.setImageBitmap(ImageUtils.getBitmapFromResource(exercise.getImageResId(), getActivity()));

                rightPatch.setScaleType(ImageView.ScaleType.FIT_XY);
                rightPatch.setImageBitmap(
                        ImageUtils.changeBitmapContrastBrightness(
                                ImageUtils.getBitmapFromResource(exercise.getImageResId(), getActivity()),
                                exercise.getContrast(),
                                0)
                );

            }else{
                header.setText(R.string.contrast_test_color);
                leftPatch.setBackgroundColor(Color.parseColor(exercise.getLefColor()));
                rightPatch.setBackgroundColor(Color.parseColor(exercise.getRightColor()));
            }
        }

        return view;
    }
}
