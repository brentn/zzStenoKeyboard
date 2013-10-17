package com.brentandjody.stenokeyboard.test;

import com.brentandjody.stenokeyboard.Dictionary;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Created by brent on 17/10/13.
 */
public class TestDictionary extends TestCase {
    protected Dictionary dictionary;

    protected void setUp() {
        dictionary = new Dictionary();
    }

    protected void tearDown() {
    }

    public void testLoaded() {
        assertTrue(dictionary.isLoaded());
    }

}


