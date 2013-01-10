/*
 * FrameInputStream.java
 *
 * Created on October 11, 2001, 4:37 PM
 */

package ca.gc.space.quicksat.ground.ax25;
import java.util.*;
import java.io.*;

/*============================================================================*/
/** Defines an AX-25 frame input stream. This input stream uses the concept
 *  of "reservation" of frames with specific destination, meaning that once
 *  frames with destination callsign XXXX are reserved, then they will not
 *  be readable by the generic read() method, only by read(XXXX). This is
 *  very usefull for specific process to read their frames, and another one
 *  finally reading "everything else".
 *  This stream supports either HDLC (without bit stuffing) or KISS. KISS is
 *  the default mode.
 *
 * @author  jfcusson
 * @version 
                                                                              */
/*============================================================================*/
public class FrameInputStream {

private int nbFramesInError     = 0;
private int nbBytesEliminated   = 0;
private int nbFramesReceived    = 0;

/*-------------------------------------------------------------*/
/* According to the standard, here is the size of each fields: */
/* Address = from 17 to 70 bytes                               */
/* Control = 1                                                 */
/* PID     = 1                                                 */
/* Info    = 256                                               */
/* FCS     = 2                                                 */
/* TOTAL MAX = 330 bytes                                       */
/*-------------------------------------------------------------*/
private int FRAME_CONSTRUCTION_BUFFER_SIZE = 330;

private final int KISS_FEND  = 0xC0;
private final int HDLC_FEND  = 0x7E;
private final int KISS_FESC  = 0xDB;
private final int KISS_TFEND = 0xDC;
private final int KISS_TFESC = 0xDD;
private int FLAG = KISS_FEND;    //Default = KISS;

private Vector frameVector              = null;
private Vector reservedAddressesVector  = null;
private Vector vectorUnformattedData    = null;
private final int MAX_FRAME_VECTOR_SIZE                 = 200;
private final int NB_FRAMES_TO_CLEAR_WHEN_VECTOR_FULL   = 10;
private final int MAX_RESERVED_VECTOR_SIZE              = 100;
private final int NB_RESERVED_TO_CLEAR_WHEN_VECTOR_FULL = 10;
private final int MAX_UNFORMATTED_DATA_BLOCK_HISTORY    = 100;

private InputStream is = null;

public final int KISS = 0;
public final int HDLC = 1;
private int level2 = KISS; //By default, assume KISS

/*---------------------------------------------------------------------------*/
/* Read modes:                                                               */
/* UNFORMATTED_READ: No interpretation of protocole. This is to be           */
/* used when communicating with the TNC on the spacecraft link, or for debug */
/*---------------------------------------------------------------------------*/
private final int NO_FLAG_RECEIVED              = 0;
private final int FIRST_BYTE_OF_NEW_FRAME       = 1;
private final int BUILDING_FRAME                = 2;
private final int BUILDING_FRAME_ESCAPED_CHAR   = 3;
private final int COMPLETED_FRAME               = 4;
private final int UNFORMATTED_READ              = 5;
private int readState = NO_FLAG_RECEIVED;

ByteArrayOutputStream frameInConstruction = null;
    
    /*========================================================================*/
    /** Creates new FrameInputStream                                          */
    /*========================================================================*/
    public FrameInputStream( InputStream is ) {
    /*========================================================================*/    
        
        if( is == null ) 
            System.out.println("Inputstream used to setup frameinputstream is null");
        this.is = is;        
        
        frameVector = new Vector();
        if( frameVector == null ) 
            System.out.println("FrameInputStream: Unable to initialize framevector");
        
        reservedAddressesVector = new Vector();
        if( reservedAddressesVector == null ) 
            System.out.println("FrameInputStream: Unable to initialize reservedAddressesVector");

        vectorUnformattedData = new Vector();
        if( vectorUnformattedData == null ) 
            System.out.println("FrameInputStream: Unable to initialize vectorUnformattedData");
        
        frameInConstruction = 
                    new ByteArrayOutputStream(FRAME_CONSTRUCTION_BUFFER_SIZE);
        if( frameInConstruction == null ) 
            System.out.println("FrameInputStream: Unable to initialize frameInConstruction");
        
        
    }
    
    /*========================================================================*/
    /** Tells the stream to get the AX25 frames encapsulated in KISS          */
    /*========================================================================*/
    public void useKISS() {
    /*========================================================================*/    
        level2 = KISS;
        FLAG = 0xC0;
    }
    
    /*========================================================================*/
    /** Tells the stream that AX25 frames are in standard HDLC format, not 
      * KISS                                                                  */
    /*========================================================================*/
    public void useHDLC() {
    /*========================================================================*/    
        level2 = HDLC;
        FLAG = 0x7E;
    }        
    
    /*========================================================================*/
    /** Set the "unformatted mode" ON/OFF. When in unformatted mode, there will
     *  be no interpretation of any protocole, everything will be stored in the
     *  unformatted data queue. This data can then be read using the 
     *  readUnformattedData() function.
     *  @param mode true or false, to turn this mode ON or OFF.               */
    /*========================================================================*/
    public void setUnformattedMode( boolean mode ) {
    /*========================================================================*/    
        if( mode ) {
            readState = UNFORMATTED_READ;
        } else {
            readState = NO_FLAG_RECEIVED;
        }
    }
    
    /*========================================================================*/
    /** Tells us if the stream is in unformatted mode or not.
     *  @return true if the stream is in unformatted mode, false otherwise.   */
    /*========================================================================*/
    public boolean isUnformattedMode() {
    /*========================================================================*/    
        if( readState == UNFORMATTED_READ )
            return( true );
        else
            return( false );
    }
    
    /*========================================================================*/
    /** Tells us if the internal stream giving us raw data is set.
     *  @return true if the internal stream is ok, false otherwise            */
    /*========================================================================*/
    public boolean isConnected() {
    /*========================================================================*/        
        if( is == null )
            return( false );
        else
            return( true );
    }
    
    /*========================================================================*/
    /** Reserve frames destined for a specific address, so that they
     *  will not be read with a normal "read()" method. DO NOT FORGET
     *  to unreserve your destination address once you will not process these
     *  anymore, since the frames will fill up the buffer...
     * @param destAddr The destination address which will be reserved.        */
    /*========================================================================*/
    public synchronized void reserve( String destAddr ) {
    /*========================================================================*/
        
        /*----------------------------------------------------------------*/
        /* Note that it could be a good idea to verify if this address is */
        /* already reserved and not reserve it twice, but it has an       */
        /* implication: if two threads reserve the same address then the  */
        /* first one to unreserve would unreserve for both...             */
        /*----------------------------------------------------------------*/
        reservedAddressesVector.addElement( (String) destAddr );
        return;
    }
    
    /*========================================================================*/
    /** unreserve frames destined for a specific address, so that they will be
     *  read with a standard "read()" method (except if the reservation was 
     *  done multiple times, and then an equal number of "unreserve" has to
     *  be issued before the "read()" can return them...)
     * @param destAddr The destination address which will be unreserved. Note
     *                 that there is no error message nor exception if the  
     *                 address to unreserve is not already reserved.          */
    /*========================================================================*/
    public synchronized void unreserve( String destAddr ) {
    /*========================================================================*/
        String addr = null;
        boolean found = false;
        for( int i=0; i<reservedAddressesVector.size(); i++ ) {
            addr = (String)reservedAddressesVector.elementAt(i);
            if( addr != null )
                if( addr.equals(destAddr) ) {
                    found = true;
                    break;
                }
        }
        if( found && (addr!=null) )
            reservedAddressesVector.removeElement( addr );
    }

    /*========================================================================*/
    /** Remove from buffer all frames which destination address matches the 
     *  one specified here.
     * @param destAddr The destination address used to filter all frames that
     *                 we want removed from the buffer.                       */
    /*========================================================================*/
    public synchronized void removeAll( String destAddr ) {
    /*========================================================================*/
        String addr = null;
        Frame frame = null;
        for( int i=0; i<frameVector.size(); i++ ) {
            frame = (Frame)frameVector.elementAt(i);
            if( frame.getDestinationAddress().equals(destAddr) ) 
                frameVector.removeElement( frame );
        }
    }
    
    
    /*========================================================================*/
    /** Reads the oldest NON-RESERVED complete frame from the frame input 
     *  stream. If no complete frame is available, return null. 
     *  NOTE: Make sure that somebody is calling parseInputStream periodically,
     *  otherwise there will be nothing on the stream!
     * @return Oldest NON_RESERVED frame in the stream                        */
    /*========================================================================*/
    public synchronized Frame read() {
    /*========================================================================*/    
        
        /*----------------------------------------------------*/
        /* Prepare to examine all frames accumulated into the */
        /* frame vector, to find the first one which is not   */
        /* reserved.                                          */
        /*----------------------------------------------------*/
        boolean thisFrameIsReserved = false;
        if( frameVector == null ) {
            //System.out.println("ERROR: Frame vector is null");
            return( null );
        }
        int nbFramesWaiting = frameVector.size();
        if( nbFramesWaiting <= 0 ) return( null );
        Frame frame = null;
        FIND_FIRST_NON_RESERVED_FRAME:
        for( int i=0; i<nbFramesWaiting; i++ ) {
            
            /*-----------------------------------*/
            /* Get a frame from the frame vector */
            /* If null, then quit this...        */
            /*-----------------------------------*/
            frame = (Frame)frameVector.elementAt( i );
            if( frame == null ) break FIND_FIRST_NON_RESERVED_FRAME;
            
            /*-------------------------------------------*/
            /* Get its callsign, and prepare to look in  */
            /* the list of reserved callsigns to compare */
            /* it to.                                    */
            /*-------------------------------------------*/
            String address = frame.getDestinationAddress();
            if( address == null ) address = "";
            thisFrameIsReserved = false;
            if( reservedAddressesVector == null ) {
                System.out.println("ERROR: reservedAddressesVector is null");
                return( null );
            }
            int nbReservedAddresses = reservedAddressesVector.size();
            CHECK_FOR_RESERVED_ADDRESS:
            for( int j=0; j<nbReservedAddresses; j++ ) {
                
                /*----------------------------------------------*/
                /* Get reserved callsign, and compare it to the */
                /* callsign of the current frame. If it is same,*/
                /* then we know we cannot release this frame so */
                /* exit this loop and go look for another one.  */
                /*----------------------------------------------*/
                String resAddress = (String)reservedAddressesVector.elementAt(j);
                if( address.equals( resAddress ) ) {
                    thisFrameIsReserved = true;
                    break CHECK_FOR_RESERVED_ADDRESS;
                }
            }//CHECK_FOR_RESERVED_ADDRESS            
            
            /*---------------------------------------------------------*/
            /* If, after checking, we found that the current frame     */
            /* is not reserved, then we found what we were looking for */
            /*---------------------------------------------------------*/
            if( !thisFrameIsReserved ) 
                break FIND_FIRST_NON_RESERVED_FRAME;
            
        }//FIND_FIRST_NON_RESERVED_FRAME
        
        /*----------------------------------------------------------*/
        /* After all verifications, if there is no frame waiting or */
        /* all waiting frames are reserved, then return null...     */
        /* Otherwise return the first non-reserved frame and remove */
        /* it from the list of waiting frames...                    */
        /*----------------------------------------------------------*/
        if( (frame == null) || thisFrameIsReserved ) {
            return( null );
        }
        else {
            frameVector.removeElement( frame );
            return( frame );
        }
                
    }

    
    /*========================================================================*/
    /** Reads a complete frame (destined to the specified callsign) from the
     *  frame intput stream. If no complete frame is available, return null.
     *  NOTE: Make sure that somebody is calling parseInputStream periodically,
     *  otherwise there will be nothing on the stream!
     * @param destCallsign Callsign filtering frames to read.
     * @return Oldest frame in the stream which destination callsign matches  */
    /*========================================================================*/
    public synchronized Frame readFrameDestinedTo( String destAddress ) {
    /*========================================================================*/    

        
        /*----------------------------------------------------*/
        /* Prepare to examine all frames accumulated into the */
        /* frame vector, to find the first one which matches  */
        /*----------------------------------------------------*/
        boolean thisFrameMatches = false;
        int nbFramesWaiting = frameVector.size();
        Frame frame = null;
        FIND_FIRST_MATCHING_FRAME:
        for( int i=0; i<nbFramesWaiting; i++ ) {
            
            /*-----------------------------------*/
            /* Get a frame from the frame vector */
            /* If null, then quit this...        */
            /*-----------------------------------*/
            frame = (Frame)frameVector.elementAt( i );
            if( frame == null ) break FIND_FIRST_MATCHING_FRAME;
            
            /*-------------------------------------------*/
            /* Get its callsign, and compare it to the   */
            /* one we want...                            */
            /*-------------------------------------------*/
            if( frame.getDestinationAddress().equals( destAddress ) ) {
                thisFrameMatches = true;
                break FIND_FIRST_MATCHING_FRAME;
            }
            
        }//FIND_FIRST_MATCHING_FRAME
        
        /*----------------------------------------------------------*/
        /* After all verifications, if there is no frame waiting or */
        /* no waiting frames are destined for the specified callsign*/
        /* return null, otherwise return the matching frame after   */
        /* having removed it from the waiting frames list.          */
        /*----------------------------------------------------------*/
        if( (frame == null) || !thisFrameMatches ) {
            return( null );
        }
        else {
            frameVector.removeElement( frame );
            return( frame );
        }                
    }

    /*========================================================================*/
    /** Reads a data block from the history of unformatted data blocks (data
     *  received that could not be identified as part of a legal frame). This
     *  data can be junk, part of a corrupted frame, or character communication
     *  with the TNC.
     * @return A byte array corresponding to one unformatted data block       */
    /*========================================================================*/
    public synchronized byte[] readUnformattedData() {
    /*========================================================================*/    
        if( vectorUnformattedData.size() <= 0 ) {
            return( null );            
        } else {
            byte[] dataBlock = (byte[])vectorUnformattedData.firstElement();
            vectorUnformattedData.remove( dataBlock );
            return( dataBlock );
        }
    }
    
    /*========================================================================*/
    /** Closes the frame input stream by freeing used resources.
     *  NOTE: Make sure that somebody is calling parseInputStream periodically,
     *  otherwise there will be nothing on the stream!                        */
    /*========================================================================*/
    public synchronized void close() {
    /*========================================================================*/    

        /*--------------------------------------*/
        /* Free all vectors used in this stream */
        /*--------------------------------------*/
        try{
            System.out.println("CLOSING FRAME INPUT STREAM");
            frameVector.clear();
            reservedAddressesVector.clear();
        }
        catch( UnsupportedOperationException uoe) {
            System.out.println("ERROR closing frame input stream: "+uoe);
        }
        
        vectorUnformattedData.removeAllElements();
        
        /*-------------------------------*/
        /* Forget about the input stream */
        /*-------------------------------*/
        is = null;
        
        return;
        
    }
    
    /*========================================================================*/
    /** Returns the number of frames available on the stream.
     *  NOTE: Make sure that somebody is calling parseInputStream periodically,
     *  otherwise there will be nothing on the stream!
     * @return The number of available frames on the stream (valid or not...) */
    /*========================================================================*/
    public int available() {
    /*========================================================================*/    

        /*---------------------------------------------------------*/
        /* Then return the size of the frame vector containing the */
        /* list of waiting frames...                               */
        /*---------------------------------------------------------*/
        return( frameVector.size() );
    }

    
    /*========================================================================*/
    /** Returns the number of frames available on the stream, destined for the
     *  specified callsign.
     *  NOTE: Make sure that somebody is calling parseInputStream periodically,
     *  otherwise there will be nothing on the stream!
     * @param destCallsign Callsign filtering frames to check.
     * @return The number of available frames on the stream (valid or not...) */
    /*========================================================================*/
    public int available( String destAddress ) {
    /*========================================================================*/    

        /*----------------------------------------------------*/
        /* Prepare to examine all frames accumulated into the */
        /* frame vector, to find all that match               */
        /*----------------------------------------------------*/
        int nbFramesMatching = 0;
        int nbFramesWaiting = frameVector.size();
        Frame frame = null;
        FIND_MATCHING_FRAMES:
        for( int i=0; i<nbFramesWaiting; i++ ) {
            
            /*-----------------------------------*/
            /* Get a frame from the frame vector */
            /* If null, then quit this...        */
            /*-----------------------------------*/
            frame = (Frame)frameVector.elementAt( i );
            if( frame == null ) break FIND_MATCHING_FRAMES;
            
            /*-------------------------------------------*/
            /* Get its callsign, and compare it to the   */
            /* one we want...                            */
            /*-------------------------------------------*/
            if( frame.getDestinationAddress().equals( destAddress ) ) {
                nbFramesMatching++;
            }
            
        }//FIND_MATCHING_FRAMES
        
        /*-----------------------------------------------------*/
        /* Returns the number of matching frames that we found */
        /*-----------------------------------------------------*/
        return( nbFramesMatching );
        
    }

    /*========================================================================*/
    /** Returns true if at least one frames of the specified destination 
     *  address is available on the stream.
     *  NOTE: Make sure that somebody is calling parseInputStream periodically,
     *  otherwise there will be nothing on the stream!
     * @param destCallsign Callsign filtering frames to check.
     * @return true if at least one frame of the specified destination address
     *              is available, false otherwise.                            */
    /*========================================================================*/
    public boolean isAvailable( String destAddress ) {
    /*========================================================================*/    

        /*----------------------------------------------------*/
        /* Prepare to examine all frames accumulated into the */
        /* frame vector, to find all that match               */
        /*----------------------------------------------------*/
        boolean frameMatch = false;
        int nbFramesWaiting = frameVector.size();
        Frame frame = null;
        FIND_AT_LEAST_ONE_MATCHING_FRAMES:
        for( int i=0; i<nbFramesWaiting; i++ ) {
            
            /*-----------------------------------*/
            /* Get a frame from the frame vector */
            /* If null, then quit this...        */
            /*-----------------------------------*/
            frame = (Frame)frameVector.elementAt( i );
            if( frame == null ) break FIND_AT_LEAST_ONE_MATCHING_FRAMES;
            
            /*-------------------------------------------*/
            /* Get its callsign, and compare it to the   */
            /* one we want...                            */
            /*-------------------------------------------*/
            if( frame.getDestinationAddress().equals( destAddress ) ) {
                frameMatch = true;
                break FIND_AT_LEAST_ONE_MATCHING_FRAMES;
            }
            
        }//FIND_AT_LEAST_ONE_MATCHING_FRAMES
        
        /*-----------------------------------------------------*/
        /* Returns the number of matching frames that we found */
        /*-----------------------------------------------------*/
        return( frameMatch );
        
    }

    
    
    
    /*========================================================================*/
    /** Parses the input stream and store complete frames into buffers
     *  indexed by their destination callsign for later easy retrieval.
     *  THIS FUNCTION HAS TO BE CALLED PERIODICALLY, OTHERWISE THERE WILL BE
     *  NOTHING ON THE STREAM. ALSO, IT MUST BE CALLED BY A SINGLE THREAD, SINCE
     *  STREAMS ARE USED!!!
     *  @return the number of frames parsed during this attempt.
    /*========================================================================*/
    public int parseInputStream() {
    /*========================================================================*/        
    boolean isByteToBePutInBuffer;
    boolean isFCS;
    boolean isNewData = false;
    int     nbFramesParsed = 0;
        
        /*--------------------------------------------------------------*/
        /* First check the input stream. If it is not set then complain */
        /*--------------------------------------------------------------*/
        if( is == null ) {
            System.out.println("No inputstream for spacecraft data");
            return( 0 );
        }
    
        /*-----------------------------------------------------------------*/
        /* Then check the frame vector. If it is too large, then eliminate */
        /* some frames before we get in trouble...                         */
        /* Note: limits are defined with main class declaration...         */
        /*-----------------------------------------------------------------*/
        if( frameVector.size() > MAX_FRAME_VECTOR_SIZE ) {
            for( int i=0; i<NB_FRAMES_TO_CLEAR_WHEN_VECTOR_FULL; i++ )
                frameVector.removeElementAt( i );
            System.out.println("WARNING: Frame vector overflow - "+
                               NB_FRAMES_TO_CLEAR_WHEN_VECTOR_FULL+" cleared");
        }
        if(reservedAddressesVector.size()>NB_RESERVED_TO_CLEAR_WHEN_VECTOR_FULL){
            for( int i=0; i<NB_RESERVED_TO_CLEAR_WHEN_VECTOR_FULL; i++ )
                reservedAddressesVector.removeElementAt( i );
            System.out.println("WARNING: Reserved vector overflow - "+
                              NB_RESERVED_TO_CLEAR_WHEN_VECTOR_FULL+" cleared");
        }
    
        try{ 
            /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
            /* THIS LOOP IS DANGEROUS, BECAUSE FOR WHATEVER REASON */
            /* WE COULD ALWAYS HAVE SOMETHING WAITING AND WE WOULD */
            /* BLOCK!!! WE SHOULD THINK OF A SOLUTION FOR THAT...  */
            /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
            byte[] singleByteArray = null;
            
            while( is.available() > 0 ) {
                isByteToBePutInBuffer = false;
                int byteRead = is.read();
                
                isNewData = true;
                
                /*-----------------*/
                /*PATCH PATCH PATCH*/
                /*-----------------*/
                //byte[] tmppatch = new byte[1];
                //tmppatch[0] = (byte)byteRead;
                //vectorUnformattedData.add((byte[])tmppatch);
                //if(vectorUnformattedData.size() > 
                //                             MAX_UNFORMATTED_DATA_BLOCK_HISTORY)
                //         vectorUnformattedData.removeAllElements();
                
                
                switch( readState ) {
                    //..........................................................
                    case UNFORMATTED_READ:
                    //..........................................................
                        singleByteArray = new byte[1];
                        singleByteArray[0] = (byte)byteRead;
                        vectorUnformattedData.add((byte[])singleByteArray);
                        break;
                    
                    //..........................................................
                    case NO_FLAG_RECEIVED:
                    //..........................................................
                        if( byteRead == FLAG ) {
                            /*----------------------------*/
                            /* Ready to start a new frame */
                            /*----------------------------*/
                            readState = FIRST_BYTE_OF_NEW_FRAME;
                        }
                        else {
                            /*-----------------------------------------*/
                            /* We won't start a frame without a flag...*/
                            /* Store this data as "unformatted".       */
                            /*-----------------------------------------*/
                            nbBytesEliminated++;
                            singleByteArray = new byte[1];
                            singleByteArray[0] = (byte)byteRead;
                            vectorUnformattedData.add((byte[])singleByteArray);
                            if( vectorUnformattedData.size()
                                > MAX_UNFORMATTED_DATA_BLOCK_HISTORY)
                            {
                                      vectorUnformattedData.removeAllElements();
                            }
                            
                        }
                        break;
                    //..........................................................        
                    case FIRST_BYTE_OF_NEW_FRAME:
                    //..........................................................    
                        if( byteRead == FLAG ) {
                            /*-------------------*/
                            /* Ignore filling... */
                            /*-------------------*/
                            break;
                        }
                        else {
                            /*-----------------------------------------*/
                            /* If we're reading KISS, then ignore the  */
                            /* first byte which contains port/commands */
                            /* destined for the radio/TNC...           */
                            /*-----------------------------------------*/
                            if( level2 != KISS )
                                isByteToBePutInBuffer = true;                                    
                            
                            /*--------------------------------------*/
                            /* Now continue to build the frame with */
                            /* no need to check for KISS header byte*/
                            /*--------------------------------------*/
                            readState = BUILDING_FRAME;                                
                        }
                        break;
                    //..........................................................    
                    case BUILDING_FRAME:                    
                    //..........................................................    
                        if( byteRead == FLAG ) {
                            /*--------------------------------------*/
                            /* The frame is completed, so create it */
                            /* and add it to the frame vector       */
                            /*--------------------------------------*/
                            Frame frame = null;
                            byte[] constructionBytes = null;
                            try {
                                nbFramesReceived++;
                                /*-------------------------------------*/
                                /* If we're getting the frame via KISS */
                                /* then there is no FCS...             */
                                /*-------------------------------------*/
                                if( level2 == KISS ) 
                                    isFCS = false;
                                else 
                                    isFCS = true;
                                /*------------------*/
                                /* Create the frame */
                                /*------------------*/
                                constructionBytes =
                                              frameInConstruction.toByteArray();
                                frame = new Frame(constructionBytes,isFCS);
                            } catch( InvalidFrameException ife ) {
                                System.out.println("ERROR creating frame: "+ife);
                                if( constructionBytes != null ) 
                                    vectorUnformattedData.add( 
                                                     (byte[])constructionBytes);
                                if(vectorUnformattedData.size() > 
                                             MAX_UNFORMATTED_DATA_BLOCK_HISTORY)
                                    vectorUnformattedData.removeAllElements();
                            }
                            /*--------------------------------------*/
                            /* Add it even if invalid. DO WE REALLY */
                            /* WANT TO DO THAT???                   */
                            /*--------------------------------------*/
                            if( frame == null )
                                System.out.println("ERROR creating frame: NULL");
                            else
                                if( frameVector == null ) {
                                    System.out.println("ERROR: Attempting to add a frame into NULL frameVector");
                                } else {
                                    frameVector.addElement( (Frame)frame );
                                    nbFramesParsed++;
                                }

                            /*---------------------------------------*/
                            /* Reset the buffer for the next frame...*/
                            /* and set our current state             */
                            /*---------------------------------------*/
                            if( frameInConstruction != null )
                                frameInConstruction.reset();
                            readState = FIRST_BYTE_OF_NEW_FRAME;

                            break;                                
                        }
                        else {
                            /*---------------------------------------*/
                            /* If we're reading KISS, then take into */
                            /* account the ESCAPE code               */
                            /*---------------------------------------*/
                            if( (level2==KISS)&&(byteRead==KISS_FESC) ) 
                            {                                    
                                /*------------------------------------*/
                                /* NEXT CHAR WAS ESCAPED...           */
                                /* Next time around we'll process the */
                                /* escaped char...                    */
                                /*------------------------------------*/
                                readState = BUILDING_FRAME_ESCAPED_CHAR;
                            } 
                            else {

                                /*----------------------------------*/
                                /* Anything else we just accumulate */
                                /* into the buffer                  */
                                /*----------------------------------*/
                                isByteToBePutInBuffer = true;
                            }
                        }
                        break;
                    //..........................................................    
                    case BUILDING_FRAME_ESCAPED_CHAR:    
                    //..........................................................    
                        if( byteRead == FLAG ) {
                            /*--------------------------------------*/
                            /* The frame is completed, so create it */
                            /* and add it to the frame vector. NOTE */
                            /* that since we were supposed to have  */
                            /* an escaped char here, this is not an */
                            /* usual situation and may have to be   */
                            /* flagged as an error!!!!              */
                            /* The Frame constructor will probably  */
                            /* report an error anyway...            */
                            /*--------------------------------------*/
                            Frame frame = null;
                            try {                                    
                                nbFramesReceived++;
                                /*-------------------------------------*/
                                /* If we're getting the frame via KISS */
                                /* then there is no FCS...             */
                                /*-------------------------------------*/
                                if( level2 == KISS ) 
                                    isFCS = false;
                                else 
                                    isFCS = true;
                                /*------------------*/
                                /* Create the frame */
                                /*------------------*/
                                frame = 
                                new Frame(frameInConstruction.toByteArray(),
                                          isFCS);
                            } catch( InvalidFrameException ife ) {
                                System.out.println("ERROR creating frame: "+ife);
                            }
                            /*--------------------------------------*/
                            /* Add it even if invalid. DO WE REALLY */
                            /* WANT TO DO THAT???                   */
                            /*--------------------------------------*/
                            frameVector.addElement( (Frame)frame );
                            nbFramesParsed++;

                            /*---------------------------------------*/
                            /* Reset the buffer for the next frame...*/
                            /* and our current state                 */
                            /*---------------------------------------*/
                            frameInConstruction.reset();
                            readState = FIRST_BYTE_OF_NEW_FRAME;                                
                        }
                        else {
                            /*---------------------------------------*/
                            /* If we're reading KISS, then take into */
                            /* account the ESCAPE code               */
                            /*---------------------------------------*/
                            if( (level2==KISS)&&(byteRead==KISS_TFEND) )
                            {
                                /*-------------------------------------*/
                                /* TRANSPOSED FRAME END. We have to    */
                                /* replace this char with the value of */
                                /* FRAME END, since it was escaped...  */
                                /*-------------------------------------*/
                                byteRead = KISS_FEND;
                                isByteToBePutInBuffer = true;
                            } 
                            else 
                            if( (level2==KISS)&&(byteRead==KISS_TFESC) )
                            {
                                /*-------------------------------------*/
                                /* TRANSPOSED FRAME ESCAPE. We have to */
                                /* replace this char with the value of */
                                /* FRAME END, since it was escaped...  */
                                /*-------------------------------------*/
                                byteRead = KISS_FESC;
                                isByteToBePutInBuffer = true;
                            } 
                            else {

                                /*----------------------------------*/
                                /* Anything else we just accumulate */
                                /* into the buffer. NOTE that this  */
                                /* is not an usual situation, and we*/
                                /* may have to flag it!!!!!!        */
                                /*----------------------------------*/
                                isByteToBePutInBuffer = true;
                            }                                
                        }
                        
                        /*----------------------------------------*/
                        /* Frame Escape only last for one byte... */
                        /* so come back to normal building state  */                        
                        /*----------------------------------------*/
                        readState = BUILDING_FRAME;
                        
                        break;
                        
                    //..........................................................                        
                    case COMPLETED_FRAME:
                    //..........................................................
                        //NOT USED FOR NOW...
                        break;
                        
                }//readState    
                
                /*---------------------------------------------------*/
                /* Now accumulate the byte just read into the buffer */
                /*if we are required to...                           */
                /*---------------------------------------------------*/
                if( isByteToBePutInBuffer ) {
                    
                    /*----------------------------------------------*/
                    /* But first check for overflow, and if so then */
                    /* thrash the byte and increment error counter. */
                    /*----------------------------------------------*/
                    if( frameInConstruction.size() 
                            >= FRAME_CONSTRUCTION_BUFFER_SIZE )
                    {
                        nbBytesEliminated++;
                    } 
                    /*-------------------------------------*/
                    /* Everything is ok, store the byte... */
                    /*-------------------------------------*/
                    else 
                    {
                        frameInConstruction.write( byteRead );
                    }
                }//Byte to be stored in buffer

            }//We processed all waiting bytes...
            
            
        } catch( IOException ioe ) {
            System.out.println("ERROR reading frame intput stream "+ioe);
        }
    
        return( nbFramesParsed );
    }

}
