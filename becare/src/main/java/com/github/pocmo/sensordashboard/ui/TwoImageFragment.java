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
import com.github.pocmo.sensordashboard.model.TwoImageInfo;
import com.github.pocmo.sensordashboard.utils.ImageUtils;

/**
 * Created by neerajpaliwal on 07/06/16.
 */
public class TwoImageFragment extends Fragment {
    ImageView leftPatch, rightPatch;
    Bitmap leftBitMap=null, rightMap=null;
    TextView header;

    private static String EXTRA_EXERCISE_ID     = "exercise_data";

    public static TwoImageFragment newInstance(TwoImageInfo exerciseData) {
        TwoImageFragment fragmentFirst = new TwoImageFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_EXERCISE_ID, exerciseData);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    TwoImageInfo exercise = null;
    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exercise = getArguments().getParcelable(EXTRA_EXERCISE_ID);
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
            if(exercise.getLeftImage().getColor() == -1){
                header.setText(R.string.contrast_test_contrast);
                leftPatch.setScaleType(ImageView.ScaleType.FIT_XY);
                leftPatch.setImageResource(exercise.getLeftImage().getImageResId());
                //leftBitMap = ImageUtils.getBitmapFromResource(exercise.getLeftImage().getImageResId(), getActivity());
                //leftPatch.setImageBitmap(
                //        leftBitMap
                //        //ImageUtils.changeBitmapContrastBrightness(leftBitMap, exercise.getLeftImage().getContrast(), 0)
                //);

                rightPatch.setScaleType(ImageView.ScaleType.FIT_XY);
                rightPatch.setImageResource(exercise.getRightImage().getImageResId());
                //rightMap = ImageUtils.getBitmapFromResource(exercise.getRightImage().getImageResId(), getActivity());
                //rightPatch.setImageBitmap(
                //        rightMap
                //        //ImageUtils.changeBitmapContrastBrightness(rightMap, exercise.getRightImage().getContrast(), 0)
                //);

            }else{
                header.setText(R.string.contrast_test_color);
                leftPatch.setBackgroundResource(exercise.getLeftImage().getColor());
                rightPatch.setBackgroundResource(exercise.getRightImage().getColor());
            }
        }
        return view;
    }

    public void disposeBitMaps() {
        if(leftBitMap != null) {
            leftBitMap = null;
            leftBitMap.recycle();
        }
        if(rightMap != null) {
            rightMap.recycle();
            rightMap = null;
        }

    }
}
