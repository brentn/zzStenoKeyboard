package com.brentandjody.stenokeyboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by brent on 16/10/13.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
//        Preference filePicker = (Preference) findPreference("pref_key_personal_dictionary");
//        filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Intent intent = new Intent(......); //Intent to start openIntents File Manager
//                startActivityForResult(intent, requestMode);
//                return true;
//            }
//        });
    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        //get the new value from Intent data
//        String newValue = ....;
//        SharedPreferences preferences = ......;
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("filePicker", newValue);
//        editor.commit();
//    }
}
