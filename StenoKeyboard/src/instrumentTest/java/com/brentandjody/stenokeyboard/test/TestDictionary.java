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
        assertEquals(dictionary.lookup("HR-GT"),"altogether");
        assertEquals(dictionary.lookup("TPHROUPBS"),"flounce");
        //single ambiguous words
        assertEquals(dictionary.lookup("PRAOE"),"");
        assertEquals(dictionary.lookup("TPAUR") ,"");
        //multi-stroke words
        assertEquals(dictionary.lookup("HR-G/HREU"),"willingly");
        assertEquals(dictionary.lookup("RE/SAOEF"),"receive");
        assertEquals(dictionary.lookup("A*UT/EPBT/EUBG"), "authentic");
        //stroke-by-stroke, should not combine
        assertEquals(dictionary.lookup("A*UT"),"");
        assertEquals(dictionary.lookup("EPBT"), "");
        assertEquals(dictionary.lookup("EUBG"), "");
    }

    public void testTranslate() {
        // word not in dictionary
        assertEquals(dictionary.translate("-TSDZ"), "-TSDZ ");
        // multi-stroke not in dictionary
        assertEquals(dictionary.translate("-TSDZ/-TSDZ"), "-TSDZ -TSDZ ");
        // multi-stroke, 1st stroke not in dictionary, unambiguous 2nd stroke
        assertEquals(dictionary.translate("-TSDZ/RAFLD"), "-TSDZ raffled ");
        // unambiguous single-stroke word
        assertEquals(dictionary.translate("RAODZ"), "roads ");
        // unambiguous multi-stroke word
        assertEquals(dictionary.translate("PWRAPL/-BLS"), "brambles ");
        // unambiguous multi-stroke word, stroke-by-stroke
        assertEquals(dictionary.translate("PWRAPL"), "");
        assertEquals(dictionary.translate("-BLS"), "brambles ");
        // same word, with non-dictionary stroke in the middle
        assertEquals(dictionary.translate("PWRAPL"), "");
        assertEquals(dictionary.translate("-TSDZ"), "PWRAPL -TSDZ ");
        assertEquals(dictionary.translate("-BLS"), "-BLS ");
        // ambiguous single-stroke word that becomes 2 words
        assertEquals(dictionary.translate("TPAULT"), "");
        assertEquals(dictionary.translate("TPEUPBD"), "fault find ");
        // ambiguous single-stroke word that becomes a single word
        assertEquals(dictionary.translate("TPEUPB"), "");
        assertEquals(dictionary.translate("HRAPBD"), "Finland ");
        // ambiguous word, with ambiguous second stroke that splits 1/2 on third
//        assertEquals(dictionary.translate("EUPB"), "");
//        assertEquals(dictionary.translate("KA"), "");
//        assertEquals(dictionary.translate("HRORBG"), "in caloric ");
        // ambiguous word, with ambiguous second stroke, that splits 2/1 on third
        assertEquals(dictionary.translate("TPAEUR"), "");
        assertEquals(dictionary.translate("KWREU"), "");
        assertEquals(dictionary.translate("TAEULS"), "fairy tails ");
    }

    public void testUndo() {
        // undo with no prior stroke
        assertEquals(dictionary.translate("*"), "");
        // undo with illegal stroke
        assertEquals(dictionary.translate("-TSDZ/*"), "");
        // undo with valid stroke
        assertEquals(dictionary.translate("RAODZ/*"), "");
        assertEquals(dictionary.translate("RAODZ"), "roads ");
        assertEquals(dictionary.translate("*"), "\b\b\b\b\b\b");
        // undo one of two strokes
        assertEquals(dictionary.translate("RAODZ/TOGT/*"), "roads ");
        // ensure correct number of backspaces are sent for multiple words
        assertEquals(dictionary.translate("RAODZ"), "roads ");
        assertEquals(dictionary.translate("TOGT"), "together ");
        assertEquals(dictionary.translate("*"), "\b\b\b\b\b\b\b\b\b");
        assertEquals(dictionary.translate("*"), "\b\b\b\b\b\b");
        // fix an invald misstroke
        assertEquals(dictionary.translate("TPEUPB/-BLS/*/HRAPBD"), "Finland ");
        // fix a valid but wrong second stroke
        assertEquals(dictionary.translate("TPEUPB/HRAPBD/*/EURB"), "finish ");
        // undo three of four strokes
        assertEquals(dictionary.translate("RAODZ/RAODZ/RAODZ/RAODZ/*/*/*"), "roads ");
    }

}


