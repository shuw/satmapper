/*
 * Frame.java
 *
 * Created on October 11, 2001, 2:44 PM
 */

package ca.gc.space.quicksat.ground.ax25;

import java.util.*;
import java.io.*;

/*============================================================================*/
/** This is the basic definition of an AX-25 frame. Only things that
 *  are applicable to ANY AX-25 frame are located here. All other
 *  frame definitions in this package are inherited from this class.
 *  Note on callsign: The callsign of a frame is composed of the station
 *  name and the SSID (SSSSSS-X where SSSSSS=station name and X=ssid).
 *
 * @author  jfcusson
 * @version 
 */
/*============================================================================*/
public class Frame {

public static final int KISS_FEND  = 0xC0;
public static final int HDLC_FEND  = 0x7E;
public static final int KISS_FESC  = 0xDB;
public static final int KISS_TFEND = 0xDC;
public static final int KISS_TFESC = 0xDD;
    
boolean initialized = false;
    
protected byte[] frameBytes           = null;    
protected byte[] infoBytes            = null;
protected String destAddress          = null;
protected String destCallsign         = null;
protected int    destSSID             = 0;
protected Vector repAddressVector     = null;
protected String origAddress          = null;
protected String origCallsign         = null;
protected int    origSSID             = 0;

protected int controlByte             = 0;

/*------------------------------------------------------------*/
/* PID Value                                                  */
/* Just for info (we're not doing anything with this yet...   */
/*                                                            */
/* Hex      Binary      Description of data field encoding    */
/*............................................................*/
/* 0x01     00000001    ISO 8208/CCITT X.25 PLP               */
/* 0x06     00000110    Compressed TCP/IP packet.             */
/*                      Van Jacobson (RFC 1144)               */
/* 0x07     00000111    Uncompressed TCP/IP packet.           */
/*                      Van Jacobson (RFC 1144)               */
/* 0x08     00001000    Segmentation fragment                 */
/* **       yy01yyyy    AX.25 layer 3 implemented.            */
/* **       yy10yyyy    AX.25 layer 3 implemented.            */
/* 0x79     01111001    AMSAT: Task Upload, standard protocol */
/* 0x7A     01111010    QUICKSAT: Task Upload, ext. protocol  */
/* 0xC3     11000011    TEXNET datagram protocol              */
/* 0xC4     11000100    Link Quality Protocol                 */
/* 0xCA     11001010    Appletalk                             */
/* 0xCB     11001011    Appletalk ARP                         */
/* 0xCC     11001100    ARPA Internet Protocol                */
/* 0xCD     11001101    ARPA Address resolution               */
/* 0xCE     11001110    FlexNet                               */
/* 0xCF     11001111    NET/ROM                               */
/* 0xF0     11110000    No layer 3 protocol implemented.      */
/* 0xFF     11111111    Escape character. Next octet contains */
/*                      more Level 3 protocol information.    */
/*------------------------------------------------------------*/
protected boolean isPID               = false;
protected int PID                     = 0;

protected boolean isInfo              = false;
protected int infoStartIndex          = 0;
protected int infoEndIndex            = 0;
protected boolean isCommand           = false;
protected boolean isResponse          = false;
protected final int TYPE_UNKNOWN      = 0;
protected final int TYPE_INFO         = 1;
protected final int TYPE_SUPERVISORY  = 2;
protected final int TYPE_UNNUMBERED   = 3;
protected int type                    = TYPE_UNKNOWN;

protected final int ID_UNKNOWN          = 0;
protected final int ID_INFO             = 1;
protected final int ID_RR               = 2;
protected final int ID_RNR              = 3;
protected final int ID_REJECT           = 4;
protected final int ID_SABM             = 5;
protected final int ID_DISC             = 6;
protected final int ID_DM               = 7;
protected final int ID_UA               = 8;
protected final int ID_FRMR             = 9;
protected final int ID_UI               = 10;
protected int id                        = ID_UNKNOWN;

protected boolean isPoll                = false;

protected int RXSequence                = 0;
protected int TXSequence                = 0;

/*---------------------------------------------------*/
/* If we're receiving the frame via KISS, then there */
/* is no FCS. "isFCS" indicates that...              */
/*---------------------------------------------------*/
private int FCSValue                    = 0;
private boolean isFCS                   = false;
private boolean isFCSValid              = false;

/*-------------------------------------------------------------*/
/* If we were able to build the frame correctly, set this flag */
/*-------------------------------------------------------------*/
private boolean isComplete              = false;

/*-----------------------------------------------------------------*/
/* This string contains a message describing any error encountered */
/* with this frame...                                              */
/*-----------------------------------------------------------------*/
private String errorMessage         = "";


    /*========================================================================*/
    /** Creates new Frame 
     * @param frameBytes Array of bytes containing the complete raw frame 
     * @param isFCS Indicates if the frame contains an FCS (in KISS=no FCS...)*/
    /*========================================================================*/
    public Frame() {
    /*========================================================================*/    
    }

    /*========================================================================*/
    /** Creates new Frame, without processing the raw bytes.
     * @param frameBytes Array of bytes containing the complete raw frame     */
    /*========================================================================*/
    public Frame(byte[] frameBytes) {
    /*========================================================================*/    
        //System.out.println("Creating raw frame");
        this.frameBytes = frameBytes;
    }
    
    /*========================================================================*/
    /** Creates new Frame.
     * @param frameBytes Array of bytes containing the complete raw frame 
     * @param isFCS Indicates if the frame contains an FCS (in KISS=no FCS...)*/
    /*========================================================================*/
    public Frame(byte[] frameBytes,boolean isFCS) throws InvalidFrameException {
    /*========================================================================*/    
        
        //System.out.println("Creating frame");
        this.isFCS = isFCS;
        
        /*------------------------------------------------------*/
        /* Records the buffer and throws an error if it is null */
        /*------------------------------------------------------*/
        this.frameBytes = frameBytes;
        if( frameBytes == null ) {
            errorMessage = "Internal error - Invalid frame pointer";
            throw new InvalidFrameException( errorMessage );  
        }
        
        //for( int i=0; i<frameBytes.length; i++ ){
        //    char c = (char)frameBytes[i];
            //if( Character.isLetterOrDigit(c) )
            //    System.out.print( ""+c );
            //else
        //        System.out.print( "("+(((int)frameBytes[i])&0xFF)+")" );
        //}
        //System.out.println("");
        
        /*-------------------------------------------------------------*/
        /* Check for minimal frame size (addresses + control at least) */
        /*-------------------------------------------------------------*/
        if( frameBytes.length < 15 ) {
            errorMessage = "Incomplete frame - too short";
            throw new InvalidFrameException( errorMessage );
        }
        
        /*----------------------------------------*/
        /* First check the FCS, if we have one... */
        /*----------------------------------------*/
        if( isFCS ) {
            /*------------------------------------*/
            /* FCS follows HDLC/ISO 3309 standard */
            /*------------------------------------*/
            isFCSValid = FCS.isValid( frameBytes );
        } else {
            /*--------------------------------------------------------*/
            /* No FCS: assume everything valid (what else can we do?) */
            /*--------------------------------------------------------*/            
            isFCSValid = true;
        }
        
        /*--------------------------------------------------------------*/
        /* Gets the address fields (format XXXXXX-Y where XXXXXX is the */
        /* callsign and Y the SSID                                      */
        /*--------------------------------------------------------------*/
        StringBuffer addrBuff = new StringBuffer(8);
        char c = 0;
        int ssid = 0;
        boolean isExtension = false;
        int currFieldStart;
        try {
            /*---------------------*/
            /* Destination address */
            /*---------------------*/
            //System.out.println("Gathering destination address");
            for( int i=0; i<6; i++ ) {                
                c = (char)((frameBytes[i] & 0xFF) >>> 1);
                if( c == (char)32 ) break; //break on first space
                addrBuff.append(c);
            }
            //System.out.println("Setting ssid delimiter");
            addrBuff.append('-');
            ssid = ((int)frameBytes[6] & 0x1E) >>> 1;        
            if( (ssid < 0) || (ssid > 0x0F) ) {
                errorMessage = "Internal error - bad ssid conversion";
                throw new InvalidFrameException( errorMessage );
            }
            //System.out.println("Inserting dest. ssid");
            addrBuff.append(ssid);
            destAddress = addrBuff.toString();
            //System.out.println("Dest addr: "+destAddress);
            //addrBuff.delete(0,9);
            addrBuff = new StringBuffer(8);
            
            /*---------------------------------------------------*/
            /* If the C bit is set, then this frame is a command */
            /*---------------------------------------------------*/
            if( (frameBytes[6] & 0x80) == 0x80 )
                isCommand = true;
            else
                isCommand = false;
            
            /*---------------------------------------------------------*/
            /* Repeater, or origin address, depending on extension bit */
            /*---------------------------------------------------------*/        
            SCAN_ADDITIONAL_ADDRESS_FIELDS:            
            for( currFieldStart=7;;currFieldStart+=7 ) {
                
                if( (currFieldStart+7) > frameBytes.length ) {
                    errorMessage = "Address fields overflow";                    
                    throw new InvalidFrameException( errorMessage );
                }
                    
                for( int i=0; i<6; i++ ) {
                    c = (char)((frameBytes[currFieldStart+i] & 0xFF) >>> 1);
                    if( c == (char)32 ) break; //break on first space
                    addrBuff.append(c);
                }
                addrBuff.append('-');
                ssid = ((int)frameBytes[currFieldStart+6] & 0x1E) >>> 1;        
                if( (ssid < 0) || (ssid > 0x0F) ) {
                    errorMessage = "Internal error - bad ssid conversion"; 
                    throw new InvalidFrameException( errorMessage );
                }
                //System.out.println("Inserting repeater/orig addr. ssid");
                addrBuff.append(ssid);
                
                if( (frameBytes[currFieldStart+6] & 0x01) == 0x01 ) {
                    
                    /*-----------------------------------------------*/
                    /* If the extension bit is set, then the address */
                    /* field is done. This is the origin address...  */
                    /*-----------------------------------------------*/
                    origAddress = addrBuff.toString();                    
                    //System.out.println("Orig addr: "+origAddress);
                    //addrBuff.delete(0,7);
                    addrBuff = new StringBuffer(8);
                    
                    /*----------------------------------------------------*/
                    /* If the C bit is set, then this frame is a response */
                    /*----------------------------------------------------*/
                    if( (frameBytes[currFieldStart+6] & 0x80) == 0x80 )
                        isResponse = true;
                    else
                        isResponse = false;
                    
                    break SCAN_ADDITIONAL_ADDRESS_FIELDS;
                }
                else {
                    /*---------------------------------------------------*/
                    /* If this is the first repeater address, initialize */
                    /* the repeater address vector...                    */
                    /*---------------------------------------------------*/
                    if( repAddressVector == null )
                        repAddressVector = new Vector();
                    
                    /*-------------------------------------------------*/
                    /* Add this address to the repeater address vector */
                    /*-------------------------------------------------*/
                    repAddressVector.addElement( (String)addrBuff.toString() );
                    //System.out.println("Rep. addr: "+addrBuff.toString());
                    //addrBuff.delete(0,7);
                    addrBuff = new StringBuffer(8);
                }
                
            }
            
        } catch( StringIndexOutOfBoundsException e ) {
            errorMessage = "Internal error - bad address";
            throw new InvalidFrameException( errorMessage );
        }
        
        /*------------------------------------------------------*/
        /* Now that we have our addresses, get the control byte */
        /*------------------------------------------------------*/
        currFieldStart += 7;
        if( currFieldStart >= frameBytes.length ) {
            errorMessage = "Control byte out of bounds";
            throw new InvalidFrameException( errorMessage );
        }
        controlByte = frameBytes[currFieldStart] & 0xFF;
        
        /*------------------------*/
        /* Interpret the Poll bit */
        /*------------------------*/
        if( (controlByte & 0x10) == 0x10 )
            isPoll = true;
        else
            isPoll = false;
        
        /*---------------------------------------------*/
        /* Interpret the type and id of this new frame */
        /*---------------------------------------------*/
        if( (controlByte & 0x01) == 0x00 ) {
            /*-----------------------*/
            /* This is an Info frame */
            /*-----------------------*/
            type = TYPE_INFO;
            id = ID_INFO;
            RXSequence = (int)((controlByte & 0xE0) >> 5);
            TXSequence = (int)((controlByte & 0x0E) >> 1);
        } 
        else if( (controlByte & 0x03) == 0x01 ) {
            /*-----------------------------*/
            /* This is a Supervisory frame */
            /*-----------------------------*/
            type = TYPE_SUPERVISORY;
            switch(controlByte & 0x0C) {
                case 0x00:
                    id = ID_RR;
                    break;
                case 0x04:
                    id = ID_RNR;
                    break;
                case 0x08:
                    id = ID_REJECT;
                    break;
                default:
                    id = ID_UNKNOWN;
                    break;
            }
            RXSequence = (int)((controlByte & 0xE0) >> 5);
        }
        else if( (controlByte & 0x03) == 0x03 ) {
            /*-----------------------------*/
            /* This is an Unnumbered frame */
            /*-----------------------------*/            
            type = TYPE_UNNUMBERED;
            int mode = ( ((controlByte&0xE0)>>3) | ((controlByte&0x0C)>>2) );
            switch( mode ) {
                case 0x07:
                    id = ID_SABM;
                    break;
                case 0x08:
                    id = ID_DISC;
                    break;
                case 0x03:
                    id = ID_DM;
                    break;
                case 0x0C:
                    id = ID_UA;
                    break;
                case 0x11:
                    id = ID_FRMR;
                    break;
                case 0x00:
                    id = ID_UI;
                    break;
                default:
                    id = ID_UNKNOWN;
                    break;
            }
        }
        else {
            type = TYPE_UNKNOWN;
            id = ID_UNKNOWN;
        }
        
        /*------------------------*/
        /* Get the Poll/Final bit */
        /*------------------------*/
        if( (controlByte & 0x10) == 0x10 )
            isPoll = true;
        else
            isPoll = false;
        
        /*--------------------*/
        /* Get the frame type */
        /*--------------------*/
        if( (controlByte & 0x01) == 0x00 ) {
            /*--------------------------*/
            /* This is an INFO frame... */
            /*--------------------------*/
            type = TYPE_INFO;            
        }
        else if( (controlByte & 0x03) == 0x01 ) {
            /*-------------------------------*/
            /* This is a SUPERVISORY frame...*/
            /*-------------------------------*/
            type = TYPE_SUPERVISORY;
        }
        else if( (controlByte & 0x03) == 0x03 ) {
            /*--------------------------------*/
            /* This is an UNNUMBERED frame... */
            /*--------------------------------*/
            type = TYPE_UNNUMBERED;
        }
        else {
            errorMessage = "Internal error - Bad frame type";
            throw new InvalidFrameException( errorMessage );            
        }
        
        /*-----------------------------------*/
        /* Now get the PID, if it is present */
        /*-----------------------------------*/
        currFieldStart++;
        if( currFieldStart < frameBytes.length ) {
            isPID = true;
            PID = frameBytes[currFieldStart] & 0xFF;
        }
        
        /*------------------------------------*/
        /* Now get the data, if it is present */
        /*------------------------------------*/
        currFieldStart++;
        if( isFCS ) {
            if( currFieldStart < (frameBytes.length-2) ) {
                isInfo = true;
                infoStartIndex = currFieldStart;
                infoEndIndex   = frameBytes.length-3;
            } else {
                isInfo = false;
                infoStartIndex = 0;
                infoEndIndex   = 0;
            }
        }else {
            if( currFieldStart < (frameBytes.length) ) {
                isInfo = true;
                infoStartIndex = currFieldStart;
                infoEndIndex   = frameBytes.length-1;
            } else {
                isInfo = false;
                infoStartIndex = 0;
                infoEndIndex   = 0;
            }
        }
        
        
        /*-------------------------------------------------------------------*/
        /* We completed the construction of the frame, set the validity flag */
        /*-------------------------------------------------------------------*/
        isComplete = true;
    }


    /*========================================================================*/
    /** Returns a byte array representing the frame, EXCLUDING flags at 
     *  both ends according to protocole used, and escaped char for KISS nor
     *  bit stuffing for HDLC (USE "getFrameReadyToSend" to get a byte array
     *  including flags and byte stuffing...).
    /*========================================================================*/
    public synchronized byte[] getBytes( ) throws InvalidFrameException {
    /*========================================================================*/
        
        BUILDING_FRAME_BYTES:
        if( frameBytes == null ) {
            System.out.println("Interpreting frame to return frame bytes");
            /*-------------------------------------------------------------*/
            /* If the frameBytes array is null, then it means that the     */
            /* frame parameters have been set but the structure not built. */
            /* So do it now, from all available parameters...              */
            /*-------------------------------------------------------------*/
            
            /*-------------------------------------------------------------*/
            /* The frame will be built using a byte array output stream of */
            /* initial size 274 (data(256)+address(14)+control+pid+FCS(2). */
            /*-------------------------------------------------------------*/
            ByteArrayOutputStream baos = new ByteArrayOutputStream(274);
            byte b = 0; //usefull...
            
            /*----------------------*/
            /* Write the start flag */
            /*----------------------*/
            //baos.write( (byte)0x7E );
            
            /*-----------------------------------------------------*/
            /* NOTE THAT WE DO NOT PROCESS THE REPEATER ADDRESSES!!*/
            /*-----------------------------------------------------*/

            /*-------------------------------------------------------*/
            /* Allocate the frameBytes buffer and fill the addresses */
            /*-------------------------------------------------------*/
            //frameBytes = new byte[size];
            
            /*---------------------*/
            /* Destination address */
            /*---------------------*/
//            if( frameBytes.length < 15 ) {
//                errorMessage = "Internal error - Buffer too short to build frame";
//                throw new InvalidFrameException( errorMessage );                
//            }
            int addrLength = 0;
            if( destCallsign != null )
                addrLength = destCallsign.length();
            if( addrLength > 6 )
                addrLength = 6;
            for( int i=0; i<addrLength; i++ )
                baos.write( (byte)(destCallsign.charAt(i) << 1) );
                //frameBytes[i] = (byte)(destCallsign.charAt(i) << 1);            
            for( int i=addrLength; i<6; i++ )
                baos.write( 0x20 << 1 );
                //frameBytes[i] = 0x20 << 1;
            
            //frameBytes[6] = (byte)((destSSID | 0xF) << 1);
            b = (byte)((destSSID & 0xF) << 1);
            b |= 0x60; //RR bits
            if( isCommand )
                b |= (byte)0x80;    //Set the "C" bit
                //frameBytes[6] |= (byte)0x80;    //Set the "C" bit
            baos.write( b );

            /*--------------------*/
            /* Originator address */
            /*--------------------*/
            if( origAddress != null )
                addrLength = origCallsign.length();
            if( addrLength > 6 )
                addrLength = 6;
            for( int i=0; i<addrLength; i++ )
                baos.write( (byte)(origCallsign.charAt(i) << 1) );
                //frameBytes[i+7] = (byte)(origCallsign.charAt(i) << 1);
            for( int i=addrLength; i<6; i++ )
                baos.write( 0x20 << 1 );
                //frameBytes[i+7] = 0x20 << 1;
            
            //frameBytes[13] = (byte)((origSSID | 0xF) << 1);
            b = (byte)((origSSID & 0xF) << 1);
            b |= 0x60; //RR bits
            if( isResponse )
                b |= (byte)0x80;   //Set the "C" bit
                //frameBytes[13] |= (byte)0x80;   //Set the "C" bit            
            
            /*----------------------------------------------------------*/
            /* And do not forget to "mark" the end of the address field */
            /*----------------------------------------------------------*/
            //frameBytes[13] |= 0x01;
            b |= 0x01;
            baos.write( b );

            
            /*-----------------------------------*/
            /* Now take care of the control byte */
            /*-----------------------------------*/            
            //frameBytes[14] = (byte)0;
            b = (byte)0;
            
            /*---------------------------------*/
            /* Insert the Poll bit if required */
            /*---------------------------------*/
            if( isPoll )
                b |= (byte)0x10;
                //frameBytes[14] |= (byte)0x10;
            
            if( id == ID_UNKNOWN ) {
                /*------------------------------------------------*/
                /* Hum, we have no idea what this frame is, so    */
                /* thwrow an exception because we cannot build it */
                /*------------------------------------------------*/
                //baos.close();
                errorMessage = "Unknown frame type and id";
                throw new InvalidFrameException( errorMessage );                
            }
            switch( id ) {
                /*-----------------------------------------------------*/
                /* This is an INFO frame, so insert the RX/TX sequence */
                /* number into the control byte, after having verified */
                /* that they are valid...                              */
                /*-----------------------------------------------------*/
                case ID_INFO:
                    type = TYPE_INFO;
                    if( (TXSequence > 7) || (TXSequence < 0) ) {
                        //baos.close();
                        errorMessage = "Invalid TX Sequence Number";
                        throw new InvalidFrameException( errorMessage );                
                    }
                    if( (RXSequence > 7) || (RXSequence < 0) ) {
                        //baos.close();
                        errorMessage = "Invalid RX Sequence Number";
                        throw new InvalidFrameException( errorMessage );                
                    }
                    //frameBytes[14] |= (byte)(TXSequence << 1);
                    //frameBytes[14] |= (byte)(RXSequence << 5);
                    b |= (byte)(TXSequence << 1);
                    b |= (byte)(RXSequence << 5); 
                    break;
                    
                /*------------------------------------------------*/
                /* These are Supervisory frames, so insert the RX */
                /* sequence and the function code...              */
                /*------------------------------------------------*/
                case ID_RR: //function = 00
                    type = TYPE_SUPERVISORY;
                    //frameBytes[14] |= (byte)0x01; //Identifies a Supervisory frame
                    //frameBytes[14] |= (byte)(RXSequence << 5);                    
                    b |= (byte)0x01; //Identifies a Supervisory frame
                    b |= (byte)(RXSequence << 5);                    
                    break;
                case ID_RNR: //function = 01
                    type = TYPE_SUPERVISORY;
                    //frameBytes[14] |= (byte)0x01; //Identifies a Supervisory frame
                    //frameBytes[14] |= (byte)(RXSequence << 5);
                    //frameBytes[14] |= (byte)0x04; //RNR function
                    b |= (byte)0x01; //Identifies a Supervisory frame
                    b |= (byte)(RXSequence << 5);
                    b |= (byte)0x04; //RNR function
                    break;
                case ID_REJECT: //function = 10
                    type = TYPE_SUPERVISORY;
                    //frameBytes[14] |= (byte)0x01; //Identifies a Supervisory frame
                    //frameBytes[14] |= (byte)(RXSequence << 5);
                    //frameBytes[14] |= (byte)0x08; //REJ function
                    b |= (byte)0x01; //Identifies a Supervisory frame
                    b |= (byte)(RXSequence << 5);
                    b |= (byte)0x08; //REJ function
                    break;
                
                /*-----------------------------------------------------*/    
                /* These are Unnumbered frames, so insert the modifier */
                /*-----------------------------------------------------*/
                case ID_SABM: //modifier=001P11
                    type = TYPE_UNNUMBERED;
                    //frameBytes[14] |= (byte)0x03; //Identifies an unnumbered frame
                    //frameBytes[14] |= (byte)0x2C; //Modifier for SABM
                    b |= (byte)0x03; //Identifies an unnumbered frame
                    b |= (byte)0x2C; //Modifier for SABM
                    break;
                case ID_DISC: //modifier=010P00
                    type = TYPE_UNNUMBERED;
                    //frameBytes[14] |= (byte)0x03; //Identifies an unnumbered frame
                    //frameBytes[14] |= (byte)0x40; //Modifier for DISC
                    b |= (byte)0x03; //Identifies an unnumbered frame
                    b |= (byte)0x40; //Modifier for DISC
                    break;
                case ID_DM:   //modifier=000F11
                    type = TYPE_UNNUMBERED;
                    //frameBytes[14] |= (byte)0x03; //Identifies an unnumbered frame
                    //frameBytes[14] |= (byte)0x0C; //Modifier for DM
                    b |= (byte)0x03; //Identifies an unnumbered frame
                    b |= (byte)0x0C; //Modifier for DM
                    break;
                case ID_UA:   //modifier=011F00
                    type = TYPE_UNNUMBERED;
                    //frameBytes[14] |= (byte)0x03; //Identifies an unnumbered frame
                    //frameBytes[14] |= (byte)0x60; //Modifier for UA
                    b |= (byte)0x03; //Identifies an unnumbered frame
                    b |= (byte)0x60; //Modifier for UA
                    break;
                case ID_FRMR: //modifier=100F01
                    type = TYPE_UNNUMBERED;
                    //frameBytes[14] |= (byte)0x03; //Identifies an unnumbered frame
                    //frameBytes[14] |= (byte)0x84; //Modifier for FRMR
                    b |= (byte)0x03; //Identifies an unnumbered frame
                    b |= (byte)0x84; //Modifier for FRMR
                    break;
                case ID_UI:   //modifier=000X00
                    type = TYPE_UNNUMBERED;
                    //frameBytes[14] |= (byte)0x03; //Identifies an unnumbered frame
                    b |= (byte)0x03; //Identifies an unnumbered frame                    
                    break;
                default:
                    //baos.close();
                    errorMessage = "Unknown frame id";
                    throw new InvalidFrameException( errorMessage );                                    
            }
            baos.write( b ); //control byte
            
            /*-------------------------------------------------------*/
            /* Ok, the control byte is finally set. Now take care of */
            /* the PID if required                                   */
            /*-------------------------------------------------------*/
            switch( id ) {
                case ID_INFO:
                case ID_UI:
                case ID_FRMR:
                    //if( frameBytes.length < 15 ) {
                    //    errorMessage = "Internal error - Buffer too short to build frame(2)";
                    //    throw new InvalidFrameException( errorMessage );                
                    //}                    
                    //frameBytes[15] = (byte)(PID&0xFF);
                    b = (byte)(PID&0xFF);   
                    baos.write( b ); //PID
                    break;
            }
            
            /*--------------------------------------------------------*/
            /* Then we have the Information (data) field, if required */
            /*--------------------------------------------------------*/
            switch( id ) {
                case ID_INFO:
                case ID_UI:
                case ID_FRMR:
                    if( infoBytes != null ) {
                        //if( frameBytes.length < (16+infoBytes.length) ) {
                        //    errorMessage = "Internal error - Buffer too short to build frame(3)";
                        //    throw new InvalidFrameException( errorMessage );                
                        //}                                            
                        for( int i=0; i<infoBytes.length; i++ ) {
                            baos.write( infoBytes[i] );
                            //frameBytes[16+i] = infoBytes[i];
                        }
                    }
                    break;
            }
            
            //???
            //baos.write(1);
            //baos.write(0);
            //baos.write(0);
            //baos.write(0);
            //baos.write(0);
            
            /*-----------------------------------------------*/
            /* And finally calculate the FCS, if required... */
            /*-----------------------------------------------*/
            if( isFCS ) {
                /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                /* IMPORTANT IMPORTANT IMPORTANT IMPORTANT IMPORTANT */
                /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                /* THIS IS EXPERIMENTAL, AND WAS NEVER TESTED!!!!    */
                /* SO IF YOU WANT TO CREATE A FRAME WITH FCS, THEN   */
                /* DO SOME TEST BEFORE, BECAUSE THE FCS COMPUTATION  */
                /* MAY BE WRONG OR SIMPLY THE BYTE ORDER OF THE FCS  */
                /* STORED IN THE FRAME MAY BE WRONG!!!! TEST BEFORE!!*/
                /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                //FCSValue = FCS.compute( frameBytes );
                FCSValue = FCS.compute( baos.toByteArray() );
                if( infoBytes == null ) {
                    //if( frameBytes.length < 18 ) {
                    //    errorMessage = "Internal error - Buffer too short to build frame(4)";
                    //    throw new InvalidFrameException( errorMessage );                
                    //}                                            
                    //frameBytes[16] = (byte)((FCSValue & 0xFF00) >> 8);
                    //frameBytes[17] = (byte)(FCSValue & 0x00FF);
                    b = (byte)((FCSValue & 0xFF00) >> 8);
                    baos.write( b );
                    b = (byte)(FCSValue & 0x00FF);
                    baos.write( b );
                } else {
                    //if( frameBytes.length < (18+infoBytes.length) ) {
                    //    errorMessage = "Internal error - Buffer too short to build frame(5)";
                    //    throw new InvalidFrameException( errorMessage );                
                    //}                                            
                    //frameBytes[16+infoBytes.length] = 
                    //                        (byte)((FCSValue & 0xFF00) >> 8);
                    //frameBytes[17+infoBytes.length] = (byte)(FCSValue & 0x00FF);                    
                    b = (byte)((FCSValue & 0xFF00) >> 8);
                    baos.write( b );
                    b = (byte)(FCSValue & 0x00FF);                    
                    baos.write( b );
                }
            }
            
            /*--------------------*/
            /* Frame all built!!! */
            /*--------------------*/
            frameBytes = baos.toByteArray();
            isComplete = true;
            try{baos.close();}catch(IOException ioe){}
            
        }//end if the frame was not previously built...
        
        return( frameBytes );
    }
    
    /*========================================================================*/
    /** Returns a frame in the form of a byte array including flags and byte 
     *  stuffing, ready to send using the KISS protocole                      */
    /*========================================================================*/
    public byte[] getBytesKISSFrame() {
    /*========================================================================*/    
        
        /*------------------------------*/
        /* Get the original frame bytes */
        /*------------------------------*/
        try{
            System.out.println("Getting the frame bytes before creating kiss");
            byte[] frameBytes = getBytes();
        } catch( InvalidFrameException e ) {
            return( null );
        }
        
        /*------------------------------------------------------------------*/
        /* Creates a byte array output stream to perform the transfer with  */
        /* the flags and the byte stuffing if we're with KISS. Add 20 bytes */
        /* more than the original frame length to the initial buffer size,  */
        /* to account for byte stuffing, and if there is more than that the */
        /* buffer will adjust automatically...                              */
        /*------------------------------------------------------------------*/
        ByteArrayOutputStream baos =
                            new ByteArrayOutputStream(frameBytes.length+20);
        
        /*-------------------------------------------*/
        /* Adds the starting flag, according to KISS */
        /*-------------------------------------------*/
        baos.write( KISS_FEND );
        
        /*---------------------------------------------------*/
        /* Then adds the command/port byte, specific to KISS */
        /*---------------------------------------------------*/
        baos.write( (byte)0 ); //Data
        
        /*---------------------------------------------*/
        /* Process all bytes and perform byte stuffing */
        /*---------------------------------------------*/
        for( int i=0; i<frameBytes.length; i++ ){
            if( frameBytes[i] == KISS_FEND ) {
                baos.write( KISS_FESC );
                baos.write( KISS_TFEND );
            }
            else if( frameBytes[i] == KISS_FESC ) {
                baos.write( KISS_FESC );
                baos.write( KISS_TFESC );
            }
            else {
                baos.write( frameBytes[i] );
            }
        }
        
        /*----------------------*/
        /* Adds the ending flag */
        /*----------------------*/
        baos.write( KISS_FEND );
        
        frameBytes = baos.toByteArray();
        try{baos.close();}catch(IOException ioe){}
        
        System.out.println("Returning kiss frame");
        return( frameBytes );
        
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public String getDestinationAddress( ) {
    /*========================================================================*/    
        if( destAddress == null ) {
            if( (destSSID<0) || (destSSID>0x0F) )
                destSSID = 0;
            if( destCallsign == null )
                destAddress = "N/A-"+destSSID;
            else
                destAddress = destCallsign.trim()+"-"+destSSID;
        }
        return( destAddress );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDestinationAddress( String destCallsign, int destSSID ) {
    /*========================================================================*/    
        this.destCallsign = destCallsign.trim();
        if( (destSSID<0) || (destSSID>0x0F) )
            destSSID = 0;
        this.destSSID = destSSID;
        this.destAddress = destCallsign+"-"+destSSID;
    }

    /*========================================================================*/
    /*========================================================================*/
    public Vector getRepeaterAddresses( ) {
    /*========================================================================*/    
        return( repAddressVector );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public String getOriginatorAddress( ) {
    /*========================================================================*/    
        if( origAddress == null ) {
            if( (origSSID<0) || (origSSID>0x0F) )
                origSSID = 0;
            if( origCallsign == null )
                origAddress = "N/A-"+origSSID;
            else
                origAddress = origCallsign.trim()+"-"+origSSID;
        }
        return( origAddress );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setOriginatorAddress( String origCallsign, int origSSID ) {
    /*========================================================================*/    
        this.origCallsign = origCallsign.trim();
        if( (origSSID<0) || (origSSID>0x0F) )
            origSSID = 0;
        this.origSSID = origSSID;
        this.origAddress = origCallsign+"-"+origSSID;
    }

    /*========================================================================*/
    /*========================================================================*/
    public int getControlByte( ) {
    /*========================================================================*/    
        return( controlByte );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public void setID( int id ) {
    /*========================================================================*/    
        switch( id ) {
            case ID_INFO:
            case ID_RR:
            case ID_RNR:
            case ID_REJECT:
            case ID_SABM:
            case ID_DISC:
            case ID_DM:
            case ID_UA:
            case ID_FRMR:
            case ID_UI:
                this.id = id;
                break;
            default:
                this.id = ID_UNKNOWN;
                break;
        }
    }
    
    /*========================================================================*/
    /** Tells us if the frame is an INFO frame.
     * @return true if the frame is an INFO frame, false otherwise
    /*========================================================================*/
    public boolean isINFO() {
    /*========================================================================*/
        return( (type==ID_INFO)?true:false );
    }

    /*========================================================================*/
    /** Tells us if the frame is a RECEIVE READY (RR) frame.
     * @return true if the frame is a RECEIVE READY (RR) frame, false otherwise
    /*========================================================================*/
    public boolean isRR() {
    /*========================================================================*/
        return( (type==ID_RR)?true:false );
    }

    /*========================================================================*/
    /** Tells us if the frame is a RECEIVE NOT READY (RNR) frame.
     * @return true if the frame is a RECEIVE NOT READY (RNR), false otherwise
    /*========================================================================*/
    public boolean isRNR() {
    /*========================================================================*/
        return( (type==ID_RNR)?true:false );
    }

    /*========================================================================*/
    /** Tells us if the frame is a REJECT (REJ) frame. Same as isREJ().
     * @return true if the frame is a REJECT (REJ), false otherwise
    /*========================================================================*/
    public boolean isREJECT() {
    /*========================================================================*/
        return( (type==ID_REJECT)?true:false );
    }
    
    /*========================================================================*/
    /** Tells us if the frame is a REJECT (REJ) frame. Same as isREJECT().
     * @return true if the frame is a REJECT (REJ), false otherwise
    /*========================================================================*/
    public boolean isREJ() {
    /*========================================================================*/
        return( (type==ID_REJECT)?true:false );
    }
    
    /*========================================================================*/
    /** Tells us if the frame is a Set Async Balance Mode (SABM) frame.
     * @return true if the frame is a SABM, false otherwise
    /*========================================================================*/
    public boolean isSABM() {
    /*========================================================================*/
        return( (type==ID_SABM)?true:false );
    }
    
    /*========================================================================*/
    /** Tells us if the frame is a DISCONNECT (DISC) frame.
     * @return true if the frame is a DISCONNECT (DIS), false otherwise
    /*========================================================================*/
    public boolean isDISC() {
    /*========================================================================*/
        return( (type==ID_DISC)?true:false );
    }

    /*========================================================================*/
    /** Tells us if the frame is a DISCONNECT MODE (DM) frame.
     * @return true if the frame is a DISCONNECT MODE (DM), false otherwise
    /*========================================================================*/
    public boolean isDM() {
    /*========================================================================*/
        return( (type==ID_DM)?true:false );
    }

    /*========================================================================*/
    /** Tells us if the frame is a UNNUMBERED ACKNOWLEDGEMENT (UA) frame.
     * @return true if the frame is a UA, false otherwise
    /*========================================================================*/
    public boolean isUA() {
    /*========================================================================*/
        return( (type==ID_UA)?true:false );
    }

    /*========================================================================*/
    /** Tells us if the frame is a FRAME REJECT (FRMR) frame.
     * @return true if the frame is a FRAME REJECT (FRMR), false otherwise
    /*========================================================================*/
    public boolean isFRMR() {
    /*========================================================================*/
        return( (type==ID_FRMR)?true:false );
    }
    
    /*========================================================================*/
    /** Tells us if the frame is a UNNUMBERED INFO (UI) frame.
     * @return true if the frame is a UNNUMBERED INFO (UI), false otherwise
    /*========================================================================*/
    public boolean isUI() {
    /*========================================================================*/
        return( (type==ID_UI)?true:false );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public boolean isPIDPresent( ) {
    /*========================================================================*/    
        return( isPID );
    }

    /*========================================================================*/
    /*========================================================================*/
    public int getPID( ) {
    /*========================================================================*/    
        return( PID );
    }

    /*========================================================================*/    
    /** Defines the Protocole ID. See header for legal values.
     *  @param PID Protocole ID.                                              */
    /*========================================================================*/    
    public void setPID( int PID ) {
    /*========================================================================*/        
        this.PID = PID;
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public boolean isInfoPresent( ) {
    /*========================================================================*/    
        return( isInfo );
    }

    /*========================================================================*/
    /*========================================================================*/
    public byte[] getInfoBytes( ) {
    /*========================================================================*/    
        int infoSize = infoEndIndex - infoStartIndex + 1;
        if( isInfo && (infoSize > 0) ) {
            byte[] infoBytes = new byte[infoSize];
            for( int i=0; i<infoSize; i++ )
                infoBytes[i] = frameBytes[i+infoStartIndex];
            return( infoBytes );
        } else {
            return( null );
        }
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public void setInfoBytes( byte[] infoBytes ) {
    /*========================================================================*/    
        this.infoBytes = infoBytes;
    }    

    /*========================================================================*/
    /*========================================================================*/
    public boolean isCommand( ) {
    /*========================================================================*/    
        return( isCommand );
    }

    /*========================================================================*/
    /*========================================================================*/
    public void setCommand( boolean isCommand ) {
    /*========================================================================*/        
        this.isCommand = isCommand;
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public boolean isResponse( ) {
    /*========================================================================*/    
        return( isResponse );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setResponse( boolean isResponse ) {
    /*========================================================================*/        
        this.isResponse = isResponse;
    }

    /*========================================================================*/
    /** Returns the type of this frame.
     * @return One of the following: <ul>
     *          <li> TYPE_UNKNOWN </li>
     *          <li> TYPE_INFO </li>
     *          <li> TYPE_SUPERVISORY </li>
     *          <li> TYPE_UNNUMBERED </li>
     *          </ul>
    /*========================================================================*/
    public int getType( ) {
    /*========================================================================*/    
        return( type );
    }
    
    /*========================================================================*/    
    /*========================================================================*/
    public boolean isPoll( ) {
    /*========================================================================*/    
        return( isPoll );
    }
    /*========================================================================*/    
    /*========================================================================*/
    public void setPoll( ) {
    /*========================================================================*/    
        isPoll = true;
    }

   
    /*========================================================================*/    
    /*========================================================================*/
    public boolean isFinal( ) {
    /*========================================================================*/    
        return( !isPoll );
    }
    
    /*========================================================================*/    
    /*========================================================================*/
    public void setFinal( ) {
    /*========================================================================*/    
        isPoll = false;
    }
    
    /*========================================================================*/    
    /*========================================================================*/
    public boolean isFCSPresent( ) {
    /*========================================================================*/    
        return( isFCS );
    }
    
    /*========================================================================*/    
    /*========================================================================*/
    public void setFCSPresent( boolean isFCS ) {
    /*========================================================================*/    
        this.isFCS = isFCS;
    }

    
    /*========================================================================*/    
    /*========================================================================*/
    public boolean isFCSValid( ) {
    /*========================================================================*/    
        return( isFCSValid );
    }
    
    /*========================================================================*/    
    /*========================================================================*/
    public boolean isComplete( ) {
    /*========================================================================*/    
        return( isComplete );
    }
    
    /*========================================================================*/    
    /** Returns a string containing information of general interest relative 
     *  to this frame. 
     *  Format: 
     * @return A string of format "[orig address]:[dest address] **Msg**> Data" 
     *         where Msg is there only if something weird about the frame...  */
    /*========================================================================*/
    public String toString() {
    /*========================================================================*/
        String str = null;
        /*-----------------------------*/
        /* Process originator address  */
        /*-----------------------------*/
        String addr = getOriginatorAddress();
        if( addr == null )
            str = "[N/A]:";
        else
            str = "["+addr+"]:";
        /*-----------------------------*/
        /* Process destination address */
        /*-----------------------------*/
        addr = getDestinationAddress();
        if( addr == null )
            str += "[N/A] ";
        else
            str += "["+addr+"] ";
        /*---------------------------*/
        /* Process any error message */
        /*---------------------------*/
        if( isFCSPresent() && !isFCSValid() )
            str += "**Bad FCS** ";
        if( !isComplete() )
            str += "**Incomplete** ";
        
        str += errorMessage;
        
        /*------------------------------------------------*/
        /* If this frame is not an Info or UI frame, then */
        /* identify it...                                 */
        /*------------------------------------------------*/        
        switch( id ) {            
            case ID_INFO:
            case ID_UI:                
                str += " > " + getPrintableInfo(); 
                break;
            case ID_RR:
                str += "{RR}";
                break;
            case ID_RNR:
                str += "{RNR}";
                break;
            case ID_REJECT:
                str += "{REJECT}";
                break;
            case ID_SABM:
                str += "{SABM}";
                break;
            case ID_DISC:
                str += "{DISC}";
                break;
            case ID_DM:
                str += "{DM}";
                break;
            case ID_UA:
                str += "{UA}";
                break;
            case ID_FRMR:
                str += "{FRMR}";
                str += " > " + getPrintableInfo(); 
                break;
            default:
                str += "{UNKNOWN TYPE}";
                break;
        }
                
        return( str );
    }
    
    /*========================================================================*/
    /** Returns the Info field, all in printable format (non-printable bytes
     *  re-formatted as "(integer value)"
     * @return A string containing the info field in printable format, or [N/A]
     *         if the info field is not present...                            */
    /*========================================================================*/     
    public String getPrintableInfo() {
    /*========================================================================*/    
        byte[] data = getInfoBytes();
        String str = "";
        if( data == null )
            str = "[N/A]";
        else {
            char c = 0;
            for( int i=0; i<data.length; i++ ) {
                c = (char)(data[i]&0xFF);
                /*--------------------------------------------------------*/
                /* Keep only printable characters as is, otherwise insert */
                /* the value of the byte between parenthesis              */
                /*--------------------------------------------------------*/
                if( (c>=0x20) && (c<0x7F) ) 
                    str += c;
                else 
                    str += "(" + (int)(data[i]&0xFF) + ")";
            }            
        }
        return( str );
    }

}
