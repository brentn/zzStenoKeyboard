package com.brentandjody.stenokeyboard;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by brent on 16/10/13.
 */

public class Dictionary {

    private static final String DICTFILE = "dict.json";
    private static TST<String> definitions = new TST<String>();
    private Queue<String> strokeQ = new LinkedBlockingQueue<String>();

    public Dictionary() {
        if (! isLoaded()) load(DICTFILE);
    }

    public void load(String filename) {
        String line, stroke, translation;
        String[] fields;
        if (filename == null || filename.isEmpty())
            throw new IllegalArgumentException("Dictionary filename not provided");
        try {
            AssetManager am = SKApplication.getAppContext().getAssets();
            InputStream filestream = am.open(filename);
            InputStreamReader reader = new InputStreamReader(filestream);
            BufferedReader lines = new BufferedReader(reader);
            while ((line = lines.readLine()) != null) {
                fields = line.split("\"");
                if ((fields.length >= 3) && (fields[3].length() > 0)) {
                    stroke = fields[1];
                    translation = fields[3];
                    definitions.put(stroke, translation);
                }
            }
            lines.close();
            reader.close();
            filestream.close();
        } catch (IOException e) {
            System.err.println("Dictionary File: "+filename+" could not be found");
        }
    }

    public boolean isLoaded() {
        return (definitions.size() > 0);
    }

    public String lookup(String stroke) {
    // basic lookup, no cacheing
    // return null if not found
    // return empty string if ambiguous
        if (((Collection) definitions.prefixMatch(stroke)).size() > 1) return "";
        return definitions.get(stroke);
    }

    public String translate(String stroke) {
        // lookup and disambiguate multiple strokes (using cache)
        // interpret special keystrokes

        String translation;
        String result = "";
        // handle multi-stroke input recursively
        if (stroke.contains("/")) {
            for (String subStroke : stroke.split("/")) {
                result += translate(subStroke);
            }
            return result;
        }


        if (strokeQ.isEmpty()) { // if there is no queue, it's easy
            translation = lookup(stroke);
            if (translation == null) return stroke + " ";
            if (translation.isEmpty()) { // ambiguous
                strokeQ.add(stroke);
                return "";
            }
            // deterministic
            return translation + " ";

        } else { // there is a queue to deal with
            String qString = strokesInQueue();
            translation = (lookup(qString+"/"+stroke));
            if (translation == null) {
                // the full stroke was not found, so let's break it up
                strokeQ.clear();
                translation = definitions.get(qString);
                if (translation == null) {
                    translation = qString.replace("/"," ");
                }
                return translation + " " + translate(stroke);
            }
            if (translation.isEmpty()) { // ambiguous
                strokeQ.add(stroke);
                return "";
            }
            // deterministic
            strokeQ.clear();
            return translation + " ";
        }
    }

    private String strokesInQueue() {
        if (strokeQ.isEmpty()) return "";
        Iterator<String> it = strokeQ.iterator();
        String result = it.next();
        while (it.hasNext()) {
            result += "/" + it.next();
        }
        return result;
    }
}
