/*============================================================================
 * jload.java
 *
 * Created on December 21, 2000, 4:10 PM
 * Copyrights Canadian Space Agency
 * Jean-Francois Cusson, Ing. QuickSat Project
 *
 *============================================================================
 *
 * jload is a utility for uploading flight software binaries into the
 * QuickSat spacecraft, using a TNC connected to the serial port of the
 * computer. AX-25 frames are packetized into KISS frames before being
 * sent to the TNC. The load protocol is custom.
 *
 */

package ca.gc.space.quicksat.ground.client;
/*ello lp*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.comm.*;
import java.net.*;


import ca.gc.space.quicksat.ground.*;
import ca.gc.space.quicksat.ground.control.*;
import ca.gc.space.quicksat.ground.pacsat.*;
import ca.gc.space.quicksat.ground.radio.*;
import ca.gc.space.quicksat.ground.rotator.*;
import ca.gc.space.quicksat.ground.satellite.*;
import ca.gc.space.quicksat.ground.telemetry.*;
import ca.gc.space.quicksat.ground.tnc.*;
import ca.gc.space.quicksat.ground.tracking.*;
import ca.gc.space.quicksat.ground.util.*;
import ca.gc.space.quicksat.ground.video.*;
import ca.gc.space.quicksat.ground.ax25.Frame;
import ca.gc.space.quicksat.ground.ax25.*;
import ca.gc.space.quicksat.ground.pacsat.*;
import ca.gc.space.quicksat.ground.satellite.Satellite;
//import qsgs.model.*;


/**
 *
 * @author  jfcusson & enrique Benedicto
 * @version
 */

/*-----------------------------------------------------------------------------------------*/
public class jload extends javax.swing.JFrame implements Runnable {
/*-----------------------------------------------------------------------------------------*/

    /*-----------------------------------------*/
    /* Configuration of what panel to display! */
    /*-----------------------------------------*/
    SatMapper mapper;
    public static long timeOffset           = 0;
    public static java.sql.Timestamp lastSync       = null;
    
    public static Satellite aSat            = null;
    private final boolean SHOW_PACSAT_PANEL = false;
    private final boolean SHOW_TEST_PANEL   = false;
    private final boolean SHOW_TNC_PANEL    = true;
    private final boolean SHOW_ROTATOR_PANEL= true;
    private final boolean SHOW_HEALTH_PANEL = false;
    private final boolean SHOW_ACS_PANEL    = false;
    private final boolean SHOW_ORBIT_PANEL = true;
    
    private String currentDir = null;

    public Log log = null;
    
    private boolean appRunning = true;
    
    /*--------------------------------------------------*/
    /* Maximum number of uploadable tasks in the upload */
    /* table                                            */
    /*--------------------------------------------------*/
    //private final int MAX_TASK_UPLOAD  = 15;
        
    private final int SCRIPT_STANDBYE   = 0;
    private final int SCRIPT_RUNNING    = 1;
    private final int SCRIPT_RELOADING  = 2;
    private final int SCRIPT_RELOADED   = 3;
    private final int SCRIPT_CLEARING   = 4;
    private final int SCRIPT_CLEARED    = 5;
    private final int SCRIPT_PAUSING    = 6;
    private final int SCRIPT_PAUSED     = 7;
    private int scriptState             = SCRIPT_STANDBYE;
    private int scriptStep              = 0;
    private Vector scriptVector         = null;
    private boolean isNewIncomingData   = false;
    private TaskLoader taskLoader       = null;
    
    /*-----------------------------------*/
    /* general variables and declaration */
    /*-----------------------------------*/    
    private ServerLink      srvLnk = null;
    private SerialPort      serialPort;
    public boolean          useSCU      = false;
    public boolean          initSerialPortAtStartup = false;
    public String           scuPhtPort  = "COM1";
    public int              scuPhtBaud  = 9600;
    public String           scuQblPort  = "COM1";
    public int              scuQblBaud  = 9600;
    public String           tncPort     = "COM1";
    public int              tncBaud     = 9600;
    public String           radioPort   = "COM2";
    public int              radioBaud   = 9600;
    public boolean          radioEnabled    = false;
    private SerialPort      radioSerialPort = null;
    private InputStream     radioInput      = null;
    private DataInputStream radioDataIn     = null;
    private OutputStream    radioOutput     = null;
    private DataOutputStream radioDataOut   = null;
    public  int             timeout         = 2000;
    private InputStream     input           = null;
    private DataInputStream satDataIn       = null;
    private OutputStream    output          = null;
    private DataOutputStream satDataOut     = null;
//    private  KISSFrame      kissFrame;
    Thread                  scriptThread = null;
    Thread                  spacecraftCommThread = null;
    Thread                  pacsatSessionThread = null;
    Thread                  satTrackThread = null;
    Thread                  radioCommThread = null;
    Thread                  antennaRotatorCommThread = null;
    public String           strInfo;
    public Runnable         writeStatus;
    LoadProgressGUI         loadProgressGUI;
    InfoGUI                 infoGUI;
    public boolean          load1GO = false;
    javax.swing.Timer       timer;
    Vector                  ldrFramesVector;
    int                     destSSID;
    
    //Status
    boolean     statusSerialPortInit = false;
    boolean     statusTNCInit = false;
    int         statusRXCount = 0;
    int         statusTXCount = 0;
    int         nbSpacecraftFramesReceived = 0;
    String      fileName;
    String      taskName;
    int         fileSize;
    byte        fileSizeMsb;
    byte        fileSizeLsb;
    int         exeSize;
    int         binSize;
    String      taskPriority = null;
    int         binSizeParagraphs;
    int         binSizeParagraphsMsb;
    int         binSizeParagraphsLsb;
    int         segment=0;
    int         segmentMsb = 0;
    int         segmentLsb = 0;
    
    boolean     ackLoadRequest = false;
    boolean     aliveLabel_ON = false;
    boolean     cancelLoad = false;
    boolean     loadInProgress = false;
    boolean     isResuming = false;
    
    int countDownTimer = 30000;
    int expiredTime=0;
    int timerStart=0;
    
    int scuDataChunkSize = 220;
    int tncDataChunkSize = 220;
    int dataChunkSize = 220;
    //final int DATA_CHUNK_SIZE_INCREMENT = 10;
    
    int rxErrorsSinceReset = 0;
    int rxFramesSinceReset = 0;
    int timeoutsCount = 0;
    int rxErrorsCount = 0;
    
    
    boolean fileSystemACK = false;
    boolean fileSystemNACK = false;
    
    
    
    String baseDir = "C:/JLOAD/";
    String securityCodesFilename = "C:/qsgs_codes.txt";
    String scriptsDir = baseDir;
    String uploadTasksDir = baseDir;
    
    YaesuFT847 radio = null;
    
    Bootloader qbl = null;      //Defines QuickSat bootloader
    //PHT pht = null;
    Telemetry tlm = null;
    //CommLink bootloaderLink= null;
    //CommLink phtLink= null;
    ReactionWheel reactionWheel = null;
    
    boolean justEditedWheelSpeed = false;
    boolean justEditedWheelTorque = false;
    
    boolean logbookChanged = false;

    public PanelHealth             panelHealth             = null;
    public PanelACS_cmd            panelACS_cmd            = null;
    public PanelEDAC_cmd           panelEDAC_cmd           = null;
    public PanelHeaters_cmd        panelHeaters_cmd        = null;
    public PanelHouseKeeping_cmd   panelHouseKeeping_cmd   = null;
    public PanelIO_cmd             panelIO_cmd             = null;
    public PanelLogBook            panelLogBook            = null;
    public PanelModems_cmd         panelModems_cmd         = null;
    public PanelPACSAT             panelPACSAT             = null;
    public PanelPayload_cmd        panelPayload_cmd        = null;
    public PanelPower_cmd          panelPower_cmd          = null;
    public PanelPyros_cmd          panelPyros_cmd          = null;
    public PanelRadio              panelRadio              = null;
    public PanelSatellites         panelSatellites         = null;
    public PanelSettings           panelSettings           = null;
    public PanelTNC                panelTNC                = null;
    public PanelRotator            panelRotator            = null;
    public PanelTX_cmd             panelTX_cmd             = null;
    public PanelTelemetry          panelTelemetry          = null;
    public PanelTesting            panelTesting            = null;
    public PanelTracking           panelTracking           = null;
            //I MADE CHANGES HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public PanelOrbit               panelOrbit              = null;
    //PanelACS                panelACS                = null;
    
    Satellite       currentSat = null;
    Satellite       previousSat = null;
    AntennaRotator  antennaRotator = null;
    
    Thread initProgressThread = null;
    DialogAbout dialogIntro = null;
    
    
    //final int clientPort = 9902;
    
    /*------------------------------------------*/
    /* Put to true to terminate the thread that */
    /* reads serial port or network inputs      */
    /*------------------------------------------*/
    boolean stopReadingInputs = false;
    
    /*-----------*/
    /* TEMPORARY */
    /*-----------*/
    boolean isExtendedLoadProto = true;
    
    /*-----------------*/
    /* Pass management */
    /*-----------------*/
    boolean isPassPaused = false; //Pause everything we would normally do
    boolean isPassActive = false; //Sat in range
    boolean isPassIgnored = false; //means pass always active!
    
    /*-------------------*/
    /* AX25 input stream */
    /*-------------------*/
    ///private ax25.FrameInputStream ax25is = null;
    
    /*--------------------------------------------------------*/
    /* PACSAT Management                                      */
    /* ENVENTUALLY ALL OF THIS SHOULD BE PUT INTO AN OBJECT!! */
    /* IN THE PACSAT PACKAGE!!!                               */
    /*--------------------------------------------------------*///$$$
    private boolean pacsatSessionActive = false;
    
    private final int AX25_PACSAT_DISCONNECTED      = 0;
    private final int AX25_PACSAT_CONNECTING        = 1;
    private final int AX25_PACSAT_CONNECTED         = 2;
    private int ax25PacsatLinkState = AX25_PACSAT_DISCONNECTED;
    
    private final int PACSAT_SESSION_DISCONNECTED   = 0;
    private final int PACSAT_SESSION_CONNECT_PEND   = 1;
    private final int PACSAT_SESSION_CONNECTING     = 2;
    private final int PACSAT_SESSION_CONNECTED      = 3;
    private final int PACSAT_SESSION_DISCONNECTING  = 4;
    private final int PACSAT_SESSION_FRAMEREJECT    = 5;
    private int pacsatSessionState = PACSAT_SESSION_DISCONNECTED;
    
    /*------------------------------------------------------------------------*/
    /** Creates new form jload                                                */
    /*------------------------------------------------------------------------*/
    public jload() {
    /*------------------------------------------------------------------------*/
        
        /*-----------------------------*/
        /* Initializes the application */
        /*-----------------------------*/
        initComponents ();
        pack ();
        
                //I MADE CHANGES HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        mapper = new SatMapper(baseDir,this);
        /*---------------------------------------------*/
        /* This object will allow us to log all events */
        /*---------------------------------------------*/
        log = new Log();
        
        /*--------------------------------*/
        /* Center the panel on the screen */
        /*--------------------------------*/
        setSize( new Dimension( 1000,700 ) );
        Dimension dim = getToolkit().getScreenSize();
        Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2,
                    (dim.height - abounds.height) / 2);

        /*---------------------------------------------------------*/
        /* Show the ABOUT dialog box (modal) to show init progress */
        /*---------------------------------------------------------*/        
        initProgressThread = new Thread( this );
        initProgressThread.start();
        while( dialogIntro == null )
            try{Thread.currentThread().sleep(500);}catch(InterruptedException ie){}
        
        /*----------------------------------------------------*/
        /* Initialize the QuickSat bootloader and PHT objects */
        /*----------------------------------------------------*/
        qbl = new Bootloader();
        //pht = new PHT( baseDir, dialogIntro );
        tlm = new Telemetry( baseDir, this );
        tlm.loadItemDescr("qsgs_tlm.txt", dialogIntro);
        
        /*----------------------------------------------------*/
        /* Initialize an object who knows how to speak to the */
        /* reaction wheel...                                  */
        /*----------------------------------------------------*/
        reactionWheel = new ReactionWheel();
        if( reactionWheel == null )
            dialogIntro.appendInfo("ERROR: Unable to instanciate reactionWheel\n");
        else
            dialogIntro.appendInfo("reactionWheel instanciated\n");
        
        /*-----------------------------------------------*/
        /* Initialize the RADIO object                   */
        /* (Eventually we should support more radios...) */
        /*-----------------------------------------------*/
        radio = new YaesuFT847( );
        if( radio == null )
            dialogIntro.appendInfo("ERROR: Unable to instanciate radio\n");
        else
            dialogIntro.appendInfo("radio instanciated\n");
    
        /*----------------------------------------*/
        /* Initialize the antenna rotator object  */
        /* (here again we should support more)    */
        /*----------------------------------------*/
        antennaRotator = new GS232();
        if( antennaRotator == null )
            dialogIntro.appendInfo("ERROR: Unable to instanciate antennaRotator\n");
        else
            dialogIntro.appendInfo("antennaRotator instanciated\n");
        
        /*-----------------------------------------------------------*/
        /* Initialize the command communication link object, so that */
        /* it speaks to the bootloader in KISS mode                  */
        /*-----------------------------------------------------------*/
        //bootloaderLink = new CommLink( "Bootloader Link", CommLink.PROTO_KISS );        
        //bootloaderLink.setInterlocutor( CommLink.BOOTLOADER );
        //bootloaderLink.setVerbose( true ); //To view error messages
        
        //phtLink = new CommLink( "PHT Link", CommLink.PROTO_AX25_IN_KISS );
        //phtLink.setInterlocutor( CommLink.HOUSEKEEPING );
        //phtLink.setVerbose( true ); //To view error messages
               
        /*------------*/
        /* Add panels */
        /*------------*/
        panelACS_cmd = new PanelACS_cmd( log );
        commandTabs.add( panelACS_cmd, "ACS" );
        
        panelEDAC_cmd = new PanelEDAC_cmd( log );
        commandTabs.add( panelEDAC_cmd, "EDAC" );
        
        panelHeaters_cmd = new PanelHeaters_cmd( log );
        commandTabs.add( panelHeaters_cmd, "Heaters" );
        
        panelHouseKeeping_cmd = new PanelHouseKeeping_cmd( log );
        commandTabs.add( panelHouseKeeping_cmd, "Housekeeping" );
        
        panelIO_cmd = new PanelIO_cmd( log );
        commandTabs.add( panelIO_cmd, "I/O" );
        
        panelLogBook = new PanelLogBook( baseDir );
        panelInfo.add( panelLogBook, "logbook" );
        
        panelModems_cmd = new PanelModems_cmd( log );
        commandTabs.add( panelModems_cmd, "Modems" );
        
        panelPACSAT = new PanelPACSAT( this, log );
        if( SHOW_PACSAT_PANEL )
            panelMain.add( panelPACSAT, "PACSAT" );
        
        panelPayload_cmd = new PanelPayload_cmd( log );
        commandTabs.add( panelPayload_cmd, "Payload" );
        
        panelPower_cmd = new PanelPower_cmd( log );
        commandTabs.add( panelPower_cmd, "Power" );
        
        panelPyros_cmd = new PanelPyros_cmd( log );
        commandTabs.add( panelPyros_cmd, "Pyros" );
        
        
       
      
        
        //I MADE CHANGES HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        panelSatellites = new PanelSatellites( this, baseDir,mapper );
        panelMain.add( panelSatellites, "Satellites");
        
        panelRadio = new PanelRadio( panelSatellites, radio );
        panelMain.add( panelRadio, "Radio" );
        
        panelSettings = new PanelSettings( this );
        panelInfo.add( panelSettings, "settings" );
        
        panelTNC = new PanelTNC( log );
        if( SHOW_TNC_PANEL )
            panelMain.add( panelTNC, "TNC" );

        panelRotator = new PanelRotator( log );
        if( SHOW_ROTATOR_PANEL )
            panelMain.add( panelRotator, "Rotator" );
        
        
        panelTX_cmd = new PanelTX_cmd( log );
        commandTabs.add( panelTX_cmd, "TX" );
        
        panelTelemetry = new PanelTelemetry( log );
        panelInfo.add( panelTelemetry, "telemetry" );

        
        panelHealth = new PanelHealth( log );
        if( SHOW_HEALTH_PANEL ) {
            panelMain.add( panelHealth, 0 );        
            panelMain.setTitleAt(0,"Health");
        }
        
        panelTesting = new PanelTesting( log );
        if( SHOW_TEST_PANEL )
            panelMain.add( panelTesting, "Test" );
        
        panelTracking = new PanelTracking();
        panelMain.add( panelTracking, "Track" );
        
        panelOrbit = new PanelOrbit(mapper);
        if(SHOW_ORBIT_PANEL)
        panelMain.add(panelOrbit,"Orbit Tracking");
        

        //panelMain.setComponentAt(1,panelTracking);
        
        //panelACS = new PanelACS();
        //if( SHOW_ACS_PANEL )
        //    panelMain.add( panelACS, "ACS" );
        
        
        radio.fillCTCSSComboBox( panelRadio.listRadioRXCTCSS );
        radio.fillCTCSSComboBox( panelRadio.listRadioTXCTCSS );
        
        
        
        /*------------------------------------------*/
        /* Initialize the flight modems CTCSS lists */
        /*------------------------------------------*/
        SatControl satCtrl = new SatControl( baseDir );
        for( int i=0; i<satCtrl.getCTCSSListSize(); i++ ) {
            panelModems_cmd.listModemACTCSS.addItem((CTCSS)satCtrl.getCTCSS(i));
            panelModems_cmd.listModemBCTCSS.addItem((CTCSS)satCtrl.getCTCSS(i));
        }
        satCtrl.finalize();
        satCtrl = null;
        
        /*----------------------------------------*/
        /* Initialize the flight modemB rate list */
        /*----------------------------------------*/
        panelModems_cmd.listModemBRate.addItem( "9600" );
        panelModems_cmd.listModemBRate.addItem( "4800" );
        panelModems_cmd.listModemBRate.addItem( "2400" );
        panelModems_cmd.listModemBRate.addItem( "1200" );
        
        
        /*-------------------------------------------------*/
        /* Setup PHT commands mode (in the menu) group and */
        /* make sure the normal mode is selected...        */
        /*-------------------------------------------------*/
        ButtonGroup radiogroupPHTCommandsMode = new ButtonGroup();
        radiogroupPHTCommandsMode.add( radPHTCommandsPrime );
        radiogroupPHTCommandsMode.add( radPHTCommandsBackup );
        radPHTCommandsPrime.setSelected( true );
        
        /*----------------------------------------------------*/
        /* Setup load protocole selection (in the menu) group */
        /* and make sure the standard protocole is selected   */
        /*----------------------------------------------------*/
        ButtonGroup radiogroupLoadProto = new ButtonGroup();
        radiogroupLoadProto.add( radUseExtendedLoadProto );
        radiogroupLoadProto.add( radUseStandardLoadProto );
        radUseStandardLoadProto.setSelected( true );
        isExtendedLoadProto = false;
        
        /*-----------------------------*/
        /* Open the configuration file */
        /*-----------------------------*/
        FileInputStream fis = null;
        DataInputStream dis = null;
        String lineRead = null;
        
        chkDynamicPacketSize.setSelected( true );
        
        /*-------------------------------------------------------------*/
        /* Fill the upload script selection list (files with extension */
        /* ".script" in base directory)...                             */
        /*-------------------------------------------------------------*/
        Filter filenameFilter = new Filter( ".script" );
        File dir = new File(baseDir);
        File[] scriptFiles = dir.listFiles(filenameFilter);        
        for( int i=0; i<scriptFiles.length; i++ ) {            
            listScripts.addItem((String)scriptFiles[i].getName());
        }
        dir = null;
        scriptFiles = null;
        
        
        try {
            fis = new FileInputStream(baseDir+"qsgs_cfg.txt");
            dis = new DataInputStream( fis );
            //System.out.println("Found file '"+baseDir+"qsgs_cfg.txt': Loading configuration");
            dialogIntro.appendInfo("Found file '"+baseDir+"qsgs_cfg.txt': Loading configuration\n");
            
            while( (dis.available()>0) ) {
                lineRead = dis.readLine();
                
                /*.................................................................................*/
                if( lineRead.equalsIgnoreCase("[CODE FILE]") && (dis.available()>0) ) {
                /*.................................................................................*/    
                    securityCodesFilename = dis.readLine().trim();
                }                    
                /*.................................................................................*/
                if( lineRead.equalsIgnoreCase("[SCU-BOOTLOADER-BAUDRATE]") && (dis.available()>0) ) {
                /*.................................................................................*/    
                    scuQblPort = dis.readLine().trim();
                    try{ scuQblBaud = Integer.parseInt(dis.readLine()); }
                    catch( NumberFormatException nfe ) {}
                }
                /*.................................................................................*/
                if( lineRead.equalsIgnoreCase("[NETWORK SERVER]") && (dis.available()>0) ) {
                /*.................................................................................*/    

                    /*-------------------------*/
                    /* Format of the sestion:  */
                    /* Name                    */
                    /* Network Address         */
                    /* Latitude/deg            */
                    /* Longitude               */
                    /* Altitude/m              */
                    /* First empty line=finish */
                    /*-------------------------*/
                    //panelSettings.setServerName(dis.readLine().trim());
                    //NetworkStation netStation = new NetworkStation( 
                    //                                dis.readLine().trim(), //Name
                    //                                dis.readLine().trim()  //Address
                    //                                );
                    GroundStation gs = new GroundStation();
                    READ_STATION_PARAMETERS:
                    {
                        /*------*/
                        /* Name */
                        /*------*/
                        lineRead = dis.readLine().trim();
                        if(lineRead.length()<=0) break READ_STATION_PARAMETERS;
                        gs.setName( lineRead );
                        
                        /*-----------------*/
                        /* Network Address */
                        /*-----------------*/
                        lineRead = dis.readLine().trim();
                        if(lineRead.length()<=0) break READ_STATION_PARAMETERS;
                        gs.setNetworkAddress( lineRead );
                        
                        /*--------------------------*/
                        /* Coordinates lat/long/alt */
                        /*--------------------------*/
                        try {
                            lineRead = dis.readLine().trim();
                            if(lineRead.length()<=0) break READ_STATION_PARAMETERS;
                            gs.setLatitude( Double.parseDouble(lineRead) );
                            lineRead = dis.readLine().trim();
                            if(lineRead.length()<=0) break READ_STATION_PARAMETERS;
                            gs.setLongitude( Double.parseDouble(lineRead) );
                            lineRead = dis.readLine().trim();
                            if(lineRead.length()<=0) break READ_STATION_PARAMETERS;
                            gs.setAltitude( Integer.parseInt(lineRead) );
                        } catch( NumberFormatException nfe ) {
                            System.out.println("ERROR parsing ground station parameters: "+nfe);
                        }
                        
                    }
                    
                    listStations.addItem( (GroundStation)gs );                            
                }
                /*.................................................................................*/
                else if( lineRead.equalsIgnoreCase("[SCU-PHT-BAUDRATE]") && (dis.available()>0) ) {
                /*.................................................................................*/    
                    scuPhtPort = dis.readLine().trim();
                    try{ scuPhtBaud = Integer.parseInt(dis.readLine()); }
                    catch( NumberFormatException nfe ) {}
                }
                /*.................................................................................*/
                else if( lineRead.equalsIgnoreCase("[TNC-BAUDRATE]") && (dis.available()>0) ) {
                /*.................................................................................*/    
                    tncPort = dis.readLine().trim();
                    try{ tncBaud = Integer.parseInt(dis.readLine()); }
                    catch( NumberFormatException nfe ) {}
                }
                /*.................................................................................*/
                else if( lineRead.equalsIgnoreCase("[RADIO-BAUDRATE]") && (dis.available()>0) ) {
                /*.................................................................................*/    
                    radioPort = dis.readLine().trim();
                    try{
                        radioBaud = Integer.parseInt(dis.readLine());
                        radioEnabled = true;
                    }
                    catch( NumberFormatException nfe ) {}                    
                }
                /*.................................................................................*/
                else if( lineRead.equalsIgnoreCase("[USE-SCU]") && (dis.available()>0) ) {
                /*.................................................................................*/    
                    useSCU = dis.readLine().trim().equalsIgnoreCase("YES");
                }
                /*.................................................................................*/
                else if( lineRead.equalsIgnoreCase("[INIT-SERIAL-PORT-AT-STARTUP]") && (dis.available()>0) ) {
                /*.................................................................................*/    
                    initSerialPortAtStartup = dis.readLine().trim().equalsIgnoreCase("YES");
                }
                /*.................................................................................*/
                else if( lineRead.equalsIgnoreCase("[SCU-INITIAL-LOADER-BLOCK-SIZE]") && (dis.available()>0) ) {
                /*.................................................................................*/    
                    //scuDataChunkSize = dis.readLine().trim();
                    try{ scuDataChunkSize = Integer.parseInt(dis.readLine()); }
                    catch( NumberFormatException nfe ) {}
                }
                /*.................................................................................*/
                else if( lineRead.equalsIgnoreCase("[TNC-INITIAL-LOADER-BLOCK-SIZE]") && (dis.available()>0) ) {
                /*.................................................................................*/    
                    //tncDataChunkSize = dis.readLine().trim();
                    try{ tncDataChunkSize = Integer.parseInt(dis.readLine()); }
                    catch( NumberFormatException nfe ) {}
                }
                /*.................................................................................*/
                else if( lineRead.equalsIgnoreCase("[LOADER-BLOCK-SIZE-DYNAMIC]") && (dis.available()>0) ) {
                /*.................................................................................*/
                    chkDynamicPacketSize.setSelected( dis.readLine().trim().equalsIgnoreCase("YES") );
                }
                
            }
            dis.close();
            fis.close();
        }catch( IOException ioe ){
            //System.out.println("Configuration file '"+baseDir+"qsgs_cfg.txt' not found");
            dialogIntro.appendInfo("Configuration file '"+baseDir+"qsgs_cfg.txt' not found\n");
        }
        
        /*--------------------------------------------*/
        /* Load the security codes for each satellite */
        /*--------------------------------------------*/
        panelSatellites.loadSecurityCodes( securityCodesFilename );
        
        /*----------------------------------------*/
        /* Initialize the list of available SSIDs */
        /*----------------------------------------*/
        listSSID.addItem("1");
        listSSID.addItem("8");
        //phtLink.setDestinationSSID( 0x01 );
        
        /*-------------------------------------------------*/
        /* Set communication parameters, and security keys */
        /*-------------------------------------------------*/
        //phtLink.setSourceIdentification( "COMND", 0x01 );
        //phtLink.setDestinationIdentification( panelSatellites.getSelectedSatellite().getControlCallsign() );
//        pht.setControlKey( panelSatellites.getSelectedSatellite().getControlKey() );
//        qbl.setEncryptionKeys(  panelSatellites.getSelectedSatellite().getBootloaderAVAL(),
//                                panelSatellites.getSelectedSatellite().getBootloaderBVAL(),
//                                panelSatellites.getSelectedSatellite().getBootloaderD3VAL(),
//                                panelSatellites.getSelectedSatellite().getBootloaderK1H(),
//                                panelSatellites.getSelectedSatellite().getBootloaderK1L() );

        
        /*---------------------------------*/
        /* Initialize the load packet size */
        /*---------------------------------*/
        if( useSCU ) {            
            dataChunkSize = scuDataChunkSize;
        } else {            
            dataChunkSize = tncDataChunkSize;
        }
        slidPacketSize.setValue(dataChunkSize);
        
        /*----------------------------------*/
        /* Initialize the ports to hardware */
        /*----------------------------------*/
        enableNetworkConnectionToServer();

        /*--------------------------------------------------------------*/
        /* Setup an AX25 input stream from the input stream coming from */
        /* the spacecraft via the network server link.                  */
        /*--------------------------------------------------------------*/
        try{Thread.currentThread().sleep(100);}catch(InterruptedException e){}
        //ax25is=new ax25.FrameInputStream(srvLnk.getSpacecraftInputStream());
        //if( ax25is == null ) {
        //    System.out.println("ERROR: Unable to instanciate AX25 input stream");
        //    appRunning = false;
        //    try{Thread.currentThread().sleep(2000);}
        //    catch(InterruptedException ie){}            
        //    System.exit(0);
        //}
        //panelTNC.setAx25is( ax25is );
        //ax25is.useKISS();
        try{Thread.currentThread().sleep(100);}catch(InterruptedException e){}        
        
        /*----------------------*/
        /* Start reading inputs */
        /*----------------------*/
        spacecraftCommThread = new Thread( this );
        spacecraftCommThread.start();                

        /*-------------------------------------------*/
        /* Start calculating satellite tracking data */
        /*-------------------------------------------*/
        satTrackThread = new Thread( this );
        satTrackThread.start();
        
        /*----------------------------*/
        /* Start Monitoring the radio */
        /*----------------------------*/
        radioCommThread = new Thread( this );
        radioCommThread.start();

        /*--------------------------------------*/
        /* Start Monitoring the antenna rotator */
        /*--------------------------------------*/
        antennaRotatorCommThread = new Thread( this );
        antennaRotatorCommThread.start();

        /*---------------------------------*/
        /* Start the script manager thread */
        /* After setting up the GUI        */
        /*---------------------------------*/
        ScriptTask.fillCommandMenu(listScriptTask1Command);
        ScriptTask.fillCommandMenu(listScriptTask2Command);
        ScriptTask.fillCommandMenu(listScriptTask3Command);
        ScriptTask.fillCommandMenu(listScriptTask4Command);
        ScriptTask.fillCommandMenu(listScriptTask5Command);
        scriptThread = new Thread( this );
        scriptThread.start();
        
        /*-------------------------------------------------------------*/
        /* Initialize the load frame vector. This is where we'll store */
        /* interesting received frame before they are interpreted by   */
        /* the main thread.                                            */
        /*-------------------------------------------------------------*/
        ldrFramesVector = new Vector();

        
        
        /*--------------------------------------------------------------------*/
        /*                     G U I    R E F R E S H                         */
        /*--------------------------------------------------------------------*/
        /* Define the actions that will be done periodically to               */
        /* update the GUI..                                                   */
        /*--------------------------------------------------------------------*/
        timer = new javax.swing.Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            
            
            /*---------------------------------------------*/
            /* Make sure that the control callsign is the  */
            /* one corresponding to the currently selected */
            /* satellite in the satellite panel...         */
            /*---------------------------------------------*/
            Satellite sat = panelSatellites.getSelectedSatellite();
            if( sat != null ) {
                
                /*~~~~~~~~~~~~~~~~~~~*/
                /* ICICICICICICIC!!! */
                /*~~~~~~~~~~~~~~~~~~~*/
                //phtLink.setDestinationIdentification( sat.getControlCallsign() );
//                sat.setControlKey( sat.getControlKey() );
//                sat.setSCID( sat.getBootloaderSCID() );
//                sat.setEncryptionKeys(  sat.getBootloaderAVAL(),
//                                        sat.getBootloaderBVAL(),
//                                        sat.getBootloaderD3VAL(),
//                                        sat.getBootloaderK1H(),
//                                        sat.getBootloaderK1L() 
//                                        );
//                sat.setCommandCodes(    sat.getBootloaderLoadCmd(),
//                                        sat.getBootloaderDumpCmd(),
//                                        sat.getBootloaderExecCmd(),
//                                        sat.getBootloaderMemeCmd(),
//                                        sat.getBootloaderMemwCmd(),
//                                        sat.getBootloaderIOeCmd(),
//                                        sat.getBootloaderIOpCmd(),
//                                        sat.getBootloaderTlmCmd(),
//                                        sat.getBootloaderMovCmd(),
//                                        sat.getBootloaderNoCmd1(),
//                                        sat.getBootloaderNoCmd2()
//                                        );
            }
            
            /*---------------------------------------------*/
            /* Make sure that we can read the latest stuff */
            /* put into the info text field, and that the  */
            /* text is not too long                        */
            /*---------------------------------------------*/
            if( chkTxtFollow.isSelected() ) {
                if( txtInfo.getText().length() > 2500 )
                    txtInfo.setText( txtInfo.getText().substring(2500) );
                txtInfo.setCaretPosition(txtInfo.getText().length());
                txtInfo.revalidate();
            }

            /*----------------------------*/
            /* Status of the TNC lines... */
            /*----------------------------*/
            if( (srvLnk!=null) && (panelTNC.isShowing()) ) {
                panelTNC.setDCD( srvLnk.isSpacecraftPortCD() );
                panelTNC.setDSR( srvLnk.isSpacecraftPortDSR() );
                panelTNC.setCTS( srvLnk.isSpacecraftPortCTS() );
                panelTNC.setPortSpeed( srvLnk.getSpacecraftPortSpeed() );
            }                
            if( srvLnk != null ) {
                if( srvLnk.isSpacecraftPortChanged() ) {
                    lblDCD.setBackground( java.awt.Color.yellow );
                } else {
                    if( srvLnk.isSpacecraftPortCD() ) {
                        lblDCD.setBackground( java.awt.Color.green );
                    } else {
                        lblDCD.setBackground( java.awt.Color.gray );
                    }
                }
            }                

            /*-------------------------------------*/
            /* Received and transmitted char count */
            /*-------------------------------------*/
            txtRxCount.setText(""+statusRXCount);
            txtTxCount.setText(""+statusTXCount);
            txtDataSize.setText(""+dataChunkSize);
            if( chkDynamicPacketSize.isSelected() )
                slidPacketSize.setValue(dataChunkSize);
            txtRxErrors.setText(""+rxErrorsCount);
            txtTimeouts.setText(""+timeoutsCount);

            /*----------------------------*/
            /* Update the error ratio bar */
            /*----------------------------*/
            //if( goodFramesCount < 0 ) goodFramesCount = 0;
            if( rxErrorsSinceReset > rxFramesSinceReset ) rxErrorsSinceReset = rxFramesSinceReset;
            double ratio = (double)((double)rxErrorsSinceReset*100.0)/((double)rxFramesSinceReset*100.0);
            ratio = ratio * 100.0;
            //prgrssErrors.setValue((int)ratio);

            /*-------------------------------*/
            /* Unformatted Mode indicator... */
            /*-------------------------------*/
            if( srvLnk == null ) {                    
                lblProto.setBackground( java.awt.Color.red );
            } else if( !srvLnk.isSpacecraftConnected() ) {
                lblProto.setBackground( java.awt.Color.gray );
            } else if( srvLnk.isSpacecraftUnformatted() ) {
                lblProto.setBackground( java.awt.Color.yellow );
            } else {
                lblProto.setBackground( java.awt.Color.green );
            }

            /*----------------------------*/
            /* Load in Progress indicator */
            /*----------------------------*/
            if( cancelLoad )
                lblLoading.setBackground(java.awt.Color.yellow);
            else if( loadInProgress )
                lblLoading.setBackground(java.awt.Color.green);
            else
                lblLoading.setBackground(new Color(0,102,0));
            
            /*--------*/
            /* Script */
            /*--------*/
            if( panelScript.isShowing() ) {
                refreshTaskEntries();
            }
            /*--------------------------*/
            /* Task load progress panel */
            /*--------------------------*/
            if( panelScript.isShowing() && (taskLoader!=null)){
                
            }
            if( panelScript.isShowing() && (taskLoader!=null)){
                
            }
            

            /*-----------------------------------------------*/
            /* RESET and RESUME load buttons: take care of   */
            /* enabling them only when necessary, and change */
            /* their color according ot the status of the    */
            /* loading thread                                */
            /*-----------------------------------------------*/
//                if( loadThread != null ) {
//                    if( loadThread.isAlive() ) {
//                        btnReset.setBackground(Color.red);
//                        btnReset.setEnabled(true);
//                        //btnResumeLoad.setText("Resume Load");
//                        //btnResumeLoad.setBackground(Color.lightGray);
//                        //btnResumeLoad.setEnabled(false);
//                    }
//                    else {
//                        btnReset.setBackground(Color.lightGray);
//                        btnReset.setEnabled(false);
//                        int lastSegment = 0;
//                        if( txtLoadSegment.getText().trim().length() > 3 ) {
//                            try{
//                                lastSegment = Integer.parseInt( txtLoadSegment.getText().trim().substring(2),16 );
//                            } catch( NumberFormatException nfe ) {}
//                        }
//                        if( lastSegment <= 0 ) {
//                            //btnResumeLoad.setText("Resume Load");
//                            //btnResumeLoad.setBackground(Color.lightGray);
//                            //btnResumeLoad.setEnabled(false);
//                        } else {
//                            //btnResumeLoad.setText("Resume Load at "+txtLoadSegment.getText().trim().substring(2));
//                            //btnResumeLoad.setBackground(Color.yellow);
//                            //btnResumeLoad.setEnabled( true );
//                        }
//                    }
//                    
//                }
//                else {
//                    
//                    btnReset.setBackground(Color.lightGray);
//                    btnReset.setEnabled(false);
//                    
//                    /*---------------------------------------*/
//                    /* Verify if the status line gave us the */
//                    /* value of the segment of the last load */
//                    /*---------------------------------------*/
//                    int lastSegment = 0;
//                    if( txtLoadSegment.getText().trim().length() > 3 ) {
//                        try{
//                            lastSegment = Integer.parseInt( txtLoadSegment.getText().trim().substring(2),16 );
//                        } catch( NumberFormatException nfe ) {}
//                    }
//                    
//                    if( lastSegment <= 0 ) {
//                        //btnResumeLoad.setText("Resume Load");
//                        //btnResumeLoad.setBackground(Color.lightGray);
//                        //btnResumeLoad.setEnabled(false);
//                    } else {
//                        //btnResumeLoad.setText("Resume Load at "+txtLoadSegment.getText().trim().substring(2));
//                        //btnResumeLoad.setBackground(Color.yellow);
//                        //btnResumeLoad.setEnabled( true );
//                    }
//                    
//                }


            /*---------------------------------------------------*/
            /* Periodically verify if the log book has changed   */
            /* and allow the panel to display a flag to the user */
            /*---------------------------------------------------*/
            panelLogBook.verifyTextChanged();

            /*--------------------------*/
            /* Satellite tracking infos */
            /*--------------------------*/
            currentSat = panelSatellites.getSelectedSatellite();
            if( currentSat != null ) {
                txtSpacecraft.setText( currentSat.toString() );
                panelTracking.updateSat( currentSat );
                if( isPassActive )
                    lblPass.setBackground(java.awt.Color.green);
                else
                    lblPass.setBackground(java.awt.Color.gray);
                if( isPassPaused )
                    panelTracking.showDisabled();
            }
            /*~~~~~~~~~~~~~~~~~~~~~~~~~*/
            /* THIS SHOULD NOT BE HERE */
            /*~~~~~~~~~~~~~~~~~~~~~~~~~*/
            //panelTX_cmd.setSatellite( currentSat );
            if( currentSat != previousSat ) {
                if( panelACS_cmd != null ) 
                    panelACS_cmd.setSatellite( currentSat );
                if( panelEDAC_cmd != null ) 
                    panelEDAC_cmd.setSatellite( currentSat );
                if( panelHeaters_cmd != null ) 
                    panelHeaters_cmd.setSatellite( currentSat );
                if( panelHouseKeeping_cmd != null ) 
                    panelHouseKeeping_cmd.setSatellite( currentSat );
                if( panelIO_cmd != null ) 
                    panelIO_cmd.setSatellite( currentSat );
                if( panelModems_cmd != null ) 
                    panelModems_cmd.setSatellite( currentSat );
                if( panelPayload_cmd != null ) 
                    panelPayload_cmd.setSatellite( currentSat );
                if( panelPower_cmd != null ) 
                    panelPower_cmd.setSatellite( currentSat );
                if( panelPyros_cmd != null ) 
                    panelPyros_cmd.setSatellite( currentSat );
                if( panelTX_cmd != null ) 
                    panelTX_cmd.setSatellite( currentSat );
                if( panelTesting != null ) 
                    panelTesting.setSatellite( currentSat );
                //if( panelRadio != null ) 
                //    panelRadio.setSatellite( currentSat );
                //if( panelRotator != null ) 
                //    panelRotator.setSatellite( currentSat );
                if( panelPACSAT != null ) 
                    panelPACSAT.setSatellite( currentSat );
                //if( panelTNC != null ) 
                //    panelTNC.setSatellite( currentSat );
                previousSat = currentSat;
            }

            /*-------------------------*/
            /* Update the Health panel */
            /*-------------------------*/
            if( SHOW_HEALTH_PANEL )
                panelHealth.setSatName(
                            panelSatellites.getSelectedSatellite().getName() );

            /*-----------------*/
            /* CountDown Timer */
            /*-----------------*/
            countDownTimer -= 1;

        }//actionPerformed
        }); // Timer GUI
        
        
        /*--------------------------------------------------*/
        /* Define object that updates load progress labels  */
        /*--------------------------------------------------*/
        loadProgressGUI = new LoadProgressGUI();
        infoGUI = new InfoGUI();
        
        /*------------------------------------*/
        /* Starts the GUI update timer thread */
        /*------------------------------------*/
        timer.start();
        
        dialogIntro.setInfoTitle("Initialization information:");
        dialogIntro.appendInfo("Completed Initialization...");
        dialogIntro.enableContinueButton();
        
        while( true ) {
            if( dialogIntro != null ) {
                if( !dialogIntro.isVisible() ) {
                    dialogIntro.dispose();                    
                    break;
                }
            }
            else break;
            try{Thread.currentThread().sleep(500);}catch( InterruptedException ie ) {}        
        }
        /*-----------------------------------------------------------------*/
        /*Ok now the intro dialog is closed, so make sure nobody else will */
        /* use it...                                                       */
        /*-----------------------------------------------------------------*/
        dialogIntro = null;
        
    }
    
    /*========================================================================*/
    /** Will be part of the PACSAT interface...                               */
    /*========================================================================*///$$$
    public void pacsatConnect() {
    /*========================================================================*/    
        if( pacsatSessionThread != null ) {
            if( pacsatSessionThread.isAlive() ) {
                panelPACSAT.println("SESSION ALREADY ACTIVE");
                return;
            }
        }
        pacsatSessionActive = true;
        pacsatSessionThread = new Thread( this );
        pacsatSessionThread.start();        
    }
    /*========================================================================*/
    /** Will be part of the PACSAT interface...                               */
    /*========================================================================*/
    public void pacsatDisconnect() {
    /*========================================================================*/    
        if( pacsatSessionActive ) {
            pacsatSessionActive = false;
        }
           
    }
            public Satellite getAsat(){
        
        return aSat;
        }
        
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
        private void initComponents() {//GEN-BEGIN:initComponents
            menuBar = new javax.swing.JMenuBar();
            fileMenu = new javax.swing.JMenu();
            exitMenuItem = new javax.swing.JMenuItem();
            toolsMenu = new javax.swing.JMenu();
            radPHTCommandsPrime = new javax.swing.JRadioButtonMenuItem();
            radPHTCommandsBackup = new javax.swing.JRadioButtonMenuItem();
            jSeparator18 = new javax.swing.JSeparator();
            chkPHTVerboseMenuItem = new javax.swing.JCheckBoxMenuItem();
            jSeparator19 = new javax.swing.JSeparator();
            radUseExtendedLoadProto = new javax.swing.JRadioButtonMenuItem();
            radUseStandardLoadProto = new javax.swing.JRadioButtonMenuItem();
            viewMenu = new javax.swing.JMenu();
            menuShowMessages = new javax.swing.JMenuItem();
            menuShowLogbook = new javax.swing.JMenuItem();
            menuShowTelemetry = new javax.swing.JMenuItem();
            menuShowSettings = new javax.swing.JMenuItem();
            helpMenu = new javax.swing.JMenu();
            aboutMenuItem = new javax.swing.JMenuItem();
            panelMain = new javax.swing.JTabbedPane();
            panelBootLoader = new javax.swing.JPanel();
            jPanel66 = new javax.swing.JPanel();
            jPanel67 = new javax.swing.JPanel();
            jPanel228 = new javax.swing.JPanel();
            jPanel231 = new javax.swing.JPanel();
            btnQBLMove = new javax.swing.JButton();
            btnQBLExecute = new javax.swing.JButton();
            jPanel230 = new javax.swing.JPanel();
            jLabel136 = new javax.swing.JLabel();
            txtQBLMemDumpSegment = new javax.swing.JTextField();
            jLabel137 = new javax.swing.JLabel();
            txtQBLMemDumpOffset = new javax.swing.JTextField();
            jLabel138 = new javax.swing.JLabel();
            txtQBLMemDumpNbBytes = new javax.swing.JTextField();
            btnQBLMemDump = new javax.swing.JButton();
            jPanel234 = new javax.swing.JPanel();
            jPanel232 = new javax.swing.JPanel();
            jLabel139 = new javax.swing.JLabel();
            txtQBLMemReadSegment = new javax.swing.JTextField();
            jLabel140 = new javax.swing.JLabel();
            txtQBLMemReadOffset = new javax.swing.JTextField();
            btnQBLMemRead = new javax.swing.JButton();
            jLabel141 = new javax.swing.JLabel();
            txtQBLMemReadResult = new javax.swing.JTextField();
            jPanel233 = new javax.swing.JPanel();
            jLabel142 = new javax.swing.JLabel();
            txtQBLMemWriteSegment = new javax.swing.JTextField();
            jLabel143 = new javax.swing.JLabel();
            txtQBLMemWriteOffset = new javax.swing.JTextField();
            jLabel144 = new javax.swing.JLabel();
            txtQBLMemWriteValue = new javax.swing.JTextField();
            btnQBLMemWrite = new javax.swing.JButton();
            jPanel235 = new javax.swing.JPanel();
            jPanel236 = new javax.swing.JPanel();
            jLabel145 = new javax.swing.JLabel();
            txtQBLIOReadPort = new javax.swing.JTextField();
            btnQBLIORead = new javax.swing.JButton();
            jLabel147 = new javax.swing.JLabel();
            txtQBLIOReadResult = new javax.swing.JTextField();
            jPanel237 = new javax.swing.JPanel();
            jLabel148 = new javax.swing.JLabel();
            txtQBLIOWritePort = new javax.swing.JTextField();
            jLabel150 = new javax.swing.JLabel();
            txtQBLIOWriteValue = new javax.swing.JTextField();
            btnQBLIOWrite = new javax.swing.JButton();
            jPanel229 = new javax.swing.JPanel();
            jLabel154 = new javax.swing.JLabel();
            jPanel238 = new javax.swing.JPanel();
            jLabel151 = new javax.swing.JLabel();
            txtBLCommandCounter = new javax.swing.JTextField();
            jLabel146 = new javax.swing.JLabel();
            txtBLCodeSegment = new javax.swing.JTextField();
            jLabel149 = new javax.swing.JLabel();
            txtBLEDACErrors = new javax.swing.JTextField();
            jLabel152 = new javax.swing.JLabel();
            txtBLDataDirection = new javax.swing.JTextField();
            jLabel153 = new javax.swing.JLabel();
            txtBLIOPortsData = new javax.swing.JTextField();
            jLabel155 = new javax.swing.JLabel();
            txtBLInfo = new javax.swing.JTextField();
            jSeparator1 = new javax.swing.JSeparator();
            btnQBLBeacon = new javax.swing.JButton();
            btnSerial9600 = new javax.swing.JButton();
            btnSerial19200 = new javax.swing.JButton();
            jPanel60 = new javax.swing.JPanel();
            panelScript = new javax.swing.JPanel();
            jPanel1 = new javax.swing.JPanel();
            jPanel33 = new javax.swing.JPanel();
            jScrollPane1 = new javax.swing.JScrollPane();
            jPanel71 = new javax.swing.JPanel();
            jPanel73 = new javax.swing.JPanel();
            jLabel27 = new javax.swing.JLabel();
            jLabel20 = new javax.swing.JLabel();
            jLabel24 = new javax.swing.JLabel();
            jLabel25 = new javax.swing.JLabel();
            jLabel26 = new javax.swing.JLabel();
            jPanel72 = new javax.swing.JPanel();
            lblScriptTask1Id = new javax.swing.JLabel();
            listScriptTask1Command = new javax.swing.JComboBox();
            txtScriptTask1Param1 = new javax.swing.JTextField();
            txtScriptTask1Param2 = new javax.swing.JTextField();
            txtScriptTask1Progress = new javax.swing.JTextField();
            jPanel74 = new javax.swing.JPanel();
            lblScriptTask2Id = new javax.swing.JLabel();
            listScriptTask2Command = new javax.swing.JComboBox();
            txtScriptTask2Param1 = new javax.swing.JTextField();
            txtScriptTask2Param2 = new javax.swing.JTextField();
            txtScriptTask2Progress = new javax.swing.JTextField();
            jPanel75 = new javax.swing.JPanel();
            lblScriptTask3Id = new javax.swing.JLabel();
            listScriptTask3Command = new javax.swing.JComboBox();
            txtScriptTask3Param1 = new javax.swing.JTextField();
            txtScriptTask3Param2 = new javax.swing.JTextField();
            txtScriptTask3Progress = new javax.swing.JTextField();
            jPanel76 = new javax.swing.JPanel();
            lblScriptTask4Id = new javax.swing.JLabel();
            listScriptTask4Command = new javax.swing.JComboBox();
            txtScriptTask4Param1 = new javax.swing.JTextField();
            txtScriptTask4Param2 = new javax.swing.JTextField();
            txtScriptTask4Progress = new javax.swing.JTextField();
            jPanel77 = new javax.swing.JPanel();
            lblScriptTask5Id = new javax.swing.JLabel();
            listScriptTask5Command = new javax.swing.JComboBox();
            txtScriptTask5Param1 = new javax.swing.JTextField();
            txtScriptTask5Param2 = new javax.swing.JTextField();
            txtScriptTask5Progress = new javax.swing.JTextField();
            jPanel4 = new javax.swing.JPanel();
            jPanel70 = new javax.swing.JPanel();
            listScripts = new javax.swing.JComboBox();
            btnLoadScript = new javax.swing.JButton();
            btnSaveAsScript = new javax.swing.JButton();
            btnPlayPauseScript = new javax.swing.JButton();
            btnResetScript = new javax.swing.JButton();
            jPanel15 = new javax.swing.JPanel();
            lblDataSize = new javax.swing.JLabel();
            txtDataSize = new javax.swing.JTextField();
            slidPacketSize = new javax.swing.JSlider();
            chkDynamicPacketSize = new javax.swing.JCheckBox();
            jPanel2 = new javax.swing.JPanel();
            pnlLoad = new javax.swing.JPanel();
            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();
            jPanel12 = new javax.swing.JPanel();
            btnLoad = new javax.swing.JButton();
            lblAckReq = new javax.swing.JLabel();
            lblEmpty1 = new javax.swing.JLabel();
            lblAckLoad = new javax.swing.JLabel();
            lblEmpty2 = new javax.swing.JLabel();
            lblAckAddress = new javax.swing.JLabel();
            jPanel13 = new javax.swing.JPanel();
            btnSetSegment = new javax.swing.JButton();
            txtSegment = new javax.swing.JTextField();
            lblSetSegmentAck = new javax.swing.JLabel();
            jPanel56 = new javax.swing.JPanel();
            btnSendData = new javax.swing.JButton();
            txtDataSegment = new javax.swing.JTextField();
            jLabel23 = new javax.swing.JLabel();
            txtDataOffset = new javax.swing.JTextField();
            lblDataAck = new javax.swing.JLabel();
            jPanel57 = new javax.swing.JPanel();
            btnSendEOF = new javax.swing.JButton();
            lblEOFAck = new javax.swing.JLabel();
            jPanel59 = new javax.swing.JPanel();
            btnCreateTask = new javax.swing.JButton();
            lblStartAck = new javax.swing.JLabel();
            jPanel68 = new javax.swing.JPanel();
            btnRun = new javax.swing.JButton();
            btnReset = new javax.swing.JButton();
            panelCommands = new javax.swing.JPanel();
            commandTabs = new javax.swing.JTabbedPane();
            panelDiagnostic = new javax.swing.JPanel();
            jPanel16 = new javax.swing.JPanel();
            jPanel18 = new javax.swing.JPanel();
            jPanel31 = new javax.swing.JPanel();
            jLabel4 = new javax.swing.JLabel();
            jPanel32 = new javax.swing.JPanel();
            txtPHTSeg = new javax.swing.JTextField();
            jPanel19 = new javax.swing.JPanel();
            jPanel34 = new javax.swing.JPanel();
            jLabel7 = new javax.swing.JLabel();
            jPanel35 = new javax.swing.JPanel();
            txtLargestBlock = new javax.swing.JTextField();
            jPanel20 = new javax.swing.JPanel();
            jPanel36 = new javax.swing.JPanel();
            jLabel8 = new javax.swing.JLabel();
            jPanel37 = new javax.swing.JPanel();
            txtMemAvail = new javax.swing.JTextField();
            jPanel26 = new javax.swing.JPanel();
            jPanel39 = new javax.swing.JPanel();
            lblEDACErr = new javax.swing.JLabel();
            jPanel38 = new javax.swing.JPanel();
            txtEDACErr = new javax.swing.JTextField();
            jPanel21 = new javax.swing.JPanel();
            jPanel22 = new javax.swing.JPanel();
            jPanel41 = new javax.swing.JPanel();
            jLabel9 = new javax.swing.JLabel();
            jPanel40 = new javax.swing.JPanel();
            txtTransmitQueue = new javax.swing.JTextField();
            jPanel23 = new javax.swing.JPanel();
            jPanel43 = new javax.swing.JPanel();
            jLabel10 = new javax.swing.JLabel();
            jPanel42 = new javax.swing.JPanel();
            txtDigipeat = new javax.swing.JTextField();
            jPanel27 = new javax.swing.JPanel();
            jPanel45 = new javax.swing.JPanel();
            jLabel13 = new javax.swing.JLabel();
            jPanel44 = new javax.swing.JPanel();
            txtRX0 = new javax.swing.JTextField();
            jPanel61 = new javax.swing.JPanel();
            jLabel12 = new javax.swing.JLabel();
            txtRX1 = new javax.swing.JTextField();
            jPanel28 = new javax.swing.JPanel();
            jPanel47 = new javax.swing.JPanel();
            jLabel14 = new javax.swing.JLabel();
            jPanel46 = new javax.swing.JPanel();
            txtRX2 = new javax.swing.JTextField();
            jPanel62 = new javax.swing.JPanel();
            jLabel17 = new javax.swing.JLabel();
            txtRX3 = new javax.swing.JTextField();
            jPanel63 = new javax.swing.JPanel();
            jLabel18 = new javax.swing.JLabel();
            txtRX4 = new javax.swing.JTextField();
            jPanel64 = new javax.swing.JPanel();
            jLabel19 = new javax.swing.JLabel();
            txtRX5 = new javax.swing.JTextField();
            jPanel30 = new javax.swing.JPanel();
            jPanel49 = new javax.swing.JPanel();
            jLabel16 = new javax.swing.JLabel();
            jPanel48 = new javax.swing.JPanel();
            txtTransmitOverflow = new javax.swing.JTextField();
            jPanel24 = new javax.swing.JPanel();
            jPanel25 = new javax.swing.JPanel();
            jPanel50 = new javax.swing.JPanel();
            jLabel11 = new javax.swing.JLabel();
            jPanel51 = new javax.swing.JPanel();
            txtTaskNumber = new javax.swing.JTextField();
            jPanel17 = new javax.swing.JPanel();
            jPanel52 = new javax.swing.JPanel();
            jLabel3 = new javax.swing.JLabel();
            jPanel53 = new javax.swing.JPanel();
            txtLoadSegment = new javax.swing.JTextField();
            jPanel29 = new javax.swing.JPanel();
            jPanel54 = new javax.swing.JPanel();
            jLabel15 = new javax.swing.JLabel();
            jPanel55 = new javax.swing.JPanel();
            txtPHTRate = new javax.swing.JTextField();
            jButton4 = new javax.swing.JButton();
            jButton2 = new javax.swing.JButton();
            panelRAMDisk = new javax.swing.JPanel();
            jLabel53 = new javax.swing.JLabel();
            jPanel111 = new javax.swing.JPanel();
            btnLoadFile = new javax.swing.JButton();
            btnReadFile = new javax.swing.JButton();
            btnFSDeleteFile = new javax.swing.JButton();
            btnFSTestFill = new javax.swing.JButton();
            btnFSAbort = new javax.swing.JButton();
            jPanel112 = new javax.swing.JPanel();
            jPanel166 = new javax.swing.JPanel();
            jScrollPane2 = new javax.swing.JScrollPane();
            txtFSFormat = new javax.swing.JTextArea();
            jPanel165 = new javax.swing.JPanel();
            jScrollPane4 = new javax.swing.JScrollPane();
            txtFSDir = new javax.swing.JTextArea();
            jPanel113 = new javax.swing.JPanel();
            prgrssRamdiskTest = new javax.swing.JProgressBar();
            jCommonPortion = new javax.swing.JPanel();
            panelInfo = new javax.swing.JPanel();
            panelMessages = new javax.swing.JPanel();
            scrpaneInfo = new javax.swing.JScrollPane();
            txtInfo = new javax.swing.JTextArea();
            jPanel14 = new javax.swing.JPanel();
            jPanel65 = new javax.swing.JPanel();
            chkTxtFollow = new javax.swing.JCheckBox();
            chkShowTimeFrames = new javax.swing.JCheckBox();
            chkShowTLMFrames = new javax.swing.JCheckBox();
            chkShowSTATFrames = new javax.swing.JCheckBox();
            chkShowLDRFrames = new javax.swing.JCheckBox();
            btnTxtSave = new javax.swing.JButton();
            jPanel5 = new javax.swing.JPanel();
            panelPassManagement = new javax.swing.JPanel();
            lblPass = new javax.swing.JLabel();
            btnIgnorePass = new javax.swing.JToggleButton();
            btnPausePass = new javax.swing.JToggleButton();
            panelComSetup = new javax.swing.JPanel();
            jPanel6 = new javax.swing.JPanel();
            jPanel10 = new javax.swing.JPanel();
            jLabel21 = new javax.swing.JLabel();
            txtSpacecraft = new javax.swing.JTextField();
            btnPing = new javax.swing.JButton();
            jPanel11 = new javax.swing.JPanel();
            jLabel22 = new javax.swing.JLabel();
            listStations = new javax.swing.JComboBox();
            jButton1 = new javax.swing.JButton();
            jGeneralStatus = new javax.swing.JPanel();
            jPanel82 = new javax.swing.JPanel();
            jLabel174 = new javax.swing.JLabel();
            listSSID = new javax.swing.JComboBox();
            jLabel32 = new javax.swing.JLabel();
            txtTelemUTC = new javax.swing.JTextField();
            jPanel7 = new javax.swing.JPanel();
            lblRxCount = new javax.swing.JLabel();
            txtRxCount = new javax.swing.JTextField();
            lblTxCount = new javax.swing.JLabel();
            txtTxCount = new javax.swing.JTextField();
            jPanel9 = new javax.swing.JPanel();
            jLabel5 = new javax.swing.JLabel();
            txtRxErrors = new javax.swing.JTextField();
            jLabel6 = new javax.swing.JLabel();
            txtTimeouts = new javax.swing.JTextField();
            pnlCommand = new javax.swing.JPanel();
            jPanel58 = new javax.swing.JPanel();
            jPanel69 = new javax.swing.JPanel();
            lblLoading = new javax.swing.JLabel();
            prgrssLoad = new javax.swing.JProgressBar();
            jPanel8 = new javax.swing.JPanel();
            lblProto = new javax.swing.JLabel();
            lblDCD = new javax.swing.JLabel();
            lblRXError = new javax.swing.JLabel();
            jPanel3 = new javax.swing.JPanel();
            
            fileMenu.setText("File");
            exitMenuItem.setText("Exit");
            exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    exitMenuItemActionPerformed(evt);
                }
            });
            
            fileMenu.add(exitMenuItem);
            menuBar.add(fileMenu);
            toolsMenu.setText("Tools");
            radPHTCommandsPrime.setText("PHT Commands: Normal");
            radPHTCommandsPrime.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    radPHTCommandsPrimeActionPerformed(evt);
                }
            });
            
            toolsMenu.add(radPHTCommandsPrime);
            radPHTCommandsBackup.setText("PHT Commands: Backup");
            radPHTCommandsBackup.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    radPHTCommandsBackupActionPerformed(evt);
                }
            });
            
            toolsMenu.add(radPHTCommandsBackup);
            toolsMenu.add(jSeparator18);
            chkPHTVerboseMenuItem.setText("Display PHT Raw Commands");
            chkPHTVerboseMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    chkPHTVerboseMenuItemActionPerformed(evt);
                }
            });
            
            toolsMenu.add(chkPHTVerboseMenuItem);
            toolsMenu.add(jSeparator19);
            radUseExtendedLoadProto.setText("Extended Task Load Protocole");
            radUseExtendedLoadProto.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    radUseExtendedLoadProtoActionPerformed(evt);
                }
            });
            
            toolsMenu.add(radUseExtendedLoadProto);
            radUseStandardLoadProto.setText("Standard Task Load Protocole");
            radUseStandardLoadProto.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    radUseStandardLoadProtoActionPerformed(evt);
                }
            });
            
            toolsMenu.add(radUseStandardLoadProto);
            menuBar.add(toolsMenu);
            viewMenu.setText("View");
            menuShowMessages.setText("Display incoming messages");
            menuShowMessages.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuShowMessagesActionPerformed(evt);
                }
            });
            
            viewMenu.add(menuShowMessages);
            menuShowLogbook.setText("Display Logbook");
            menuShowLogbook.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuShowLogbookActionPerformed(evt);
                }
            });
            
            viewMenu.add(menuShowLogbook);
            menuShowTelemetry.setText("Display Telemetry");
            menuShowTelemetry.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuShowTelemetryActionPerformed(evt);
                }
            });
            
            viewMenu.add(menuShowTelemetry);
            menuShowSettings.setText("Settings");
            menuShowSettings.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    menuShowSettingsActionPerformed(evt);
                }
            });
            
            viewMenu.add(menuShowSettings);
            menuBar.add(viewMenu);
            helpMenu.setText("Help");
            aboutMenuItem.setText("About");
            aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    aboutMenuItemActionPerformed(evt);
                }
            });
            
            helpMenu.add(aboutMenuItem);
            menuBar.add(helpMenu);
            
            getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));
            
            setTitle("QuickSat Ground Station - Executable Version 5 (Tentative 2)");
            setForeground(java.awt.Color.black);
            setBackground(new java.awt.Color(153, 153, 204));
            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    exitForm(evt);
                }
            });
            
            panelMain.setBackground(java.awt.Color.gray);
            panelMain.setPreferredSize(new java.awt.Dimension(800, 450));
            panelMain.setName("Panels");
            panelMain.setMinimumSize(new java.awt.Dimension(800, 200));
            panelMain.setDoubleBuffered(true);
            panelBootLoader.setLayout(new javax.swing.BoxLayout(panelBootLoader, javax.swing.BoxLayout.Y_AXIS));
            
            panelBootLoader.setName("Bootloader");
            panelBootLoader.addComponentListener(new java.awt.event.ComponentAdapter() {
                public void componentShown(java.awt.event.ComponentEvent evt) {
                    panelBootLoaderComponentShown(evt);
                }
            });
            
            jPanel66.setLayout(new javax.swing.BoxLayout(jPanel66, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel67.setLayout(new javax.swing.BoxLayout(jPanel67, javax.swing.BoxLayout.X_AXIS));
            
            jPanel67.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jPanel67.setPreferredSize(new java.awt.Dimension(800, 270));
            jPanel228.setLayout(new javax.swing.BoxLayout(jPanel228, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel228.setBorder(new javax.swing.border.TitledBorder("Bootloader Commands"));
            jPanel231.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
            
            btnQBLMove.setToolTipText("Sends a command to the bootloader to move the operating system and the 1st stage housekeeping task into the spacecraft's memory");
            btnQBLMove.setText("Move SCOS/PHT in memory");
            btnQBLMove.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnQBLMoveActionPerformed(evt);
                }
            });
            
            jPanel231.add(btnQBLMove);
            
            btnQBLExecute.setToolTipText("Sends a command to the bootloader to request it to execute the operating system and the first stage housekeeping task. MUST MOVE THEM IN MEMORY FIRST!");
            btnQBLExecute.setText("Execute SCOS & PHT");
            btnQBLExecute.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnQBLExecuteActionPerformed(evt);
                }
            });
            
            jPanel231.add(btnQBLExecute);
            
            jPanel228.add(jPanel231);
            
            jPanel230.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
            
            jPanel230.setBorder(new javax.swing.border.TitledBorder("Dump Memory"));
            jLabel136.setText("Starting Address:");
            jPanel230.add(jLabel136);
            
            txtQBLMemDumpSegment.setPreferredSize(new java.awt.Dimension(50, 20));
            jPanel230.add(txtQBLMemDumpSegment);
            
            jLabel137.setText(":");
            jPanel230.add(jLabel137);
            
            txtQBLMemDumpOffset.setPreferredSize(new java.awt.Dimension(50, 20));
            jPanel230.add(txtQBLMemDumpOffset);
            
            jLabel138.setText("Number of Bytes:");
            jPanel230.add(jLabel138);
            
            txtQBLMemDumpNbBytes.setPreferredSize(new java.awt.Dimension(50, 20));
            jPanel230.add(txtQBLMemDumpNbBytes);
            
            btnQBLMemDump.setText("DUMP");
            btnQBLMemDump.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnQBLMemDumpActionPerformed(evt);
                }
            });
            
            jPanel230.add(btnQBLMemDump);
            
            jPanel228.add(jPanel230);
            
            jPanel234.setLayout(new javax.swing.BoxLayout(jPanel234, javax.swing.BoxLayout.X_AXIS));
            
            jPanel232.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 1));
            
            jPanel232.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Read EDAC Memory Location"));
            jLabel139.setText("Addr:");
            jPanel232.add(jLabel139);
            
            txtQBLMemReadSegment.setPreferredSize(new java.awt.Dimension(30, 20));
            jPanel232.add(txtQBLMemReadSegment);
            
            jLabel140.setText(":");
            jPanel232.add(jLabel140);
            
            txtQBLMemReadOffset.setPreferredSize(new java.awt.Dimension(30, 20));
            jPanel232.add(txtQBLMemReadOffset);
            
            btnQBLMemRead.setText("READ");
            btnQBLMemRead.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnQBLMemReadActionPerformed(evt);
                }
            });
            
            jPanel232.add(btnQBLMemRead);
            
            jLabel141.setText("Res:");
            jPanel232.add(jLabel141);
            
            txtQBLMemReadResult.setPreferredSize(new java.awt.Dimension(30, 20));
            jPanel232.add(txtQBLMemReadResult);
            
            jPanel234.add(jPanel232);
            
            jPanel233.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 1));
            
            jPanel233.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Write EDAC Memory Location"));
            jLabel142.setText("Addr:");
            jPanel233.add(jLabel142);
            
            txtQBLMemWriteSegment.setPreferredSize(new java.awt.Dimension(30, 20));
            jPanel233.add(txtQBLMemWriteSegment);
            
            jLabel143.setText(":");
            jPanel233.add(jLabel143);
            
            txtQBLMemWriteOffset.setPreferredSize(new java.awt.Dimension(30, 20));
            jPanel233.add(txtQBLMemWriteOffset);
            
            jLabel144.setText("Val:");
            jPanel233.add(jLabel144);
            
            txtQBLMemWriteValue.setPreferredSize(new java.awt.Dimension(30, 20));
            jPanel233.add(txtQBLMemWriteValue);
            
            btnQBLMemWrite.setText("WRITE");
            btnQBLMemWrite.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnQBLMemWriteActionPerformed(evt);
                }
            });
            
            jPanel233.add(btnQBLMemWrite);
            
            jPanel234.add(jPanel233);
            
            jPanel228.add(jPanel234);
            
            jPanel235.setLayout(new javax.swing.BoxLayout(jPanel235, javax.swing.BoxLayout.X_AXIS));
            
            jPanel236.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 1));
            
            jPanel236.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Read I/O Port"));
            jLabel145.setText("Port:");
            jPanel236.add(jLabel145);
            
            txtQBLIOReadPort.setPreferredSize(new java.awt.Dimension(50, 20));
            jPanel236.add(txtQBLIOReadPort);
            
            btnQBLIORead.setText("READ");
            btnQBLIORead.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnQBLIOReadActionPerformed(evt);
                }
            });
            
            jPanel236.add(btnQBLIORead);
            
            jLabel147.setText("Result:");
            jPanel236.add(jLabel147);
            
            txtQBLIOReadResult.setPreferredSize(new java.awt.Dimension(40, 20));
            jPanel236.add(txtQBLIOReadResult);
            
            jPanel235.add(jPanel236);
            
            jPanel237.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 1));
            
            jPanel237.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Write I/O Port"));
            jLabel148.setText("Port:");
            jPanel237.add(jLabel148);
            
            txtQBLIOWritePort.setPreferredSize(new java.awt.Dimension(50, 20));
            jPanel237.add(txtQBLIOWritePort);
            
            jLabel150.setText("Value:");
            jPanel237.add(jLabel150);
            
            txtQBLIOWriteValue.setPreferredSize(new java.awt.Dimension(50, 20));
            jPanel237.add(txtQBLIOWriteValue);
            
            btnQBLIOWrite.setText("WRITE");
            btnQBLIOWrite.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnQBLIOWriteActionPerformed(evt);
                }
            });
            
            jPanel237.add(btnQBLIOWrite);
            
            jPanel235.add(jPanel237);
            
            jPanel228.add(jPanel235);
            
            jPanel67.add(jPanel228);
            
            jPanel229.setLayout(new javax.swing.BoxLayout(jPanel229, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel229.setBorder(new javax.swing.border.TitledBorder("Bootloader Status (Beacon)"));
            jPanel229.setPreferredSize(new java.awt.Dimension(200, 159));
            jLabel154.setText("  BEACON  ");
            jLabel154.setForeground(java.awt.Color.yellow);
            jLabel154.setBackground(java.awt.Color.blue);
            jLabel154.setFont(new java.awt.Font("Arial Black", 0, 14));
            jLabel154.setMinimumSize(new java.awt.Dimension(95, 10));
            jLabel154.setAlignmentX(0.5F);
            jLabel154.setMaximumSize(new java.awt.Dimension(1500, 21));
            jLabel154.setOpaque(true);
            jPanel229.add(jLabel154);
            
            jPanel238.setLayout(new java.awt.GridLayout(6, 2, 5, 0));
            
            jPanel238.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jPanel238.setPreferredSize(new java.awt.Dimension(190, 122));
            jPanel238.setMaximumSize(new java.awt.Dimension(1000, 130));
            jLabel151.setText("Cmmnd Count:");
            jLabel151.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jPanel238.add(jLabel151);
            
            txtBLCommandCounter.setPreferredSize(new java.awt.Dimension(60, 20));
            jPanel238.add(txtBLCommandCounter);
            
            jLabel146.setText("Code Segment:");
            jLabel146.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jLabel146.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
            jPanel238.add(jLabel146);
            
            txtBLCodeSegment.setPreferredSize(new java.awt.Dimension(80, 20));
            jPanel238.add(txtBLCodeSegment);
            
            jLabel149.setText("EDAC Errors:");
            jLabel149.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jLabel149.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
            jPanel238.add(jLabel149);
            
            txtBLEDACErrors.setPreferredSize(new java.awt.Dimension(60, 20));
            jPanel238.add(txtBLEDACErrors);
            
            jLabel152.setText("Data Direction:");
            jLabel152.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jPanel238.add(jLabel152);
            
            txtBLDataDirection.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    txtDataDirectionActionPerformed(evt);
                }
            });
            
            jPanel238.add(txtBLDataDirection);
            
            jLabel153.setText("I/O Ports Data:");
            jLabel153.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jPanel238.add(jLabel153);
            
            txtBLIOPortsData.setPreferredSize(new java.awt.Dimension(80, 20));
            jPanel238.add(txtBLIOPortsData);
            
            jLabel155.setText("Bootloader Info:");
            jLabel155.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jLabel155.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    jLabel155MouseClicked(evt);
                }
            });
            
            jPanel238.add(jLabel155);
            
            txtBLInfo.setPreferredSize(new java.awt.Dimension(80, 20));
            jPanel238.add(txtBLInfo);
            
            jPanel229.add(jPanel238);
            
            jSeparator1.setPreferredSize(new java.awt.Dimension(0, 5));
            jSeparator1.setMinimumSize(new java.awt.Dimension(1, 5));
            jSeparator1.setMaximumSize(new java.awt.Dimension(32767, 10));
            jPanel229.add(jSeparator1);
            
            btnQBLBeacon.setToolTipText("Request the bootloader to send back a beacon. It will appear in the message window");
            btnQBLBeacon.setText("Request Beacon");
            btnQBLBeacon.setAlignmentX(0.5F);
            btnQBLBeacon.setMaximumSize(new java.awt.Dimension(1000, 30));
            btnQBLBeacon.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnQBLBeaconActionPerformed(evt);
                }
            });
            
            jPanel229.add(btnQBLBeacon);
            
            btnSerial9600.setText("9600bps connection");
            btnSerial9600.setBackground(java.awt.Color.orange);
            btnSerial9600.setAlignmentX(0.5F);
            btnSerial9600.setMaximumSize(new java.awt.Dimension(1000, 27));
            btnSerial9600.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnSerial9600ActionPerformed(evt);
                }
            });
            
            jPanel229.add(btnSerial9600);
            
            btnSerial19200.setText("19200bps connection");
            btnSerial19200.setBackground(java.awt.Color.orange);
            btnSerial19200.setAlignmentX(0.5F);
            btnSerial19200.setMaximumSize(new java.awt.Dimension(1000, 27));
            btnSerial19200.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnSerial19200ActionPerformed(evt);
                }
            });
            
            jPanel229.add(btnSerial19200);
            
            jPanel229.add(jPanel60);
            
            jPanel67.add(jPanel229);
            
            jPanel66.add(jPanel67);
            
            panelBootLoader.add(jPanel66);
            
            panelMain.addTab("BootLoader", panelBootLoader);
            
            panelScript.setLayout(new java.awt.GridLayout(1, 2));
            
            panelScript.addComponentListener(new java.awt.event.ComponentAdapter() {
                public void componentShown(java.awt.event.ComponentEvent evt) {
                    panelScriptComponentShown(evt);
                }
            });
            
            jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel33.setLayout(new javax.swing.BoxLayout(jPanel33, javax.swing.BoxLayout.Y_AXIS));
            
            jScrollPane1.setPreferredSize(new java.awt.Dimension(380, 150));
            jPanel71.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
            
            jPanel71.setPreferredSize(new java.awt.Dimension(250, 175));
            jPanel71.setMinimumSize(new java.awt.Dimension(200, 39));
            jPanel73.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));
            
            jPanel73.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
            jLabel27.setText("Id");
            jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel27.setPreferredSize(new java.awt.Dimension(20, 17));
            jPanel73.add(jLabel27);
            
            jLabel20.setText("Command");
            jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel20.setPreferredSize(new java.awt.Dimension(150, 17));
            jLabel20.setMaximumSize(new java.awt.Dimension(150, 17));
            jPanel73.add(jLabel20);
            
            jLabel24.setText("Arg1");
            jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel24.setPreferredSize(new java.awt.Dimension(75, 17));
            jLabel24.setMaximumSize(new java.awt.Dimension(100, 17));
            jPanel73.add(jLabel24);
            
            jLabel25.setText("Arg2/Status");
            jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel25.setPreferredSize(new java.awt.Dimension(75, 17));
            jLabel25.setMaximumSize(new java.awt.Dimension(100, 17));
            jPanel73.add(jLabel25);
            
            jLabel26.setText("Progress");
            jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel26.setPreferredSize(new java.awt.Dimension(75, 17));
            jLabel26.setMaximumSize(new java.awt.Dimension(200, 17));
            jPanel73.add(jLabel26);
            
            jPanel71.add(jPanel73);
            
            jPanel72.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));
            
            jPanel72.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
            lblScriptTask1Id.setText("1");
            lblScriptTask1Id.setForeground(java.awt.Color.black);
            lblScriptTask1Id.setBackground(java.awt.Color.gray);
            lblScriptTask1Id.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblScriptTask1Id.setPreferredSize(new java.awt.Dimension(20, 17));
            lblScriptTask1Id.setMinimumSize(new java.awt.Dimension(10, 17));
            lblScriptTask1Id.setOpaque(true);
            jPanel72.add(lblScriptTask1Id);
            
            listScriptTask1Command.setPreferredSize(new java.awt.Dimension(150, 26));
            listScriptTask1Command.setMaximumSize(new java.awt.Dimension(150, 26));
            listScriptTask1Command.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    listScriptTask1CommandActionPerformed(evt);
                }
            });
            
            jPanel72.add(listScriptTask1Command);
            
            txtScriptTask1Param1.setPreferredSize(new java.awt.Dimension(75, 21));
            txtScriptTask1Param1.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    txtScriptTask1Param1MouseClicked(evt);
                }
            });
            
            jPanel72.add(txtScriptTask1Param1);
            
            txtScriptTask1Param2.setPreferredSize(new java.awt.Dimension(75, 21));
            jPanel72.add(txtScriptTask1Param2);
            
            txtScriptTask1Progress.setPreferredSize(new java.awt.Dimension(75, 21));
            jPanel72.add(txtScriptTask1Progress);
            
            jPanel71.add(jPanel72);
            
            jPanel74.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));
            
            jPanel74.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
            lblScriptTask2Id.setText("2");
            lblScriptTask2Id.setForeground(java.awt.Color.black);
            lblScriptTask2Id.setBackground(java.awt.Color.gray);
            lblScriptTask2Id.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblScriptTask2Id.setPreferredSize(new java.awt.Dimension(20, 17));
            lblScriptTask2Id.setMinimumSize(new java.awt.Dimension(10, 17));
            lblScriptTask2Id.setOpaque(true);
            jPanel74.add(lblScriptTask2Id);
            
            listScriptTask2Command.setPreferredSize(new java.awt.Dimension(150, 26));
            listScriptTask2Command.setMaximumSize(new java.awt.Dimension(150, 26));
            listScriptTask2Command.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    listScriptTask2CommandActionPerformed(evt);
                }
            });
            
            jPanel74.add(listScriptTask2Command);
            
            txtScriptTask2Param1.setPreferredSize(new java.awt.Dimension(75, 21));
            txtScriptTask2Param1.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    txtScriptTask2Param1MouseClicked(evt);
                }
            });
            
            jPanel74.add(txtScriptTask2Param1);
            
            txtScriptTask2Param2.setPreferredSize(new java.awt.Dimension(75, 21));
            jPanel74.add(txtScriptTask2Param2);
            
            txtScriptTask2Progress.setPreferredSize(new java.awt.Dimension(75, 21));
            jPanel74.add(txtScriptTask2Progress);
            
            jPanel71.add(jPanel74);
            
            jPanel75.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));
            
            jPanel75.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
            lblScriptTask3Id.setText("3");
            lblScriptTask3Id.setForeground(java.awt.Color.black);
            lblScriptTask3Id.setBackground(java.awt.Color.gray);
            lblScriptTask3Id.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblScriptTask3Id.setPreferredSize(new java.awt.Dimension(20, 17));
            lblScriptTask3Id.setMinimumSize(new java.awt.Dimension(10, 17));
            lblScriptTask3Id.setOpaque(true);
            jPanel75.add(lblScriptTask3Id);
            
            listScriptTask3Command.setPreferredSize(new java.awt.Dimension(150, 26));
            listScriptTask3Command.setMaximumSize(new java.awt.Dimension(150, 26));
            listScriptTask3Command.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    listScriptTask3CommandActionPerformed(evt);
                }
            });
            
            jPanel75.add(listScriptTask3Command);
            
            txtScriptTask3Param1.setPreferredSize(new java.awt.Dimension(75, 21));
            txtScriptTask3Param1.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    txtScriptTask3Param1MouseClicked(evt);
                }
            });
            
            jPanel75.add(txtScriptTask3Param1);
            
            txtScriptTask3Param2.setPreferredSize(new java.awt.Dimension(75, 21));
            jPanel75.add(txtScriptTask3Param2);
            
            txtScriptTask3Progress.setPreferredSize(new java.awt.Dimension(75, 21));
            jPanel75.add(txtScriptTask3Progress);
            
            jPanel71.add(jPanel75);
            
            jPanel76.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));
            
            jPanel76.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
            lblScriptTask4Id.setText("4");
            lblScriptTask4Id.setForeground(java.awt.Color.black);
            lblScriptTask4Id.setBackground(java.awt.Color.gray);
            lblScriptTask4Id.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblScriptTask4Id.setPreferredSize(new java.awt.Dimension(20, 17));
            lblScriptTask4Id.setMinimumSize(new java.awt.Dimension(10, 17));
            lblScriptTask4Id.setOpaque(true);
            jPanel76.add(lblScriptTask4Id);
            
            listScriptTask4Command.setPreferredSize(new java.awt.Dimension(150, 26));
            listScriptTask4Command.setMaximumSize(new java.awt.Dimension(150, 26));
            listScriptTask4Command.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    listScriptTask4CommandActionPerformed(evt);
                }
            });
            
            jPanel76.add(listScriptTask4Command);
            
            txtScriptTask4Param1.setPreferredSize(new java.awt.Dimension(75, 21));
            txtScriptTask4Param1.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    txtScriptTask4Param1MouseClicked(evt);
                }
            });
            
            jPanel76.add(txtScriptTask4Param1);
            
            txtScriptTask4Param2.setPreferredSize(new java.awt.Dimension(75, 21));
            jPanel76.add(txtScriptTask4Param2);
            
            txtScriptTask4Progress.setPreferredSize(new java.awt.Dimension(75, 21));
            jPanel76.add(txtScriptTask4Progress);
            
            jPanel71.add(jPanel76);
            
            jPanel77.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));
            
            jPanel77.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
            lblScriptTask5Id.setText("5");
            lblScriptTask5Id.setForeground(java.awt.Color.black);
            lblScriptTask5Id.setBackground(java.awt.Color.gray);
            lblScriptTask5Id.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblScriptTask5Id.setPreferredSize(new java.awt.Dimension(20, 17));
            lblScriptTask5Id.setMinimumSize(new java.awt.Dimension(10, 17));
            lblScriptTask5Id.setOpaque(true);
            jPanel77.add(lblScriptTask5Id);
            
            listScriptTask5Command.setPreferredSize(new java.awt.Dimension(150, 26));
            listScriptTask5Command.setMaximumSize(new java.awt.Dimension(150, 26));
            listScriptTask5Command.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    listScriptTask5CommandActionPerformed(evt);
                }
            });
            
            jPanel77.add(listScriptTask5Command);
            
            txtScriptTask5Param1.setPreferredSize(new java.awt.Dimension(75, 21));
            txtScriptTask5Param1.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    txtScriptTask5Param1MouseClicked(evt);
                }
            });
            
            jPanel77.add(txtScriptTask5Param1);
            
            txtScriptTask5Param2.setPreferredSize(new java.awt.Dimension(75, 21));
            jPanel77.add(txtScriptTask5Param2);
            
            txtScriptTask5Progress.setPreferredSize(new java.awt.Dimension(75, 21));
            jPanel77.add(txtScriptTask5Progress);
            
            jPanel71.add(jPanel77);
            
            jScrollPane1.setViewportView(jPanel71);
            
            jPanel33.add(jScrollPane1);
            
            jPanel70.setBorder(new javax.swing.border.TitledBorder("Script"));
            listScripts.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    listScriptsActionPerformed(evt);
                }
            });
            
            jPanel70.add(listScripts);
            
            btnLoadScript.setText("Load");
            btnLoadScript.setMargin(new java.awt.Insets(2, 5, 2, 5));
            btnLoadScript.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnLoadScriptActionPerformed(evt);
                }
            });
            
            jPanel70.add(btnLoadScript);
            
            btnSaveAsScript.setText("Save As");
            btnSaveAsScript.setMargin(new java.awt.Insets(2, 5, 2, 5));
            btnSaveAsScript.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnSaveAsScriptActionPerformed(evt);
                }
            });
            
            jPanel70.add(btnSaveAsScript);
            
            btnPlayPauseScript.setText("Play");
            btnPlayPauseScript.setPreferredSize(new java.awt.Dimension(65, 27));
            btnPlayPauseScript.setMaximumSize(new java.awt.Dimension(70, 27));
            btnPlayPauseScript.setMargin(new java.awt.Insets(2, 5, 2, 5));
            btnPlayPauseScript.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnPlayPauseScriptActionPerformed(evt);
                }
            });
            
            jPanel70.add(btnPlayPauseScript);
            
            btnResetScript.setText("Reset");
            btnResetScript.setMargin(new java.awt.Insets(2, 5, 2, 5));
            btnResetScript.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnResetScriptActionPerformed(evt);
                }
            });
            
            jPanel70.add(btnResetScript);
            
            jPanel4.add(jPanel70);
            
            jPanel33.add(jPanel4);
            
            jPanel1.add(jPanel33);
            
            jPanel15.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 1, 2));
            
            jPanel15.setBorder(new javax.swing.border.EtchedBorder());
            lblDataSize.setText("Packet Size:");
            lblDataSize.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jPanel15.add(lblDataSize);
            
            txtDataSize.setEditable(false);
            txtDataSize.setText(" ");
            txtDataSize.setPreferredSize(new java.awt.Dimension(30, 20));
            txtDataSize.setMinimumSize(new java.awt.Dimension(30, 20));
            jPanel15.add(txtDataSize);
            
            slidPacketSize.setMinorTickSpacing(10);
            slidPacketSize.setPaintTicks(true);
            slidPacketSize.setMinimum(1);
            slidPacketSize.setMajorTickSpacing(100);
            slidPacketSize.setMaximum(230);
            slidPacketSize.setName("PacketSize");
            slidPacketSize.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent evt) {
                    slidPacketSizeStateChanged(evt);
                }
            });
            
            jPanel15.add(slidPacketSize);
            
            chkDynamicPacketSize.setSelected(true);
            chkDynamicPacketSize.setText("Dynamic");
            chkDynamicPacketSize.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    chkDynamicPacketSizeActionPerformed(evt);
                }
            });
            
            jPanel15.add(chkDynamicPacketSize);
            
            jPanel1.add(jPanel15);
            
            panelScript.add(jPanel1);
            
            jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel2.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jPanel2.setBackground(java.awt.Color.black);
            pnlLoad.setLayout(new java.awt.GridLayout(8, 2));
            
            pnlLoad.setName("Progress");
            jLabel1.setText("GROUND");
            jLabel1.setForeground(java.awt.Color.yellow);
            jLabel1.setBackground(java.awt.Color.black);
            jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel1.setFont(new java.awt.Font("Dialog", 3, 12));
            jLabel1.setOpaque(true);
            pnlLoad.add(jLabel1);
            
            jLabel2.setText("SATELLITE");
            jLabel2.setForeground(java.awt.Color.yellow);
            jLabel2.setBackground(java.awt.Color.black);
            jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel2.setFont(new java.awt.Font("Dialog", 3, 12));
            jLabel2.setOpaque(true);
            pnlLoad.add(jLabel2);
            
            jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.X_AXIS));
            
            btnLoad.setText("Load Task");
            btnLoad.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
            btnLoad.setMaximumSize(new java.awt.Dimension(1000, 50));
            btnLoad.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnLoad1ActionPerformed(evt);
                }
            });
            
            jPanel12.add(btnLoad);
            
            pnlLoad.add(jPanel12);
            
            lblAckReq.setText("ACK");
            lblAckReq.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblAckReq.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            lblAckReq.setOpaque(true);
            pnlLoad.add(lblAckReq);
            
            lblEmpty1.setText(" ");
            lblEmpty1.setBackground(java.awt.Color.lightGray);
            lblEmpty1.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            lblEmpty1.setOpaque(true);
            pnlLoad.add(lblEmpty1);
            
            lblAckLoad.setText("LOAD");
            lblAckLoad.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblAckLoad.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            lblAckLoad.setOpaque(true);
            pnlLoad.add(lblAckLoad);
            
            lblEmpty2.setText(" ");
            lblEmpty2.setBackground(java.awt.Color.lightGray);
            lblEmpty2.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            lblEmpty2.setOpaque(true);
            pnlLoad.add(lblEmpty2);
            
            lblAckAddress.setText("ADRESS");
            lblAckAddress.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblAckAddress.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            lblAckAddress.setOpaque(true);
            pnlLoad.add(lblAckAddress);
            
            jPanel13.setLayout(new javax.swing.BoxLayout(jPanel13, javax.swing.BoxLayout.X_AXIS));
            
            btnSetSegment.setText("Set Segment");
            btnSetSegment.setPreferredSize(new java.awt.Dimension(85, 23));
            btnSetSegment.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
            btnSetSegment.setMaximumSize(new java.awt.Dimension(1000, 50));
            jPanel13.add(btnSetSegment);
            
            txtSegment.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
            jPanel13.add(txtSegment);
            
            pnlLoad.add(jPanel13);
            
            lblSetSegmentAck.setText("ACK");
            lblSetSegmentAck.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblSetSegmentAck.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            lblSetSegmentAck.setOpaque(true);
            pnlLoad.add(lblSetSegmentAck);
            
            jPanel56.setLayout(new javax.swing.BoxLayout(jPanel56, javax.swing.BoxLayout.X_AXIS));
            
            btnSendData.setText("Send Data");
            btnSendData.setPreferredSize(new java.awt.Dimension(85, 23));
            btnSendData.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
            btnSendData.setMaximumSize(new java.awt.Dimension(1000, 50));
            jPanel56.add(btnSendData);
            
            txtDataSegment.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
            jPanel56.add(txtDataSegment);
            
            jLabel23.setText(":");
            jPanel56.add(jLabel23);
            
            txtDataOffset.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
            jPanel56.add(txtDataOffset);
            
            pnlLoad.add(jPanel56);
            
            lblDataAck.setText("ACK");
            lblDataAck.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblDataAck.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            lblDataAck.setOpaque(true);
            pnlLoad.add(lblDataAck);
            
            jPanel57.setLayout(new javax.swing.BoxLayout(jPanel57, javax.swing.BoxLayout.X_AXIS));
            
            btnSendEOF.setText("Send EOF");
            btnSendEOF.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
            btnSendEOF.setMaximumSize(new java.awt.Dimension(1000, 50));
            jPanel57.add(btnSendEOF);
            
            pnlLoad.add(jPanel57);
            
            lblEOFAck.setText("ACK");
            lblEOFAck.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblEOFAck.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            lblEOFAck.setOpaque(true);
            pnlLoad.add(lblEOFAck);
            
            jPanel59.setLayout(new javax.swing.BoxLayout(jPanel59, javax.swing.BoxLayout.X_AXIS));
            
            btnCreateTask.setText("Create Task");
            btnCreateTask.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
            btnCreateTask.setMaximumSize(new java.awt.Dimension(1000, 50));
            jPanel59.add(btnCreateTask);
            
            pnlLoad.add(jPanel59);
            
            lblStartAck.setText("ACK");
            lblStartAck.setToolTipText("");
            lblStartAck.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblStartAck.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            lblStartAck.setOpaque(true);
            pnlLoad.add(lblStartAck);
            
            jPanel2.add(pnlLoad);
            
            btnRun.setText("Execute Loaded Tasks");
            btnRun.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnRun1ActionPerformed(evt);
                }
            });
            
            jPanel68.add(btnRun);
            
            btnReset.setForeground(java.awt.Color.yellow);
            btnReset.setFont(new java.awt.Font("Dialog", 3, 11));
            btnReset.setText("RESET LOAD");
            btnReset.setBackground(java.awt.Color.red);
            btnReset.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnResetActionPerformed(evt);
                }
            });
            
            jPanel68.add(btnReset);
            
            jPanel2.add(jPanel68);
            
            panelScript.add(jPanel2);
            
            panelMain.addTab("Script", panelScript);
            
            panelCommands.setPreferredSize(new java.awt.Dimension(910, 450));
            commandTabs.setPreferredSize(new java.awt.Dimension(790, 250));
            commandTabs.setMinimumSize(new java.awt.Dimension(700, 200));
            panelCommands.add(commandTabs);
            
            panelMain.addTab("PHT-Commands", panelCommands);
            
            panelDiagnostic.setLayout(new java.awt.GridLayout(1, 3));
            
            panelDiagnostic.setBackground(new java.awt.Color(153, 153, 204));
            panelDiagnostic.addComponentListener(new java.awt.event.ComponentAdapter() {
                public void componentShown(java.awt.event.ComponentEvent evt) {
                    panelDiagnosticComponentShown(evt);
                }
            });
            
            jPanel16.setLayout(new java.awt.GridLayout(5, 1));
            
            jPanel16.setBorder(new javax.swing.border.TitledBorder("Memory Management"));
            jPanel16.setMinimumSize(new java.awt.Dimension(200, 200));
            jPanel16.setMaximumSize(new java.awt.Dimension(2000, 2000));
            jPanel18.setLayout(new java.awt.GridLayout(2, 1));
            
            jPanel18.setBorder(new javax.swing.border.CompoundBorder());
            jLabel4.setText("Segment used for the housekeeping task:");
            jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel4.setAlignmentX(0.5F);
            jPanel31.add(jLabel4);
            
            jPanel18.add(jPanel31);
            
            jPanel32.setLayout(new java.awt.GridLayout(1, 1));
            
            txtPHTSeg.setPreferredSize(new java.awt.Dimension(50, 20));
            txtPHTSeg.setMaximumSize(new java.awt.Dimension(50, 20));
            txtPHTSeg.setMinimumSize(new java.awt.Dimension(50, 20));
            jPanel32.add(txtPHTSeg);
            
            jPanel18.add(jPanel32);
            
            jPanel16.add(jPanel18);
            
            jPanel19.setLayout(new java.awt.GridLayout(2, 1));
            
            jPanel19.setBorder(new javax.swing.border.CompoundBorder());
            jLabel7.setText("Size of largest block available:");
            jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jPanel34.add(jLabel7);
            
            jPanel19.add(jPanel34);
            
            jPanel35.setLayout(new java.awt.GridLayout(1, 1));
            
            txtLargestBlock.setPreferredSize(new java.awt.Dimension(50, 20));
            txtLargestBlock.setMaximumSize(new java.awt.Dimension(50, 20));
            txtLargestBlock.setMinimumSize(new java.awt.Dimension(50, 20));
            jPanel35.add(txtLargestBlock);
            
            jPanel19.add(jPanel35);
            
            jPanel16.add(jPanel19);
            
            jPanel20.setLayout(new java.awt.GridLayout(2, 1));
            
            jPanel20.setBorder(new javax.swing.border.CompoundBorder());
            jLabel8.setText("Total memory available:");
            jPanel36.add(jLabel8);
            
            jPanel20.add(jPanel36);
            
            jPanel37.setLayout(new java.awt.GridLayout(1, 1));
            
            txtMemAvail.setPreferredSize(new java.awt.Dimension(50, 20));
            txtMemAvail.setMaximumSize(new java.awt.Dimension(50, 20));
            txtMemAvail.setMinimumSize(new java.awt.Dimension(50, 20));
            jPanel37.add(txtMemAvail);
            
            jPanel20.add(jPanel37);
            
            jPanel16.add(jPanel20);
            
            jPanel26.setLayout(new java.awt.GridLayout(2, 1));
            
            jPanel26.setBorder(new javax.swing.border.CompoundBorder());
            lblEDACErr.setText("EDAC error byte:");
            jPanel39.add(lblEDACErr);
            
            jPanel26.add(jPanel39);
            
            jPanel38.setLayout(new java.awt.GridLayout(1, 1));
            
            txtEDACErr.setPreferredSize(new java.awt.Dimension(50, 20));
            txtEDACErr.setMaximumSize(new java.awt.Dimension(50, 20));
            txtEDACErr.setMinimumSize(new java.awt.Dimension(50, 20));
            jPanel38.add(txtEDACErr);
            
            jPanel26.add(jPanel38);
            
            jPanel16.add(jPanel26);
            
            panelDiagnostic.add(jPanel16);
            
            jPanel21.setLayout(new java.awt.GridLayout(9, 1));
            
            jPanel21.setBorder(new javax.swing.border.TitledBorder("Communication Link"));
            jPanel22.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jPanel22.setBorder(new javax.swing.border.CompoundBorder());
            jPanel22.setPreferredSize(new java.awt.Dimension(218, 30));
            jPanel22.setMinimumSize(new java.awt.Dimension(218, 30));
            jLabel9.setText("Downlink queue:");
            jPanel41.add(jLabel9);
            
            jPanel22.add(jPanel41);
            
            jPanel40.setLayout(new java.awt.GridLayout(1, 1));
            
            jPanel40.setPreferredSize(new java.awt.Dimension(70, 25));
            jPanel40.setMinimumSize(new java.awt.Dimension(70, 25));
            jPanel40.setMaximumSize(new java.awt.Dimension(70, 25));
            jPanel40.setOpaque(false);
            txtTransmitQueue.setPreferredSize(new java.awt.Dimension(50, 20));
            txtTransmitQueue.setMaximumSize(new java.awt.Dimension(70, 20));
            txtTransmitQueue.setMinimumSize(new java.awt.Dimension(70, 20));
            jPanel40.add(txtTransmitQueue);
            
            jPanel22.add(jPanel40);
            
            jPanel21.add(jPanel22);
            
            jPanel23.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jPanel23.setBorder(new javax.swing.border.CompoundBorder());
            jLabel10.setText("Digipeat mode:");
            jPanel43.add(jLabel10);
            
            jPanel23.add(jPanel43);
            
            jPanel42.setLayout(new java.awt.GridLayout(1, 1));
            
            jPanel42.setPreferredSize(new java.awt.Dimension(70, 25));
            jPanel42.setMinimumSize(new java.awt.Dimension(70, 25));
            jPanel42.setMaximumSize(new java.awt.Dimension(70, 25));
            txtDigipeat.setPreferredSize(new java.awt.Dimension(70, 20));
            txtDigipeat.setMaximumSize(new java.awt.Dimension(70, 20));
            txtDigipeat.setMinimumSize(new java.awt.Dimension(70, 20));
            jPanel42.add(txtDigipeat);
            
            jPanel23.add(jPanel42);
            
            jPanel21.add(jPanel23);
            
            jPanel27.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jPanel27.setBorder(new javax.swing.border.CompoundBorder());
            jLabel13.setText("Char. received, port 0:");
            jPanel45.add(jLabel13);
            
            jPanel27.add(jPanel45);
            
            jPanel44.setLayout(new java.awt.GridLayout(1, 1));
            
            jPanel44.setPreferredSize(new java.awt.Dimension(70, 25));
            jPanel44.setMinimumSize(new java.awt.Dimension(50, 25));
            txtRX0.setPreferredSize(new java.awt.Dimension(75, 20));
            txtRX0.setMaximumSize(new java.awt.Dimension(75, 20));
            txtRX0.setMinimumSize(new java.awt.Dimension(75, 20));
            jPanel44.add(txtRX0);
            
            jPanel27.add(jPanel44);
            
            jPanel21.add(jPanel27);
            
            jPanel61.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jLabel12.setText("Char. received, port 1:");
            jPanel61.add(jLabel12);
            
            txtRX1.setPreferredSize(new java.awt.Dimension(70, 25));
            txtRX1.setMinimumSize(new java.awt.Dimension(70, 25));
            jPanel61.add(txtRX1);
            
            jPanel21.add(jPanel61);
            
            jPanel28.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jPanel28.setBorder(new javax.swing.border.CompoundBorder());
            jLabel14.setText("Char. received, port 2:");
            jPanel47.add(jLabel14);
            
            jPanel28.add(jPanel47);
            
            jPanel46.setLayout(new java.awt.GridLayout(1, 1));
            
            jPanel46.setPreferredSize(new java.awt.Dimension(70, 25));
            jPanel46.setMinimumSize(new java.awt.Dimension(70, 25));
            txtRX2.setPreferredSize(new java.awt.Dimension(70, 20));
            txtRX2.setMaximumSize(new java.awt.Dimension(70, 20));
            txtRX2.setMinimumSize(new java.awt.Dimension(70, 20));
            jPanel46.add(txtRX2);
            
            jPanel28.add(jPanel46);
            
            jPanel21.add(jPanel28);
            
            jPanel62.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jLabel17.setText("Char. received, port 3:");
            jPanel62.add(jLabel17);
            
            txtRX3.setPreferredSize(new java.awt.Dimension(70, 25));
            txtRX3.setMinimumSize(new java.awt.Dimension(70, 25));
            jPanel62.add(txtRX3);
            
            jPanel21.add(jPanel62);
            
            jPanel63.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jLabel18.setText("Char. received, port 4:");
            jPanel63.add(jLabel18);
            
            txtRX4.setPreferredSize(new java.awt.Dimension(70, 25));
            txtRX4.setMinimumSize(new java.awt.Dimension(70, 25));
            jPanel63.add(txtRX4);
            
            jPanel21.add(jPanel63);
            
            jPanel64.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jLabel19.setText("Char. received, port 5:");
            jPanel64.add(jLabel19);
            
            txtRX5.setPreferredSize(new java.awt.Dimension(70, 25));
            txtRX5.setMinimumSize(new java.awt.Dimension(70, 25));
            jPanel64.add(txtRX5);
            
            jPanel21.add(jPanel64);
            
            jPanel30.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jPanel30.setBorder(new javax.swing.border.CompoundBorder());
            jLabel16.setText("Tx overflow count:");
            jPanel49.add(jLabel16);
            
            jPanel30.add(jPanel49);
            
            jPanel48.setLayout(new java.awt.GridLayout(1, 1));
            
            jPanel48.setPreferredSize(new java.awt.Dimension(70, 25));
            jPanel48.setMinimumSize(new java.awt.Dimension(70, 25));
            txtTransmitOverflow.setPreferredSize(new java.awt.Dimension(70, 20));
            txtTransmitOverflow.setMaximumSize(new java.awt.Dimension(70, 20));
            txtTransmitOverflow.setMinimumSize(new java.awt.Dimension(70, 20));
            jPanel48.add(txtTransmitOverflow);
            
            jPanel30.add(jPanel48);
            
            jPanel21.add(jPanel30);
            
            panelDiagnostic.add(jPanel21);
            
            jPanel24.setLayout(new java.awt.GridLayout(5, 1));
            
            jPanel24.setBorder(new javax.swing.border.TitledBorder("Task Management"));
            jPanel25.setLayout(new java.awt.GridLayout(2, 1));
            
            jPanel25.setBorder(new javax.swing.border.CompoundBorder());
            jLabel11.setText("Number of last task built:");
            jLabel11.setToolTipText("Number of last task built");
            jPanel50.add(jLabel11);
            
            jPanel25.add(jPanel50);
            
            jPanel51.setLayout(new java.awt.GridLayout(1, 1));
            
            txtTaskNumber.setPreferredSize(new java.awt.Dimension(50, 20));
            txtTaskNumber.setMaximumSize(new java.awt.Dimension(50, 20));
            txtTaskNumber.setMinimumSize(new java.awt.Dimension(50, 20));
            jPanel51.add(txtTaskNumber);
            
            jPanel25.add(jPanel51);
            
            jPanel24.add(jPanel25);
            
            jPanel17.setLayout(new java.awt.GridLayout(2, 1));
            
            jPanel17.setBorder(new javax.swing.border.CompoundBorder());
            jLabel3.setText("Segment used for current load:");
            jPanel52.add(jLabel3);
            
            jPanel17.add(jPanel52);
            
            jPanel53.setLayout(new java.awt.GridLayout(1, 1));
            
            txtLoadSegment.setPreferredSize(new java.awt.Dimension(50, 20));
            txtLoadSegment.setMaximumSize(new java.awt.Dimension(50, 20));
            txtLoadSegment.setMinimumSize(new java.awt.Dimension(50, 20));
            jPanel53.add(txtLoadSegment);
            
            jPanel17.add(jPanel53);
            
            jPanel24.add(jPanel17);
            
            jPanel29.setLayout(new java.awt.GridLayout(2, 1));
            
            jPanel29.setBorder(new javax.swing.border.CompoundBorder());
            jLabel15.setText("Rate of housekeeping task:");
            jPanel54.add(jLabel15);
            
            jPanel29.add(jPanel54);
            
            jPanel55.setLayout(new java.awt.GridLayout(1, 1));
            
            txtPHTRate.setPreferredSize(new java.awt.Dimension(50, 20));
            txtPHTRate.setMaximumSize(new java.awt.Dimension(50, 20));
            txtPHTRate.setMinimumSize(new java.awt.Dimension(50, 20));
            jPanel55.add(txtPHTRate);
            
            jPanel29.add(jPanel55);
            
            jPanel24.add(jPanel29);
            
            jButton4.setText("ECHO a Character String");
            jButton4.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton4ActionPerformed(evt);
                }
            });
            
            jPanel24.add(jButton4);
            
            jButton2.setText("Flood (TEST TASK ONLY)");
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton2ActionPerformed(evt);
                }
            });
            
            jPanel24.add(jButton2);
            
            panelDiagnostic.add(jPanel24);
            
            panelMain.addTab("PHT-Diagnostic", panelDiagnostic);
            
            panelRAMDisk.setLayout(new javax.swing.BoxLayout(panelRAMDisk, javax.swing.BoxLayout.Y_AXIS));
            
            jLabel53.setText("IMPORTANT: MFILE and TSTFS must be loaded for this panel to function.");
            jLabel53.setForeground(java.awt.Color.yellow);
            jLabel53.setBackground(java.awt.Color.gray);
            jLabel53.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel53.setFont(new java.awt.Font("Verdana", 1, 12));
            jLabel53.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jLabel53.setAlignmentX(0.5F);
            jLabel53.setMaximumSize(new java.awt.Dimension(1000, 1000));
            jLabel53.setOpaque(true);
            jLabel53.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            panelRAMDisk.add(jLabel53);
            
            jPanel111.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
            
            jPanel111.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            btnLoadFile.setText("Upload a File");
            btnLoadFile.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnLoadFileActionPerformed(evt);
                }
            });
            
            jPanel111.add(btnLoadFile);
            
            btnReadFile.setText("Read a File");
            btnReadFile.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnReadFileActionPerformed(evt);
                }
            });
            
            jPanel111.add(btnReadFile);
            
            btnFSDeleteFile.setText("Delete a File");
            btnFSDeleteFile.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnFSDeleteFileActionPerformed(evt);
                }
            });
            
            jPanel111.add(btnFSDeleteFile);
            
            btnFSTestFill.setText("   Test Fill   ");
            btnFSTestFill.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnFSTestFillActionPerformed(evt);
                }
            });
            
            jPanel111.add(btnFSTestFill);
            
            btnFSAbort.setForeground(java.awt.Color.yellow);
            btnFSAbort.setText("File System Interface Task Abort");
            btnFSAbort.setBackground(java.awt.Color.red);
            btnFSAbort.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnFSAbortActionPerformed(evt);
                }
            });
            
            jPanel111.add(btnFSAbort);
            
            panelRAMDisk.add(jPanel111);
            
            jPanel112.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jPanel166.setBorder(new javax.swing.border.TitledBorder("RAM Disk Structure"));
            jScrollPane2.setPreferredSize(new java.awt.Dimension(600, 150));
            txtFSFormat.setLineWrap(true);
            txtFSFormat.setBackground(new java.awt.Color(204, 255, 255));
            jScrollPane2.setViewportView(txtFSFormat);
            
            jPanel166.add(jScrollPane2);
            
            jPanel112.add(jPanel166);
            
            jPanel165.setBorder(new javax.swing.border.TitledBorder("File Directory"));
            jScrollPane4.setPreferredSize(new java.awt.Dimension(200, 150));
            txtFSDir.setFont(new java.awt.Font("Courier New", 0, 11));
            jScrollPane4.setViewportView(txtFSDir);
            
            jPanel165.add(jScrollPane4);
            
            jPanel112.add(jPanel165);
            
            panelRAMDisk.add(jPanel112);
            
            jPanel113.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jPanel113.setPreferredSize(new java.awt.Dimension(612, 15));
            prgrssRamdiskTest.setForeground(java.awt.Color.cyan);
            prgrssRamdiskTest.setPreferredSize(new java.awt.Dimension(600, 15));
            jPanel113.add(prgrssRamdiskTest);
            
            panelRAMDisk.add(jPanel113);
            
            panelMain.addTab("RAM Disk", panelRAMDisk);
            
            getContentPane().add(panelMain);
            
            jCommonPortion.setLayout(new javax.swing.BoxLayout(jCommonPortion, javax.swing.BoxLayout.Y_AXIS));
            
            jCommonPortion.setBackground(java.awt.Color.black);
            jCommonPortion.setPreferredSize(new java.awt.Dimension(795, 300));
            jCommonPortion.setMaximumSize(new java.awt.Dimension(2000, 2000));
            panelInfo.setLayout(new java.awt.CardLayout());
            
            panelInfo.setBackground(java.awt.Color.darkGray);
            panelInfo.setPreferredSize(new java.awt.Dimension(795, 200));
            panelInfo.setMaximumSize(new java.awt.Dimension(2147483647, 200));
            panelMessages.setLayout(new javax.swing.BoxLayout(panelMessages, javax.swing.BoxLayout.X_AXIS));
            
            panelMessages.setBorder(new javax.swing.border.TitledBorder("Message Window"));
            panelMessages.setPreferredSize(new java.awt.Dimension(500, 200));
            scrpaneInfo.setPreferredSize(new java.awt.Dimension(500, 120));
            scrpaneInfo.setViewportView(txtInfo);
            
            panelMessages.add(scrpaneInfo);
            
            jPanel14.setLayout(new javax.swing.BoxLayout(jPanel14, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel14.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jPanel14.setBackground(java.awt.Color.darkGray);
            jPanel14.setPreferredSize(new java.awt.Dimension(200, 72));
            jPanel14.setMinimumSize(new java.awt.Dimension(50, 72));
            jPanel14.setMaximumSize(new java.awt.Dimension(200, 32767));
            jPanel65.setLayout(new javax.swing.BoxLayout(jPanel65, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel65.setMinimumSize(new java.awt.Dimension(300, 120));
            chkTxtFollow.setSelected(true);
            chkTxtFollow.setText("Follow Data Received");
            chkTxtFollow.setPreferredSize(new java.awt.Dimension(300, 24));
            chkTxtFollow.setMaximumSize(new java.awt.Dimension(300, 24));
            chkTxtFollow.setMinimumSize(new java.awt.Dimension(150, 24));
            chkTxtFollow.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    chkTxtFollowActionPerformed(evt);
                }
            });
            
            jPanel65.add(chkTxtFollow);
            
            chkShowTimeFrames.setSelected(true);
            chkShowTimeFrames.setText("Show TIME Frames");
            chkShowTimeFrames.setPreferredSize(new java.awt.Dimension(150, 24));
            chkShowTimeFrames.setMaximumSize(new java.awt.Dimension(150, 24));
            chkShowTimeFrames.setMinimumSize(new java.awt.Dimension(150, 24));
            jPanel65.add(chkShowTimeFrames);
            
            chkShowTLMFrames.setSelected(true);
            chkShowTLMFrames.setText("Show/Decode TELEMETRY");
            chkShowTLMFrames.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    chkShowTLMFramesActionPerformed(evt);
                }
            });
            
            jPanel65.add(chkShowTLMFrames);
            
            chkShowSTATFrames.setSelected(true);
            chkShowSTATFrames.setText("Show STATUS Frames");
            chkShowSTATFrames.setPreferredSize(new java.awt.Dimension(150, 24));
            chkShowSTATFrames.setMaximumSize(new java.awt.Dimension(150, 24));
            jPanel65.add(chkShowSTATFrames);
            
            chkShowLDRFrames.setSelected(true);
            chkShowLDRFrames.setText("Show LOADER Frames");
            chkShowLDRFrames.setPreferredSize(new java.awt.Dimension(150, 24));
            chkShowLDRFrames.setMaximumSize(new java.awt.Dimension(150, 24));
            jPanel65.add(chkShowLDRFrames);
            
            jPanel14.add(jPanel65);
            
            btnTxtSave.setText("Save to File");
            btnTxtSave.setAlignmentY(1.0F);
            btnTxtSave.setPreferredSize(new java.awt.Dimension(200, 25));
            btnTxtSave.setMaximumSize(new java.awt.Dimension(200, 75));
            btnTxtSave.setMinimumSize(new java.awt.Dimension(120, 25));
            btnTxtSave.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnTxtSaveActionPerformed(evt);
                }
            });
            
            jPanel14.add(btnTxtSave);
            
            panelMessages.add(jPanel14);
            
            panelInfo.add(panelMessages, "messages");
            
            jCommonPortion.add(panelInfo);
            
            jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.X_AXIS));
            
            jPanel5.setForeground(java.awt.Color.white);
            jPanel5.setBackground(java.awt.Color.black);
            jPanel5.setPreferredSize(new java.awt.Dimension(643, 100));
            panelPassManagement.setLayout(new javax.swing.BoxLayout(panelPassManagement, javax.swing.BoxLayout.Y_AXIS));
            
            panelPassManagement.setMaximumSize(new java.awt.Dimension(100, 75));
            lblPass.setText("PASS");
            lblPass.setForeground(java.awt.Color.black);
            lblPass.setBackground(java.awt.Color.gray);
            lblPass.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblPass.setPreferredSize(new java.awt.Dimension(40, 17));
            lblPass.setMaximumSize(new java.awt.Dimension(1000, 17));
            lblPass.setOpaque(true);
            panelPassManagement.add(lblPass);
            
            btnIgnorePass.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
            btnIgnorePass.setText("Ignore");
            btnIgnorePass.setPreferredSize(new java.awt.Dimension(93, 20));
            btnIgnorePass.setMinimumSize(new java.awt.Dimension(80, 20));
            btnIgnorePass.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnIgnorePassActionPerformed(evt);
                }
            });
            
            panelPassManagement.add(btnIgnorePass);
            
            btnPausePass.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
            btnPausePass.setText("Pause");
            btnPausePass.setPreferredSize(new java.awt.Dimension(93, 20));
            btnPausePass.setMinimumSize(new java.awt.Dimension(80, 20));
            btnPausePass.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnPausePassActionPerformed(evt);
                }
            });
            
            panelPassManagement.add(btnPausePass);
            
            jPanel5.add(panelPassManagement);
            
            panelComSetup.setPreferredSize(new java.awt.Dimension(200, 100));
            panelComSetup.setMinimumSize(new java.awt.Dimension(100, 50));
            panelComSetup.setMaximumSize(new java.awt.Dimension(300, 100));
            panelComSetup.setOpaque(false);
            jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel6.setOpaque(false);
            jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jPanel10.setOpaque(false);
            jLabel21.setText("Sat:");
            jLabel21.setForeground(java.awt.Color.lightGray);
            jPanel10.add(jLabel21);
            
            txtSpacecraft.setBackground(new java.awt.Color(255, 255, 204));
            txtSpacecraft.setPreferredSize(new java.awt.Dimension(110, 21));
            txtSpacecraft.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    txtSpacecraftActionPerformed(evt);
                }
            });
            
            jPanel10.add(txtSpacecraft);
            
            btnPing.setFont(new java.awt.Font("Dialog", 0, 10));
            btnPing.setText("Ack");
            btnPing.setMargin(new java.awt.Insets(2, 7, 2, 7));
            btnPing.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnPingActionPerformed(evt);
                }
            });
            
            jPanel10.add(btnPing);
            
            jPanel6.add(jPanel10);
            
            jPanel11.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
            
            jPanel11.setOpaque(false);
            jLabel22.setText("Gnd:");
            jLabel22.setForeground(java.awt.Color.lightGray);
            jPanel11.add(jLabel22);
            
            listStations.setForeground(java.awt.Color.white);
            listStations.setBackground(java.awt.Color.gray);
            listStations.setPreferredSize(new java.awt.Dimension(120, 26));
            listStations.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    listStationsActionPerformed(evt);
                }
            });
            
            jPanel11.add(listStations);
            
            jButton1.setText("Connect");
            jButton1.setMargin(new java.awt.Insets(2, 5, 2, 5));
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });
            
            jPanel11.add(jButton1);
            
            jPanel6.add(jPanel11);
            
            panelComSetup.add(jPanel6);
            
            jPanel5.add(panelComSetup);
            
            jGeneralStatus.setLayout(new java.awt.GridLayout(3, 1));
            
            jGeneralStatus.setPreferredSize(new java.awt.Dimension(250, 100));
            jGeneralStatus.setMinimumSize(new java.awt.Dimension(100, 50));
            jGeneralStatus.setMaximumSize(new java.awt.Dimension(300, 100));
            jGeneralStatus.setOpaque(false);
            jPanel82.setLayout(new java.awt.GridLayout(1, 4));
            
            jPanel82.setOpaque(false);
            jLabel174.setText("SSID:");
            jLabel174.setForeground(java.awt.Color.white);
            jLabel174.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jPanel82.add(jLabel174);
            
            listSSID.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    listSSIDActionPerformed(evt);
                }
            });
            
            jPanel82.add(listSSID);
            
            jLabel32.setText("Tlm UTC:");
            jLabel32.setForeground(java.awt.Color.white);
            jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jPanel82.add(jLabel32);
            
            txtTelemUTC.setForeground(java.awt.Color.white);
            txtTelemUTC.setText("0");
            txtTelemUTC.setBackground(java.awt.Color.darkGray);
            txtTelemUTC.setHorizontalAlignment(javax.swing.JTextField.CENTER);
            jPanel82.add(txtTelemUTC);
            
            jGeneralStatus.add(jPanel82);
            
            jPanel7.setLayout(new java.awt.GridLayout(1, 4));
            
            jPanel7.setOpaque(false);
            lblRxCount.setText("RX Count: ");
            lblRxCount.setForeground(java.awt.Color.cyan);
            lblRxCount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jPanel7.add(lblRxCount);
            
            txtRxCount.setToolTipText("Counts the number of frames received from the spacecraft");
            txtRxCount.setForeground(java.awt.Color.white);
            txtRxCount.setText("0");
            txtRxCount.setBackground(java.awt.Color.darkGray);
            txtRxCount.setHorizontalAlignment(javax.swing.JTextField.CENTER);
            jPanel7.add(txtRxCount);
            
            lblTxCount.setText("TX Count:");
            lblTxCount.setForeground(java.awt.Color.cyan);
            lblTxCount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jPanel7.add(lblTxCount);
            
            txtTxCount.setToolTipText("Counts the number of frames sent to the spacecraft");
            txtTxCount.setForeground(java.awt.Color.white);
            txtTxCount.setText("0");
            txtTxCount.setBackground(java.awt.Color.darkGray);
            txtTxCount.setHorizontalAlignment(javax.swing.JTextField.CENTER);
            jPanel7.add(txtTxCount);
            
            jGeneralStatus.add(jPanel7);
            
            jPanel9.setLayout(new java.awt.GridLayout(1, 4));
            
            jPanel9.setOpaque(false);
            jLabel5.setText("RX Errors:");
            jLabel5.setForeground(java.awt.Color.red);
            jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jPanel9.add(jLabel5);
            
            txtRxErrors.setToolTipText("Counts the number of bad frames received from the spacecraft");
            txtRxErrors.setForeground(java.awt.Color.white);
            txtRxErrors.setText("0");
            txtRxErrors.setBackground(java.awt.Color.darkGray);
            txtRxErrors.setHorizontalAlignment(javax.swing.JTextField.CENTER);
            jPanel9.add(txtRxErrors);
            
            jLabel6.setText("Timeouts:");
            jLabel6.setForeground(java.awt.Color.red);
            jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jPanel9.add(jLabel6);
            
            txtTimeouts.setForeground(java.awt.Color.white);
            txtTimeouts.setText("0");
            txtTimeouts.setBackground(java.awt.Color.darkGray);
            txtTimeouts.setHorizontalAlignment(javax.swing.JTextField.CENTER);
            jPanel9.add(txtTimeouts);
            
            jGeneralStatus.add(jPanel9);
            
            jPanel5.add(jGeneralStatus);
            
            pnlCommand.setLayout(new javax.swing.BoxLayout(pnlCommand, javax.swing.BoxLayout.X_AXIS));
            
            pnlCommand.setBackground(java.awt.Color.lightGray);
            pnlCommand.setPreferredSize(new java.awt.Dimension(100, 75));
            pnlCommand.setMinimumSize(new java.awt.Dimension(100, 50));
            pnlCommand.setMaximumSize(new java.awt.Dimension(1000, 100));
            jPanel58.setLayout(new javax.swing.BoxLayout(jPanel58, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel58.setPreferredSize(new java.awt.Dimension(100, 75));
            jPanel58.setMinimumSize(new java.awt.Dimension(75, 50));
            jPanel69.setLayout(new javax.swing.BoxLayout(jPanel69, javax.swing.BoxLayout.X_AXIS));
            
            jPanel69.setPreferredSize(new java.awt.Dimension(65, 22));
            lblLoading.setText("Load");
            lblLoading.setForeground(java.awt.Color.gray);
            lblLoading.setBackground(new java.awt.Color(0, 102, 0));
            lblLoading.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblLoading.setFont(new java.awt.Font("Dialog", 1, 11));
            lblLoading.setPreferredSize(new java.awt.Dimension(56, 22));
            lblLoading.setMinimumSize(new java.awt.Dimension(20, 22));
            lblLoading.setAlignmentX(0.5F);
            lblLoading.setMaximumSize(new java.awt.Dimension(56, 100));
            lblLoading.setOpaque(true);
            jPanel69.add(lblLoading);
            
            prgrssLoad.setForeground(java.awt.Color.blue);
            prgrssLoad.setBackground(java.awt.Color.lightGray);
            prgrssLoad.setFont(new java.awt.Font("Dialog", 1, 11));
            prgrssLoad.setPreferredSize(new java.awt.Dimension(50, 21));
            prgrssLoad.setMinimumSize(new java.awt.Dimension(10, 10));
            prgrssLoad.setStringPainted(true);
            prgrssLoad.setMaximumSize(new java.awt.Dimension(32767, 100));
            jPanel69.add(prgrssLoad);
            
            jPanel58.add(jPanel69);
            
            jPanel8.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
            jPanel8.setPreferredSize(new java.awt.Dimension(200, 40));
            jPanel8.setMaximumSize(new java.awt.Dimension(32767, 60));
            lblProto.setText("PROTO");
            lblProto.setForeground(java.awt.Color.darkGray);
            lblProto.setBackground(java.awt.Color.gray);
            lblProto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblProto.setPreferredSize(new java.awt.Dimension(56, 20));
            lblProto.setMaximumSize(new java.awt.Dimension(75, 50));
            lblProto.setOpaque(true);
            jPanel8.add(lblProto);
            
            lblDCD.setText("DCD");
            lblDCD.setForeground(java.awt.Color.darkGray);
            lblDCD.setBackground(java.awt.Color.gray);
            lblDCD.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblDCD.setPreferredSize(new java.awt.Dimension(56, 22));
            lblDCD.setMaximumSize(new java.awt.Dimension(60, 30));
            lblDCD.setOpaque(true);
            jPanel8.add(lblDCD);
            
            lblRXError.setText("RX Err");
            lblRXError.setForeground(java.awt.Color.gray);
            lblRXError.setBackground(new java.awt.Color(102, 0, 0));
            lblRXError.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblRXError.setFont(new java.awt.Font("Dialog", 1, 11));
            lblRXError.setPreferredSize(new java.awt.Dimension(56, 22));
            lblRXError.setMinimumSize(new java.awt.Dimension(20, 22));
            lblRXError.setAlignmentX(0.5F);
            lblRXError.setMaximumSize(new java.awt.Dimension(56, 100));
            lblRXError.setOpaque(true);
            jPanel8.add(lblRXError);
            
            jPanel58.add(jPanel8);
            
            pnlCommand.add(jPanel58);
            
            jPanel5.add(pnlCommand);
            
            jCommonPortion.add(jPanel5);
            
            jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.X_AXIS));
            
            jCommonPortion.add(jPanel3);
            
            getContentPane().add(jCommonPortion);
            
            setJMenuBar(menuBar);
        }//GEN-END:initComponents

    private void btnSerial19200ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSerial19200ActionPerformed
        if( srvLnk != null ) {
            System.out.println("Sent command to change sc link to 19200bps");
            srvLnk.setSpacecraftLinkSpeed( 19200 );
        }
    }//GEN-LAST:event_btnSerial19200ActionPerformed

    private void btnSerial9600ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSerial9600ActionPerformed
        if( srvLnk != null ) {
            System.out.println("Sent command to change sc link to 9600bps");
            srvLnk.setSpacecraftLinkSpeed( 9600 );
        }
    }//GEN-LAST:event_btnSerial9600ActionPerformed

    private void btnPlayPauseScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayPauseScriptActionPerformed
        switch( scriptState ) {
            case SCRIPT_STANDBYE:
                scriptState = SCRIPT_RUNNING;
                break;
            case SCRIPT_RUNNING:
                scriptState = SCRIPT_PAUSING;
                break;
            case SCRIPT_PAUSING:
                scriptState = SCRIPT_RUNNING;
                break;
            case SCRIPT_PAUSED:
                scriptState = SCRIPT_RUNNING;
                break;
            case SCRIPT_RELOADING:
                break;
            case SCRIPT_RELOADED:
                scriptState = SCRIPT_RUNNING;
                break;
            case SCRIPT_CLEARING:
                break;
            case SCRIPT_CLEARED:
                scriptState = SCRIPT_RUNNING;
                break;
            default:
                break;
        }
        if( scriptThread != null )
            scriptThread.interrupt();
        
    }//GEN-LAST:event_btnPlayPauseScriptActionPerformed

    private void txtScriptTask5Param1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtScriptTask5Param1MouseClicked
        if( scriptVector.size() < 5 ) return;
        ScriptTask task = null;
        try{task=(ScriptTask)scriptVector.elementAt( 4 );}catch(Exception e){}
        if( task == null ) return;
        if( task.isUploadTask() || task.isExtendedUploadTask() ) {
            FileDialog fileDialog=new FileDialog(this,"Task File...",FileDialog.LOAD);
            fileDialog.setDirectory( uploadTasksDir );
            fileDialog.show ();
            if( fileDialog.getFile() == null ) return;
            uploadTasksDir = fileDialog.getDirectory();
            String fileName = fileDialog.getDirectory() + fileDialog.getFile();
            task.setFileName( fileName );
            txtScriptTask5Param1.setText( fileName );
        }
    }//GEN-LAST:event_txtScriptTask5Param1MouseClicked

    private void txtScriptTask4Param1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtScriptTask4Param1MouseClicked
        if( scriptVector.size() < 4 ) return;
        ScriptTask task = null;
        try{task=(ScriptTask)scriptVector.elementAt( 3 );}catch(Exception e){}
        if( task == null ) return;
        if( task.isUploadTask() || task.isExtendedUploadTask() ) {
            FileDialog fileDialog=new FileDialog(this,"Task File...",FileDialog.LOAD);
            fileDialog.setDirectory( uploadTasksDir );
            fileDialog.show ();
            if( fileDialog.getFile() == null ) return;
            uploadTasksDir = fileDialog.getDirectory();
            String fileName = fileDialog.getDirectory() + fileDialog.getFile();
            task.setFileName( fileName );
            txtScriptTask4Param1.setText( fileName );
        }
    }//GEN-LAST:event_txtScriptTask4Param1MouseClicked

    private void txtScriptTask3Param1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtScriptTask3Param1MouseClicked
        if( scriptVector.size() < 3 ) return;
        ScriptTask task = null;
        try{task=(ScriptTask)scriptVector.elementAt( 2 );}catch(Exception e){}
        if( task == null ) return;
        if( task.isUploadTask() || task.isExtendedUploadTask() ) {
            FileDialog fileDialog=new FileDialog(this,"Task File...",FileDialog.LOAD);
            fileDialog.setDirectory( uploadTasksDir );
            fileDialog.show ();
            if( fileDialog.getFile() == null ) return;
            uploadTasksDir = fileDialog.getDirectory();
            String fileName = fileDialog.getDirectory() + fileDialog.getFile();
            task.setFileName( fileName );
            txtScriptTask3Param1.setText( fileName );
        }
    }//GEN-LAST:event_txtScriptTask3Param1MouseClicked

    private void txtScriptTask2Param1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtScriptTask2Param1MouseClicked
        if( scriptVector.size() < 2 ) return;
        ScriptTask task = null;
        try{task=(ScriptTask)scriptVector.elementAt( 1 );}catch(Exception e){}
        if( task == null ) return;
        if( task.isUploadTask() || task.isExtendedUploadTask() ) {
            FileDialog fileDialog=new FileDialog(this,"Task File...",FileDialog.LOAD);
            fileDialog.setDirectory( uploadTasksDir );
            fileDialog.show ();
            if( fileDialog.getFile() == null ) return;
            uploadTasksDir = fileDialog.getDirectory();
            String fileName = fileDialog.getDirectory() + fileDialog.getFile();
            task.setFileName( fileName );
            txtScriptTask2Param1.setText( fileName );
        }
    }//GEN-LAST:event_txtScriptTask2Param1MouseClicked

    private void txtScriptTask1Param1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtScriptTask1Param1MouseClicked
        if( scriptVector.size() < 1 ) return;
        ScriptTask task = null;
        try{task=(ScriptTask)scriptVector.elementAt( 0 );}catch(Exception e){}
        if( task == null ) return;
        if( task.isUploadTask() || task.isExtendedUploadTask() ) {
            FileDialog fileDialog=new FileDialog(this,"Task File...",FileDialog.LOAD);
            fileDialog.setDirectory( uploadTasksDir );
            fileDialog.show ();
            if( fileDialog.getFile() == null ) return;
            uploadTasksDir = fileDialog.getDirectory();
            String fileName = fileDialog.getDirectory() + fileDialog.getFile();
            task.setFileName( fileName );
            txtScriptTask1Param1.setText( fileName );
        }
    }//GEN-LAST:event_txtScriptTask1Param1MouseClicked

    private void btnResetScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetScriptActionPerformed
        int res = JOptionPane.showOptionDialog( this,
                                                "Clear Current Script?",
                                                "RESET",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE,
                                                null,
                                                null,
                                                null );
        scriptState = SCRIPT_CLEARING;
        if( res == JOptionPane.YES_OPTION ) {
            scriptVector.removeAllElements();
        } else {
            for( int i=0; i<scriptVector.size(); i++ ) {
                ScriptTask task = null;
                try{
                    task=(ScriptTask)scriptVector.elementAt(i);
                } catch( Exception e ) {
                    log.error("Unable to reset task status");
                }
                task.setStarted( false );
                task.setCompleted( false );
                task = null;
            }
        }
        scriptStep = 0;
        updateScriptPanel();
        scriptState = SCRIPT_CLEARED;
        if( scriptThread != null )
            scriptThread.interrupt();
        
    }//GEN-LAST:event_btnResetScriptActionPerformed

    private void listScriptTask5CommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listScriptTask5CommandActionPerformed
        if( scriptVector == null ) return;
        ScriptTask task = null;        
        if(scriptVector.size()<5) {
            /*--------------------------------*/
            /* Task does not exist, create it */
            /*--------------------------------*/
            task = new ScriptTask();
            task.setAsSelected( listScriptTask5Command );
            scriptVector.addElement( (ScriptTask) task );
        } else {
            /*--------------------------*/
            /* Task exists, retrieve it */
            /*--------------------------*/
            task = (ScriptTask)scriptVector.elementAt(4);
            task.setAsSelected( listScriptTask5Command );
        }

    }//GEN-LAST:event_listScriptTask5CommandActionPerformed

    private void listScriptTask4CommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listScriptTask4CommandActionPerformed
        if( scriptVector == null ) return;
        ScriptTask task = null;
        if(scriptVector.size()<4) {
            /*--------------------------------*/
            /* Task does not exist, create it */
            /*--------------------------------*/
            task = new ScriptTask();            
            task.setAsSelected( listScriptTask4Command );
            scriptVector.addElement( (ScriptTask) task );
        } else {
            /*--------------------------*/
            /* Task exists, retrieve it */
            /*--------------------------*/
            task = (ScriptTask)scriptVector.elementAt(3);
            task.setAsSelected( listScriptTask4Command );            
        }
    }//GEN-LAST:event_listScriptTask4CommandActionPerformed

    private void listScriptTask3CommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listScriptTask3CommandActionPerformed
        if( scriptVector == null ) return;
        ScriptTask task = null;
        if(scriptVector.size()<3) {
            /*--------------------------------*/
            /* Task does not exist, create it */
            /*--------------------------------*/
            task = new ScriptTask();            
            task.setAsSelected( listScriptTask3Command );
            scriptVector.addElement( (ScriptTask) task );            
        } else {
            /*--------------------------*/
            /* Task exists, retrieve it */
            /*--------------------------*/
            task = (ScriptTask)scriptVector.elementAt(2);
            task.setAsSelected( listScriptTask3Command );
        }
    }//GEN-LAST:event_listScriptTask3CommandActionPerformed

    private void listScriptTask2CommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listScriptTask2CommandActionPerformed
        if( scriptVector == null ) return;
        ScriptTask task = null;        
        if(scriptVector.size()<2) {
            /*--------------------------------*/
            /* Task does not exist, create it */
            /*--------------------------------*/
            System.out.println("Task2 list action add new");
            task = new ScriptTask();            
            task.setAsSelected( listScriptTask2Command );
            scriptVector.addElement( (ScriptTask) task );            
        } else {
            /*--------------------------*/
            /* Task exists, retrieve it */
            /*--------------------------*/
            System.out.println("Task2 list action");
            task = (ScriptTask)scriptVector.elementAt(1);
            task.setAsSelected( listScriptTask2Command );
        }
    }//GEN-LAST:event_listScriptTask2CommandActionPerformed

    private void listScriptTask1CommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listScriptTask1CommandActionPerformed
        if( scriptVector == null ) return;
        ScriptTask task = null;
        if(scriptVector.size()<1) {
            /*--------------------------------*/
            /* Task does not exist, create it */
            /*--------------------------------*/
            task = new ScriptTask();            
            task.setAsSelected( listScriptTask1Command );
            scriptVector.addElement( (ScriptTask) task );
        } else {
            /*--------------------------*/
            /* Task exists, retrieve it */
            /*--------------------------*/
            task = (ScriptTask)scriptVector.elementAt(0);
            task.setAsSelected( listScriptTask1Command );
        }
    }//GEN-LAST:event_listScriptTask1CommandActionPerformed

    /*------------------------------------------------------------------------*/
    /** Used to load a script file                                            */
    /*------------------------------------------------------------------------*/
    private void btnLoadScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadScriptActionPerformed
    /*------------------------------------------------------------------------*/
        String fileName = (String)listScripts.getSelectedItem();
        fileName = baseDir + fileName;
        log.info("Loading script from file "+fileName);
        scriptState = SCRIPT_RELOADING;
        scriptVector.clear();        
        try {            
            FileReader fr = new FileReader( fileName );
            StreamTokenizer fi = new StreamTokenizer( fr );
            fi.eolIsSignificant(true);
            fi.slashSlashComments(true);
            fi.slashStarComments(true);
            fi.parseNumbers();
            READING_SCRIPT_FILE:
            while( true ) {
                if( fi.nextToken() == fi.TT_EOF ) break READING_SCRIPT_FILE;
                if( (fi.ttype==fi.TT_EOL) || (fi.sval==null) ) continue;
                System.out.println("Token: "+fi.sval);
                //..............................................................
                if(fi.sval.trim().equals(ScriptTask.getWaitForNSecondsToken())){
                //..............................................................                        
                    if( fi.nextToken() == fi.TT_EOF ) break READING_SCRIPT_FILE;
                    ScriptTask sTask = new ScriptTask();
                    sTask.setWaitForNSeconds( (int)fi.nval );
                    scriptVector.addElement( (ScriptTask)sTask );
                    System.out.println("Waiting for "+sTask.getParam()+" seconds");
                //..............................................................    
                }else if(fi.sval.trim().equals(ScriptTask.getWaitForIncomingDataToken())){
                //..............................................................
                    ScriptTask sTask = new ScriptTask();
                    sTask.setWaitForIncomingData();
                    scriptVector.addElement( (ScriptTask)sTask );
                    System.out.println("Waiting for data");
                //..............................................................    
                }else if(fi.sval.trim().equals(ScriptTask.getChangeSSIDToken())){
                //..............................................................                        
                    if( fi.nextToken() == fi.TT_EOF ) break READING_SCRIPT_FILE;
                    ScriptTask sTask = new ScriptTask();
                    sTask.setChangeSSID( fi.sval );
                    scriptVector.addElement( (ScriptTask)sTask );
                    System.out.println("Change SSID to "+sTask.getParam());
                //..............................................................    
                }else if(fi.sval.trim().equals(ScriptTask.getUploadTaskToken())){
                //..............................................................
                    if( fi.nextToken() == fi.TT_EOF ) break READING_SCRIPT_FILE;
                    ScriptTask sTask = new ScriptTask();
                    sTask.setUploadTask( fi.sval );
                    if( fi.nextToken() == fi.TT_EOF ) break READING_SCRIPT_FILE;
                    if( fi.ttype != fi.TT_EOL )
                        sTask.setArgument( fi.sval );
                    scriptVector.addElement( (ScriptTask)sTask );
                    System.out.println("Upload task");
                //..............................................................
                }else if(fi.sval.trim().equals(ScriptTask.getExtendedUploadTaskToken())){
                //..............................................................
                    if( fi.nextToken() == fi.TT_EOF ) break READING_SCRIPT_FILE;
                    ScriptTask sTask = new ScriptTask();
                    sTask.setExtendedUploadTask( fi.sval );
                    if( fi.nextToken() == fi.TT_EOF ) break READING_SCRIPT_FILE;
                    if( fi.ttype != fi.TT_EOL )
                        sTask.setArgument( fi.sval );
                    scriptVector.addElement( (ScriptTask)sTask );
                    System.out.println("Extended upload task");
                //..............................................................
                }else if(fi.sval.trim().equals(ScriptTask.getStartHousekeepingToken())){
                //..............................................................
                    ScriptTask sTask = new ScriptTask();
                    sTask.setStartHousekeeping();
                    scriptVector.addElement( (ScriptTask)sTask );
                    System.out.println("Start housekeeping");
                //..............................................................
                }else if(fi.sval.trim().equals(ScriptTask.getExecuteUploadedTasksToken())){
                //..............................................................
                    ScriptTask sTask = new ScriptTask();
                    sTask.setExecuteUploadedTasks();
                    scriptVector.addElement( (ScriptTask)sTask );
                    System.out.println("Executed loaded tasks");
                } //else {
                    //System.out.println("Token type: "+fi.ttype);
                //}
            }
            //System.out.println("Completed reading script file normally");
            fr.close();
        } catch( FileNotFoundException e ) {
            log.warning("Script file load error: "+e);
        } catch( IOException e ) {
            log.error("Script file load error: "+e);
        }
        
        System.out.println("Script panel being updated");
        updateScriptPanel();

        scriptState = SCRIPT_RELOADED;
    }//GEN-LAST:event_btnLoadScriptActionPerformed

    /*------------------------------------------------------------------------*/
    /** Saves the current script in a file.                                   */
    /*------------------------------------------------------------------------*/
    private void btnSaveAsScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveAsScriptActionPerformed
    /*------------------------------------------------------------------------*/
        
        /*------------------------------*/
        /* Ask for the name of the file */
        /*------------------------------*/
        FileDialog fileDialog=new FileDialog(this,"Save As...",FileDialog.SAVE);
        fileDialog.setDirectory( scriptsDir );
        fileDialog.show ();
        if (fileDialog.getFile () == null) return;
        scriptsDir = fileDialog.getDirectory();
        String fileName = fileDialog.getDirectory() + fileDialog.getFile();
        
        /*--------------------------------*/
        /* Delete the content of the file */
        /*--------------------------------*/
        File f = new File( fileName );
        if( f == null ) return;
        f.delete();
        
        /*---------------------------------------------------------------*/
        /* Open the file and save each script entry in the proper format */
        /*---------------------------------------------------------------*/        
        FileWriter fw = null;
        try{
            fw = new FileWriter( f );
            for( int i=0; i<scriptVector.size(); i++ ) {
                ScriptTask task = (ScriptTask)scriptVector.elementAt(i);
                if( task == null ) continue;
                fw.write( task.toFileEntry() );
            }
        } catch( Exception e ) {
            log.warning("Script save problems: "+e);
        } finally {
            try{fw.close();}catch(IOException ioe){}
        }
        
    }//GEN-LAST:event_btnSaveAsScriptActionPerformed

    private void txtSpacecraftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSpacecraftActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_txtSpacecraftActionPerformed

    private void listScriptsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listScriptsActionPerformed

//        /*-----------------------*/
//        /* Get file to upload... */
//        /*-----------------------*/
//        String uploadScript = 
//                        (ScriptTask)listScripts.getSelectedItem();        
//        fileName = uploadScript.getFullFilename();
//        
//        int currentTaskIndex = 0;
//        for( currentTaskIndex = 0; currentTaskIndex < MAX_TASK_UPLOAD; currentTaskIndex++ ) {
//            tblUpload.setValueAt( "", currentTaskIndex, 0 );
//            tblUpload.setValueAt( "", currentTaskIndex, 1 );
//            tblUpload.setValueAt( "", currentTaskIndex, 2 );
//            tblUpload.setValueAt( "", currentTaskIndex, 3 );
//        }
//        
//        if( fileName == null ) return;
//        
//        FileInputStream fis = null;
//        DataInputStream dis = null;
//        String lineRead     = null;
//        try{
//            fis = new FileInputStream( fileName );
//            dis = new DataInputStream( fis );            
//            currentTaskIndex = 0;
//            while( (dis.available()>0) ) {
//                lineRead = dis.readLine();
//                tblUpload.setValueAt( lineRead.trim(), currentTaskIndex, 0 );
//                lineRead = dis.readLine();
//                tblUpload.setValueAt( lineRead.trim(), currentTaskIndex, 1 );
//                currentTaskIndex++;
//            }
//        } catch( IOException ioe ) {
//            System.out.println("Error reading upload task list file: "+ioe);
//        } 
//        try {
//            dis.close();
//            fis.close();            
//        } catch( IOException ioe ){}

    }//GEN-LAST:event_listScriptsActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        enableNetworkConnectionToServer();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jLabel155MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel155MouseClicked
        System.out.print("Load: "+qbl.cmdLoad);
        System.out.print(" Dump: "+qbl.cmdDumpMem);
        System.out.print(" Exec: "+qbl.cmdExecute);
        System.out.print(" MemE: "+qbl.cmdPeekMem);
        System.out.print(" MemW: "+qbl.cmdPokeMem);
        System.out.print(" IOEx: "+qbl.cmdPeekIO);
        System.out.print(" IOPo: "+qbl.cmdPokeIO);
        System.out.print(" Tlmy: "+qbl.cmdBeacon);
        System.out.print(" Move: "+qbl.cmdMove);
        System.out.print(" Cmd1: "+qbl.cmd1);
        System.out.println(" Cmd2: "+qbl.cmd2);
    }//GEN-LAST:event_jLabel155MouseClicked

    private void btnPausePassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPausePassActionPerformed
        if( btnPausePass.isSelected() ) {
            System.out.println("Pass PAUSED");
            isPassPaused = true;
        }
        else {
            System.out.println("Pass un-paused");
            isPassPaused = false;
        }
    }//GEN-LAST:event_btnPausePassActionPerformed

    private void btnIgnorePassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIgnorePassActionPerformed
        if( btnIgnorePass.isSelected() ) {
            System.out.println("Pass Ignored");
            isPassIgnored = true;
        }
        else {
            System.out.println("Pass NOT ignored");
            isPassIgnored = false;
        }
    }//GEN-LAST:event_btnIgnorePassActionPerformed

    private void chkDynamicPacketSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDynamicPacketSizeActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_chkDynamicPacketSizeActionPerformed

    private void radUseStandardLoadProtoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radUseStandardLoadProtoActionPerformed
        System.out.println("Setting load proto to standard");
        isExtendedLoadProto = false;
    }//GEN-LAST:event_radUseStandardLoadProtoActionPerformed

    private void radUseExtendedLoadProtoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radUseExtendedLoadProtoActionPerformed
        System.out.println("Setting load proto to extended");
        isExtendedLoadProto = true;
    }//GEN-LAST:event_radUseExtendedLoadProtoActionPerformed

    private void btnPingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPingActionPerformed
        SatControl ctrl = panelSatellites.getSelectedSatellite().getControl();
        if( (ctrl==null) || (srvLnk==null) ) return;
        srvLnk.sendPacketToSpacecraft(ctrl.messageToRequestBeacon());
        //phtLink.sendPacket( PHT.dataPacket( PHT.cmdRequestAcknowledgement ) );
    }//GEN-LAST:event_btnPingActionPerformed

    private void listSSIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listSSIDActionPerformed
        System.out.println(listSSID.getSelectedItem().toString());
        if( listSSID.getSelectedItem().toString().equals("1") ) {
            System.out.println("1 selected");
            //phtLink.setDestinationSSID( 0x01 );//to eliminate eventually
            currentSat = panelSatellites.getSelectedSatellite();
            currentSat.setControlSSID( 0x01 );
        }
        else {
            System.out.println("8 selected");
            //phtLink.setDestinationSSID( 0x08 );//to eliminate eventually
            currentSat = panelSatellites.getSelectedSatellite();
            currentSat.setControlSSID( 0x08 );
        }
    }//GEN-LAST:event_listSSIDActionPerformed

    private void listStationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listStationsActionPerformed
        //enableNetworkConnectionToServer();
        panelTracking.setGroundRefCoordinates( 
                        (GroundStation)listStations.getSelectedItem() );
    }//GEN-LAST:event_listStationsActionPerformed
    
    private void txtDataDirectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDataDirectionActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_txtDataDirectionActionPerformed
    
    private void chkShowTLMFramesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowTLMFramesActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_chkShowTLMFramesActionPerformed
            
  private void menuShowSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuShowSettingsActionPerformed
      CardLayout cl = (CardLayout)(panelInfo.getLayout());
      cl.show( panelInfo,"settings" );
  }//GEN-LAST:event_menuShowSettingsActionPerformed
    
  private void menuShowTelemetryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuShowTelemetryActionPerformed
      CardLayout cl = (CardLayout)(panelInfo.getLayout());
      cl.show( panelInfo,"telemetry" );
  }//GEN-LAST:event_menuShowTelemetryActionPerformed
  
  
  private void btnQBLIOWriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQBLIOWriteActionPerformed
      SatControl ctrl = panelSatellites.getSelectedSatellite().getControl();
      if( (ctrl == null) || (srvLnk == null) ) return;
      int port = 0;
      int value = 0;
      try{ 
        port = Integer.parseInt(txtQBLIOWritePort.getText(),16);
        value = Integer.parseInt(txtQBLIOWriteValue.getText(), 16);
        srvLnk.sendPacketToSpacecraft(ctrl.BLmessageToWriteIOPort(port,value));
      } catch( NumberFormatException e ){}

  }//GEN-LAST:event_btnQBLIOWriteActionPerformed
  
  private void btnQBLIOReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQBLIOReadActionPerformed
      SatControl ctrl = panelSatellites.getSelectedSatellite().getControl();
      if( (ctrl == null) || (srvLnk == null) ) return;
      int port = 0;
      int value = 0;
      try{ 
        port = Integer.parseInt(txtQBLIOReadPort.getText(),16);
        srvLnk.sendPacketToSpacecraft(ctrl.BLmessageToReadIOPort(port));
      } catch( NumberFormatException e ){}

  }//GEN-LAST:event_btnQBLIOReadActionPerformed
  
  private void btnQBLMemWriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQBLMemWriteActionPerformed
   SatControl ctrl = panelSatellites.getSelectedSatellite().getControl();
   if( (ctrl == null) || (srvLnk == null) ) return;
   int seg = 0;
   int off = 0;
   int val = 0;
   try{ 
     seg = Integer.parseInt(txtQBLMemWriteSegment.getText(),16);
     off = Integer.parseInt(txtQBLMemWriteOffset.getText(), 16);
     val = Integer.parseInt(txtQBLMemWriteValue.getText(), 16);
     srvLnk.sendPacketToSpacecraft(ctrl.BLmessageToWriteMemory(seg,off,val));
   } catch( NumberFormatException e ){}
      
  }//GEN-LAST:event_btnQBLMemWriteActionPerformed
  
  private void btnQBLMemReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQBLMemReadActionPerformed
   SatControl ctrl = panelSatellites.getSelectedSatellite().getControl();
   if( (ctrl == null) || (srvLnk == null) ) return;
   int seg = 0;
   int off = 0;
   int val = 0;
   try{ 
     seg = Integer.parseInt(txtQBLMemReadSegment.getText(),16);
     off = Integer.parseInt(txtQBLMemReadOffset.getText(), 16);
     srvLnk.sendPacketToSpacecraft(ctrl.BLmessageToReadMemory(seg,off));
   } catch( NumberFormatException e ){}
  }//GEN-LAST:event_btnQBLMemReadActionPerformed
  
  private void btnQBLMemDumpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQBLMemDumpActionPerformed
   SatControl ctrl = panelSatellites.getSelectedSatellite().getControl();
   if( (ctrl == null) || (srvLnk == null) ) return;
   int seg = 0;
   int off = 0;
   int nb = 0;
   try{ 
     seg = Integer.parseInt(txtQBLMemDumpSegment.getText(),16);
     off = Integer.parseInt(txtQBLMemDumpOffset.getText(), 16);
     nb  = Integer.parseInt(txtQBLMemDumpNbBytes.getText(),10);
     srvLnk.sendPacketToSpacecraft(ctrl.BLmessageToDumpMemory(seg,off,nb));
   } catch( NumberFormatException e ){}

  }//GEN-LAST:event_btnQBLMemDumpActionPerformed
  
  private void menuShowMessagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuShowMessagesActionPerformed
      CardLayout cl = (CardLayout)(panelInfo.getLayout());
      cl.show( panelInfo,"messages" );
  }//GEN-LAST:event_menuShowMessagesActionPerformed
  
  private void menuShowLogbookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuShowLogbookActionPerformed
      panelLogBook.txtLogbook.setCaretPosition(panelLogBook.txtLogbook.getText().length());
      panelLogBook.txtLogbook.grabFocus();
      panelLogBook.txtLogbook.revalidate();
      CardLayout cl = (CardLayout)(panelInfo.getLayout());
      cl.show( panelInfo,"logbook" );
      panelLogBook.txtLogbook.setCaretPosition(panelLogBook.txtLogbook.getText().length());
      panelLogBook.txtLogbook.grabFocus();
      panelLogBook.txtLogbook.revalidate();
  }//GEN-LAST:event_menuShowLogbookActionPerformed
  
  private void radPHTCommandsBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radPHTCommandsBackupActionPerformed
      
      //pht.setCommandPrime( false );
      
    /*-------------------------------*/
    /* ACS module activation command */
    /*-------------------------------*/
      //panelACS_cmd.lblACSActivationMode.setText("Backup Commands");
      //panelACS_cmd.btnACSON.setBackground(java.awt.Color.yellow);
      //panelACS_cmd.btnACSOFF.setBackground(java.awt.Color.yellow);
      
    /*-----------------------*/
    /* Payload power command */
    /*-----------------------*/
      //panelPayload_cmd.lblPayloadCommandMode.setText("Backup Commands");
      //panelPayload_cmd.btnPayloadON.setBackground(java.awt.Color.yellow);
      //panelPayload_cmd.btnPayloadOFF.setBackground(java.awt.Color.yellow);
      
    /*------------------*/
    /* Heaters commands */
    /*------------------*/
      //panelHeaters_cmd.panHtrSurvPower.setBorder(new javax.swing.border.TitledBorder("Power - Backup Command"));
      //panelHeaters_cmd.btnHtrSurvON.setBackground(java.awt.Color.yellow);
      //panelHeaters_cmd.btnHtrSurvOFF.setBackground(java.awt.Color.yellow);
      //panelHeaters_cmd.panHtrOpPower.setBorder(new javax.swing.border.TitledBorder("Power - Backup Command"));
      //panelHeaters_cmd.btnHtrOpON.setBackground(java.awt.Color.yellow);
      //panelHeaters_cmd.btnHtrOpOFF.setBackground(java.awt.Color.yellow);
      
    /*----------------*/
    /* Pyros commands */
    /*----------------*/
      //panelPyros_cmd.panPyrosBank.setBorder(new javax.swing.border.TitledBorder("Power - Backup Commands"));
      //panelPyros_cmd.btnPyrosON.setBackground(java.awt.Color.yellow);
      //panelPyros_cmd.btnPyrosOFF.setBackground(java.awt.Color.yellow);
      
    /*-----------------------*/
    /* Solar panels commands */
    /*-----------------------*/
      //panelPower_cmd.lblPanelsCommandMode.setText("Backup Commands");
      //panelPower_cmd.btnPanel1Normal.setBackground(java.awt.Color.yellow);
      //panelPower_cmd.btnPanel1Shutdown.setBackground(java.awt.Color.yellow);
      //panelPower_cmd.btnPanel2Normal.setBackground(java.awt.Color.yellow);
      //panelPower_cmd.btnPanel2Shutdown.setBackground(java.awt.Color.yellow);
      
  }//GEN-LAST:event_radPHTCommandsBackupActionPerformed
  
  private void radPHTCommandsPrimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radPHTCommandsPrimeActionPerformed
      
      //pht.setCommandPrime( true );
      
    /*-------------------------------*/
    /* ACS module activation command */
    /*-------------------------------*/
      //panelACS_cmd.lblACSActivationMode.setText("Prime Commands");
      //panelACS_cmd.btnACSON.setBackground( new java.awt.Color(204,204,204));
      //panelACS_cmd.btnACSOFF.setBackground(new java.awt.Color(204,204,204));
      
    /*-----------------------*/
    /* Payload power command */
    /*-----------------------*/
      //panelPayload_cmd.lblPayloadCommandMode.setText("Prime Commands");
      //panelPayload_cmd.btnPayloadON.setBackground( new java.awt.Color(204,204,204));
      //panelPayload_cmd.btnPayloadOFF.setBackground(new java.awt.Color(204,204,204));
      
    /*------------------*/
    /* Heaters commands */
    /*------------------*/
      //panelHeaters_cmd.panHtrSurvPower.setBorder(new javax.swing.border.TitledBorder("Power - Primary Commands"));
      //panelHeaters_cmd.btnHtrSurvON.setBackground(new java.awt.Color(204,204,204));
      //panelHeaters_cmd.btnHtrSurvOFF.setBackground(new java.awt.Color(204,204,204));
      //panHtrOpPower.setBorder(new javax.swing.border.TitledBorder("Power - Primary Commands"));
      //panelHeaters_cmd.btnHtrOpON.setBackground(new java.awt.Color(204,204,204));
      //panelHeaters_cmd.btnHtrOpOFF.setBackground(new java.awt.Color(204,204,204));
      
    /*----------------*/
    /* Pyros commands */
    /*----------------*/
      //panelPyros_cmd.panPyrosBank.setBorder(new javax.swing.border.TitledBorder("Power - Primary Commands"));
      //panelPyros_cmd.btnPyrosON.setBackground(new java.awt.Color(204,204,204));
      //panelPyros_cmd.btnPyrosOFF.setBackground(new java.awt.Color(204,204,204));
      
    /*-----------------------*/
    /* Solar panels commands */
    /*-----------------------*/
      //panelPower_cmd.lblPanelsCommandMode.setText("Prime Commands");
      //panelPower_cmd.btnPanel1Normal.setBackground(new java.awt.Color(204,204,204));
      //panelPower_cmd.btnPanel1Shutdown.setBackground(new java.awt.Color(204,204,204));
      //panelPower_cmd.btnPanel2Normal.setBackground(new java.awt.Color(204,204,204));
      //panelPower_cmd.btnPanel2Shutdown.setBackground(new java.awt.Color(204,204,204));
      
  }//GEN-LAST:event_radPHTCommandsPrimeActionPerformed
  
  private void chkPHTVerboseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkPHTVerboseMenuItemActionPerformed
//      if( chkPHTVerboseMenuItem.isSelected() ) {
//          if( pht != null )
//              pht.verbose = true;
//      } else {
//          if( pht != null )
//              pht.verbose = false;
//      }
  }//GEN-LAST:event_chkPHTVerboseMenuItemActionPerformed
  
  private void btnFSDeleteFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFSDeleteFileActionPerformed
      
//      String fileName = JOptionPane.showInputDialog("File to DELETE:");
//      if( fileName == null ) return;
//      
//      /*-----------------------*/
//      /* Create the data frame */
//      /*-----------------------*/
//      byte[] ldrFrame = new byte[13];
//      ldrFrame[0] = 0;
//      ldrFrame[1] = (byte)12;    //Size of packet
//      //without leading 0
//      ldrFrame[2] = 'E';            //code for ERASE
//      ldrFrame[3] = 0;              //File block counter
//      for( int i=0; i<8; i++ ) {
//          if( i < fileName.length() )
//              ldrFrame[i+4] = (byte) fileName.charAt( i );
//          else
//              ldrFrame[i+4] = (byte) 0;
//      }
//      ldrFrame[12] = phtLink.calculateChecksum( ldrFrame, 12 );
//      
//      /*---------------------------------*/
//      /* Create the AX-25 and KISS frame */
//      /*---------------------------------*/
//      kissFrame = new KISSFrame( (byte)0x00, (byte)0x00,
//      new AX25UIFrame( "COMND", (byte)0x01,
//      "TSTFS",   (byte)0x08,
//      (byte)0x79,
//      (byte[])ldrFrame ).getBytes()
//      );
//      
//      //---------
//      // Send it
//      //---------
//      phtLink.sendRaw( kissFrame.getBytes() );
//      statusTXCount++;
      
  }//GEN-LAST:event_btnFSDeleteFileActionPerformed
  
  private void btnTNCDisableKISSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTNCDisableKISSActionPerformed
        }//GEN-LAST:event_btnTNCDisableKISSActionPerformed
        
  private void btnTNCInitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTNCInitActionPerformed
        }//GEN-LAST:event_btnTNCInitActionPerformed
        
  private void btnFSAbortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFSAbortActionPerformed
//      /*-----------------------*/
//      /* Create the data frame */
//      /*-----------------------*/
//      byte[] ldrFrame = new byte[13];
//      ldrFrame[0] = 0;              //I do not know why this byte is here
//      ldrFrame[1] = (byte)12;       //Size of packet
//      //without leading 0
//      ldrFrame[2] = 'X';            //code for ABORT
//      ldrFrame[3] = 0;              //File block counter
//      for( int i=0; i<8; i++ ) {
//          ldrFrame[i+4] = (byte) 0;
//      }
//      ldrFrame[12] = phtLink.calculateChecksum( ldrFrame, 12 );
//      
//      /*---------------------------------*/
//      /* Create the AX-25 and KISS frame */
//      /*---------------------------------*/
//      kissFrame = new KISSFrame( (byte)0x00, (byte)0x00,
//      new AX25UIFrame( "COMND", (byte)0x01,
//      "TSTFS",   (byte)0x08,
//      (byte)0x79,
//      (byte[])ldrFrame ).getBytes()
//      );
//      
//      //---------
//      // Send it
//      //---------
//      phtLink.sendRaw( kissFrame.getBytes() );
//      statusTXCount++;
      
  }//GEN-LAST:event_btnFSAbortActionPerformed
  
  private void btnFSTestFillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFSTestFillActionPerformed
      
//      /*-----------------------*/
//      /* Create the data frame */
//      /*-----------------------*/
//      byte[] ldrFrame = new byte[13];
//      ldrFrame[0] = 0;              //I do not know why this byte is here
//      ldrFrame[1] = (byte)12;       //Size of packet
//      //without leading 0
//      if( btnFSTestFill.getText().equals("   Test Fill   ") ) {
//          ldrFrame[2] = 'T';          //code for TEST FILL
//          btnFSTestFill.setText("Abort Test Fill");
//      }
//      else {
//          ldrFrame[2] = 't';          //code for STOP TEST FILL
//          btnFSTestFill.setText("   Test Fill   ");
//      }
//      ldrFrame[3] = 0;              //File block counter
//      for( int i=0; i<8; i++ ) {
//          ldrFrame[i+4] = (byte) 0;
//      }
//      ldrFrame[12] = phtLink.calculateChecksum( ldrFrame, 12 );
//      
//      /*---------------------------------*/
//      /* Create the AX-25 and KISS frame */
//      /*---------------------------------*/
//      kissFrame = new KISSFrame( (byte)0x00, (byte)0x00,
//      new AX25UIFrame( "COMND", (byte)0x01,
//      "TSTFS",   (byte)0x08,
//      (byte)0x79,
//      (byte[])ldrFrame ).getBytes()
//      );
//      
//      //---------
//      // Send it
//      //---------
//      phtLink.sendRaw( kissFrame.getBytes() );
//      statusTXCount++;
      
  }//GEN-LAST:event_btnFSTestFillActionPerformed
  
  private void btnReadFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadFileActionPerformed
      
//      String fileName = JOptionPane.showInputDialog("File Name:");
//      if( fileName == null ) return;
//      
//      /*-----------------------*/
//      /* Create the data frame */
//      /*-----------------------*/
//      byte[] ldrFrame = new byte[13];
//      ldrFrame[0] = 0;    //I do not know why this byte is here
//      ldrFrame[1] = (byte)12;    //Size of packet
//      //without leading 0
//      ldrFrame[2] = 'D';            //code for DOWNLOAD
//      ldrFrame[3] = 0;              //File block counter
//      for( int i=0; i<8; i++ ) {
//          if( i < fileName.length() )
//              ldrFrame[i+4] = (byte) fileName.charAt( i );
//          else
//              ldrFrame[i+4] = (byte) 0;
//      }
//      ldrFrame[12] = phtLink.calculateChecksum( ldrFrame, 12 );
//      
//      /*---------------------------------*/
//      /* Create the AX-25 and KISS frame */
//      /*---------------------------------*/
//      kissFrame = new KISSFrame( (byte)0x00, (byte)0x00,
//      new AX25UIFrame( "COMND", (byte)0x01,
//      "TSTFS",   (byte)0x08,
//      (byte)0x79,
//      (byte[])ldrFrame ).getBytes()
//      );
//      
//      //---------
//      // Send it
//      //---------
//      phtLink.sendRaw( kissFrame.getBytes() );
//      statusTXCount++;
      
  }//GEN-LAST:event_btnReadFileActionPerformed
  
  /*=====================================================================*/
  /* LOAD A FILE                                                         */
  /*=====================================================================*/
  private void btnLoadFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadFileActionPerformed
  /*=====================================================================*/
//      FileInputStream fis = null;
//      byte[] binaryImage = null;
//      UploadFile uploadFile = null;
//      
//      if( (uploadFile != null) && (uploadFile.uploading) ) {
//          uploadFile.abort();
//      }
//      else {
//          
//        /*--------------------------------------*/
//        /* Ask the user which file to upload... */
//        /*--------------------------------------*/
//          FileDialog fileDialog = new FileDialog (this, "Open...", FileDialog.LOAD);
//          fileDialog.setDirectory( "C:\\" );
//          fileDialog.show ();
//          if (fileDialog.getFile () == null)
//              return;
//          String fileName = fileDialog.getDirectory () + fileDialog.getFile ();
//          String shortFileName = fileDialog.getFile();
//          if( shortFileName.length() > 8 )
//              shortFileName = shortFileName.substring(0,8);
//          
//          try {
//              fis = new FileInputStream (fileName);
//              binaryImage = new byte[fis.available()];
//              fis.read(binaryImage);
//          }
//          catch( IOException ioe ) {
//              System.out.println("ERROR reading binary image "+ioe);
//          }
//          
//          uploadFile = new UploadFile( shortFileName, binaryImage );
//          Thread uploadFileThread = new Thread( uploadFile );
//          uploadFileThread.start();
//      }
//      
      
      
  }//GEN-LAST:event_btnLoadFileActionPerformed
  
  
  private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
      JOptionPane.showConfirmDialog(this,"QuickSat Ground Station - Executable V6, Copyrights Canadian Space Agency (JFC 2002)", "ABOUT", JOptionPane.OK_OPTION);
  }//GEN-LAST:event_aboutMenuItemActionPerformed
  
  
  private void btnGetKepsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetKepsActionPerformed
      
  }//GEN-LAST:event_btnGetKepsActionPerformed
  
  
  private void panelPHTMiscCommandsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelPHTMiscCommandsComponentShown
  }//GEN-LAST:event_panelPHTMiscCommandsComponentShown
  
  private void panelPHTCommandsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelPHTCommandsComponentShown
  }//GEN-LAST:event_panelPHTCommandsComponentShown
  
    
  private void slidPacketSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slidPacketSizeStateChanged
      
      dataChunkSize = slidPacketSize.getValue();
      txtDataSize.setText(""+dataChunkSize);
      
  }//GEN-LAST:event_slidPacketSizeStateChanged
  
  private void panelDiagnosticComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelDiagnosticComponentShown
  }//GEN-LAST:event_panelDiagnosticComponentShown
  
  private void panelScriptComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelScriptComponentShown
  }//GEN-LAST:event_panelScriptComponentShown
  
  private void panelBootLoaderComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelBootLoaderComponentShown
  }//GEN-LAST:event_panelBootLoaderComponentShown
      
  private void chkUseSCUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkUseSCUActionPerformed
  }//GEN-LAST:event_chkUseSCUActionPerformed
  
  private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
      
//        /*-------------------*/
//        /* Prepare the frame */
//        /*-------------------*/
//      String commandString = "F";
//      
//      //----------------
//      // Packetize data
//      //----------------
//      kissFrame = new KISSFrame( (byte)0x00, (byte)0x00,
//      new AX25UIFrame( "COMND", (byte)0x01,
//      "TSTCOM",   (byte)0x08,
//      (byte)0x00,
//      (byte[])commandString.getBytes() ).getBytes()
//      );
//      //---------
//      // Send it
//      //---------
//      try {
//          output.write( kissFrame.getBytes() );
//          statusTXCount++;
//      } catch( IOException ioe ) {
//          System.out.println( "Unable to send KISS frame: " + ioe.toString() );
//      }
  }//GEN-LAST:event_jButton2ActionPerformed
  
  private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
//      byte SSID = 1;
//      String commandString = "";
//      
//        /*-------------------*/
//        /* Prepare the frame */
//        /*-------------------*/
//      //if( panelSettings.isSSID1Selected() ) {
//      if( listSSID.getSelectedItem().toString().equals("1") ) {
//          SSID = 1;
//          commandString = "???########1#########2#########3#########4#########5#########6#########7#########8#########9#########";
//      }
//      else {
//          SSID = 8;
//          commandString = "EEE########1#########2#########3#########4#########5#########6#########7#########8#########9#########";
//      }
//      
//      //----------------
//      // Packetize data
//      //----------------
//      kissFrame = new KISSFrame( (byte)0x00, (byte)0x00,
//      new AX25UIFrame( "COMND", (byte)0x01,
//      panelSatellites.getSelectedSatellite().getControlCallsign(),   
//      (byte)SSID,
//      (byte)0x00,
//      (byte[])commandString.getBytes() ).getBytes()
//      );
//      //---------
//      // Send it
//      //---------
//      try {
//          output.write( kissFrame.getBytes() );
//          statusTXCount++;
//      } catch( IOException ioe ) {
//          System.out.println( "Unable to send KISS frame: " + ioe.toString() );
//      }
  }//GEN-LAST:event_jButton4ActionPerformed
      
  private void btnTxtSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTxtSaveActionPerformed
      // Add your handling code here:
      FileOutputStream fos = null;
      
      try {
          fos = new FileOutputStream (baseDir+"qsgs.txt");
          fos.write( txtInfo.getText().getBytes() );
          fos.flush();
          fos.close();
      }catch( IOException ioe ){}
      
  }//GEN-LAST:event_btnTxtSaveActionPerformed
  
  
  private void chkTxtFollowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTxtFollowActionPerformed
      // Add your handling code here:
      
  }//GEN-LAST:event_chkTxtFollowActionPerformed
  
  private void btnRun1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRun1ActionPerformed

      /*--------------------------------------------------------------*/
      /* First verify if we have a valid link with the ground station */
      /*--------------------------------------------------------------*/
      if( srvLnk == null ) {
          JOptionPane.showMessageDialog(this,"Server Link Not Established");
          return;
      }
      
      /*--------------------------------------------------------------*/
      /* Then create and send the message to execute tasks, depending */
      /* on the currently active satellite.                           */
      /*--------------------------------------------------------------*/
      Satellite sat = panelSatellites.getSelectedSatellite();
      if( listSSID.getSelectedItem().toString().equals("1") ) {
          /*----------------------------------------------------*/
          /* If SSID=1 it indicates we're talking to the loader */
          /* task, so we can only order an execution of the     */
          /* housekeeping task.                                 */
          /*----------------------------------------------------*/
          SatControl ctrl = sat.getControl();
          if( (ctrl != null) && (srvLnk != null) )
            srvLnk.sendPacketToSpacecraft(ctrl.messageToStartHousekeeping());
      } else {
          /*--------------------------------------------------------*/
          /* If SSID=8 then we're talking to the housekeeping task: */
          /* send the message to start all tasks just uploaded...   */
          /*--------------------------------------------------------*/
          SatControl ctrl = sat.getControl();
          if( ctrl != null && (srvLnk != null) )
            srvLnk.sendPacketToSpacecraft(ctrl.messageToStartLoadedTasks());
      }
      statusTXCount++;
      
  }//GEN-LAST:event_btnRun1ActionPerformed
  
  private void btnQBLExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQBLExecuteActionPerformed
      // Bootloader - Temporary - Executes Kernal and Pht1 only, but should eventually
      // execute any code segment.
      SatControl ctrl = panelSatellites.getSelectedSatellite().getControl();
      if( (ctrl != null) && (srvLnk != null) )
        srvLnk.sendPacketToSpacecraft(
                    ctrl.BLmessageToExecuteTask(0x0000,0x0000,0x0040,0x013f));
  }//GEN-LAST:event_btnQBLExecuteActionPerformed
  
  private void btnQBLBeaconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQBLBeaconActionPerformed
      // Bootloader - Commands the Bootloader to send a Beacon
      SatControl ctrl = panelSatellites.getSelectedSatellite().getControl();
      if( (ctrl != null) && (srvLnk != null) )
        srvLnk.sendPacketToSpacecraft(ctrl.BLmessageToRequestBeacon());      
  }//GEN-LAST:event_btnQBLBeaconActionPerformed
  
  private void btnQBLMoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQBLMoveActionPerformed
      // Bootloader - Moves the Kernal and Pht1 into RAM
      SatControl ctrl = panelSatellites.getSelectedSatellite().getControl();
      if( (ctrl != null) && (srvLnk != null) )
        srvLnk.sendPacketToSpacecraft(ctrl.BLmessageToMoveTasksInMemory());      
  }//GEN-LAST:event_btnQBLMoveActionPerformed
  
  
  /*=======================================================================*/
  private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
  /*=======================================================================*/
/* THIS FUNCTION STILL CRASHES THE S/C. UNTIL WE DEBUG THAT */
/* THERE IS NO POINT IMPLEMENTING IT HERE...                */
/*----------------------------------------------------------*/
      
        /*----------------------------*/
        /* Prepare the frame to RESET */
        /*----------------------------*/
      //byte[] ldrFrame = new byte[6];
      //ldrFrame[0] = 0;    //I do not know why this byte is here
      //ldrFrame[1] = 0;    //Size of data
      //ldrFrame[2] = 0;    //offset msb
      //ldrFrame[3] = 0;    //offset lsb
      //ldrFrame[4] = 100;   //code for RESET
      
      //------------------------
      // Calculate the checksum
      //------------------------
      //ldrFrame[5] = calculateChecksum( ldrFrame, 5 );
      
      //----------------
      // Packetize data
      //----------------
      //kissFrame = new KISSFrame( (byte)0x00, (byte)0x00,
      //                            new AX25UIFrame( "COMND", (byte)0x01,
      //                                             "1IKSAT",   (byte)0x01,
      //                                             (byte)0x79,
      //                                             (byte[])ldrFrame ).getBytes()
      //                            );
      
      //---------
      // Send it
      //---------
      //try {
      //    output.write( kissFrame.getBytes() );
      //} catch( IOException ioe ) {
      //    System.out.println( "Unable to send KISS frame: " + ioe.toString() );
      //}
      
      
        /*-------------------------------------------*/
        /* Set the flag to cancel the load procedure */
        /*-------------------------------------------*/
      cancelLoad = true;
      //if( loadThread != null )
      //        if( loadThread.isAlive() ) loadThread.resume();
      
      //loadProgressGUI.reset();
      //loadProgressGUI.update();
      
      
  }//GEN-LAST:event_btnResetActionPerformed
  
  /*======================================================================*/
  private void btnLoad1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoad1ActionPerformed
  /*======================================================================*/
      
    /*--------------------------------------*/
    /* Ask the user which file to upload... */
    /*--------------------------------------*/
      //FileDialog fileDialog = new FileDialog (this, "Open...", FileDialog.LOAD);
      //fileDialog.setDirectory( "C:\\pht-revc\\Phtqik\\Qikobj" );
      //fileDialog.show ();
      //if (fileDialog.getFile () == null)
      //    return;
      //fileName = fileDialog.getDirectory () + fileDialog.getFile ();
      //taskName = fileDialog.getFile();
      //int posOfPoint = taskName.indexOf('.');
      //if( posOfPoint > 0 )
      //    taskName = taskName.substring(0,posOfPoint);
      //System.out.println("Task name: "+taskName);
      
      //txtCommandLine.setText( JOptionPane.showInputDialog("Command Line") );
    /* 0 = OK, 2 = CANCEL */
      //int dRes = JOptionPane.showConfirmDialog(this,
      //                "WARNING: This command MUST be used ONLY to resume a previously aborted load, \nand you must select the EXACT same file or unpredictable results will occur.\nCONTINUE?",
      //                "RESUME LOAD REQUEST",
      //                JOptionPane.OK_CANCEL_OPTION );
      
      
      
      
    /*------------------------------*/
    /* Define thread to load a file */
    /*------------------------------*/
      cancelLoad = false;
//      loadThread = new Thread( this );
//      if( loadThread != null ) {
//          System.out.println("Starting load thread");
//          isResuming = false;
//          loadThread.start();
//      }
//      else System.out.println("ERROR: Load thread not available!");
      
      
  }//GEN-LAST:event_btnLoad1ActionPerformed
  
  /*=======================================================================*/  
  
  /*=======================================================================*/
  private void cutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutMenuItemActionPerformed
  /*=======================================================================*/
  }//GEN-LAST:event_cutMenuItemActionPerformed
  
  /*=======================================================================*/
    private void exitMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
  /*=======================================================================*/
        appRunning = false;
        try{Thread.currentThread().sleep(2000);}
        catch(InterruptedException ie){}
        System.exit (0);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
   /*=======================================================================*/
   /** Exit the Application                                                 */
   /*=======================================================================*/
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
   /*=======================================================================*/
        appRunning = false;
        try{Thread.currentThread().sleep(2000);}
        catch(InterruptedException ie){}
        System.exit (0);
    }//GEN-LAST:event_exitForm
    
    
    //=====================================================================main
    // @param args the command line arguments
    //=========================================================================
    public static void main (String args[]) {
        //=========================================================================
        
        new jload ().show ();
        
        
    }
    
    
    
    /*========================================================================*/
    /*                                                                        */
    /*                              T H R E A D S                             */
    /*                                                                        */
    /*========================================================================*/
    
    /*========================================================================*/
    /** Implements our threads: InitProgressThread started while the software
     *  is initializing (reporting progress), satTrackThread used to perio-
     *  dically perform tracking calculation and control the ground station
     *  accordingly, pacsatAcquisitionThread used to test PACSAT, 
     *  spacecraftCommThread, radioCommThread and antennaRotatorThread used
     *  to communicate with the spacecraft, radio and antenna rotator
     *  respectively.                                                         */
    /*========================================================================*/
    public void run() {
    /*========================================================================*/
        
        /*--------------------------------------------------------------------*/
        /*   I  N  I  T  I  A  L  I  Z  A  T  I  O  N    T H R E A D          */
        /*--------------------------------------------------------------------*/                
        if( Thread.currentThread() == initProgressThread ) {
            dialogIntro = new DialogAbout( this, true );//true for modal
            dialogIntro.centerScreen();
            dialogIntro.disableContinueButton();
            dialogIntro.setInfoTitle("Initializing...");
            dialogIntro.setVisible( true );//Blocks here...            
            System.out.println("Closed intro dialog");            
        }
        /*--------------------------------------------------------------------*/
        /* S  a  t  e  l  l  i  t  e     T  r  a  c  k  i  n  g    T H R E A D*/
        /*--------------------------------------------------------------------*/                
        else if( Thread.currentThread() == satTrackThread ) {
            while( appRunning ) {
                if( !isPassPaused ) {
                    
                    /*---------------------------*/
                    /* Get the current satellite */
                    /*---------------------------*/
                    Satellite sat = panelSatellites.getSelectedSatellite();
                    sat.setVerbose( false );
                    
                    /*-------------------------*/
                    /* Calculates its position */
                    /*-------------------------*/
                    sat.calculatePosition( 
                                (GroundStation)listStations.getSelectedItem(),
                               System.currentTimeMillis()+timeOffset );
                    if(sat!=null){
                    mapper.updateSat(sat);
                    }
                    /*--------------------------------------------------*/
                    /* Set the "isPassActive" flag (if it is indeed...) */
                    /* This flag will influence the GUI and any upload  */
                    /* process...                                       */
                    /*--------------------------------------------------*/
                    if( sat.isVisible() )
                        isPassActive = true;                        
                    else
                        isPassActive = false;
                }
                else {
                    isPassActive = false;
                }
                try{ Thread.currentThread().sleep( 2000 ); }
                catch( InterruptedException ie ) {}                
            }
        }                
        /*--------------------------------------------------------------------*/
        /*              Thread to read chars from the network link            */
        /*--------------------------------------------------------------------*/
        else if( Thread.currentThread() == spacecraftCommThread ) {            
            
            if( dialogIntro != null );
                dialogIntro.appendInfo("Entering data acquisition thread. Pri was "+Thread.currentThread().getPriority()+"\n");
            System.out.println("Data acquisition thread priority was "+Thread.currentThread().getPriority());
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            int dataSize = 0;
            byte[] byteArrayRead = null;
            Frame frameRead = null;
            int i = 0;
            boolean readSomething = false;
            int oldSpacecraftFrameCount = 0;
            
            try{Thread.currentThread().sleep(3000);}
            catch(InterruptedException ie){}
            
            /*------------------------------------------------------------*/
            /* Read inputs from the network server, and filter/parse them */
            /*------------------------------------------------------------*/
            READ_INPUT_LOOP:
            while( !stopReadingInputs && appRunning ) {

                /*------------------------------------------------------*/
                /* First we need to be sure that there is an AX25 input */
                /* stream to read the spacecraft's frames. If not, this */
                /* is NOT a normal situation, so keep complaining...    */
                /*------------------------------------------------------*/
                while( srvLnk == null ) {
                    try{spacecraftCommThread.sleep(1000);}
                    catch(InterruptedException e){}
                    log.criticalError("NO LINK WITH SERVER");
                }
                
                i = 0;
                readSomething = false;
                  
                /*------------------------------------------------------*/
                /* Verify the amount of frames received from the S/C and*/
                /* compare it to what we had the lase time. If different*/
                /* set the isNewIncomingData flag.                      */
                /*------------------------------------------------------*/
                //isNewIncomingData = ax25is.parseInputStream();
                nbSpacecraftFramesReceived = 
                                    srvLnk.getNbSpacecraftFramesReceived();
                if( oldSpacecraftFrameCount != nbSpacecraftFramesReceived ) {
                    isNewIncomingData = true;
                    oldSpacecraftFrameCount = nbSpacecraftFramesReceived;
                }
                
                /*-----------------------------------------------------------*/
                /* Systematically read data received from the spacecraft but */
                /* un-formatted. This could be junk, bad frames or data from */
                /* the TNC...                                                */
                /* Send it to the TNC panel anyway.                          */
                /*-----------------------------------------------------------*/
                //byte[] unformattedData = ax25is.readUnformattedData();
                byte[] unformattedData = 
                                    srvLnk.getSpacecraftUnformattedData();
                if( (panelTNC!=null) && (unformattedData!=null) ) {
                        panelTNC.print(new String(unformattedData));
                }
                    
                /*-----------------------------------------*/
                /* See if data from spacecraft is present. */
                /* Read any non-reserved frame...          */
                /*-----------------------------------------*/
                SC_READ_ATTEMPT:
                {
                    //frameRead = ax25is.read();
                    frameRead = (Frame)srvLnk.getSpacecraftFrame();
                    if( frameRead == null ) {                        
                        break SC_READ_ATTEMPT;
                    }
                    
                    if( panelTNC != null ) {
                        byte[] frameInfoBytes = frameRead.getInfoBytes();
                        if( (frameInfoBytes!=null)&&(panelTNC.isShowing()) )
                            panelTNC.println(new String(frameInfoBytes));
                    }
                    
                    //........................................................FS
                    if( frameRead.getDestinationAddress().equals("FS-0") ) {
                        
                        /*---------------------------------------------*/
                        /* We received something from the file system, */
                        /* decode it                                   */
                        /*---------------------------------------------*/
                        String strFSData = null;
            
                        switch( frameRead.getPID() ) {
                        case 0x01:
                            /*--------------------------------------*/
                            /* This is for file uploading responses */
                            /*--------------------------------------*/
                            strFSData = new String(frameRead.getInfoBytes());
                            if( strFSData.startsWith("Y") ) {
                                fileSystemACK = true;
                                System.out.println("Received ACK from FS");
                            } else if( strFSData.startsWith("N") ) {
                                fileSystemNACK = true;
                                System.out.println("Received NACK from FS.........");
                            }
                            txtInfo.append( frameRead.toString() );
                            break;
                        case 0x02:
                            /*--------------------------------*/
                            /* This is for downloading a file */
                            /*--------------------------------*/
                            txtInfo.append(frameRead.toString());
                            break;
                        case 0x03:
                            /*-----------------------------------*/
                            /* This is for sending the directory */
                            /*-----------------------------------*/
                            strFSData = frameRead.getPrintableInfo();
                            if( strFSData.startsWith("CLEAR") ) {
                                txtFSDir.setText("");
                            } else {
                                txtFSDir.append(strFSData+"\n");
                            }
                            break;
                        case 0x04:
                            /*------------------------------------*/
                            /* This is for formatting information */
                            /*------------------------------------*/
                            strFSData = frameRead.getPrintableInfo();
                            if( strFSData.startsWith("CLEAR") ) {
                                txtFSFormat.setText("");
                            } else {
                                txtFSFormat.append(strFSData);
                            }
                            break;
                        default:
                            /*---------------------------------------*/
                            /* Anything else is for information text */
                            /*---------------------------------------*/
                            txtInfo.append( frameRead.toString() );
                            break;
                        }                        
                    }                    
                    //.....................................................LSTAT
                    else
                    if( frameRead.getDestinationAddress().equals("LSTAT-1") ) {
                        /*--------------------*/
                        /* Decode status line */
                        /*--------------------*/
                        String strStatus = frameRead.getPrintableInfo();
                        int startIndex, endIndex;            
                        startIndex = strStatus.indexOf("o:")+2;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 1) && (endIndex > 1) )
                            txtTransmitOverflow.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtTransmitOverflow.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("P:")+2;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 1) && (endIndex > 1) )
                            txtPHTSeg.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtPHTSeg.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("q:")+2;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 1) && (endIndex > 1) )
                            txtTransmitQueue.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtTransmitQueue.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("l:")+2;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 1) && (endIndex > 1) )
                            txtLargestBlock.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtLargestBlock.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("f:")+2;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 1) && (endIndex > 1) )
                            txtMemAvail.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtMemAvail.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("d:")+2;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 1) && (endIndex > 1) )
                            txtDigipeat.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtDigipeat.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("st:")+3;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 2) && (endIndex > 1) )
                            txtTaskNumber.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtTaskNumber.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("e:")+2;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 1) && (endIndex > 1) )
                            txtEDACErr.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtEDACErr.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("rx0:")+4;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 3) && (endIndex > 1) )
                            txtRX0.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtRX0.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("rx1:")+4;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 3) && (endIndex > 1) )
                            txtRX1.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtRX1.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("rx2:")+4;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 3) && (endIndex > 1) )
                            txtRX2.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtRX2.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("rx3:")+4;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 3) && (endIndex > 1) )
                            txtRX3.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtRX3.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("rx4:")+4;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 3) && (endIndex > 1) )
                            txtRX4.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtRX4.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("rx5:")+4;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 3) && (endIndex > 1) )
                            txtRX5.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtRX5.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("pht:")+4;
                        endIndex = strStatus.indexOf(" ",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 3) && (endIndex > 1) )
                            txtPHTRate.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtPHTRate.setText( "N/A" );
            
                        startIndex = strStatus.indexOf("A:")+2;
                        endIndex = strStatus.indexOf(",",startIndex);
                        if( endIndex < 0 ) endIndex = strStatus.length()-1;
                        if( (startIndex > 1) && (endIndex > 1) )
                            txtLoadSegment.setText( strStatus.substring(startIndex,endIndex) );
                        //else txtLoadSegment.setText( "N/A" );
            
                        if( chkShowSTATFrames.isSelected() ) {
                            txtInfo.append(frameRead.toString());
                            txtInfo.append("\n");
                        }
            
                    }
                    //....................................................DBLCTL
                    else
                    if( frameRead.getDestinationAddress().equals("DBLCTL-0") ) {
            
                        /*---------------------------*/
                        /* Decode bootloader message */
                        /*---------------------------*/
                        String strBLMessage = frameRead.getPrintableInfo();
            
                        if( strBLMessage.startsWith("T") ) {
                
                            /*----------------*/
                            /* Telemetry data */
                            /*----------------*/
                            int startIndex, endIndex;
                
                            /*--------------*/
                            /* Code segment */
                            /*--------------*/
                            startIndex = strBLMessage.indexOf("A:")+2;
                            endIndex = strBLMessage.indexOf(" ",startIndex);
                            if( endIndex < 0 ) endIndex = strBLMessage.length()-1;
                            if( (startIndex > 1) && (endIndex > 1) )
                                txtBLCodeSegment.setText( strBLMessage.substring(startIndex,endIndex) );
                
                            /*--------------------*/
                            /* EDAC Error Counter */
                            /*--------------------*/
                            startIndex = strBLMessage.indexOf("E:")+2;
                            endIndex = strBLMessage.indexOf(" ",startIndex);
                            if( endIndex < 0 ) endIndex = strBLMessage.length()-1;
                            if( (startIndex > 1) && (endIndex > 1) )
                                txtBLEDACErrors.setText( strBLMessage.substring(startIndex,endIndex) );
                
                            /*-----------------*/
                            /* Command Counter */
                            /*-----------------*/
                            startIndex = strBLMessage.indexOf("C:")+2;
                            endIndex = strBLMessage.indexOf(" ",startIndex);
                            if( endIndex < 0 ) endIndex = strBLMessage.length()-1;
                            if( (startIndex > 1) && (endIndex > 1) ) {
                                int commandCounter = Integer.parseInt( strBLMessage.substring(startIndex,endIndex), 16 );
                                txtBLCommandCounter.setText( ""+commandCounter );
                                if( (commandCounter % 2) > 0 ) {
                                    txtBLCommandCounter.setBackground(java.awt.Color.black);
                                    txtBLCommandCounter.setForeground(java.awt.Color.white);
                                } else {
                                    txtBLCommandCounter.setBackground(java.awt.Color.white);
                                    txtBLCommandCounter.setForeground(java.awt.Color.black);
                                }   
                    
                            }
                
                            /*--------------------------*/
                            /* I/O Ports Data Direction */
                            /*--------------------------*/
                            startIndex = strBLMessage.indexOf("D:")+2;
                            endIndex = strBLMessage.indexOf(" ",startIndex);
                            if( endIndex < 0 ) endIndex = strBLMessage.length()-1;
                            if( (startIndex > 1) && (endIndex > 1) )
                                txtBLDataDirection.setText( strBLMessage.substring(startIndex,endIndex) );
                
                            /*----------------*/
                            /* I/O Ports Data */
                            /*----------------*/
                            startIndex = strBLMessage.indexOf("IO:")+3;
                            //endIndex = strBLMessage.indexOf(" ",startIndex);
                            endIndex = startIndex+10;
                            if( endIndex < 0 ) endIndex = strBLMessage.length()-1;
                            if( (startIndex > 1) && (endIndex > 1) )
                                txtBLIOPortsData.setText( strBLMessage.substring(startIndex,endIndex) );
                
                            /*----------*/
                            /* QBL Info */
                            /*----------*/
                            startIndex = 4;
                            endIndex = strBLMessage.indexOf("A:",startIndex);
                            if( endIndex < 0 ) endIndex = strBLMessage.length()-1;
                            if( (startIndex > 1) && (endIndex > 1) )
                                txtBLInfo.setText( strBLMessage.substring(startIndex,endIndex) );
                
                        }
                        
                        String addr = frameRead.getOriginatorAddress();
                        if( addr == null ) 
                            txtInfo.append( "[N/A]:" );
                        else
                            txtInfo.append( "["+addr+"]:" );
                        addr = frameRead.getDestinationAddress();
                        if( addr == null )
                            txtInfo.append( "[N/A] >" );
                        else
                            txtInfo.append( "["+addr+"] >" );
                        String type = "";
                        int EDAC = 0;
                        int data = 0;
                        txtInfo.append( 
                            qbl.interpretReply(frameRead.getInfoBytes(),type,EDAC,data) );
                        txtInfo.append("\n");                        
            
                    }
                    //.......................................................TLM
                    else 
                    if( frameRead.getDestinationAddress().equals("TLM-1") ) {
                        if( chkShowTLMFrames.isSelected() ) {
                            txtInfo.append("["+frameRead.getOriginatorAddress()+"]"
                                        + ":["+frameRead.getDestinationAddress()+"] >" 
                                        + tlm.decode(frameRead.getInfoBytes()) 
                                        + "\n" );                            
                        }
            
                    }
                    //......................................................TIME
                    else 
                    if( frameRead.getDestinationAddress().equals("TIME-1") ) {
                        if( chkShowTimeFrames.isSelected() ) {
                            txtInfo.append(frameRead.toString()+"\n");
                        }
            
                    }
                    //.............................................ANYTHING ELSE
                    else {
            
                        txtInfo.append(frameRead.toString()+"\n");
                        //try{
                        //  for( int i=0; i<received.length(); i++ ) {
                        //    if( received.charAt(i) != (char)0xFF )
                        //        System.out.print(""+(int)(received.charAt(i))+",");
                        //        //System.out.print(""+Character.digit(received.charAt(i),10));
                        //  }
                        //  System.out.println("");
                        //} catch( NumberFormatException nfe ){}
                    }

                }//SC_READ_ATTEMPT
                  
                /*----------------------------------------------------------*/
                /* If we read something on this loop then wait less then if */
                /* the link with the spacecraft was idle...                 */
                /*----------------------------------------------------------*/
                if( readSomething )
                    try{Thread.currentThread().sleep(5);}catch( InterruptedException ie ) {}
                else
                    try{Thread.currentThread().sleep(10);}catch( InterruptedException ie ) {}
            }
            
            /*----------------------------*/
            /* We should never exit this! */
            /*----------------------------*/
            log.criticalError("Exiting data acquisition");
            //JOptionPane.showMessageDialog( this, 
            //                                "Spacecraft Input Stream Died",
            //                                "CRITICAL ERROR", 
            //                                JOptionPane.ERROR_MESSAGE );
            
        }        
        
        /*------------------------------------------------------------------*/
        /*              P A C S A T   S E S S I O N   T H R E A D           */
        /*------------------------------------------------------------------*/
        else if( Thread.currentThread() == pacsatSessionThread ) {
            
            try{Thread.currentThread().sleep(2000);}
            catch(InterruptedException ie){};

            /*---------------------------------------------------------*/
            /* Could we support multiple concurrent pacsat sessions??? */
            /*---------------------------------------------------------*/
            ax25PacsatLinkState = AX25_PACSAT_DISCONNECTED;
            pacsatSessionState = PACSAT_SESSION_DISCONNECTED;
            
            PACSAT_SESSION:
            while( pacsatSessionActive && appRunning ) {
                
                /*-----------------------------------------------*/
                /* First we have to establish an AX25 connection */
                /*-----------------------------------------------*/
                
                /*-------------------------*/
                /* Prepare the SABM packet */
                /*-------------------------*/
                boolean ax25ConnectionActive = false;
                Satellite sat = panelSatellites.getSelectedSatellite();
                SABMFrame sabm = new SABMFrame(
                                    panelSettings.getGroundStationCallsign(),
                                    panelSettings.getGroundStationSSID(),
                                    sat.getPacsatCallsign(),
                                    sat.getPacsatSSID()                                    
                                    );
                panelPACSAT.setPacsatAddress( 
                                    panelSettings.getGroundStationCallsign(),
                                    panelSettings.getGroundStationSSID() );
                
                /*--------------------------------------------*/
                /* Reserve the replies for our own usage only */
                /*--------------------------------------------*/
                String ourAddress = panelSettings.getGroundStationAddress();
                //ax25is.reserve( ourAddress );
                srvLnk.reserveSpacecraftAddress( ourAddress );
                
                /*----------------------------------*/
                /* Then send the connection request */
                /*----------------------------------*/
                //byte[] sabmKISS = sabm.getBytesKISSFrame();
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    //System.out.print("FORMED>");
                    //for( int i=0; i<ba.length; i++ ){
                    //    System.out.print("/"+(int)(ba[i]&0xFF));
                    //}
                    //System.out.println("/");
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~                
                srvLnk.sendPacketToSpacecraft(sabm.getBytesKISSFrame());
                panelPACSAT.println("(SENT)"+sabm.toString());
                
                /*---------------------------------------------*/
                /* Then we wait for a connection confirmation: */
                /* An UA from the AX25 driver, and a LOGIN     */
                /* CONFIRMATION from the PACSAT task.          */
                /*---------------------------------------------*/
                Frame frameRead = null;
                LoginRespPacket pacsatLoginResp = null;
                boolean receivedUA          = false;
                boolean receivedLOGIN_RESP   = false;
                
                /*--------------------------------------------------------*/
                /* Wait until we cancel our wish to activate a session or */
                /* we receive both a UA and a login response...           */
                /*--------------------------------------------------------*/
                WAIT_FOR_PACSAT_CONNECTION:
                while( pacsatSessionActive&& !receivedLOGIN_RESP ) {
                    //frameRead = ax25is.readFrameDestinedTo( ourAddress );
                    frameRead = srvLnk.getSpacecraftFrameDestinedTo(ourAddress);
                    if( frameRead != null ) {
                        panelPACSAT.println(frameRead.toString());
                        /*--------------*/
                        /* Check for UA */
                        /*--------------*/
                        if( frameRead.isUA() ) {
                            System.out.println("===================>UA");
                            receivedUA = true;
                        }
                        /*----------------------*/
                        /* Check for LOGIN RESP */
                        /*----------------------*/
                        if( frameRead.isINFO() ) {
                            System.out.println("===================>INFO");
                            Packet pacsatPacket = 
                                new Packet( frameRead.getInfoBytes() );
                            if( pacsatPacket.isLOGIN_RESP() ) {
                                System.out.println("==============>LOGIN RESP");
                                receivedLOGIN_RESP = true;
                            }
                            panelPACSAT.println(pacsatPacket.toString());
                        }
                    }
                    try{Thread.currentThread().sleep(1000);}
                    catch( InterruptedException e ){} //$$$
                }
                
                /*------------------------------------------------------*/
                /* If received UA and login response just say hello and */
                /* FTL0 should consider the connection active...        */
                /*------------------------------------------------------*/
                if( receivedLOGIN_RESP ) {                    
                    ax25ConnectionActive = true;        
                    panelPACSAT.println("============>Connecting");
                    UAFrame ua = new UAFrame(
                                    panelSettings.getGroundStationCallsign(),
                                    panelSettings.getGroundStationSSID(),
                                    sat.getPacsatCallsign(),
                                    sat.getPacsatSSID()
                                    );
                    srvLnk.sendPacketToSpacecraft(ua.getBytesKISSFrame());
                    panelPACSAT.println("(SENT)"+ua.toString());                    
                }
                
                /*--------------------------------------------------*/
                /* If the connection is successfull, then begin the */
                /* PACSAT functions                                 */
                /*--------------------------------------------------*/
                panelPACSAT.println("============>Connected");
                IFrame info = new IFrame(
                                    panelSettings.getGroundStationCallsign(),
                                    panelSettings.getGroundStationSSID(),
                                    sat.getPacsatCallsign(),
                                    sat.getPacsatSSID(),
                                    new DirShortPacket(0).getBytes()
                                    );
                srvLnk.sendPacketToSpacecraft(info.getBytesKISSFrame());
                panelPACSAT.println("(SENT)"+info.toString());                                    
                PACSAT_CONNECTED:
                while( pacsatSessionActive && ax25ConnectionActive ) {                    
                    try{Thread.currentThread().sleep(1000);}
                    catch( InterruptedException e ){}
                }
                panelPACSAT.println("============>Disconnected");
                
                /*-------------------------------------------------*/
                /* Connection closed, we don't need to reserve any */
                /* frames for us anymore...                        */
                /*-------------------------------------------------*/
                srvLnk.unreserveSpacecraftAddress( ourAddress );
                
                /*------------------------------------------------------*/
                /* Wait a little bit before the next connection attempt */
                /*------------------------------------------------------*/
                try{Thread.currentThread().sleep(2000);}
                catch( InterruptedException e ){}
            }//wend pacsatSessionActive
            
        }
        /*--------------------------------------------------------------------*/
        /*          A N T E N N A    R O T A T O R    T H R E A D             */
        /*--------------------------------------------------------------------*/
        else if( Thread.currentThread() == antennaRotatorCommThread ) {

            try{Thread.currentThread().sleep(2000);}
            catch(InterruptedException ie){};
            log.info("Starting antenna rotator manager link");
            boolean readSomething = false;            
            
            while( appRunning ) {                
                
                readSomething = false;
                
                /*---------------------------*/
                /* Get the current satellite */
                /*---------------------------*/
                Satellite sat = panelSatellites.getSelectedSatellite();
                sat.setVerbose( false );

                /*------------------------------------------------*/
                /* Control the antenna, if required and if we see */
                /* the satellite over the horizon...              */
                /*------------------------------------------------*/
                if( panelTracking.isAntennaRotatorEnabled()
                 && sat.isVisible() 
                 && antennaRotator.isTimeToSetOrientation(sat.getAzimuth(),
                                                          sat.getElevation()) )
                {
                    srvLnk.sendPacketToAntennaRotator( 
                        antennaRotator.messageToSetOrientation(sat.getAzimuth(),
                                                           sat.getElevation()));
                }
                
                
                /*----------------------------------------------------*/
                /* See if data from rotator is present                */
                /*----------------------------------------------------*/
                byte[] data = srvLnk.getRotatorData();
                //log.info("Reading from rotator input stream");
                if( data != null ) {
                  //log.info("Read:"+data.length+" - "+new String(data));  
                  readSomething = true;
                  panelTracking.setAntennaRotatorFeedbackText(new String(data));
                  panelRotator.print( new String(data) );
                }
                if( readSomething )
                    try{Thread.currentThread().sleep(50);}
                    catch( InterruptedException ie ) {}
                else
                    try{Thread.currentThread().sleep(1000);}
                    catch( InterruptedException ie ) {}
            }
        }
        /*------------------------------------------------------------------*/
        /*           R A D I O    M O N I T O R I N G    T H R E A D        */
        /*------------------------------------------------------------------*/
        else if( Thread.currentThread() == radioCommThread ) {

            try{Thread.currentThread().sleep(2000);}
            catch(InterruptedException ie){};

            boolean alreadyEnabledRadioInterface = false;
            byte[] rep = null;//radio reply
            
            while( appRunning ) {
                
                alreadyEnabledRadioInterface = false;
                
                /*---------------------------*/
                /* Get the current satellite */
                /*---------------------------*/
                Satellite sat = panelSatellites.getSelectedSatellite();

                /*---------------------------------------------------*/
                /* Control the radio frequency to compensate for the */
                /* Doppler effect...                                 */
                /*---------------------------------------------------*/
                if( panelTracking.isDopplerCompensationEnabled()
                    && (sat.isVisible()||isPassIgnored) )
                {
                    /*----------------*/
                    /* Enable the CAT */
                    /*----------------*/
                    srvLnk.sendPacketToRadio(
                                radio.messageToEnableComputerInterface() );
                    alreadyEnabledRadioInterface = true;

                    /*-------------------------------------------*/
                    /* Modify the downlink frequency accordingly */
                    /*-------------------------------------------*/
                    int freqToSet = sat.getActiveDownlinkFrequency();
                    if( freqToSet > 300 ) {
                       freqToSet+=sat.getDopplerCompensationAt435();
                       panelTracking.setUsingDopplerCompensationAt435(true);
                    } else {
                       freqToSet+=sat.getDopplerCompensationAt146();
                       panelTracking.setUsingDopplerCompensationAt146(true);
                    }
                    freqToSet+=panelTracking.getDopplerTrim();
                    srvLnk.sendPacketToRadio( 
                                    radio.messageToSetRXFrequency(freqToSet));

                }
                    
                /*--------------------------------------------*/
                /* Read the radio port and update the status, */
                /* if the radio is online...                  */
                /*--------------------------------------------*/
                if( (radio != null) && (panelRadio != null) 
                    && panelRadio.isRadioOnline()
                    && panelRadio.isShowing() 
                    && !panelRadio.isChangingRadioRXFrequency ) 
                {
                    if( !alreadyEnabledRadioInterface ) {
                        srvLnk.sendPacketToRadio(
                                    radio.messageToEnableComputerInterface() );
                        alreadyEnabledRadioInterface = true;
                    }
                    
                    /*--------------------------------*/
                    /* Ask for the frequency (sat RX) */
                    /*--------------------------------*/
                    srvLnk.radioDataFlush();                    
                    //System.out.println("---------------");
                    srvLnk.sendPacketToRadio(
                                          radio.messageToRequestRXFrequency());
                    rep = new byte[5];
                    WAIT_FOR_RADIO_REPLY1:
                    for( int i=0; i<100; i++ ) {
                        if( srvLnk.radioDataAvailable() >= 5 ) {
                            srvLnk.radioDataRead( rep );
                            break WAIT_FOR_RADIO_REPLY1;
                        }
                        try{Thread.currentThread().sleep( 20 );}
                        catch( InterruptedException ie ) {}
                    }
                    panelRadio.txtRadioRX100MHz.setText(""+((rep[0]&0xFF)>>4));
                    panelRadio.txtRadioRX10MHz.setText (""+(rep[0]&0x0F));                    
                    panelRadio.txtRadioRX1MHz.setText  (""+((rep[1]&0xFF)>>4));
                    panelRadio.txtRadioRX100KHz.setText(""+(rep[1]&0x0F));
                    panelRadio.txtRadioRX10KHz.setText (""+((rep[2]&0xFF)>>4));
                    panelRadio.txtRadioRX1KHz.setText  (""+(rep[2]&0x0F));
                    panelRadio.txtRadioRX100Hz.setText (""+((rep[3]&0xFF)>>4));
                    panelRadio.txtRadioRX10Hz.setText  (""+(rep[3]&0x0F));
                    switch( (rep[4]&0xFF) ) {
                        case 0x00:
                            panelRadio.listRadioRXMode.setSelectedIndex(1); //LSB
                            break;
                        case 0x01:
                            panelRadio.listRadioRXMode.setSelectedIndex(2); //USB
                            break;
                        case 0x02:
                            panelRadio.listRadioRXMode.setSelectedIndex(3); //CW
                            break;
                        case 0x03:
                            panelRadio.listRadioRXMode.setSelectedIndex(4); //CW-R
                            break;
                        case 0x04:
                            panelRadio.listRadioRXMode.setSelectedIndex(5); //AM
                            break;
                        case 0x08:
                            panelRadio.listRadioRXMode.setSelectedIndex(6); //FM
                            break;
                        case 0x82:
                            panelRadio.listRadioRXMode.setSelectedIndex(7); //CW(N)
                            break;
                        case 0x83:
                            panelRadio.listRadioRXMode.setSelectedIndex(8); //CW(N)-R
                            break;
                        case 0x84:
                            panelRadio.listRadioRXMode.setSelectedIndex(9); //AM(N)
                            break;
                        case 0x88:
                            panelRadio.listRadioRXMode.setSelectedIndex(2); //FM(N)
                            break;
                        default:
                            panelRadio.listRadioRXMode.setSelectedIndex(0); //N/A
                            break;
                    }

                    /*-----------------------*/
                    /* Ask for the RX status */
                    /*-----------------------*/
                    srvLnk.sendPacketToRadio(
                                              radio.messageToRequestRXStatus());
                    rep = new byte[1];
                    WAIT_FOR_RADIO_REPLY2:
                    for( int i=0; i<100; i++ ) {
                        if( srvLnk.radioDataAvailable() >= 1 ) {
                            srvLnk.radioDataRead( rep );
                            break WAIT_FOR_RADIO_REPLY2;
                        }
                        try{Thread.currentThread().sleep( 20 );}
                        catch( InterruptedException ie ) {}
                    }                                        
                    int valueMeter = rep[0] & 0x1F;
                    panelRadio.prgrssRadioRXPower.setValue( valueMeter );
                    if( ((rep[0] & 0x80)>0) ) {
                      panelRadio.lblSquelchIndicator.setBackground(
                                                new java.awt.Color (0, 51, 51));
                    } else {
                      panelRadio.lblSquelchIndicator.setBackground(Color.green);
                    }

                    /*--------------------------------*/
                    /* Ask for the frequency (sat TX) */
                    /*--------------------------------*/
                    srvLnk.sendPacketToRadio(
                                          radio.messageToRequestTXFrequency());
                    rep = new byte[5];
                    WAIT_FOR_RADIO_REPLY3:
                    for( int i=0; i<100; i++ ) {
                        if( srvLnk.radioDataAvailable() >= 5 ) {
                            srvLnk.radioDataRead( rep );
                            break WAIT_FOR_RADIO_REPLY3;
                        }
                        try{Thread.currentThread().sleep( 20 );}
                        catch( InterruptedException ie ) {}
                    }
                    panelRadio.txtRadioTX100MHz.setText(""+((rep[0]&0xFF)>>4));
                    panelRadio.txtRadioTX10MHz.setText (""+(rep[0]&0x0F));
                    panelRadio.txtRadioTX1MHz.setText  (""+((rep[1]&0xFF)>>4));
                    panelRadio.txtRadioTX100KHz.setText(""+(rep[1]&0x0F));
                    panelRadio.txtRadioTX10KHz.setText (""+((rep[2]&0xFF)>>4));
                    panelRadio.txtRadioTX1KHz.setText  (""+(rep[2]&0x0F));
                    panelRadio.txtRadioTX100Hz.setText (""+((rep[3]&0xFF)>>4));
                    panelRadio.txtRadioTX10Hz.setText  (""+(rep[3]&0x0F));
                    switch( (rep[4]&0xFF) ) {
                        case 0x00:
                            panelRadio.listRadioTXMode.setSelectedIndex(1);//LSB
                            break;
                        case 0x01:
                            panelRadio.listRadioTXMode.setSelectedIndex(2);//USB
                            break;
                        case 0x02:
                            panelRadio.listRadioTXMode.setSelectedIndex(3); //CW
                            break;
                        case 0x03:
                            panelRadio.listRadioTXMode.setSelectedIndex(4);//CW-R
                            break;
                        case 0x04:
                            panelRadio.listRadioTXMode.setSelectedIndex(5); //AM
                            break;
                        case 0x08:
                            panelRadio.listRadioTXMode.setSelectedIndex(6); //FM
                            break;
                        case 0x82:
                            panelRadio.listRadioTXMode.setSelectedIndex(7);//CW(N)
                            break;
                        case 0x83:
                            panelRadio.listRadioTXMode.setSelectedIndex(8);//CW(N)-R
                            break;
                        case 0x84:
                            panelRadio.listRadioTXMode.setSelectedIndex(9);//AM(N)
                            break;
                        case 0x88:
                            panelRadio.listRadioTXMode.setSelectedIndex(2);//FM(N)
                            break;
                        default:
                            panelRadio.listRadioTXMode.setSelectedIndex(0);//N/A
                            break;
                    }

                    /*-----------------------*/
                    /* Ask for the TX status */
                    /*-----------------------*/
                    srvLnk.sendPacketToRadio(
                                              radio.messageToRequestTXStatus());
                    rep = new byte[1];
                    WAIT_FOR_RADIO_REPLY4:
                    for( int i=0; i<100; i++ ) {
                        if( srvLnk.radioDataAvailable() >= 1 ) {
                            srvLnk.radioDataRead( rep );
                            break WAIT_FOR_RADIO_REPLY4;
                        }
                        try{Thread.currentThread().sleep( 20 );}
                        catch( InterruptedException ie ) {}
                    }
                    valueMeter = rep[0] & 0x1F;
                    panelRadio.prgrssRadioTXPower.setValue( valueMeter );
                    if( ((rep[0] & 0x80) > 0) ) {
                        panelRadio.lblPTTIndicator.setBackground(
                                                new java.awt.Color (70, 0, 0));
                    } else {
                        panelRadio.lblPTTIndicator.setBackground(Color.red);
                    }

                }
                try{Thread.currentThread().sleep(2000);}catch(InterruptedException ie){}
            }//wend
        }
        /*--------------------------------------------------------------------*/
        /*                  S C R I P T I N G    T H R E A D                  */
        /*--------------------------------------------------------------------*/
        else if( Thread.currentThread() == scriptThread )
        {
            
            log.info("Starting scripting thread");
            
            scriptState = SCRIPT_STANDBYE;
            int previousScriptState = scriptState;
            scriptStep = 0;          
            scriptVector = new Vector();                        
          
            /*---------------------------------------*/
            /* Main loop managing the current script */
            /*---------------------------------------*/
            while( appRunning ) {
                
                switch( scriptState ) {
                //..........................................................
                case SCRIPT_STANDBYE:
                //..........................................................
                    /*------------------------*/
                    /* STANDBYE: Just wait... */
                    /*------------------------*/
                    if( previousScriptState != scriptState ) {
                        log.test("Setting script state to STANDBYE");
                        listScripts.setEnabled( true );
                        btnLoadScript.setEnabled( true );
                        btnSaveAsScript.setEnabled( true );
                        btnPlayPauseScript.setText("PLAY");
                        btnPlayPauseScript.setEnabled( true );
                        btnResetScript.setEnabled( true );
                        previousScriptState = scriptState;
                    }
                    break;
                //..........................................................                        
                case SCRIPT_RUNNING:
                //..........................................................
                    /*-------------------------------------*/
                    /* RUNNING: Step thru each script task */
                    /*-------------------------------------*/
                    if( previousScriptState != scriptState ) {
                        log.test("Setting script state to RUNNING");
                        listScripts.setEnabled( false );
                        btnLoadScript.setEnabled( false );
                        btnSaveAsScript.setEnabled( false );
                        btnPlayPauseScript.setText("PAUSE");
                        btnPlayPauseScript.setEnabled( true );
                        btnResetScript.setEnabled( true );
                        previousScriptState = scriptState;
                    }
                    if( scriptStep < scriptVector.size() ) {

                        /*----------------------------*/
                        /* There is a task to execute */
                        /* Get it...                  */
                        /*----------------------------*/
                        ScriptTask task = null;
                        try{
                          log.test("Processing script step "+scriptStep);
                          task=(ScriptTask)scriptVector.elementAt(scriptStep);
                        }catch( Exception e ) {
                          log.error("Could not process script task");
                        }
                        //..................................................
                        if( task == null ) {
                        //..................................................
                            /*-------------------------------------*/
                            /* Task does not exist, go to next one */
                            /*-------------------------------------*/
                            scriptStep++;

                        /*------------------------------------------------*/
                        /*           Task is WAIT FOR N SECONDS           */
                        /*------------------------------------------------*/
                        } else if( task.isWaitForNSeconds() ) {

                            /*--------------------------------------------*/
                            /* Wait for a certain elapsed time. First     */
                            /* start the timer, then on following loop    */
                            /* check if the timer has elapsed...          */
                            /*--------------------------------------------*/
                            log.test("Wait for N seconds");
                            if( !task.isStarted() ) {
                                log.test("Start wait for N seconds");
                                task.startWaitForNSeconds();
                                task.setStarted( true );
                            } else {
                                log.test("Verify if wait is complete");
                                if( task.isWaitForNSecondsCompleted() ) {
                                    log.test("Wait IS COMPLETE.......");
                                    task.setCompleted( true );
                                    scriptStep++;
                                }
                            }

                        /*------------------------------------------------*/
                        /*        Task is WAIT FOR INCOMING DATA          */
                        /*------------------------------------------------*/
                        }else if( task.isWaitForIncomingData() ) {

                            if( !task.isStarted() ) {
                                /*-------------------------------------*/
                                /* To start the task, simply put the   */
                                /* isNewIncomingData to false and wait */
                                /* for calls to ax25.parseinputstream  */
                                /* to put this global flag to true when*/
                                /* incoming data is sensed.            */
                                /*-------------------------------------*/
                                isNewIncomingData = false;
                                task.setStarted( true );
                            } else if( isNewIncomingData ) {
                                /*---------------------------------------*/
                                /* When global flag isNewIncomingData is */
                                /* true, simply go to next step.         */
                                /*---------------------------------------*/
                                task.setCompleted( true );
                                scriptStep++;
                            }                                

                        /*------------------------------------------------*/
                        /*             Task is UPLOAD A TASK              */
                        /*------------------------------------------------*/
                        }else if( task.isUploadTask() ) {
                        //..................................................    
                            log.test("Task is UPLOAD TASK");
                            
                            if( !task.isStarted() ) {

                              log.test("Init UPLOAD TASK");
                              /*------------------------------------------*/
                              /* First make sure that if the user changed */
                              /* the script on screen we captured if...   */
                              /*------------------------------------------*/
                              refreshTaskEntries();

                              /*------------------------------------------*/
                              /* Then create the taskLoader object, which */
                              /* will load it from a local disk file.     */
                              /*------------------------------------------*/
                              taskLoader=new TaskLoader(task.getFileName(),
                                                        task.getArgument(),
                                                        log );
                              task.setStarted( true );

                            } else if( !task.isCompleted() ) {
                                log.test("Process UPLOAD TASK");
                                /*---------------------------------*/
                                /* Ready to start the load process */
                                /*---------------------------------*/
                                if( taskLoader == null ) {
                                    log.error(  "Unable to prepare task "
                                                + "for upload");
                                    task.setCompleted( true );
                                } else if( taskLoader.isReady() ) {
                                    /*------------------------------------*/
                                    /* Here we "lose" control until the   */
                                    /* task is loaded or the cancelLoad() */
                                    /* method is called from the GUI      */
                                    /* manager thread...                  */
                                    /*------------------------------------*/
                                    log.test("Preparing to upload task");
                                    Satellite sat = 
                                        panelSatellites.getSelectedSatellite();                                    
                                    
                                    boolean cancel = 
                                    taskLoader.performLoad(srvLnk,
                                                           sat,
                                                           isExtendedLoadProto);
                                    if( cancel ) {
                                        scriptState = SCRIPT_STANDBYE;
                                    } else {
                                        task.setCompleted( true );
                                    }
                                } else {
                                    /*---------------------------------*/
                                    /* If the taskLoader is not ready, */
                                    /* log the reason and cancel...    */
                                    /*---------------------------------*/
                                    log.test("UPLOAD TASK NOT READY");
                                    log.error(taskLoader.getStatus());
                                    taskLoader.finalize();
                                    task.setCompleted( true );
                                }

                            } else {

                                /*----------------------------------------*/
                                /* Task completed, simply go to next step */
                                /*----------------------------------------*/
                                log.test("UPLOAD TASK COMPLETE");
                                scriptStep++;
                            }


                        //..................................................
                        }else if( task.isExtendedUploadTask() ) {
                        //..................................................    
                            scriptStep++;
                        //..................................................    
                        }else if( task.isStartHousekeeping() ) {
                        //..................................................    
                            scriptStep++;
                        //..................................................    
                        }else if( task.isExecuteUploadedTasks() ) {
                        //..................................................
                            scriptStep++;
                        //..................................................    
                        }else if( task.isChangeSSID() ) {
                        //..................................................    
                            scriptStep++;
                        //..................................................    
                        }else {
                        //..................................................    
                            scriptStep++;
                        }        

                    } else {
                        /*-----------------------------------------*/
                        /* No more task to execute, go to STANDBYE */
                        /*-----------------------------------------*/
                        scriptState = SCRIPT_STANDBYE;
                    }
                    break;
                //..........................................................
                case SCRIPT_PAUSING:
                //..........................................................
                    /*------------------------------------*/
                    /* Temporary state, do not sleep more */
                    /*------------------------------------*/
                    if( previousScriptState != scriptState ) {
                        listScripts.setEnabled( false );
                        btnLoadScript.setEnabled( false );
                        btnSaveAsScript.setEnabled( false );
                        btnPlayPauseScript.setText("PAUSE");
                        btnPlayPauseScript.setEnabled( true );
                        btnResetScript.setEnabled( true );
                        previousScriptState = scriptState;
                    }
                    scriptState = SCRIPT_PAUSED;
                    break;
                //..........................................................
                case SCRIPT_PAUSED:
                //..........................................................
                    if( previousScriptState != scriptState ) {
                        listScripts.setEnabled( false );
                        btnLoadScript.setEnabled( false );
                        btnSaveAsScript.setEnabled( true );
                        btnPlayPauseScript.setText("UNPAUSE");
                        btnPlayPauseScript.setEnabled( true );
                        btnResetScript.setEnabled( true );
                        previousScriptState = scriptState;
                    }
                    /*---------------------------------------*/
                    /* Here the UNPAUSE will interrupt us... */
                    /*---------------------------------------*/
                    try{Thread.currentThread().sleep(1000);}
                    catch(InterruptedException e){}                        
                    break;
                //..........................................................
                case SCRIPT_RELOADING:
                //..........................................................
                    /*------------------------------------*/
                    /* Temporary state, do not sleep more */
                    /*------------------------------------*/
                    if( previousScriptState != scriptState ) {
                        listScripts.setEnabled( false );
                        btnLoadScript.setEnabled( false );
                        btnSaveAsScript.setEnabled( false );
                        btnPlayPauseScript.setText("PLAY");
                        btnPlayPauseScript.setEnabled( false );
                        btnResetScript.setEnabled( true );
                        previousScriptState = scriptState;
                    }
                    break;
                //..........................................................    
                case SCRIPT_RELOADED:
                //..........................................................    
                    /*------------------------------------*/
                    /* Temporary state, do not sleep more */
                    /*------------------------------------*/
//                        if( previousScriptState != scriptState ) {
//                            listScripts.enable( true );
//                            btnLoadScript.enable( true );
//                            btnSaveAsScript.enable( true );
//                            btnPlayPauseScript.setText("PLAY");
//                            btnPlayPauseScript.enable( true );
//                            btnResetScript.enable( true );
//                            previousScriptState = scriptState;
//                        }
                    scriptStep = 0;
                    scriptState = SCRIPT_STANDBYE;
                    break;
                //..........................................................                        
                case SCRIPT_CLEARING:
                //..........................................................
                    /*------------------------------------*/
                    /* Temporary state, do not sleep more */
                    /*------------------------------------*/
                    if( previousScriptState != scriptState ) {
                        listScripts.setEnabled( false );
                        btnLoadScript.setEnabled( false );
                        btnSaveAsScript.setEnabled( false );
                        btnPlayPauseScript.setText("PLAY");
                        btnPlayPauseScript.setEnabled( false );
                        btnResetScript.setEnabled( true );
                        previousScriptState = scriptState;
                    }
                    break;
                //..........................................................                        
                case SCRIPT_CLEARED:
                //..........................................................
                    /*---------------------------------------*/
                    /* Temporary state, do not sleep more... */
                    /*---------------------------------------*/
                    scriptStep = 0;
                    scriptState = SCRIPT_STANDBYE;
                    break;
                //..........................................................    
                default:
                //..........................................................    
                    scriptStep = 0;
                    scriptState = SCRIPT_STANDBYE;
                    try{Thread.currentThread().sleep(1000);}
                    catch(InterruptedException e){}                        
                    break;
                }//switch
                
                /*-----------------------------------------------------*/
                /* This is the minimum time we want to sleep, normally */
                /* we want to sleep more depending on the state...     */
                /*-----------------------------------------------------*/                
                try{Thread.currentThread().sleep(250);}
                catch(InterruptedException e){}
            }
            
            log.info("Stopping scripting thread");

        }//Script Thread    
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JRadioButtonMenuItem radPHTCommandsPrime;
    private javax.swing.JRadioButtonMenuItem radPHTCommandsBackup;
    private javax.swing.JSeparator jSeparator18;
    private javax.swing.JCheckBoxMenuItem chkPHTVerboseMenuItem;
    private javax.swing.JSeparator jSeparator19;
    private javax.swing.JRadioButtonMenuItem radUseExtendedLoadProto;
    private javax.swing.JRadioButtonMenuItem radUseStandardLoadProto;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem menuShowMessages;
    private javax.swing.JMenuItem menuShowLogbook;
    private javax.swing.JMenuItem menuShowTelemetry;
    private javax.swing.JMenuItem menuShowSettings;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JTabbedPane panelMain;
    public javax.swing.JPanel panelBootLoader;
    private javax.swing.JPanel jPanel66;
    private javax.swing.JPanel jPanel67;
    private javax.swing.JPanel jPanel228;
    private javax.swing.JPanel jPanel231;
    private javax.swing.JButton btnQBLMove;
    private javax.swing.JButton btnQBLExecute;
    private javax.swing.JPanel jPanel230;
    private javax.swing.JLabel jLabel136;
    private javax.swing.JTextField txtQBLMemDumpSegment;
    private javax.swing.JLabel jLabel137;
    private javax.swing.JTextField txtQBLMemDumpOffset;
    private javax.swing.JLabel jLabel138;
    private javax.swing.JTextField txtQBLMemDumpNbBytes;
    private javax.swing.JButton btnQBLMemDump;
    private javax.swing.JPanel jPanel234;
    private javax.swing.JPanel jPanel232;
    private javax.swing.JLabel jLabel139;
    private javax.swing.JTextField txtQBLMemReadSegment;
    private javax.swing.JLabel jLabel140;
    private javax.swing.JTextField txtQBLMemReadOffset;
    private javax.swing.JButton btnQBLMemRead;
    private javax.swing.JLabel jLabel141;
    private javax.swing.JTextField txtQBLMemReadResult;
    private javax.swing.JPanel jPanel233;
    private javax.swing.JLabel jLabel142;
    private javax.swing.JTextField txtQBLMemWriteSegment;
    private javax.swing.JLabel jLabel143;
    private javax.swing.JTextField txtQBLMemWriteOffset;
    private javax.swing.JLabel jLabel144;
    private javax.swing.JTextField txtQBLMemWriteValue;
    private javax.swing.JButton btnQBLMemWrite;
    private javax.swing.JPanel jPanel235;
    private javax.swing.JPanel jPanel236;
    private javax.swing.JLabel jLabel145;
    private javax.swing.JTextField txtQBLIOReadPort;
    private javax.swing.JButton btnQBLIORead;
    private javax.swing.JLabel jLabel147;
    private javax.swing.JTextField txtQBLIOReadResult;
    private javax.swing.JPanel jPanel237;
    private javax.swing.JLabel jLabel148;
    private javax.swing.JTextField txtQBLIOWritePort;
    private javax.swing.JLabel jLabel150;
    private javax.swing.JTextField txtQBLIOWriteValue;
    private javax.swing.JButton btnQBLIOWrite;
    private javax.swing.JPanel jPanel229;
    private javax.swing.JLabel jLabel154;
    private javax.swing.JPanel jPanel238;
    private javax.swing.JLabel jLabel151;
    private javax.swing.JTextField txtBLCommandCounter;
    private javax.swing.JLabel jLabel146;
    private javax.swing.JTextField txtBLCodeSegment;
    private javax.swing.JLabel jLabel149;
    private javax.swing.JTextField txtBLEDACErrors;
    private javax.swing.JLabel jLabel152;
    private javax.swing.JTextField txtBLDataDirection;
    private javax.swing.JLabel jLabel153;
    private javax.swing.JTextField txtBLIOPortsData;
    private javax.swing.JLabel jLabel155;
    private javax.swing.JTextField txtBLInfo;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton btnQBLBeacon;
    private javax.swing.JButton btnSerial9600;
    private javax.swing.JButton btnSerial19200;
    private javax.swing.JPanel jPanel60;
    public javax.swing.JPanel panelScript;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel jPanel71;
    private javax.swing.JPanel jPanel73;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JPanel jPanel72;
    private javax.swing.JLabel lblScriptTask1Id;
    private javax.swing.JComboBox listScriptTask1Command;
    private javax.swing.JTextField txtScriptTask1Param1;
    private javax.swing.JTextField txtScriptTask1Param2;
    private javax.swing.JTextField txtScriptTask1Progress;
    private javax.swing.JPanel jPanel74;
    private javax.swing.JLabel lblScriptTask2Id;
    private javax.swing.JComboBox listScriptTask2Command;
    private javax.swing.JTextField txtScriptTask2Param1;
    private javax.swing.JTextField txtScriptTask2Param2;
    private javax.swing.JTextField txtScriptTask2Progress;
    private javax.swing.JPanel jPanel75;
    private javax.swing.JLabel lblScriptTask3Id;
    private javax.swing.JComboBox listScriptTask3Command;
    private javax.swing.JTextField txtScriptTask3Param1;
    private javax.swing.JTextField txtScriptTask3Param2;
    private javax.swing.JTextField txtScriptTask3Progress;
    private javax.swing.JPanel jPanel76;
    private javax.swing.JLabel lblScriptTask4Id;
    private javax.swing.JComboBox listScriptTask4Command;
    private javax.swing.JTextField txtScriptTask4Param1;
    private javax.swing.JTextField txtScriptTask4Param2;
    private javax.swing.JTextField txtScriptTask4Progress;
    private javax.swing.JPanel jPanel77;
    private javax.swing.JLabel lblScriptTask5Id;
    private javax.swing.JComboBox listScriptTask5Command;
    private javax.swing.JTextField txtScriptTask5Param1;
    private javax.swing.JTextField txtScriptTask5Param2;
    private javax.swing.JTextField txtScriptTask5Progress;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel70;
    private javax.swing.JComboBox listScripts;
    private javax.swing.JButton btnLoadScript;
    private javax.swing.JButton btnSaveAsScript;
    private javax.swing.JButton btnPlayPauseScript;
    private javax.swing.JButton btnResetScript;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JLabel lblDataSize;
    private javax.swing.JTextField txtDataSize;
    private javax.swing.JSlider slidPacketSize;
    private javax.swing.JCheckBox chkDynamicPacketSize;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel pnlLoad;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JButton btnLoad;
    private javax.swing.JLabel lblAckReq;
    private javax.swing.JLabel lblEmpty1;
    private javax.swing.JLabel lblAckLoad;
    private javax.swing.JLabel lblEmpty2;
    private javax.swing.JLabel lblAckAddress;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JButton btnSetSegment;
    private javax.swing.JTextField txtSegment;
    private javax.swing.JLabel lblSetSegmentAck;
    private javax.swing.JPanel jPanel56;
    private javax.swing.JButton btnSendData;
    private javax.swing.JTextField txtDataSegment;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JTextField txtDataOffset;
    private javax.swing.JLabel lblDataAck;
    private javax.swing.JPanel jPanel57;
    private javax.swing.JButton btnSendEOF;
    private javax.swing.JLabel lblEOFAck;
    private javax.swing.JPanel jPanel59;
    private javax.swing.JButton btnCreateTask;
    private javax.swing.JLabel lblStartAck;
    private javax.swing.JPanel jPanel68;
    private javax.swing.JButton btnRun;
    private javax.swing.JButton btnReset;
    public javax.swing.JPanel panelCommands;
    private javax.swing.JTabbedPane commandTabs;
    public javax.swing.JPanel panelDiagnostic;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JTextField txtPHTSeg;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JTextField txtLargestBlock;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JTextField txtMemAvail;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JLabel lblEDACErr;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JTextField txtEDACErr;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JTextField txtTransmitQueue;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JTextField txtDigipeat;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JTextField txtRX0;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JTextField txtRX1;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel47;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JPanel jPanel46;
    private javax.swing.JTextField txtRX2;
    private javax.swing.JPanel jPanel62;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JTextField txtRX3;
    private javax.swing.JPanel jPanel63;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JTextField txtRX4;
    private javax.swing.JPanel jPanel64;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JTextField txtRX5;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JPanel jPanel48;
    private javax.swing.JTextField txtTransmitOverflow;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel50;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JPanel jPanel51;
    private javax.swing.JTextField txtTaskNumber;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel52;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel53;
    private javax.swing.JTextField txtLoadSegment;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel54;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JPanel jPanel55;
    private javax.swing.JTextField txtPHTRate;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton2;
    public javax.swing.JPanel panelRAMDisk;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JPanel jPanel111;
    private javax.swing.JButton btnLoadFile;
    private javax.swing.JButton btnReadFile;
    private javax.swing.JButton btnFSDeleteFile;
    private javax.swing.JButton btnFSTestFill;
    private javax.swing.JButton btnFSAbort;
    private javax.swing.JPanel jPanel112;
    private javax.swing.JPanel jPanel166;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea txtFSFormat;
    private javax.swing.JPanel jPanel165;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea txtFSDir;
    private javax.swing.JPanel jPanel113;
    private javax.swing.JProgressBar prgrssRamdiskTest;
    private javax.swing.JPanel jCommonPortion;
    private javax.swing.JPanel panelInfo;
    private javax.swing.JPanel panelMessages;
    private javax.swing.JScrollPane scrpaneInfo;
    private javax.swing.JTextArea txtInfo;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel65;
    private javax.swing.JCheckBox chkTxtFollow;
    private javax.swing.JCheckBox chkShowTimeFrames;
    private javax.swing.JCheckBox chkShowTLMFrames;
    private javax.swing.JCheckBox chkShowSTATFrames;
    private javax.swing.JCheckBox chkShowLDRFrames;
    private javax.swing.JButton btnTxtSave;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel panelPassManagement;
    private javax.swing.JLabel lblPass;
    private javax.swing.JToggleButton btnIgnorePass;
    private javax.swing.JToggleButton btnPausePass;
    private javax.swing.JPanel panelComSetup;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JTextField txtSpacecraft;
    private javax.swing.JButton btnPing;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JComboBox listStations;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jGeneralStatus;
    private javax.swing.JPanel jPanel82;
    private javax.swing.JLabel jLabel174;
    private javax.swing.JComboBox listSSID;
    private javax.swing.JLabel jLabel32;
    public javax.swing.JTextField txtTelemUTC;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JLabel lblRxCount;
    private javax.swing.JTextField txtRxCount;
    private javax.swing.JLabel lblTxCount;
    private javax.swing.JTextField txtTxCount;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField txtRxErrors;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField txtTimeouts;
    private javax.swing.JPanel pnlCommand;
    private javax.swing.JPanel jPanel58;
    private javax.swing.JPanel jPanel69;
    private javax.swing.JLabel lblLoading;
    private javax.swing.JProgressBar prgrssLoad;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JLabel lblProto;
    private javax.swing.JLabel lblDCD;
    private javax.swing.JLabel lblRXError;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
    
    
    /*------------------------------------------------------------------------*/
    /** Does not take care of status or progress, but display type and 
     *  parameters...                                                         */
    /*------------------------------------------------------------------------*/
    private void displayTaskEntry( ScriptTask task, 
                                   JLabel lblScriptTaskId,
                                   JComboBox listScriptTaskCommand,
                                   JTextField txtScriptTaskParam1,
                                   JTextField txtScriptTaskParam2,
                                   JTextField txtScriptTaskProgress ) {
    /*------------------------------------------------------------------------*/
        lblScriptTaskId.setBackground(java.awt.Color.gray);
        txtScriptTaskProgress.setText("");
        if( task == null ) {
            ScriptTask.setNoActionSelected(listScriptTaskCommand);
            txtScriptTaskParam1.setText("");
            txtScriptTaskParam2.setText("");
            return;
        }
        if( task.isWaitForNSeconds() ) {                    
            ScriptTask.setWaitForNSecondsSelected(listScriptTaskCommand);
            txtScriptTaskParam1.setText(task.getParam());
            txtScriptTaskParam2.setText("");
            
        } else if( task.isWaitForIncomingData() ) {
            ScriptTask.setWaitForIncomingDataSelected(listScriptTaskCommand);
            txtScriptTaskParam1.setText(task.getFileName());
            txtScriptTaskParam2.setText("");
        } else if( task.isUploadTask() ) {
            ScriptTask.setUploadTaskSelected(listScriptTaskCommand);
            txtScriptTaskParam1.setText(task.getFileName());
            System.out.println("File name: "+task.getFileName());
            txtScriptTaskParam2.setText(task.getArgument());
        } else if( task.isExtendedUploadTask() ) {
            ScriptTask.setExtendedUploadTaskSelected(listScriptTaskCommand);
            txtScriptTaskParam1.setText(task.getFileName());
            txtScriptTaskParam2.setText(task.getArgument());
        } else if( task.isStartHousekeeping() ) {
            ScriptTask.setStartHousekeepingSelected(listScriptTaskCommand);
            txtScriptTaskParam1.setText("");
            txtScriptTaskParam2.setText("");
        } else if( task.isExecuteUploadedTasks() ) {
            ScriptTask.setExecuteUploadedTasksSelected(listScriptTaskCommand);
            txtScriptTaskParam1.setText("");
            txtScriptTaskParam2.setText("");
        } else if( task.isChangeSSID() ) {
            ScriptTask.setChangeSSIDSelected(listScriptTaskCommand);
            txtScriptTaskParam1.setText(task.getArgument());
            txtScriptTaskParam2.setText("");
        } else {
            ScriptTask.setNoActionSelected(listScriptTaskCommand);
            txtScriptTaskParam1.setText("");
            txtScriptTaskParam2.setText("");
        }        
    }    
    
    /*------------------------------------------------------------------------*/
    /** Update all task entries.                                              */
    /*------------------------------------------------------------------------*/
    private void updateScriptPanel() {
    /*------------------------------------------------------------------------*/
    
        ScriptTask task = null;
        DISPLAY_SCRIPT_TASKS:
        {
            if( scriptVector == null ) break DISPLAY_SCRIPT_TASKS;
            if( scriptVector.size() < 1 ) task = null;
            else task = (ScriptTask)scriptVector.elementAt(0);            
            displayTaskEntry(   task, lblScriptTask1Id,
                                listScriptTask1Command,
                                txtScriptTask1Param1,
                                txtScriptTask1Param2,
                                txtScriptTask1Progress );
            if( scriptVector.size() < 2 ) task = null;
            else task = (ScriptTask)scriptVector.elementAt(1);
            displayTaskEntry(   task, lblScriptTask2Id,
                                listScriptTask2Command,
                                txtScriptTask2Param1,
                                txtScriptTask2Param2,
                                txtScriptTask2Progress );
            if( scriptVector.size() < 3 ) task = null;
            else task = (ScriptTask)scriptVector.elementAt(2);
            displayTaskEntry(   task, lblScriptTask3Id,
                                listScriptTask3Command,
                                txtScriptTask3Param1,
                                txtScriptTask3Param2,
                                txtScriptTask3Progress );
            if( scriptVector.size() < 4 ) task = null;
            else task = (ScriptTask)scriptVector.elementAt(3);            
            displayTaskEntry(   task, lblScriptTask4Id,
                                listScriptTask4Command,
                                txtScriptTask4Param1,
                                txtScriptTask4Param2,
                                txtScriptTask4Progress );
            if( scriptVector.size() < 5 ) task = null;
            else task = (ScriptTask)scriptVector.elementAt(4);
            displayTaskEntry(   task, lblScriptTask5Id,
                                listScriptTask5Command,
                                txtScriptTask5Param1,
                                txtScriptTask5Param2,
                                txtScriptTask5Progress );
        }
    }

    /*------------------------------------------------------------------------*/
    /** Will not refresh parameter field to let user the chance to change
     *  it...                                                                 */
    /*------------------------------------------------------------------------*/
    private void refreshTaskEntry( ScriptTask task, 
                                   JLabel lblScriptTaskId,
                                   JComboBox listScriptTaskCommand,
                                   JTextField txtScriptTaskParam1,
                                   JTextField txtScriptTaskParam2,
                                   JTextField txtScriptTaskProgress ) {
    /*------------------------------------------------------------------------*/
         
        if( task == null ) 
            return;
        if( (scriptState==SCRIPT_RELOADING) || (scriptState==SCRIPT_CLEARING) )
            return;

        //......................................................................                               
        if( task.isWaitForNSeconds() ) {                    
        //......................................................................
            ScriptTask.setWaitForNSecondsSelected(listScriptTaskCommand);
            task.setNSeconds( txtScriptTaskParam1.getText() );
            txtScriptTaskParam2.setText(task.getStatus());
            txtScriptTaskProgress.setText(task.getProgress());
            if( task.isCompleted() ) 
                lblScriptTaskId.setBackground(java.awt.Color.white);
            else if( task.isStarted() )
                lblScriptTaskId.setBackground(java.awt.Color.green);
            else
                lblScriptTaskId.setBackground(java.awt.Color.gray);
        //......................................................................
        } else if( task.isWaitForIncomingData() ) {
        //......................................................................            
            ScriptTask.setWaitForIncomingDataSelected(listScriptTaskCommand);
            txtScriptTaskParam2.setText(task.getStatus());
            txtScriptTaskProgress.setText(task.getProgress());
            if( task.isCompleted() ) 
                lblScriptTaskId.setBackground(java.awt.Color.white);
            else if( task.isStarted() )
                lblScriptTaskId.setBackground(java.awt.Color.green);
            else
                lblScriptTaskId.setBackground(java.awt.Color.gray);
        //......................................................................
        } else if( task.isUploadTask() ) {
        //......................................................................
            ScriptTask.setUploadTaskSelected(listScriptTaskCommand);
            task.setFileName( txtScriptTaskParam1.getText() );
            task.setArgument( txtScriptTaskParam2.getText() );
            txtScriptTaskProgress.setText(task.getProgress());
            if( task.isCompleted() ) 
                lblScriptTaskId.setBackground(java.awt.Color.white);
            else if( task.isStarted() )
                lblScriptTaskId.setBackground(java.awt.Color.green);
            else
                lblScriptTaskId.setBackground(java.awt.Color.gray);
        //......................................................................
        } else if( task.isExtendedUploadTask() ) {
        //......................................................................
            ScriptTask.setExtendedUploadTaskSelected(listScriptTaskCommand);            
            task.setFileName( txtScriptTaskParam1.getText() );
            task.setArgument( txtScriptTaskParam2.getText() );
            txtScriptTaskProgress.setText(task.getProgress());
            if( task.isCompleted() ) 
                lblScriptTaskId.setBackground(java.awt.Color.white);
            else if( task.isStarted() )
                lblScriptTaskId.setBackground(java.awt.Color.green);
            else
                lblScriptTaskId.setBackground(java.awt.Color.gray);
        //......................................................................
        } else if( task.isStartHousekeeping() ) {
        //......................................................................
            ScriptTask.setStartHousekeepingSelected(listScriptTaskCommand);
            txtScriptTaskParam2.setText(task.getStatus());
            txtScriptTaskProgress.setText(task.getProgress());
            if( task.isCompleted() ) 
                lblScriptTaskId.setBackground(java.awt.Color.white);
            else if( task.isStarted() )
                lblScriptTaskId.setBackground(java.awt.Color.green);
            else
                lblScriptTaskId.setBackground(java.awt.Color.gray);
        //......................................................................
        } else if( task.isExecuteUploadedTasks() ) {
        //......................................................................
            ScriptTask.setExecuteUploadedTasksSelected(listScriptTaskCommand);
            txtScriptTaskParam2.setText(task.getStatus());
            txtScriptTaskProgress.setText(task.getProgress());
            if( task.isCompleted() ) 
                lblScriptTaskId.setBackground(java.awt.Color.white);
            else if( task.isStarted() )
                lblScriptTaskId.setBackground(java.awt.Color.green);
            else
                lblScriptTaskId.setBackground(java.awt.Color.gray);
        //......................................................................
        } else if( task.isChangeSSID() ) {
        //......................................................................
            ScriptTask.setChangeSSIDSelected(listScriptTaskCommand);
            task.setArgument( txtScriptTaskParam1.getText() );
            txtScriptTaskParam2.setText(task.getStatus());
            txtScriptTaskProgress.setText(task.getProgress());
            if( task.isCompleted() ) 
                lblScriptTaskId.setBackground(java.awt.Color.white);
            else if( task.isStarted() )
                lblScriptTaskId.setBackground(java.awt.Color.green);
            else
                lblScriptTaskId.setBackground(java.awt.Color.gray);
        //......................................................................
        } else {
        //......................................................................
            ScriptTask.setNoActionSelected(listScriptTaskCommand);
            txtScriptTaskParam2.setText("");
            txtScriptTaskProgress.setText("");
            if( task.isCompleted() ) 
                lblScriptTaskId.setBackground(java.awt.Color.white);
            else if( task.isStarted() )
                lblScriptTaskId.setBackground(java.awt.Color.green);
            else
                lblScriptTaskId.setBackground(java.awt.Color.gray);
        }
    }
    /*========================================================================*/
    /** Update all script tasks from what is displayed                        */
    /*========================================================================*/
    private void refreshTaskEntries() {
    /*========================================================================*/
        ScriptTask task = null;
        REFRESH_SCRIPT_TASKS:
        {
            if( scriptVector == null ) break REFRESH_SCRIPT_TASKS;
            if( scriptVector.size() < 1 ) break REFRESH_SCRIPT_TASKS;
            task = (ScriptTask)scriptVector.elementAt(0);
            if( task == null ) break REFRESH_SCRIPT_TASKS;
            refreshTaskEntry(   task, lblScriptTask1Id,
                                listScriptTask1Command,
                                txtScriptTask1Param1,
                                txtScriptTask1Param2,
                                txtScriptTask1Progress );

            if( scriptVector.size() < 2 ) break REFRESH_SCRIPT_TASKS;
            task = (ScriptTask)scriptVector.elementAt(1);
            if( task == null ) break REFRESH_SCRIPT_TASKS;
            System.out.println(task);
            refreshTaskEntry(   task, lblScriptTask2Id,
                                listScriptTask2Command,
                                txtScriptTask2Param1,
                                txtScriptTask2Param2,
                                txtScriptTask2Progress );

            if( scriptVector.size() < 3 ) break REFRESH_SCRIPT_TASKS;
            task = (ScriptTask)scriptVector.elementAt(2);
            if( task == null ) break REFRESH_SCRIPT_TASKS;
            System.out.println(task);
            refreshTaskEntry(   task, lblScriptTask3Id,
                                listScriptTask3Command,
                                txtScriptTask3Param1,
                                txtScriptTask3Param2,
                                txtScriptTask3Progress );

            if( scriptVector.size() < 4 ) break REFRESH_SCRIPT_TASKS;
            task = (ScriptTask)scriptVector.elementAt(3);
            if( task == null ) break REFRESH_SCRIPT_TASKS;
            System.out.println(task);
            refreshTaskEntry(   task, lblScriptTask4Id,
                                listScriptTask4Command,
                                txtScriptTask4Param1,
                                txtScriptTask4Param2,
                                txtScriptTask4Progress );

            if( scriptVector.size() < 5 ) break REFRESH_SCRIPT_TASKS;
            task = (ScriptTask)scriptVector.elementAt(4);
            if( task == null ) break REFRESH_SCRIPT_TASKS;
            System.out.println(task);
            refreshTaskEntry(   task, lblScriptTask5Id,
                                listScriptTask5Command,
                                txtScriptTask5Param1,
                                txtScriptTask5Param2,
                                txtScriptTask5Progress );
        }    
    }

    
    /*=====================================enableNetworkConnectionToServer====*/
    /*========================================================================*/
    public void enableNetworkConnectionToServer() {
    /*========================================================================*/
        
        /*------------------------------------------------*/
        /* First make sure to close serial port if opened */
        /*------------------------------------------------*/
        if( serialPort != null ) {
            serialPort.notifyOnDataAvailable(false);
            serialPort.close();
        }
        
        /*----------------------------------------------*/
        /* Then attempt to establish network connection */
        /*----------------------------------------------*/


        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
        /* PATCH PATCH PATCH TO REPLACE */
        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/        
        GroundStation stationSelected = (GroundStation)listStations.getSelectedItem();        
        if( stationSelected.getNetworkAddress().trim().equals("") ) {
            /*----------------*/
            /* Do not connect */
            /*----------------*/
            //phtLink.setNetworkMode( true );
            //phtLink.setServerLink( null );
            
            if( panelACS_cmd != null ) 
                panelACS_cmd.setServerLink(null);
            if( panelEDAC_cmd != null ) 
                panelEDAC_cmd.setServerLink(null);
            if( panelHeaters_cmd != null ) 
                panelHeaters_cmd.setServerLink(null);
            if( panelHouseKeeping_cmd != null ) 
                panelHouseKeeping_cmd.setServerLink(null);
            if( panelIO_cmd != null ) 
                panelIO_cmd.setServerLink(null);
            if( panelModems_cmd != null ) 
                panelModems_cmd.setServerLink(null);
            if( panelPayload_cmd != null ) 
                panelPayload_cmd.setServerLink(null);
            if( panelPower_cmd != null ) 
                panelPower_cmd.setServerLink(null);
            if( panelPyros_cmd != null ) 
                panelPyros_cmd.setServerLink(null);
            if( panelTX_cmd != null ) 
                panelTX_cmd.setServerLink(null);
            if( panelTesting != null ) 
                panelTesting.setServerLink(null);
            if( panelRadio != null ) 
                panelRadio.setServerLink(null);
            if( panelRotator != null ) 
                panelRotator.setServerLink(null);
            if( panelPACSAT != null ) 
                panelPACSAT.setServerLink(null);
            if( panelTNC != null ) 
                panelTNC.setServerLink(null);
            

            //bootloaderLink.setNetworkMode( true );
            //bootloaderLink.setServerLink( null );            
            String message = "No station connected\n";
            if( dialogIntro == null )
                System.out.println( message );
            else
                dialogIntro.appendInfo( message );
            message = null;            
            return;    
        }
                
        /*----------------------------------------------*/
        /* If the server link doesn't exist, create it! */
        /*----------------------------------------------*/
        if( srvLnk == null ) {
            
            System.out.println("Instantiating new server link");
            srvLnk = new ServerLink( stationSelected.getNetworkAddress().trim(), 
                                        log );
        }
        System.out.println("Setting server link");
        srvLnk.setServer( stationSelected.getNetworkAddress().trim() );

        /*----------------------------------------------------------------*/
        /* We must make sure that every panel knows the link to the server*/
        /*----------------------------------------------------------------*/ 
        if( panelACS_cmd != null ) 
            panelACS_cmd.setServerLink(srvLnk);
        if( panelEDAC_cmd != null ) 
            panelEDAC_cmd.setServerLink(srvLnk);
        if( panelHeaters_cmd != null ) 
            panelHeaters_cmd.setServerLink(srvLnk);
        if( panelHouseKeeping_cmd != null ) 
            panelHouseKeeping_cmd.setServerLink(srvLnk);
        if( panelIO_cmd != null ) 
            panelIO_cmd.setServerLink(srvLnk);
        if( panelModems_cmd != null ) 
            panelModems_cmd.setServerLink(srvLnk);
        if( panelPayload_cmd != null ) 
            panelPayload_cmd.setServerLink(srvLnk);
        if( panelPower_cmd != null ) 
            panelPower_cmd.setServerLink(srvLnk);
        if( panelPyros_cmd != null ) 
            panelPyros_cmd.setServerLink(srvLnk);
        if( panelTX_cmd != null ) 
            panelTX_cmd.setServerLink(srvLnk);
        if( panelTesting != null ) 
            panelTesting.setServerLink(srvLnk);
        if( panelRadio != null ) 
            panelRadio.setServerLink(srvLnk);
        if( panelRotator != null ) 
            panelRotator.setServerLink(srvLnk);
        if( panelPACSAT != null ) 
            panelPACSAT.setServerLink(srvLnk);
        if( panelTNC != null ) 
            panelTNC.setServerLink(srvLnk);
        
        //phtLink.setNetworkMode( true );
        //phtLink.setServerLink( srvLnk );
            
        //bootloaderLink.setNetworkMode( true );
        //bootloaderLink.setServerLink( srvLnk );
            
        String message = "Setup for all streams complete\n";
        if( dialogIntro == null )
            System.out.println( message );
        else
            dialogIntro.appendInfo( message );
        message = null;
            
        //if( antennaRotator != null ) {
            //antennaRotator.setNetworkMode( true );
            //antennaRotator.setsrvLnk( srvLnk );
        //}
            
        //if( radio != null ) {
            //radio.setNetworkMode( true );
            //radio.setsrvLnk( srvLnk );
        //}
        
        /*---------------------------------------------------------*/
        /* Request an update of all available status at the ground */
        /* station. This is necessary for data which is normally   */
        /* updated only on a change, like the serial port lines... */
        /*---------------------------------------------------------*/
        if( srvLnk != null ) {
            srvLnk.updateStatus();
        }
            
    }
    
    
  //............................................................................
  //............................................................................
  //............................................................................  
  /*=====================================================LoadProgressGUI */
    public class LoadProgressGUI implements Runnable {
  /*=====================================================================*/
        
        final int RESET   = 0;
        final int PENDING = 1;
        final int SENT    = 2;
        final int ACK     = 3;
        final int LOAD    = 4;
        final int ADDRESS  = 5;
        final int NACK    = 10;
        final int ERROR   = 11;
        final int INTERRUPTED = 12;
        final int UNABLE = 13;
        
        int loadRequestStatus = 0;  //0=none, 1=pending, 2=sent, 3=ack, 4=load, 5=adress, 10=nack, 11=error
        boolean loadRequestLoadReceived = false;
        int setSegmentStatus  = 0;
        int sendDataStatus    = 0;
        int EOFStatus         = 0;
        int createTaskStatus  = 0;
        
        int progressBar = 0;
        java.awt.Color progressBarColor = java.awt.Color.blue;
        
        String loadRequestString        = "LOAD REQUEST";
        String loadRequestAckString     = "ACK";
        String loadRequestAddressString = "ADDRESS";
        String setSegmentString         = "SET SEGMENT";
        String segmentString            = "";
        String setSegmentAckString      = "ACK";
        String sendDataString           = "SEND DATA";
        String dataSegmentString        = "";
        String dataOffsetString         = "";
        String sendDataAckString        = "ACK";
        String EOFString                = "END OF FILE";
        String EOFAckString             = "ACK";
        String createTaskString         = "CREATE TASK";
        String createTaskAckString      = "ACK";
        
    /*==================================================================run */
        public void run() {
    /*======================================================================*/
            
        /*-------------------------------------------*/
        /* Takes care of the LOAD REQUEST indicators */
        /*-------------------------------------------*/
        switch( loadRequestStatus ) {
            case RESET: //Init                    
                btnLoad.setBackground(java.awt.Color.lightGray);
                lblAckReq.setBackground(java.awt.Color.lightGray);
                lblAckLoad.setBackground(java.awt.Color.lightGray);
                lblAckAddress.setBackground(java.awt.Color.lightGray);
                loadRequestLoadReceived = false;
                loadRequestString = "LOAD REQUEST";
                loadRequestAddressString = "ADDRESS";
                loadRequestAckString = "ACK";
                break;
            case PENDING: //Pending
                btnLoad.setBackground(java.awt.Color.yellow);
                lblAckReq.setBackground(java.awt.Color.lightGray);
                lblAckLoad.setBackground(java.awt.Color.lightGray);
                lblAckAddress.setBackground(java.awt.Color.lightGray);
                loadRequestAckString = "ACK";
                loadRequestAddressString = "ADDRESS";
                break;
            case SENT: //Sent
                btnLoad.setBackground(java.awt.Color.green);
                lblAckReq.setBackground(java.awt.Color.yellow);
                lblAckLoad.setBackground(java.awt.Color.yellow);
                lblAckAddress.setBackground(java.awt.Color.yellow);
                break;
            case ACK: //Acknowledged OK
                btnLoad.setBackground(java.awt.Color.green);
                lblAckReq.setBackground(java.awt.Color.green);
                if( loadRequestLoadReceived )
                    lblAckLoad.setBackground(java.awt.Color.green);
                break;
            case LOAD:
                lblAckReq.setBackground(java.awt.Color.green);
                lblAckLoad.setBackground(java.awt.Color.green);
                break;
            case ADDRESS:
                lblAckReq.setBackground(java.awt.Color.green);
                if( loadRequestLoadReceived )
                    btnLoad.setBackground(java.awt.Color.green);
                lblAckAddress.setBackground(java.awt.Color.green);
                break;
            case NACK: //Acknowledge non OK
                btnLoad.setBackground(java.awt.Color.green);
                lblAckReq.setBackground(java.awt.Color.red);
                break;
            case INTERRUPTED:    //On-board buffer full: previous load interrupted
                btnLoad.setBackground(java.awt.Color.green);
                lblAckReq.setBackground(java.awt.Color.green);
                lblAckLoad.setBackground(java.awt.Color.green);
                lblAckAddress.setBackground(java.awt.Color.red);
                loadRequestAddressString = "PREVIOUS LOAD IN PROGRESS";
                break;
            case UNABLE:    //SCOS was unable to get the memory requested
                btnLoad.setBackground(java.awt.Color.green);
                lblAckReq.setBackground(java.awt.Color.green);
                lblAckLoad.setBackground(java.awt.Color.green);
                lblAckAddress.setBackground(java.awt.Color.red);
                loadRequestAddressString = "UNABLE TO GET MEMORY";
                break;

            default: // Error
                btnLoad.setBackground(java.awt.Color.red);
                lblAckReq.setBackground(java.awt.Color.red);
                loadRequestString = "LOAD REQUEST ERROR";
                loadRequestAckString = "ACK ERROR";
                break;
        }
        btnLoad.setText( loadRequestString );
        lblAckReq.setText( loadRequestAckString );
        lblAckAddress.setText( loadRequestAddressString );
            
            
        /*------------------------------------------*/
        /* Takes care of the SET SEGMENT indicators */
        /*------------------------------------------*/
        switch( setSegmentStatus ) {
            case RESET: //Init
                btnSetSegment.setBackground(java.awt.Color.lightGray);
                lblSetSegmentAck.setBackground(java.awt.Color.lightGray);
                setSegmentString = "SET SEGMENT";
                segmentString = "";
                setSegmentAckString = "ACK";
                break;
            case PENDING: //Pending
                btnSetSegment.setBackground(java.awt.Color.yellow);
                lblSetSegmentAck.setBackground(java.awt.Color.lightGray);
                break;
            case SENT: //Sent
                btnSetSegment.setBackground(java.awt.Color.green);
                lblSetSegmentAck.setBackground(java.awt.Color.yellow);
                break;
            case ACK: //Acknowledged OK
                btnSetSegment.setBackground(java.awt.Color.green);
                lblSetSegmentAck.setBackground(java.awt.Color.green);
                break;
            case NACK: //Acknowledge non OK
                lblSetSegmentAck.setBackground(java.awt.Color.red);
            default: // Error
                btnSetSegment.setBackground(java.awt.Color.red);
                lblSetSegmentAck.setBackground(java.awt.Color.red);
                setSegmentString = "SET SEGMENT ERROR";
                setSegmentAckString = "ACK ERROR";
                break;
        }
        btnSetSegment.setText( setSegmentString );
        txtSegment.setText( segmentString );
        lblSetSegmentAck.setText( setSegmentAckString );

            
        /*----------------------------------------*/
        /* Takes care of the SEND DATA indicators */
        /*----------------------------------------*/
        switch( sendDataStatus ) {
            case RESET: //Init
                btnSendData.setBackground(java.awt.Color.lightGray);
                lblDataAck.setBackground(java.awt.Color.lightGray);
                sendDataString = "SEND DATA";
                dataSegmentString = "";
                dataOffsetString = "";
                sendDataAckString = "ACK";
                break;
            case PENDING: //Pending
                sendDataAckString = "ACK";
                btnSendData.setBackground(java.awt.Color.yellow);
                lblDataAck.setBackground(java.awt.Color.lightGray);
                break;
            case SENT: //Sent
                sendDataAckString = "ACK";
                btnSendData.setBackground(java.awt.Color.green);
                lblDataAck.setBackground(java.awt.Color.yellow);
                break;
            case ACK: //Acknowledged OK
                sendDataAckString = "ACK";
                btnSendData.setBackground(java.awt.Color.green);
                lblDataAck.setBackground(java.awt.Color.green);
                break;
            case NACK: //Acknowledge non OK
                lblDataAck.setBackground(java.awt.Color.red);
                default: // Error
                    btnSendData.setBackground(java.awt.Color.red);
                    lblDataAck.setBackground(java.awt.Color.red);
                    sendDataString = "SEND DATA ERROR";
                    sendDataAckString = "ACK ERROR";
                    break;
        }
        btnSendData.setText( sendDataString );
        txtDataSegment.setText( dataSegmentString );
        txtDataOffset.setText( dataOffsetString );
        lblDataAck.setText( sendDataAckString );
            
        /*------------------------------------------*/
        /* Takes care of the END OF FILE indicators */
        /*------------------------------------------*/
        switch( EOFStatus ) {
            case RESET: //Init
                btnSendEOF.setBackground(java.awt.Color.lightGray);
                lblEOFAck.setBackground(java.awt.Color.lightGray);
                EOFString = "END OF FILE";
                EOFAckString = "ACK";
                break;
            case PENDING: //Pending
                btnSendEOF.setBackground(java.awt.Color.yellow);
                lblEOFAck.setBackground(java.awt.Color.lightGray);
                break;
            case SENT: //Sent
                btnSendEOF.setBackground(java.awt.Color.green);
                lblEOFAck.setBackground(java.awt.Color.yellow);
                break;
            case ACK: //Acknowledged OK
                btnSendEOF.setBackground(java.awt.Color.green);
                lblEOFAck.setBackground(java.awt.Color.green);
                break;
            case NACK: //Acknowledge non OK
                lblEOFAck.setBackground(java.awt.Color.red);
                default: // Error
                    btnSendEOF.setBackground(java.awt.Color.red);
                    lblEOFAck.setBackground(java.awt.Color.red);
                    EOFString = "SEND DATA ERROR";
                    EOFAckString = "ACK ERROR";
                    break;
        }
        btnSendEOF.setText( EOFString );
        lblEOFAck.setText( EOFAckString );
            
        /*------------------------------------------*/
        /* Takes care of the CREATE TASK indicators */
        /*------------------------------------------*/
        switch( createTaskStatus ) {
            case RESET: //Init
                btnCreateTask.setBackground(java.awt.Color.lightGray);
                lblStartAck.setBackground(java.awt.Color.lightGray);
                createTaskString = "CREATE TASK";
                createTaskAckString = "ACK";
                break;
            case PENDING: //Pending
                btnCreateTask.setBackground(java.awt.Color.yellow);
                lblStartAck.setBackground(java.awt.Color.lightGray);
                break;
            case SENT: //Sent
                btnCreateTask.setBackground(java.awt.Color.green);
                lblStartAck.setBackground(java.awt.Color.yellow);
                break;
            case ACK: //Acknowledged OK
                btnCreateTask.setBackground(java.awt.Color.green);
                lblStartAck.setBackground(java.awt.Color.green);
                break;
            case NACK: //Acknowledge non OK
                lblStartAck.setBackground(java.awt.Color.red);
                break;
                default: // Error
                    btnCreateTask.setBackground(java.awt.Color.red);
                    lblStartAck.setBackground(java.awt.Color.red);
                    createTaskString = "CREATE TASK ERROR";
                    createTaskAckString = "ACK ERROR";
                    break;
        }
        btnCreateTask.setText( createTaskString );
        lblStartAck.setText( createTaskAckString );

        prgrssLoad.setForeground( progressBarColor );
        prgrssLoad.setValue( progressBar );                        

    }
        
    /*==============================================================reset*/
    /* Reset the state of the GUI. Automatically calls invokeLater       */
    /*===================================================================*/
        public void reset() {
    /*=======================================================================*/
            
            progressBar = 0;
            
            loadRequestStatus   = 0;
            loadRequestLoadReceived = false;
            setSegmentStatus    = 0;
            sendDataStatus      = 0;
            EOFStatus           = 0;
            createTaskStatus    = 0;
            
            loadRequestString       = "LOAD REQUEST";
            loadRequestAckString    = "ACK";
            setSegmentString        = "SET SEGMENT";
            setSegmentAckString     = "ACK";
            sendDataString           = "SEND DATA";
            sendDataAckString       = "ACK";
            EOFString               = "END OF FILE";
            EOFAckString            = "ACK";
            createTaskString        = "CREATE TASK";
            createTaskAckString     = "ACK";
            
            SwingUtilities.invokeLater( this );
            
        }
        
    /*=================================================================update*/
    /* Call to request an update of the load progress GUI                    */
    /*=======================================================================*/
        public void update() {
    /*=======================================================================*/
            SwingUtilities.invokeLater( this );
            
        }
        
    }
    
    
  //............................................................................
  //............................................................................
  //............................................................................  
  /*=====================================================LoadProgressGUI */
    public class InfoGUI implements Runnable {
  /*=====================================================================*/
        
        String textShown = "";
        String message = "";
        
    /*==================================================================run */
        public void run() {
    /*======================================================================*/
            txtInfo.append(message);
            txtInfo.setCaretPosition(txtInfo.getText().length());
            txtInfo.revalidate();
        }
        
        
        
        
        

        
        
    /*=================================================================update*/
    /* Call to request an update of the load progress GUI                    */
    /*=======================================================================*/
        public void update() {
    /*=======================================================================*/
            SwingUtilities.invokeLater( this );
        }
        
    }
  //............................................................................
  //............................................................................
  //............................................................................  
    
    
    
//    public class UploadFile implements Runnable {
//        byte[] binaryImage = null;
//        String shortFileName = null;
//        boolean uploading = false;
//        boolean stopLoading = false;
//        
//        public UploadFile( String fileName, byte[] fileContent ){
//            shortFileName = fileName;
//            binaryImage = fileContent;
//        }
//        
//        public void abort() {
//            stopLoading = true;
//        }
//        
//        public void run() {
//            
//            uploading = true;
//            /*---------------------------------------------------------------*/
//            /* Create a stream to read from the executable binary byte array */
//            /*---------------------------------------------------------------*/
//            ByteArrayInputStream binaryImageInputStream = new ByteArrayInputStream(binaryImage);
//            
//            /*----------------------*/
//            /* Prepare to send data */
//            /*----------------------*/
//            byte[] dataPacket = null;
//            int offset = 0;
//            int offsetMsb = 0;
//            int offsetLsb = 0;
//            boolean attemptSuccessfull = false;
//            boolean firstTime = true;
//            
//            byte frameCounter = 0;
//            
//            SENDING_FILEDATA:
//            while( true ) {
//                    
//                /*------------------------*/
//                /* Create the data packet */
//                /*------------------------*/
//                if( binaryImageInputStream.available() >= 200 ) {
//                    dataPacket = new byte[200];
//                } else if(binaryImageInputStream.available() > 0)  {
//                    dataPacket = new byte[ binaryImageInputStream.available() ];
//                } else {
//                    /*-- -----------------------------------*/
//                    /* If no more data, then exit the loop */
//                    /*-------------------------------------*/
//                    System.out.println("No more data to send to FS");
//                    break SENDING_FILEDATA;
//                }
//                try{
//                    binaryImageInputStream.read(dataPacket);
//                } catch( IOException ioe ) {
//                    System.out.println("Error filling data packet");
//                }
//                    
//                /*-----------------------*/
//                /* Create the data frame */
//                /*-----------------------*/
//                byte[] ldrFrame = new byte[dataPacket.length+13];
//                ldrFrame[0] = 0;
//                ldrFrame[1] = (byte)((ldrFrame.length-1) & 0xFF);    //Size of packet
//                //without leading 0
//                ldrFrame[2] = 'U';            //code for UPLOAD
//                ldrFrame[3] = frameCounter++;
//                for( int i=0; i<8; i++ ) {
//                    if( i < shortFileName.length() )
//                        ldrFrame[i+4] = (byte) shortFileName.charAt( i );
//                    else
//                        ldrFrame[i+4] = (byte) 0;
//                }
//                /*----------------------------------*/
//                /* Transfer the data into the frame */
//                /*----------------------------------*/
//                for( int i=0; i<dataPacket.length; i++ ) {
//                    ldrFrame[i+12] = dataPacket[i];
//                }
//                ldrFrame[dataPacket.length+12] = phtLink.calculateChecksum( ldrFrame, dataPacket.length+12 );
//                    
//                /*---------------------------------*/
//                /* Create the AX-25 and KISS frame */
//                /*---------------------------------*/
//                kissFrame = new KISSFrame( (byte)0x00, (byte)0x00,
//                new AX25UIFrame( "COMND", (byte)0x01,
//                "TSTFS",   (byte)0x08,
//                (byte)0x79,
//                (byte[])ldrFrame ).getBytes()
//                );
//                    
//                    
//                /*---------------------------------------------------*/
//                /* For now only allow for 3 attempts, but eventually */
//                /* will have to accomodate several orbits waiting... */
//                /*---------------------------------------------------*/
//                attemptSuccessfull = false;
//                SENDING_FILEDATA_ATTEMPT:
//                for( int attempt=0; attempt<3; attempt++ ) {
//
//                    System.out.println("Send data packet to FS");
//                    phtLink.sendRaw( kissFrame.getBytes() );
//
//                    countDownTimer = 10;
//                    fileSystemACK = false;
//                    fileSystemNACK= false;
//
//                    RECEIVING_FILEDATA_ACK:
//                    /*-------------------------------*/
//                    /* Wait for a reply or a timeout */
//                    /*-------------------------------*/
//                    while( true ) {
//
//                        if( stopLoading )
//                            break SENDING_FILEDATA;
//                        try{Thread.currentThread().sleep(50);}catch( InterruptedException ie ) {}
//                        if( countDownTimer < 0 ) {
//                            System.out.println("Time out");
//                            break RECEIVING_FILEDATA_ACK;
//                        }
//                        if(fileSystemNACK ) {
//                            System.out.println(">>>>>>FS: NACK--------");
//                            break RECEIVING_FILEDATA_ACK;
//                        }
//                        if( fileSystemACK ) {
//                            System.out.println(">>>>>FS: ACK");
//                            attemptSuccessfull = true;
//                            break SENDING_FILEDATA_ATTEMPT;
//                        }
//
//                        /*---------------------------------*/
//                        /* Give a little time to others... */
//                        /*---------------------------------*/
//                        try{Thread.currentThread().sleep(50);}catch( InterruptedException ie ) {}
//                                    
//                    }//RECEIVING_FILEDATA_ACK
//                                
//                }//SENDING_FILEDATA_ATTEMPT
//                        
//                if( !attemptSuccessfull ) break;
//            
//                try{Thread.currentThread().sleep(50);}catch( InterruptedException ie ) {}
//                
//            }// SENDING_FILEDATA
//                
//            System.out.println("End file upload");
//            uploading = false;
//                
//        }
//    }
    
    
    
    /*==================================================================*/
    /*==================================================================*/
    public class Filter implements FilenameFilter {
    /*==================================================================*/    
    String pattern = null;
    
        public Filter( String str ) {
            pattern = str;
        }
        public boolean accept (File dir, String name) {
            return name.endsWith(pattern);
        }
    }
    
    /*==================================================================*/
    /*==================================================================*/
    public class UploadScript {
    /*==================================================================*/    
    File sourceFile = null;
        
        public UploadScript() {
            this.sourceFile = null;            
        }
        public UploadScript( File sourceFile ) {
            this.sourceFile = sourceFile;            
        }
        public String getFullFilename() {
            if( sourceFile == null )
                return( null );
            else
                return( sourceFile.getPath() );
        }
        public String toString() {
            if( sourceFile == null ) 
                return( "NONE" );
            else
                return( sourceFile.getName() );
        }
    }    
    

    
}