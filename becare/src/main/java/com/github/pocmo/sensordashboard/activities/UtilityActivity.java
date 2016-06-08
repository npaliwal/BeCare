package com.github.pocmo.sensordashboard.activities;

import android.app.ListActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.SensorAdapter;

import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by neerajpaliwal on 04/05/16.
 */
public class UtilityActivity extends ListActivity {
    private SensorManager mSensorManager;
    private SensorAdapter adapter;
    private PreferenceStorage preferenceStorage;

    private EditText socketIp;
    private EditText socketPort;
    private Button   updateSocket;
    private Button   testSocket;
    private CheckBox radioAccel;
    private RadioGroup radioGyro;

    private BecareRemoteSensorManager remoteSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility);

        preferenceStorage = new PreferenceStorage(getApplicationContext());
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        ArrayList<Sensor> deviceSensorsArr = new ArrayList<>(deviceSensors);

        adapter = new SensorAdapter(getApplicationContext(), deviceSensorsArr);
        getListView().setAdapter(adapter);

        socketIp = (EditText)findViewById(R.id.ev_public_ip);
        socketIp.setText(preferenceStorage.getSocketIp());

        socketPort = (EditText)findViewById(R.id.ev_public_port);
        socketPort.setText(""+preferenceStorage.getSocketPort());

        updateSocket = (Button)findViewById(R.id.update_button);
        updateSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int port = -1;
                try{
                    port = Integer.valueOf(socketPort.getText().toString());
                }catch (Exception e){}
                if(port < 999 || port > 999999){
                    Toast.makeText(UtilityActivity.this, "Socket Port should be 4 digit integer", Toast.LENGTH_LONG).show();
                }else {
                    preferenceStorage.setSocketInfo(socketIp.getText().toString(), port);
                    Toast.makeText(UtilityActivity.this, "Socket Info updated", Toast.LENGTH_LONG).show();
                    remoteSensorManager.getSocketManager().refresh(preferenceStorage);
                }
            }
        });
        testSocket = (Button)findViewById(R.id.test_button);
        testSocket.setOnClickListener(testSocketCilck);

        remoteSensorManager = BecareRemoteSensorManager.getInstance(UtilityActivity.this);
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
