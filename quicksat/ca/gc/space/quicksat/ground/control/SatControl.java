/*
 * SatControl.java
 *
 * Created on February 22, 2002, 10:07 AM
 */

package ca.gc.space.quicksat.ground.control;

import ca.gc.space.quicksat.ground.ax25.*;
import ca.gc.space.quicksat.ground.radio.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class SatControl {
    
String  ctrlCallsign        = "";
int     ctrlSSID            = 0;
String  ctrlKey             = "";
String  ctrlHeader          = "CCC";
int     COMMAND_PID         = 0;
int     LOADER_PID          = 0;
int     EXTENDED_LOADER_PID = 0;

int     BLspacecraftID = 0x00;    

/*-----------------------------------------*/
/* Bootloader Encryption Keys and commands */
/*-----------------------------------------*/
int AVAL = 0;
int BVAL = 0;
int D3VAL= 0;
int K1H  = 0;
int K1L  = 0;
public int blcmdMove     = 0;      //Move Kernal/PHT
public int blcmdExecute  = 0;      //Execute loaded program
public int blcmdBeacon   = 0;      //Request beacon
public int blcmdPeekMem  = 0;      //Examine memory
public int blcmdPeekIO   = 0;      //Examine IO port
public int blcmdPokeMem  = 0;      //Write memory
public int blcmdPokeIO   = 0;      //Write IO port
public int blcmdDumpMem  = 0;      //Dump memory content
public int blcmdLoad     = 0;      //Load a file
public int blcmd1        = 0;
public int blcmd2        = 0;

String baseDir           = null; //Where we find init data
Vector CTCSSVector       = null;

    /*------------------------------------------------------------------------*/
    /** Creates new SatControl                                                */
    /*------------------------------------------------------------------------*/
    public SatControl( String baseDir ){
    /*------------------------------------------------------------------------*/
        this.baseDir = baseDir;
        if( this.baseDir == null ) this.baseDir = "";
        CTCSSVector = new Vector();
        try {
            FileInputStream fis = new FileInputStream(baseDir+"qsgs_ctcss.txt");
            DataInputStream dis = new DataInputStream( fis );
            //if( progressDialog == null )
            //    System.out.println("Found file '"+baseDir+"qsgs_ctcss.txt': Loading flight CTCSS data");            
            //else
            //    progressDialog.appendInfo("Found file '"+baseDir+"qsgs_ctcss.txt': Loading flight CTCSS data\n");
            while( (dis.available()>0) ) {                
                String codeRead = dis.readLine().trim();
                String frequencyRead = dis.readLine().trim();
                CTCSSVector.add( new CTCSS( frequencyRead, codeRead ) );
            }
        } catch( IOException ioe ) { }
        
        for( int i=0; i<CTCSSVector.size(); i++ ) {
            CTCSS ctcss = (CTCSS)CTCSSVector.elementAt(i);
        }        
    }
    
    public void finalize() {
        CTCSSVector.removeAllElements();
    }
    
    /*=======================================================================*/
    /*=======================================================================*/
    public int getCTCSSListSize() {
    /*=======================================================================*/    
        return CTCSSVector.size();
    }
    
    
    /*=======================================================================*/
    /*=======================================================================*/
    public CTCSS getCTCSS( int i ) {
    /*=======================================================================*/    
        return( (CTCSS)CTCSSVector.elementAt( i ) );
    }
    
    public SatControl(String ctrlCallsign, int ctrlSSID) {
        this.ctrlCallsign = ctrlCallsign.trim();
        this.ctrlSSID = ctrlSSID;
    }
    
    public void setControlAddress( String ctrlCallsign, int ctrlSSID) {
        this.ctrlCallsign = ctrlCallsign.trim();
        this.ctrlSSID = ctrlSSID;
    }
    
    public void setControlCallsign( String ctrlCallsign ) {
        this.ctrlCallsign = ctrlCallsign;
    }
    public void setControlSSID( int ctrlSSID ) {
        this.ctrlSSID = ctrlSSID;
    }

    public void setControlProtocolId( int PID ) {
        this.COMMAND_PID = PID;
    }
    
    public void setControlKey( String ctrlKey ) {
        this.ctrlKey = ctrlKey;
    }
    
    public void setLoaderProtocolId( int PID ) {
        this.LOADER_PID = PID;
    }
    public void setExtLoaderProtocolId( int PID ) {
        this.EXTENDED_LOADER_PID = PID;
    }
    
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageGeneric(byte[] generic) {
    /*------------------------------------------------------------------------*/
        if( generic == null ) return( null );
        
        byte[] ctrlHeaderBytes = ctrlHeader.getBytes();
        byte[] ctrlKeyBytes = ctrlKey.getBytes();
        byte[] message = new byte[   ctrlHeaderBytes.length
                                    + ctrlKeyBytes.length
                                    + generic.length ];
        int i=0;
        int j=0;
        for( i=0; i<ctrlHeaderBytes.length; i++, j++ )
            message[i] = ctrlHeaderBytes[i];
        for( i=0; i<ctrlKeyBytes.length; i++, j++ )
            message[j] = ctrlKeyBytes[i];
        for( i=0; i<generic.length; i++, j++ )
            message[j] = generic[i];
        
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToRequestBeacon() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"AAA").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /** Creates the message to set the transmitter delay.
     *  @param transmitterId Ident of transmitter (starts at 1) - NOTE THAT IN
     *                       THIS IMPLEMENTATION THE TXDELAY IS SET FOR ALL 
     *                       TRANSMITTER, so this param is ignored...
     *  @param delay Delay, in increments of 20mSec.
     *  @return Byte array, containing the message to send to the spacecraft  */    
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetTransmitterDelay(    int transmitterId, 
                                                   int delay ){
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(   ctrlHeader
                              + ctrlKey
                              + "BBB"
                              + String.valueOf(delay).trim() ).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToCrashToBootloader() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"CCC").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToTerminateHWAC() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"LLLK").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToActivateDigipeat() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"DDD1").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDeactivateDigipeat() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"DDD0").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemA(   int offset, 
                                        int deviation, 
                                        String source,
                                        int CTCSScode ) {
    /*------------------------------------------------------------------------*/
                                            
        String baseMessage = ctrlHeader+ctrlKey+"EEE";
        
        String offsetMessage = Integer.toString( offset, 16 ).trim();
        if( (offsetMessage.length() <= 0) || (offset < 0) )  offsetMessage = "00";
        else if(offsetMessage.length() == 1) offsetMessage = "0"+offsetMessage;
        else if(offsetMessage.length() != 2) offsetMessage = "FF";        
            
        String deviationMessage = Integer.toString( deviation, 16 ).trim();
        if( (deviationMessage.length() <= 0) || (deviation < 0) )  deviationMessage = "00";
        else if(deviationMessage.length() == 1) deviationMessage = "0"+deviationMessage;
        else if(deviationMessage.length() != 2) deviationMessage = "FF";        
        
        /* NOT COMPLETE */
        
        String message = baseMessage + offsetMessage + deviationMessage;
        byte[] messageBytes = null;
        messageBytes = message.getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  messageBytes ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemAOffset( String offset ) {
    /*------------------------------------------------------------------------*/                                            
        offset = offset.trim();
        if( offset.length() <= 0 )  offset = "00";
        else if(offset.length() == 1) offset = "0"+offset;
        else if(offset.length() != 2) offset = "FF";        
        String message = ctrlHeader+ctrlKey+"EEEO" + offset;
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemADeviation( String deviation ) {
    /*------------------------------------------------------------------------*/                                            
        deviation = deviation.trim();
        if( deviation.length() <= 0 )  deviation = "00";
        else if(deviation.length() == 1) deviation = "0"+deviation;
        else if(deviation.length() != 2) deviation = "FF";        
        String message = ctrlHeader+ctrlKey+"EEED" + deviation;
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemASource( String source ) {
    /*------------------------------------------------------------------------*/
        source = source.trim().toUpperCase();
        if( !source.equals("A") && !source.equals("D") ) return(null);
        String message = ctrlHeader+ctrlKey+"EEES" + source;
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemACTCSS( String CTCSSCode ) {
    /*------------------------------------------------------------------------*/                                            
        CTCSSCode = CTCSSCode.trim();
        String message = ctrlHeader+ctrlKey+"EEEC" + CTCSSCode;
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemB(   int offset,
                                        int deviation,
                                        String source,
                                        boolean scramble,
                                        int rate,
                                        int CTCSScode ) {
    /*------------------------------------------------------------------------*/
        String baseMessage = ctrlHeader+ctrlKey+"FFF";
        
        String offsetMessage = Integer.toString( offset, 16 ).trim();
        if( (offsetMessage.length() <= 0) || (offset < 0) )  offsetMessage = "00";
        else if(offsetMessage.length() == 1) offsetMessage = "0"+offsetMessage;
        else if(offsetMessage.length() != 2) offsetMessage = "FF";        
            
        String deviationMessage = Integer.toString( deviation, 16 ).trim();
        if( (deviationMessage.length() <= 0) || (deviation < 0) )  deviationMessage = "00";
        else if(deviationMessage.length() == 1) deviationMessage = "0"+deviationMessage;
        else if(deviationMessage.length() != 2) deviationMessage = "FF";        
        
        /* NOT COMPLETE */
        
        String message = baseMessage + offsetMessage + deviationMessage;
        byte[] messageBytes = null;
        messageBytes = message.getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  messageBytes ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemBOffset( String offset ) {
    /*------------------------------------------------------------------------*/                                            
        offset = offset.trim();
        if( offset.length() <= 0 )  offset = "00";
        else if(offset.length() == 1) offset = "0"+offset;
        else if(offset.length() != 2) offset = "FF";        
        String message = ctrlHeader+ctrlKey+"FFFO" + offset;
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemBDeviation( String deviation ) {
    /*------------------------------------------------------------------------*/                                            
        deviation = deviation.trim();
        if( deviation.length() <= 0 )  deviation = "00";
        else if(deviation.length() == 1) deviation = "0"+deviation;
        else if(deviation.length() != 2) deviation = "FF";        
        String message = ctrlHeader+ctrlKey+"FFFD" + deviation;
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemBSource( String source ) {
    /*------------------------------------------------------------------------*/
        source = source.trim().toUpperCase();
        if( !source.equals("A") && !source.equals("D") ) return(null);
        String message = ctrlHeader+ctrlKey+"FFFS" + source;
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToActivateModemBScrambler() {
    /*------------------------------------------------------------------------*/
        String message = ctrlHeader+ctrlKey+"FFFB1";
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDeactivateModemBScrambler() {
    /*------------------------------------------------------------------------*/
        String message = ctrlHeader+ctrlKey+"FFFB0";
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemBRate( String rate ) {
    /*------------------------------------------------------------------------*/
        rate = rate.trim().toUpperCase();
        if( rate.length() > 1 ) rate = rate.substring(0,1);
        String message = ctrlHeader+ctrlKey+"FFFR" + rate;
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetModemBCTCSS( String CTCSSCode ) {
    /*------------------------------------------------------------------------*/                                            
        CTCSSCode = CTCSSCode.trim();
        String message = ctrlHeader+ctrlKey+"FFFC" + CTCSSCode;
        return(new UIFrame("CTRLST",1,ctrlCallsign,ctrlSSID,(byte)COMMAND_PID,
                           message.getBytes()).getBytesKISSFrame());
    }
    
    /*------------------------------------------------------------------------*/
    /** Creates the message to set the trasnmitter source modem.
     *  @param transmitterId Ident of transmitter (starts at 1)
     *  @param source Identify the source modem (A or B)
     *  @return Byte array, containing the message to send to the spacecraft  */    
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetTransmitterSource(    int transmitterId, 
                                                    String source ){
    /*------------------------------------------------------------------------*/
        byte[] message = null;                                                
        switch( transmitterId ) {
            case 1:
                message = new String(   ctrlHeader
                                      + ctrlKey
                                      + "GGG1"
                                      + source.trim() ).getBytes();
                break;
            case 2:        
                message = new String(   ctrlHeader
                                      + ctrlKey
                                      + "GGG2"
                                      + source.trim() ).getBytes();
                break;
            case -1:
            default:
                return( null );
        }
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToReadIOPortByte( String address ) {
    /*------------------------------------------------------------------------*/
                
        String baseMessage = ctrlHeader+ctrlKey+"IIIBR";
        
        //String data1 = Integer.toString( address, 16 ).trim();
        /*------------------------------------------------------*/
        /* with IO ports command, the first parameter (IO port) */
        /* must be 3 chars long                                 */
        /*------------------------------------------------------*/
        if( address.length() <= 0 )        address = "000";
        else if( address.length() == 1 )   address = "00" + address;
        else if( address.length() == 2 )   address = "0" + address;            
        else if( address.length() != 3 )   address = "";

        String message = baseMessage + address;
        byte[] messageBytes = message.getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  messageBytes ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToReadIOPortWord( String address ) {
    /*------------------------------------------------------------------------*/
                
        String baseMessage = ctrlHeader+ctrlKey+"IIIWR";
        //String data1 = Integer.toString( address, 16 ).trim();
        if( address.length() <= 0 )        address = "000";
        else if( address.length() == 1 )   address = "00" + address;
        else if( address.length() == 2 )   address = "0" + address;            
        else if( address.length() != 3 )   address = "";

        String message = baseMessage + address;
        byte[] messageBytes = message.getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  messageBytes ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToWriteIOPortByte( String address, String value ) {
    /*------------------------------------------------------------------------*/
        String baseMessage = ctrlHeader+ctrlKey+"IIIBP";
        //String data1 = Integer.toString( address, 16 ).trim();
        //String data2 = Integer.toString( value, 16 ).trim();
        /*------------------------------------------------------*/
        /* with IO ports command, the first parameter (IO port) */
        /* must be 3 chars long, the 2nd (value) 4 chars...     */
        /*------------------------------------------------------*/
        if( address.length() <= 0 )        address = "000";
        else if( address.length() == 1 )   address = "00" + address;
        else if( address.length() == 2 )   address = "0" + address;            
        else if( address.length() != 3 )   address = "";

        if( value.length() <= 0 )        value = "0000";
        else if( value.length() == 1 )   value = "000" + value;
        else if( value.length() == 2 )   value = "00" + value;            
        else if( value.length() == 3 )   value = "0" + value;
        else if( value.length() != 3 )   value = "";  //Error
        
        String message = baseMessage + address + value;
        byte[] messageBytes = message.getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  messageBytes ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToWriteIOPortWord(String address, String value) {
    /*------------------------------------------------------------------------*/
        String baseMessage = ctrlHeader+ctrlKey+"IIIWP";
        //String data1 = Integer.toString( address, 16 ).trim();
        //String data2 = Integer.toString( value, 16 ).trim();
        if( address.length() <= 0 )        address = "000";
        else if( address.length() == 1 )   address = "00" + address;
        else if( address.length() == 2 )   address = "0" + address;            
        else if( address.length() != 3 )   address = "";

        if( value.length() <= 0 )        value = "0000";
        else if( value.length() == 1 )   value = "000" + value;
        else if( value.length() == 2 )   value = "00" + value;            
        else if( value.length() == 3 )   value = "0" + value;
        else if( value.length() != 3 )   value = "";  //Error
        
        String message = baseMessage + address + value;
        byte[] messageBytes = message.getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  messageBytes ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDisableTelemetry() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"LLL3").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToPutTelemetryOnStandbye() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"LLL0").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToRequestOneShotTelemetry() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"LLL1").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetContinuousTelemetry() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"LLL2").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDeactivateTelemetryOnCommand() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"LLLN").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToActivateTelemetryOnCommand() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"LLLR").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToReadMemory(int segment, int offset) {
    /*------------------------------------------------------------------------*/
        
        String data1 = Integer.toString( segment, 16 ).trim();
        String data2 = Integer.toString( offset, 16 ).trim();
        
        if( data1.length() <= 0 )        data1 = "0000";
        else if( data1.length() == 1 )   data1 = "000" + data1;
        else if( data1.length() == 2 )   data1 = "00" + data1;            
        else if( data1.length() == 3 )   data1 = "0" + data1;
        else if( data1.length() != 3 )   data1 = "";  //Error

        if( data2.length() <= 0 )        data2 = "0000";
        else if( data2.length() == 1 )   data2 = "000" + data2;
        else if( data2.length() == 2 )   data2 = "00" + data2;            
        else if( data2.length() == 3 )   data2 = "0" + data2;
        else if( data2.length() != 3 )   data2 = "";  //Error

        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"MMMR"+data1+data2).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToReadMemory(String segment,String offset) {
    /*------------------------------------------------------------------------*/
        
        /*-------------------------------------*/
        /* HERE WE SHOULD VALIDATE THE VALUES! */
        /*-------------------------------------*/
        segment = segment.trim();
        offset = offset.trim();
        
        if( segment.length() <= 0 )        segment = "0000";
        else if( segment.length() == 1 )   segment = "000" + segment;
        else if( segment.length() == 2 )   segment = "00" + segment;            
        else if( segment.length() == 3 )   segment = "0" + segment;
        else if( segment.length() != 3 )   segment = "";  //Error

        if( offset.length() <= 0 )        offset = "0000";
        else if( offset.length() == 1 )   offset = "000" + offset;
        else if( offset.length() == 2 )   offset = "00" + offset;            
        else if( offset.length() == 3 )   offset = "0" + offset;
        else if( offset.length() != 3 )   offset = "";  //Error

        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"MMMR"+segment+offset).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToWriteMemory(int segment,int offset,int value) {
    /*------------------------------------------------------------------------*/
        String data1 = Integer.toString( segment, 16 ).trim();
        String data2 = Integer.toString( offset, 16 ).trim();
        String data3 = Integer.toString( value, 16 ).trim();
        
        if( data1.length() <= 0 )        data1 = "0000";
        else if( data1.length() == 1 )   data1 = "000" + data1;
        else if( data1.length() == 2 )   data1 = "00" + data1;            
        else if( data1.length() == 3 )   data1 = "0" + data1;
        else if( data1.length() != 3 )   data1 = "";  //Error

        if( data2.length() <= 0 )        data2 = "0000";
        else if( data2.length() == 1 )   data2 = "000" + data2;
        else if( data2.length() == 2 )   data2 = "00" + data2;            
        else if( data2.length() == 3 )   data2 = "0" + data2;
        else if( data2.length() != 3 )   data2 = "";  //Error
        
        if( data3.length() > 3 )   data3 = "";  //Error

        byte[] message = null;
        message = new String(   ctrlHeader
                              + ctrlKey
                              + "MMMP"
                              + data1
                              + data2
                              + data3 ).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToWriteMemory(String segment,String offset,String value){
    /*------------------------------------------------------------------------*/

        /*----------------------------------*/
        /* WE SHOULD VALIDATE THE VALUES!!! */
        /*----------------------------------*/
        segment = segment.trim();
        offset = offset.trim();
        value = value.trim();
        
        if( segment.length() <= 0 )        segment = "0000";
        else if( segment.length() == 1 )   segment = "000" + segment;
        else if( segment.length() == 2 )   segment = "00" + segment;            
        else if( segment.length() == 3 )   segment = "0" + segment;
        else if( segment.length() != 3 )   segment = "";  //Error

        if( offset.length() <= 0 )        offset = "0000";
        else if( offset.length() == 1 )   offset = "000" + offset;
        else if( offset.length() == 2 )   offset = "00" + offset;            
        else if( offset.length() == 3 )   offset = "0" + offset;
        else if( offset.length() != 3 )   offset = "";  //Error
        
        if( value.length() > 3 )   value = "";  //Error

        byte[] message = null;
        message = new String(   ctrlHeader
                              + ctrlKey
                              + "MMMP"
                              + segment
                              + offset
                              + value ).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToInitializeModems() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"OOO").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetHousekeepingRate(int rate) {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"PPP"+rate).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetBinaryTelemetryRate(int rate) {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"RRR1"+rate).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetASCIITelemetryRate(int rate) {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"RRR2"+rate).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetTextBroadcastRate(int rate) {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"RRR3"+rate).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /* Message max 199 chars                                                  */
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetTextBroadcast( String txt ) {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"UUU"+txt).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSendToMomentumWheel( String command ) {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"WWW"+command).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetMomentumWheelTorque( int torque ) {
    /*------------------------------------------------------------------------*/
        String dataString = Integer.toString( torque, 16 ).trim();
        if( (dataString.length() <= 0) || (torque < 0) )  dataString = "000";
        else if( dataString.length() == 1 )             dataString = "00" + dataString;
        else if( dataString.length() == 2 )             dataString = "0" + dataString;
        else if( dataString.length() != 3 )             dataString = "FFF";
        byte[] message = null;        
        message = new String(ctrlHeader+ctrlKey+"WWWB"+dataString).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetMomentumWheelSpeed( int speed ) {
    /*------------------------------------------------------------------------*/
        String dataString = Integer.toString( speed, 16 ).trim();
        if( (dataString.length() <= 0) || (speed < 0) )  dataString = "000";
        else if( dataString.length() == 1 )             dataString = "00" + dataString;
        else if( dataString.length() == 2 )             dataString = "0" + dataString;
        else if( dataString.length() != 3 )             dataString = "FFF";
        byte[] message = null;        
        message = new String(ctrlHeader+ctrlKey+"WWWA"+dataString).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToEnableMomentumWheel() {
    /*------------------------------------------------------------------------*/
        byte[] message = new String(ctrlHeader+ctrlKey+"WWWC").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDisableMomentumWheel() {
    /*------------------------------------------------------------------------*/
        byte[] message = new String(ctrlHeader+ctrlKey+"WWWO").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    
    
    /*------------------------------------------------------------------------*/
    /** Creates the message to set the trasnmitter power.
     *  @param transmitterId Ident of transmitter (starts at 1)
     *  @param level Output level of the transmitter (0-4095 for now)
     *  @return Byte array, containing the message to send to the spacecraft  */    
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetTransmitterPower(    int transmitterId, 
                                                   int level ){
    /*------------------------------------------------------------------------*/
        byte[] msg = null;
        switch( transmitterId ) {
            case 1:
                msg=new String( ctrlHeader
                              + ctrlKey
                              + "XXXA"
                              + String.valueOf(level).trim() ).getBytes();
                break;
            case 2:        
                msg=new String( ctrlHeader
                              + ctrlKey
                              + "XXXB"
                              + String.valueOf(level).trim() ).getBytes();
                break;
            case -1:
            default:
                msg=new String( ctrlHeader
                              + ctrlKey
                              + "XXX"
                              + String.valueOf(level).trim() ).getBytes();
                break;
        }
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  COMMAND_PID,
                                  msg ).getBytesKISSFrame() );
    }

    /*------------------------------------------------------------------------*/
    /** Creates the message to turn ON a transmitter. FOR NOW SUPPORTS BOTH
     *  SPACEQUEST FORMAT (WITH TIMER) OR QUICKSAT FORMAT, BUT WE'LL HAVE
     *  TO CHANGE THAT...
     *  @param transmitterId Ident of transmitter to turn ON (starts at 1)
     *  @param timer Number of minutes the transmitter should be turned ON
     *  @return Byte array, containing the message to send to the spacecraft  */    
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnTransmitterON(   int transmitterId,
                                                String timerMin        ){
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        switch( transmitterId ) {
            case 1:
                message = (ctrlHeader+ctrlKey+"YYYA").getBytes();
                break;
            case 2:        
                message = (ctrlHeader+ctrlKey+"YYYB").getBytes();
                break;
            case -1:
            default:
                message = new String(   ctrlHeader
                                      + ctrlKey
                                      + "YYY"
                                      + timerMin.trim() ).getBytes() ;
                break;
        }
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /** Creates the message to turn OFF a transmitter
     *  @param transmitterId Ident of transmitter to turn OFF (starts at 1)
     *  @param timer Number of minutes the transmitter should be turned OFF
     *  @return Byte array, containing the message to send to the spacecraft  */    
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnTransmitterOFF( int transmitterId ){
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        switch( transmitterId ) {
            case 1:
                message = (ctrlHeader+ctrlKey+"YYYa").getBytes();
                break;
            case 2:        
                message = (ctrlHeader+ctrlKey+"YYYb").getBytes();
                break;
            case -1:
            default:
                message = (ctrlHeader+ctrlKey+"YYY0").getBytes();
                break;
        }
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  COMMAND_PID,
                                  message ).getBytesKISSFrame() );

    }
    
    /*------------------------------------------------------------------------*/
    /* Use primary command for now                                            */
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnACSON() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"2221").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /* Use primary command for now                                            */
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnACSOFF() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"2220").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnPayloadPowerON() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"3331").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnPayloadPowerOFF() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"3330").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /* Count starts at 1                                                      */
    /*------------------------------------------------------------------------*/
    public byte[] messageToActivatePayloadLine( int whichLine ) {
    /*------------------------------------------------------------------------*/
        String line = null;
        switch( whichLine ) {
            case 1: line = "A"; break;
            case 2: line = "B"; break;
            case 3: line = "C"; break;
            case 4: line = "D"; break;
            case 5: line = "E"; break;
            case 6: line = "F"; break;
            case 7: line = "G"; break;
            case 8: line = "H"; break;
            default: line =" "; break; //error
        }
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"333"+line).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDeactivatePayloadLine(int whichLine) {
    /*------------------------------------------------------------------------*/
        String line = null;
        switch( whichLine ) {
            case 1: line = "a"; break;
            case 2: line = "b"; break;
            case 3: line = "c"; break;
            case 4: line = "d"; break;
            case 5: line = "e"; break;
            case 6: line = "f"; break;
            case 7: line = "g"; break;
            case 8: line = "h"; break;
            default: line =" "; break; //error
        }
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"333"+line).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnHeatersPowerON() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"444X").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToEnableOperationHeatersBank() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"444O").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDisableOperationHeatersBank() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"444o").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToEnableSurvivalHeatersBank() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"444S").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDisableSurvivalHeatersBank() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"444s").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnHeatersPowerOFF() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"444x").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToActivateHeater(int whichHeater) {
    /*------------------------------------------------------------------------*/
        String line = null;
        switch( whichHeater ) {
            case 1: line = "A"; break;
            case 2: line = "B"; break;
            case 3: line = "C"; break;
            case 4: line = "D"; break;
            case 5: line = "E"; break;
            case 6: line = "F"; break;
            case 7: line = "G"; break;
            case 8: line = "H"; break;
            default: line =" "; break; //error
        }
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"444"+line).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDeactivateHeater(int whichHeater) {
    /*------------------------------------------------------------------------*/
        String line = null;
        switch( whichHeater ) {
            case 1: line = "a"; break;
            case 2: line = "b"; break;
            case 3: line = "c"; break;
            case 4: line = "d"; break;
            case 5: line = "e"; break;
            case 6: line = "f"; break;
            case 7: line = "g"; break;
            case 8: line = "h"; break;
            default: line =" "; break; //error
        }
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"444"+line).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnPyrosPowerON() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"5551").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnPyrosPowerOFF() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"5550").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToEnablePyros() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"555X").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDisablePyros() {
    /*------------------------------------------------------------------------*/
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"555x").getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToActivatePyro(int whichPyro) {
    /*------------------------------------------------------------------------*/
        String line = null;
        switch( whichPyro ) {
            case 1: line = "A"; break;
            case 2: line = "B"; break;
            case 3: line = "C"; break;
            case 4: line = "D"; break;
            case 5: line = "E"; break;
            case 6: line = "F"; break;
            default: line =" "; break; //error
        }
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"555"+line).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToDeactivatePyro(int whichPyro) {
    /*------------------------------------------------------------------------*/
        String line = null;
        switch( whichPyro ) {
            case 1: line = "a"; break;
            case 2: line = "b"; break;
            case 3: line = "c"; break;
            case 4: line = "d"; break;
            case 5: line = "e"; break;
            case 6: line = "f"; break;
            default: line =" "; break; //error
        }
        byte[] message = null;
        message = new String(ctrlHeader+ctrlKey+"555"+line).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnTorqueRodON(String axis,int coil) {
    /*------------------------------------------------------------------------*/
        axis = axis.toUpperCase();
        if( !axis.equals("X") && !axis.equals("Y") && !axis.equals("Z") )
            axis = " ";
        String coilString = null;
        switch( coil ) {
            case 1: coilString = "A"; break;
            case 2: coilString = "B"; break;
            default:coilString = " "; break;
        }
        byte[] message = null;
        message = new String(   ctrlHeader
                              + ctrlKey
                              + "666"
                              + axis
                              + coilString
                              + "YYY" ).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToTurnTorqueRodOFF(String axis,int coil) {
    /*------------------------------------------------------------------------*/
        axis = axis.toUpperCase();
        if( !axis.equals("X") && !axis.equals("Y") && !axis.equals("Z") )
            axis = " ";
        String coilString = null;
        switch( coil ) {
            case 1: coilString = "A"; break;
            case 2: coilString = "B"; break;
            default:coilString = " "; break;
        }
        byte[] message = null;
        message = new String(   ctrlHeader
                              + ctrlKey
                              + "666"
                              + axis
                              + coilString
                              + "NNN" ).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /** Creates the message to set the torque rod power value.                */
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetTorqueRodPower(   String axis,
                                                int coil,
                                                int powerValue ) {
    /*------------------------------------------------------------------------*/

        axis = axis.toUpperCase();
        if( !axis.equals("X") && !axis.equals("Y") && !axis.equals("Z") )
            axis = " ";
        String coilString = null;
        switch( coil ) {
            case 1: coilString = "A"; break;
            case 2: coilString = "B"; break;
            default:coilString = " "; break;
        }
        String dataString = Integer.toString( powerValue, 16 ).trim();
        if( (dataString.length() <= 0) || (powerValue < 0) ) dataString = "000";
        else if( dataString.length() == 1 )      dataString = "00" + dataString;
        else if( dataString.length() == 2 )      dataString = "0" + dataString;
        else if( dataString.length() != 3 )      dataString = "FFF";
        
        byte[] message = null;
        message = new String(   ctrlHeader
                              + ctrlKey
                              + "666"
                              + axis
                              + coilString
                              + dataString ).getBytes();
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  (byte)COMMAND_PID,
                                  message ).getBytesKISSFrame() );
    }

    
    /*------------------------------------------------------------------------*/
    /*                                                                        */
    /*                     L O A D E R    P R O T O C O L                     */
    /*                                                                        */
    /*------------------------------------------------------------------------*/
    
    /*------------------------------------------------------------------------*/
    /** Creates the message used to set the time onboard the spacecraft
     *  @param timeInSecondsUTC Seconds elapsed since UTC (1970)
     *  @return Byte array, containing the message to send to the spacecraft  */
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetTime( long timeInSecondsUTC ) {
    /*------------------------------------------------------------------------*/    
        
        /*------------------------------------------*/
        /* Prepare the frame to set the time of day */
        /*------------------------------------------*/
        byte[] ldrFrame = new byte[10];
        ldrFrame[0] = 0;    
        ldrFrame[1] = 4;    //Size of data
        ldrFrame[2] = 0;    //offset msb
        ldrFrame[3] = 0;    //offset lsb
        ldrFrame[4] = 13;   //code for SET TIME
      
        /*-----------------------------------------------------*/
        /* Get the current UTC in milliseconds, divide by 1000 */
        /*-----------------------------------------------------*/
        long time = (long)(System.currentTimeMillis()/1000);
        ldrFrame[5] = (byte)(time&0xFF);
        ldrFrame[6] = (byte)((time>>8)&0xFF);
        ldrFrame[7] = (byte)((time>>16)&0xFF);
        ldrFrame[8] = (byte)((time>>24)&0xFF);
      
        /*-----------------------------------------------------*/
        /* Calculate the checksum (required for loader frames) */
        /*-----------------------------------------------------*/
        ldrFrame[9] = calculateLoaderFrameChecksum( ldrFrame, 9 );

        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
    }

    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToStartHousekeeping() {
    /*------------------------------------------------------------------------*/
        byte[] ldrFrame = new byte[6];
        ldrFrame[0] = 0;    
        ldrFrame[1] = 0;    //Size of data
        ldrFrame[2] = 0;    //offset msb
        ldrFrame[3] = 0;    //offset lsb
        ldrFrame[4] = 10;   //code for EXIT PROTO
        ldrFrame[5] = calculateLoaderFrameChecksum( ldrFrame, 5 );
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
        
    }

    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToStartLoadedTasks() {
    /*------------------------------------------------------------------------*/
        byte[] ldrFrame = new byte[6];
        ldrFrame[0] = 0;    
        ldrFrame[1] = 0;    //Size of data
        ldrFrame[2] = 0;    //offset msb
        ldrFrame[3] = 0;    //offset lsb
        ldrFrame[4] = 15;   //code for START_GOT
        ldrFrame[5] = calculateLoaderFrameChecksum( ldrFrame, 5 );
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
        
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageLoadRequest( int binSize ) {
    /*------------------------------------------------------------------------*/
        int binSizeParagraphs = binSize / 16;
        if( (binSizeParagraphs * 16) < binSize ) binSizeParagraphs++;
        byte binSizeParagraphsMsb = (byte)(binSizeParagraphs >> 8);
        byte binSizeParagraphsLsb = (byte)(binSizeParagraphs & 0x0FF);
        byte[] ldrFrame = new byte[8];
        ldrFrame[0] = 0;    
        ldrFrame[1] = 2;    //Size of data
        ldrFrame[2] = 0;    //offset msb
        ldrFrame[3] = 0;    //offset lsb
        ldrFrame[4] = 9;    //code for LOAD_REQUEST
        ldrFrame[5] = (byte)binSizeParagraphsMsb;
        ldrFrame[6] = (byte)binSizeParagraphsLsb;
        ldrFrame[7] = calculateLoaderFrameChecksum( ldrFrame, 7 );
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageExtLoadRequest( int binSize ) {
    /*------------------------------------------------------------------------*/        
        int binSizeParagraphs = binSize / 16;
        if( (binSizeParagraphs * 16) < binSize ) binSizeParagraphs++;
        byte binSizeParagraphsMsb = (byte)(binSizeParagraphs >> 8);
        byte binSizeParagraphsLsb = (byte)(binSizeParagraphs & 0x0FF);
        byte[] ldrFrame = new byte[10];
        ldrFrame[0] = 0;    
        ldrFrame[1] = 2;    //Size of data
        ldrFrame[2] = 0;    //segment msb
        ldrFrame[3] = 0;    //segment lsb
        ldrFrame[4] = 0;    //offset msb
        ldrFrame[5] = 0;    //offset lsb
        ldrFrame[6] = 9;    //code for LOAD_REQUEST
        ldrFrame[7] = (byte)binSizeParagraphsMsb;
        ldrFrame[8] = (byte)binSizeParagraphsLsb;
        ldrFrame[9] = calculateLoaderFrameChecksum( ldrFrame, 9 );
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  EXTENDED_LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageExtSetSegment( int segment ) {
    /*------------------------------------------------------------------------*/    
        byte segmentMsb = (byte)(segment>>8);
        byte segmentLsb = (byte)(segment & 0x0FF);
        byte[] ldrFrame = new byte[8];
        ldrFrame[0] = 0;    
        ldrFrame[1] = 2;    //Size of data
        ldrFrame[2] = (byte)segmentMsb;
        ldrFrame[3] = (byte)segmentLsb;
        ldrFrame[4] = 0;    //offset msb
        ldrFrame[5] = 0;    //offset lsb
        ldrFrame[6] = 2;    //code for SEGMENT_RECORD
        ldrFrame[7] = calculateLoaderFrameChecksum( ldrFrame, 7 );
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  EXTENDED_LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageSetSegment( int segment ) {
    /*------------------------------------------------------------------------*/    
        byte segmentMsb = (byte)(segment>>8);
        byte segmentLsb = (byte)(segment & 0x0FF);
        byte[] ldrFrame = new byte[8];
        ldrFrame[0] = 0;    //I do not know why this byte is here
        ldrFrame[1] = 2;    //Size of data
        ldrFrame[2] = 0;    //offset msb
        ldrFrame[3] = 0;    //offset lsb
        ldrFrame[4] = 2;    //code for SEGMENT_RECORD
        ldrFrame[5] = (byte)segmentMsb;
        ldrFrame[6] = (byte)segmentLsb;
        ldrFrame[7] = calculateLoaderFrameChecksum( ldrFrame, 7 );
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
    }

    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageExtTaskData(int segment,int offset,byte[] dataPacket) {
    /*------------------------------------------------------------------------*/    

        byte segmentMsb = (byte)((segment>>8)&0xFF);
        byte segmentLsb = (byte)(segment & 0x0FF);
        byte offsetMsb  = (byte)((offset>>8)&0xFF);
        byte offsetLsb  = (byte)(offset & 0x0FF);        

        /*------------------------------------------*/
        /* OK, here we must be VERY carefull not to */
        /* overflow our AX25 or whatever proto data */
        /* field length...                          */
        /*------------------------------------------*/
        byte[] ldrFrame = new byte[dataPacket.length+8];
        ldrFrame[0] = 0;
        ldrFrame[1] = (byte)(dataPacket.length & 0xFF);    //Size of data

        /*---------------------------*/
        /* Takes care of the segment */
        /*---------------------------*/
        if( (segmentMsb & 0x80) > 0 )
            ldrFrame[2] = (byte)(((byte)(segmentMsb&0x7F)) | 0x80 );
        else    ldrFrame[2] = (byte)segmentMsb;
        if( (segmentLsb & 0x80) > 0 )
            ldrFrame[3] = (byte)(((byte)(segmentLsb&0x7F)) | 0x80 );
        else    ldrFrame[3] = (byte)segmentLsb;

        /*--------------------------*/
        /* Takes care of the offset */
        /*--------------------------*/
        if( (offsetMsb & 0x80) > 0 )
            ldrFrame[4] = (byte)(((byte)(offsetMsb&0x7F)) | 0x80 );
        else    ldrFrame[4] = (byte)offsetMsb;
        if( (offsetLsb & 0x80) > 0 )
            ldrFrame[5] = (byte)(((byte)(offsetLsb&0x7F)) | 0x80 );
        else    ldrFrame[5] = (byte)offsetLsb;

        /*----------------------------*/
        /* Then the code and the data */
        /*----------------------------*/
        ldrFrame[6] = 0;            //code for DATA_RECORD
        for( int i=0; i<dataPacket.length; i++ ) {
            ldrFrame[i+7] = dataPacket[i];
        }
        ldrFrame[dataPacket.length+7] 
                = calculateLoaderFrameChecksum( ldrFrame, dataPacket.length+7 );
        
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  EXTENDED_LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
    }

    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageTaskData( int segment, int offset, byte[] dataPacket ) {
    /*------------------------------------------------------------------------*/    

        byte pid = (byte)0x79;

        byte offsetMsb = (byte)((offset>>8)&0xFF);
        byte offsetLsb = (byte)(offset & 0x0FF);

        byte[] ldrFrame = new byte[dataPacket.length+6];
        ldrFrame[0] = 0;    
        ldrFrame[1] = (byte)(dataPacket.length & 0xFF);    //Size of data

        if( (offsetMsb & 0x80) > 0 )
            ldrFrame[2] = (byte)(((byte)(offsetMsb&0x7F)) | 0x80 );
        else    ldrFrame[2] = (byte)offsetMsb;
        if( (offsetLsb & 0x80) > 0 )
            ldrFrame[3] = (byte)(((byte)(offsetLsb&0x7F)) | 0x80 );
        else    ldrFrame[3] = (byte)offsetLsb;

        ldrFrame[4] = 0;            //code for DATA_RECORD
        for( int i=0; i<dataPacket.length; i++ ) {
            ldrFrame[i+5] = dataPacket[i];
        }
        ldrFrame[dataPacket.length+5] = 
                calculateLoaderFrameChecksum( ldrFrame, dataPacket.length+5 );        
        
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
    
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageExtEOF() {
    /*------------------------------------------------------------------------*/    
        byte[] ldrFrame = new byte[8];
        ldrFrame[0] = 0;    
        ldrFrame[1] = 2;    //Size of data
        ldrFrame[2] = 0;    //segment msb
        ldrFrame[3] = 0;    //segment lsb                        
        ldrFrame[4] = 0;    //offset msb
        ldrFrame[5] = 0;    //offset lsb
        ldrFrame[6] = 1;    //code for EOF_RECORD
        ldrFrame[7] = calculateLoaderFrameChecksum( ldrFrame, 7 );
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  EXTENDED_LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );        
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageEOF() {
    /*------------------------------------------------------------------------*/    
        byte[] ldrFrame = new byte[6];
        ldrFrame[0] = 0;    
        ldrFrame[1] = 2;    //Size of data
        ldrFrame[2] = 0;    //offset msb
        ldrFrame[3] = 0;    //offset lsb
        ldrFrame[4] = 1;    //code for EOF_RECORD
        ldrFrame[5] = calculateLoaderFrameChecksum( ldrFrame, 5 );
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageExtCreateTask( int taskNumber ) {
    /*------------------------------------------------------------------------*/    
        int taskNumberMsb = (byte)(taskNumber>>8);
        int taskNumberLsb = (byte)(taskNumber & 0x0FF);
        byte[] ldrFrame = new byte[10];
        ldrFrame[0] = 0;    
        ldrFrame[1] = 2;    //Size of data
        ldrFrame[2] = 0;    //segment msb
        ldrFrame[3] = 0;    //segment lsb
        ldrFrame[4] = 0;    //offset msb
        ldrFrame[5] = 0;    //offset lsb
        ldrFrame[6] = 8;    //code for START TASK
        ldrFrame[7] = (byte)taskNumberMsb;
        ldrFrame[8] = (byte)taskNumberLsb;
        ldrFrame[9] = calculateLoaderFrameChecksum( ldrFrame, 9 );
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  EXTENDED_LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );        
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageCreateTask( int taskNumber ) {
    /*------------------------------------------------------------------------*/    
        byte taskNumberMsb = (byte)((taskNumber>>8)&0xFF);
        byte taskNumberLsb = (byte)(taskNumber&0xFF);
        byte[] ldrFrame = new byte[8];
        ldrFrame[0] = 0;    
        ldrFrame[1] = 2;    //Size of data
        ldrFrame[2] = 0;    //offset msb
        ldrFrame[3] = 0;    //offset lsb
        ldrFrame[4] = 8;    //code for START TASK
        ldrFrame[5] = (byte)taskNumberMsb;
        ldrFrame[6] = (byte)taskNumberLsb;
        ldrFrame[7] = calculateLoaderFrameChecksum( ldrFrame, 7 );
        return( new UIFrame( "CTRLST", 1,
                                  ctrlCallsign,ctrlSSID,
                                  LOADER_PID,
                                  ldrFrame ).getBytesKISSFrame() );
    }
    
    /*------------------------------------------------------------------------*/
    /** Calculates a simple checksum on a sequence of byte. The checksum is
     *  defined as the byte which, when added with all the bytes in the 
     *  byte sequence, will give a result of ZERO. Usefull for calculating the 
     *  checksum on the data packet for the upload protocole, for example.
     *
     * @param data Byte array from which the data sequence is taken.
     * @param length Number of bytes (from the start of the array) to take
     *               into account for calculating the checksum.
     * @return The checksum byte.                                             */
    /*------------------------------------------------------------------------*/
    private synchronized byte calculateLoaderFrameChecksum( byte[] data,
                                                            int length) {
    /*------------------------------------------------------------------------*/
        int checkSum = 0;
        int unsignedData;
        
        /*------------------------------------------------------------*/
        /* There must be a better way to play with unsigned bytes!!!! */
        /*------------------------------------------------------------*/
        for( int i=1; i<length; i++ ) {
            if( data[i] >=0 ) unsignedData = data[i];
            else unsignedData = 256+data[i];
            checkSum += unsignedData;
            if( checkSum > 255 ) checkSum -= 256;
        }
        if( checkSum > 0 ) {
            checkSum = 256 - checkSum;
            if( checkSum > 127 ) checkSum -= 256;
        }
        else {
            checkSum = 0;
        }        
        return( (byte)checkSum );
        
    }
    /*------------------------------------------------------------------------*/
    /*                                                                        */
    /*                 B O O T L O A D E R    P R O T O C O L                 */
    /*                                                                        */
    /*------------------------------------------------------------------------*/
    
    /*=SETENCRYPTIONKEYS======================================================*/
    /** Sets the encryption keys required to create the data packet sent to the
     *  bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param message data packet received from bootloader (byte[])
     * @param aval AVAL Encryption key
     * @param bval BVAL Encryption key
     * @param d3val D3VAL Encryption key
     * @param k1h K1H Encryption key
     * @param k1l K1L Encryption key
     * @since Beginning
   ===========================================================================*/       
   public void setBLEncryptionKeys(int aval,int bval,int d3val,int k1h,int k1l){
   /*=========================================================================*/
        if( aval != -1 )    AVAL    = aval;
        if( bval != -1 )    BVAL    = bval;
        if( d3val != -1 )   D3VAL   = d3val;
        if( k1h != -1 )     K1H     = k1h;
        if( k1l != -1 )     K1L     = k1l;
   }
   
    /*=SETCOMMANDCODES========================================================*/
    /** Sets the bootloader command codes.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param load Load data command code
     * @param dump Dump memory command code
     * @param exec Execute task command code
     * @param meme Memory Examine command code
     * @param memw Memory Write command code
     * @param ioe  I/O Port Examine command code
     * @param iop  I/O Port Poke command code
     * @param tlm  Command code to request a beacon with telemetry data
     * @param mov  Move kernal/pht into memory command code
     * @param cmd1 User defined command code #1
     * @param cmd2 User defined command code #2
    ==========================================================================*/       
    public void setBLCommandCodes(  int load, int dump, int exec, int meme,
                                    int memw, int ioe, int iop, int tlm,
                                    int mov, int cmd1, int cmd2 ) {
    /*========================================================================*/
        System.out.println("Setting bootloader command");
        if( mov != -1 )     blcmdMove     = mov;       //Move Kernal/PHT
        if( exec != -1 )    blcmdExecute  = exec;      //Execute loaded program
        if( tlm != -1 )     blcmdBeacon   = tlm;       //Request beacon
        if( meme != -1 )    blcmdPeekMem  = meme;      //Examine memory
        if( ioe != -1 )     blcmdPeekIO   = ioe;       //Examine IO port
        if( memw != -1 )    blcmdPokeMem  = memw;      //Write memory
        if( iop != -1 )     blcmdPokeIO   = iop;       //Write IO port
        if( mov != -1 )     blcmdDumpMem  = dump;      //Dump memory content
        if( mov != -1 )     blcmdLoad     = load;      //Load a file
        if( mov != -1 )     this.blcmd1   = cmd1;
        if( mov != -1 )     this.blcmd2   = cmd2;
    }

    /*=SETSCID================================================================*/
    /** Sets the bootloader spacecraft ID
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param scid Spacecraft ID to use
    ==========================================================================*/       
    public void setBLSpacecraftID( int scid ) {
    /*========================================================================*/
        this.BLspacecraftID = scid;
    }
    public int getBLSpacecraftID() {               
    /*========================================================================*/
        return(BLspacecraftID);
    }
    
    /*=DATAPACKET=============================================================*/
    /** Creates a data packet understandable by the bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code:<ul>
     *  <li>blcmdMove (Move Kernal/PHT)
     *  <li>blcmdExecute (Execute loaded program)
     *  <li>blcmdBeacon (Request beacon)
     *  <li>blcmdPeekMem (Examine memory)
     *  <li>blcmdPeekIO (Examine IO port)
     *  <li>blcmdPokeMem (Write memory)
     *  <li>blcmdPokeIO (Write IO port)
     *  <li>blcmdDumpMem (Dump memory content)
     *  <li>blcmdLoad (Load a file)
     *  </ul>
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/
    private byte[] dataPacket( int cmd ) {
    /*========================================================================*/    
        return createDataPacket( cmd, 0, 0, 0, 0 );
    }
    
    /*=DATAPACKET=============================================================*/
    /** Creates a data packet understandable by the bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code:<ul>
     *  <li>blcmdMove (Move Kernal/PHT)
     *  <li>blcmdExecute (Execute loaded program)
     *  <li>blcmdBeacon (Request beacon)
     *  <li>blcmdPeekMem (Examine memory)
     *  <li>blcmdPeekIO (Examine IO port)
     *  <li>blcmdPokeMem (Write memory)
     *  <li>blcmdPokeIO (Write IO port)
     *  <li>blcmdDumpMem (Dump memory content)
     *  <li>blcmdLoad (Load a file)
     *  </ul>
     * @param param1 First parameter to the command (specific to it)
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/
    private byte[] dataPacket( int cmd, int param1 ) {
    /*========================================================================*/    
        return createDataPacket( cmd, param1, 0, 0, 0 );
    }
    
    /*=DATAPACKET=============================================================*/
    /** Creates a data packet understandable by the bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code:<ul>
     *  <li>blcmdMove (Move Kernal/PHT)
     *  <li>blcmdExecute (Execute loaded program)
     *  <li>blcmdBeacon (Request beacon)
     *  <li>blcmdPeekMem (Examine memory)
     *  <li>blcmdPeekIO (Examine IO port)
     *  <li>blcmdPokeMem (Write memory)
     *  <li>blcmdPokeIO (Write IO port)
     *  <li>blcmdDumpMem (Dump memory content)
     *  <li>blcmdLoad (Load a file)
     *  </ul>
     * @param param1 First parameter to the command (specific to it)
     * @param param2 Second parameter to the command (specific to it)
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/
    private byte[] dataPacket( int cmd, int param1, int param2 ) {
    /*========================================================================*/    
        return createDataPacket( cmd, param1, param2, 0, 0 );
    }
    
    /*=DATAPACKET=============================================================*/
    /** Creates a data packet understandable by the bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code:<ul>
     *  <li>blcmdMove (Move Kernal/PHT)
     *  <li>blcmdExecute (Execute loaded program)
     *  <li>blcmdBeacon (Request beacon)
     *  <li>blcmdPeekMem (Examine memory)
     *  <li>blcmdPeekIO (Examine IO port)
     *  <li>blcmdPokeMem (Write memory)
     *  <li>blcmdPokeIO (Write IO port)
     *  <li>blcmdDumpMem (Dump memory content)
     *  <li>blcmdLoad (Load a file)
     *  </ul>
     * @param param1 First parameter to the command (specific to it)
     * @param param2 Second parameter to the command (specific to it)
     * @param param3 Third parameter to the command (specific to it)
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/
    private byte[] dataPacket( int cmd, int param1, int param2, int param3 ) {
    /*========================================================================*/    
        return createDataPacket( cmd, param1, param2, param3, 0 );
    }
    
    /*=DATAPACKET=============================================================*/
    /** Creates a data packet understandable by the bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code:<ul>
     *  <li>blcmdMove (Move Kernal/PHT)
     *  <li>blcmdExecute (Execute loaded program)
     *  <li>blcmdBeacon (Request beacon)
     *  <li>blcmdPeekMem (Examine memory)
     *  <li>blcmdPeekIO (Examine IO port)
     *  <li>blcmdPokeMem (Write memory)
     *  <li>blcmdPokeIO (Write IO port)
     *  <li>blcmdDumpMem (Dump memory content)
     *  <li>blcmdLoad (Load a file)
     *  </ul>
     * @param param1 First parameter to the command (specific to it)
     * @param param2 Second parameter to the command (specific to it)
     * @param param3 Third parameter to the command (specific to it)
     * @param param4 Fourth parameter to the command (specific to it)
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/
    private byte[] dataPacket( int cmd, int param1, int param2, int param3, int param4 ) {
    /*========================================================================*/    
        return createDataPacket( cmd, param1, param2, param3, param4 );
    }
    
    /*=CREATEDATAPACKET=======================================================*/
    /** Actually creates the data packet. Used internally only (should be private).
     *  This class is synchronized.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code
     * @param param1 First parameter to the command (specific to it)
     * @param param2 Second parameter to the command (specific to it)
     * @param param3 Third parameter to the command (specific to it)
     * @param param4 Fourth parameter to the command (specific to it)
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/    
    private byte[] createDataPacket(int cmd,int param1,int param2,int param3,int param4) {
    /*========================================================================*/    
    int[] qblWords = new int[7];
    int random;      
    
    
        System.out.println("Creating bootloader data packet with cmd = " + cmd);
        /*-----------------------------------------*/
        /* Random number! (well, almost random...) */
        /*-----------------------------------------*/
        random = 0x024;        
        
        /*----------------------------------------*/
        /* Encrypt data in an integer array first */
        /*----------------------------------------*/
        qblWords[0] = random ^ (K1H<<8);
        qblWords[1] = cmd    ^ ((qblWords[0] * AVAL) + BVAL );
        qblWords[2] = param1 ^ ((qblWords[1] * AVAL) + BVAL );
        qblWords[3] = param2 ^ ((qblWords[2] * AVAL) + BVAL );
        qblWords[4] = param3 ^ ((qblWords[3] * AVAL) + BVAL );
        qblWords[5] = param4 ^ ((qblWords[4] * AVAL) + BVAL );
        qblWords[6] = D3VAL  ^ ((qblWords[5] * AVAL) + BVAL );
      
        /*-----------------------------------------*/
        /* Then create data packet in a byte array */
        /*-----------------------------------------*/
        byte[] qblFrame = new byte[15];
        
        /*-------------------------*/
        /* First the spacecraft ID */
        /*-------------------------*/
        qblFrame[0] = (byte)BLspacecraftID;
        
        /*------------------------------------------------*/
        /* Then the command and parameters (16 bits), low */
        /* order byte first                               */
        /*------------------------------------------------*/
        for( int i=0; i<7; i++ ) {
          qblFrame[2*i+1] = (byte)(qblWords[i] & 0x00ff);
          qblFrame[2*i+2] = (byte)( (qblWords[i] & 0xff00)>>8 );
        }
      
        /*------------------------------------------------------*/
        /* Return the frame, ready to be sent to the bootloader */
        /*------------------------------------------------------*/
        return qblFrame;
      
    }

    
    /*=INTERPRETREPLY=========================================================*/
    /** Interprets a reply frame from the spacecraft's bootloader.
     *  Reply =  ABCDEF where <ul>
     *  <li> A = A (Ack) N (Nack) T (Telemetry) E (Error) D (Dump)
     *  <li> B = EDAC Counter
     *  <li> CD = Bootloader status (C = low order) = command origin
     *  <li> EF = Data
     *  </ul>
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param message data packet received from bootloader (byte[])
     * @param type (returned) The type of data received: <ul>
     *          <li> "ACK" (Acknowlegment)
     *          <li> "NACK" (Negative Acknowledgment)
     *          <li> "TELEM" (Telemetry data)
     *          <li> "ERROR" (Indication of error)
     *          <li> "UNKN" (Unknown packet received from bootloader)
     *      </ul>
     * @param EDACCounter (returned) Gives the number of error encountered
     *          on the EDAC memory since last reboot.
     * @param data (returned) Specifc to previous command sent.
     * @return String merging all returned infos
     * @since Beginning
    ==========================================================================*/       
    public String interpretBLReply( byte[] message,
                                    String type,
                                    int EDACCounter,
                                    int data ) {
    /*========================================================================*/    
        
        /*---------------------*/
        /* Some initialization */
        /*---------------------*/
        type = "";
        EDACCounter = -1;
        data = -1;
        
        /*--------------------------------------------------*/
        /* Message must be at least 6 byte long, or invalid */
        /*--------------------------------------------------*/
        if( message.length < 6 ) return "";
        
        /*--------------------------*/
        /* Switch upon command code */
        /*--------------------------*/
        switch( message[0] ) {
            case 'A':
                type = "ACK";
                break;
            case 'N':
                type = "NACK";
                break;
            case 'T':
                type = "TELEM";
                break;
            case 'E':
                type = "ERROR";
                break;
            default:
                type = "UNKN";
                break;
        }        
        
        /*------------------------*/
        /* Get EDAC error counter */
        /*------------------------*/
        EDACCounter = (int)message[1];
        
        /*----------*/
        /* Get data */
        /*----------*/
        data = (int)message[5] + (int)(message[4])*(int)256;
        
        /*--------------------------------------------------*/
        /* Return a string summarizing what was received... */
        /*--------------------------------------------------*/
        return( type + ": EDAC Error counter=" + EDACCounter + " Data=" + data + "("+message[4]+")" + "("+message[5]+")");
    }

    /*------------------------------------------------------------------------*/
    /* Ok, I'm ashamed to use a,b,c,d as variable names, but I do not know
     * what they all mean yet...                                              */
    /*------------------------------------------------------------------------*/    
    public synchronized byte[] BLmessageToExecuteTask(int a,int b,int c,int d) {
    /*------------------------------------------------------------------------*/
        Frame frame = new Frame(dataPacket(blcmdExecute,a,b,c,d));
        if( frame != null ) {
            return( frame.getBytesKISSFrame() );
        } else {
            System.out.println( "Unable to create frame to execute task" );
        }
        return( null );
        //return( dataPacket( blcmdExecute, a, b, c, d ) );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/    
    public synchronized byte[] BLmessageToRequestBeacon() {
    /*------------------------------------------------------------------------*/
        Frame frame = new Frame(dataPacket(blcmdBeacon));
        if( frame != null ) {
            return( frame.getBytesKISSFrame() );            
        } else {
            System.out.println("Unable to create frame to request beacon");
        }
        return( null );
        //return( dataPacket( blcmdBeacon ) );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/    
    public synchronized byte[] BLmessageToMoveTasksInMemory() {
    /*------------------------------------------------------------------------*/
        Frame frame = new Frame(dataPacket(blcmdMove));
        if( frame != null ) {
            return( frame.getBytesKISSFrame() );
        } else {
            System.out.println( "Unable to create frame to move task" );
        }
        return( null );
        //return( dataPacket( blcmdMove ) );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/    
    public synchronized byte[] BLmessageToWriteIOPort(int port, int value) {
    /*------------------------------------------------------------------------*/
        Frame frame = new Frame(dataPacket(blcmdPokeIO));
        if( frame != null ) {
            return( frame.getBytesKISSFrame() );
        } else {
            System.out.println("Unable to create frame to write to IO port");
        }
        return( null );
        //return( dataPacket( blcmdPokeIO, port, value ) );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/    
    public synchronized byte[] BLmessageToReadIOPort(int port) {
    /*------------------------------------------------------------------------*/
        Frame frame = new Frame(dataPacket(blcmdPeekIO));
        if( frame != null ) {
            return( frame.getBytesKISSFrame() );
        } else {
            System.out.println( "Unable to create frame to read IO port" );
        }
        return( null );
        //return( dataPacket( blcmdPeekIO, port ) );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/    
    public synchronized byte[] BLmessageToWriteMemory(int seg,int off,int val){
    /*------------------------------------------------------------------------*/
        Frame frame = new Frame(dataPacket(blcmdPokeMem,seg,off,val));
        if( frame != null ) {
            return(frame.getBytesKISSFrame());
        } else {
            System.out.println( "Unable to create frame to write to memory");
        }
        return( null );
        //return( dataPacket( blcmdPokeMem, seg, off, val ) );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/    
    public synchronized byte[] BLmessageToReadMemory(int seg,int off) {
    /*------------------------------------------------------------------------*/
        Frame frame = new Frame(dataPacket(blcmdPeekMem,seg,off));
        if( frame != null ) {
            return( frame.getBytesKISSFrame() );
        } else {
            System.out.println( "Unable to create frame to read memory" );
        }
        return( null );
        //return( dataPacket( blcmdPeekMem, seg, off ) );
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/    
    public synchronized byte[] BLmessageToDumpMemory(int seg,int off,int nb) {
    /*------------------------------------------------------------------------*/
        Frame frame = new Frame(dataPacket(blcmdDumpMem,seg,off,nb));
        if( frame != null ) {
            return( frame.getBytesKISSFrame() );
        } else {
            System.out.println( "Unable to create frame to dump memory" );
        }
        return( null );
        //return( dataPacket( blcmdDumpMem, seg, off, nb ) );
    }
    
    

}
