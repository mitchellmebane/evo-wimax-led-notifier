package com.mitchellmebane.android.wimaxnotifier;

import java.util.IllegalFormatConversionException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.mitchellmebane.android.wimaxnotifier.lib.StackTrace;

public class AboutActivity extends Activity implements OnClickListener {
    
    private void formatStrings() {
        String versionName = "ERROR READING VERSION";
        Resources res = this.getResources();
        
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo( 
                    getPackageName(), PackageManager.GET_META_DATA );
            versionName = packageInfo.versionName;
        }
        catch( PackageManager.NameNotFoundException exception ) {
            StackTrace.ToFile( exception );
        }
        
        try {
            TextView tvAppVersion = (TextView) findViewById( R.id.tvAppVersion );
            tvAppVersion.setText( versionName );
            
            TextView tvAppAuthor = (TextView) findViewById( R.id.tvAppAuthor );
            String strAppAuthor = String.format( 
                    getResources().getString( R.string.strAppAuthor ), 
                    getResources().getString( R.string.appAuthor ) );
            tvAppAuthor.setText( strAppAuthor );
            
            TextView tvProblems = (TextView) this.findViewById( R.id.tvProblems );
            tvProblems.setMovementMethod( LinkMovementMethod.getInstance() );
            String strProblems = 
                "In you have any issues, please see the " +
                "<a href=\"http://forum.xda-developers.com/showthread.php?t=900535\">WiMAX Message Notifier thread</a> " +
                "on XDA";
            tvProblems.setText( Html.fromHtml( strProblems ) );
            
            TextView tvSpecialThanks = (TextView)this.findViewById( R.id.tvSpecialThanks );
            tvSpecialThanks.setMovementMethod( LinkMovementMethod.getInstance() );
            String strSpecialThanks = 
                "Special thanks to: " + 
                "* The <a href=\"http://forum.xda-developers.com/forumdisplay.php?f=619\">XDA-Developers Evo board</a>; " +   
                "* bluedragon742, for starting this project; " +  
                "* kevin lynx of Stack Overflow, for pointing out ContentObserver";
            tvSpecialThanks.setText( Html.fromHtml( strSpecialThanks ) );
            
            TextView tvCopyright = (TextView) this.findViewById( R.id.tvCopyright );
            String copyrightDate = res.getString( R.string.copyrightDate );
            String strCopyright = String.format( 
                    res.getString( R.string.strCopyright ), 
                    copyrightDate, 
                    res.getString( R.string.appCompany ) );
            tvCopyright.setText( strCopyright );
        }
        catch( Resources.NotFoundException nfe ) {
            StackTrace.ToFile( nfe );
        }
        catch( IllegalFormatConversionException ife ) {
            StackTrace.ToFile( ife );
        }
        catch( Exception ex ) {
            StackTrace.ToFile( ex );
        }
        
        return;
    }
    
    @Override
    public void onClick( View view ) {
        if( view.getId() == R.id.btnDonate ) {
            Uri uri = Uri.parse( "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=NJPPCPDC2NWKJ&lc=US&item_name=Mebane%20Software&item_number=WiMAXNotifier&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted" );
            this.startActivity( new Intent( "android.intent.action.VIEW", uri ) );
        }
        return;
    }
    
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        
        Window window = this.getWindow();
        window.requestFeature( Window.FEATURE_NO_TITLE );
        
        this.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        this.setContentView( R.layout.about_dialog );
        this.formatStrings();
        
        Button btnDonate = (Button)this.findViewById( R.id.btnDonate );
        btnDonate.setOnClickListener( this );
        
        return;
    }
}
