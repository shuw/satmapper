/*
 * Tnc.java
 *
 * Created on January 21, 2002, 12:48 PM
 */

package ca.gc.space.quicksat.ground.tnc;
import ca.gc.space.quicksat.ground.ax25.*;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class Tnc {

    /** Creates new Tnc */
    public Tnc() {
    }
    
    public static byte[] createDisableKissMessage() {
        
        byte[] seq1 = {     (byte)Frame.KISS_FEND, 
                            (byte)Frame.KISS_FEND,  
                            (byte)0x05,  
                            (byte)0x00, 
                            (byte)Frame.KISS_FEND,  
                            (byte)'\r',   
                            (byte)'\r',   
                            (byte)Frame.KISS_FEND, 
                            (byte)Frame.KISS_FEND,  
                            (byte)Frame.KISS_FEND, 
                            (byte)0xFF,   
                            (byte)Frame.KISS_FEND };
        
        return( seq1 );        
    }

}
