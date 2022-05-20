package com.example.chatappfirebase.utilities;

import android.content.Context;
import android.content.SharedPreferences;

//  Class to handle shared preferences
public class PreferenceManager {

    private final SharedPreferences sharedPreferences;

    //  construstor
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        //  null is the default value if string is not found
        return sharedPreferences.getString(key, null);
    }

    //  Clear the shared preferences from the system
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
