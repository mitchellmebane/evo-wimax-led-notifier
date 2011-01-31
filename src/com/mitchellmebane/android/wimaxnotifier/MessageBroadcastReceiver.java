package com.mitchellmebane.android.wimaxnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.mitchellmebane.android.wimaxnotifier.WiMAXNotifierMessageService.MessageType;
import com.mitchellmebane.android.wimaxnotifier.exceptions.InvalidBroadcastException;
import com.mitchellmebane.android.wimaxnotifier.lib.StackTrace;
import com.mitchellmebane.android.wimaxnotifier.utils.Misc;

public class MessageBroadcastReceiver extends BroadcastReceiver {
     // private static final String TAG = BroadcastReceiver.class.getSimpleName();
    private static final String TAG = "WiMAX Message Notifier - Receiver";
    
    @Override
    public void onReceive( Context context, Intent intent ) {
        Misc.logIntent( intent );
        try {
            // if we got a text message or an MMS, show the notification
            if( (isSMS( intent ) && !isVoicemail( intent )) || isMMS( intent ) ) {
                showMessageNotification( context, intent );
            }
            // if we had a connectivity changed event, see if we need to change the 4G status (not yet implemented) 
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
    
    /**
     * Determines whether a given notification represents a new SMS. Note: This
     * does not necessarily mean it is a new text message! SMSs are also used
     * for things like new voicemail notifications.
     * 
     * @param i
     *            The intent originally delivered to onReceive
     * @return <code>True</code> if this notification is an SMS notification,
     *         <code>false</code> otherwise
     */
    private static boolean isSMS( Intent i ) {
        return "android.provider.Telephony.SMS_RECEIVED".equals( i.getAction() );
    }
    
    /**
     * Determines whether a given notification represents a new MMS
     * 
     * @param i The intent originally delivered to onReceive
     * @return <code>True</code> if this notification is a new MMS, <code>false</code> otherwise
     */
    private static boolean isMMS( Intent i ) {
        return 
            "android.provider.Telephony.WAP_PUSH_RECEIVED".equals( i.getAction() ) && 
            "application/vnd.wap.mms-message".equals(  i.getType() );
    }
    
    /**
     * Determines whether a given notification is a voicemail-related SMS
     * 
     * @param i The intent originally delivered to onReceive
     * @return <code>True</code> if this SMS is a voicemail notification, <code>false</code> otherwise
     */
    private static boolean isVoicemail( Intent i ) {
        boolean isSmsAction = "android.provider.Telephony.SMS_RECEIVED".equals( i.getAction() );
        
        Object[] pdus = (Object[]) i.getExtras().get( "pdus" );
        byte[] pdu = (byte[]) pdus[0];
        SmsMessage sms = SmsMessage.createFromPdu( pdu );
        
        // only the clear one seems to be true for new voicemail notifications
        // who knows when the others get used, but let's ignore them anyway
        boolean isVoicemailNotification = 
            sms.isCphsMwiMessage() ||
            sms.isMWIClearMessage() || // incoming message
            sms.isMWISetMessage() || 
            sms.isMwiDontStore();
        
        return isSmsAction && isVoicemailNotification;
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
