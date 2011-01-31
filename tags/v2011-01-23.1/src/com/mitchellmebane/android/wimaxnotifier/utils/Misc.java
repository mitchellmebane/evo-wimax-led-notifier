package com.mitchellmebane.android.wimaxnotifier.utils;

import java.io.Closeable;
import java.io.OutputStream;

public class Misc {
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
}