package me.smartwatches.becare;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by neerajpaliwal on 27/04/16.
 */
public class BallRectangleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    private static class SampleView extends View {

        // CONSTRUCTOR
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

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
            // opacity
            //p.setAlpha(0x80); //

            RectF rectF = new RectF(50, 20, 100, 80);
            canvas.drawRect(rectF, p);
            canvas.drawOval(rectF, p);
            p.setColor(Color.BLACK);
            canvas.drawArc(rectF, 180, 45, true, p);
            canvas.drawArc (rectF, 0, 90, false, p);

            RectF rectF2 = new RectF(100, 100, 500, 500);
            canvas.drawRect(rectF2, p);


        }

    }
}
