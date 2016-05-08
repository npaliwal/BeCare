package me.smartwatches.becare.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.smartwatches.becare.R;
import me.smartwatches.becare.SensorAdapter;

public class StartingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SensorManager mSensorManager;
    private boolean allSensorsAvailable = true;
    private TextView mSensorStatus, mCompatiblityStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorStatus = (TextView)findViewById(R.id.tv_header);
        mCompatiblityStatus = (TextView)findViewById(R.id.tv_compatiblity);
        checkSensorCompatiblity();
    }

    public void checkSensorCompatiblity(){
        allSensorsAvailable = true;
        String sensorStatus = "Sensors Status: ";

        sensorStatus += "\n\tAccelerometer : ";
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            allSensorsAvailable = false;
            sensorStatus += "Missing";
        }else{
            sensorStatus += "Available";
        }

        sensorStatus += "\n\tGyroscope : ";
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null) {
            allSensorsAvailable = false;
            sensorStatus += "Missing";
        }else{
            sensorStatus += "Available";
        }

        mSensorStatus.setText(sensorStatus);
        if(allSensorsAvailable){
            mCompatiblityStatus.setTextColor(ContextCompat.getColor(this, R.color.green));
            mCompatiblityStatus.setText("Awesome!!!\nAll sensors available");
        }else{
            mCompatiblityStatus.setTextColor(ContextCompat.getColor(this, R.color.red));
            mCompatiblityStatus.setText("Oops!!!\nSome exercises will not be available to you");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_arm_elavation) {
            // Handle the arm elevation test
        } else if (id == R.id.nav_snooker) {
            startActivity(new Intent(this, BallRectangleActivity.class));
        } else if (id == R.id.nav_transcription) {
            // Handle the transription test
        } else if (id == R.id.nav_contranst) {
            // Handle the transription test

        } else if (id == R.id.nav_timed_walk) {
            // Handle the transription test

        } else if (id == R.id.nav_share) {
            // Handle the transription test

        } else if (id == R.id.nav_tools) {
            startActivity(new Intent(this, SensorsListActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
