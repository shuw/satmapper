/*
 * GS232.java
 *
 * Created on August 13, 2001, 1:35 PM
 */

package ca.gc.space.quicksat.ground.rotator;
import java.io.*;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class GS232 extends AntennaRotator {
    
private int lastAzimuth     = -10000;
private int lastElevation   = -10000;
private int orientationTol  = 3; //in deg.
private long lastCommandTimeStamp = (long)0;
private long enoughDelay          = (long)5000; //in mSec

    
    /** Creates new GS232 */
    public GS232() {        
        System.out.println("Instantiating GS-232 antenna rotator");
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public byte[] messageToSetAzimuth( int degrees ) {
    /*========================================================================*/    
        String command =    "M" + 
                            convertToStringOf3Chars( degrees ) +
                            "\r\n";
        lastAzimuth = degrees;
        return( command.getBytes() );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public byte[] messageToSetElevation( int degrees ) {
    /*========================================================================*/    
        String command =    "W" + 
                            convertToStringOf3Chars( degrees ) +
                            " " +
                            convertToStringOf3Chars( lastElevationSet ) +                            
                            "\r\n";
        lastElevation = degrees;
        return( command.getBytes() );
    }
  
    /*========================================================================*/
    /** Tell us if it is time to command the antenna rotation. An algorithm is
     *  implemented that make sure that we do not use the rotator to often
     *  (each operation is recorded, and if we order a little change when the 
     *  previous command was issued not too long ago then we do not move until
     *  a longer time has passed...
    /*========================================================================*/
    public boolean isTimeToSetOrientation(  int azimuthDegrees,
                                            int elevationDegrees ) {
    /*========================================================================*/
        
        /*----------------------*/
        /* Get the current time */
        /*----------------------*/
        long currTime = System.currentTimeMillis();
        
        /*-------------------------------------------------------------*/
        /* If the changes are large or a relative long time has passed */
        /* since the last command, then issue a new command...         */
        /*-------------------------------------------------------------*/
        //System.out.println("Az test:"
        //                +java.lang.Math.abs(azimuthDegrees-lastAzimuth));
        //System.out.println("El test:"
        //                +java.lang.Math.abs(elevationDegrees-lastElevation));
        //System.out.println("Time test:"
        //                +java.lang.Math.abs(currTime-lastCommandTimeStamp));
        if(  (java.lang.Math.abs(azimuthDegrees-lastAzimuth)>orientationTol)
           ||(java.lang.Math.abs(elevationDegrees-lastElevation)>orientationTol)
           ||(java.lang.Math.abs(currTime-lastCommandTimeStamp)>enoughDelay))
        {   
            /*----------------------------------------------------*/
            /* First thing we record the command time stamp, even */
            /* if we cannot send it ultimately                    */
            /*----------------------------------------------------*/
            lastCommandTimeStamp = currTime;            
            return( true );
        } else {
            return( false );
        }
    }
    /*========================================================================*/
    /** Set the orientation of the antenna.                                   */
    /*========================================================================*/
    public byte[] messageToSetOrientation(  int azimuthDegrees,
                                            int elevationDegrees ) {
    /*========================================================================*/
        String command =    "W" + 
                            convertToStringOf3Chars( azimuthDegrees ) +
                            " " +
                            convertToStringOf3Chars( elevationDegrees ) +
                            "\r\n";
        //System.out.println("Set orientation to: az="+azimuthDegrees
        //                   +" el="+elevationDegrees);
        lastAzimuth = azimuthDegrees;
        lastElevation = elevationDegrees;
        return( command.getBytes() );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    private String convertToStringOf3Chars( int value ) {
    /*========================================================================*/    
        String strValue = null;
        
        /*-------------------------------*/
        /* Check for out of range values */
        /*-------------------------------*/
        if( value < 0 ) return( "000" );
        if( value > 999 ) return( "999" );
        
        /*---------------------------*/
        /* Convert integer to string */
        /*---------------------------*/
        strValue = String.valueOf( value );
        
        /*-------------------------------------------*/
        /* Pad if necessary to return 3 chars string */
        /*-------------------------------------------*/
        int lengthOfString = strValue.length();
        if( lengthOfString <= 0 ) {
            return( "000" );
        } else if( lengthOfString == 1 ) {
            return( "00" + strValue );
        } else if( lengthOfString == 2 ) {
            return( "0" + strValue );
        } else if( lengthOfString == 3 ) {
            return( strValue );
        } 
        
        /*---------------------------------------------------*/
        /* If does not match above criteria, it is more than */
        /* 3 chars so return 999.                            */
        /*---------------------------------------------------*/
        return( "999" );        
    }

}
