package com.mitchellmebane.android.wimaxnotifier;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mitchellmebane.android.wimaxnotifier.lib.PDU;
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
        Log.i( "WiMAX Message Notifier", "Resetting WiMAX LED..." );
        try {
            WiMAXLED.turnOff();
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
    
    public void showVoicemailTest( View v ) {
        showMessageInfo( SmsMessage.createFromPdu( PDU.SAMPLE_VOICEMAIL ) );
    }
    
    public void showTextTest( View v ) {
        showMessageInfo( SmsMessage.createFromPdu( PDU.SAMPLE_TEXT ) );
    }
    
    public void showMessageInfo( SmsMessage sms ) {
        StringBuilder msg = new StringBuilder();
        
        msg.append( "getDisplayMessageBody: " + sms.getDisplayMessageBody() + "\n" );
        msg.append( "getDisplayOriginatingAddress: " + sms.getDisplayOriginatingAddress() + "\n" );
        msg.append( "getEmailBody: " + sms.getEmailBody() + "\n" );
        msg.append( "getEmailFrom: " + sms.getEmailFrom() + "\n" );
        msg.append( "getMessageBody: " + sms.getMessageBody() + "\n" );
        msg.append( "getMessageClass: " + sms.getMessageClass() + "\n" );
        msg.append( "getOriginatingAddress: " + sms.getOriginatingAddress() + "\n" );
        msg.append( "getProtocolIdentifier: " + sms.getProtocolIdentifier() + "\n" );
        msg.append( "getPseudoSubject: " + sms.getPseudoSubject() + "\n" );
        msg.append( "getServiceCenterAddress: " + sms.getServiceCenterAddress() + "\n" );
        msg.append( "getStatus: " + sms.getStatus() + "\n" );
        msg.append( "isCphsMwiMessage: " + sms.isCphsMwiMessage() + "\n" );
        msg.append( "isEmail: " + sms.isEmail() + "\n" );
        msg.append( "isMWIClearMessage: " + sms.isMWIClearMessage() + "\n" );
        msg.append( "isMWISetMessage: " + sms.isMWISetMessage() + "\n" );
        msg.append( "isMwiDontStore: " + sms.isMwiDontStore() + "\n" );
        msg.append( "isReplace: " + sms.isReplace() + "\n" );
        msg.append( "isReplyPathPresent: " + sms.isReplyPathPresent() + "\n" );
        msg.append( "isStatusReportMessage: " + sms.isStatusReportMessage() + "\n" );
        
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder( this ).create();
        alertDialog.setTitle( "SMS Info" );
        alertDialog.setMessage( msg.toString() );
        alertDialog.show();
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