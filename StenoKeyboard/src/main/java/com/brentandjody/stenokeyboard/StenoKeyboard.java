package com.brentandjody.stenokeyboard;

import android.app.ProgressDialog;
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

    private Button sendButton;
    private Dictionary dictionary;
    private TouchLayer keyboardView;
    private LinearLayout candidatesView;

    @Override
    public void onCreate() {
        super.onCreate();
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Loading Dictionary...");
        progress.show();
        dictionary = new Dictionary(getApplicationContext());
        progress.dismiss();
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
        HorizontalScrollView scrollView = (HorizontalScrollView) layoutInflater.inflate(R.layout.candidates, null);
        candidatesView = (LinearLayout) scrollView.findViewById(R.id.candidatesView);
        return scrollView;
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
