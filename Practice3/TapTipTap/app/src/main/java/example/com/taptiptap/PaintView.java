package example.com.taptiptap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class PaintView extends View {
    // Hold data for active touch pointer IDs
    private SparseArray<TouchHistory> mTouches;
    Paint mPaint;
    public float[] mX = {0};
    public float[] mY = {0};
    int mPointerId;
    TextView mTVCoordinates;

    public PaintView(Context context,AttributeSet attributeSet){
        super(context,attributeSet);

        /** Initializing the variables */
        mPaint = new Paint();
        mTVCoordinates = null;
        mX = new float[10];
        mY = new float[10];
        // SparseArray for touch events, indexed by touch id
        mTouches = new SparseArray<TouchHistory>(10);
        initialisePaint();
    }

    static final class TouchHistory {

        // number of historical points to store
        public static final int HISTORY_COUNT = 20;

        public float x;
        public float y;
        public String label = null;

        // current position in history array
        public int historyIndex = 0;
        public int historyCount = 0;

        // arrray of pointer position history
        public PointF[] history = new PointF[HISTORY_COUNT];

        private static final int MAX_POOL_SIZE = 10;
        private static final Pools.SimplePool<TouchHistory> sPool =
                new Pools.SimplePool<TouchHistory>(MAX_POOL_SIZE);

        public static TouchHistory obtain(float x, float y) {
            TouchHistory data = sPool.acquire();
            if (data == null) {
                data = new TouchHistory();
            }

            data.setTouch(x, y);

            return data;
        }

        public TouchHistory() {

            // initialise history array
            for (int i = 0; i < HISTORY_COUNT; i++) {
                history[i] = new PointF();
            }
        }

        public void setTouch(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void recycle() {
            this.historyIndex = 0;
            this.historyCount = 0;
            sPool.release(this);
        }

        public void addHistory(float x, float y) {
            PointF p = history[historyIndex];
            p.x = x;
            p.y = y;

            historyIndex = (historyIndex + 1) % history.length;

            if (historyCount < HISTORY_COUNT) {
                historyCount++;
            }
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mTouches.size(); i++) {

            int id = mTouches.keyAt(i);
            TouchHistory data = mTouches.valueAt(i);

            drawCircle(canvas, id, data);
        }
    }

    public void setTextView(TextView tv){
        // Reference to TextView Object
        mTVCoordinates = tv;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            // When user touches the screen
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                int mPointerIndex = event.getActionIndex();
                int mPointerId = event.getPointerId(mPointerIndex);

                TouchHistory data = TouchHistory.obtain(event.getX(mPointerIndex), event.getY(mPointerIndex));
                data.label = "id" + mPointerId;
                // Getting X coordinate
                mX[mPointerIndex] = event.getX(mPointerIndex);
                // Getting Y Coordinate
                mY[mPointerIndex] = event.getY(mPointerIndex);
                // Setting the coordinates on TextView
                if (mTVCoordinates != null) {
                    mTVCoordinates.setText("Pointer ID: 0 , X :" + mX[0] + " , " + "Y :" + mY[0] + "\n" +
                                    "Pointer ID: 1 , X :" + mX[1] + " , " + "Y :" + mY[1] + "\n" +
                                    "Pointer ID: 2 , X :" + mX[2] + " , " + "Y :" + mY[2] + "\n" +
                                    "Pointer ID: 3 , X :" + mX[3] + " , " + "Y :" + mY[3] + "\n" +
                                    "Pointer ID: 4 , X :" + mX[4] + " , " + "Y :" + mY[4]
                    );}
                    mTouches.put(mPointerId, data);

                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP: {

                    int mPointerIndex = event.getActionIndex();
                    int mPointerId = event.getPointerId(mPointerIndex);

                    TouchHistory data = mTouches.get(mPointerId);
                    mTouches.remove(mPointerId);
                    data.recycle();

                    break;
                }

                case MotionEvent.ACTION_MOVE: {

                    for (int index = 0; index < event.getPointerCount(); index++) {

                        int id = event.getPointerId(index);

                        TouchHistory data = mTouches.get(id);

                        data.addHistory(data.x, data.y);
                        data.setTouch(event.getX(index), event.getY(index));

                        mX[index] = event.getX(index);
                        mY[index] = event.getY(index);

                        if(mTVCoordinates!=null) {
                            mTVCoordinates.setText(
                                    "PointerID:0 , X :" + mX[0] + " , " + "Y :" + mY[0]+"\n"+
                                            "PointerID:1 , X :" + mX[1] + " , " + "Y :" + mY[1]+"\n"+
                                            "PointerID:2 , X :" + mX[2] + " , " + "Y :" + mY[2]+"\n"+
                                            "PointerID:3 , X :" + mX[3] + " , " + "Y :" + mY[3]+"\n"+
                                            "PointerID:4 , X :" + mX[4] + " , " + "Y :" + mY[4]);
                        }

                    }

                    break;
                }
            }

            this.postInvalidate();

            return true;
    }
      /*
     * Below are only helper methods and variables required for drawing.
     */

    // radius of active touch circle in dp
    private static final float CIRCLE_RADIUS_DP = 75f;
    // radius of historical circle in dp
    private static final float CIRCLE_HISTORICAL_RADIUS_DP = 7f;

    // calculated radiuses in px
    private float mCircleRadius;
    private float mCircleHistoricalRadius;

    private Paint mCirclePaint = new Paint();
    private Paint mTextPaint = new Paint();

    private static final int BACKGROUND_ACTIVE = Color.WHITE;

    // inactive border
    private static final float INACTIVE_BORDER_DP = 15f;
    private static final int INACTIVE_BORDER_COLOR = 0xFFffd060;
    private Paint mBorderPaint = new Paint();
    private float mBorderWidth;

    public final int[] COLORS = {
            0xFF33B5E5, 0xFFAA66CC, 0xFF99CC00, 0xFFFFBB33, 0xFFFF4444,
            0xFF0099CC, 0xFF9933CC, 0xFF669900, 0xFFFF8800, 0xFFCC0000
    };

    private void initialisePaint() {
        float density = getResources().getDisplayMetrics().density;
        mCircleRadius = CIRCLE_RADIUS_DP * density;
        mCircleHistoricalRadius = CIRCLE_HISTORICAL_RADIUS_DP * density;
        mTextPaint.setTextSize(27f);
        mTextPaint.setColor(Color.BLACK);
        mBorderWidth = INACTIVE_BORDER_DP * density;
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setColor(INACTIVE_BORDER_COLOR);
        mBorderPaint.setStyle(Paint.Style.STROKE);

    }

    protected void drawCircle(Canvas canvas, int id, TouchHistory data) {
        // select the color based on the id
        int color = COLORS[id % COLORS.length];
        mCirclePaint.setColor(color);
        /*
         * Draw the circle, size scaled to its pressure. Pressure is clamped to
         * 1.0 max to ensure proper drawing. (Reported pressure values can
         * exceed 1.0, depending on the calibration of the touch screen).
         */
        float radius = 50;
        canvas.drawCircle(data.x, (data.y) - (radius / 2f), radius,
                mCirclePaint);
        // draw its label next to the main circle
        canvas.drawText(data.label, data.x + radius, data.y
                - radius, mTextPaint);
    }

}
