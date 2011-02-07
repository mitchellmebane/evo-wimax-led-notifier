package com.mitchellmebane.android.wimaxnotifier;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mitchellmebane.android.wimaxnotifier.lib.StackTrace;
import com.mitchellmebane.android.wimaxnotifier.lib.WiMAXLED;

public class HomeActivity extends Activity {
    private static final int DIALOG_LEGEND = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.home_activity );
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        if( !WiMAXLED.exists() ) {
            Toast.makeText( this, "Your phone/ROM doesn't support the WiMAX LED!", Toast.LENGTH_LONG ).show();
        }
    }
    
    public void resetLED( View v ) {
        Log.i( "WiMAX Message Notifier", "Resetting WiMAX LED and stopping service..." );
        try {
            // turn off the LED
            WiMAXLED.turnOff();
            
            // kill the service if it is running
            Intent serviceIntent = new Intent( this.getApplicationContext(), WiMAXNotifierMessageService.class );
            stopService( serviceIntent );
            
            Toast.makeText( this, "WiMAX LED Reset", Toast.LENGTH_SHORT ).show();
        }
        catch( Exception e ) {
            Log.e( "WiMAX Message Notifier", "Unhandled Exception while resetting WiMAX LED", e );
            StackTrace.ToFile( e );
        }
    }
    
    public void queryLEDState( View v ) {
        String stateName = "";
        int state = 0;
        
        try {
            WiMAXLED.Mode mode = WiMAXLED.Mode.fromCode( WiMAXLED.getState() );
            stateName = mode.name();
        }
        catch( IllegalArgumentException e ) {
            stateName = "Unknown";
        }
        catch( IOException e1 ) {
            stateName = "Unknown";
        }
        
        Toast.makeText( this, "State: " + stateName + " (" + state + ")", Toast.LENGTH_LONG ).show();
    }
    
    public void showLegend( View v ) {
        this.showDialog( DIALOG_LEGEND );
    }
    
    public void showAboutDialog( View v ) {
        Intent showAboutIntent = new Intent( this, AboutActivity.class );
        this.startActivity( showAboutIntent );
    }
    
    public void testNotification( View v ) {
        
    }
    
    public void showNotificationPreferences( View v ) {
        Intent showPrefsIntent = new Intent( this, NotificationPrefsActivity.class );
        this.startActivity( showPrefsIntent );
    }
    
    @Override
    protected Dialog onCreateDialog( int id ) {
        switch( id ) {
        case DIALOG_LEGEND:
            return createLegendDialog();
        default:
            throw new IllegalArgumentException( "Unknown id " + id + " in onCreateDialog" );
        }
    }
    
    private Dialog createLegendDialog() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService( "layout_inflater" );
        View view = inflater.inflate( R.layout.legend_dialog, (ViewGroup) this.findViewById( R.id.tlLegend ) );
        
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setView( view );
        builder.setTitle( R.string.prefLegend );
        builder.setPositiveButton( R.string.btnClose, new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int which ) {
                dialog.dismiss();
                return;
            }
        });
        return builder.create();
    }
}