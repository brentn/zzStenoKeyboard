package com.brentandjody.stenokeyboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment {

    private EditTextPreference dictionary_1, dictionary_2;
    private CheckBoxPreference default_dictionary;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        default_dictionary = (CheckBoxPreference) findPreference("pref_key_use_embedded_dictionary");
        dictionary_1 = (EditTextPreference) findPreference("pref_key_personal_dictionary_1");
        dictionary_2 = (EditTextPreference) findPreference("pref_key_personal_dictionary_2");
        editor = getPreferenceScreen().getEditor();
        // enable default dictionary if no personal dictionaries are defined
        if ((dictionary_1.getText()+dictionary_2.getText()).isEmpty()) {
            default_dictionary.setChecked(true);
            default_dictionary.setEnabled(false);
        }
        dictionary_1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                dictionary_1.getDialog().dismiss();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT); //Intent to start openIntents File Manager
                intent.setType("application/json");
                startActivityForResult(intent, 1);
                return true;
            }
        });
        dictionary_2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                dictionary_2.getDialog().dismiss();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT); //Intent to start openIntents File Manager
                intent.setType("application/json");
                startActivityForResult(intent, 2);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fillSummary(dictionary_1);
        fillSummary(dictionary_2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    String FilePath = data.getData().getPath();
                    if (! FilePath.endsWith(".json")) {
                        FilePath = "";
                        Toast.makeText(getActivity(), "Dictionaries must be .json files", Toast.LENGTH_SHORT).show();
                    }
                    editor.putString("pref_key_personal_dictionary_1", FilePath);
                    editor.commit();
                    dictionary_1.setText(FilePath);
                    fillSummary(dictionary_1);
                    if ((FilePath+dictionary_2.getText()).isEmpty()) {
                        default_dictionary.setChecked(true);
                        default_dictionary.setEnabled(false);
                    } else {
                        default_dictionary.setEnabled(true);
                    }
                }
                break;
            case 2:
                if(resultCode == Activity.RESULT_OK){
                    String FilePath = data.getData().getPath();
                    if (! FilePath.endsWith(".json")) {
                        FilePath = "";
                        Toast.makeText(getActivity(), "Dictionaries must be .json files", Toast.LENGTH_SHORT).show();
                    }
                    editor.putString("pref_key_personal_dictionary_2", FilePath);
                    editor.commit();
                    dictionary_2.setText(FilePath);
                    fillSummary(dictionary_2);
                    if ((FilePath+dictionary_1.getText()).isEmpty()) {
                        default_dictionary.setChecked(true);
                        default_dictionary.setEnabled(false);
                    } else {
                        default_dictionary.setEnabled(true);
                    }
                }
                break;
        }
    }

    private void fillSummary(EditTextPreference pref) {
        String filename = pref.getText();
        if (filename.contains("/")) {
            int pos = filename.lastIndexOf("/");
            filename = filename.substring(pos+1);
        }
        pref.setSummary(filename);
    }
}