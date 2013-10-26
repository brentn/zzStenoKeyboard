package com.brentandjody.stenokeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by brent on 16/10/13.
 */
public class StenoKeyboard extends InputMethodService {

    private static final int MAX_CANDIDATES = 20;

    private Button sendButton;
    private Dictionary dictionary;
    private TouchLayer keyboardView;
    private LinearLayout candidatesView;

    @Override
    public void onCreate() {
        super.onCreate();
         dictionary = new Dictionary(getApplicationContext());
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
        sendButton = (Button) keyboardView.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stroke = keyboardView.getStroke();
                String message = dictionary.translate(stroke);
                populateCandidates(dictionary.getCandidates());
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                sendText(message);
            }
        });
        return keyboardView;
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
                        dictionary.addPhraseToHistory(phrase);
                        sendText(phrase+" ");
                        candidatesView.removeAllViews();
                        setCandidatesViewShown(false);
                    }
                });
                tv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Toast.makeText(getApplicationContext(), ((TextView) view).getHint(), Toast.LENGTH_LONG).show();
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
            while (message.charAt(i)=='\b')
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

}
