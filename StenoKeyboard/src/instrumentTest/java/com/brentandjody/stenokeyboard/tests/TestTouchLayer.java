package com.brentandjody.stenokeyboard.tests;

import android.app.Activity;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContext;
import android.view.LayoutInflater;
import android.widget.Button;

import com.brentandjody.stenokeyboard.R;
import com.brentandjody.stenokeyboard.StenoKeyboard;
import com.brentandjody.stenokeyboard.TouchLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brent on 23/10/13.
 */
public class TestTouchLayer extends AndroidTestCase {

    private TouchLayer keyboard = new TouchLayer(getContext());

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context context = new MockContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        keyboard = (TouchLayer) inflater.inflate(R.layout.keyboard, null);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCornerKeysOnScreen() {
        List<Button> cornerKeys = getCornerKeys();
        for (Button key : cornerKeys) {
            assertTrue(key.getLeft() > 0);
            assertTrue(key.getLeft()+key.getWidth() < keyboard.getWidth());
            assertTrue(key.getTop() > 0);
            assertTrue(key.getTop() + key.getHeight() < keyboard.getHeight());
        }

    }
    public void testKeysAllThere() {
        String[] keys = {"S-","T-","K-","P-","W-","H-","R-","A-","O-","*","-E","-U","-F","-R","-P","-B","-L","-G","-T","-S","-D","-Z"};
        for (String key : keys) {
            assertNotNull(keyboard.getKey(key));
        }
    }

    private List<Button> getCornerKeys() {
        List<Button> result = new ArrayList<Button>();
        result.add(keyboard.getKey("S-"));
        result.add(keyboard.getKey("T-"));
        result.add(keyboard.getKey("K-"));
        result.add(keyboard.getKey("-D"));
        result.add(keyboard.getKey("-Z"));
        return result;
    }
}
