/** InfoFrame.java
 *
 * Created on October 11, 2001, 2:31 PM
 */

package ca.gc.space.quicksat.ground.ax25;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class IFrame extends Frame {

    /*========================================================================*/
    /** Creates new INFO Frame. NOTE that we're using the default underlying
     *  protocole (KISS), and eventually we should have another constructor
     *  that allow us to specify if we want KISS or HDLC...                   */
    /*========================================================================*/
    public IFrame(  String originCallsign, int originSSID,
                    String destinationCallsign, int destinationSSID,
                    byte[] infoBytes ) {
    /*========================================================================*/
                        
        /*-----------------------------------------------*/
        /* Use Frame methods to build the new INFO Frame */
        /*-----------------------------------------------*/
        setDestinationAddress( destinationCallsign.trim(), destinationSSID );
        setOriginatorAddress( originCallsign.trim(), originSSID );
        setID( ID_INFO );
        setInfoBytes( infoBytes );
        setFinal();
        
    }
    public String toString() { return(super.toString()); }

}
