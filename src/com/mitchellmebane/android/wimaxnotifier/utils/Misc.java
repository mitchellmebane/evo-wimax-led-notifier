package com.mitchellmebane.android.wimaxnotifier.utils;

import java.io.Closeable;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Misc {
    public static final String TAG = "WiMAX Message Notifier - Misc";
    
    public static void closeOrLikeWhatever( Closeable c ) {
        if( c != null ) {
            try {
                c.close();
            }
            catch( Throwable t ) {
                // ignore
            }
        }
    }
    
    /**
     * Flushes and closes an {@link OutputStream}, ignoring any exceptions which
     * may arise during the process
     * 
     * @param os
     *            The {@link OutputStream} to flush and close
     */
    public static void closeOrLikeWhatever( OutputStream os ) {
        if( os != null ) {
            try {
                os.flush();
            }
            catch( Throwable t ) {
                // ignore
            }
            
            try {
                os.close();
            }
            catch( Throwable t ) {
                // ignore
            }
        }
    }
    
    public static void destroyOrLikeWhatever( Process p ) {
        if( p != null ) {
            try {
                p.destroy();
            }
            catch( Throwable t ) {
                // ignore
            }
        }
    }
    
    /**
     * Works like Arrays.toString( byte[] ), except each entry is printed as a
     * two-digit hex string, prefixed with 0x. E.g., "[0x01, 0xff]"
     * 
     * @param b
     *            The byte[] to convert to a {@link String}
     * @return The {@link String} representation of the passed-in byte[].
     */
    public static String toHexArrayString( byte[] b ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "[" );
        for( int i = 0; i < b.length; ++i ) {
            if( i > 0 ) {
                sb.append( ", " );
            }
            
            sb.append( "0x" );
            
            if( (b[i] & 0x000000FF) < 0x10 ) {
                sb.append( "0" );
            }
            sb.append( Integer.toHexString( b[i] & 0x000000FF ) );
        }
        sb.append( "]" );
        return sb.toString();
    }

    
    public static void logObject( String name, Object o ) {
        Class<?> c = o.getClass();
        
        Log.i( TAG, "onReceive: extra " +
                "key=[ " + name + "], " +
                "type=[" + c.getCanonicalName() + "], " +
                "value=[" + o.toString() + "]" );
        
        if( c.isArray() ) {
            try {
                Object[] oa = (Object[]) o;
                for( int i = 0; i < oa.length; ++i ) {
                    logObject( name + "[" + i + "]", oa[i] );
                }
            }
            catch( ClassCastException cce ) {
                Log.w( TAG, "Can't cast array to Object[], it must be a primitive array!" );
            }
        }
        else {
            if( c.isAnonymousClass() ) {
                Log.i( TAG, "Anonymous class in: " + c.getEnclosingMethod().toGenericString() );
            }
            
            Log.i( TAG, "*** Superclass: " + c.getSuperclass().getCanonicalName() );
            
            Log.i( TAG, "*** Interfaces:" );
            for( Class<?> iface : c.getInterfaces() ) {
                Log.i( TAG, iface.getCanonicalName() );
            }
            
            Log.i( TAG, "*** Constructors:" );
            for( Constructor< ? > con : c.getConstructors() ) {
                Log.i( TAG, con.toGenericString() );
            }
            
            Log.i( TAG, "*** Fields:" );
            for( Field f : c.getDeclaredFields() ) {
                f.setAccessible( true );
                try {
                    Log.i( TAG, f.toGenericString() + ": " + f.get( o ).toString() );
                }
                catch( Exception e ) {
                    Log.e( TAG, "Exception while accessing field", e );
                }
            }
            
            Log.i( TAG, "*** Methods:" );
            for( Method m : c.getDeclaredMethods() ) {
                Log.i( TAG, m.toGenericString() );
            }
        }
    }
    
    public static void logIntent( Intent intent ) {
        Log.i( TAG, "onReceive: intent action: " + intent.getAction() );
        Log.i( TAG, "onReceive: intent type: " + intent.getType() );
        
        Log.i( TAG, "onReceive: intent extras:" );
        
        Bundle extras = intent.getExtras();
        for( String key : extras.keySet() ) {
            Object val = extras.get( key );
            try {
                Misc.logObject( key, val );
            }
            catch( Exception e ) {
                Log.e( TAG, "Exception while logging object", e );
            }
        }
        
    }
}
