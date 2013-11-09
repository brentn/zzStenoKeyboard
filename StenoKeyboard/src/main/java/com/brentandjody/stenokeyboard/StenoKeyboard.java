package com.brentandjody.stenokeyboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class StenoKeyboard extends InputMethodService {

    private static final int MAX_CANDIDATES = 20;

    private Dictionary dictionary;
    private TouchLayer keyboardView;
    private LinearLayout candidatesView;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        loadDictionaries();
    }


//    @Override
//    public boolean onEvaluateFullscreenMode() {
//        return true;
//    }


    @Override
    public View onCreateInputView() {
        super.onCreateInputView();
        LayoutInflater layoutInflater = getLayoutInflater();
        keyboardView = (TouchLayer) layoutInflater.inflate(R.layout.keyboard, null);
        keyboardView.setOnStrokeCompleteListener(new TouchLayer.OnStrokeCompleteListener() {
            @Override
            public void onStrokeComplete() {
                if (dictionary.isLoaded()) {
                    String stroke = keyboardView.getStroke();
                    String message = dictionary.translate(stroke);
                    populateCandidates(dictionary.getCandidates());
                    if (BuildConfig.DEBUG) Log.d("onCreateInputView", "sent stroke:"+stroke+"   translation:"+message);
                    sendText(message);
                } else {
                    Toast.makeText(StenoKeyboard.this, "Dictionary not yet loaded", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Button settingsButton = (Button) keyboardView.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        if (! dictionary.isLoaded()) {
            keyboardView.lock();
        }
        return keyboardView;
    }

    @Override
    public void onUnbindInput() {
        super.onUnbindInput();
        sendText(dictionary.flush());
        dictionary.purge();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        sendText(dictionary.flush());
        dictionary.purge();
    }

    @Override
    public View onCreateCandidatesView() {
        super.onCreateCandidatesView();
        LayoutInflater layoutInflater = getLayoutInflater();
        HorizontalScrollView scrollView = (HorizontalScrollView) layoutInflater.inflate(R.layout.candidates, null);
        candidatesView = (LinearLayout) scrollView.findViewById(R.id.candidatesView);
        return scrollView;
    }

    private void populateCandidates(List<Definition> candidates) {
        candidatesView.removeAllViews();
        LayoutInflater layoutInflater = getLayoutInflater();
        int count = 0;
        for (Definition candidate : candidates) {
            if (count < MAX_CANDIDATES) {
                TextView tv = (TextView) layoutInflater.inflate(R.layout.candidate, null);
                tv.setText(candidate.getTranslation());
                tv.setHint(candidate.getStroke());
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String phrase = ((TextView) view).getText().toString();
                        String stroke = ((TextView) view).getHint().toString();
                        dictionary.updateHistory(stroke, phrase);
                        sendText(phrase);
                        candidatesView.removeAllViews();
                        setCandidatesViewShown(false);
                    }
                });
                tv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Toast.makeText(StenoKeyboard.this, ((TextView) view).getHint(), Toast.LENGTH_LONG).show();
                        return false;
                    }
                });
                candidatesView.addView(tv);
            }
            count ++;
        }
        setCandidatesViewShown(candidatesView.getChildCount() > 0);
    }

    private void sendText(String message) {
        // deals with backspaces
        if (message.contains("\b")) {
            // deal with any backspaces at the start first
            int i = 0;
            while (i < message.length() && message.charAt(i)=='\b')
                i++;
            if (i > 0) {
                getCurrentInputConnection().deleteSurroundingText(i,0);
                message = message.substring(i);
            }
            // split the text on the first backspace, and recurse
            if (message.contains("\b")) {
                i = message.indexOf('\b');
                sendText(message.substring(0,i));
                sendText(message.substring(i));
            }
        } else {
            getCurrentInputConnection().commitText(message, 1);
        }
    }

    private void loadDictionaries() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String dict;
        List<String> customDictionaries = new ArrayList<String>();
        if (prefs.getBoolean("pref_key_use_embedded_dictionary",true)) {
            customDictionaries.add(Dictionary.getDictFile());
        }
        dict = prefs.getString("pref_key_personal_dictionary_1", "");
        if (! dict.isEmpty()) customDictionaries.add(dict);
        dict = prefs.getString("pref_key_personal_dictionary_2", "");
        if (! dict.isEmpty()) customDictionaries.add(dict);
        dictionary = new Dictionary(StenoKeyboard.this, customDictionaries.toArray(new String[0]));
        dictionary.setOnDictionaryLoadedListener(new Dictionary.OnDictionaryLoadedListener() {
            @Override
            public void onDictionaryLoaded() {
            if (keyboardView != null)
                keyboardView.unlock();
            }
        });

    }
}
