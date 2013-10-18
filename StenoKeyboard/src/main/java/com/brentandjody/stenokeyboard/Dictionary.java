package com.brentandjody.stenokeyboard;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by brent on 16/10/13.
 */

public class Dictionary {

    private static final String DICTFILE = "dict.json";
    private static TST<String> definitions;

    public Dictionary() {
        definitions = new TST<String>();
        load(DICTFILE);
    }

    public void load(String filename) {
        String line, stroke, translation;
        String[] fields;
        if (filename == null || filename.equals(""))
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
    //basic lookup, no cacheing
    //return null if not found
    //return empty string if ambiguous
        return definitions.get(stroke);
    }
}
