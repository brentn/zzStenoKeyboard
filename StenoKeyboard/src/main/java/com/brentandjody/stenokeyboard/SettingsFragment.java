package com.brentandjody.stenokeyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final int SELECT_PERSONAL_DICTIONARY = 1;

    private PreferenceCategory dictionaries;
    private int dictionary_count;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dictionary_count = prefs.getInt("pref_key_dictionary_count", 0);
        dictionaries = (PreferenceCategory) findPreference("pref_cat_dictionaries");
        if (BuildConfig.DEBUG) Log.d("Dictionary", Integer.toString(dictionary_count));
        if (dictionary_count > 0) {
            listPersonalDictionaries();
        }
        findPreference("pref_key_add_button").setOnPreferenceClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
//        fillSummary(dictionary_1);
//        fillSummary(dictionary_2);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("pref_key_add_button")) {
            selectDictionary();
            return true;
        }
        return false;
    }

    private void  selectDictionary() {
        // browse for a dictionary
        if (BuildConfig.DEBUG) Log.d("selectDictionary", "");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); //Intent to start File Manager
        intent.setType("application/json");
        startActivityForResult(intent, SELECT_PERSONAL_DICTIONARY);
    }

    private void listPersonalDictionaries() {
        for (int x = 1; x <= dictionary_count; x++) {
            String path = prefs.getString("pref_key_personal_dictionary_"+x, "");
            if (BuildConfig.DEBUG) Log.d("listDictionary", path);
            if (!path.isEmpty()) {
                addDictionaryPreference(x, path);
            }
        }
    }

    private void addDictionaryPreference(int number, String path) {
        EditTextPreference dict = new EditTextPreference(getActivity());
        dict.setText("Personal dictionary "+number);
        dict.setKey("pref_key_personal_dictionary_"+number);
        dict.setSummary(path.substring(path.lastIndexOf("/") + 1));
        dict.setIcon(android.R.drawable.ic_menu_delete);
        dict.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((EditTextPreference) preference).getDialog().dismiss();
                final Preference dictionary = preference;
                AlertDialog.Builder confirm = new AlertDialog.Builder(getActivity());
                confirm.setMessage("Remove this dictionary?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                removeDictionary(dictionary);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                return true;
            }
        });
        dictionaries.addPreference(dict);
    }

    private void addNewDictionary(String path) {
        if (path.isEmpty()) return;
        dictionary_count++;
        prefs.edit().putInt("pref_key_dictionary_count", dictionary_count).commit();
        prefs.edit().putString("pref_key_personal_dictionary_"+dictionary_count, path).commit();
        // add an element
        addDictionaryPreference(dictionary_count, path);
    }

    private void removeDictionary(Preference dictionary) {
        dictionaries.removePreference(dictionary);
        String key = dictionary.getKey();
        String value;
        //renumber higher dictionaries
        int dict_number = Integer.parseInt(key.substring(key.lastIndexOf("_")+1));
        while (dict_number < dictionary_count) {
            value = prefs.getString("pref_key_personal_dictionary_"+(dict_number+1), "");
            prefs.edit().putString("pref_key_personal_dictionary_"+dict_number, value);
        }
        // remove the last entry
        prefs.edit().remove("pref_key_personal_dictionary_"+dictionary_count).commit();
        dictionary_count--;
        prefs.edit().putInt("pref_key_dictionary_count", dictionary_count).commit();
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SELECT_PERSONAL_DICTIONARY: {
                    String filePath = data.getData().getPath();
                    if (filePath.toLowerCase().endsWith(".json")) {
                        addNewDictionary(filePath);
                    } else {
                        Toast.makeText(getActivity(), "Dictionaries must be .json files", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
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