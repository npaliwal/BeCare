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
import android.widget.Toast;

import com.github.pocmo.sensordashboard.BecareRemoteSensorManager;
import com.github.pocmo.sensordashboard.R;
import com.github.pocmo.sensordashboard.utils.FileUtils;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

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
    float treeXpos = 1000;
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
    int seq = 0;
    long preTime = 0;
    //Measure frames per second.
    long now;
    int framesCount = 0;
    int framesCountAvg = 0;
    long framesTimer = 0;
    Paint fpsPaint = new Paint();

    private List<PathPoint> pathPoints = new ArrayList<>();
    private List<PathPointInt> pathPointTemps = new ArrayList<>();
    BecareRemoteSensorManager mRemoteSensorManager = null;
    private int BUILD_PATH_MODE=0;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

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
        bgr = BitmapFactory.decodeResource(getResources(), R.drawable.road2); //Load a background.
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
            PathPoint fractionPoint = new PathPoint(0f, 0f);
            for(PathPointInt point : pathPointTemps){
                fractionPoint.x = (float) point.x / bgrW;
                fractionPoint.y = (float) point.y / bgrH;
                if(isFirst){
                    data = data + gson.toJson(fractionPoint, PathPoint.class);
                    isFirst = false;
                }else{
                    data = data +", "+ gson.toJson(fractionPoint, PathPoint.class);
                }
            }
            data = data + "]";

            File myFile = new File("/sdcard/road2.txt");
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
            startX = pathPointTemps.get(0).x / 100;
            startY = pathPointTemps.get(0).y / 100;

            stopX = pathPointTemps.get(pathPointTemps.size() - 1).x / 100;
            stopY = pathPointTemps.get(pathPointTemps.size() - 1).y / 100;

            ballX = startX - ballW/2;
            ballY = startY - ballH/2;
        }
    }

    private void buildPathFromFile(Context context) {
        try {
            Gson gson = new Gson();
            String pathData = FileUtils.readResourceToString(context, R.raw.road2_path);
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
                        pathPointTemps.add(i, new PathPointInt(
                                (int)(width * pathPoints.get(i).x),
                                (int)(height * pathPoints.get(i).y)));

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

    private boolean isPointBetween(int prevY, int currY, int touchY){
        if(currY == touchY || prevY == touchY)
            return true;
        if(prevY - touchY >= 0 && touchY - currY >= 0)
            return true;
        if(prevY - touchY <= 0 && touchY - currY <= 0)
            return true;
        return false;
    }

    private int getPathXforTouchY(int touchX, int touchY) {
        int pathPointIndex = 0;
        PathPointInt prev;
        int i = 0, minimunDistance = 10000000;
        for (PathPointInt curr : pathPointTemps) {
            prev = pathPointTemps.get(i == 0 ? 0 : i - 1);
            if(isPointBetween(prev.y, curr.y, touchY*100)){
                //Log.d("ball debug", "Point found between -> Prev.y=" + prev.y + ", Curr.y" + curr.y + ", touchY=" + touchY);
                if(Math.abs(curr.x - touchX*100) < minimunDistance ){
                //    Log.d("ball debug", "Minimum distance reached -> Curr.x" + curr.x + ", touchX=" + touchX*100);
                    minimunDistance = Math.abs(curr.x - touchX*100);
                    pathPointIndex = i;
                }
            }
            i++;
        }
        return pathPointTemps.get(pathPointIndex).x / 100;
    }


    private int getPathXforTouchY1(int touchY) {
        int pathX = -1, i = 0, touchYHelper = touchY*100;
        for (PathPointInt curr : pathPointTemps) {
            pathX = curr.x;
            if (touchYHelper >= curr.y) {
                if (i > 0) {
                    PathPointInt prev = pathPointTemps.get(i - 1);
                    pathX = curr.x + (prev.x - curr.x) * (curr.y - touchYHelper) / (touchYHelper - prev.y);
                }
                break;
            }
            i++;
        }
        return pathX/100;
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
            if (xDiff < 50 && yDiff < 50) {
                started = true;
                seq = 0;
                preTime = 0;
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
                    PathPointInt newPoint = new PathPointInt((int)(100*ev.getX()), (int)(100*ev.getY()));
                    pathPointTemps.add(newPoint);
                }else if(BUILD_PATH_MODE == 1){
                    pathY = (int) ev.getY();
                    pathX = getPathXforTouchY((int) ev.getX(), (int) ev.getY());
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
                    PathPointInt newPoint = new PathPointInt((int)(100*ev.getX()), (int)(100*ev.getY()));
                    pathPointTemps.add(newPoint);
                }else if(BUILD_PATH_MODE == 1){
                    pathY = (int) ev.getY();
                    pathX = getPathXforTouchY((int) ev.getX(), (int) ev.getY());
                }else {
                    if (started) {
                        pathY = (int) ev.getY();
                        pathX = getPathXforTouchY((int) ev.getX(), (int) ev.getY());

                        ballX = (int) ev.getX() - ballW / 2;
                        ballY = (int) ev.getY() - ballH / 2;
                        //Log.d("pathDebug", "tY:" + ev.getY() + ", tX:" + ev.getX() + ", pathX:" + pathX);
                        if (mRemoteSensorManager != null && started) {
                            String value = "(" + pathX + "," + pathY + ") (" + (int) ev.getX() + "," + (int) ev.getY() + ")";
                            long now = System.currentTimeMillis();
                            if (preTime == 0)
                                preTime = now;
                            long dur = now - preTime;
                            preTime = now;
                            String readTime = timeFormat.format(now);
                            Hashtable dictionary = new Hashtable();
                            dictionary.put("value",value );
                            dictionary.put("activityname", "snooker");
                            dictionary.put("seq", seq);
                            dictionary.put("dur (ms)", dur);
                            dictionary.put("time", readTime);
                            mRemoteSensorManager.uploadActivityDataAsyn(dictionary);
                            seq++;
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
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            boolean first = true;
            for (int i = 0; i < pathPointTemps.size(); i += 2) {
                PathPointInt point = pathPointTemps.get(i);
                canvas.drawCircle(point.x/100, point.y/100, 5, p);
                if (first) {
                    first = false;
                    path.moveTo(point.x/100, point.y/100);
                } else if (i < pathPointTemps.size() - 1) {
                    PathPointInt next = pathPointTemps.get(i + 1);
                    path.quadTo(point.x/100, point.y/100, next.x/100, next.y/100);
                } else {
                    path.lineTo(point.x/100, point.y/100);
                }
            }
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
                if (treeXpos > 300)
                    treeXpos -= 0.6;

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

class PathPointInt{
    int x;
    int y;

    public PathPointInt(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

