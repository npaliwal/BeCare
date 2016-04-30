package me.smartwatches.becare.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import me.smartwatches.becare.ui.RingArea;

/**
 * Created by neerajpaliwal on 27/04/16.
 */
public class BallRectangleActivity extends AppCompatActivity {
    private static final String TAG = "BallRectangle";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new CanvasView(this));
    }

    private static class CanvasView extends View {
        private RectF mMeasuredRect, mWrapRect;
        private RingArea mRing;
        private int startX, startY;

        // CONSTRUCTOR
        public CanvasView(Context context) {
            super(context);
            setFocusable(true);

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

                    for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                        xTouch = (int) event.getX(actionIndex);
                        yTouch = (int) event.getY(actionIndex);

                        if(isRingTouched(xTouch, yTouch)){
                            mRing.setCenterX(xTouch);
                            mRing.setCenterY(yTouch);
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
            //canvas.drawRect(mWrapRect, p);
            canvas.drawArc (mWrapRect, 90, 90, false, p);

            canvas.drawRect(mMeasuredRect, p);

            p.setColor(Color.RED);
            canvas.drawCircle(mRing.getCenterX(), mRing.getCenterY(), mRing.getRadius(), p);

        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int width = getMeasuredWidth()-100;
            int height = getMeasuredHeight()-100;
            mMeasuredRect = new RectF(50, 50, width + 50, height + 50);
            mWrapRect = new RectF(50, 50-height, 2*width + 50, height + 50);

            startX = (int)mMeasuredRect.right;
            startY = (int)mMeasuredRect.bottom;
            mRing = new RingArea(startX, startY, 50);
        }

    }
}
