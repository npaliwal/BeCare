package com.github.pocmo.sensordashboard.activities;



//import com.juzi.main.AppConnect;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import ldp.games.doodlejump.DoodleJumpActivity;
import ldp.games.doodlejump.GameView;
import ldp.games.doodlejump.otherviews.AboutView;
import ldp.games.doodlejump.otherviews.ExitView;
import ldp.games.doodlejump.otherviews.FailView;
import ldp.games.doodlejump.otherviews.OptionView;
import ldp.games.doodlejump.otherviews.ScoreView;
import ldp.games.doodlejump.otherviews.WelcomeView;
import ldp.games.doodlejump.resource.SoundPlayer;

public class TapTaskActivity extends DoodleJumpActivity {
    /** Called when the activity is first created. */
	
	public static final int GAME_OVER  = 0;
	public static final int GAME_START = 1;
	public static final int SCORE      = 2;
	public static final int ABOUT      = 3;
	public static final int EXIT       = 4;
	public static final int WELCOME    = 5;
	public static final int OPTION     = 6;
	
	private GameView gameView;
	private FailView failView;
	private SensorManager sensorManager;
	private MySensorEventListener sensorEventListener;
	int pre_speed = 0;
	View current_view;
	
	public static boolean isGame_Running = false;
	public static float screen_width;
	public static float screen_height;
	public static float width_mul;
	public static float height_mul;
	
	
	public Handler handler = new Handler(){
		 public void handleMessage(Message msg) {
			 if(msg.what == GAME_OVER){ //��Ϸʧ��,ȥ��failview
				current_view = null;
				initFailView();
			 }
			 if(msg.what == GAME_START){
				isGame_Running = true;
			    initGameView();
			 }
		 }

	

	};

	private void initFailView() {
		failView = new FailView(TapTaskActivity.this);
		current_view = failView;
		setContentView(failView);
		gameView = null;
	}

	
	private void initGameView() {
		gameView = new GameView(TapTaskActivity.this);
		current_view = gameView;
		setContentView(gameView);
	}
	
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
      //  Debug.startMethodTracing("AndroidJumpTrace");
        DisplayMetrics dm;
		dm = new DisplayMetrics();  
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);     
		screen_width = dm.widthPixels;  
		screen_height = dm.heightPixels;
		width_mul = screen_width/320;
		height_mul = screen_height/480;
		if(screen_height >= 800)
			height_mul = (float) 1.5;
		SoundPlayer.initSound(this);
		initGameView();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
       

		Log.e("fuck", ""+ TapTaskActivity.height_mul);
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(current_view == gameView){
			  if( (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) || (keyCode == KeyEvent.KEYCODE_HOME && event.getRepeatCount() == 0)){  
				  //GameView.ispause = true;
				  handler.sendEmptyMessage(EXIT);

				  return true;
		      }  
		}
		else{
			 if( (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) || (keyCode == KeyEvent.KEYCODE_HOME && event.getRepeatCount() == 0)){  
		           return true;  
		      } 
		}
		return super.onKeyDown(keyCode, event);
	}





	@Override
	protected void onResume() {
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorEventListener = new MySensorEventListener();
		sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onPause() {
		sensorManager.unregisterListener(sensorEventListener);
	//	Debug.stopMethodTracing();
		super.onPause();
	}

	
	private final class MySensorEventListener implements SensorEventListener{
        
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if(current_view == gameView){
				float X = event.values[SensorManager.DATA_X];
				pre_speed += X;
				if(X > 0.45 || X < -0.45){
					int temp = X > 0 ? 4 : -4;
					if(pre_speed > 7 || pre_speed < -7)
						pre_speed = pre_speed > 0 ? 7 : -7;
					gameView.logicManager.SetAndroid_HSpeed(pre_speed + temp);
				}
			}
		}
		
	}
	
    
}