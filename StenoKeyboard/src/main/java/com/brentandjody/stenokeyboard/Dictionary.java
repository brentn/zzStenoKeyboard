package com.brentandjody.stenokeyboard;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
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
        String result = "";
        if (stroke.contains("/")) {
            for (String partialStroke : stroke.split("/")) {
                result += translate(partialStroke);
            }
            return result;
        }
        strokeQ.add(stroke);
        String newStroke = "";
        List<String> backup = new ArrayList<String>();
        while (result.isEmpty() && strokeQ.size() > 0) {
            newStroke += "/" + strokeQ.peek();
            backup.add(strokeQ.remove());
            result = lookup(newStroke.substring(1));
            if (result == null) {
                result = newStroke.substring(1).replace("/"," ");
            } else if (result.isEmpty()) {
                result = definitions.get(newStroke.substring(1));
                if (result == null) result = "";
            }
        }
        if (result.isEmpty()) { //deterministic result not found
            for (String backupStroke : backup) {
                strokeQ.add(backupStroke);
            }
        } else {
            result += " ";
        }
        return result;
    }
}
