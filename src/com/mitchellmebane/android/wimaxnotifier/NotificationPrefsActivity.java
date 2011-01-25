package com.mitchellmebane.android.wimaxnotifier;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class NotificationPrefsActivity extends PreferenceActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        
        this.addPreferencesFromResource( R.xml.preferences );
        PreferenceManager.setDefaultValues( this, R.xml.preferences, false );
        
        return;
    }
}