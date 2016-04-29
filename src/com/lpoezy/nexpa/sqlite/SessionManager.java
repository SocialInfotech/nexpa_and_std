package com.lpoezy.nexpa.sqlite;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "Nexpa";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_SUPERUSER = "isSuperuser";
    private static final String KEY_IS_SUPERUSER_BROADCAST = "isSuperuserBroadcast";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public void setUsername(String username) {

        editor.putString(KEY_USERNAME, username);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public void setSuperuser(boolean isSuperuser) {

        editor.putBoolean(KEY_IS_SUPERUSER, isSuperuser);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public void setSuperuserBroadcast(boolean isSuperuser) {

        editor.putBoolean(KEY_IS_SUPERUSER_BROADCAST, isSuperuser);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }


    public  void clearSession() {
        this._context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }


    public String getUsername(){
        return pref.getString(KEY_USERNAME, "");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public boolean isSuperuser(){
        return pref.getBoolean(KEY_IS_SUPERUSER, false);
    }

    public boolean isSuperuserBroadcast(){
        return pref.getBoolean(KEY_IS_SUPERUSER_BROADCAST, false);
    }
}
