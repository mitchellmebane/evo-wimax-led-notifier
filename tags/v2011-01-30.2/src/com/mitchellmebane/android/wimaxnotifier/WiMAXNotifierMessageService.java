package com.mitchellmebane.android.wimaxnotifier;

import java.io.IOException;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.mitchellmebane.android.wimaxnotifier.lib.StackTrace;
import com.mitchellmebane.android.wimaxnotifier.lib.WiMAXLED;

public class WiMAXNotifierMessageService extends Service {
    // 
    // STATIC MEMBER VARIABLES
    //
    
    public static final Uri SMS_WATCH_URI = Uri.parse( "content://sms/" );
    public static final Uri SMS_MMS_WATCH_URI = Uri.parse( "content://mms-sms/" );

    // private static final String TAG = WiMAXNotifierMessageService.class.getSimpleName();
    private static final String TAG = "WiMAX Message Notifier - Service";

    public static final String EXTRA_COMMAND = "com.mitchellmebane.android.wimaxnotifier.service_extra_command";
    public static final String EXTRA_MESSAGE_TYPE = "com.mitchellmebane.android.wimaxnotifier.service_extra_message_type";

    public static final String COMMAND_NEW_MESSAGE = "new_message";
    
    
    // 
    // INNER CLASSES/ENUMS
    //
    
    public enum MessageType implements Comparable< MessageType > {
        SMS, 
        MMS, 
        /** MMS and SMS combined */
        MMSSMS
    }
    
    private class MMSSMSInboxObserver extends ContentObserver {
        public MMSSMSInboxObserver( Handler h ) {
            super( h );
        }
        
        @Override
        public void onChange( boolean selfChange ) {
            Log.i( TAG, "MMS-SMS Observer onChange" );
            handleMMSSMSChange();
        }
    }
    
    
    //
    // INSTANCE MEMBER VARIABLES
    // 
    
    private final Handler h = new Handler( new Handler.Callback() {
        @Override
        public boolean handleMessage( Message msg ) {
            Log.i( TAG, "In handler callback" );
            // true: message handled, false: continue trying to handle 
            return false;
        }
    });
    
    private final MMSSMSInboxObserver mmsSmsObserver = new MMSSMSInboxObserver( this.h );
    
    private int startId;
    
    private boolean mmsSmsObserverRegistered = false;

    private SharedPreferences sharedPreferences;
    
    
    // 
    // PRIVATE METHODS
    //
    
    private int getUnreadMessageCount( MessageType type ) {
        Log.i( TAG, "getUnreadMessageCount" );
        
        int count = 0;
        String messageURI = "";
        
        switch( type ) {
        case SMS:
            messageURI = "content://sms";
            break;
        case MMS:
            messageURI = "content://mms";
            break;
        case MMSSMS:
            return getUnreadMessageCount( MessageType.SMS ) + getUnreadMessageCount( MessageType.MMS );
        }
        
        Uri parsedMessageURI = Uri.parse( messageURI );
        
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query( parsedMessageURI, null, "read=0", null, null );
        
        if( cursor != null ) {
            try {
                count = cursor.getCount();
            }
            finally {
                cursor.close();
            }
        }
        
        Log.i( TAG, "Unread messages: " + count );
        
        return count;
    }
    
    private void handleMMSSMSChange() {
        int unreadMessageCount = getUnreadMessageCount( MessageType.MMSSMS );
        Log.i( TAG, "handleSMSChange - unread messages: " + unreadMessageCount );
        
        // if there are no more unread messages, turn off the light and shut down the service
        if( unreadMessageCount == 0 ) {
            try {
                Log.i( TAG, "handleSMSChange - turning off LED" );
                WiMAXLED.turnOff();
            }
            catch( IOException exception ) {
                Log.e( TAG, "Error when turning off LED", exception );
                StackTrace.ToFile( exception );
            }
            finally {
                Log.i( TAG, "handleSMSChange - unregistering observer" );
                this.getContentResolver().unregisterContentObserver( this.mmsSmsObserver );
                this.mmsSmsObserverRegistered = false;
                
                Log.i( TAG, "handleSMSChange - stopping service" );
                this.stopSelf( this.startId );
            }
        }
    }
    
    private void handleNewSMS_MMS() {
        Log.i( TAG, "handleNewSMS_MMS" );
        
        
        // turn on the LED
        try {
            if( !this.sharedPreferences.contains( "prefLEDColorRate" ) ) {
                Toast.makeText( this, "prefLEDColorRate not found!", Toast.LENGTH_SHORT ).show();
            }
            
            int mode = Integer.parseInt( this.sharedPreferences.getString( "prefLEDColorRate", "0" ) );
            WiMAXLED.setMode( mode );
        }
        catch( Exception exception ) {
            Log.e( TAG, exception.getMessage() );
            StackTrace.ToFile( exception );
        }
        
        
        // register the observer responsible for turning the LED off when messages get read
        if( !this.mmsSmsObserverRegistered ) {
            Log.i( TAG, "handleNewSMS_MMS - registering observer" );
            this.getContentResolver().registerContentObserver( SMS_MMS_WATCH_URI, true, this.mmsSmsObserver );
            this.mmsSmsObserverRegistered = true;
        }
    }
    
    
    private void handleCommand( Intent intent, int flags ) {
        if( intent == null ) {
            throw new IllegalArgumentException( "Intent must be non-null!" );
        }
        
        Bundle extras = intent.getExtras();
        
        String command = extras.getString( WiMAXNotifierMessageService.EXTRA_COMMAND );
        if( command.equals( WiMAXNotifierMessageService.COMMAND_NEW_MESSAGE ) ) {
            MessageType type = (MessageType)extras.get( WiMAXNotifierMessageService.EXTRA_MESSAGE_TYPE );
            
            switch( type ) {
            case SMS:
            case MMS:
            case MMSSMS:
                handleNewSMS_MMS();
                break;
            default:
                throw new IllegalArgumentException( "Unrecognized message type!" );
            }
        }
    }
    
    /**
     * Determines which notifications were active before the service got killed,
     * and starts handling them again
     */
    private void reloadNotifications() {
        // TODO Temp version, need to actually save and load active notifications
        if( getUnreadMessageCount( MessageType.MMSSMS ) > 0 ) {
            handleNewSMS_MMS();
        }
    }

    private boolean isStickyRestart( Intent intent, int flags ) {
        return (intent == null);
    }
    
    private boolean isRedeliveryRestart( Intent intent, int flags ) {
        return ((flags & Service.START_FLAG_REDELIVERY) == Service.START_FLAG_REDELIVERY);
    }
    
    
    
    // 
    // OVERRIDDEN METHODS
    //
    
    @Override
    public void onCreate() {
        super.onCreate();        
        Log.i( TAG, "onCreate" );
        
        PreferenceManager.setDefaultValues( this, R.xml.preferences, false );
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this.getApplicationContext() ); 
    }
    
    
    @Override
    public void onDestroy() {
        Log.i( TAG, "WiMAXNotifierMessageService.onDestroy" );
        this.getApplicationContext().getContentResolver().unregisterContentObserver( this.mmsSmsObserver );
    }
    
    
    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        Log.i( TAG, "WiMAXNotifierMessageService.onStartCommand" );
        
        this.startId = startId;
        
        if( isStickyRestart( intent, flags ) ) {
            Log.i( TAG, "intent was null, service sticky-restarted" );
            Toast.makeText( this, "Looks like the service was restarted - light might be stuck on", Toast.LENGTH_LONG ).show();
            // TODO: Refresh notifications
            reloadNotifications();
        }
        else {
            // TODO: Check for an existing notifications store - this would mean that the service was killed, and
            // restarted through a new notification instead of by the system
            handleCommand( intent, flags );
        }
        
        
        // we want to get restarted if we die, but we don't need the intent again
        // TODO: Save state when new notifications come in and old ones get turned off
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind( Intent intent ) {
        throw new UnsupportedOperationException( "WiMAXNotifierMessageService does not support binding" );
    }
}