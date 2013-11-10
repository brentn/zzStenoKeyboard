package com.brentandjody.stenokeyboard;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;


//Load and manage steno dictionary
//Lookup words/phrases, decode special characters
//auto-insert spaces
//Maintain history & implement undo


public class Dictionary {

    public static final String NEWLINE = System.getProperty("line.separator");

    private static final String DICTFILE = "dict.json";
    private static final int HISTORY_LIMIT = 60;
    private static final int MAX_CANDIDATES = 20;
    private static TST<String> definitions = new TST<String>();
    private static Boolean loaded = false;
    private Deque<String> strokeQ = new LinkedBlockingDeque<String>();
    private List<Definition> candidates = new ArrayList<Definition>();
    private LimitedSizeQueue history = new LimitedSizeQueue();
    private LimitedSizeQueue strokeHistory = new LimitedSizeQueue();
    private Boolean capitalizeNextWord = false;
    private Boolean hasGlue = false;
    private Context context;

    public static String getDictFile() {
        return DICTFILE;
    }

    public Dictionary(Context c) {
        context = c;
        if (! loaded) loadDictionaries(DICTFILE);
    }

    public Dictionary(Context c, Boolean loadDefault) {
        context = c;
        if (loadDefault && !loaded) loadDictionaries(DICTFILE);
    }

    private OnDictionaryResetListener onDictionaryResetListener;
    public interface OnDictionaryResetListener {
        public void onDictionaryReset();
    }
    public void setOnDictionaryResetListener(OnDictionaryResetListener listener) {
        onDictionaryResetListener = listener;
    }

    private OnDictionaryLoadedListener onDictionaryLoadedListener;
    public interface OnDictionaryLoadedListener {
        public void onDictionaryLoaded();
    }
    public void setOnDictionaryLoadedListener(OnDictionaryLoadedListener listener) {
        onDictionaryLoadedListener = listener;
    }


    public void loadDictionaries(String... filenames) {
        new loadDictionary().execute(filenames);
    }

    public void unload() {
        onDictionaryResetListener.onDictionaryReset();
        definitions = new TST<String>();
        loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String lookup(String stroke) {
    // basic lookup, no cacheing
    // return null if not found
    // return empty string if ambiguous
        if (! loaded) {
            Toast.makeText(context, "Dictionary not yet loaded...", Toast.LENGTH_SHORT).show();
            return "";
        }
        if (stroke == null || stroke.isEmpty()) return null;
        if (((Collection) definitions.prefixMatch(stroke+"/")).size() > 0) {
            return ""; //ambiguous
        }
        return definitions.get(stroke); //if exists
    }

    public String translate(String strokes) {
    // break up multi-stroke entries and translate one piece at a time
    // remove any backspaces in the middle of the translation
    // but leave any at the start
        String result = "";
        if (BuildConfig.DEBUG) Log.d("translate", "strokes: "+strokes);

        for (String stroke : strokes.split("/")) {
            if (BuildConfig.DEBUG) Log.d("translate", "stroke: "+stroke);
            result = result+stroke_translate(stroke);
            result = eliminate_backspaces(result);
            if (BuildConfig.DEBUG) Log.d("translate", "result: "+result+"   length: "+result.length()+"   queue: "+strokeQ.size());
        }
        return result;
    }

    private String eliminate_backspaces(String input) {
    // iterate over string, removing characters before \b (except \b itself)
        if (BuildConfig.DEBUG) Log.d("eliminate_backspaces", "input: "+input+"   length: "+input.length());
        if (! input.contains("\b")) return input; //there are no backspaces
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '\b') {
                if ((result.length() > 0) && (result.charAt(result.length()-1) != '\b')) {
                    result.deleteCharAt(result.length()-1);
                } else {
                    result.append(c);
                }
            } else {
                result.append(c);
            }
        }
        if (BuildConfig.DEBUG) Log.d("eliminate_backspaces", "result: " + result.toString()+"   length: "+result.toString().length());
        return result.toString();
    }

    private String stroke_translate(String stroke) {
    // translate and decode a single stroke
        if (BuildConfig.DEBUG) Log.d("stroke_translate", "stroke: "+stroke+"   queue: "+strokeQ.size());
        String translation, result;
        candidates.clear();
        if (stroke.equals("*")) {
            result = undo_last_stroke();
            if (BuildConfig.DEBUG) Log.d("stroke_translate", "result 1: "+result+"   queue: "+strokeQ.size());
            return result;
        }
        // if there is no queue...
        if (strokeQ.isEmpty()) {
            translation = lookup(stroke);
            if (is_deterministic(translation)) { // deterministic stroke
                result = decode(translation);
                updateHistory(stroke, result);
            } else {
                if (is_ambiguous(translation)) { //ambiguous stroke
                    strokeQ.add(stroke);
                    generateCandidates(stroke);
                    result = "";
                } else { //not a valid stroke
                    result = stroke+" ";
                    updateHistory(stroke, result);
                }
            }
        } else { // there is a queue to deal with
            translation = (lookup(strokesInQueue()+"/"+stroke));
            if (is_deterministic(translation)) { // deterministic
                strokeQ.add(stroke);
                result = decode(translation);
                updateHistory(strokeQ, result);
            } else {
                if (is_ambiguous(translation)) { // ambiguous
                    strokeQ.add(stroke);
                    generateCandidates(strokesInQueue());
                    result = "";
                } else {// full stroke was not found
                    result = "";
                    if (! strokesInQueue().isEmpty())
                        result = definitions.get(strokesInQueue());
                    if (result == null) {
                        result = strokesInQueue()+" ";
                    } else {
                        result = decode(result);
                    }
                    updateHistory(strokeQ, result);
                    result += stroke_translate(stroke);
                    if (BuildConfig.DEBUG) Log.d("stroke_translate", "result 2: "+result+"   queue: "+strokeQ.size());
                    return result;
                }
            }
        }
        if (BuildConfig.DEBUG) Log.d("stroke_translate", "result: "+result+"   queue: "+strokeQ.size());
        return result;
    }

    private Boolean is_deterministic(String s) {
        // not null and not empty
        return (! (s == null || s.isEmpty()));
    }

    private Boolean is_ambiguous(String s) {
        // not null and is empty
        return ((s != null) && (s.isEmpty()));
    }

    private String undo_last_stroke() {
        if (BuildConfig.DEBUG) Log.d("undo_last_stroke", "stokeQ: "+strokeQ.size()+"   history:"+history.size());
        String result = "";
        if (! strokeQ.isEmpty()) {
            strokeQ.removeLast();
        } else {
           result = undoFromHistory();
        }
        generateCandidates(strokesInQueue());
        if (BuildConfig.DEBUG) Log.d("undo_last_stroke", "result:   strokeQ: "+strokeQ.size()+"   history:"+history.size());
        return result;
    }

    public void clearQ() {
        strokeQ.clear();
    }

    public void purge() {
        // erase queue and candidates and history
        if (BuildConfig.DEBUG) Log.d("purgeHistory", "BEFORE history:"+history.size()+"   strokeHistory:"+strokeHistory.size());
        strokeQ.clear();
        candidates.clear();
        history.clear();
        strokeHistory.clear();
        if (BuildConfig.DEBUG) Log.d("purgeHistory", "AFTER history:"+history.size()+"   strokeHistory:"+strokeHistory.size());
    }

    public String flush() {
        //empty queue (and candidates) by returning words for what is already there.
        String result = "";
        if (! strokeQ.isEmpty()) {
            String strokes = strokesInQueue();
            if (lookup(strokes)!=null && !strokes.isEmpty()) {
                result = decode(definitions.get(strokes));
            }
            if (result.isEmpty()) {
                result = strokes + " ";
            }
            updateHistory(strokeQ, result);
            strokeQ.clear();
        }
        candidates.clear();
        return result;
    }

    public List<Definition> getCandidates() {
        return candidates;
    }

    private void generateCandidates(String stroke) {
        if (stroke==null || stroke.isEmpty()) return;
        candidates.clear();
        Boolean cnw = capitalizeNextWord;
        Boolean hgl = hasGlue;
        if ((((Collection) definitions.prefixMatch(stroke+"/")).size() > 0)) {
            Definition candidate;
            // add the translation for the base stroke
            String translation = definitions.get(stroke);
            // add this stroke (if it's a word)
            if (translation != null) {
                candidate = new Definition(stroke,(decode(translation)));
                candidates.add(candidate);
            }
            // add translations that begin with this stroke
            for (String candidateStroke : definitions.prefixMatch(stroke+"/")) {
                if (!candidateStroke.isEmpty()) {
                    candidate = new Definition(candidateStroke, decode(definitions.get(candidateStroke)));
                    candidates.add(candidate);
                }
                if (candidates.size() >= MAX_CANDIDATES) {
                    break;
                }
            }
        }
        capitalizeNextWord=cnw;
        hasGlue = hgl;
    }

    public String strokesInQueue() {
        if (strokeQ.isEmpty()) return "";
        Iterator<String> it = strokeQ.iterator();
        String result = it.next();
        while (it.hasNext()) {
            result += "/" + it.next();
        }
        return result;
    }

    public String strokesInHistory() {
        if (strokeHistory.isEmpty()) return "";
        Iterator<String> it = strokeHistory.iterator();
        String result = it.next();
        while (it.hasNext()) {
            result += "/" + it.next();
        }
        return result;
    }

    private String decode(String input) {
        // decode special dictionary codes
        // add space if not overridden
        if (input == null || input.isEmpty()) return "";
        input = input.trim();
        if (capitalizeNextWord) {
            input = input.substring(0,1).toUpperCase() + input.substring(1);
            capitalizeNextWord = false;
        }
        // short circuit if no special characters
        if (! input.contains("{")) return input+" ";
        if (BuildConfig.DEBUG) Log.d("decode", "input:"+input+"   size: "+input.length());
        //handle glue
        String output = input+" ";
        if (output.contains("{&")) {
            if (hasGlue) { // if the last stroke also had glue
                output = "\b"+output;
            }
            hasGlue = true;
            output = output.replace("{&","");
        } else {
            hasGlue = false;
        }
        // start glue
        if (output.substring(0,2).equals("{^")) {
            output = "\b" + output.replace("{^","");
        }
        // end glue
        int pos = output.indexOf("^}");
        if (pos >= 0) {
            output = output.replace("^}","");
            output = output.replaceAll("\\s+$", ""); //trim space at end
        }
        // capitalization
        pos = output.indexOf("{-|}");
        if (pos >= 0) {
            capitalizeNextWord = true;
            output = output.replace("{-|}","").replaceAll("\\s+$", ""); //trim space at end
        }
        if (BuildConfig.DEBUG) Log.d("decode", "output:"+output+"   size: "+output.length());
        output = output.replace("{#Return}", "\n");
        output = output.replace("{#BackSpace}", "\b");
        pos = output.length()-2; //2nd character from end
        if ((output.indexOf(NEWLINE+" ") == pos ) || (output.indexOf("\b ") == pos)) {
            output = output.replaceAll(" +$", "");
        }
        output = output.replace("{","").replace("}","");
        if (BuildConfig.DEBUG) Log.d("decode", "output:"+output+"   size: "+output.length());
        return output;
    }

    public void updateHistory(Object stroke, String translation) {
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
        if (historyItem == null) {
            purge();
            return "\b"; // return backspace if there is no history
        }
        String translation = historyItem.remove();
        String result = getBackspaces(translation.length());
        if (BuildConfig.DEBUG) Log.d("undoFromHistory", "translation: " +translation+"   result length: "+result.length());
        // put all strokes but the last one on the strokeQ
        while (! historyItem.isEmpty()) {
            strokeQ.addLast(historyItem.remove());
        }
        strokeQ.removeLast();
        if (strokeQ.isEmpty()) {
            // get one more item from history,
            historyItem = getHistoryItem();
            if (historyItem == null) return result;
            translation = historyItem.remove();
            result = getBackspaces(translation.length()) + result;
            String strokes = historyItem.remove();
            while (! historyItem.isEmpty()) {
                strokes += "/" + historyItem.remove();
            }
            if (BuildConfig.DEBUG) Log.d("undoFromHistory", "Second Stroke: translation: " +translation+"   strokes: "+strokes);
            result += translate(strokes);  //replay strokes
//            //if it is deterministic, put it back in history, otherwise put it on the queue
//            if (translation.equals(decode(lookup(strokes)))) {
//                if (BuildConfig.DEBUG) Log.d("undoFromHistory", "deterministic");
//                history.push(translation);
//                for (String s : strokes.split("/")) {
//                    strokeHistory.push(s);
//                }
//            } else {
//                if (BuildConfig.DEBUG) Log.d("undoFromHistory", "ambiguous");
//                result += new String(new char[translation.length()]).replace("\0", "\b");
//                for (String s : strokes.split("/")) {
//                    strokeQ.addLast(s);
//                }
//            }
        }
        if (BuildConfig.DEBUG) Log.d("undoFromHistory", "stokeQ: " +strokesInQueue());
        if (BuildConfig.DEBUG) Log.d("undoFromHistory", "history:..." + strokesInHistory());
        return result;
    }

    private String getBackspaces(int size) {
        return new String(new char[size]).replace("\0", "\b");
    }

    private Queue<String> getHistoryItem() {
        if (history.isEmpty()) return null;
        if (BuildConfig.DEBUG) Log.d("getHistoryItem", "history: "+history.size()+"   strokeHistory: "+strokeHistory.size());
        String translation, stroke;
        stroke = "";
        translation = history.pop();
        if (! strokeHistory.isEmpty()) {
            stroke = strokeHistory.pop();
            while ((! stroke.isEmpty())
                    && (! strokeHistory.isEmpty())
                    && (! translation.equals(stroke+" "))
                    && (! translation.equals(decode(definitions.get(stroke))))) {
                stroke =  strokeHistory.pop() + "/" + stroke;
            }
        }
        Queue<String> result = new LinkedBlockingQueue<String>();
        result.add(translation);
        if (BuildConfig.DEBUG) Log.d("getHistoryItem", "translation: "+translation+"   stroke: " +stroke);
        for (String s : stroke.split("/")) {
            result.add(s);
        }
        return result;
    }

    class LimitedSizeQueue {
        private static final int MAX_SIZE = HISTORY_LIMIT;
        private Deque<String> stack;

        public LimitedSizeQueue() {
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

        public int size() {
            return stack.size();
        }

        public Iterator<String> iterator() {
            return stack.iterator();
        }

        public void clear() {
            stack.clear();
        }
    }

    private class loadDictionary extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... dictionaries) {
            if (BuildConfig.DEBUG) Log.d("LoadDictionary", "Starting load...  Size:"+definitions.size());
            int count = dictionaries.length;
            String line, stroke, translation;
            String[] fields;
            for (int i = 0; i < count; i++) {
                if (BuildConfig.DEBUG) Log.d("LoadDictionary", "Loading file:"+dictionaries[i]);
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
                    if (BuildConfig.DEBUG) Log.e("LoadDictionary", e.toString());
                    System.err.println("Dictionary File: "+dictionaries[i]+" could not be found");
                }
                publishProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return (long) count;
        }

      protected void onPostExecute(Long result) {
          loaded = true;
          onDictionaryLoadedListener.onDictionaryLoaded();
          if (BuildConfig.DEBUG) Log.d("LoadDictionary", "Load complete.  Size:"+definitions.size());
      }
    }
}
