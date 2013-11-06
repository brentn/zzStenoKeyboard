package com.brentandjody.stenokeyboard.tests;

import android.test.AndroidTestCase;

import com.brentandjody.stenokeyboard.Definition;
import com.brentandjody.stenokeyboard.Dictionary;


import java.util.List;


public class TestDictionary extends AndroidTestCase {
    protected Dictionary dictionary;

    protected void setUp() {
        dictionary = new Dictionary(getContext());
        while (! dictionary.isLoaded()) {
            System.out.print(".");
        }
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
        assertNull(dictionary.lookup("-TSDZ"));
        //single deterministic words
        assertEquals(dictionary.lookup("HR-GT"), "altogether");
        assertEquals(dictionary.lookup("TPHROUPBS"), "flounce");
        //single ambiguous words
        assertEquals(dictionary.lookup("PRAOE"), "");
        assertEquals(dictionary.lookup("TPAUR"), "");
        //multi-stroke words
        assertEquals(dictionary.lookup("HR-G/HREU"), "willingly");
        assertEquals(dictionary.lookup("RE/SAOEF"), "receive");
        assertEquals(dictionary.lookup("A*UT/EPBT/EUBG"), "authentic");
        //stroke-by-stroke, should not combine
        assertEquals(dictionary.lookup("A*UT"), "");
        assertEquals(dictionary.lookup("EPBT"), "");
        assertEquals(dictionary.lookup("EUBG"), "");
        //no stroke exists, but one that starts with this does (SPEUPB/A*UF)
        assertEquals(dictionary.lookup("SPEUPB/A"), null);
    }

    public void testPurge() {
        assertEquals(dictionary.translate("PAOEUPB/A*PL"),"pineapple ");
        assertEquals(dictionary.translate("PAOEUPB"),"");
        assertEquals(dictionary.getCandidates().size(), 11);
        dictionary.purge();
        assertEquals(dictionary.getCandidates().size(), 0);
        assertEquals(dictionary.translate("A*PL"),"");
        assertEquals(dictionary.translate("SAUS"),"applesauce ");
    }

    public void testFlush() {
        assertEquals(dictionary.translate("PAOEUPB"),"");
        assertEquals(dictionary.getCandidates().size(), 11);
        assertEquals(dictionary.flush(),"pine ");
        assertEquals(dictionary.getCandidates().size(), 0);
        assertEquals(dictionary.translate("KOEPBS"),"cones ");
    }

    public void testTranslate() {
        dictionary.purge();
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
        //phrase
        assertEquals(dictionary.translate("SPEUPB/A/ROUPBD/RAODZ/-PLT"), "spin around roads . ");
        assertEquals(dictionary.translate("KPA*/WHA/WR/U/THEU/-G/KW-PL"), "\bWhat were you thinking ? ");
    }

    public void testUndo() {
        dictionary.purge();
        // undo with no prior stroke
        assertEquals(dictionary.translate("*"), "\b");
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
        assertEquals(dictionary.translate("TPEUPB/HRAPBD/*/EURB/-G"), "finishing ");
        // undo three of four strokes
        assertEquals(dictionary.translate("RAODZ/RAODZ/RAODZ/RAODZ/*/*/*"), "roads ");
        // test undo more than there is in history
        assertEquals(dictionary.translate("RAOD/*/*"), "\b");
        assertEquals(dictionary.translate("RAODZ/*/*"), "\b");
        assertEquals(dictionary.translate("HOU/-R/-U/KW-PL/*/*/*/*/*/*/*"), "/b/b/b");
    }

    public void testGlue() {
        // start glue
        assertEquals(dictionary.translate("*ERLT/ES/S-LS"), "earthes is also ");
        // end glue
        assertEquals(dictionary.translate("*ERBGS/A/KROS"), "extraacross ");
        // both glue
        assertEquals(dictionary.translate("TOGT/TK-LS/TOGT"), "togethertogether ");
        // sticky glue
        assertEquals(dictionary.translate("A*/PW*/KR*/S-P"), "abc ");
    }

    public void testCapitalize() {
        assertEquals(dictionary.translate("KPA*/TOEPBS"), "\bTones ");
        assertEquals(dictionary.translate("KPA"),"");
        assertEquals(dictionary.translate("TOEPBS"), " Tones ");
    }

    public void testCandidates() {
        assertEquals(dictionary.translate("A/ABT"),"");
        List<Definition> candidates = dictionary.getCandidates();
        assertEquals(candidates.size(), 2);
        assertEquals(candidates.get(0).getTranslation(),"attribute ");
        assertEquals(candidates.get(1).getTranslation(),"attributing ");
    }

}


