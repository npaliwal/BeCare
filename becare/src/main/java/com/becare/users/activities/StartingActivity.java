package com.becare.users.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;


import com.becare.users.AppConfig;
import com.becare.users.AppConstant;
import com.becare.users.BecareRemoteSensorManager;
import com.becare.users.MenuAdapter;
import com.becare.users.PreferenceStorage;
import com.becare.users.R;
import com.becare.users.events.BusProvider;
import com.becare.users.events.LoginCompleted;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Map;


public class StartingActivity extends AppCompatActivity {
    private static final String TAG = StartingActivity.class.getSimpleName();

    private SensorManager mSensorManager;
    private BecareRemoteSensorManager remoteSensorManager;
    private List<Node> mNodes;
    private ListView menuItems;
    private MenuAdapter adapter;
    private TextView welcome;

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

        welcome = (TextView)findViewById(R.id.welcome_name);
        menuItems = (ListView) findViewById(R.id.menu_list);
        adapter = new MenuAdapter(StartingActivity.this);
        menuItems.setAdapter(adapter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        instance = this;

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

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
        welcome.setText("Hello " + preferenceStorage.getUserName());
    }



    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);

        if(preferenceStorage.isLoggedIn()) {

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
        BusProvider.getInstance().unregister(this);
        if(preferenceStorage.isLoggedIn()) {

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

    @Subscribe
    public void onLoginCompletedEvent(LoginCompleted event){
        Log.d(TAG, "Login completed with name=" + event.getName());
        welcome.setText("Hello " + preferenceStorage.getUserName());
    }
}
