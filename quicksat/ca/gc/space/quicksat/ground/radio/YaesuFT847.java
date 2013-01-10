/*
 * YaesuFT847.java
 *
 * Created on April 17, 2001, 4:29 PM
 */

package ca.gc.space.quicksat.ground.radio;


import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.comm.*;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class YaesuFT847 extends Radio {

boolean isChangingRadioRXFrequency      = false;
boolean alreadyChangingRadioRXFrequency = false;
boolean isChangingRadioTXFrequency      = false;
boolean alreadyChangingRadioTXFrequency = false;

    /*------------------------------------------------------------------------*/
    /** Creates new YaesuFT847                                                */
    /*------------------------------------------------------------------------*/
    public YaesuFT847() {
    /*------------------------------------------------------------------------*/    
        System.out.println("Initializing FT847 radio");
    }

    /*------------------------------------------------------------------------*/
    /** Creates the message that is used to enable ("wakeup...") the computer 
     *  interface of the radio.                                               
     *  @return A byte array containing the message to send to the radio      */
    /*------------------------------------------------------------------------*/
    public byte[] messageToEnableComputerInterface() {
    /*------------------------------------------------------------------------*/    
        byte[] mess = { (byte)0x00,
                        (byte)0x00,
                        (byte)0x00,
                        (byte)0x00,
                        (byte)0x00};
        return( mess );
    }
    
    /*------------------------------------------------------------------------*/
    /** Creates the message used to request the current reception frequency
     *  to the radio.
     *  @return A byte array containing the message used to request the 
     *          receive frequency to the radio                                */
    /*------------------------------------------------------------------------*/
    public byte[] messageToRequestRXFrequency() {
    /*------------------------------------------------------------------------*/            
        byte[] mess = { (byte)0x00,
                        (byte)0x00,
                        (byte)0x00,
                        (byte)0x00,
                        (byte)0x13};
        return( mess );
    }

    /*------------------------------------------------------------------------*/
    /** Creates the message used to request the current transmission frequency
     *  to the radio.
     *  @return A byte array containing the message used to request the 
     *          transmission frequency to the radio                           */
    /*------------------------------------------------------------------------*/
    public byte[] messageToRequestTXFrequency() {
    /*------------------------------------------------------------------------*/    
        byte[] mess = { (byte)0x00,
                        (byte)0x00,
                        (byte)0x00,
                        (byte)0x00,
                        (byte)0x23};
        return( mess );
    }

    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToRequestRXStatus() {
    /*------------------------------------------------------------------------*/    
        byte[] mess = { (byte)0x00,
                        (byte)0x00,
                        (byte)0x00,
                        (byte)0x00,
                        (byte)0xE7};
        return( mess );
    }
        
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToRequestTXStatus() {
    /*------------------------------------------------------------------------*/    
        byte[] mess = { (byte)0x00,
                        (byte)0x00,
                        (byte)0x00,
                        (byte)0x00,
                        (byte)0xF7};
        return( mess );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetRXFrequency(  int v100M,int v10M,int v1M,
                                            int v100K,int v10K,int v1K,
                                            int v100,int v10,int v1   ) {
    /*------------------------------------------------------------------------*/                                
        byte[] dataToSend = new byte[5];
        dataToSend[0] = (byte)(((v100M << 4) | v10M) & 0xFF );
        dataToSend[1] = (byte)(((v1M << 4) | v100K) & 0xFF );
        dataToSend[2] = (byte)(((v10K << 4) | v1K) & 0xFF );
        dataToSend[3] = (byte)(((v100 << 4) | v10) & 0xFF );
        dataToSend[4] = (byte)0x11;
        return( dataToSend );
    }
    
    /*------------------------------------------------------------------------*/
    /** Set the receiver frequency
     *  @param frequency Frequency to set, in Hertz                           */
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetRXFrequency( int frequency ) {
    /*------------------------------------------------------------------------*/    
        int v100M = 0;
        int v10M  = 0;
        int v1M   = 0;
        int v100K = 0;
        int v10K  = 0;
        int v1K   = 0;
        int v100  = 0;
        int v10   = 0;
        int v1    = 0;        
        if( frequency < 0 ) {
            frequency = 0;
        } else if( frequency >= 1000000000 ) {
            frequency = 0;
        } else { 
            v100M       =  frequency / 100000000;            
            frequency   -= v100M*100000000;
            v10M        =  frequency / 10000000;
            frequency   -= v10M*10000000;
            v1M         =  frequency / 1000000;
            frequency   -= v1M*1000000;
            v100K       =  frequency / 100000;
            frequency   -= v100K*100000;
            v10K        =  frequency / 10000;
            frequency   -= v10K*10000;
            v1K         =  frequency / 1000;
            frequency   -= v1K*1000;
            v100        =  frequency / 100;
            frequency   -= v100*100;
            v10         =  frequency / 10;
            frequency   -= v10 * 10;
            v1          =  frequency;                        
        }        
        byte[] dataToSend = new byte[5];
        dataToSend[0] = (byte)(((v100M << 4) | v10M) & 0xFF );
        dataToSend[1] = (byte)(((v1M << 4) | v100K) & 0xFF );
        dataToSend[2] = (byte)(((v10K << 4) | v1K) & 0xFF );
        dataToSend[3] = (byte)(((v100 << 4) | v10) & 0xFF );
        dataToSend[4] = (byte)0x11;
        return( dataToSend );
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public byte[] messageToSetTXFrequency(  int v100M,int v10M,int v1M,
                                            int v100K,int v10K,int v1K,
                                            int v100,int v10,int v1   ) {
    /*------------------------------------------------------------------------*/                                
        byte[] dataToSend = new byte[5];
        dataToSend[0] = (byte)(((v100M << 4) | v10M) & 0xFF );
        dataToSend[1] = (byte)(((v1M << 4) | v100K) & 0xFF );
        dataToSend[2] = (byte)(((v10K << 4) | v1K) & 0xFF );
        dataToSend[3] = (byte)(((v100 << 4) | v10) & 0xFF );
        dataToSend[4] = (byte)0x21;
        return( dataToSend );
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
    int valueToSend=0;
        
            if( (mode == null) || (link == null) ) return( null );
            mode = mode.trim();
            link = link.trim();
            
            if( mode.equals("Lower Sideband") )
                valueToSend = 0x00;
            else if( mode.equals("Upper Sideband") )
                valueToSend = 0x01;
            else if( mode.equals("CW") )
                valueToSend = 0x02;
            else if( mode.equals("CW-R") )
                valueToSend = 0x03;
            else if( mode.equals("AM") )
                valueToSend = 0x04;
            else if( mode.equals("FM") )
                valueToSend = 0x08;
            else if( mode.equals("CW(N)") )
                valueToSend = 0x82;
            else if( mode.equals("CW(N)-R") )
                valueToSend = 0x83;
            else if( mode.equals("AM(N)") )
                valueToSend = 0x84;
            else if( mode.equals("FM(N)") )
                valueToSend = 0x88;
            else {
                System.out.println("Invalid Mode for Radio");
                return( null );
            }

            byte[] command = new byte[5];
            command[0] = (byte)valueToSend;
            command[1] = 0x00;
            command[2] = 0x00;
            command[3] = 0x00;
            if( link.equals( "TX" ) )
                command[4] = 0x27 ;
            else if( link.equals( "RX" ) )
                command[4] = 0x17 ;
            else {
                System.out.println("Invalid link specified for radio");
            }
            return( command );
                    
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
    int valueToSend=0;
        
        if( (freq == null) || (link == null) ) return( null );        
        freq = freq.trim();
        link = link.trim();
            
        if( freq.equals("OFF") ) {
            byte[] command = new byte[5];
            command[0] = (byte)0x8A;
            command[1] = 0x00;
            command[2] = 0x00;
            command[3] = 0x00;
            if( link.equals( "TX" ) )
                command[4] = 0x2A;
            else
                command[4] = 0x1A;            
            return( command );
        }

        if( freq.equals("67.0") )
            valueToSend = 0x3F;
        else if( freq.equals("69.3") ) 
            valueToSend = 0x39;
        else if( freq.equals("71.9") ) 
            valueToSend = 0x1F;
        else if( freq.equals("74.4") ) 
            valueToSend = 0x3E;
        else if( freq.equals("77.0") ) 
            valueToSend = 0x0F;
        else if( freq.equals("79.7") ) 
            valueToSend = 0x3D;
        else if( freq.equals("82.5") ) 
            valueToSend = 0x1E;
        else if( freq.equals("85.4") ) 
            valueToSend = 0x3C;
        else if( freq.equals("88.5") ) 
            valueToSend = 0x0E;
        else if( freq.equals("91.5") ) 
            valueToSend = 0x3B;
        else if( freq.equals("94.8") ) 
            valueToSend = 0x1D;
        else if( freq.equals("97.4") ) 
            valueToSend = 0x3A;
        else if( freq.equals("100.0") ) 
            valueToSend = 0x0D;
        else if( freq.equals("103.5") ) 
            valueToSend = 0x1C;
        else if( freq.equals("107.2") ) 
            valueToSend = 0x0C;
        else if( freq.equals("110.9") ) 
            valueToSend = 0x1B;
        else if( freq.equals("114.8") ) 
            valueToSend = 0x0B;
        else if( freq.equals("118.8") ) 
            valueToSend = 0x1A;
        else if( freq.equals("123.0") ) 
            valueToSend = 0x0A;
        else if( freq.equals("127.3") ) 
            valueToSend = 0x19;
        else if( freq.equals("131.8") ) 
            valueToSend = 0x09;
        else if( freq.equals("136.5") ) 
            valueToSend = 0x18;
        else if( freq.equals("141.3") ) 
            valueToSend = 0x08;
        else if( freq.equals("146.2") ) 
            valueToSend = 0x17;
        else if( freq.equals("151.4") ) 
            valueToSend = 0x07;
        else if( freq.equals("156.7") ) 
            valueToSend = 0x16;
        else if( freq.equals("162.2") ) 
            valueToSend = 0x06;
        else if( freq.equals("167.9") ) 
            valueToSend = 0x15;
        else if( freq.equals("173.8") ) 
            valueToSend = 0x05;
        else if( freq.equals("179.9") ) 
            valueToSend = 0x14;
        else if( freq.equals("186.2") ) 
            valueToSend = 0x04;
        else if( freq.equals("192.8") ) 
            valueToSend = 0x13;
        else if( freq.equals("203.5") ) 
            valueToSend = 0x03;
        else if( freq.equals("210.7") ) 
            valueToSend = 0x12;
        else if( freq.equals("218.1") ) 
            valueToSend = 0x02;
        else if( freq.equals("225.7") ) 
            valueToSend = 0x11;
        else if( freq.equals("233.6") ) 
            valueToSend = 0x01;
        else if( freq.equals("241.8") ) 
            valueToSend = 0x10;
        else if( freq.equals("250.3") ) 
            valueToSend = 0x00;
        else {
            System.out.println("Bad CTCSS requested");
            return( null );
        }

        byte[] command = new byte[5];
        command[0] = (byte)valueToSend;
        command[1] = 0x00;
        command[2] = 0x00;
        command[3] = 0x00;
        if( link.equals( "TX" ) )
            command[4] = 0x2B;
        else
            command[4] = 0x1B;
        return( command );
    }
        
        
        
    /*------------------------------------------------------------------------*/
    /** Creates the message used to enable/disable the CTCSS.
     *  @param link String indicating uplink or downlink ("TX" or "RX" respect.)
     *  @param enable true to enable, false to disable CTCSS
    /*------------------------------------------------------------------------*/
    public byte[] messageToEnableCTCSS( String link, boolean enable ) {
    /*------------------------------------------------------------------------*/    
            
        /*-----------------*/
        /* Set CTCSS state */
        /*-----------------*/
        byte[] command = new byte[5];
        if( enable ) {
            command[0] = 0x2A;
        } else {
            command[0] = (byte)0x8A;
        }
        command[1] = 0x00;
        command[2] = 0x00;
        command[3] = 0x00;
        if( link.equals( "TX" ) )
            command[4] = 0x2A;
        else
            command[4] = 0x1A;
        return( command );
                    
    }
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public void fillCTCSSComboBox(JComboBox combo) {
    /*------------------------------------------------------------------------*/    
        
        combo.addItem("OFF");
        combo.addItem("67.0");
        combo.addItem("69.3");
        combo.addItem("71.9");
        combo.addItem("74.4");
        combo.addItem("77.0");
        combo.addItem("79.7");
        combo.addItem("82.5");
        combo.addItem("85.4");
        combo.addItem("88.5");
        combo.addItem("91.5");
        combo.addItem("94.8");
        combo.addItem("97.4");
        combo.addItem("100.0");
        combo.addItem("103.5");
        combo.addItem("107.2");
        combo.addItem("110.9");
        combo.addItem("114.8");
        combo.addItem("118.8");
        combo.addItem("123.0");
        combo.addItem("127.3");
        combo.addItem("131.8");
        combo.addItem("136.5");
        combo.addItem("141.3");
        combo.addItem("146.2");
        combo.addItem("151.4");
        combo.addItem("156.7");
        combo.addItem("162.2");
        combo.addItem("167.9");
        combo.addItem("173.8");
        combo.addItem("179.9");
        combo.addItem("186.2");
        combo.addItem("192.8");
        combo.addItem("203.5");
        combo.addItem("210.7");
        combo.addItem("218.1");
        combo.addItem("225.7");
        combo.addItem("233.6");
        combo.addItem("241.8");
        combo.addItem("250.3");
        
    }
    

}
