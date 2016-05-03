package me.smartwatches.becare.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.animation.PathInterpolator;

/**
 * Created by neerajpaliwal on 03/05/16.
 */
public class PathSegment {
    //private PathInterpolator mInterpolator;
    //private Path             mHelperPath;

    private float yMin, yMax, xMin, xMax;
    private boolean upperConvex;

    public PathSegment(float xRightBottom, float yRightBottom, float xTopLeft, float yTopLeft, boolean upperConvex){
        super();

        yMax = yRightBottom;
        yMin = yTopLeft;
        xMin = xTopLeft;
        xMax = xRightBottom;
        this.upperConvex = upperConvex;

        //mHelperPath = new Path();
        //if(upperConvex) {
        //    mHelperPath.addArc(-1f, 0f, 1f, 2f, 270, 90);
        //}else {
        //    mHelperPath.addArc(0f, -1f, 2f, 1f, 180, -90);
        //}
        //mInterpolator = new PathInterpolator(mHelperPath);
    }

    //public float getXFromInterpolator(float y){
    //    float yHelper = (y - yMin) / (yMax - yMin);
    //    float xHelper = mInterpolator.getInterpolation(yHelper);
    //    return xMin + xHelper * (xMax - xMin);
    //}

    public boolean contains(float y){
        if(y >= yMin && y < yMax)
            return true;
        return false;
    }

    public void addArc(Canvas canvas, Paint p){
        float heightBy2 = yMax - yMin;
        float widthBy2  = xMax - xMin;

        if(upperConvex) {
            canvas.drawArc(xMin - widthBy2, yMin, xMax, yMax + heightBy2, 270, 90, false, p);
        }else {
            canvas.drawArc(xMin, yMin - heightBy2, xMax + widthBy2, yMax, 90, 90, false, p);
        }
    }


    public float getX(float y){
        float yHelper = (y - yMin) / (yMax - yMin);
        float xHelper = 0f;
        if(upperConvex){
            yHelper = 1 - yHelper;
            xHelper = (float) Math.sqrt(1 - yHelper*yHelper);
        }else{
            xHelper = (float) (1 - Math.sqrt(1 - yHelper*yHelper));
        }
        return xMin + xHelper * (xMax - xMin);
    }
}
