package com.mitchellmebane.android.wimaxnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.mitchellmebane.android.wimaxnotifier.WiMAXNotifierMessageService.MessageType;
import com.mitchellmebane.android.wimaxnotifier.exceptions.InvalidBroadcastException;
import com.mitchellmebane.android.wimaxnotifier.lib.StackTrace;

public class MessageBroadcastReceiver extends BroadcastReceiver {
     // private static final String TAG = BroadcastReceiver.class.getSimpleName();
    private static final String TAG = "WiMAX Message Notifier - Receiver";
    
    @Override
    public void onReceive( Context context, Intent intent ) {
        try {
            if( intent.getAction().equals( "android.provider.Telephony.WAP_PUSH_RECEIVED" ) || 
                    intent.getAction().equals( "android.provider.Telephony.SMS_RECEIVED" ) ) {
                showMessageNotification( context, intent );
            }
            else if( intent.getAction().equals( ConnectivityManager.CONNECTIVITY_ACTION ) ) {
                showConnectivityChangedNotification( context, intent );
            }
        }
        catch( Exception e ) {
            Log.e( "WiMAX Message Notifier", "Unhandled Exception", e );
            StackTrace.ToFile( e );
            throw new RuntimeException( e );
        }
        
        return;
    }

    private void showMessageNotification( Context context, Intent intent ) {
        MessageType messageType = getMessageType( intent );
        
        // start the message service which will turn off the LED when the message gets read
        Intent serviceIntent = new Intent( context, WiMAXNotifierMessageService.class );
        serviceIntent.putExtra( WiMAXNotifierMessageService.EXTRA_COMMAND, WiMAXNotifierMessageService.COMMAND_NEW_MESSAGE );
        serviceIntent.putExtra( WiMAXNotifierMessageService.EXTRA_MESSAGE_TYPE, messageType );
        context.startService( serviceIntent );

    }
    
    
    /**
     * Determines the type of incoming message based on the intent given to the
     * {@link BroadcastReceiver} in onReceive
     * 
     * @param intent
     *            The intent originally passed to onReceive
     * @return A {@link MessageType} indicating which type of message was
     *         received
     */
    private MessageType getMessageType( Intent intent ) {
        if( intent.getAction().equals( "android.provider.Telephony.WAP_PUSH_RECEIVED" )
                && intent.getType().equals( "application/vnd.wap.mms-message" ) ) {
            return MessageType.MMS;
        }
        else if( intent.getAction().equals( "android.provider.Telephony.SMS_RECEIVED" ) ) {
            return MessageType.SMS;
        }
        else {
            throw new InvalidBroadcastException( "Unsupported message type!  Check receiver settings in manifest." );
        }
    }

    
    
    private void showConnectivityChangedNotification( Context context, Intent intent ) {
        NetworkInfo networkInfo = intent.getParcelableExtra( ConnectivityManager.EXTRA_NETWORK_INFO );
        
        String action = "";
        if( networkInfo.isConnectedOrConnecting() ) {
            action = "Connected to";
        }
        else {
            action = "Disconnected from";
        }
        
        String message = action + " type = " + networkInfo.getTypeName() + "(" + networkInfo.getType() + ")";
        Toast.makeText( context.getApplicationContext(), message, Toast.LENGTH_LONG ).show();
    }
}
