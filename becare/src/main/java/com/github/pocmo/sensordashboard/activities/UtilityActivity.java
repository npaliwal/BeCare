package com.github.pocmo.sensordashboard.activities;

import android.app.ListActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.SensorAdapter;

import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


/**
 * Created by neerajpaliwal on 04/05/16.
 */
@ContentView(R.layout.activity_utility)
public class UtilityActivity extends RoboActivity {
    private SensorManager mSensorManager;
    private SensorAdapter adapter;
    private PreferenceStorage preferenceStorage;

    @InjectView(R.id.connection_header)
    TextView uploadSettingsHeader;
    @InjectView(R.id.connection_container)
    View uploadSettingContainer;
    @InjectView(R.id.ev_public_ip)
    private EditText socketIp;
    @InjectView(R.id.ev_public_port)
    private EditText socketPort;
    @InjectView(R.id.update_button)
    private Button   updateSocket;
    @InjectView(R.id.test_button)
    private Button   testSocket;
    boolean uploadSettingsExpand = false;


    @InjectView(R.id.arm_header)
    TextView armSettingsHeader;
    @InjectView(R.id.arm_container)
    View armSettingContainer;
    @InjectView(R.id.arm_duration_incr)
    TextView armDurationIncr;
    @InjectView(R.id.arm_duration)
    TextView armDuration;
    @InjectView(R.id.arm_duration_decr)
    TextView armDurationDecr;
    boolean armSettingsExpand = false;
    int armDurationVal = -1;


    @InjectView(R.id.lv_sensors)
    private ListView sensorsList;

    private CheckBox radioAccel;
    private RadioGroup radioGyro;

    private BecareRemoteSensorManager remoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceStorage = new PreferenceStorage(getApplicationContext());
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        ArrayList<Sensor> deviceSensorsArr = new ArrayList<>(deviceSensors);

        adapter = new SensorAdapter(getApplicationContext(), deviceSensorsArr);
        sensorsList.setAdapter(adapter);

        initClickListeners();

        //Upload Settings
        socketIp.setText(preferenceStorage.getSocketIp());
        socketPort.setText("" + preferenceStorage.getSocketPort());
        updateSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int port = -1;
                try {
                    port = Integer.valueOf(socketPort.getText().toString());
                } catch (Exception e) {
                }
                preferenceStorage.setSocketInfo(socketIp.getText().toString(), port);
                Toast.makeText(UtilityActivity.this, "Socket Info updated", Toast.LENGTH_LONG).show();
                remoteSensorManager.getSocketManager().refresh(preferenceStorage);

            }
        });
        testSocket.setOnClickListener(testSocketCilck);

        armDurationVal = preferenceStorage.getArmElevationTaskDuration();
        armDuration.setText("" + armDurationVal);

        remoteSensorManager = BecareRemoteSensorManager.getInstance(UtilityActivity.this);
    }

    private void initClickListeners(){
        uploadSettingsHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadSettingsExpand = !uploadSettingsExpand;
                adjustContainers();
            }
        });

        armSettingsHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                armSettingsExpand = !armSettingsExpand;
                adjustContainers();
            }
        });
        armDurationIncr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                armDurationVal++;
                armDuration.setText(""+armDurationVal);
                preferenceStorage.setArmElevationTaskDuration(armDurationVal);
            }
        });

        armDurationDecr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(armDurationVal <= 10)
                    return;
                armDurationVal--;
                armDuration.setText(""+armDurationVal);
                preferenceStorage.setArmElevationTaskDuration(armDurationVal);
            }
        });
    }

    private void adjustContainers(){
        if(uploadSettingsExpand){
            uploadSettingContainer.setVisibility(View.VISIBLE);
        }else{
            uploadSettingContainer.setVisibility(View.GONE);
        }

        if(armSettingsExpand){
            armSettingContainer.setVisibility(View.VISIBLE);
        }else{
            armSettingContainer.setVisibility(View.GONE);
        }

    }

    private View.OnClickListener testSocketCilck = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean success = true;
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ip", socketIp.getText().toString());
                jsonObject.put("port", socketPort.getText().toString());

                remoteSensorManager.getSocketManager().pushData(jsonObject.toString());
            } catch (UnknownHostException e) {
                success = false;
                e.printStackTrace();
            } catch (IOException e) {
                success = false;
                e.printStackTrace();
            } catch (Exception e) {
                success = false;
                e.printStackTrace();
            }finally {
                if(success){
                    Toast.makeText(UtilityActivity.this, "Data sent to socket successfully !!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(UtilityActivity.this, "Data sent to socket FAILED !!", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private int debugAcceleroMode = -1;
    private int debugGyrooMode = -1;

    public void onDebugRadioClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.radio_accel_all:
                if (checked)
                    debugAcceleroMode = 4;
                break;
            case R.id.radio_accel_x:
                if (checked)
                    debugAcceleroMode = 1;
                break;

            case R.id.radio_accel_y:
                if (checked)
                    debugAcceleroMode = 2;
                break;

            case R.id.radio_accel_z:
                if (checked)
                    debugAcceleroMode = 3;
                break;
            case R.id.radio_accel_none:
                if (checked)
                    debugAcceleroMode = -1;
                break;

            case R.id.radio_gyro_all:
                if (checked)
                    debugGyrooMode = 4;
                break;
            case R.id.radio_gyro_x:
                if (checked)
                    debugGyrooMode = 1;
                break;

            case R.id.radio_gyro_y:
                if (checked)
                    debugGyrooMode = 2;
                break;

            case R.id.radio_gyro_z:
                if (checked)
                    debugGyrooMode = 3;
                break;
            case R.id.radio_gyro_none:
                if (checked)
                    debugGyrooMode = -1;
                break;
        }
    }

}
