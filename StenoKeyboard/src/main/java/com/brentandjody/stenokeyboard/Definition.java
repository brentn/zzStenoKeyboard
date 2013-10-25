package com.brentandjody.stenokeyboard;

/**
 * Created by brent on 24/10/13.
 */
public class Definition {
    private String mStroke;
    private String mTranslation;
    public Definition(String stroke, String translation) {
        set(stroke, translation);
    }

    public void set(String stroke, String translation) {
        mStroke = stroke;
        mTranslation = translation;
    }

    public String getStroke() {
        return mStroke;
    }

    public String getTranslation() {
        return mTranslation;
    }
}
