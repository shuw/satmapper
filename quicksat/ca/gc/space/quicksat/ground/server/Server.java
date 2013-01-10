/*============================================================================*/
/** Server.java
 *  Main class used for the QuickSat ground station server.
 *  @author Jean-Francois Cusson, Canadian Space Agency
 *  THIS IS NOT AN OFFICIAL RELEASE. THIS CODE IS STILL BEING DEVELOPED AND
 *  TESTED!
 * Created on July 2, 2001, 9:22 AM
/*============================================================================*/

package ca.gc.space.quicksat.ground.server;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.comm.*;
import java.util.Date.*;

/*============================================================================*/
/** Main class for server, destined to be standalone application.
 *  This version was made on Windows, and the only two things to change/check
 *  before going to cross-plattform are: <P>
 *  - setup file path is C:/...
 *  - the CommAPI, the last time I checked, is only "officially" supported
 *    on Solaris and Windows, although a Linux version exists.
 *
 *  Here is the (simple) protocole implemented for now, on the socket between
 *  the server and the client. It consist of a single packet format:
 *  
 *  [command][size_of_data/value][data]
 *
 *  Where [command] is an integer 
 *        [size_of_data] is an integer telling telling us how many
 *                       chars are in [data]
 *        ..or [value] an integer, parameter of [command]
 *        [data] a serie of chars...
 *
 * NOTE that integers are sent in the "big-endian" format, 4 bytes, so:
 * MSB (sent first), Byte3, Byte2, LSB (sent last).
 * NOTE also that the COMMAND FIELD is COMPULSORY even if it has no meaning
                                                                              */
/*============================================================================*/
public class Server extends Object implements Runnable, SerialPortEventListener{
/*============================================================================*/    

/*--------------------------------------------------------------------------*/
/* On the spacecraft link, we're waiting to complete a kiss frame before    */
/* sending the data to the client. However, if we do not received a frame   */
/* end flag FEND_TIMEOUT mSec after beginning a frame, we send the data     */
/* anyway. Do not put this timeout too high, since when we're communicating */
/* with the TNC there are no frames...                                      */
/*--------------------------------------------------------------------------*/
private final long FEND_TIMEOUT = (long) 1000;    

/*-----------------------------------------------------------------------*/
/* Allow us to disable specific links (char that would have been send to */
/* the port are printed on screen instead...)                            */
/*-----------------------------------------------------------------------*/
private boolean RADIO_ACTIVE  = true;
private boolean ROTATOR_ACTIVE= true;
private boolean TNC_ACTIVE    = true;

/*------------------------------------------------*/
/* These are possible values for the command byte */
/* MUST BE IN SYNC WITH SERVERLINK VALUES...      */
/* See following table to get explanation of all  */
/* commands...                                    */
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
private byte            portLineStatus  = 0;
private final byte      MASK_CD         = 0x01;
private final byte      MASK_CTS        = 0x02;
private final byte      MASK_DSR        = 0x04;


/*---------------------------------------------------------------------------*/
/* SERVER COMMANDS:                                                          */
/* Name                      Descr.                      Command/Value       */
/*...........................................................................*/
/* KILL_SERVER               Forces server to abort      None                */
/* KILL_MYSELF               Ends the client connection  None                */
/* KILL_A_CLIENT             Kills a specific client     Client id           */
/*                           (only on admin link)                            */
/* DATA_BOOTLOADER           Data destined to the S/C    Data field length   */
/*                           bootloader                                      */
/* DATA_HOUSEKEEPING         Data destined to the S/C    Data field length   */
/*                           tasks under PHT reign...                        */
/* DATA_SPACECRAFT           General data destined to    Data field length   */
/*                           S/C (Not implemented yet)                       */
/* DATA_RADIO                Data destined to radio      Data field length   */
/* DATA_ROTATOR              Data destined to rotator    Data field length   */
/* SET_SPACECRAFT_LINK_SPEED Change speed of serial      Speed               */
/*                           port used to talk to S/C                        */
/*---------------------------------------------------------------------------*/
/* Note that to prevent an eventual desync mess we should implement something*/
/* like a mini KISS frame, with flags and byte stuffing...                   */
/*---------------------------------------------------------------------------*/

/*--------------------------------------------------------------------------*/
/* vector containing the list of all currently connected clients and admins */
/*--------------------------------------------------------------------------*/
Vector                  adminsConnected = null;
Vector                  clientsConnected = null;

/*-----------------------------------------*/
/* Threads started for every connection... */
/*-----------------------------------------*/
Thread                  threadWaitingForAdmin = null;
Thread                  threadWaitingForClients = null;
Thread                  threadMonitor = null;

/*------------------------------------------------------------------*/
/* Although we're using polling for serial port inputs, still we're */
/* listening and "helping" the polling by waking up the serial port */
/* polling thread when a char is waiting...                         */
/*------------------------------------------------------------------*/
Thread                  threadSerialPortsListener = null;

/*---------------------------------------*/
/* Socket parameters and usefull vars... */
/*---------------------------------------*/
Socket                  adminSocket;
Socket                  clientSocket;
final int               adminPort = 9901;   //Should be a parameter!!!
final int               clientPort = 9902;
ServerSocket            adminWait = null;
ServerSocket            clientWait = null;

/*---------------------------------*/
/* Spacecraft link usefull vars... */
/*---------------------------------*/
private boolean         spacecraftEnabled= false;
private SerialPort      spacecraftSerialPort = null;
private InputStream     spacecraftInput  = null;
private OutputStream    spacecraftOutput = null;
private String          spacecraftPort   = "COM1";
private int             spacecraftBaud  = 9600;
public int              scuPhtBaud      = 9600;
public int              scuQblBaud      = 19200;
private boolean         scCD           = false;
private boolean         scCTS          = false;
private boolean         scDSR          = false;

/*----------------------------*/
/* Radio link usefull vars... */
/*----------------------------*/
private boolean         radioEnabled     = false;
private SerialPort      radioSerialPort  = null;
private InputStream     radioInput       = null;
private OutputStream    radioOutput      = null;
private String          radioPort        = "COM2";
private int             radioBaud        = 9600;

/*------------------------------*/
/* Rotator link usefull vars... */
/*------------------------------*/
private boolean         rotatorEnabled   = false;
private SerialPort      rotatorSerialPort= null;
private InputStream     rotatorInput     = null;
private OutputStream    rotatorOutput    = null;
private String          rotatorPort      = "COM2";
private int             rotatorBaud      = 9600;

/*------------------------------------------------------------*/
/* This is where we'll look for setup files.                  */
/* OK, this is NOT PORTABLE. It will have to change before we */
/* go to Linux or Solaris...                                  */
/*------------------------------------------------------------*/
private String          baseDir = "C:/JLOAD/";



    /*========================================================================*/
    /** main method starting server. Simply instantiate a new server...
     * @param arg command line arguments                                      */
    /*========================================================================*/
    public static void main(java.lang.String[] arg) {        
    /*========================================================================*/    
        System.out.println("================================================");
        System.out.println("====CSA GROUND STATION SERVER V5 Tentative D====");
        System.out.println("==== JF Cusson, 2002, Canadian Space Agency ====");
        System.out.println("================================================");
        Server server = new Server();
    }

    /*========================================================================*/
    /** Creates new Server. Reads setup files and starts all necessary threads*/
    /*========================================================================*/
    public Server() {        
    /*========================================================================*/    

        /*-----------------------------------------------*/
        /* Initialize the vectors containing the list of */
        /* clients and administrators connected          */
        /*-----------------------------------------------*/
        adminsConnected = new Vector();
        clientsConnected= new Vector();
        
        /*-----------------------------*/
        /* Open the configuration file */
        /*-----------------------------*/
        FileInputStream fis = null;
        DataInputStream dis = null;
        String lineRead = null;                
        
        try {
            fis = new FileInputStream(baseDir+"qsgs_cfg.txt");
            dis = new DataInputStream( fis );
            System.out.println( "Found file '"
                                + baseDir +
                                "qsgs_cfg.txt': Loading configuration");
            
            /*-------------------------------------------------------------*/
            /* I've been pretty lazy with this. Java includes many usefull */
            /* classes to help, like tokenizer, which I simply had time to */
            /* investigate yet...                                          */
            /*-------------------------------------------------------------*/
            while( (dis.available()>0) ) {
                lineRead = dis.readLine();
                /*............................................................*/
                if( lineRead.equalsIgnoreCase("[SPACECRAFT LINK]") 
                    && (dis.available()>0) ) {
                /*............................................................*/    
                    spacecraftPort = dis.readLine().trim();
                    try{ 
                        spacecraftBaud = Integer.parseInt(dis.readLine()); 
                        spacecraftEnabled = true;
                    }
                    catch( NumberFormatException nfe ) {}
                }
                /*............................................................*/
                else if( lineRead.equalsIgnoreCase("[SCU-BOOTLOADER-BAUDRATE]") 
                        && (dis.available()>0) ) {
                /*............................................................*/    
                    String junk = dis.readLine().trim(); //Not used
                    try{ scuQblBaud = Integer.parseInt(dis.readLine()); }
                    catch( NumberFormatException nfe ) {}
                    System.out.println("Bootloader baudrate:"+scuQblBaud);
                }
                /*............................................................*/
                else if( lineRead.equalsIgnoreCase("[SCU-PHT-BAUDRATE]") 
                        && (dis.available()>0) ) {
                /*............................................................*/    
                    String junk = dis.readLine().trim(); //Not used
                    try{ scuPhtBaud = Integer.parseInt(dis.readLine()); }
                    catch( NumberFormatException nfe ) {}
                    System.out.println("PHT baudrate:"+scuPhtBaud);
                }                
                /*............................................................*/
                else if( lineRead.equalsIgnoreCase("[RADIO LINK]") 
                         && (dis.available()>0) ) {
                /*............................................................*/    
                    radioPort = dis.readLine().trim();
                    try{
                        radioBaud = Integer.parseInt(dis.readLine());
                        radioEnabled = true;
                    }
                    catch( NumberFormatException nfe ) {}
                }
                /*............................................................*/
                else if( lineRead.equalsIgnoreCase("[ROTATOR LINK]") 
                         && (dis.available()>0) ) {
                /*............................................................*/    
                    rotatorPort = dis.readLine().trim();
                    try{
                        rotatorBaud = Integer.parseInt(dis.readLine());
                        rotatorEnabled = true;
                    }
                    catch( NumberFormatException nfe ) {}
                }
            }
            fis.close();
        }catch( IOException ioe ){
            System.out.println( "Configuration file '"
                                +baseDir+"qsgs_cfg.txt' not found");
        }
        
        
        /*-----------------------------*/
        /* Initialize the serial ports */
        /*-----------------------------*/
        if( spacecraftEnabled )
            initSpacecraftSerialPort( spacecraftPort, spacecraftBaud );
        if( radioEnabled )
            initRadioSerialPort( radioPort, radioBaud );
        if( rotatorEnabled )
            initRotatorSerialPort( rotatorPort, rotatorBaud );

        
        try{
            /*---------------------------------------------*/
            /* Creates socket to wait for admin connection */
            /*---------------------------------------------*/
            adminWait = new ServerSocket( adminPort );
            System.out.println("Waiting on port " + adminPort + " for admin");
            
            /*----------------------------------------------*/
            /* Creates socket to wait for client connection */
            /*----------------------------------------------*/
            clientWait = new ServerSocket( clientPort );
            System.out.println("Waiting on port " +clientPort+ " for clients");
            
            /*---------------------------------------------------------------*/
            /* Creates a thread that will actually wait for an administrator */
            /* and communicate with it...                                    */
            /*---------------------------------------------------------------*/
            threadWaitingForAdmin = new Thread( this );
            threadWaitingForAdmin.start();            
            
            /*-------------------------------------------------------*/
            /* Creates a thread that will actually wait for a client */
            /* and communicate with it...                            */
            /*-------------------------------------------------------*/
            threadWaitingForClients = new Thread( this );
            threadWaitingForClients.start();            
            
            /*-------------------------------------------------------*/
            /* Creates a thread that will listen to serial ports and */
            /* send the data to connected clients                    */
            /*-------------------------------------------------------*/
            threadSerialPortsListener = new Thread( this );
            threadSerialPortsListener.start();
            
            /*--------------------------------------------*/
            /* Thread to monitor all of the other threads */
            /*--------------------------------------------*/
            threadMonitor = new Thread( this );
            threadMonitor.start();
            
        } catch( Exception e ) { System.out.println("Server error: " + e);}

    }

    /*========================================================================*/
    /** Executes our local threads: threadWaitingForAdmin, 
     *  threadWaitingForClients, threadSerialPortsListener and threadMonitor. */
    /*========================================================================*/
    public void run() {
    /*========================================================================*/    
    
        /*--------------------------------------------------------------------*/
        /*        This is a thread to communicate with an administrator       */
        /*--------------------------------------------------------------------*/
        if( Thread.currentThread() == threadWaitingForAdmin ) {
            System.out.println("This is the thread to wait for admin...");
            while( true ) {
                try { 
                    
                    /*-----------------------------*/
                    /* Block here until connection */
                    /*-----------------------------*/
                    adminSocket = adminWait.accept();
                    System.out.println("Admin connected:"
                                        + adminSocket.getInetAddress());                    
                    
                    /*----------------------------------------------*/
                    /* Create new object to take care of this admin */
                    /*----------------------------------------------*/
                    AdminLink adminServer = new AdminLink( adminSocket, this );
                    Thread thread = new Thread( adminServer, "admin" );
                    thread.start();
                    
                    /*--------------------------------------------*/
                    /* Adds this admin object in the admin Vector */
                    /* to keep track of it...                     */
                    /*--------------------------------------------*/
                    adminsConnected.add( (AdminLink) adminServer );
                    
                } catch(Exception e) {System.out.println("Server error: " + e);}
            }                        
        }    
                        
        /*--------------------------------------------------------------------*/
        /*            This is a thread to communicate with a client           */
        /*--------------------------------------------------------------------*/
        else if( Thread.currentThread() == threadWaitingForClients ) {
            System.out.println("This is the thread to wait for clients...");            
            while( true ) {
                try {                                         
                    /*------------------------------------*/
                    /* Block here until a client connects */
                    /*------------------------------------*/
                    clientSocket = clientWait.accept();
                    System.out.println("Client connected: " 
                                        + clientSocket.getInetAddress() );
                    
                    /*-----------------------------------------------------*/
                    /* Client connected: Create new object to take care of */
                    /* this client...                                      */
                    /*-----------------------------------------------------*/
                    ClientLink clientServer = new ClientLink(clientSocket,this);
                    Thread thread = new Thread( clientServer, "client" );
                    thread.start();
                    
                    /*--------------------------------------------------*/
                    /* Adds this client object to the client Vector, to */
                    /* keep track of it...                              */
                    /*--------------------------------------------------*/
                    clientsConnected.add( (ClientLink) clientServer );
                    
                } catch(Exception e) {System.out.println("Server error: " + e);}
            }                        
        } 
        
        /*--------------------------------------------------------------------*/
        /*            This is a thread to monitor the serial ports            */
        /*--------------------------------------------------------------------*/
        else if( Thread.currentThread() == threadSerialPortsListener ) {
            
            /*--------------------------------------------------*/
            /*>>>>> OK, HERE THERE IS AN OBVIOUS SYNC PROBLEM:  */
            /* BEHAVIORS CAN BE BAD IF WE ARE RESETTING THE     */
            /* SERIAL PORT AT THE SAME TIME: WE HAVE TO DISABLE */
            /* READING SAFELY WHEN RESETTING THE SERIAL PORT!!! */
            /*--------------------------------------------------*/
            
            /*------------------------------------------------------*/
            /* We'll accumulate the data read into a byte array via */
            /* this byte array output stream, eliminating the flags */
            /* in the meantime...                                   */
            /*------------------------------------------------------*/
            boolean somethingRead = false;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            /*-----------------------------------------------------------*/
            /* This next var is used to keep track of when we began to   */
            /* read chars on the port. If we cannot complete a KISS/AX25 */
            /* frame within a reasonable amount of time we'll react...   */
            /*-----------------------------------------------------------*/
            long timeSpacecraftDataBufferBegun = 0;            
            
            /*---------------------------------------*/
            /* Main loop receiving from serial ports */
            /*---------------------------------------*/
            while( true ) {
                
                /*------------------------------------------------------------*/
                /*               Take care of the spacecraft link             */
                /*------------------------------------------------------------*/
                somethingRead = false;
                if( spacecraftInput != null ) {                    
                    try{
                        int byteRead = 0;
                        while( spacecraftInput.available() > 0 ) {
                            //System.out.println("Reading data from sc port");
                            byteRead = spacecraftInput.read();
                            //System.out.println("S/C port read: "+byteRead);
                            if( timeSpacecraftDataBufferBegun == 0 )
                                timeSpacecraftDataBufferBegun = 
                                                        new Date().getTime();
                            baos.write( byteRead );
                            /*---------------------------------*/
                            /* THIS IS PROTOCOLE DEPENDENT, WE */
                            /* HAVE NOT TO FORGET IT!!!!!!!!!! */
                            /*---------------------------------*/
                            if( (byteRead == 0xC0) || (baos.size()>254) ) {
                                /*-------------------------------*/
                                /* Received a frame delimiter,   */
                                /* or more data than a frame can */
                                /* contain...                    */
                                /* So send data to clients and   */
                                /* reset the byte output stream  */
                                /*-------------------------------*/
                                /* THERE IS A PROBLEM HERE IF WE   */
                                /* CONTINUALLY RECEIVE FENDs WE'LL */
                                /* SEND EMPTY FRAMES TO CLIENTS FOR*/
                                /* NOTHING!!!!!!!!!!!!!!!!!! But   */
                                /* I've not seen KISS flag fill... */
                                /*---------------------------------*/
                                sendSpacecraftDataToAllClients(
                                                            baos.toByteArray());
                                baos.reset();                                    
                                timeSpacecraftDataBufferBegun = 0;
                                if( byteRead != 0xC0 ) 
                                    System.out.println("Frame size > limit");
                            }
                            somethingRead = true;
                        }//wend
                        /*--------------------------------------------*/
                        /* Now if we never encountered the conditions */
                        /* to send the data to the clients within a   */
                        /* specific delay, send the data anyway, the  */
                        /* client will deal with it...                */
                        /*--------------------------------------------*/
                        if( timeSpacecraftDataBufferBegun > 0 ) {
                            long delayRetainedSpacecraftData = 
                                            new Date().getTime()
                                            - timeSpacecraftDataBufferBegun;
                            if( (delayRetainedSpacecraftData > FEND_TIMEOUT) ||
                                (delayRetainedSpacecraftData < 0) )
                            {
                                sendSpacecraftDataToAllClients(
                                                            baos.toByteArray());
                                baos.reset();
                                timeSpacecraftDataBufferBegun = 0;
                                System.out.println("Timeout: send to clients");
                            }
                        }
                        /*------------------------------*/
                        /* Now verify state of lines... */
                        /*------------------------------*/                        
                        if( (spacecraftSerialPort.isCTS() != scCTS )
                            || (spacecraftSerialPort.isDSR() != scDSR )
                            || (spacecraftSerialPort.isCD() != scCD ) )
                        {
                            scCTS = spacecraftSerialPort.isCTS();
                            scDSR = spacecraftSerialPort.isDSR();
                            scCD  = spacecraftSerialPort.isCD();
                            portLineStatus = (byte)(
                                                          (scCTS?MASK_CTS:0)
                                                        | (scDSR?MASK_DSR:0)
                                                        | (scCD?MASK_CD:0)  );
                            sendSpacecraftPortInfoToAllClients();
                        }                        
                    }
                    catch( IOException ioe ) {
                        System.out.println("Spacecraft link event: "+ioe);
                    }
                } else {
                    System.out.println("ERROR: Spacecraft input stream is null");
                    try{Thread.currentThread().sleep(2000);}
                    catch(InterruptedException ie){}
                }
                
                /*-----------------------------*/
                /* Take care of the radio link */
                /*-----------------------------*/
                if( radioInput != null ) {
                    try{
                        /*--------------------------------------------------*/
                        /* Simply dump anything we get from the serial port */
                        /* to the client(s)...                              */
                        /*--------------------------------------------------*/
                        int bytesAvail = radioInput.available();
                        if( bytesAvail > 0 ) {
                            byte[] dataRead = new byte[bytesAvail];
                            radioInput.read(dataRead);
                            sendRadioDataToAllClients( dataRead );
                            somethingRead = true;
                        }
                    }
                    catch( IOException ioe ) {
                        System.out.println("Radio link event: "+ioe);
                    }
                }
                
                /*-------------------------------*/
                /* Take care of the rotator link */
                /*-------------------------------*/
                if( rotatorInput != null ) {
                    try{
                        /*--------------------------------------------------*/
                        /* Simply dump anything we get from the serial port */
                        /* to the client(s)...                              */
                        /*--------------------------------------------------*/
                        int bytesAvail = rotatorInput.available();
                        if( bytesAvail > 0 ) {
                            //System.out.println("Read something from rotator port");
                            byte[] dataRead = new byte[bytesAvail];                            
                            rotatorInput.read(dataRead);
                            System.out.println("Rotator port read: "+new String(dataRead));
                            sendRotatorDataToAllClients( dataRead );
                            somethingRead = true;
                        }
                    }
                    catch( IOException ioe ) {
                        System.out.println("ERROR reading rotator link: "+ioe);
                    }
                }

                /*------------------------------------------*/
                /* If nothing happened on the serial ports, */
                /* then sleep a litte, otherwise check back */
                /* right away (almost...)                   */
                /*------------------------------------------*/
                if( !somethingRead ) {
                    try{Thread.currentThread().sleep(50);}
                    catch(InterruptedException ie){}
                }
                else {
                    try{Thread.currentThread().sleep(10);}catch( InterruptedException ie ) {}
                }
                    
            }//wend
        }
        
        /*--------------------------------------------------------------------*/
        /*           This is a thread to monitor all other threads...         */
        /*--------------------------------------------------------------------*/
        else if( Thread.currentThread() == threadMonitor ) {    
            
            while( true ) {
                
                /*----------------------------------------------------------*/
                /* Do verification periodically, so sleep here 4 seconds... */
                /*----------------------------------------------------------*/
                try{ Thread.sleep( 4000 ); } catch( InterruptedException ie ){}
                
                /*----------------------------------------*/
                /* Verify all administrator's connections */
                /*----------------------------------------*/
                //System.out.println( 
                //     "Number of connected admins:" + adminsConnected.size() );
                for( int i=0; i<adminsConnected.size(); i++ ) {
                    AdminLink adm = (AdminLink)adminsConnected.get(i);
                    //Thread thread = (Thread)adminsConnected.get(i);
                    //System.out.print("Thread #"+i);
                    //if( adm.isConnectionAlive() ) System.out.println(" : alive");
                    //else System.out.println(" : Dead");
                    
                }
                /*-----------------------------------------------------*/
                /* Remove dead adminstrators connections from the list */
                /* (one at a time for simplicity...)                   */
                /*-----------------------------------------------------*/
                REMOVING_DEAD_ADMINS:
                for( int i=0; i<adminsConnected.size(); i++ ) {
                    AdminLink adm = (AdminLink)adminsConnected.get(i);
                    if( !adm.isConnectionAlive() ) {
                        adminsConnected.removeElementAt(i);
                        break REMOVING_DEAD_ADMINS;
                    }
                }

                /*---------------------------------*/
                /* Verify all client's connections */
                /*---------------------------------*/
                //System.out.println( 
                // "\nNumber of connected clients:" + clientsConnected.size() );
                for( int i=0; i<clientsConnected.size(); i++ ) {
                    ClientLink clt = (ClientLink)clientsConnected.get(i);
                    //System.out.print("Client #"+i);
                    //if(clt.isConnectionAlive()) System.out.println(" : alive");
                    //else System.out.println(" : Dead");                    
                }

                /*-----------------------------------------------*/
                /* Remove dead clients connections from the list */
                /* (one at a time for simplicity...)             */
                /*-----------------------------------------------*/
                REMOVING_DEAD_CLIENTS:
                for( int i=0; i<clientsConnected.size(); i++ ) {
                    ClientLink clt = (ClientLink)clientsConnected.get(i);
                    if( !clt.isConnectionAlive() ) {
                        clientsConnected.removeElementAt(i);
                        System.out.println("Removing client manager: "+i);
                        break REMOVING_DEAD_CLIENTS;
                    }
                }                
            }        
        /*============================*/
        /* ERROR, should never happen */
        /*============================*/
        } else {
            System.out.println("ERROR: Bad thread!");
        }
    }
    
    /*=======================================================================*/
    /** Used to force an admin connection to abort, and free corresponing    
     *  resources.
     *  @param adminToKill Index of the admin to kill, from admin Vector.    */
    /*=======================================================================*/
    public void killAdminConnection(int adminToKill) {
    /*=======================================================================*/
        if( adminToKill >= adminsConnected.size() ) {
            System.out.println("Error killing admin connection"); 
            return;
        }
        AdminLink admToKill = (AdminLink)adminsConnected.get(adminToKill);
        admToKill.kill();
    }
    /*=======================================================================*/
    /** Forces a client connection to abort, freeing corresponding resources
     *  @param clientToKill Index of client to terminate, from client Vector */
    /*=======================================================================*/
    public void killClientConnection(int clientToKill) {
    /*=======================================================================*/
        if( clientToKill >= clientsConnected.size() ) {
            System.out.println("Error killing client connection"); 
            return;
        }
        ClientLink cltToKill = (ClientLink)clientsConnected.get(clientToKill);
        cltToKill.kill();
    }
    
    /*=======================================================================*/
    /** Returns the number of admins currently connected. 
     *  @return Number of admins currently connected.                        */
    /*=======================================================================*/
    public int nbAdmins() {
    /*=======================================================================*/
        return( adminsConnected.size() );
    }
    
    /*=======================================================================*/
    /** Kills ourself, after terminating all connections and freeing 
     *  resources. FOR NOW USED ONLY ON ADMIN FOR TESTING...                 */
    /*=======================================================================*/
    public void kill() {
    /*=======================================================================*/
        System.out.println("KILLING ALL CLIENTS");
        for( int i=0; i<adminsConnected.size(); i++ ) {
            AdminLink adm = (AdminLink)adminsConnected.get(i);
            adm.kill();
        }                
        System.out.println("KILLING MYSELF");
        try{
            adminSocket.close();
        }catch(IOException ioe){}

        System.exit( 0 );
    }
    
    /*.......................................................................*/
    /*                                                                       */
    /*                      S  P  A  C  E  C  R  A  F  T                     */
    /*                                                                       */
    /*.......................................................................*/

    /*=======================================================================*/
    /** Initialize the serial port used to talk to the spacecraft.
     * @param port Serial port to use (format "COM1" for PC)
     * @param baudrate Speed of the serial port, in bits per second.         */
    /*=======================================================================*/
    public synchronized void initSpacecraftSerialPort( String port, int baudRate) {
    /*=======================================================================*/

        System.out.println("Initializing spacecraft serial port");
        if( spacecraftSerialPort != null ) {
            spacecraftSerialPort.notifyOnDataAvailable(false);
            spacecraftSerialPort.close();
        }

        /*-----------------------------------------------*/
        /* WIN98 PATCH TO GET MORE THAN COM1-4           */
        /* obtain instance of original SUN's com driver  */
        /*-----------------------------------------------*/
        CommDriver commdriver = null;
        try{ 
            commdriver = 
            (CommDriver)Class.forName("com.sun.comm.Win32Driver").newInstance();  
        } catch( ClassNotFoundException cnfe ) {
            System.out.println("ERROR loading serial port driver: "+cnfe);
        } catch( InstantiationException ie ) {
            System.out.println("ERROR instantiating serial port interface: "+ie);
        } catch( IllegalAccessException iae ) {
            System.out.println("ERROR accessing serial port driver: "+iae);
        }
        /*---------*/
        /* init it */
        /*---------*/
        commdriver.initialize();  
        
        /*------------------------------------------------*/
        /* add the new port (and it will be enumerated..) */
        /* (This is still the patch for Win98...)         */
        /*------------------------------------------------*/
        CommPortIdentifier.addPortName( "COM5", 
                                        CommPortIdentifier.PORT_SERIAL, 
                                        commdriver);
        CommPortIdentifier.addPortName( "COM6", 
                                        CommPortIdentifier.PORT_SERIAL, 
                                        commdriver);
        CommPortIdentifier.addPortName( "COM7", 
                                        CommPortIdentifier.PORT_SERIAL, 
                                        commdriver);
        CommPortIdentifier.addPortName( "COM8", 
                                        CommPortIdentifier.PORT_SERIAL, 
                                        commdriver);
        
        /*-----------------------------*/
        /* get list of available ports */
        /*-----------------------------*/
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        
        /*--------------------------------*/
        /* find the specified serial port */
        /*--------------------------------*/
        while( portList.hasMoreElements() ) {
            
            CommPortIdentifier portId=(CommPortIdentifier)portList.nextElement();
            System.out.println("Port Found: "+portId.getName());
            
            if( (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) 
                && portId.getName().equals( port.trim().toUpperCase() ) ) 
            {
                System.out.println("Setting port for spacecraft");
                /*-----------------------------------------*/
                /* create a new instance of the serial port*/
                /*-----------------------------------------*/
                try {
                    spacecraftSerialPort = 
                                (SerialPort) portId.open("spacecraft", 2000);
                } catch( PortInUseException piue ) {
                    System.err.println( "Port unavailable or already in use " +
                                        "for spacecraft" );
                    return;
                }
                
                /*------------------------*/
                /* initialize the streams */
                /*------------------------*/
                try {
                    
                    /*-------------------------------------*/
                    /* Do not forget to set the comm links */
                    /* I/O to these...                     */
                    /*-------------------------------------*/
                    if( spacecraftInput != null ) spacecraftInput.close();
                    if( spacecraftOutput != null ) spacecraftOutput.close();                    
                    spacecraftOutput  = spacecraftSerialPort.getOutputStream();
                    spacecraftInput   = spacecraftSerialPort.getInputStream();
                } catch( IOException ioe ) {
                    System.err.println( "Error setting spacecraft streams" );
                    return;
                }
                
                /*------------------------------*/
                /* add the appropriate listener */
                /*------------------------------*/
                try {
                    spacecraftSerialPort.addEventListener(this);
                } catch( TooManyListenersException tme ) {
                    System.out.println("Error adding event listener");
                    return;
                }
                
                /*--------------------------------------------------------*/
                /* specify the required protocol and                      */
                /* Enable 5 second timeout (so input.read() cannot block) */
                /*--------------------------------------------------------*/
                try {
                    spacecraftSerialPort.setSerialPortParams(baudRate,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_NONE);
                    spacecraftSerialPort.enableReceiveTimeout(5000);
                } catch( UnsupportedCommOperationException ucoe ) {
                    System.err.println( "Error setting spacecraft comm port" );
                    return;
                }                
                
                /*------------------*/
                /* Start interrupts */
                /*------------------*/
                spacecraftSerialPort.notifyOnDataAvailable(true);
                
            }//end if the right port
        }//wend looping thru all the ports
        
    }
    
    /*=======================================================================*/
    /** Pretty self-explanatory, it sets the speed of the serial port used
     *  to talk to the spacecraft.
     * @param baudRate Speed to be set (in bits per second)                  */
    /*=======================================================================*/
    public synchronized void setSpacecraftSerialPortBaudRate( int baudRate ) {
    /*=======================================================================*/
        
        if( spacecraftSerialPort == null ) return;                
        
        /*---------------------------------------------*/
        /* If the serial port is already opened, check */
        /* it config. If it is the same as the one we  */
        /* need, don't continue and leave it...        */
        /*---------------------------------------------*/
        if( baudRate == spacecraftSerialPort.getBaudRate() )
            return;
        
        /*-----------------------*/
        /* specify the new speed */
        /*-----------------------*/
        try {
            spacecraftSerialPort.setSerialPortParams(baudRate,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_NONE);
            spacecraftBaud = baudRate;
            sendSpacecraftPortInfoToAllClients();
        } catch( UnsupportedCommOperationException ucoe ) {
            System.err.println( "Error setting spacecraft comm port speed" );
            return;
        }
    }
    
    /*=======================================================================*/
    /** Sends the given chunk of data to all currently connected clients, 
     *  specified as "data coming from the spacecraft".
     *  @param data Byte array to be sent to all clients currently connected */
    /*=======================================================================*/
    public synchronized void sendSpacecraftDataToAllClients( byte[] data ) {
    /*=======================================================================*/                
        
        /*-------------------------------------------------*/
        /* For each client in the Vector, send the data... */
        /*-------------------------------------------------*/
        for( int i=0; i<clientsConnected.size(); i++ ) {
            ClientLink clt = (ClientLink)clientsConnected.get(i);            
            if( (clt != null) && clt.isConnectionAlive() ) {
                //System.out.print("Sending data to client #"+i);
                clt.sendSpacecraftData( data );
            }
        }
    }
    /*=======================================================================*/
    /** Sends s/c port line status to all currently connected clients.       */
    /*=======================================================================*/
    public synchronized void sendSpacecraftPortInfoToAllClients() {
    /*=======================================================================*/

        /*---------------------------------------------------*/
        /* For each client in the Vector, send the status... */
        /*---------------------------------------------------*/
        for( int i=0; i<clientsConnected.size(); i++ ) {
            ClientLink clt = (ClientLink)clientsConnected.get(i);            
            if( (clt != null) && clt.isConnectionAlive() ) {                
                clt.sendSpacecraftPortInfo( portLineStatus, spacecraftBaud );
            }
        }
    }
    
    /*=======================================================================*/
    /** Sends the given chunk of data to the spacecraft via its dedicated
     *  serial port.
     *  @param destination Identifies the virtual destination on the S/C side,
     *                     so that we know the speed of the serial port (for
     *                     example with direct connection bootloader is not at
     *                     the same speed as the PHT...)
     *  @param data Byte array to be sent to the spacecraftt                 */
    /*=======================================================================*/
    public synchronized void sendDataToSpacecraft(int destination,byte[] data) {
    /*=======================================================================*/    
        //System.out.println("sendDataToSpacecraft");
        /*--------------------------------------------------------*/
        /* Sets the right speed for the destination mentionned... */
        /*--------------------------------------------------------*/
//        switch( destination ) {
//            case DATA_BOOTLOADER:
//                setSpacecraftSerialPortBaudRate( scuQblBaud );
//                break;
//            case DATA_HOUSEKEEPING:
//                setSpacecraftSerialPortBaudRate( scuPhtBaud );
//                break;
//            case DATA_SPACECRAFT:
//                setSpacecraftSerialPortBaudRate( spacecraftBaud );
//                break;
//            default:
//                break;
//        }
        if( spacecraftOutput != null ) {
            System.out.println("Writing to sc serial port: "+new String(data));
            try{
                /*--------------------------*/
                /* Dump data to serial port */
                /*--------------------------*/
                spacecraftOutput.write( data );
            } catch( IOException ioe ) {
                System.out.println("Error sending data to spacecraft's serial port: "+ioe);
            }
        }
        else {
            System.out.println("WARNING: Attempting to send data to un-initialized spacecraft port");
        }
    }

    /*.......................................................................*/
    /*                                                                       */
    /*                             R  A  D  I  O                             */
    /*                                                                       */
    /*.......................................................................*/
    
    /*=======================================================================*/
    /** Initializes the serial port used to talk to the radio.
     * @param port Serial port identifier (ex: "COM1" for PC 1st comm port)
     * @param baudRate Speed of the serial port, in bits per second          */
    /*=======================================================================*/
    public synchronized void initRadioSerialPort( String port, int baudRate) {
    /*=======================================================================*/

        System.out.println("Initializing radio serial port");
        if( radioSerialPort != null ) {
            radioSerialPort.notifyOnDataAvailable(false);
            radioSerialPort.close();
        }
       
        /*-----------------------------*/
        /* get list of available ports */
        /*-----------------------------*/
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        
        /*--------------------------------*/
        /* find the specified serial port */
        /*--------------------------------*/
        while( portList.hasMoreElements() ) {
            
            CommPortIdentifier portId=(CommPortIdentifier)portList.nextElement();
            
            if( (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) 
                && portId.getName().equals( port.trim().toUpperCase() )) 
            {
                /*-----------------------------------------*/
                /* create a new instance of the serial port*/
                /*-----------------------------------------*/
                try {
                    radioSerialPort = (SerialPort) portId.open("radio", 2000);
                } catch( PortInUseException piue ) {
                    System.err.println("Radio port unavailable or already in use");
                    return;
                }
                
                /*------------------------*/
                /* initialize the streams */
                /*------------------------*/
                try {
                    
                    /*-------------------------------------*/
                    /* Do not forget to set the comm links */
                    /* I/O to these...                     */
                    /*-------------------------------------*/
                    if( radioInput != null ) radioInput.close();
                    if( radioOutput != null ) radioOutput.close();                    
                    radioOutput  = radioSerialPort.getOutputStream();
                    radioInput   = radioSerialPort.getInputStream();
                } catch( IOException ioe ) {
                    System.err.println( "Error setting radio streams" );
                    return;
                }
                /*--------------------------------------------------------*/
                /* specify the required protocol and                      */
                /* Enable 5 second timeout (so input.read() cannot block) */
                /*--------------------------------------------------------*/
                try {
                    radioSerialPort.setSerialPortParams(baudRate,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_NONE);
                    radioSerialPort.enableReceiveTimeout(5000);
                } catch( UnsupportedCommOperationException ucoe ) {
                    System.err.println( "Error setting radio comm port" );
                    return;
                }                
            }//end if the right port
        }//wend looping thru all the ports
        
    }
    
    /*=======================================================================*/
    /** Sets the speed of the serial port used to talk to the radio.
     * @param baudRate Speed to set, in bits per seconds.                    */
    /*=======================================================================*/
    public synchronized void setRadioSerialPortBaudRate( int baudRate ) {
    /*=======================================================================*/
        
        if( radioSerialPort == null ) return;
        
        /*---------------------------------------------*/
        /* If the serial port is already opened, check */
        /* it config. If it is the same as the one we  */
        /* need, don't continue and leave it...        */
        /*---------------------------------------------*/
        if( baudRate == radioSerialPort.getBaudRate() )
            return;
        
        /*-----------------------*/
        /* specify the new speed */
        /*-----------------------*/
        try {
            radioSerialPort.setSerialPortParams(baudRate,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_NONE);            
        } catch( UnsupportedCommOperationException ucoe ) {
            System.err.println( "Error setting radio comm port" );
            return;
        }                                
    }

    /*=======================================================================*/
    /** Sends a chunk of data (marked as coming from the radio) to all 
     *  connected clients.
     * @param data Byte array to send to all connected client.
    /*=======================================================================*/
    public synchronized void sendRadioDataToAllClients( byte[] data ) {
    /*=======================================================================*/                
        
        /*-----------------------------------------------------*/
        /* For each client in the Vector, send data from radio */
        /*-----------------------------------------------------*/
        for( int i=0; i<clientsConnected.size(); i++ ) {
            ClientLink clt = (ClientLink)clientsConnected.get(i);            
            if( (clt != null) && clt.isConnectionAlive() ) {
                System.out.print("Sending radio data to client #"+i);
                clt.sendRadioData( data );
            }
        }
    }
    
    /*=======================================================================*/
    /** Sends a chunk of data to the radio via its dedicated serial port.
     * @param data Byte array to send to the radio                           */
    /*=======================================================================*/
    public synchronized void sendDataToRadio( byte[] data ) {
    /*=======================================================================*/    
        //System.out.println("sendDataToRadio");
        if( !RADIO_ACTIVE ) {
            System.out.println("");
            System.out.print("Data to radio >>> ");
            for( int i=0; i<data.length; i++ ) {
                System.out.print("("+(int)data[i]+")");
            }
        } else if( radioOutput != null ) {
            //System.out.println("Writing to radio serial port");
            try{                
                radioOutput.write( data );
            } catch( IOException ioe ) {
                System.out.println("Error sending data to radio port: "+ioe);
            }
        }
    }
    
    /*.......................................................................*/
    /*                                                                       */
    /*                            T  N  C                                    */
    /*                                                                       */
    /*.......................................................................*/
    
    /*=======================================================================*/
    /** Sends a chunk of data to the TNC via the same serial port as the 
     *  spacecraft.
     * @param data Byte array to send to the TNC                             */
    /*=======================================================================*/
    public synchronized void sendDataToTnc( byte[] data ) {
    /*=======================================================================*/    
        //System.out.println("sendDataToTnc");
        if( !TNC_ACTIVE ) {
            System.out.println("");
            System.out.print("Data to TNC >>> ");
            for( int i=0; i<data.length; i++ ) {
                System.out.print("("+(int)data[i]+")");
            }
        } else if( spacecraftOutput != null ) {            
            //setSpacecraftSerialPortBaudRate( spacecraftBaud );
            try{
                /*--------------------------*/
                /* Dump data to serial port */
                /*--------------------------*/
                System.out.println("Writing to TNC: "+new String(data));
                spacecraftOutput.write( data );
            } catch( IOException ioe ) {
                System.out.println("Error sending data to spacecraft's serial port: "+ioe);
            }
        }
        else {
            System.out.println("WARNING: Attempting to send data to un-initialized spacecraft port");
        }

    }
    
    /*.......................................................................*/
    /*                                                                       */
    /*                         R  O  T  A  T  O  R                           */
    /*                                                                       */
    /*.......................................................................*/
    
    /*=======================================================================*/
    /** Initializes the serial port used to talk to the antenna rotator.
     * @param port Comm port identifier (ex: "COM1" for PC 1st serial port)
     * @param baudRate Speed of the serial port, in bits per second.         */
    /*=======================================================================*/
    public synchronized void initRotatorSerialPort( String port, int baudRate) {
    /*=======================================================================*/

        System.out.println("Initializing rotator serial port");
        if( rotatorSerialPort != null ) {
            rotatorSerialPort.notifyOnDataAvailable(false);
            rotatorSerialPort.close();
        }
       
        /*-----------------------------*/
        /* get list of available ports */
        /*-----------------------------*/
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        
        /*--------------------------------*/
        /* find the specified serial port */
        /*--------------------------------*/
        while( portList.hasMoreElements() ) {
            
            CommPortIdentifier portId=(CommPortIdentifier)portList.nextElement();
            
            if( (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) 
                && portId.getName().equals( port.trim().toUpperCase() )) 
            {
                /*-----------------------------------------*/
                /* create a new instance of the serial port*/
                /*-----------------------------------------*/
                try {
                    rotatorSerialPort = (SerialPort)portId.open("rotator",2000);
                } catch( PortInUseException piue ) {
                    System.err.println( "Rotator port unavailable or already in use" );
                    return;
                }
                
                /*------------------------*/
                /* initialize the streams */
                /*------------------------*/
                try {
                    
                    /*-------------------------------------*/
                    /* Do not forget to set the comm links */
                    /* I/O to these...                     */
                    /*-------------------------------------*/
                    if( rotatorInput != null ) rotatorInput.close();
                    if( rotatorOutput != null ) rotatorOutput.close();                    
                    rotatorOutput  = rotatorSerialPort.getOutputStream();
                    rotatorInput   = rotatorSerialPort.getInputStream();
                } catch( IOException ioe ) {
                    System.err.println( "Error setting rotator streams" );
                    return;
                }
                /*--------------------------------------------------------*/
                /* specify the required protocol and                      */
                /* Enable 5 second timeout (so input.read() cannot block) */
                /*--------------------------------------------------------*/
                try {
                    rotatorSerialPort.setSerialPortParams(baudRate,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_NONE);
                    rotatorSerialPort.enableReceiveTimeout(5000);
                } catch( UnsupportedCommOperationException ucoe ) {
                    System.err.println( "Error setting rotator comm port" );
                    return;
                }                
            }//end if the right port
        }//wend looping thru all the ports
        
    }
    
    /*=======================================================================*/
    /** Sets the speed of the serial port used to talk to the rotator.
     * @param baudRate Speed to set, in bits per second                      */
    /*=======================================================================*/
    public synchronized void setRotatorSerialPortBaudRate( int baudRate ) {
    /*=======================================================================*/
        
        if( rotatorSerialPort == null ) return;
        
        /*---------------------------------------------*/
        /* If the serial port is already opened, check */
        /* it config. If it is the same as the one we  */
        /* need, don't continue and leave it...        */
        /*---------------------------------------------*/
        if( baudRate == rotatorSerialPort.getBaudRate() )
            return;
        
        /*-----------------------*/
        /* specify the new speed */
        /*-----------------------*/
        try {
            rotatorSerialPort.setSerialPortParams(baudRate,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_NONE);            
        } catch( UnsupportedCommOperationException ucoe ) {
            System.err.println( "Error setting rotator comm port" );
            return;
        }                                
    }

    /*=======================================================================*/
    /* Sends a data chunk (marked as coming from the antenna rotator) to all
     * connected clients.
     * @param data Byte array to send to all connected clients               */
    /*=======================================================================*/
    public synchronized void sendRotatorDataToAllClients( byte[] data ) {
    /*=======================================================================*/                
        
        /*----------------------------------------------------*/
        /* for each client in the vector, send the data chunk */
        /*----------------------------------------------------*/
        for( int i=0; i<clientsConnected.size(); i++ ) {
            ClientLink clt = (ClientLink)clientsConnected.get(i);            
            if( (clt != null) && clt.isConnectionAlive() ) {
                System.out.print("Sending rotator data to client #"+i);
                clt.sendRotatorData( data );
            }
        }
    }
    
    /*=======================================================================*/
    /* Sends a data chunk to the rotator controller via its dedicated serial
     * port.
     * @param data Byte array to send to the rotator controller.             */
    /*=======================================================================*/
    public synchronized void sendDataToRotator( byte[] data ) {
    /*=======================================================================*/    
        //System.out.println("sendDataToRotator");
        if( !ROTATOR_ACTIVE ) {
            System.out.println("");
            System.out.print("Data to rotator >>> ");
            for( int i=0; i<data.length; i++ ) {
                System.out.print("("+(int)data[i]+")");
            }
        } else if( rotatorOutput != null ) {
            //System.out.println("Writing to rotator serial port");
            try{
                System.out.println("");
                System.out.print("Data to rotator >>> ");
                for( int i=0; i<data.length; i++ ) {
                    System.out.print("("+(int)data[i]+")");
                }
                rotatorOutput.write( data );
            } catch( IOException ioe ) {
                System.out.println("Error sending data to rotator port: "+ioe);
            }
        }
    }

    /*=======================================================================*/
    /** Method called every time the system sense a char coming through the 
     *  serial port. Only interrupts the serial port listener thread that we
     *  defined, to get better response time. We're basically polling but with
     *  some little help here...
     *  The reason we're polling is to get better control, until we fully 
     *  understand how this damn interrupt react in all circumstances...
     * @param event The serial event
    /*=======================================================================*/
    public void serialEvent(javax.comm.SerialPortEvent event) {
    /*=======================================================================*/        
        switch(event.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:                
                threadSerialPortsListener.interrupt();
                break;
        }
    }
    
}
