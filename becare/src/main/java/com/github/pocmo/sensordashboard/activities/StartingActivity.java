package com.github.pocmo.sensordashboard.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ListViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;


import com.github.pocmo.sensordashboard.AppConfig;
import com.github.pocmo.sensordashboard.AppConstant;
import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.MenuAdapter;
import com.github.pocmo.sensordashboard.PreferenceStorage;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.events.BusProvider;
import com.github.pocmo.sensordashboard.events.SensorUpdatedEvent;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Map;


public class StartingActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private BecareRemoteSensorManager remoteSensorManager;
    private List<Node> mNodes;
    private ListView menuItems;
    private MenuAdapter adapter;

    private PreferenceStorage preferenceStorage;
    private static StartingActivity instance;

    public static synchronized StartingActivity getInstance(){
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig.initSensors();
        AppConfig.initWellnessTasks();
        setContentView(R.layout.activity_main);

        menuItems = (ListView) findViewById(R.id.menu_list);
        adapter = new MenuAdapter(StartingActivity.this);
        menuItems.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        instance = this;


        preferenceStorage = new PreferenceStorage(getApplicationContext());

        if(preferenceStorage.isLoggedIn()){
            initialize();
        }else{
            Intent loginIntent = new Intent(StartingActivity.this, LoginActivity.class);
            startActivityForResult(loginIntent, LoginActivity.REQUEST_LOGIN_CODE);
        }
    }


    private void initialize(){
        checkAndConfigureSocket();
        checkandConfigureTaskSettings();
        remoteSensorManager = BecareRemoteSensorManager.getInstance(StartingActivity.this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        checkSensorCompatiblity();
    }



    @Override
    protected void onResume() {
        super.onResume();

        if(preferenceStorage.isLoggedIn()) {
            BusProvider.getInstance().register(this);

            remoteSensorManager.startMeasurement();

            remoteSensorManager.getNodes(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult pGetConnectedNodesResult) {
                    mNodes = pGetConnectedNodesResult.getNodes();

                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(preferenceStorage.isLoggedIn()) {
            BusProvider.getInstance().unregister(this);

            remoteSensorManager.stopMeasurement();
        }
    }

    private void checkAndConfigureSocket(){
        String defaultIp = preferenceStorage.getSocketIp();
        int defaultPort = preferenceStorage.getSocketPort();

        if(defaultIp == null){
            defaultIp = AppConstant.DEFAULT_SOCKET_IP;
        }

        if(defaultPort == -1){
            defaultPort = AppConstant.DEFAULT_SOCKET_PORT;
        }

        if(preferenceStorage.getSocketIp() == null){
            preferenceStorage.setSocketInfo(defaultIp, defaultPort);
        }
    }
    private void checkandConfigureTaskSettings() {
        if(preferenceStorage.getArmElevationTaskDuration() == -1){
            preferenceStorage.setArmElevationTaskDuration(AppConfig.DEFAULT_ARM_TASK_DURATION);
        }
    }

    public void checkSensorCompatiblity(){
        //allSensorsAvailable = true;
        String sensorStatus = "Mobile Sensors Status:";

        for(Map.Entry<Integer, String> entry: AppConfig.MANDATORY_SENSORS.entrySet()){
            sensorStatus += "\n\t" + entry.getValue() + " : ";
            if (mSensorManager.getDefaultSensor(entry.getKey()) == null) {
                //allSensorsAvailable = false;
                sensorStatus += "Missing";
            }else{
                sensorStatus += "Available";
            }
        }
    }

    @Override
    public void onBackPressed() {
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //if (drawer.isDrawerOpen(GravityCompat.START)) {
        //    drawer.closeDrawer(GravityCompat.START);
        //} else {
            super.onBackPressed();
        //}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            MenuUtils.getTools(StartingActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void getMenuLayout(View view) {
        Intent intent = new Intent(this, MenuUtils.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == LoginActivity.REQUEST_LOGIN_CODE){
            if(resultCode == LoginActivity.LOGIN_RESULT_SUCCESS || resultCode == LoginActivity.LOGIN_RESULT_SKIPPED){
                initialize();
            }else{
                finish();
            }
        }
    }

}
