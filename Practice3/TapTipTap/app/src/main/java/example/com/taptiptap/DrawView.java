package example.com.taptiptap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by edward on 15/11/2.
 */

public class DrawView extends View{

        private final int paintColor = Color.BLUE;

        private Paint drawPaint;

        private float pointX, pointY;

        private Path path = new Path();

        TextView mCoordinates;

        public DrawView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setFocusable(true);
            setFocusableInTouchMode(true);
            pointX = pointY = 0;
            mCoordinates = null;
            setupPaint();
        }

        public void setTextView(TextView tv){
            mCoordinates = tv;
        }

        public boolean onTouchEvent(MotionEvent event) {
            pointX = event.getX();
            pointY = event.getY();
            // Checks for the event that occurs
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Starts a new line in the path
                    path.reset();
                    path.moveTo(pointX, pointY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    // Draws line between last point and this point
                    path.lineTo(pointX, pointY);

                    if(mCoordinates!=null) {
                        mCoordinates.setText("PointerID = 0 "+"X :" + pointX + " , " + "Y :" + pointY);
                    }
                    break;
                default:
                    return false;
            }
            postInvalidate(); // Indicate view should be redrawn
            return true; // Indicate we've consumed the touch
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, drawPaint);
        }

        // Setup paint with color and stroke styles
        private void setupPaint() {
            drawPaint = new Paint();
            drawPaint.setColor(paintColor);
            drawPaint.setAntiAlias(true);
            drawPaint.setStrokeWidth(10);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeJoin(Paint.Join.ROUND);
            drawPaint.setStrokeCap(Paint.Cap.ROUND);
        }
}

