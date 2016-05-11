package com.github.pocmo.sensordashboard.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.github.pocmo.sensordashboard.activities.BallRectangleActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by neerajpaliwal on 03/05/16.
 */
public class RingRectArenaView extends RelativeLayout {
    private static final String TAG = "RingRectArenaView";

    private RectF mMeasuredRect;
    private RingArea mRing;
    private List<PathSegment> mPaths = new ArrayList<>();
    private int startX, startY;
    private BallRectangleActivity mActivity;

    // CONSTRUCTOR
    public RingRectArenaView(Context context) {
        super(context);
        setFocusable(true);
        setWillNotDraw(false);
        initView(context);
    }

    public RingRectArenaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        initView(context);
    }

    public RingRectArenaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        initView(context);
    }


    private boolean isRingTouched(final int xTouch, final int yTouch){
        if ((mRing.getCenterX() - xTouch) * (mRing.getCenterX() - xTouch) + (mRing.getCenterY() - yTouch) * (mRing.getCenterY() - yTouch) <= mRing.getRadius() * mRing.getRadius()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        int xTouch;
        int yTouch;
        int actionIndex = event.getActionIndex();

        // get touch event coordinates and make transparent circle from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);

                // check if we've touched inside ring
                if(isRingTouched(xTouch, yTouch)){
                    mRing.setCenterX(xTouch);
                    mRing.setCenterY(yTouch);
                }

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "Pointer down");
                xTouch = (int) event.getX(actionIndex);
                yTouch = (int) event.getY(actionIndex);

                // check if we've touched inside some circle
                if(isRingTouched(xTouch, yTouch)){
                    mRing.setCenterX(xTouch);
                    mRing.setCenterY(yTouch);
                }
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerCount = event.getPointerCount();
                //Log.w(TAG, "Move");

                xTouch = (int) event.getX(actionIndex);
                yTouch = (int) event.getY(actionIndex);

                if(isRingTouched(xTouch, yTouch)){
                    mRing.setCenterX(xTouch);
                    mRing.setCenterY(yTouch);
                    for(PathSegment path : mPaths){
                        if(path.contains(yTouch)){
                            mActivity.setDeviationText(xTouch - (int)path.getX(yTouch));
                            //Log.d(TAG, "xTouch - xPath = " + (xTouch - (int)path.getX(yTouch)));
                        }
                    }
                }

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_UP:
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                handled = true;
                break;

            default:
                // do nothing
                break;
        }

        return super.onTouchEvent(event) || handled;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawColor(Color.WHITE);
        Paint p = new Paint();
        // smooths
        p.setAntiAlias(true);
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5);

        p.setColor(Color.BLACK);
        for (PathSegment path : mPaths) {
            path.addArc(canvas, p);
        }


        p.setColor(Color.BLUE);
        canvas.drawRect(mMeasuredRect, p);

        p.setColor(Color.RED);
        canvas.drawCircle(mRing.getCenterX(), mRing.getCenterY(), mRing.getRadius(), p);

    }

    public void initView(Context context) {
        mActivity = (BallRectangleActivity)context;

        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width = getMeasuredWidth() - 100;
                int height = getMeasuredHeight() - 100;
                mMeasuredRect = new RectF(50, 50, width + 50, height + 50);

                startX = (int) mMeasuredRect.right;
                startY = (int) mMeasuredRect.bottom;
                mRing = new RingArea(startX, startY, 50);

                mPaths.add(new PathSegment(getMeasuredWidth() - 50, getMeasuredHeight() - 50,
                        getMeasuredWidth() / 2, getMeasuredHeight() / 2, false));

                mPaths.add(new PathSegment(getMeasuredWidth() / 2, getMeasuredHeight() / 2, 50, 50, true));
            }
        });


    }


}
