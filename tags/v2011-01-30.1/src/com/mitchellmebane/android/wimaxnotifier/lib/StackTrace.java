package com.mitchellmebane.android.wimaxnotifier.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.os.Environment;
import android.util.Log;

public class StackTrace {
    private static final String TAG = "WiMAX Message Notifier :: " + StackTrace.class.getSimpleName();
    
    private static final String DIRECTORY_SEPARATOR = System.getProperty( "file.separator" );
    
    private StackTrace() {
        super();
    }
    
    public static void ToFile( Exception exception ) {
        String timestamp = new SimpleDateFormat( "yyyyMMddHHmmss" ).format( System.currentTimeMillis() );
        File dirFile = new File( 
                Environment.getExternalStorageDirectory() + DIRECTORY_SEPARATOR + 
                "WiMAXNotifier" + DIRECTORY_SEPARATOR + 
                "logs" + DIRECTORY_SEPARATOR );
        dirFile.mkdirs();
        File file = new File( dirFile, "wmnTrace_" + timestamp + ".stack" );
        
        FileOutputStream fileOutputStream = null;
        
        try {
            // get a string representation of the stack trace
            String stackString = Log.getStackTraceString( exception );
            
            // if there's something to write, write it
            if( stackString.length() > 0 ) {
                file.createNewFile();
                fileOutputStream = new FileOutputStream( file );
                fileOutputStream.write( stackString.getBytes() );
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }
        catch( FileNotFoundException fileNotFoundException ) {
            Log.e( StackTrace.TAG, "File not found!", fileNotFoundException );
        }
        catch( IOException ioException ) {
            Log.e( StackTrace.TAG, "Unable to write to file!", ioException );
        }
        
        return;
    }
}
