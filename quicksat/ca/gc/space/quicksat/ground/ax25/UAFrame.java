/*
 * UAFrame.java
 *
 * Created on October 11, 2001, 2:35 PM
 */

package ca.gc.space.quicksat.ground.ax25;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class UAFrame extends UnnumberedFrame {

    /*========================================================================*/
    /** Creates new UAFrame. NOTE that we're using the default underlying
     *  protocole (KISS), and eventually we should have another constructor
     *  that allow us to specify if we want KISS or HDLC...                   */
    /*========================================================================*/
    public UAFrame(   String originCallsign, int originSSID,
                        String destinationCallsign, int destinationSSID ) {
        /*---------------------------------------*/
        /* Use Frame methods to build the new UA */
        /*---------------------------------------*/
        setDestinationAddress( destinationCallsign.trim(), destinationSSID );
        setOriginatorAddress( originCallsign.trim(), originSSID );
        setID( ID_UA );
        setFinal();
    }
    public String toString() { return(super.toString()); }

}
