package com.github.pocmo.sensordashboard.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.ui.PathSegment;
import com.github.pocmo.sensordashboard.ui.RingArea;
import com.github.pocmo.sensordashboard.utils.FileUtils;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neerajpaliwal on 01/07/16.
 */
public class SnookerActivity extends AppCompatActivity {
    BallBounces ball;
    private BecareRemoteSensorManager mRemoteSensorManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(SnookerActivity.this);

        ball = new BallBounces(this, mRemoteSensorManager);
        setContentView(ball);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mRemoteSensorManager.startMeasurement();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mRemoteSensorManager.getUploadDataHelper().setUserActivity(null, null);
        mRemoteSensorManager.stopMeasurement();
    }

}

class PathPoint{
    float x;
    float y;

    public PathPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }
}

class BallBounces extends SurfaceView implements SurfaceHolder.Callback {

    int screenW; //Device's screen width.
    int screenH; //Devices's screen height.
    int ballX; //Ball x position.
    int ballY; //Ball y position.
    int pathX, pathY;
    int initialY ;
    float dY; //Ball vertical speed.
    int ballW;
    int ballH;
    int bgrW;
    int bgrH;
    int angle;
    int bgrScroll;
    int dBgrY; //Background scroll speed.
    float acc;
    Bitmap ball, bgr, bgrReverse;
    boolean reverseBackroundFirst;
    boolean ballFingerMove;

    //Measure frames per second.
    long now;
    int framesCount=0;
    int framesCountAvg=0;
    long framesTimer=0;
    Paint fpsPaint=new Paint();

    private List<PathPoint> pathPoints = new ArrayList<>();
    private List<PathPoint> pathPointTemps = new ArrayList<>();
    private boolean BUILD_PATH = false;
    BecareRemoteSensorManager mRemoteSensorManager = null;

    public BallBounces(Context context, BecareRemoteSensorManager sensorMgr) {
        super(context);
        ball = BitmapFactory.decodeResource(getResources(), R.drawable.football); //Load a ball image.
        bgr = BitmapFactory.decodeResource(getResources(), R.drawable.road1); //Load a background.
        ballW = ball.getWidth();
        ballH = ball.getHeight();

        //Create a flag for the onDraw method to alternate background with its mirror image.
        reverseBackroundFirst = false;

        //Initialise animation variables.
        acc = 0.2f; //Acceleration
        dY = 0; //vertical speed
        initialY = 100; //Initial vertical position
        angle = 0; //Start value for the rotation angle
        bgrScroll = 0;  //Background scroll position
        dBgrY = 1; //Scrolling background speed

        fpsPaint.setTextSize(20);
        fpsPaint.setColor(Color.rgb(200, 220, 220));
        //Set thread
        getHolder().addCallback(this);

        setFocusable(true);
        setWillNotDraw(false);
        buildPathFromFile(context);
        mRemoteSensorManager = BecareRemoteSensorManager.getInstance(context);

    }

    private void buildPathFromFile(Context context) {
        try {
            Gson gson = new Gson();
            String pathData = FileUtils.readResourceToString(context, R.raw.road1_path);
            JSONArray dataArr = new JSONArray(pathData);
            for(int i=0 ;i < dataArr.length(); i++){
                pathPoints.add(i, gson.fromJson(dataArr.getJSONObject(i).toString(), PathPoint.class));
            }
            ViewTreeObserver vto = getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    int width = getMeasuredWidth();
                    int height = getMeasuredHeight();

                    for (int i = 0; i < pathPoints.size(); i++) {
                        //TODO: For road1, we have captured absolute cordinate, hence divide by screen size
                        // We will be storing percentage cordinate, so won't be required
                        pathPointTemps.add(i, new PathPoint(width * (pathPoints.get(i).x / 720f),
                                height * (pathPoints.get(i).y / 1170f)));

                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //This event-method provides the real dimensions of this custom view.
        screenW = w;
        screenH = h;

        bgr = Bitmap.createScaledBitmap(bgr, w, h, true); //Scale background to fit the screen.
        bgrW = bgr.getWidth();
        bgrH = bgr.getHeight();

        //Create a mirror image of the background (horizontal flip) - for a more circular background.
        Matrix matrix = new Matrix();  //Like a frame or mould for an image.
        matrix.setScale(-1, 1); //Horizontal mirror effect.
        bgrReverse = Bitmap.createBitmap(bgr, 0, 0, bgrW, bgrH, matrix, true); //Create a new mirrored bitmap by applying the matrix.

        ballX = 0;
        ballY = screenH -60;
    }


    private int getPathXforTouchY(int touchY){
        int pathX = -1, i=0;
        for(PathPoint curr : pathPointTemps){
            if(touchY >= curr.y){
                PathPoint prev = pathPointTemps.get(i);
                return (int) (curr.x + (prev.x - curr.x)* (curr.y - touchY) / (touchY - prev.y));
            }
            i++;
        }

        return pathX;
    }

    //***************************************
    //*************  TOUCH  *****************
    //***************************************
    @Override
    public synchronized boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if(BUILD_PATH){
                    //pathPoints.add(new PathPoint((int)ev.getX(), (int)ev.getY()));
                }else {
                    ballX = (int) ev.getX() - ballW / 2;
                    ballY = (int) ev.getY() - ballH / 2;

                    ballFingerMove = true;
                }
                invalidate();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                ballX = (int) ev.getX() - ballW/2;
                ballY = (int) ev.getY() - ballH/2;
                pathY = (int) ev.getY();
                pathX = getPathXforTouchY(pathY);
                //Log.d("pathDebug", "tY:" + ev.getY() + ", tX:" + ev.getX() + ", pathX:" + pathX);
                if(mRemoteSensorManager == null) {
                    String value = "(" + pathX + "," + pathY + ") (" + (int) ev.getX() + "," + (int) ev.getY() + ")";
                    mRemoteSensorManager.getUploadDataHelper().setUserActivity("Snooker", value);
                }
                invalidate();
                break;
            }

            case MotionEvent.ACTION_UP:
                ballFingerMove = false;
                dY = 0;
                invalidate();
                break;
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect fromRect = new Rect(0, 0, bgrW, bgrH);
        Rect toRect = new Rect(0, 0, bgrW, bgrH);


        canvas.drawBitmap(bgr,fromRect, toRect, null);

        if(BUILD_PATH){
            Path path = new Path();
            boolean first = true;
            for(int i = 0; i < pathPointTemps.size(); i += 2){
                PathPoint point = pathPointTemps.get(i);
                if(first){
                    first = false;
                    path.moveTo(point.x, point.y);
                }

                else if(i < pathPointTemps.size() - 1){
                    PathPoint next = pathPointTemps.get(i + 1);
                    path.quadTo(point.x, point.y, next.x, next.y);
                }
                else{
                    path.lineTo(point.x, point.y);
                }
            }
            Paint p = new Paint();
            // smooths
            p.setAntiAlias(true);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2);
            p.setColor(Color.RED);
            canvas.drawPath(path, p);
            canvas.drawCircle(pathX, pathY, 50, p);

        }else {
            //Next value for the background's position.
            if ((bgrScroll += dBgrY) >= bgrW) {
                bgrScroll = 0;
                reverseBackroundFirst = !reverseBackroundFirst;
            }

            if (!ballFingerMove) {
                ballY += (int) dY; //Increase or decrease vertical position.
                if (ballY > (screenH - ballH)) {
                    dY = (-1) * dY; //Reverse speed when bottom hit.
                }
                dY += acc; //Increase or decrease speed.
            }

            angle += 5;
            if (angle > 360)
                angle = 0;



            canvas.save(); //Save the position of the canvas matrix.
            canvas.rotate(angle, ballX + (ballW / 2), ballY + (ballH / 2)); //Rotate the canvas matrix.
            canvas.drawBitmap(ball, ballX, ballY, null); //Draw the ball by applying the canvas rotated matrix.
        }
        canvas.restore(); //Rotate the canvas matrix back to its saved position - only the ball bitmap was rotated not all canvas.
        //*/

        //Measure frame rate (unit: frames per second).
        now=System.currentTimeMillis();

        canvas.drawText("Deviation from path: (" + pathX + ", " + pathY, 30, 30, fpsPaint);

        framesCount++;
        if(now-framesTimer>1000) {
            framesTimer=now;
            framesCountAvg=framesCount;
            framesCount=0;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //    thread = new GameThread(getHolder(), this);
        //   thread.setRunning(true);
        //  thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;

    }




}