package com.becare.users.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.becare.users.R;
import com.becare.users.activities.BallRectangleActivity;
import com.becare.users.activities.StartingActivity;
import com.becare.users.data.UploadDataHelper;
import com.becare.users.network.PostClass;


/**
 * Created by neerajpaliwal on 06/04/16.
 */
public class SummaryFragment extends Fragment {

    private View emptyState;
    private ImageView watchImage;
    private TextView accelerometer;
    private TextView gyroscope;
    private Button uploadData;
    private TextView uploadDataStatus;

    private UploadDataHelper mUploadDataHelper = null;
    private StartingActivity mainActivity;

    private Button running, walking, sleeping;
    private String userActivity = "NA";
    private String RUNNING = "running";
    private String WALKING = "walking";
    private String SLEEPING = "sleeping";

    public void setMainActivity(StartingActivity activity){
        mainActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary, container, false);

        running = (Button)rootView.findViewById(R.id.running);
        walking = (Button)rootView.findViewById(R.id.walking);
        sleeping = (Button)rootView.findViewById(R.id.sleeping);

        watchImage = (ImageView)rootView.findViewById(R.id.iv_watch);
        watchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonUnPressed();
                userActivity = "NA";
            }
        });

        setupActivityButton();
        emptyState = rootView.findViewById(R.id.empty_state);
        accelerometer = (TextView)rootView.findViewById(R.id.accelorometer_reading);
        gyroscope = (TextView)rootView.findViewById(R.id.gyroscope_reading);
        uploadData = (Button)rootView.findViewById(R.id.upload_data);
        uploadDataStatus = (TextView)rootView.findViewById(R.id.upload_status);
        uploadData.setOnClickListener(uploadDataAction);

        return rootView;
    }

    private void setButtonUnPressed(){
        running.setSelected(false);
        walking.setSelected(false);
        sleeping.setSelected(false);
    }

    private void setupActivityButton(){
        setButtonUnPressed();
        if(RUNNING.equals(userActivity))
            running.setSelected(true);

        if(WALKING.equals(userActivity))
            walking.setSelected(true);

        if(SLEEPING.equals(userActivity))
            sleeping.setSelected(true);

        running.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonUnPressed();
                running.setSelected(true);
                userActivity = RUNNING;
//                RemoteSensorManager.getInstance(getActivity()).setUserActivity(userActivity);
                startActivity(new Intent(getActivity(), BallRectangleActivity.class));
            }
        });

        walking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonUnPressed();
                walking.setSelected(true);
                userActivity = WALKING;
//                RemoteSensorManager.getInstance(getActivity()).setUserActivity(userActivity);
            }
        });

        sleeping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonUnPressed();
                sleeping.setSelected(true);
                userActivity = SLEEPING;
//                RemoteSensorManager.getInstance(getActivity()).setUserActivity(userActivity);
            }
        });
    }

    private View.OnClickListener uploadDataAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            uploadDataStatus.setText("Uploading...");
            //mUploadDataHelper = RemoteSensorManager.getInstance(getActivity()).getUploadDataHelper();
            //mUploadDataHelper.update(mainActivity.getGyroCurrData(), mainActivity.getAcceleroCurrData(), userActivity);
            new PostClass(mainActivity, uploadDataStatus, mUploadDataHelper).execute();
        }
    };

    public void setEmptyStateVisiblity(boolean visiblity){
        if(emptyState == null)
            return;
        if(visiblity)
            emptyState.setVisibility(View.VISIBLE);
        else
            emptyState.setVisibility(View.GONE);
    }

    private void setAllVisible(){
        emptyState.setVisibility(View.GONE);
        uploadData.setVisibility(View.VISIBLE);
        running.setVisibility(View.VISIBLE);
        sleeping.setVisibility(View.VISIBLE);
        walking.setVisibility(View.VISIBLE);

    }
    public void setGyroVisible() {
        if(gyroscope == null)
            return;
        gyroscope.setVisibility(View.VISIBLE);
        setAllVisible();
    }

    public void setAccelerovisible() {
        if(accelerometer == null)
            return;
        accelerometer.setVisibility(View.VISIBLE);
        setAllVisible();
    }

    public void updateGyroText() {
        //if(gyroscope != null)
        //    gyroscope.setText("Gyroscope : \n(" + mainActivity.getGyroCurrData().getValueString() + ")");
    }

    public void updateAcceleroText() {
        //if(accelerometer != null)
        //    accelerometer.setText("Accelerometer : \n(" + mainActivity.getAcceleroCurrData().getValueString() + ")");

    }

}
