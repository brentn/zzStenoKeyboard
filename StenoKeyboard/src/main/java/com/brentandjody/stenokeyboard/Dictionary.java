package com.brentandjody.stenokeyboard;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by brent on 16/10/13.
 * Lookup words/phrases in steno dictionary
 * Add spacing
 * Decode several "special characters"
 */

public class Dictionary {

    private static final String DICTFILE = "dict.json";
    private static TST<String> definitions = new TST<String>();
    private Deque<String> strokeQ = new LinkedBlockingDeque<String>();
    private List<Definition> candidates = new ArrayList<Definition>();
    private History history = new History();
    private History strokeHistory = new History();
    private Boolean capitalizeNextWord = false;
    private Context context;

    public Dictionary(Context c) {
        context = c;
        if (! isLoaded()) load(DICTFILE);
    }

    public void load(String filename) {
        new loadDictionary().execute(filename);
    }

    public boolean isLoaded() {
        return (definitions.size() > 0);
    }

    public String lookup(String stroke) {
    // basic lookup, no cacheing
    // return null if not found
    // return empty string if ambiguous
        candidates.clear();
        if ((!stroke.isEmpty()) && (((Collection) definitions.prefixMatch(stroke)).size() > 1)) {
            generateCandidates(stroke);
            return "";
        }
        if (stroke.isEmpty()) return "";
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
                translation = translate(subStroke);
                while ((result.length()>0) && (translation.length()>0) && (translation.charAt(0) == '\b')) {
                    translation = translation.substring(1);
                    result = result.substring(0,result.length()-1);
                }
                result += translation;
            }
            return result;
        }

        // handle undo first
        if (stroke.equals("*")) {
            if (strokeQ.isEmpty()) {
                return undoFromHistory();
            } else {
                // pop the stroke queue
                strokeQ.removeLast();
                return "";
            }
        }

        if (strokeQ.isEmpty()) { // if there is no queue, it's easy
            translation = lookup(stroke);
            if (translation == null) {
                updateHistory(stroke, stroke);
                return stroke + " ";
            }
            if (translation.isEmpty()) { // ambiguous
                strokeQ.add(stroke);
                return "";
            }
            // deterministic
            updateHistory(stroke, translation);
            return decode(translation);

        } else { // there is a queue to deal with
            String qString = strokesInQueue();
            translation = (lookup(qString+"/"+stroke));
            if (translation == null) {
                // the full stroke was not found, so let's break it up
                translation = definitions.get(qString);
                if (translation == null) {
                    translation = qString.replace("/"," ");
                }
                updateHistory(strokeQ, translation);
                return decode(translation) + translate(stroke);
            }
            if (translation.isEmpty()) { // ambiguous
                strokeQ.add(stroke);
                return "";
            }
            // deterministic
            strokeQ.add(stroke);
            updateHistory(strokeQ, translation);
            return decode(translation);
        }
    }

    public void addPhraseToHistory(String phrase) {

        updateHistory(strokeQ, phrase);
    }

    public List<Definition> getCandidates() {
        return candidates;
    }

    private void generateCandidates(String stroke) {
        candidates.clear();
        for (String candidateStroke : definitions.prefixMatch(stroke)) {
            Definition candidate = new Definition(candidateStroke, definitions.get(candidateStroke));
            candidates.add(candidate);
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

    private String decode(String input) {
        if (capitalizeNextWord) {
            input = input.substring(0,1).toUpperCase() + input.substring(1);
        }
        capitalizeNextWord = false;
        // short circuit if no special characters
        if (! input.contains("{")) return input+" ";
        //handle glue
        String output = input + " ";
        // start glue
        if (input.substring(0,2).equals("{^")) {
            output = "\b" + output.replace("{^","");
        }
        // end glue
        int pos = input.indexOf("^}");
        if (pos >= 0) {
            output = output.replace("^}","");
            output = output.substring(0, output.length()-1);
        }
        // capitalization
        pos = input.indexOf("{-|}");
        if (pos > 0) {
            capitalizeNextWord = true;
            output = output.replace("{-|}","");
        }
        output = output.replace("{","").replace("}","");
        return output;
    }

    private void updateHistory(Object stroke, String translation) {
        if (stroke instanceof String) {
            strokeHistory.push((String) stroke);
        }
        if (stroke instanceof Deque) {
            for (String s : (Deque<String>) stroke) {
                strokeHistory.push(s);
            }
            ((Deque<String>) stroke).clear();
        }
        history.push(translation);
    }

    private String undoFromHistory() {
        // erase the latest item from history
        Queue<String> historyItem = getHistoryItem();
        if (historyItem == null) return "";
        String translation = historyItem.remove();
        String result = new String(new char[translation.length()+1]).replace("\0", "\b");
        // put all strokes but the last one on the strokeQ
        while (! historyItem.isEmpty()) {
            strokeQ.addLast(historyItem.remove());
        }
        strokeQ.removeLast();
        if (strokeQ.isEmpty()) {
            // get one more item from history, if it is ambiguous put it on the queue,
            // otherwise put it back in the history
            historyItem = getHistoryItem();
            if (historyItem == null) return result;
            translation = historyItem.remove();
            String strokes = historyItem.remove();
            while (! historyItem.isEmpty()) {
                strokes += "/" + historyItem.remove();
            }
            if (translation.equals(lookup(strokes))) {
                history.push(translation);
                for (String s : strokes.split("/")) {
                    strokeHistory.push(s);
                }
            } else {
                result += new String(new char[translation.length()+1]).replace("\0", "\b");
                for (String s : strokes.split("/")) {
                    strokeQ.addLast(s);
                }
            }
        }
        return result;
    }

    private Queue<String> getHistoryItem() {
        if (history.isEmpty()) return null;
        String translation, stroke;
        stroke = "";
        translation = history.pop();
        if (! strokeHistory.isEmpty()) {
            stroke = strokeHistory.pop();
            while ((! strokeHistory.isEmpty()) && (! translation.equals(stroke)) && (! translation.equals(definitions.get(stroke)))) {
                stroke =  strokeHistory.pop() + "/" + stroke;
            }
        }
        Queue<String> result = new LinkedBlockingQueue<String>();
        result.add(translation);
        for (String s : stroke.split("/")) {
            result.add(s);
        }
        return result;
    }

    class History {
        private static final int MAX_SIZE = 40;
        private Deque<String> stack;

        public History() {
            stack = new LinkedBlockingDeque<String>();
        }

        public Boolean isEmpty() {
            return stack.isEmpty();
        }

        public void push(String s) {
            stack.addFirst(s);
            if (stack.size() > MAX_SIZE) {
                stack.removeLast();
            }
        }

        public String pop() {
            if (! stack.isEmpty()) {
                return stack.removeFirst();
            }
            return null;
        }
    }

    private class loadDictionary extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... dictionaries) {
            int count = dictionaries.length;
            String line, stroke, translation;
            String[] fields;
            for (int i = 0; i < count; i++) {
                if (dictionaries[i] == null || dictionaries[i].isEmpty())
                    throw new IllegalArgumentException("Dictionary filename not provided");
                try {
                    AssetManager am = context.getAssets();
                    InputStream filestream = am.open(dictionaries[i]);
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
                    System.err.println("Dictionary File: "+dictionaries[i]+" could not be found");
                }
                publishProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return (long) count;
        }

      protected void onPostExecute(Long result) {
          Toast.makeText(context, "Dictionary Loaded", Toast.LENGTH_LONG).show();
      }
    }
}
