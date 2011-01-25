package com.mitchellmebane.android.wimaxnotifier.lib;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.mitchellmebane.android.wimaxnotifier.utils.Misc;


public class WiMAXLED {
    public static final String CONTROL_DEVICE = "/sys/class/leds/wimax/brightness";

    public enum Mode {
        OFF( 0 ),
        GREEN_RAPID( 1 ),
        GREEN_SLOW( 2 ),
        GREEN_GLOW( 3 ),
        GREEN_RED_ALTERNATE( 4 ),
        ORANGE_GLOW( 5 ),
        RED_SOLID( 129 ),
        GREEN_SOLID( 130 ),
        ORANGE_SOLID( 131 );
        
        private int stateCode;
        
        private Mode( int stateCode ) {
            this.stateCode = stateCode;
        }
        
        public int getCode() {
            return this.stateCode;
        }
        
        public static Mode fromCode( int code ) {
            switch( code ) {
            case 0:
                return OFF;
            case 1:
                return Mode.GREEN_SLOW;
            case 2:
                return Mode.GREEN_SLOW;
            case 3:
                return Mode.GREEN_GLOW;
            case 4:
                return Mode.GREEN_RED_ALTERNATE;
            case 5:
                return Mode.ORANGE_GLOW;
            case 129:
                return Mode.RED_SOLID;
            case 130:
                return Mode.GREEN_SOLID;
            case 131:
                return Mode.ORANGE_SOLID;
            default:
                throw new IllegalArgumentException();
            }
        }
    }
    
    private WiMAXLED() {
    }
    
    public static void setMode( int mode ) throws IOException {
        // convert the mode to a string
        String modeString = Integer.toString( mode );
        
        File ledDevice = new File( CONTROL_DEVICE );
        DataOutputStream ledOut = new DataOutputStream( new FileOutputStream( ledDevice ) );
        
        ledOut.writeBytes( modeString );
        ledOut.flush();
        
        Misc.closeOrLikeWhatever( ledOut );
    }
    
    public static void setMode( Mode mode ) throws IOException {
        setMode( mode.getCode() );
    }

    public static void turnOff() throws IOException {
        setMode( Mode.OFF );
    }
    
    public static boolean exists() {
        File ledDevice = new File( CONTROL_DEVICE );
        return ledDevice.exists();
    }
    
    public static int getState() throws FileNotFoundException, IOException {
        File ledDevice = new File( CONTROL_DEVICE );
        DataInputStream ledIn = new DataInputStream( new FileInputStream( ledDevice ) );
        String state = ledIn.readLine();
        
        Misc.closeOrLikeWhatever( ledIn );
        
        return Integer.parseInt( state );
    }
    
    public static boolean isOff() throws IOException {
        return !isOn();
    }
    
    public static boolean isOn() throws IOException {
        switch( getState() ) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 129:
        case 130:
        case 131:
            return true;
        default:
            return false;
        }
    }
    
    public static boolean isInKnownState() throws IOException {
        switch( getState() ) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 129:
        case 130:
        case 131:
            return true;
        default:
            return false;
        }
    }
}
