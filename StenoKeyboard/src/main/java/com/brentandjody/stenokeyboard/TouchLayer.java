package com.brentandjody.stenokeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class TouchLayer extends LinearLayout {

    private static final Hashtable<String,String> NUMBER_KEYS = new Hashtable<String, String>() {{
        put("S-", "1-");
        put("T-", "2-");
        put("P-", "3-");
        put("H-", "4-");
        put("A-", "5-");
        put("O-", "0-");
        put("-F", "-6");
        put("-P", "-7");
        put("-L", "-8");
        put("-T", "-9");
    }};
    private static final int NUM_PATHS = 2;
    private static final int MIN_KBD_HEIGHT = 400;
    private static Paint PAINT = new Paint();
    private List<Button> keys = new ArrayList<Button>();
    private Path[] paths = new Path[NUM_PATHS];
    private Button sendKey;
    private Context context;
    private Boolean expandVowelKeys = false;
    private Boolean autoSend = true;



    public TouchLayer(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public TouchLayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    public TouchLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        PAINT.setColor(getResources().getColor(android.R.color.holo_blue_bright));
        PAINT.setAntiAlias(true);
        PAINT.setDither(true);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeJoin(Paint.Join.ROUND);
        PAINT.setStrokeCap(Paint.Cap.ROUND);
        PAINT.setStrokeWidth(12);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        expandVowelKeys = sharedPrefs.getBoolean("pref_key_expand_vowel_keys", true);
//        autoSend = ! sharedPrefs.getBoolean("pref_key_send_button", false);
        if (autoSend) {
            ((LinearLayout) this.findViewById(R.id.send_button).getParent()).setVisibility(GONE);
        } else {
            ((LinearLayout) this.findViewById(R.id.send_button).getParent()).setVisibility(VISIBLE);
            sendKey = (Button) this.findViewById(R.id.send_button);
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.findViewById(R.id.A).getLayoutParams();
        if (expandVowelKeys) {
            layoutParams.setMargins(2,-25,2,0);
            this.findViewById(R.id.A).setLayoutParams(layoutParams);
            this.findViewById(R.id._U).setLayoutParams(layoutParams);
            layoutParams.setMargins(2,-40,2,0);
            this.findViewById(R.id.O).setLayoutParams(layoutParams);
            this.findViewById(R.id._E).setLayoutParams(layoutParams);
            layoutParams.setMargins(2,0,2,25);
            this.findViewById(R.id.number_bar).setLayoutParams(layoutParams);
            this.findViewById(R.id.star).setLayoutParams(layoutParams);
        } else {
            layoutParams.setMargins(2,0,2,0);
            this.findViewById(R.id.A).setLayoutParams(layoutParams);
            this.findViewById(R.id.O).setLayoutParams(layoutParams);
            this.findViewById(R.id._E).setLayoutParams(layoutParams);
            this.findViewById(R.id._U).setLayoutParams(layoutParams);
            layoutParams.setMargins(2,0,2,0);
            this.findViewById(R.id.number_bar).setLayoutParams(layoutParams);
            this.findViewById(R.id.star).setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i=0; i<NUM_PATHS; i++) {
            canvas.drawPath(paths[i], PAINT);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // find letter keys
        keys.clear();
        enumerateKeys(this);
        // initialize paths
        for (int i=0; i<NUM_PATHS; i++) {
            paths[i] = new Path();
        }
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
        // drag to select, touch to toggle
        // multi-touch for both of above
        // implement onstrokecompletelistener, in case we use it later
        float x, y;
        int i;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                i = event.getActionIndex();
                x = event.getX(i);
                y = event.getY(i);
                paths[i].reset();
                paths[i].moveTo(x, y);
                toggleKeyAt(x, y);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                selectKeys(event);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                i = event.getActionIndex();
                if (autoSend) {
                    if (i == 0) {
                        onStrokeCompleteListener.onStrokeComplete();
                    }
                } else {
                    List<Button> peek = peekKeys();
                    // a tap of the * button alone will autosend delete stroke
                    if (i == 0 && peek.size() == 1 && peek.get(0).getHint().equals("*")) {
                        onStrokeCompleteListener.onStrokeComplete();
                    }
                    sendKey.setSelected(false);
                }
                paths[i].reset();
                break;
            }
        }
        return true;
    }

    private void toggleKeyAt(float x, float y) {
        Point pointer = new Point();
        Point offset = getScreenOffset(this);
        pointer.set((int)x+offset.x, (int)y+offset.y);
        if ((!autoSend) && pointerOnKey(pointer, sendKey)) {
            sendKey.setSelected(true);
            onStrokeCompleteListener.onStrokeComplete();
            return;
        }
        for (Button key : keys) {
            if (pointerOnKey(pointer, key)) {
                key.setSelected(! key.isSelected());
                return;
            }
        }
    }

    private void selectKeys(MotionEvent e) {
        Point pointer = new Point();
        Point offset = getScreenOffset(this);
        int i = e.getActionIndex();
        for (int n=0; n<e.getHistorySize(); n++) {
            pointer.set((int)e.getHistoricalX(i, n)+offset.x, (int)e.getHistoricalY(i, n)+offset.y);
            paths[i].lineTo(e.getHistoricalX(i, n), e.getHistoricalY(i, n));
            if (! autoSend) {
                if ((!sendKey.isSelected()) && pointerOnKey(pointer, sendKey)) {
                    sendKey.setSelected(true);
                    onStrokeCompleteListener.onStrokeComplete();
                }
            }
            for (Button key : keys) {
                if (pointerOnKey(pointer, key)) {
                    key.setSelected(true);
                }
            }
        }
    }

    private Point getScreenOffset(View v) {
        Point result = new Point();
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        result.set(location[0], location[1]);
        return result;
    }

    private Boolean pointerOnKey(Point p, View key) {
        Point bottomRight = new Point();
        Point topLeft = getScreenOffset(key);
        bottomRight.set(topLeft.x+key.getWidth(),
                topLeft.y+key.getHeight());
        if ((p.x < topLeft.x) || (p.x > bottomRight.x) || (p.y < topLeft.y) || (p.y > bottomRight.y)) return false;
        return true;
    }

    private List<Button> peekKeys() {
        List<Button> result = new ArrayList<Button>();
        for (Button key : keys) {
            if (key.isSelected()) {
                result.add(key);
            }
        }
        return result;
    }

    public String getStroke() {
        String result = "";
        List<String> chord = new ArrayList<String>();
        for (Button key : keys) {
            if (key.isSelected()) {
                chord.add(key.getHint().toString());
                key.setSelected(false);
            }
        }
        if (chord.contains("#")) {
            chord = convertNumbers(chord);
        }
        result = constructStroke(chord);
        return result;
    }

    private List<String> convertNumbers(List<String> chord) {
        List<String> result = new ArrayList<String>();
        Boolean numeral = false;
        for (String thisKey : chord) {
            if (NUMBER_KEYS.containsKey(thisKey)) {
                result.add(NUMBER_KEYS.get(thisKey));
                numeral = true;
            } else {
                result.add(thisKey);
            }
        }
        if (numeral) {
            result.remove("#");
        }
        return result;

    }

    private String constructStroke(List<String> chord) {
        String result = "";
        String suffix = "";
        if (! Collections.disjoint(chord, Arrays.asList("A-", "O-", "5-", "0-", "-E", "-U", "*"))) {
            for (String key : chord) {
                result += key.replace("-","");
            }
        } else {
            for (String key : chord) {
                if (key.equals("#") || key.charAt(key.length()-1) == '-') {
                    result += key.replace("-", "");
                }
                if (key.charAt(0) == '-') {
                    suffix += key.replace("-", "");
                }
            }
            if (! suffix.isEmpty()) {
                result += "-"+suffix;
            }
        }
        return result;
    }

}
