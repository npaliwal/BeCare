package com.github.pocmo.sensordashboard.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.utils.FileUtils;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by neerajpaliwal on 11/07/16.
 */
public class BallBounces extends SurfaceView implements SurfaceHolder.Callback {

    int screenW; //Device's screen width.
    int screenH; //Devices's screen height.
    int ballX; //Ball x position.
    int ballY; //Ball y position.
    int pathX, pathY;
    int initialY;
    float dY; //Ball vertical speed.
    int ballW;
    int ballH;
    int bgrW;
    int bgrH;
    int treeW;
    int treeH;
    float treeXpos = 400;
    float treeYpos = -20;
    int angle;
    int bgrScroll;
    int dBgrY; //Background scroll speed.
    float acc;
    Bitmap ball, bgr, bgrReverse, tree, stop;
    boolean reverseBackroundFirst;
    boolean ballFingerMove;
    boolean started=false;
    int stopX, stopY;
    int startX, startY;

    //Measure frames per second.
    long now;
    int framesCount = 0;
    int framesCountAvg = 0;
    long framesTimer = 0;
    Paint fpsPaint = new Paint();

    private List<PathPoint> pathPoints = new ArrayList<>();
    private List<PathPoint> pathPointTemps = new ArrayList<>();
    BecareRemoteSensorManager mRemoteSensorManager = null;
    private int BUILD_PATH_MODE=0;

    public BallBounces(Context context){
        super(context);
        setFocusable(true);
        setWillNotDraw(false);
        initView(context);
    }
    public BallBounces(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        setFocusable(true);
        setWillNotDraw(false);
        initView(context);
    }
    public BallBounces(Context context, AttributeSet attrs){
        super(context, attrs);
        setFocusable(true);
        setWillNotDraw(false);
        initView(context);
    }
    public void initView(Context context) {
        ball = BitmapFactory.decodeResource(getResources(), R.drawable.football); //Load a ball image.
        bgr = BitmapFactory.decodeResource(getResources(), R.drawable.road1); //Load a background.
        tree = BitmapFactory.decodeResource(getResources(),R.drawable.tree); //Load a background.
        stop = BitmapFactory.decodeResource(getResources(),R.drawable.stop);

        ballW = ball.getWidth();
        ballH = ball.getHeight();

        treeW = tree.getWidth();
        treeH = tree.getHeight();
        tree = Bitmap.createScaledBitmap(tree, treeW/3, treeH/3, true);
        stop = Bitmap.createScaledBitmap(stop, stop.getWidth()/2, stop.getHeight()/2, true);

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

    private void savePathToFile() {
        try {
            Gson gson = new Gson();
            String data = "[";
            boolean isFirst = true;
            for(PathPoint point : pathPointTemps){
                point.x = point.x * 100 / bgrW;
                point.y = point.y * 100 / bgrH;
                if(isFirst){
                    data = data + gson.toJson(point, PathPoint.class);
                    isFirst = false;
                }else{
                    data = data +", "+ gson.toJson(point, PathPoint.class);
                }
            }
            data = data + "]";

            File myFile = new File("/sdcard/road1.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void initializeCoordinates(){
        if(pathPointTemps != null && pathPointTemps.size() > 0) {
            startX = (int) pathPointTemps.get(0).x;
            startY = (int) pathPointTemps.get(0).y;

            stopX = (int) pathPointTemps.get(pathPointTemps.size() - 1).x;
            stopY = (int) pathPointTemps.get(pathPointTemps.size() - 1).y;

            ballX = startX - ballW/2;
            ballY = startY - ballH/2;
        }
    }

    private void buildPathFromFile(Context context) {
        try {
            Gson gson = new Gson();
            String pathData = FileUtils.readResourceToString(context, R.raw.road1_path);
            JSONArray dataArr = new JSONArray(pathData);
            for (int i = 0; i < dataArr.length(); i++) {
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
                        pathPointTemps.add(i, new PathPoint(width * (pathPoints.get(i).x / 100),
                                height * (pathPoints.get(i).y / 100)));

                    }
                    initializeCoordinates();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
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

        initializeCoordinates();
    }

    private int getPathXforTouchY(int touchY) {
        int pathX = -1, i = 0;
        for (PathPoint curr : pathPointTemps) {
            if (touchY >= curr.y) {
                PathPoint prev = pathPointTemps.get(i);
                return (int) (curr.x + (prev.x - curr.x) * (curr.y - touchY) / (touchY - prev.y));
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

        float x = ev.getX();
        float y =  ev.getY();
        float xDiff = 0, yDiff = 0;
        if(started) {
            xDiff = Math.abs(x - stopX);
            yDiff = Math.abs(y - stopY);
            if (xDiff < 30 && yDiff < 30) {
                started = false;
                ballX = stopX - ballW / 2;
                ballY = stopY - ballH / 2;
                Toast.makeText(getContext(), "Finished snooker activity", Toast.LENGTH_LONG).show();
                if (mRemoteSensorManager != null) {
                    mRemoteSensorManager.getUploadDataHelper().setUserActivity(getResources().getString(R.string.exercise_ring_rect), null);
                    mRemoteSensorManager.getUploadMobileDataHelper().setUserActivity(getResources().getString(R.string.exercise_ring_rect), null);
                }
                return true;
            }
        }else{
            xDiff = Math.abs(x - startX);
            yDiff = Math.abs(y - startY);
            if (xDiff < 30 && yDiff < 30) {
                started = true;
                Toast.makeText(getContext(), "Started snooker activity", Toast.LENGTH_LONG).show();
                if (mRemoteSensorManager != null) {
                    mRemoteSensorManager.getUploadDataHelper().setNullUserActivity();
                    mRemoteSensorManager.getUploadMobileDataHelper().setNullUserActivity();
                }
                return true;
            }
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (BUILD_PATH_MODE == 2) {
                    PathPoint newPoint = new PathPoint((int)ev.getX(), (int)ev.getY());
                    pathPointTemps.add(newPoint);
                    Collections.sort(pathPointTemps, new Comparator<PathPoint>() {
                        @Override
                        public int compare(PathPoint lhs, PathPoint rhs) {
                            return (int)(rhs.y - lhs.y);
                        }
                    });
                } else {
                    if(started){
                        ballX = (int) ev.getX() - ballW / 2;
                        ballY = (int) ev.getY() - ballH / 2;

                        ballFingerMove = true;
                    }
                }
                invalidate();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (BUILD_PATH_MODE == 2) {
                    pathY = (int) ev.getY();
                    pathX = getPathXforTouchY(pathY);
                }else {
                    if (started) {
                        pathY = (int) ev.getY();
                        pathX = getPathXforTouchY(pathY);

                        ballX = (int) ev.getX() - ballW / 2;
                        ballY = (int) ev.getY() - ballH / 2;
                        Log.d("pathDebug", "tY:" + ev.getY() + ", tX:" + ev.getX() + ", pathX:" + pathX);
                        if (mRemoteSensorManager != null && started) {
                            String value = "(" + pathX + "," + pathY + ") (" + (int) ev.getX() + "," + (int) ev.getY() + ")";
                            mRemoteSensorManager.getUploadDataHelper().setUserActivity("Snooker", value);
                        }
                    }
                }
                invalidate();
                break;
            }

            case MotionEvent.ACTION_UP:
                if(started) {
                    ballFingerMove = false;
                    dY = 0;
                    invalidate();
                }
                break;
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect fromRect = new Rect(0, 0, bgrW, bgrH);
        Rect toRect = new Rect(0, 0, bgrW, bgrH);

        canvas.drawBitmap(bgr, fromRect, toRect, null);

        if (BUILD_PATH_MODE > 0) {
            Path path = new Path();
            boolean first = true;
            for (int i = 0; i < pathPointTemps.size(); i += 2) {
                PathPoint point = pathPointTemps.get(i);
                if (first) {
                    first = false;
                    path.moveTo(point.x, point.y);
                } else if (i < pathPointTemps.size() - 1) {
                    PathPoint next = pathPointTemps.get(i + 1);
                    path.quadTo(point.x, point.y, next.x, next.y);
                } else {
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
            if(BUILD_PATH_MODE == 1) {
                canvas.drawCircle(pathX, pathY, 50, p);
            }
        } else {
            if (started) {
                canvas.drawBitmap(tree, treeXpos, treeYpos, null);
                if (treeXpos > 180)
                    treeXpos -= 0.4;

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
            }
            angle += 5;
            if (angle > 360)
                angle = 0;


            canvas.save(); //Save the position of the canvas matrix.
            canvas.rotate(angle, ballX + (ballW / 2), ballY + (ballH / 2)); //Rotate the canvas matrix.
            canvas.drawBitmap(ball, ballX, ballY, null); //Draw the ball by applying the canvas rotated matrix.


        }
        canvas.restore(); //Rotate the canvas matrix back to its saved position - only the ball bitmap was rotated not all canvas.
        canvas.drawBitmap(stop, stopX - stop.getWidth()/2, stopY - stop.getHeight()/2, null);

        //Measure frame rate (unit: frames per second).
        now = System.currentTimeMillis();

        canvas.drawText("Deviation from path: (" + pathX + ", " + pathY, 30, 30, fpsPaint);

        framesCount++;
        if (now - framesTimer > 1000) {
            framesTimer = now;
            framesCountAvg = framesCount;
            framesCount = 0;
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

    public void setPathBuildMode(int pathBuildMode) {
        this.BUILD_PATH_MODE = pathBuildMode;
        if(pathBuildMode == 2){
            pathPoints.clear();
            pathPointTemps.clear();
        }else if(pathBuildMode == 1){
            if(pathPointTemps.size() > 0) {
                savePathToFile();
            }
        }
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

