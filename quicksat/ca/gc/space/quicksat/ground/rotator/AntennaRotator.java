/*
 * Antenna.java
 *
 * Created on August 13, 2001, 1:24 PM
 */

package ca.gc.space.quicksat.ground.rotator;

import java.io.*;
import java.util.*;
import ca.gc.space.quicksat.ground.client.*;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class AntennaRotator extends Object {

private final int MAX_BUFFER_ELEMENTS = 1000;    
DataInputStream din = null;
DataOutputStream dout = null;
int lastAzimuthSet = 0;
int lastElevationSet = 0;
boolean useNetwork = true;
ServerLink serverLink = null;
Vector dataBuffer = null;
    
    /** Creates new Antenna */
    public AntennaRotator() {
        System.out.println("Instantiating antenna rotator");
        dataBuffer = new Vector();
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public byte[] messageToSetAzimuth( int degrees ) {
    /*========================================================================*/    
        return( null );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public byte[] messageToSetElevation( int degrees ) {
    /*========================================================================*/    
        return( null );
    }
  
    /*========================================================================*/
    /** Tell us if it is time to command the antenna rotation. An algorithm is
     *  implemented that make sure that we do not use the rotator to often
     *  (each operation is recorded, and if we order a little change when the 
     *  previous command was issued not too long ago then we do not move until
     *  a longer time has passed...                                           */
    /*========================================================================*/
    public boolean isTimeToSetOrientation(  int azimuthDegrees,
                                            int elevationDegrees ) {
    /*========================================================================*/
        return( false );
    }
    
    /*========================================================================*/
    /** Set the orientation of the antenna.                                   */
    /*========================================================================*/
    public byte[] messageToSetOrientation(  int azimuthDegrees,
                                            int elevationDegrees ) {
    /*========================================================================*/
        return( null );
    }
    
}
