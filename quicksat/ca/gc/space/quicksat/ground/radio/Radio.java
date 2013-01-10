/*
 * Radio.java
 *
 * Created on April 17, 2001, 4:45 PM
 */

package ca.gc.space.quicksat.ground.radio;
import java.io.*;
import javax.swing.*;
import ca.gc.space.quicksat.ground.client.*;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class Radio extends java.lang.Object {

private final int       MAX_BUFFER_ELEMENTS = 1000;
private final int       DATA_BUFFER_SIZE = 1000;
static final boolean    NOT_USING_NETWORK = false;
static final boolean    USING_NETWORK = true;    
OutputStream            radioOutput = null;
DataOutputStream        dataOut = null;
InputStream             radioInput = null;
DataInputStream         dataIn = null;
ServerLink              serverLink = null;
boolean                 useNetwork = false;
//public boolean          isOnline = false;
//PanelRadio              panelRadio;
//Vector                  dataBuffer = null;
//byte[]                  dataByteBuffer = null;
//ByteArrayInputStream    dataByteIn = null;
//ByteArrayOutputStream   dataByteOut = null;
PipedInputStream        dataByteIn = null;
PipedOutputStream       dataByteOut = null;

    /** Creates new Radio */
    public Radio(  ) {
        //panelRadio = panelRadioToUse;
        //dataByteBuffer = new byte[MAX_BUFFER_ELEMENTS];
        try{
            dataByteIn = new PipedInputStream();
            dataByteOut = new PipedOutputStream(dataByteIn);
        } catch( IOException ioe ) {
            System.out.println("ERROR setting radio buffer: "+ioe);
        }
        System.out.println("Initializing Radio");
    }

    /*------------------------------------------------------------------------*/
    /** Creates the message that is used to enable ("wakeup...") the computer 
     *  interface of the radio.                                               
     *  @return A byte array containing the message to send to the radio      */
    /*------------------------------------------------------------------------*/
    public byte[] messageToEnableComputerInterface() {
    /*------------------------------------------------------------------------*/    
        System.out.println("Enabling radio-computer interface");
        return( null );
    }
    
    /*------------------------------------------------------------------------*/
    /** Creates the message used to request the current reception frequency
     *  to the radio.
     *  @return A byte array containing the message used to request the 
     *          receive frequency to the radio                                */
    /*------------------------------------------------------------------------*/
    public byte[] messageToRequestRXFrequency() {
    /*------------------------------------------------------------------------*/            
        System.out.println("Requesting RX Frequency from radio");
        return( null );
    }

    /*------------------------------------------------------------------------*/
    /** Creates the message used to request the current transmission frequency
     *  to the radio.
     *  @return A byte array containing the message used to request the 
     *          transmission frequency to the radio                           */
    /*------------------------------------------------------------------------*/
    public byte[] messageToRequestTXFrequency() {
    /*------------------------------------------------------------------------*/    
        System.out.println("Requesting TX Frequency from radio");
        return( null );
    }

    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToRequestRXStatus() {
    /*------------------------------------------------------------------------*/    
        System.out.println("Requesting RX Status from radio");
        return( null );
    }
        
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToRequestTXStatus() {
    /*------------------------------------------------------------------------*/    
        System.out.println("Requesting TX Status from radio");
        return( null );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetRXFrequency(  int v100M,int v10M,int v1M,
                                            int v100K,int v10K,int v1K,
                                            int v100,int v10,int v1   ) {
    /*------------------------------------------------------------------------*/ 
        System.out.println("Set radio RX Frequency");
        return( null );
    }
    
    /*------------------------------------------------------------------------*/
    /** Set the receiver frequency
     *  @param frequency Frequency to set, in Hertz                           */
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetRXFrequency( int frequency ) {
    /*------------------------------------------------------------------------*/    
        System.out.println("Set radio RX Frequency");
        return( null );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetTXFrequency(  int v100M,int v10M,int v1M,
                                            int v100K,int v10K,int v1K,
                                            int v100,int v10,int v1   ) {
    /*------------------------------------------------------------------------*/                                
        System.out.println("Set radio TX Frequency");
        return( null );
    }
    
    /*------------------------------------------------------------------------*/
    /** Creates the message to control the radio mode.
     *  @param link String selecting up or downling ("TX" or "RX" respectively).
     *  @param mode One of: "Lower Sideband" (Single Side Band - Voice)
     *                      "Upper Sideband"
     *                      "CW" (Constant Wave - Morse code)
     *                      "CW-R"
     *                      "AM" (Amplitude Modulation)
     *                      "FM" (Frequency Modulation - 150KHz bandwidth)
     *                      "CW(N)"
     *                      "CW(N)-R"
     *                      "AM(N)"
     *                      "FM(N)" (FM Narrowband - 10KHz bandwidth)         */
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetMode( String link, String mode ) {
    /*------------------------------------------------------------------------*/    
        System.out.println("Set mode");
        return( null );
    }        
    
    /*------------------------------------------------------------------------*/
    /** Creates the message used to set the CTCSS frequency. Note that the CTCSS
     *  mode has to be turned ON by sending messageToEnableCTCSS.
     *  @param link String indicating uplink or downlink ("TX" or "RX" respect.)
     *  @param freq String containing in plain ASCII the CTCSS frequency. One of
     *  "67.0", "69.3", "71.9", "74.4", "77.0", "79.7", "82.5", "85.4", "88.5",
     *  "91.5", "94.8", "97.4", "100.0", "103.5", "107.2", "110.9", "114.8",
     *  "118.8", "123.0", "127.3", "131.8", "136.5", "141.3", "146.2", "151.4",
     *  "156.7", "162.2", "167.9", "173.8", "179.9", "186.2", "192.8", "203.5",
     *  "210.7", "218.1", "225.7", "233.6", "241.8", "250.3" or "OFF"         */
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetCTCSS( String link, String freq ) {
    /*------------------------------------------------------------------------*/    
        System.out.println("Set CTCSS");
        return( null );
    }
        
        
        
    /*------------------------------------------------------------------------*/
    /** Creates the message used to enable/disable the CTCSS.
     *  @param link String indicating uplink or downlink ("TX" or "RX" respect.)
     *  @param enable true to enable, false to disable CTCSS                  */
    /*------------------------------------------------------------------------*/
    public byte[] messageToEnableCTCSS( String link, boolean enable ) {
    /*------------------------------------------------------------------------*/    
        System.out.println("Enable CTCSS");    
        return( null );                    
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public void fillCTCSSComboBox(JComboBox combo) {
    /*------------------------------------------------------------------------*/    
        System.out.println("Fill CTCSS Combobox");
        return;
    }
    
        
        
    
    
}
