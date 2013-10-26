package com.brentandjody.stenokeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brent on 20/10/13.
 */
public class TouchLayer extends RelativeLayout {

    private static final int TOUCH_RADIUS = 2;
    private static final int TOUCH_OFFSET = -10;
    private static final int MIN_KBD_HEIGHT = 300;
    private static Paint PAINT = new Paint();
    private Canvas canvas;
    private List<Button> keys = new ArrayList<Button>();
    private Button fKey;



    public TouchLayer(Context context) {
        super(context);
    }

    public TouchLayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TouchLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setColor(getResources().getColor(android.R.color.holo_blue_bright));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        keys.clear();
        enumerateKeys(this);
        // all this is to set the height of the keyboard
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screen_height = size.y;
        int keyboard_height = screen_height / 3;
        if (keyboard_height < MIN_KBD_HEIGHT) keyboard_height = MIN_KBD_HEIGHT;
        this.getLayoutParams().height = keyboard_height;
    }

    private void enumerateKeys(View v) {
        //list steno keys IN ORDER
        fKey = (Button) v.findViewById(R.id._F);
        keys.add((Button) v.findViewById(R.id.number_bar));
        keys.add((Button) v.findViewById(R.id.S));
        keys.add((Button) v.findViewById(R.id.T));
        keys.add((Button) v.findViewById(R.id.K));
        keys.add((Button) v.findViewById(R.id.P));
        keys.add((Button) v.findViewById(R.id.W));
        keys.add((Button) v.findViewById(R.id.H));
        keys.add((Button) v.findViewById(R.id.R));
        keys.add((Button) v.findViewById(R.id.A));
        keys.add((Button) v.findViewById(R.id.O));
        keys.add((Button) v.findViewById(R.id.star));
        keys.add((Button) v.findViewById(R.id._E));
        keys.add((Button) v.findViewById(R.id._U));
        keys.add((Button) v.findViewById(R.id._F));
        keys.add((Button) v.findViewById(R.id._R));
        keys.add((Button) v.findViewById(R.id._P));
        keys.add((Button) v.findViewById(R.id._B));
        keys.add((Button) v.findViewById(R.id._L));
        keys.add((Button) v.findViewById(R.id._G));
        keys.add((Button) v.findViewById(R.id._T));
        keys.add((Button) v.findViewById(R.id._S));
        keys.add((Button) v.findViewById(R.id._D));
        keys.add((Button) v.findViewById(R.id._Z));
    }

    private OnStrokeCompleteListener onStrokeCompleteListener;
    public interface OnStrokeCompleteListener {
        public void onStrokeComplete();
    }

    public void setOnStrokeCompleteListener(OnStrokeCompleteListener listener) {
        onStrokeCompleteListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // iterate over all keys, and toggle all those within TOUCH_RADIUS
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            drawTouch(event.getX() + TOUCH_OFFSET, event.getY());
            checkKeyPressed(event, event.getX() + TOUCH_OFFSET, event.getY());
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            for (int i=0; i< event.getHistorySize(); i++){
                drawTouch(event.getHistoricalX(i) + TOUCH_OFFSET, event.getHistoricalY(i));
                checkKeyPressed(event, event.getHistoricalX(i) + TOUCH_OFFSET, event.getHistoricalY(i));
            }
            checkKeyPressed(event, event.getX(), event.getY());
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            onStrokeCompleteListener.onStrokeComplete();
        }
        return true;
    }

    private void drawTouch(float x, float y) {

        canvas.drawCircle(x, y, TOUCH_RADIUS, PAINT);
    }

    private void checkKeyPressed(MotionEvent event, float X, float Y) {
        float top, left, bottom, right;
        int[] location = new int[2];
        this.getLocationOnScreen(location);
        int Xoffset = location[0];
        int Yoffset = location[1];
        float minX = X + Xoffset - TOUCH_RADIUS;
        float maxX = X + Xoffset + TOUCH_RADIUS;
        float minY = Y + Yoffset - TOUCH_RADIUS;
        float maxY = Y + Yoffset + TOUCH_RADIUS;
        for (Button key:keys) {

            key.getLocationOnScreen(location);
            left = location[0];
            top = location[1];
            right = left+key.getWidth();
            bottom = top+key.getHeight();
            if ((maxX > left) && (minX < right)) {
                if ((maxY > top) && (minY < bottom)) {
                    key.setSelected(true);
                }
            }
        }
    }

    public String getStroke() {
        String result = "";
        Boolean addDash = true;
        Boolean rightSide = false;
        for (Button key : keys) {
            if (key.isSelected()) {
                if ("*AOEU".contains(key.getText())) addDash = false;
                if (key == fKey) rightSide=true;
                if (rightSide && addDash) {
                    result += "-";
                    addDash = false;
                }
                    result += key.getText();
                key.setSelected(false);
            }
        }
        return result;
    }


}
