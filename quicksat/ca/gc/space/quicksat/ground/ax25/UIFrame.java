/*
 * UIFrame.java
 *
 * Created on October 11, 2001, 2:22 PM
 */

package ca.gc.space.quicksat.ground.ax25;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class UIFrame extends UnnumberedFrame {

    /*========================================================================*/
    /** Creates new UIFrame                                                   */
    /*========================================================================*/
    public UIFrame(String originCallsign, int originSSID,
                    String destinationCallsign, int destinationSSID,
                    int protocolID,
                    byte[] infoBytes) {
    /*========================================================================*/                    
                        
        /*-----------------------------------------------*/
        /* Use Frame methods to build the new INFO Frame */
        /*-----------------------------------------------*/
        setDestinationAddress( destinationCallsign.trim(), destinationSSID );
        setOriginatorAddress( originCallsign.trim(), originSSID );
        setID( ID_UI );
        setPID( protocolID );
        setInfoBytes( infoBytes );
        setFinal();
       
    }
    public String toString() { return(super.toString()); }

}
