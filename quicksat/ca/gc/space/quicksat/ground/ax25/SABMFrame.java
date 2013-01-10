/*
 * SABMFrame.java
 *
 * Created on October 11, 2001, 2:33 PM
 */

package ca.gc.space.quicksat.ground.ax25;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class SABMFrame extends UnnumberedFrame {

    /*========================================================================*/
    /** Creates new SABMFrame. NOTE that we're using the default underlying
     *  protocole (KISS), and eventually we should have another constructor
     *  that allow us to specify if we want KISS or HDLC...                   */
    /*========================================================================*/
    public SABMFrame(   String originCallsign, int originSSID,
                        String destinationCallsign, int destinationSSID ) {
    /*========================================================================*/        
        
        /*-----------------------------------------*/
        /* Use Frame methods to build the new SABM */
        /*-----------------------------------------*/
        setDestinationAddress( destinationCallsign.trim(), destinationSSID );
        setOriginatorAddress( originCallsign.trim(), originSSID );
        setID( ID_SABM );
        setPoll();

    }
    public String toString() { return(super.toString()); }

}
