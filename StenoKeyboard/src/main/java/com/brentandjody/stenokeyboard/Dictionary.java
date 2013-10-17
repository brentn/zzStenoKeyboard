package com.brentandjody.stenokeyboard;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by brent on 16/10/13.
 */

public class Dictionary {

    private static final String DICTFILE = "dictionary/dict.json";
    private static TST<String> definitions;

    public Dictionary() {
        definitions = new TST<String>();
        load(DICTFILE);
    }

    public void load(String filename) {
        String line, stroke, translation;
        String[] fields;
        if (filename == null || filename == "")
            throw new IllegalArgumentException("Dictionary filename not provided");
        try {
            BufferedReader file = new BufferedReader(new FileReader(filename));
            while ((line = file.readLine()) != null) {
                fields = line.split("\"");
                if ((fields.length >= 3) && (fields[3].length() > 0)) {
                    stroke = fields[1];
                    translation = fields[3];
                    definitions.put(stroke, translation);
                }
            }
            file.close();
        } catch (IOException e) {
            System.err.println("Dictionary File: "+filename+" could not be found");
        }
    }

    public boolean isLoaded() {
        return (definitions.size() > 0);
    }
}
