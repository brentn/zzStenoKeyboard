package com.brentandjody.stenokeyboard.test;

import android.test.AndroidTestRunner;

import junit.framework.TestCase;

/**
 * Created by brent on 17/10/13.
 */
public class TestRunner extends AndroidTestRunner {
    public void main() {
        TestCase test = new TestDictionary("testLoaded");
        test.run();
    }
}
