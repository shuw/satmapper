/*
 * ServerLink.java
 *
 * Created on August 13, 2001, 3:49 PM
 */

package ca.gc.space.quicksat.ground.client;

import java.io.*;
import java.util.*;
import java.net.*;
import ca.gc.space.quicksat.ground.ax25.*;
import ca.gc.space.quicksat.ground.util.*;
import ca.gc.space.quicksat.ground.server.*;

/*============================================================================*/
/**
 *
 * @author  jfcusson
 * @version 
 * VERY IMPORTANT NOTE: If a thread which acquired the scInput is to die, 
 * then if it is the last thread to have accessed this piped stream it will
 * kill it and there will be TROUBLE!!!.
 *
 * Actually, thinking about it, a serverlink should correspond to a ground
 * station (therefore the "GroundStation" object should be integrated here).
 * That is why I began to put stuff specific to a ground station here, like 
 * the status of the serial ports line (admin stuff...).
 */
/*============================================================================*/
public class ServerLink implements Runnable {
/*============================================================================*/    
Vector              packetVector                = null;
boolean             running                     = true;
boolean             verbose                     = true;
String              currentServerAddressString  = "";
DataOutputStream    serverDataOutput            = null;
DataInputStream     serverDataInput             = null;
final int           clientPort                  = 9902;
Socket              socket                      = null;
OutputStream        output                      = null;
InputStream         input                       = null;
private boolean     standbye                    = false; //block to change settings
Thread              serverLinkThread            = null;
PipedInputStream    scInputStream               = null;
PipedOutputStream   scInputStreamFill           = null;
PipedInputStream    radioInputStream            = null;
PipedOutputStream   radioInputStreamFill        = null;
PipedInputStream    rotatorInputStream          = null;
PipedOutputStream   rotatorInputStreamFill      = null;

/*-------------------*/
/* AX25 input stream */
/*-------------------*/
private FrameInputStream ax25is = null;
int nbFramesReceivedFromSpacecraft = 0;

/*-------------------------------------------------------*/
/* These parameters control how much data we accumulate! */
/*-------------------------------------------------------*/
private final int PIPE_OVERFLOW     = 20000;    //in bytes
private final int PIPE_FLUSH_COUNT  = 10000;


/*------------------------------------------------*/
/* These are possible values for the command byte */
/* MUST BE IN SYNC WITH SERVER VALUES...          */
/* Value put in the "interlocutor" byte (actually */
/* this name is misleading...)                    */
/*------------------------------------------------*/
public static final int KILL_SERVER                 = -2;
public static final int KILL_MYSELF                 = -1;
public static final int KILL_A_CLIENT               = 2;
public static final int SET_SPACECRAFT_LINK_SPEED   = 3;
public static final int UPDATE                      = 4;
public static final int DATA_BOOTLOADER             = 10;
public static final int DATA_HOUSEKEEPING           = 11;
public static final int DATA_SPACECRAFT             = 20;
public static final int PORTINFO_SPACECRAFT         = 21;
public static final int DATA_RADIO                  = 30;
public static final int DATA_ROTATOR                = 40;
public static final int DATA_TNC                    = 50;


//PORTINFO_SPACECRAFT data byte
private final byte      MASK_CD         = 0x01;
private final byte      MASK_CTS        = 0x02;
private final byte      MASK_DSR        = 0x04;

/*-----------------------------------------------------*/
/* Status of the ground station where the server lives */
/*-----------------------------------------------------*/
private boolean isSpacecraftPortCTS = false;    //Clear to Send (TNC)
private boolean isSpacecraftPortDSR = false;    //Data Set Ready
private boolean isSpacecraftPortCD  = false;    //Carrier Detect
private int portInfoUpdateCount     = 0; //Incremented each time an update
private int lastPortInfoUpdateCount = 0;
private int spacecraftPortSpeed     = 0;

private Log log = null;
    
    /*========================================================================*/
    /** Creates new ServerLink.
     *  @param serverAddressString A string defining the initial server address.
     *  @param log Where we will log all events                               */
    /*========================================================================*/
    public ServerLink( String serverAddressString, Log log ) {
    /*========================================================================*/    

        this.log = log;
        if( this.log == null ) this.log = new Log();
        
        packetVector = new Vector();        
        
        try {
            /*-------------------------------------------------------*/
            /* Setup a pipe stream to accumulate data that is coming */
            /* from the spacecraft as sent by the ground station     */
            /* and then an AX25 input stream.                        */
            /*-------------------------------------------------------*/
            scInputStream = new PipedInputStream();
            scInputStreamFill = new PipedOutputStream( scInputStream );
            if( scInputStream != null )
                ax25is = new FrameInputStream(scInputStream);
            if( ax25is != null )
                ax25is.useKISS();

            /*-------------------------------------------------------*/
            /* Setup a pipe stream to accumulate data that is coming */
            /* from the radio as sent by the ground station          */
            /*-------------------------------------------------------*/
            radioInputStream = new PipedInputStream();
            radioInputStreamFill = new PipedOutputStream( radioInputStream );

            /*-------------------------------------------------------*/
            /* Setup a pipe stream to accumulate data that is coming */
            /* from the rotator as sent by the ground station        */
            /*-------------------------------------------------------*/
            rotatorInputStream = new PipedInputStream();
            rotatorInputStreamFill = new PipedOutputStream(rotatorInputStream);
            
        } catch( IOException ioe ) {
            System.out.println("ERROR setting serverlink: "+ioe);
        }
        
        /*------------------------------------------------------------*/
        /* Start the thread managing the link with the ground station */
        /*------------------------------------------------------------*/
        serverLinkThread = new Thread( this );
        serverLinkThread.start();
        
    }

    /*========================================================================*/
    /** Allow us to retrieve an stream to receive data from the spacecraft.
     *  @return An input stream used to read data from the spacecraft.
     *  @DEPRECATED?                                                          */
    /*========================================================================*/
    public InputStream getSpacecraftInputStream() {
    /*========================================================================*/    
        return( (InputStream)scInputStream );
    }
    
    /*========================================================================*/
    /** Tells us how many bytes are waiting to be read on the spacecraft
     *  input stream (data coming from the ground station server).
     *  NOTE: THIS METHOD WILL BECOME PRIVATE!!!! SHOULD NOT BE USED.
     *  @return Number of bytes identified as coming from the spacecraft and 
     *          waiting to be read.                                           */
    /*========================================================================*/
    public int spacecraftDataAvailable() {
    /*========================================================================*/    
        try{
            return( scInputStream.available() );
        } catch( IOException ioe ) {
            System.out.println("ERROR on spacecraft input stream");
            return( 0 );
        }
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public void setSpacecraftUnformatted( boolean mode ) {
    /*========================================================================*/
        if( ax25is == null ) return;
        else ax25is.setUnformattedMode( mode );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public boolean isSpacecraftUnformatted() {
    /*========================================================================*/    
        if( ax25is == null ) return( false );
        else return( ax25is.isUnformattedMode() );
    }
    
    /*========================================================================*/
    /** Actually this name is misleading: we're only telling if the spacecraft
     *  link is implemented and ready to interpret the AX25 protocole, as
     *  opposed to being in unformatted mode.                                 */
    /*========================================================================*/
    public boolean isSpacecraftConnected() {
    /*========================================================================*/
        if( ax25is == null ) return( false );
        else return( ax25is.isConnected() );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public int getNbSpacecraftFramesReceived() {
    /*========================================================================*/
        if( ax25is == null ) return( 0 );
        else return( nbFramesReceivedFromSpacecraft );
    }
    
    /*========================================================================*/
    /** Reads data from the spacecraft input stream, up to the space available
     *  in the dataBuffer array.
     *  NOTE: WILL BECOME PRIVATE! SHOULD NOT BE USED EXTERNALLY.
     *  @param dataBuffer Byte array where the data read will be stored. Note 
     *                    that the array is not initialized, so if the read
     *                    is not successfull whatever was in the array before
     *                    is still there...
     *  @return The number of bytes actually read.                            */
    /*========================================================================*/
    public int spacecraftDataRead( byte[] dataBuffer ) {        
    /*========================================================================*/    
        try{
            return( scInputStream.read( dataBuffer ) );
        } catch( IOException ioe ) {
            return( 0 );
        }
    }
    
    /*========================================================================*/
    /** Another way to retrieve data from the spacecraft input stream, this 
     *  time it just returns a byte array of everything contained in the stream.
     *  NOTE: WILL BECOME PRIVATE! SHOULD NOT BE USED EXTERNALLY.
     *  @return Byte array containing what was in the spacecraft input stream, 
     *          or null if there was an error.
    /*========================================================================*/
    public byte[] getSpacecraftData() {        
    /*========================================================================*/    
        try{
            if( scInputStream.available() <= 0 ) 
                return( null );
            byte[] bytesToGet = new byte[scInputStream.available()];
            scInputStream.read( bytesToGet );
            return( bytesToGet );
        } catch( IOException ioe ) {
            return( null );
        }
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public Frame getSpacecraftFrame() {
    /*========================================================================*/
        if( ax25is == null ) return( null );
        else return( ax25is.read() );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public void reserveSpacecraftAddress( String address ) {
    /*========================================================================*/    
        if( ax25is == null ) return;
        ax25is.reserve( address );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void unreserveSpacecraftAddress( String address ) {
    /*========================================================================*/    
        if( ax25is == null ) return;
        ax25is.unreserve( address );
    }
    /*========================================================================*/
    /*========================================================================*/
    public boolean isSpacecraftFrameAvailable( String address ) {        
    /*========================================================================*/        
        if( ax25is == null ) return( false );
        else return( ax25is.isAvailable( address ) );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public void removeReceivedSpacecraftFramesDestinedTo( String address ) {
    /*========================================================================*/    
        if( ax25is == null ) return;
        else ax25is.removeAll( address );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public Frame getSpacecraftFrameDestinedTo( String ourAddress ) {
    /*========================================================================*/
        if( ax25is == null ) return( null );
        else return(ax25is.readFrameDestinedTo( ourAddress ));
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public byte[] getSpacecraftUnformattedData() {
    /*========================================================================*/    
        if( ax25is == null ) return( null );
        else return( ax25is.readUnformattedData() );
    }
    
    /*========================================================================*/
    /** Clear all data currently in the spacecraft input stream.              */
    /*========================================================================*/
    public void spacecraftDataFlush() {
    /*========================================================================*/    
        try{
            scInputStream.skip( scInputStream.available() );
        } catch( IOException ioe ){}
    }
    
    /*========================================================================*/
    /** Change the speed of the serial port (local to the server) used to
      * speak to the spacecraft.
      * @param speedBps Speed, in bits per second (9600, 19200)               */
    /*========================================================================*/
    public void setSpacecraftLinkSpeed( int speedBps ) {
    /*========================================================================*/    
        sendPacket(SET_SPACECRAFT_LINK_SPEED,speedBps);
    }
    
    
    
    
    /*========================================================================*/
    /** Allow us to retrieve an stream to receive data from the radio.
     *  @return An input stream used to read data from the radio.
     *  @DEPRECATED?                                                          */
    /*========================================================================*/
    public InputStream getRadioInputStream() {
    /*========================================================================*/    
        return( (InputStream)radioInputStream );
    }
    
    /*========================================================================*/
    /** Tells us how many bytes are waiting to be read on the radio
     *  input stream (data coming from the ground station server).
     *  @return Number of bytes identified as coming from the radio and 
     *          waiting to be read.                                           */
    /*========================================================================*/
    public int radioDataAvailable() {
    /*========================================================================*/    
        try{
            return( radioInputStream.available() );
        } catch( IOException ioe ) {
            System.out.println("ERROR on radio input stream");
            return( 0 );
        }
    }
    
    /*========================================================================*/
    /** Reads data from the radio input stream, up to the space available
     *  in the dataBuffer array.
     *  @param dataBuffer Byte array where the data read will be stored. Note 
     *                    that the array is not initialized, so if the read
     *                    is not successfull whatever was in the array before
     *                    is still there...
     *  @return The number of bytes actually read.                            */
    /*========================================================================*/
    public int radioDataRead( byte[] dataBuffer ) {        
    /*========================================================================*/    
        try{
            return( radioInputStream.read( dataBuffer ) );
        } catch( IOException ioe ) {
            return( 0 );
        }
    }
    
    /*========================================================================*/
    /** Another way to retrieve data from the radio input stream, this 
     *  time it just returns a byte array of everything contained in the stream.
     *  @return Byte array containing what was in the radio input stream, 
     *          or null if there was an error.
    /*========================================================================*/
    public byte[] getRadioData() {        
    /*========================================================================*/    
        try{
            if( radioInputStream.available() <= 0 ) 
                return( null );
            byte[] bytesToGet = new byte[radioInputStream.available()];
            radioInputStream.read( bytesToGet );
            return( bytesToGet );
        } catch( IOException ioe ) {
            return( null );
        }
    }
    
    /*========================================================================*/
    /** Clear all data currently in the radio input stream.                   */
    /*========================================================================*/
    public void radioDataFlush() {
    /*========================================================================*/    
        try{
            radioInputStream.skip( radioInputStream.available() );
        } catch( IOException ioe ){}
    }

    

    /*========================================================================*/
    /** Allow us to retrieve an stream to receive data from the rotator.
     *  @return An input stream used to read data from the rotator.
     *  @DEPRECATED?                                                          */
    /*========================================================================*/
    public InputStream getRotatorInputStream() {
    /*========================================================================*/    
        return( (InputStream)rotatorInputStream );
    }
    
    /*========================================================================*/
    /** Tells us how many bytes are waiting to be read on the rotator controller
     *  input stream (data coming from the ground station server).
     *  @return Number of bytes identified as coming from the rotator and 
     *          waiting to be read.                                           */
    /*========================================================================*/
    public int rotatorDataAvailable() {
    /*========================================================================*/    
        try{
            return( rotatorInputStream.available() );
        } catch( IOException ioe ) {
            System.out.println("ERROR on rotator input stream");
            return( 0 );
        }
    }
    
    /*========================================================================*/
    /** Reads data from the rotator input stream, up to the space available
     *  in the dataBuffer array.
     *  @param dataBuffer Byte array where the data read will be stored. Note 
     *                    that the array is not initialized, so if the read
     *                    is not successfull whatever was in the array before
     *                    is still there...
     *  @return The number of bytes actually read.                            */
    /*========================================================================*/
    public int rotatorDataRead( byte[] dataBuffer ) {        
    /*========================================================================*/    
        try{
            return( rotatorInputStream.read( dataBuffer ) );
        } catch( IOException ioe ) {
            return( 0 );
        }
    }
    
    /*========================================================================*/
    /** Another way to retrieve data from the rotator input stream, this 
     *  time it just returns a byte array of everything contained in the stream.
     *  @return Byte array containing what was in the rotator input stream, 
     *          or null if there was an error.
    /*========================================================================*/
    public byte[] getRotatorData() {        
    /*========================================================================*/    
        try{
            if( rotatorInputStream.available() <= 0 ) 
                return( null );
            byte[] bytesToGet = new byte[rotatorInputStream.available()];
            rotatorInputStream.read( bytesToGet );
            return( bytesToGet );
        } catch( IOException ioe ) {
            return( null );
        }
    }
    
    /*========================================================================*/
    /** Clear all data currently in the rotator input stream.                 */
    /*========================================================================*/
    public void rotatorDataFlush() {
    /*========================================================================*/    
        try{
            rotatorInputStream.skip( rotatorInputStream.available() );
        } catch( IOException ioe ){}
    }
    
    
    
    /*========================================================================*/
    /** Used to define/change the address of the ground station server we talk
     *  to.
     *  @param serverAddressString A string defining the internet address of the
     *                             ground station server.
    /*========================================================================*/
    public synchronized void setServer( String serverAddressString ){
    /*========================================================================*/    

        log.info("SET SERVER---------");
        currentServerAddressString = serverAddressString;
        
        /*--------------------------------*/
        /* First block the running thread */
        /*--------------------------------*/
        if( serverLinkThread != null ) {
            standbye = true;
            try{Thread.currentThread().sleep(1000);}
            catch(InterruptedException ie){}
        }
        if((serverAddressString==null)||(serverAddressString.trim().equals("")))
            serverAddressString = "localhost";
        
        InetAddress serverAddress = null;
        try{
             serverAddress = InetAddress.getByName(serverAddressString);
             log.info("Server address: "+serverAddress.toString());
        } catch( UnknownHostException uhe ) {
            log.error("Could not initialize server link: "+uhe);
        }
        
        if( serverAddress == null ) {
            log.criticalError("Unable to setup serveraddress: Returning");
            return; //What will this do???
        }
        
        try {
            /*-------------------------------------------------------*/
            /* Attempt to close everything that was setup previously */
            /*-------------------------------------------------------*/
            if( serverDataOutput != null ) serverDataOutput.close();
            if( output != null ) output.close();
            if( serverDataInput != null ) serverDataInput.close();
            if( input != null ) input.close();
            if( socket != null ) socket.close();
            
            /*-------------------------*/
            /* Create socket to server */
            /*-------------------------*/
            log.info("Attempting server:"+serverAddress+" on port:"+clientPort);
            socket = new TimedSocket().getSocket(serverAddress,clientPort,5000);
            log.info("Connected...");
            
            /*--------------------------------*/
            /* Setup the input/output streams */
            /*--------------------------------*/
            output = socket.getOutputStream();
            serverDataOutput = new DataOutputStream( output );
            input = socket.getInputStream();
            serverDataInput = new DataInputStream( input );
        } 
        /*--------------------------------*/
        /* A connection timeout occured.. */
        /*--------------------------------*/
        catch( InterruptedIOException iie ) {
            log.warning("TIMEOUT connecting to server: "+iie);
        }
        /*--------------------------*/
        /* An IO problem occured... */
        /*--------------------------*/
        catch( IOException ioe ) {
            log.error("Could not initialize server link: "+ioe);
        } 
        /*--------------------------------*/
        /* Restart the server link thread */
        /*--------------------------------*/
        standbye = false;
    }

    /*========================================================================*/
    /** Retrieves the current internet address of the ground station server.
     *  @return A string containing the current address of the server.        */
    /*========================================================================*/
    public String getCurrentServerAddress() {
    /*========================================================================*/    
        return( currentServerAddressString );
    }
    
    /*========================================================================*/
    /** This method order the serverlink main thread to terminate, thus killing
     *  the communication link with the ground station server.                */
    /*========================================================================*/
    public void kill() {
    /*========================================================================*/
        running = false;
    }

    /*========================================================================*/
    /** Used to control if this serverlink talks a lot or not...
     *  @param status Set to true to make this class display more progress 
     *                messages.                                               */
    /*========================================================================*/
    public void setVerbose( boolean status ) {
        verbose = status;
    }
    
    /*========================================================================*/
    /** Tells us how many packets, destined to the ground station server, are
     *  waiting in the queue to be transmitted.
     *  @return the number of packets waiting to be transmitted to the server.*/
    /*========================================================================*/
    public int getSendQueueSize() {
    /*========================================================================*/
        return( packetVector.size() );
    }

    /*========================================================================*/
    /** Tells us the current status of the Clear To Send line on the serial port
     *  used to talk to the spacecraft (via the TNC) on the ground station
     *  server.
     *  @return true if the line is active, false otherwise.                  */
    /*========================================================================*/
    public boolean isSpacecraftPortCTS() {
    /*========================================================================*/
        return( isSpacecraftPortCTS );    //Clear to Send (TNC)
    }
    
    /*========================================================================*/
    /** Tells us current status of the Data Set Ready line on the serial port
     *  used to talk to the spacecraft (via the TNC) on the ground station
     *  server.
     *  @return true if the line is active, false otherwise.                  */
    /*========================================================================*/
    public boolean isSpacecraftPortDSR() {
    /*========================================================================*/
        return( isSpacecraftPortDSR );    //Data Set Ready
    }
    
    /*========================================================================*/
    /** Tells us current status of the Carrier Detect line on the serial port
     *  used to talk to the spacecraft (via the TNC) on the ground station
     *  server.
     *  @return true if the line is active, false otherwise.                  */
    /*========================================================================*/
    public boolean isSpacecraftPortCD() {
    /*========================================================================*/
        return( isSpacecraftPortCD );    //Carrier Detect
    }

    /*========================================================================*/
    /** Tells us the speed of the serial port used to talk to the spacecraft
     *  @return Speed, in bits per seconds.
    /*========================================================================*/
    public int getSpacecraftPortSpeed() {
    /*========================================================================*/
        return( spacecraftPortSpeed );
    }
    
    /*========================================================================*/
    /** Tells us if the status of the spacecraft serial port lines has changed
     *  since we last called this function. Of course this is NOT THREAD SAFE.
     *  @return true if the status has changed, false otherwise.              */
    /*========================================================================*/
    public boolean isSpacecraftPortChanged() {
    /*========================================================================*/    
        if( portInfoUpdateCount != lastPortInfoUpdateCount ) {
            lastPortInfoUpdateCount = portInfoUpdateCount;
            return( true );
        }
        return( false );
    }
    
    /*========================================================================*/
    /** Request an update of the status of the ground station. This is required
     *  periodically since some of the status are transferred only on a change
     *  (like for the serial port lines) and therefore if there is no change
     *  we will never know (at startup) what is the real state....            */
    /*========================================================================*/
    public void updateStatus() {
    /*========================================================================*/
        sendPacket( UPDATE, 0 );
    }
    
    /*========================================================================*/
    /** Sends a packet to the ground station server.
     *  @param interlocutor To whom do we speak? See header for possible values.
     *  @param dataLength In bytes.
     *  @param data An array of bytes containing what we want to say...       */
    /*========================================================================*/
    public synchronized void sendPacket(    int interlocutor,
                                            int dataLength,
                                            byte[] data) {
    /*========================================================================*/    
        packetVector.add( (byte[])createServerPacket(   interlocutor, 
                                                        dataLength, 
                                                        data ) );
    }
    
    /*========================================================================*/
    /** Sends a packet to the ground station server.
     *  @param interlocutor To whom do we speak? See header for possible values.
     *  @param param This is what we want to say to the ground station server.
     *               See header for possible values.                          */
    /*========================================================================*/
    public synchronized void sendPacket(int interlocutor,int param) {
    /*========================================================================*/    
        packetVector.add( (byte[])createServerPacket(   interlocutor, 
                                                        param, 
                                                        null  ) );
    }
    
    /*========================================================================*/
    /** Sends a packet to the spacecraft, encapsulated in the right protocole.
     *  @param data A byte array containing the packet to send.               */
    /*========================================================================*/
    public synchronized void sendPacketToSpacecraft(byte[] data) {
    /*========================================================================*/
        if( data == null ) return;
        packetVector.add((byte[])createServerPacket(    DATA_SPACECRAFT,
                                                        data.length, 
                                                        data ) );
    }
    
    /*========================================================================*/
    /** Sends a packet to the TNC.
     *  @param data A byte array containing the packet to send.               */
    /*========================================================================*/
    public synchronized void sendPacketToTnc(byte[] data) {
    /*========================================================================*/    
        if( data == null ) return;
        packetVector.add((byte[])createServerPacket(    DATA_TNC,
                                                        data.length, 
                                                        data ) );
    }
    
    /*========================================================================*/
    /** Sends a packet to the antenna rotator.
     *  @param data A byte array containing the packet to send.               */
    /*========================================================================*/
    public synchronized void sendPacketToAntennaRotator(byte[] data) {
    /*========================================================================*/    
        packetVector.add((byte[])createServerPacket(    DATA_ROTATOR,
                                                        data.length, 
                                                        data ) );
    }
    
    /*========================================================================*/
    /** Sends a packet to the radio.
     *  @param data A byte array containing the packet to send.               */
    /*========================================================================*/
    public synchronized void sendPacketToRadio(byte[] data) {
    /*========================================================================*/    
        packetVector.add((byte[])createServerPacket(    DATA_RADIO,
                                                        data.length, 
                                                        data ) );
    }
    
    
    /*========================================================================*/
    /** Creates a packet in the format that the server will understand.
     *  @param interlocutor To whom do we want to speak. See header for possible
     *                      values.
     *  @param lengthOrParam In bytes (for the data length), or a parameter
     *                       when we're speaking to the server.
     *  @param data Byte array containing the message to send.                */
    /*========================================================================*/
    private synchronized byte[] createServerPacket( int interlocutor,    
                                                    int lengthOrParam,
                                                    byte[] data ) {
    /*========================================================================*/                                                    
        byte[] serverPacket = null;
        try{                                                        
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt( interlocutor );
            //if( (data==null) && (lengthOrParam!=0) )
            //    log.warning("data=null while lengthorparam="+lengthOrParam+" for "+interlocutor);
            dos.writeInt( lengthOrParam );
            if( data != null ) dos.write(data);
            serverPacket= baos.toByteArray();
            dos.close();
            baos.close();
        } catch( IOException ioe ) {
            log.error("ERROR creating server packet: "+ioe);
        }
        return( serverPacket );
        
    }
    
    /*========================================================================*/
    /** Thread executed by the serverLink, used to manage the communication with
     *  the ground station server.                                            */
    /*========================================================================*/
    public void run() {
    /*========================================================================*/            
    
        boolean doneSomething = false;
        log.info("Starting Server Link Thread");
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);        
        
        /*....................................................................*/
        SERVERLINK_LOOP:
        while( running ) {
                        
            /*-----------------------------------------------------*/
            /* This is the standbye loop, allowing a change in the */
            /* server link setup...                                */
            /*-----------------------------------------------------*/
            while( standbye ) {
                System.out.print(".");
                try{Thread.currentThread().sleep(500);}catch(InterruptedException ie){}
            }
            
            doneSomething = false;
            
            /*----------------------------------------------------------------*/
            /*                            T   X                               */
            /*----------------------------------------------------------------*/
            if( (packetVector.size() > 0) && (serverDataOutput != null) ) {
                
                /*--------------------------------------------*/
                /* We have something to send to the server... */
                /*--------------------------------------------*/
                byte[] packet = (byte[])packetVector.firstElement();
                packetVector.remove( packet );
                try{
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    StringBuffer testMessage = new StringBuffer("SENT>"); 
                    int c = 0;
                    for( int i=0; i<packet.length; i++ ){
                        c = (int)(packet[i]&0xFF);
                        if( (c>=0x20) && (c<0x7E) )
                            testMessage.append((char)packet[i]);
                        else
                            testMessage.append(":"+c);
                    }
                    testMessage.append(":");
                    log.test( testMessage.toString() );
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    serverDataOutput.write( packet );
                    //if( verbose ) System.out.println("Send packet to server");
                }catch( IOException ioe ) {
                    System.out.println("ERROR writing to server: "+ioe);
                }
                doneSomething = true;
            }

            
            /*----------------------------------------------------------------*/
            /*                            R   X                               */
            /*----------------------------------------------------------------*/
            if( serverDataInput != null ) {
              try{
                if( serverDataInput.available() > 0 ) {
                    /*---------------------------------------------*/
                    /* We have something from the server for us... */
                    /*---------------------------------------------*/
                    int i = serverDataInput.readInt();
                    //System.out.println("Read " + i);
                    int dataSize = 0;
                    byte[] data = null;
                    /*--------------------------------------------*/
                    /* Here is the format of what is expected:    */
                    /* [command][size_of_data/value][data]        */
                    /* Where [command] is an integer              */
                    /*       [size_of_data] is an integer telling */
                    /*                      telling us how many   */
                    /*                      chars are in [data]   */
                    /*  ..or [value] an integer, parameter of     */
                    /*               [command]                    */
                    /*       [data] a serie of chars...           */
                    /* EVENTUALLY SHOULD BE XML                   */
                    /*--------------------------------------------*/
                    SWITCH_ON_DATA_RECEIVED:
                    switch( i ) {
                        /*....................................................*/
                        case Server.KILL_MYSELF:    //Kill myself
                        /*....................................................*/    
                            log.info("Got request to kill myself");
                            break SERVERLINK_LOOP;
                        /*....................................................*/    
                        case Server.DATA_SPACECRAFT:
                        /*....................................................*/    
                            //System.out.println("Got data from spacecraft");
                            dataSize = serverDataInput.readInt();
                            /*---------------------------------*/
                            /* Gather data into receive buffer */
                            /*---------------------------------*/
                            RECEIVING_SPACECRAFT_DATA:
                            {
                                if( scInputStreamFill == null ) {
                                    log.error("Receiving data "
                                                     + "from spacecraft, but "
                                                     + "input stream dead!");
                                    byte[] junk = new byte[ dataSize ];
                                    serverDataInput.read( junk );
                                    break RECEIVING_SPACECRAFT_DATA;
                                }
                                
                                data = new byte[ dataSize ];
                                serverDataInput.read( data );
                                //System.out.println("Data: "+new String(data));
                                try {
                                    scInputStreamFill.write( data );
                                    if(scInputStream.available()>PIPE_OVERFLOW){
                                        scInputStream.skip(PIPE_FLUSH_COUNT);
                                        log.warning("Flushing s/c pipe");
                                    }                                    
                                } catch( IOException ioe ) {
                                    /*---------------------------------------*/
                                    /* If a thread that used this piped      */
                                    /* stream died and was the last one to   */
                                    /* use the stream, then it dies with it. */
                                    /* So attempt to re-establish it now...  */
                                    /*---------------------------------------*/
                                    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                                    /* This is actually bad. We have to make */
                                    /* a method to do it!!!                  */
                                    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                                    log.error("writing to "
                                                   + "scInputStreamFill: "+ioe);
                                    try {
                                        if( scInputStream != null )
                                            scInputStream.close();
                                    } catch( IOException e ) {}
                                    try {
                                        if( scInputStreamFill != null )
                                            scInputStreamFill.close();
                                    } catch( IOException e ) {}                                    
                                    log.error("Attempting to reset stream");
                                    try {                                        
                                        scInputStream = new PipedInputStream();
                                        scInputStreamFill = 
                                           new PipedOutputStream(scInputStream);
                                    } catch( IOException e ) {
                                        log.error("resetting s/c pipe: "+ioe);
                                    }                                    
                                }
                                
                            } //RECEIVING_SPACECRAFT_DATA:
                            /*------------------------------------------------------*/
                            /* NOW this is the place we take care of the AX25 input */
                            /* stream. If we do not parse it, it will never contain */
                            /* anything; however it is not done automatically since */
                            /* we're playing with streams, and this is NOT MULTI-   */
                            /* THREAD SAFE! So we're in the only thread allowed to  */
                            /* parse the AX25 input stream...                       */
                            /*------------------------------------------------------*/
                            int nbFramesParsed = 0;
                            if( ax25is != null )
                                nbFramesParsed = ax25is.parseInputStream();
                            nbFramesReceivedFromSpacecraft += nbFramesParsed;                            
                            break;
                        /*....................................................*/    
                        case Server.PORTINFO_SPACECRAFT:
                        /*....................................................*/    
                           //System.out.println("Got port update for s/c link");
                            portInfoUpdateCount++;
                            dataSize = serverDataInput.readInt();
                            
                            data = new byte[1];
                            serverDataInput.read( data );
                            if( dataSize > 1 )
                                spacecraftPortSpeed = serverDataInput.readInt();
                            
                            if((data[0]&MASK_CD)>0)  isSpacecraftPortCD=true;
                            else                     isSpacecraftPortCD=false;
                            
                            if((data[0]&MASK_CTS)>0) isSpacecraftPortCTS=true;
                            else                     isSpacecraftPortCTS=false;

                            if((data[0]&MASK_DSR)>0) isSpacecraftPortDSR=true;
                            else                     isSpacecraftPortDSR=false;
                            
                            break;
                        /*....................................................*/
                        case Server.DATA_RADIO:
                        /*....................................................*/    
                            //System.out.println("Got data from Radio");
                            dataSize = serverDataInput.readInt();                            
                            data = new byte[ dataSize ];
                            serverDataInput.read( data );
                            try{ 
                                radioInputStreamFill.write( data );
                                if(radioInputStream.available()>PIPE_OVERFLOW){
                                   radioInputStream.skip(PIPE_FLUSH_COUNT);
                                   log.warning("Flushing radio pipe");
                                }                                
                            } catch( IOException ioe ) {
                                log.error("receiving data from radio"+ioe );
                            }
                            break;
                        /*....................................................*/
                        case Server.DATA_ROTATOR:
                        /*....................................................*/
                            //System.out.println("Got data from rotator");
                            dataSize = serverDataInput.readInt();                            
                            data = new byte[ dataSize ];
                            serverDataInput.read( data );
                            try{ 
                               rotatorInputStreamFill.write( data );
                               if(rotatorInputStream.available()>PIPE_OVERFLOW){
                                   rotatorInputStream.skip(PIPE_FLUSH_COUNT);
                                   log.warning("Flushing rotator pipe");
                               }
                            } catch( IOException ioe ) {
                               log.error("receiving data from rotator"+ioe);
                            }
                            break;
                        /*....................................................*/    
                        default:
                        /*....................................................*/    
                            log.warning("Got illegal request from server");
                            /*-----------------------------------------*/
                            /* Discard everything, hoping to re-sync...*/
                            /*-----------------------------------------*/
                            serverDataInput.skip(serverDataInput.available());
                            break;
                    }
                        
                }
              }catch( IOException ioe ) {
                    log.error("Reading from server: "+ioe);
                    log.error("Attempting to re-establish connection");
                    setServer( currentServerAddressString );
              }
            }
            
            if( !doneSomething )
                try{Thread.currentThread().sleep(10);}catch(InterruptedException ie){}
            else
                try{Thread.currentThread().sleep(5);}catch(InterruptedException ie){}
        }
        
        log.info("Killed current server link");
        
        /*-------------------------------------------------------*/
        /* Attempt to close everything that was setup previously */
        /*-------------------------------------------------------*/
        try{
            if( serverDataOutput != null ) serverDataOutput.close();
            if( output != null ) output.close();
            if( serverDataInput != null ) serverDataInput.close();
            if( input != null ) input.close();
            if( socket != null ) socket.close();
        } catch( IOException ioe ) {}
    }    
    

}
