package com.brentandjody.stenokeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by brent on 16/10/13.
 */
public class StenoKeyboard extends InputMethodService {

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
                populateCandidates(dictionary.getCandidateStrings());
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                getCurrentInputConnection().commitText(message, 1);
            }
        });
        return keyboardView;
    }

    @Override
    public View onCreateCandidatesView() {
        super.onCreateCandidatesView();
        LayoutInflater layoutInflater = getLayoutInflater();
        candidatesView = (LinearLayout) layoutInflater.inflate(R.layout.candidates, null);
        return candidatesView;
    }

    private void populateCandidates(List<String> items) {
        candidatesView.removeAllViews();
        LayoutInflater layoutInflater = getLayoutInflater();
        for (String item : items) {
            TextView tv = (TextView) layoutInflater.inflate(R.layout.candidate, null);
            tv.setText(item);
            candidatesView.addView(tv);
        }
        setCandidatesViewShown(candidatesView.getChildCount() > 0);
    }

}
