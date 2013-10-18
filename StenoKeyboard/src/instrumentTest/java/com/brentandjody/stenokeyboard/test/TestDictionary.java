package com.brentandjody.stenokeyboard.test;

import com.brentandjody.stenokeyboard.Dictionary;

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

    public void testCreated() {
        assertNotNull(dictionary);
    }

    public void testLoaded() {
        assertTrue(dictionary.isLoaded());
    }

    public void testLookup() {
        //word not in dictionary
        assertNull (dictionary.lookup("-TSDZ"));
        //single deterministic words
        assertEquals(dictionary.lookup("AEU"),"a");
        assertEquals(dictionary.lookup("TPEUL"),"fill");
        //single ambiguous words
        assertEquals(dictionary.lookup("TPAUR"),"");
        assertEquals(dictionary.lookup("-UPB") ,"");
        //multi-stroke words
        assertEquals(dictionary.lookup("HR-G/HREU"),"willingly");
        assertEquals(dictionary.lookup("RE/SAOEF"),"receive");
        assertEquals(dictionary.lookup("A*US/RAOUL/KWRAPB"), "Austrailian");
        //stroke-by-stroke, should not combine
        assertEquals(dictionary.lookup("A*US"),"");
        assertEquals(dictionary.lookup("RAEUL"), "rail");
        assertEquals(dictionary.lookup("KWRAPB"), "{^ian}");
    }

}


