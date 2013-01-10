/*
 * TaskLoader.java
 *
 * Created on February 20, 2002, 1:15 PM
 */

package ca.gc.space.quicksat.ground.control;

import java.io.*;
import java.util.*;
import ca.gc.space.quicksat.ground.ax25.*;
import ca.gc.space.quicksat.ground.client.*;
import ca.gc.space.quicksat.ground.satellite.*;
import ca.gc.space.quicksat.ground.util.*;

/*============================================================================*/
/** Implements what is required to prepare and upload a task into the 
 *  satellite: Load the task from a file at the ground station, process
 *  it so it is suitable to be received by the satellite, and implement
 *  the communication protocole to transfer the task on-board.
 *  The task loader is controlled by a state machine. Every time you call
 *  performLoad the process starts at the current step machine state, and
 *  if will continue to the next state unless we're in single step mode.
 *
 * @author  jfcusson
 * @version 
 */
/*============================================================================*/
public class TaskLoader {
/*============================================================================*/    

/*-----------------------*/
/* State machine control */
/*-----------------------*/
public final int STEP_INITIALIZE    = 0;
public final int STEP_LOAD_FILE     = 10;
public final int STEP_FILE_LOADED   = 20;
public final int STEP_LOAD_REQUEST  = 30;
public final int STEP_SET_SEGMENT   = 40;
public final int STEP_PROCESS_BINARY= 50;
public final int STEP_LOAD_DATA     = 60;
public final int STEP_EOF           = 70;
public final int STEP_CREATE_TASK   = 80;
public final int STEP_LOAD_COMPLETED= 100;
private int      step               = STEP_INITIALIZE;
private boolean  isSingleStepMode   = false;

/*--------*/
/* Global */
/*--------*/
String                  status                  = "Task loader is idle";
String                  fileName                = "";
String                  taskName                = "";
String                  commandLine             = "";
boolean                 isExtendedLoadProto     = true;
String                  ldrFramesAddress        = "LDR-0";    //Use QuickSat address by default.
Frame                   frame                   = null;
Satellite               sat                     = null;
ServerLink              srvLnk                  = null;
int[]                   relocItemAddress        = null;

FileInputStream         fis                     = null;
byte[]                  scosTaskHeader          = null;
byte[]                  binaryImage             = null;
ByteArrayInputStream    binaryImageInputStream  = null;
String                  taskPriority            = "N/A";
int                     fileSize                = 0;
boolean                 readyToLoad             = false;
int                     taskNumber              = 0;
byte                    fileSizeMsb             = (byte)0;
byte                    fileSizeLsb             = (byte)0;
byte                    segmentMsb              = (byte)0;
byte                    segmentLsb              = (byte)0;
boolean                 receivedAck             = false;
byte[]                  ldrFrame                = null;
int                     nextReadWithinFile      = 0; //in bytes
String                  str                     = null;
int                     exeSize                 = 0; //Lenght of executable data
int                     binSize                 = 0; //Length of data to be uploaded
int                     binSizeParagraphs       = 0; //1 paragraph = 16 bytes.
int                     binSizeParagraphsMsb    = (byte)0;
int                     binSizeParagraphsLsb    = (byte)0;

byte[]      loadReq                 = null;
long        timeLoadReqSent         = (long)0;
final long  LOAD_REQUEST_TIMEOUT    = 3000; //3 sec
boolean     isLoadRequestOk         = false;
boolean     isLoadRequestError      = false;
boolean     retryLoadReq            = false;
boolean     isLoadRequestSent       = false;
int         nbLoadRequestSent       = 0;
boolean     isLoadRequestAcked      = false;
int         nbLoadRequestAckErrors  = 0;
boolean     isLoadReadyReceived     = false;
boolean     isLoadAdressReceived    = false;
String      loadAddress             = "";
boolean     isLoadAdressValid       = false;
boolean     addressWasInSeveralChunks = false;
String      accumulatedAddress      = ""; // The address may not be

byte[]      setSegment              = null;
boolean     isSetSegmentOk          = false;
long        timeSetSegmentSent      = (long)0;
final long  SET_SEGMENT_TIMEOUT     = 2000; //2 sec
boolean     isSetSegmentError       = false;
boolean     retrySetSegment         = false;
boolean     isSetSegmentSent        = false;
int         nbSetSegmentSent        = 0;
int         nbSetSegmentAckErrors   = 0;
boolean     isSetSegmentAcked       = false;
int         segment                 = 0;

byte[]      data                    = null;
byte[]      dataFrame               = null;
boolean     isTaskBinaryOk          = false;
long        timeDataSent            = (long)0;
final long  DATA_TIMEOUT            = 2000; //2 sec
boolean     isTaskBinaryProcessed   = false; //Once true, no re-load initiation
boolean     isTaskBinaryError       = false;
boolean     retryTaskBinary         = false;
boolean     isDataSent              = false;
int         nbDataSent              = 0;
int         nbDataAckErrors         = 0;
int         nbDataAckTimeouts       = 0;
boolean     isDataAcked             = false;
boolean     isDynamicPacketSize     = false;
final int   MAX_DATA                = 220;
final int   MIN_DATA                = 4;
int         dataChunkSize           = MAX_DATA;
final int   DATA_SIZE_INCREMENT     = 15;
long        uploadPacingTime        = 625;  //mSec
int         uploadProgress          = 0;
int         dataOffset              = 0;
int         nextDataOffset          = 0;
int         dataSegment             = 0;
int         nextDataSegment         = 0;
long        dataOffsetAbsolute      = 0; //(not using segment...)
long        nextDataOffsetAbsolute  = 0;
//long        dataStartOffsetAbsolute = 0;
long        dataStartSegment        = 0;
boolean     isFirstDataPacket       = true;
boolean     timeToResendUnackPackets= false;
int         consecutiveCRCErrors    = 0;
int         consecutiveGood         = 0;
Vector      unackPackets            = null;
int         numberOfUnackedAtFirstPass = 0;
int         retryIndex              = 0;


byte[]      EOF                     = null;
boolean     isEOFOk                 = false;
long        timeEOFSent             = (long)0;
final long  EOF_TIMEOUT             = 2000; //2 sec
boolean     isEOFError              = false;
boolean     retryEOF                = false;
boolean     isEOFSent               = false;
int         nbEOFSent               = 0;
int         nbEOFAckErrors          = 0;
int         nbEOFAckTimeouts        = 0;
boolean     isEOFAcked              = false;
boolean     isTaskFlagReceived      = false;
boolean     isTaskNumberReceived    = false;
String      accumulatedTaskNumberString = null;

byte[]      createTask              = null;
boolean     isCreateTaskOk          = false;
long        timeCreateTaskSent      = (long)0;
final long  CREATE_TASK_TIMEOUT     = 2000; //2 sec
boolean     isCreateTaskError       = false;
boolean     retryCreateTask         = false;
boolean     isCreateTaskSent        = false;
int         nbCreateTaskSent        = 0;
int         nbCreateTaskErrors      = 0;
int         nbCreateTaskTimeouts    = 0;
boolean     isCreateTaskAcked       = false;
final long  CREATE_TASK_ACK_TIMEOUT = 2000;

boolean cancelLoad              = false;
boolean isPaused                = false;

Log log = null;
            
    /*------------------------------------------------------------------------*/
    /** Creates new TaskLoader. Sets the file name and the command line, and 
     *  then get the task from disk and process the binary in the spacecraft's
     *  operating system's format.
     *  @param fileName File name (full path) of the file containing the task
     *                  to upload.
     *  @param commandLine Command line to pass to the spacecraft at the same
     *                     time as the task.                                  */
    /*------------------------------------------------------------------------*/
    public TaskLoader( String fileName, String commandLine, Log log ) {
    /*------------------------------------------------------------------------*/    
        step = STEP_INITIALIZE;
        this.fileName = fileName;
        this.commandLine = commandLine;
        this.log = log;
        if( this.log == null ) this.log = new Log();
        step = STEP_LOAD_FILE;
        getTaskFile();
        step = STEP_FILE_LOADED;
        return;
    }
    
    /*------------------------------------------------------------------------*/
    /** Defines the current state of the uploading state machine.
     *  @param step Possible values:
     *              STEP_INITIALIZE
     *              STEP_LOAD_FILE
     *              STEP_FILE_LOADED
     *              STEP_LOAD_REQUEST
     *              STEP_SET_SEGMENT
     *              STEP_PROCESS_BINARY
     *              STEP_LOAD_DATA
     *              STEP_EOF
     *              STEP_CREATE_TASK
     *              STEP_LOAD_COMPLETED                                       */
    /*------------------------------------------------------------------------*/
    public void setStep( int step ) {
    /*------------------------------------------------------------------------*/    
        log.test("Set taskloader step to: "+step);
        this.step = step;
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us in which step we currently are.
     *  @return The current step.
    /*------------------------------------------------------------------------*/
    public int getStep() {
    /*------------------------------------------------------------------------*/    
        return(step);
    }
    
    /*------------------------------------------------------------------------*/
    /** Determine if we want the state machine to execute continuously on in
     *  single step mode. In single step mode a call to performLoad will return
     *  once the step, current at the moment of the call, is complete.
     *  @param isSingleStep true for single step, false for continuous.
    /*------------------------------------------------------------------------*/
    public void setSingleStepMode( boolean isSingleStepMode ) {
    /*------------------------------------------------------------------------*/    
        this.isSingleStepMode = isSingleStepMode;
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us if we are in single step mode or not.
     *  @return true if in single step, false otherwise.
    /*------------------------------------------------------------------------*/
    public boolean isSingleStepMode() {
    /*------------------------------------------------------------------------*/    
        return(isSingleStepMode);
    }
    
    /*------------------------------------------------------------------------*/
    /** Determine if we want dynamic packet size for task binary upload. In
     *  the dynamic mode, consecutive errors diminishes packet size, while
     *  consecutive successes augment it.
     *  @param isDynamic true to enable dynamic packet size, false otherwise  */
    /*------------------------------------------------------------------------*/
    public void setDynamicPacketSize( boolean isDynamic ) {
    /*------------------------------------------------------------------------*/    
        this.isDynamicPacketSize = isDynamic;
    }
    
    /*------------------------------------------------------------------------*/
    /** Tell us if the packet size is dynamic or not.
     *  @return True if the packet size is dynamic, false otherwise.          */
    /*------------------------------------------------------------------------*/
    public boolean isDynamicPacketSize() {
    /*------------------------------------------------------------------------*/    
        return(isDynamicPacketSize);
    }
    
    /*------------------------------------------------------------------------*/
    /** Defines the adress of the frames used to upload the tasks.
     *  @param adress Adress to use, in the form CALLSIGN-SSID (ex. LDR-0)    */
    /*------------------------------------------------------------------------*/
    public void setLoaderFramesAddress( String address ) {
    /*------------------------------------------------------------------------*/    
        ldrFramesAddress = address;
        if( ldrFramesAddress == null ) ldrFramesAddress = "";
    }

    /*------------------------------------------------------------------------*/
    /** Tells us the progress of the upload process.
     *  @return Progress, in percentage of accomplished upload.               */
    /*------------------------------------------------------------------------*/
    public int getUploadProgress() {
    /*------------------------------------------------------------------------*/    
        if( uploadProgress < 0 ) uploadProgress = 0;
        if( uploadProgress > 100 ) uploadProgress = 100;
        return( uploadProgress );
    }
    
    /*------------------------------------------------------------------------*/
    /** Defines delay between two packet sent in the extended upload proto.
     *  @param uploadPacingTime Time in milliseconds between to packets       */
    /*------------------------------------------------------------------------*/
    public void setUploadPacingTime( long uploadPacingTime ) {
    /*------------------------------------------------------------------------*/    
        this.uploadPacingTime = uploadPacingTime;
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the delay between two packet sent in the extended upload proto.
     *  @return Time in milliseconds between to packets                       */
    /*------------------------------------------------------------------------*/
    public long getUploadPacingTime() {
    /*------------------------------------------------------------------------*/    
        return(uploadPacingTime);
    }
    
    /*------------------------------------------------------------------------*/
    /** Defines the size of the data field in the packet uploading binary task.
     *  @param size Size in bytes of the data field.                          */
    /*------------------------------------------------------------------------*/
    public void setDataSize( int size ) {
    /*------------------------------------------------------------------------*/    
        dataChunkSize = size;
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the size of the data field in the packet uploading binary task.
     *  @return Size in bytes of the data field.                              */
    /*------------------------------------------------------------------------*/
    public int getDataSize() {
    /*------------------------------------------------------------------------*/    
        return(dataChunkSize);
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the address used for the upload protocole frames.
     *  @return Adress used, in the form CALLSIGN-SSID (ex. LDR-0)            */
    /*------------------------------------------------------------------------*/
    public String getLoaderFramesAddress() {
    /*------------------------------------------------------------------------*/    
        return( ldrFramesAddress );
    }
    
    /*------------------------------------------------------------------------*/
    /** If the task has been loaded from file and processed correctly, then
     *  this methog will indicate that we're ready by returning true.
     *  @return true if ready to upload the task, false otherwise. Use the
     *          getStatus() method to know more details.                      */
    /*------------------------------------------------------------------------*/
    public boolean isReady() {
    /*------------------------------------------------------------------------*/    
        return( readyToLoad );
    }
    
    /*------------------------------------------------------------------------*/
    /** Returns a message indicating the status of the task loader, in 
     *  particular any problem encountered while processing the disk file
     *  which contains the task to load.
     *  @return A string indicating our status.                               */
    /*------------------------------------------------------------------------*/
    public String getStatus() {
    /*------------------------------------------------------------------------*/    
        return( status );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tell us the size of the task binary to be loaded.
     *  @return The size (in bytes) of the task binary.                       */
    /*------------------------------------------------------------------------*/
    public int getTaskBinarySize() {
    /*------------------------------------------------------------------------*/    
        return( binSize );
    }

    /*------------------------------------------------------------------------*/
    /** Allow us to pause/unpause the current loading process.
     *  @param isPaused True to pause, false to unpause.
    /*------------------------------------------------------------------------*/
    public void setPause( boolean isPaused ) {
    /*------------------------------------------------------------------------*/        
        this.isPaused = isPaused;
    }

    /*------------------------------------------------------------------------*/
    /** Perform a task load process.
     *  @param srvLnk Link with the spacecraft via the server.
     *  @param sat Satellite that we talk to.
     *  @return true if the process was cancelled by the user or due to problems
     *          encountered, false otherwise.                                 */
    /*------------------------------------------------------------------------*/
    public boolean performLoad( ServerLink srvLnk,
                                Satellite sat, 
                                boolean isExtendedLoadProto ) {
    /*------------------------------------------------------------------------*/    
    
        this.isExtendedLoadProto = isExtendedLoadProto;
        this.sat = sat;
        this.srvLnk = srvLnk;
        if( weAreNotReadyToProceed() ) {
            log.warning("Not ready to upload");
            return( true );
        }
        cancelLoad = false;
        if( step == STEP_FILE_LOADED ) step = STEP_LOAD_REQUEST;
        log.info( "Preparing to upload " 
                    + fileName + " " + commandLine
                    + " into spacecraft "
                    + sat.getName() );
        srvLnk.removeReceivedSpacecraftFramesDestinedTo( ldrFramesAddress );
        srvLnk.reserveSpacecraftAddress( ldrFramesAddress );
                        
        /*--------------------------------------------------------------------*/
        /*                             LOAD REQUEST                           */
        /*--------------------------------------------------------------------*/         
        LOAD_REQUEST:
        while( (step==STEP_LOAD_REQUEST) && !cancelLoad ) {
            initLoadRequest();
            ifPausedThenWaitHere();
            createLoadRequest();
            sendLoadRequest();                        
            while( !cancelLoad ) {
              ifPausedThenWaitHere();                
              getLoadRequestReply();                
              if(isLoadRequestAcked&&isLoadReadyReceived&&isLoadAdressReceived){
                    isLoadRequestOk = true;
                    step = STEP_SET_SEGMENT;
                    if( isSingleStepMode ) cancelLoad = true;
                    break LOAD_REQUEST;
              }
              if( isLoadRequestTimeout() ) break; //retry
              waitXMilliSeconds(50);
            }
            waitXMilliSeconds(5);
        }
        
        /*----------------------------------------------------------------*/
        /*                           SET SEGMENT                          */
        /*----------------------------------------------------------------*/
        SET_SEGMENT:
        while( (step==STEP_SET_SEGMENT) && !cancelLoad ) {
            if( segment == 0 ) {
                status = "Load refused";
                log.warning(    "Load of "+fileName
                                +" into spacecraft "+sat.getName()+" refused");
                cancelLoad = true;
                break SET_SEGMENT;
            }
            ifPausedThenWaitHere();
            createSetSegment();
            sendSetSegment();
            while( !cancelLoad ) {  //...............Wait for ACK
                ifPausedThenWaitHere();
                getSetSegmentReply();
                if( isSetSegmentAcked ) {
                    isSetSegmentOk = true;
                    step = STEP_PROCESS_BINARY; //Do it even if single step
                    break SET_SEGMENT;
                }
                if( isSetSegmentTimeout() ) break; //retry
                waitXMilliSeconds(50);
            }
            waitXMilliSeconds(5);
        }
                        
        /*-------------------------------------------------------*/
        /*     U P D A T E   S C O S   T A S K   H E A D E R     */
        /*-------------------------------------------------------*/
        /* Now we have all the information required to complete  */
        /* the SCOS task header, so lets do it...                */
        /* NOTE: If we cancelled the load exactly at the end of  */
        /* the set segment step, we still want to proceed with   */
        /* the binary processing...                              */
        /*-------------------------------------------------------*/
        PROCESS_BINARY:
        if( step == STEP_PROCESS_BINARY ) {
            processTaskBinary();
            step = STEP_LOAD_DATA;
        }


        /*--------------------------------------------------------------------*/
        /*                              SEND DATA                             */
        /*--------------------------------------------------------------------*/
        if( step == STEP_LOAD_DATA ) initSendData();
        SEND_DATA:
        while( (step==STEP_LOAD_DATA) && !cancelLoad ) {            
            ifPausedThenWaitHere();
            if( isExtendedLoadProto ) {
                getNextDataBlockForExtendedProtocole();
                createExtDataFrame();
                sendExtDataFrame();
            } else {
                if( isDataAcked ) //Only get new block if last one acked.
                    getNextDataBlockForStandardProtocole();
                createDataFrame();
                sendDataFrame();
            }            
            DATA_ACK:
            while( !cancelLoad ) {
                ifPausedThenWaitHere();
                getDataReply();                
                if( isDataTimeout() ) break DATA_ACK; //retry                
                if( isExtendedLoadProto && !timeToResendUnackPackets ) {
                    waitXMilliSeconds((int)uploadPacingTime);
                    break DATA_ACK;//No wait for ext. proto first pass...
                } else {
                    waitXMilliSeconds(25);
                }
            }
            adjustDataSizeIfNecessary();
            updateUploadProgress();
            if( isExtendedLoadProto ) {
                checkForExtendedProtocoleSecondPassConditions();
                checkForExtendedProtocoleCompletionConditions();
            } else {
                checkForStandardProtocoleCompletionConditions();
            }
            waitXMilliSeconds(25);
        }
                            
                            
        /*--------------------------------------------------------------------*/
        /*                             EOF                                    */
        /*--------------------------------------------------------------------*/
        SENDING_EOF:
        while( (step==STEP_EOF) && !cancelLoad ) { 
            ifPausedThenWaitHere();
            initEOF();            
            createEOF();
            sendEOF();
            RECEIVING_EOF_ACK:
            while( !cancelLoad ) {
                getEOFReply();
                waitXMilliSeconds(25);
                if( isEOFTimeout() ) break; //retry
                if( isTaskNumberReceived ) {
                    isEOFOk = true;
                    step = STEP_CREATE_TASK;
                    if( isSingleStepMode ) cancelLoad = true;
                    break SENDING_EOF;
                }
            }
            waitXMilliSeconds(25);
        }
                                
        /*--------------------------------------------------------------------*/
        /*                            CREATE TASK                             */
        /*--------------------------------------------------------------------*/
        SENDING_CREATE_TASK:
        while( (step==STEP_CREATE_TASK) && !cancelLoad ) { 
            ifPausedThenWaitHere();
            initCreateTask();
            createCreateTask();
            sendCreateTask();
            RECEIVING_CREATE_TASK_ACK:
            while( !cancelLoad ) {
                getCreateTaskReply();
                waitXMilliSeconds(25);
                if( isCreateTaskTimeout() ) break;
                if( isCreateTaskAcked ) {
                    isCreateTaskOk = true;
                    step = STEP_LOAD_COMPLETED;
                    if( isSingleStepMode ) cancelLoad = true;
                    break SENDING_CREATE_TASK;
                }
            }
            waitXMilliSeconds(25);
        }

        /*--------------------------------------------------------------------*/
        /*                    L O A D    C O M P L E T E                      */
        /*--------------------------------------------------------------------*/
        
        /*---------------------------------------------------*/
        /* It is very important to unreserve the LDR frames! */
        /*---------------------------------------------------*/
        srvLnk.unreserveSpacecraftAddress("LDR-0");

        /*-----------------------------------------------------*/
        /* Return false, to indicate load was not cancelled... */
        /*-----------------------------------------------------*/
        return( false );
    }

    /*------------------------------------------------------------------------*/
    /** When called, cancels the current loading process.                     */
    /*------------------------------------------------------------------------*/
    public void cancelLoad() {
    /*------------------------------------------------------------------------*/    
        cancelLoad = true;
    }
    
    /*------------------------------------------------------------------------*/
    /** Called prior releasing this object.                                   */
    /*------------------------------------------------------------------------*/
    public void finalize() {
    /*------------------------------------------------------------------------*/    
        return;
    }
    
    /*------------------------------------------------------------------------*/
    /** Will open the task file, load it into memory, and process it in
     *  preparation for the upload.                                           */
    /*------------------------------------------------------------------------*/
    private void getTaskFile() {
    /*------------------------------------------------------------------------*/    
        
        /*--------------------------------------*/
        /* Also check filename and command line */
        /*--------------------------------------*/
        if( fileName == null ) {
            status = "Task to load does not have filename";
            readyToLoad = false;
            return;
        }
        if( commandLine == null ) commandLine = "";
                    
        /*----------------------*/
        /* Perform the sequence */
        /*----------------------*/
        byte pid = 0;          
                
        /*-----------------------------------------------------*/
        /* First of all verify if the current "file to upload" */
        /* exists                                              */
        /*-----------------------------------------------------*/
        status = "Checking file: " + fileName;            
        fileName = fileName.trim();
        FileInputStream fis = null;
        try {                   
            fis = new FileInputStream (fileName);
            fileSize = fis.available ();
            fis.close();
            if( fileSize <= 0 ) {
                status = "Task file "+fileName+" is empty";
                readyToLoad = false;
                return;
            }
        } catch( IOException ioe ) {
            status = "Task file "+fileName+" does not exist";
            readyToLoad = false;
            return;
        }
            
        /*-----------------------------------------------*/
        /* Build the task name (could be done better...) */
        /*-----------------------------------------------*/
        taskName = fileName;
        int posOfPoint = taskName.indexOf('.');
        if( posOfPoint > 0 )
            taskName = taskName.substring(0,posOfPoint);
        int posOfSlash = taskName.lastIndexOf('/');
        if( posOfSlash > 0 )
            taskName = taskName.substring(posOfSlash+1,taskName.length());            
        posOfSlash = taskName.lastIndexOf('\\');
        if( posOfSlash > 0 )
            taskName = taskName.substring(posOfSlash+1,taskName.length());                        
        //System.out.println("Task name: "+taskName);
              
        try {
            /*----------------------*/
            /* Get the file to load */
            /*----------------------*/
            fis = new FileInputStream (fileName);
            fileSize = fis.available ();
            fileSizeMsb = (byte)(fileSize>>8);
            fileSizeLsb = (byte)(fileSize & 0x0FF);
            nextReadWithinFile = 0;
                    
            /*------------------------------------------------*/
            /* Read the parameters of the                    */
            /* EXE header to know where to get the executable */
            /* code...                                        */
            /*------------------------------------------------*/

            /*-EXE HEADER - exeSignature--------------------------*/
            /* This value is set to the two initials of an MS-DOS */
            /* developer, 'MZ'. This word value is 0x5A4D, since  */
            /* this is a little-endian machine. This is just a    */
            /* "magic" value that is placed at the beginning of   */
            /* every .EXE file. If the file isn't identified with */
            /* these two bytes, then it probably isn't an .EXE    */
            /* file (should we block it then?)                    */
            /*----------------------------------------------------*/
            int exeSignature = fis.read() | (fis.read()<<8);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER - Last Page Byte Count----------------------*/
            /* Each disk block of the EXE file is an exact 512 bytes  */
            /* in size. EXE programs are not, however. They might be  */
            /* 100 bytes or 10,000 bytes. But rarely do they work out */
            /* to an exact multiple of 512 bytes. This value specifies*/
            /* how many bytes in the last block (or page) are valid,  */
            /* if the value is other than zero. If zero, then the     */
            /* entire last block is considered valid.                 */
            /*--------------------------------------------------------*/
            int lastPageByteCount   = fis.read() | (fis.read()<<8);
            //System.out.println("Last Page Byte Count: "+lastPageByteCount);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER - Page Count of EXE-------------------------*/
            /* This specifies how many blocks (pages) are used by the */
            /* entire EXE program. This value includes the size of the*/
            /* header, itself. This should be equal to:               */
            /* FLOOR( (exefilesize+511) / 512 ).                      */
            /*--------------------------------------------------------*/
            int exePages            = fis.read() | (fis.read()<<8);
            //System.out.println("Number of exe pages: "+exePages);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER - Pointer Count in Relocation Table-----*/
            /* This is number of entries in the relocation table, */
            /* provided elsewhere in the EXE file.                */
            /*----------------------------------------------------*/
            int exeRelocItems        = fis.read() | (fis.read()<<8);
            //System.out.println("Number of relocation items: "+exeRelocItems);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER - Header Size--------------------------------*/
            /* This value is the size, in paragraphs (16-byte "chunks")*/
            /* , of the EXE header. Even though the fixed size part of */
            /* the header is 28 bytes, this value allows the EXE file  */
            /* to include other information after the 28-byte header,  */
            /* but before the beginning of the program, itself. For    */
            /* example, the relocation entries may be located directly */
            /* after the 28-byte header.                               */
            /*---------------------------------------------------------*/
            int exeHeaderSize       = fis.read() | (fis.read()<<8);
            //System.out.println("Size of exe header: "+exeHeaderSize);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER - MinAlloc-----------------------------------*/
            /* Minimum additonal memory to be allocated after binary   */
            /*---------------------------------------------------------*/
            int exeMinAlloc         = fis.read() | (fis.read()<<8);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER - MaxAlloc-----------------------------------*/
            /* Maximum additonal memory to be allocated after binary   */
            /*---------------------------------------------------------*/
            int exeMaxAlloc         = fis.read() | (fis.read()<<8);
            nextReadWithinFile += 2;

            /*-EXE HEADER - InitSS-------------------------------------*/
            /* Initial Stack Segment                                   */
            /*---------------------------------------------------------*/
            int exeInitSS           = fis.read() | (fis.read()<<8);
            //System.out.println("Initial Stack Segment: "+exeInitSS);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER - InitSP-------------------------------------*/
            /* Initial Stack Pointer                                   */
            /*---------------------------------------------------------*/
            int exeInitSP           = fis.read() | (fis.read()<<8);
            //System.out.println("Initial Stack Pointer: "+exeInitSP);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER - CheckSum-----------------------------------*/
            /* Not Used                                                */
            /*---------------------------------------------------------*/
            int exeChecksum         = fis.read() | (fis.read()<<8);
            nextReadWithinFile += 2;
                   
            /*-EXE HEADER - InitIP-------------------------------------*/
            /* Initial Instruction Pointer                             */
            /*---------------------------------------------------------*/
            int exeInitIP           = fis.read() | (fis.read()<<8);
            //System.out.println("Initial Instruction Pointer: "+exeInitIP);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER - InitCS-------------------------------------*/
            /* Initial Code Segment                                    */
            /*---------------------------------------------------------*/
            int exeInitCS           = fis.read() | (fis.read()<<8);
            //System.out.println("Initial Code Segment: "+exeInitCS);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER RelocTable-----------------------------------*/
            /* Start of the relocation table (from start of file)      */
            /*---------------------------------------------------------*/
            int exeRelocTable       = fis.read() | (fis.read()<<8);
            //System.out.println("Reloc Table Postition: "+exeRelocTable);
            nextReadWithinFile += 2;
                    
            /*-EXE HEADER - Overlay------------------------------------*/
            /* Something to do with TSR...                             */
            /*---------------------------------------------------------*/
            int exeOverlay          = fis.read() | (fis.read()<<8);
            nextReadWithinFile += 2;
                    
            /*------------------------------------------*/
            /* Position ourself at the relocation table */
            /*------------------------------------------*/
            int c = 0;
            for(;nextReadWithinFile<exeRelocTable;fis.read(),nextReadWithinFile++);
                    
            /*----------------------------------------------*/
            /* Read the relocation table and create address */
            /* array relocItemAddress[]                     */
            /*----------------------------------------------*/
            relocItemAddress = new int[exeRelocItems];
            int relocSegment, relocOffset;
            for( int i=0; i<exeRelocItems; i++ ) {
                //System.out.print(">"+nextReadWithinFile);
                relocOffset = fis.read() | (fis.read()<<8);
                relocSegment  = fis.read() | (fis.read()<<8);
                nextReadWithinFile += 4;
                relocItemAddress[i] = (relocSegment*16)+relocOffset;
                //System.out.println(": Relocation address "+i+" = "+relocItemAddress[i]);
            }
                    
            /*--------------------------------------------*/
            /* And finally position ourself at the binary */
            /*--------------------------------------------*/
            for(;nextReadWithinFile<(exeHeaderSize*16);fis.read(),nextReadWithinFile++ );
            //System.out.println("Now the binary is supposed to be here, at "+nextReadWithinFile);
                    
            /*------------------------------------*/
            /* Calculate the executable file size */
            /*------------------------------------*/
            exeSize = (((exePages-1) * 512) + lastPageByteCount);                    
            //System.out.println("Executable size:"+exeSize);
            
                    
            /*-------------------------------------------------------*/
            /*     C R E A T E   S C O S   T A S K   H E A D E R     */
            /*-------------------------------------------------------*/

            /*------------------------------------------------------*/
            /* Task header size = 41(structure) + 81(command line). */
            /*------------------------------------------------------*/
            scosTaskHeader = new byte[122];

            /*------------------------------------------------------*/
            /* At the beginning of the task binary in the exe file, */
            /* we can find the template of the SCOS task header.    */
            /* Load it into our array and then we'll have to "fill  */
            /* in the blanks"                                       */
            /*------------------------------------------------------*/
            fis.read(scosTaskHeader,0,122);

            nextReadWithinFile += 122;
                        
            /*--------------------------------------------------------*/
            /* Now calculate the total number of bytes we're going to */
            /* send out there. This includes the header since it      */
            /* simply replaces the first 122 bytes of the binary data */
            /* found in the executable file...                        */
            /*--------------------------------------------------------*/
            binSize = exeSize - (exeHeaderSize * 16);
            //System.out.println("Binary size:"+binSize);
            binSizeParagraphs       = binSize / 16;
            if( (binSizeParagraphs * 16) < binSize ) binSizeParagraphs++;
            //System.out.println("Number of 16 bytes chunks:"+binSizeParagraphs);
            binSizeParagraphsMsb    = (byte)(binSizeParagraphs >> 8);
            binSizeParagraphsLsb    = (byte)(binSizeParagraphs & 0x0FF);
        }
        catch( IOException ioe ) {
            status = "Problems reading from exe file "+ioe;
            readyToLoad = false;
            return;
        }
        finally { 
            try{fis.close();}catch(IOException e){}
        }
        
        readyToLoad = true;
        return;
    }    

    
    
    
    /*========================================================================*/
    /*                      I N I T I A L I Z A T I O N S                     */
    /*========================================================================*/
    
    /*------------------------------------------------------------------------*/
    /* LOAD REQUEST                                                           */
    /*------------------------------------------------------------------------*/
    private void initLoadRequest() {
    /*------------------------------------------------------------------------*/    
        isLoadRequestAcked          = false;
        isLoadRequestError          = false;
        isLoadRequestSent           = false;
        loadAddress                 = "";
        isLoadRequestAcked          = false;
        isLoadReadyReceived         = false;
        isLoadAdressReceived        = false;
        isLoadAdressValid           = false;            
        retryLoadReq                = false;
        addressWasInSeveralChunks   = false;
        accumulatedAddress          = "";
        segment                     = 0;
    }
    /*------------------------------------------------------------------------*/
    /* SEND DATA                                                              */
    /*------------------------------------------------------------------------*/
    private void initSendData() {
    /*------------------------------------------------------------------------*/
        data                        = null;
        dataFrame                   = null;
        dataOffset                  = 0;
        dataSegment                 = segment;
        dataOffsetAbsolute          = 0; //(not using segment...)
        dataStartSegment            = segment;
        isFirstDataPacket           = true;
        timeToResendUnackPackets    = false;
        consecutiveCRCErrors        = 0;
        consecutiveGood             = 0;
        unackPackets                = new Vector();
        numberOfUnackedAtFirstPass  = 0;
        retryIndex                  = 0;
    }    
    /*------------------------------------------------------------------------*/
    /* EOF                                                                    */
    /*------------------------------------------------------------------------*/
    private void initEOF() {
    /*------------------------------------------------------------------------*/
        taskNumber = 0;            
        accumulatedTaskNumberString = "";
        isTaskFlagReceived = false;
        isTaskNumberReceived = false;
    }
    /*------------------------------------------------------------------------*/
    /* CREATE TASK                                                            */
    /*------------------------------------------------------------------------*/
    private void initCreateTask() {
    /*------------------------------------------------------------------------*/
        isCreateTaskAcked           = false;        
    }
    
    
    /*========================================================================*/
    /*                     C R E A T E     F R A M E S                        */
    /*========================================================================*/
    /*------------------------------------------------------------------------*/
    /* LOAD REQUEST                                                           */
    /*------------------------------------------------------------------------*/    
    private void createLoadRequest() {
    /*------------------------------------------------------------------------*/    
        loadReq = null;
        if( sat == null ) return;
        SatControl ctrl = sat.getControl();
        if( ctrl != null ) {
            if( isExtendedLoadProto ) {
                loadReq = ctrl.messageExtLoadRequest( binSize );
            } else {
                loadReq = ctrl.messageLoadRequest( binSize );
            }
        }
    }
    /*------------------------------------------------------------------------*/
    /* SET SEGMENT                                                            */
    /*------------------------------------------------------------------------*/    
    private void createSetSegment() {
    /*------------------------------------------------------------------------*/
        setSegment = null;
        if( sat == null ) return;
        SatControl ctrl = sat.getControl();
        if( ctrl != null ) {
            if( isExtendedLoadProto ) {                
                setSegment = ctrl.messageSetSegment( segment );                
            } else {
                setSegment = ctrl.messageExtSetSegment( segment );
            }
        }
    }    
    /*------------------------------------------------------------------------*/
    /* EXTENDED DATA                                                          */
    /*------------------------------------------------------------------------*/
    private void createExtDataFrame() {
    /*------------------------------------------------------------------------*/
        dataFrame = null;
        if( sat == null ) return;
        SatControl ctrl = sat.getControl();
        if( ctrl != null ) {
            dataFrame=ctrl.messageExtTaskData(dataSegment,dataOffset,data);
        }
    }
    /*------------------------------------------------------------------------*/
    /* STANDARD DATA                                                          */
    /*------------------------------------------------------------------------*/
    private void createDataFrame() {
    /*------------------------------------------------------------------------*/
        dataFrame = null;
        if( sat == null ) return;
        SatControl ctrl = sat.getControl();
        if( ctrl != null ) {
            dataFrame=ctrl.messageTaskData(dataSegment,dataOffset,data);
        }
    }
    /*------------------------------------------------------------------------*/
    /* EOF                                                                    */
    /*------------------------------------------------------------------------*/
    private void createEOF() {
    /*------------------------------------------------------------------------*/
        EOF = null;
        if( sat == null ) return;
        SatControl ctrl = sat.getControl();
        if( ctrl != null ) {
            if( isExtendedLoadProto )
                EOF = ctrl.messageExtEOF();
            else
                EOF = ctrl.messageEOF();                        
        }
    }
    /*------------------------------------------------------------------------*/
    /* CREATE TASK                                                            */
    /*------------------------------------------------------------------------*/
    private void createCreateTask() {
    /*------------------------------------------------------------------------*/
        createTask = null;
        if( sat == null ) return;
        SatControl ctrl = sat.getControl();
        if( ctrl != null ) {
            if( isExtendedLoadProto )
                createTask = ctrl.messageExtCreateTask(taskNumber);
            else
                createTask = ctrl.messageCreateTask(taskNumber);
        }
    }    
    

    /*========================================================================*/
    /*                       S E N D     F R A M E S                          */
    /*========================================================================*/
    /*------------------------------------------------------------------------*/
    /* LOAD REQUEST                                                           */
    /*------------------------------------------------------------------------*/
    private void sendLoadRequest() {
    /*------------------------------------------------------------------------*/
        timeLoadReqSent = (long)0;
        isLoadRequestAcked = false;
        if( loadReq != null ) {
            srvLnk.sendPacketToSpacecraft( loadReq );
            isLoadRequestSent = true;
            timeLoadReqSent = System.currentTimeMillis();
            nbLoadRequestSent++;
        }            
    }
    /*------------------------------------------------------------------------*/
    /* SET SEGMENT                                                            */
    /*------------------------------------------------------------------------*/
    private void sendSetSegment() {
    /*------------------------------------------------------------------------*/
        timeSetSegmentSent = (long)0;
        isSetSegmentAcked    = false;
        if( setSegment != null ) {
            srvLnk.sendPacketToSpacecraft( setSegment );
            isSetSegmentSent = true;
            timeSetSegmentSent = System.currentTimeMillis();
            nbSetSegmentSent++;            
        }
    }
    /*------------------------------------------------------------------------*/
    /* EXTENDED DATA                                                          */
    /*------------------------------------------------------------------------*/
    private void sendExtDataFrame() {
    /*------------------------------------------------------------------------*/
        timeDataSent = (long)0;
        isDataAcked = false;
        if( data != null ) {
            srvLnk.sendPacketToSpacecraft( dataFrame );
            isDataSent = true;
            timeDataSent = System.currentTimeMillis();
            nbDataSent++;
            UploadPacket up = new UploadPacket(dataSegment,dataOffset,data);
            unackPackets.addElement( up );
        }
    }
    /*------------------------------------------------------------------------*/
    /* STANDARD DATA                                                          */
    /*------------------------------------------------------------------------*/
    private void sendDataFrame() {
    /*------------------------------------------------------------------------*/
        timeDataSent = (long)0;
        isDataAcked = false;
        if( data != null ) {
            srvLnk.sendPacketToSpacecraft( dataFrame );
            isDataSent = true;
            timeDataSent = System.currentTimeMillis();
            nbDataSent++;
        }
    }
    /*------------------------------------------------------------------------*/
    /* EOF                                                                    */
    /*------------------------------------------------------------------------*/
    private void sendEOF() {
    /*------------------------------------------------------------------------*/
        timeEOFSent = (long)0;
        isEOFAcked = false;
        if( EOF != null ) {
            srvLnk.sendPacketToSpacecraft( EOF );
            timeEOFSent = System.currentTimeMillis();            
            nbEOFSent++;
        }
    }
    /*------------------------------------------------------------------------*/
    /* CREATE TASK                                                            */
    /*------------------------------------------------------------------------*/
    private void sendCreateTask() {
    /*------------------------------------------------------------------------*/
        timeCreateTaskSent = (long)0;
        isCreateTaskAcked = false;
        if( createTask != null ) {
            srvLnk.sendPacketToSpacecraft( createTask );
            timeCreateTaskSent = System.currentTimeMillis();
            nbCreateTaskSent++;
        }
    }    
    

    /*========================================================================*/
    /*                   G E T    R E P L I E S                               */
    /*========================================================================*/
    /*------------------------------------------------------------------------*/
    /* LOAD REQUEST                                                           */
    /*------------------------------------------------------------------------*/
    private void getLoadRequestReply() {
    /*------------------------------------------------------------------------*/        
        frame =  srvLnk.getSpacecraftFrameDestinedTo(ldrFramesAddress);
        if( frame != null ) {
            byte[] datax = frame.getInfoBytes();
            if( datax[0] == 19 ) {
                /*---------------------------------------*/
                /* Received an ERROR: only possible with */
                /* CRC error in this case...             */
                /*---------------------------------------*/ 
                nbLoadRequestAckErrors++;
                isLoadRequestError = true;
                status = "CHECKSUM ERROR ON LOAD REQUEST";
            }
            else if( datax[0] == 15 ) {
                /*--------------------------------------------*/
                /* received an ACK: now we must still wait for*/
                /* the LOAD char and the address...           */
                /*--------------------------------------------*/
                isLoadRequestAcked = true;
                isLoadRequestError = false;
            }
            else if( datax[0] == 24 ) {
                /*----------------------------------------------*/
                /* Received the LOAD char. Now wait for address */
                /*----------------------------------------------*/
                isLoadReadyReceived = true;                        
            }
            else if( isLoadReadyReceived == true ) {
                /*--------------------------------------*/
                /* Assuming that now its the address... */
                /*--------------------------------------*/
                String receivedData = "";
                receivedData = new String(datax,0,datax.length);
                loadAddress += receivedData;
                if( loadAddress.length() >= 4 ) {
                    isLoadAdressReceived = true;                                
                    segment = 0;
                    try{
                        segment = Integer.parseInt( loadAddress, 16 );
                        isLoadAdressValid    = true;
                        status = "ADRESS: "+loadAddress;
                    } catch( NumberFormatException nfe ) {
                        status = "Bad segment address: "+loadAddress;                        
                    }
                } else {
                    addressWasInSeveralChunks = true;
                }
            }
        } //end if a loader frame is waiting            
    }
    
    /*------------------------------------------------------------------------*/
    /* SET SEGMENT                                                            */
    /*------------------------------------------------------------------------*/
    private void getSetSegmentReply() {
    /*------------------------------------------------------------------------*/
        /*-------------------------------------------*/
        /* Check to see if a loader frame was caught */
        /*-------------------------------------------*/
        frame = srvLnk.getSpacecraftFrameDestinedTo(ldrFramesAddress);
        if( frame != null ) {
            /*----------------------------------------------------*/
            /* If so, get the frame and remove it from the vector */
            /*----------------------------------------------------*/
            byte[] datax = frame.getInfoBytes();
            if( datax[0] == 19 ) {
                /*---------------------------------------*/
                /* Received an ERROR: only possible with */
                /* CRC error in this case...             */
                /*---------------------------------------*/
                nbSetSegmentAckErrors++;
                isSetSegmentError = true;
                status = "CHECKSUM ERROR ON SET SEGMENT";                        
            }
            else if( datax[0] == 15 ) {
                /*----------------------------------*/
                /* received an ACK: we can continue */
                /*----------------------------------*/
                isSetSegmentAcked = true;
                isSetSegmentOk = true;
            }
        } // if a loader frame is waiting
        
    }
    
    /*------------------------------------------------------------------------*/
    /* EOF                                                                    */
    /*------------------------------------------------------------------------*/
    private void getEOFReply() {
    /*------------------------------------------------------------------------*/
        frame = srvLnk.getSpacecraftFrameDestinedTo("LDR-0");
        if( frame != null ) {
            byte[] datax = frame.getInfoBytes();
            if( datax[0] == 19 ) {
                /*---------------------------------------*/
                /* Received an ERROR: only possible with */
                /* CRC error in this case...             */
                /*---------------------------------------*/
                nbEOFAckErrors++;
                isEOFError = true;
                return;
            }
            else if( datax[0] == 11 ) {
                /*-----------------------------------------*/
                /* received the TASK char: we can continue */
                /* and read the task number now...         */
                /*-----------------------------------------*/
                isTaskFlagReceived = true;
            }
            else if( isTaskFlagReceived ) {
                String taskNumberString = "";
                try{
                    taskNumberString = new String(datax,0,datax.length);
                    accumulatedTaskNumberString += taskNumberString;
                    if( accumulatedTaskNumberString.length() >= 2 ) {
                       isTaskNumberReceived = true;
                       taskNumber = 
                            Integer.parseInt(accumulatedTaskNumberString,16);
                       //System.out.println("Task number="+taskNumber);
                       isEOFAcked = true;
                       isEOFOk = true;                       
                    }
                } catch( NumberFormatException nfe ) {}
            }
        }
    }
    
    /*------------------------------------------------------------------------*/
    /* CREATE TASK                                                            */
    /*------------------------------------------------------------------------*/
    private void getCreateTaskReply() {
    /*------------------------------------------------------------------------*/
        frame = srvLnk.getSpacecraftFrameDestinedTo("LDR-0");
        if( frame != null ) {
            byte[] datax = frame.getInfoBytes();
            if( datax[0] == 19 ) {
                /*---------------------------------------*/
                /* Received an ERROR: only possible with */
                /* CRC error in this case...             */
                /*---------------------------------------*/
                nbCreateTaskErrors++;                
                isCreateTaskError = true;
                return;
            }
            else if( datax[0] == 15 ) {
                /*----------------------------------*/
                /* received an ACK: we can continue */
                /* the LOAD char and the address... */
                /*----------------------------------*/
                isCreateTaskAcked = true;
                isCreateTaskOk = true;
            }
        } 
    }
    
    /*------------------------------------------------------------------------*/
    /* DATA                                                                   */
    /*------------------------------------------------------------------------*/
    private void getDataReply() {
    /*------------------------------------------------------------------------*/
        /*-------------------------------------------------*/
        /* Check all frames sent back by the flight loader */
        /*-------------------------------------------------*/
        while( srvLnk.isSpacecraftFrameAvailable("LDR-0") ) {

            frame = srvLnk.getSpacecraftFrameDestinedTo("LDR-0");
            byte[] datax = frame.getInfoBytes();

            if( datax[0] == 19 ) {
                /*---------------------------------------*/
                /* Received an ERROR: only possible with */
                /* CRC error in this case...             */
                /*---------------------------------------*/
                nbDataAckErrors++;
                consecutiveGood = 0;
                break;
            }
            else if( datax[0] == 15 ) {
                /*----------------------------------*/
                /* received an ACK: we can continue */
                /*----------------------------------*/
                consecutiveCRCErrors = 0;
                isDataAcked = true;

                /*-----------------------------------------*/
                /* If this is the extended proto, then we  */
                /* must remove the acknoledged packet from */
                /* the unack vector...                     */
                /*-----------------------------------------*/
                if( isExtendedLoadProto && (datax.length>4) ) {
                    int ackSeg = (datax[1]<<8)+(int)(datax[2]&0xFF);
                    int ackOff = (datax[3]<<8)+(int)(datax[4]&0xFF);
                    boolean matchedAck = false;
                    for( int i=0; i<unackPackets.size(); i++ ) {
                        UploadPacket up = 
                                (UploadPacket)unackPackets.elementAt(i);                                        
                        if((up.segment==ackSeg)&&(up.offset==ackOff)){
                            unackPackets.remove( up );
                            matchedAck = true;
                            break;
                        }
                    }
                    //if( !matchedAck )
                    //    System.out.println("Un-matched ACK at ["+ackSeg+":"+ackOff+"]");
                    //else
                        //System.out.println("ACK for ["+ackSeg+":"+ackOff+"]");
                }
            }

            /*-------------------*/
            /* Let others breath */
            /*-------------------*/
            try{Thread.currentThread().sleep(5);}
            catch( InterruptedException ie ) {}

        } // wend a loader frame is waiting
    }
    
    /*========================================================================*/
    /*                          T I M E O U T S                               */
    /*========================================================================*/
    /*------------------------------------------------------------------------*/
    /* LOAD REQUEST                                                           */
    /*------------------------------------------------------------------------*/
    private boolean isLoadRequestTimeout() {
    /*------------------------------------------------------------------------*/
        long elapsedTime = System.currentTimeMillis() - timeLoadReqSent;
        return((elapsedTime<0) || (elapsedTime>LOAD_REQUEST_TIMEOUT));
    }
    /*------------------------------------------------------------------------*/
    /* SET SEGMENT                                                            */
    /*------------------------------------------------------------------------*/
    private boolean isSetSegmentTimeout() {
    /*------------------------------------------------------------------------*/
        long elapsedTime = System.currentTimeMillis() - timeSetSegmentSent;
        return((elapsedTime<0) || (elapsedTime>SET_SEGMENT_TIMEOUT));
    }
    /*------------------------------------------------------------------------*/
    /* DATA                                                                   */
    /*------------------------------------------------------------------------*/
    private boolean isDataTimeout() {
    /*------------------------------------------------------------------------*/
        long elapsedTime = System.currentTimeMillis() - timeDataSent;
        if((elapsedTime<0) || (elapsedTime>DATA_TIMEOUT)) {
            nbDataAckTimeouts++;
            nbDataAckErrors++;
            consecutiveGood = 0;
            return( true );
        }
        return( false );
    }
    /*------------------------------------------------------------------------*/
    /* EOF                                                                    */
    /*------------------------------------------------------------------------*/
    private boolean isEOFTimeout() {
    /*------------------------------------------------------------------------*/
        long elapsedTime = System.currentTimeMillis() - timeEOFSent;
        return((elapsedTime<0) || (elapsedTime>EOF_TIMEOUT));
    }
    /*------------------------------------------------------------------------*/
    /* CREATE TASK                                                            */
    /*------------------------------------------------------------------------*/
    private boolean isCreateTaskTimeout() {
    /*------------------------------------------------------------------------*/
        long elapsedTime = System.currentTimeMillis() - timeCreateTaskSent;
        return((elapsedTime<0) || (elapsedTime>CREATE_TASK_TIMEOUT));
    }    
    
    
    /*========================================================================*/
    /*                             U T I L                                    */
    /*========================================================================*/
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private void ifPausedThenWaitHere() {
    /*------------------------------------------------------------------------*/
        while( isPaused && !cancelLoad ) {
            try{Thread.currentThread().sleep(1000);}
            catch(InterruptedException ie){}
        }
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private void getNextDataBlockForExtendedProtocole() {
    /*------------------------------------------------------------------------*/
        /*--------------------------------------------------------*/
        /* If this is the first block, then it is the task header */
        /*--------------------------------------------------------*/
        if( dataOffsetAbsolute == (long)0 ) {
            dataOffset = nextDataOffset;
            dataSegment = nextDataSegment;
            dataOffsetAbsolute = nextDataOffsetAbsolute;
            data = scosTaskHeader;
            nextDataOffset += data.length;
            nextDataOffsetAbsolute += data.length;
            if( nextDataOffset >= (int)0x8000 ) {                                
                nextDataOffset -= (int)0x8000;
                nextDataSegment += (int)0x0800;
                //System.out.println("Changing segment to "+segment);
            }
        /*------------------------------------------------------------*/
        /* If another block during the first pass, then get data from */
        /* binary stream...                                           */
        /*------------------------------------------------------------*/
        } else if( !timeToResendUnackPackets ) {
            dataOffset = nextDataOffset;
            dataSegment = nextDataSegment;
            dataOffsetAbsolute = nextDataOffsetAbsolute;
            if( binaryImageInputStream.available() >= dataChunkSize )
                data = new byte[dataChunkSize];
            else
                data = new byte[ binaryImageInputStream.available() ];        
            try {
                binaryImageInputStream.read(data);            
            } catch( IOException ioe ) {
                log.error("Problem reading task binary");
                data = null;
                return;
            }
            nextDataOffset += data.length;
            nextDataOffsetAbsolute += data.length;
            if( nextDataOffset >= (int)0x8000 ) {                                
                nextDataOffset -= (int)0x8000;
                nextDataSegment += (int)0x0800;
                //System.out.println("Changing segment to "+segment);
            }
        /*-------------------------------------------------------------*/
        /* If another block during the second pass, then get data from */
        /* unacknowledged block vector....                             */
        /*-------------------------------------------------------------*/
        } else if( timeToResendUnackPackets ) {
            if( unackPackets.size() <= 0 ) {
                /*-----------------------------------------*/
                /* COMPLETED AN UPLOAD WITH EXTENDED PROTO */
                /*-----------------------------------------*/
                isTaskBinaryOk = true;
                return;
            }
            if( retryIndex >= unackPackets.size() )
                retryIndex = 0;                        
            UploadPacket unacked=(UploadPacket)unackPackets.elementAt(retryIndex);
            retryIndex++;
            data = unacked.data;
            dataSegment = unacked.segment;
            dataOffset = unacked.offset;
            nextDataOffset = dataOffset + data.length;            
            //System.out.println("RE-attempting data at ["+segment+":"+offset+"]");
        }
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private void getNextDataBlockForStandardProtocole() {
    /*------------------------------------------------------------------------*/
        dataOffset = nextDataOffset;
        dataSegment = nextDataSegment;
        dataOffsetAbsolute = nextDataOffsetAbsolute;
        if( dataOffsetAbsolute == (long)0 ) {
            data = scosTaskHeader;
        } else {
            if( binaryImageInputStream.available() >= dataChunkSize )
                data = new byte[dataChunkSize];
            else
                data = new byte[ binaryImageInputStream.available() ];
            try {
                binaryImageInputStream.read(data);
            } catch( IOException ioe ) {
                log.error("Problem reading binary image: "+ioe);
                data = null;
                return;
            }
        }
        nextDataOffset += data.length;
        nextDataOffsetAbsolute += data.length;
    }
   
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private void adjustDataSizeIfNecessary() {
    /*------------------------------------------------------------------------*/
        if((consecutiveCRCErrors>2)&&isDynamicPacketSize){
            dataChunkSize -= DATA_SIZE_INCREMENT;
            if( dataChunkSize < MIN_DATA ) dataChunkSize = MIN_DATA;
            consecutiveCRCErrors = 0;
        }
    }
    /*------------------------------------------------------------------------*/
    /* Update the progress                                                    */
    /*------------------------------------------------------------------------*/
    private void updateUploadProgress() {
        double progress = 0;
        if( !timeToResendUnackPackets ) {
            progress = (double)
                    ((double)dataOffsetAbsolute*100.0)/((double)binSize*100.0);
            progress = progress * 100.0;
        }
        else {
            progress = (double)
            ((double)(numberOfUnackedAtFirstPass-unackPackets.size())*100.0)
                /((double)numberOfUnackedAtFirstPass*100.0);
            progress = progress * 100.0;
        }
        uploadProgress = (int)progress;
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private void checkForExtendedProtocoleSecondPassConditions() {
    /*------------------------------------------------------------------------*/
        if( !timeToResendUnackPackets ) {
            if( nextDataOffsetAbsolute >= binSize ) {
                numberOfUnackedAtFirstPass = unackPackets.size();
                waitXMilliSeconds(1000); //Then go for second pass...
                timeToResendUnackPackets = true;
            }
        }
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private void checkForExtendedProtocoleCompletionConditions() {
    /*------------------------------------------------------------------------*/
        if( timeToResendUnackPackets && (unackPackets.size()<=0) ) {
            isTaskBinaryOk = true;
            step = STEP_EOF;
            if( isSingleStepMode ) cancelLoad = true;
        }
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private void checkForStandardProtocoleCompletionConditions() {
    /*------------------------------------------------------------------------*/
        if( nextDataOffsetAbsolute >= binSize ) {
            isTaskBinaryOk = true;
            step = STEP_EOF;
            if( isSingleStepMode ) cancelLoad = true;
        }
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private void waitXMilliSeconds( int mSec ) {
    /*------------------------------------------------------------------------*/
        try{Thread.currentThread().sleep(mSec);}
        catch( InterruptedException ie ) {}
    }
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private boolean weAreNotReadyToProceed() {
    /*------------------------------------------------------------------------*/
        /*-------------------------*/
        /* Verify error conditions */
        /*-------------------------*/
        if( !readyToLoad ) {
            status="Unable to start a load: Local file not processed correctly";
            log.warning(status);
            return( true );
        }
        if( srvLnk==null ) {
            status = "Unable to start a load: No link to ground station server";
            log.error(status);
            return( true );
        }
        if( sat==null ) {
            status = "Unable to start a load: No satellite definition";
            log.error(status);
            return( true );
        }
        return( false );
    }        
    
    /*------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private void processTaskBinary() {
    /*------------------------------------------------------------------------*/
        
        if( scosTaskHeader == null ) return;
        
        /*---------------------------------------------------*/
        /* Put the size of the binary the first two bytes,   */
        /* lsb first.                                        */
        /*---------------------------------------------------*/
        scosTaskHeader[0] = (byte)(binSize & 0x000000ff);
        scosTaskHeader[1] = (byte)((binSize & 0x0000ff00)>>8);
        scosTaskHeader[2] = (byte)((binSize & 0x00ff0000)>>16);;
        scosTaskHeader[3] = (byte)((binSize & 0xff000000)>>32);;
                        
        /*-----------------------------------------------*/
        /* The task number will be assigned later....    */
        /* when we'll get the answer back from the START */
        /* TASK command.                                 */
        /*-----------------------------------------------*/
        scosTaskHeader[4] = 0;

        /*-----------------------------------*/
        /* The next byte is reserved by SCOS,*/
        /* so we do not change it            */
        /*-----------------------------------*/
        scosTaskHeader[5] = scosTaskHeader[5];  //OK, this was not
                                                //necessary but it
                                                //is clearer.....!

        /*---------------------------------------------------*/
        /* The next word is the task id. I DO NOT KNOW WHAT  */
        /* TO DO WITH IT!!                                   */
        /*---------------------------------------------------*/
        scosTaskHeader[6] = scosTaskHeader[6];
        scosTaskHeader[7] = scosTaskHeader[7];

        /*------------------------------------------------*/
        /* Then we have the task priority. This is set at */
        /* build time so do not change it...              */
        /*------------------------------------------------*/
        taskPriority = String.valueOf( scosTaskHeader[8] );

        /*--------------------------------------------*/
        /* Then a debug flag, which is not documented */
        /*--------------------------------------------*/
        scosTaskHeader[9] = scosTaskHeader[9];

        /*-------------------------------------------------*/
        /* This is the command vector (what is it???)      */
        /*-------------------------------------------------*/
        scosTaskHeader[10] = scosTaskHeader[10];
        scosTaskHeader[11] = scosTaskHeader[11];
        scosTaskHeader[12] = scosTaskHeader[12];
        scosTaskHeader[13] = scosTaskHeader[13];

        /*---------------------------------------------------*/
        /* Then the init vector:                             */
        /* the segment (received after the load command, and */
        /* the instruction pointer, found in the exe header. */
        /*---------------------------------------------------*/
        scosTaskHeader[14] = scosTaskHeader[14]; //We should verify
        scosTaskHeader[15] = scosTaskHeader[15]; //with exeInitIP...
        scosTaskHeader[16] = (byte)(segment&0x00ff);
        scosTaskHeader[17] = (byte)(((segment&0xff00)>>8)&0xff);

        /*-----------------------------------------------------*/
        /* For the data and stack segments, we have to add the */
        /* relative value that was already found in the binary */
        /* exe header with the absolute base segment address   */
        /*-----------------------------------------------------*/
        int lsb, msb= 0;
                        
        /* THERE MUST BE A BETTER WAY!!!!!!!!!!!!!*/
        if( (scosTaskHeader[18] & 0x80) > 0 )
            lsb = (((byte)scosTaskHeader[18]&0x7F) | 0x0080 );
        else    lsb = scosTaskHeader[18];

        if( (scosTaskHeader[19] & 0x80) > 0 )
            msb = (((byte)scosTaskHeader[19]&0x7F) | 0x0080 );
        else    msb = scosTaskHeader[19];

        int dataSegment = (lsb | (msb<<8));
        //System.out.println("Relative data segment:"+dataSegment);
        dataSegment = dataSegment + segment;
        //System.out.println("Absolute data segment:"+dataSegment);
        scosTaskHeader[18] = (byte)(dataSegment & 0x00ff);
        scosTaskHeader[19] = (byte)((dataSegment & 0xff00)>>8);

        if( (scosTaskHeader[20] & 0x80) > 0 )
            lsb = (((byte)scosTaskHeader[20]&0x7F) | 0x0080 );
        else    lsb = scosTaskHeader[20];

        if( (scosTaskHeader[21] & 0x80) > 0 )
            msb = (((byte)scosTaskHeader[21]&0x7F) | 0x0080 );
        else    msb = scosTaskHeader[21];
        int stackSegment = (lsb | (msb<<8));
        //System.out.println("Relative stack segment:"+stackSegment);
        stackSegment = stackSegment + segment;
        //System.out.println("Absolute stack segment:"+stackSegment);
        scosTaskHeader[20] = (byte)(stackSegment & 0x00ff);
        scosTaskHeader[21] = (byte)((stackSegment & 0xff00)>>8);
                        
        /*------------------------------------------------------*/
        /* The stack pointer is already OK in the exe header... */
        /*------------------------------------------------------*/
        scosTaskHeader[22] = scosTaskHeader[22];
        scosTaskHeader[23] = scosTaskHeader[23];

        /*------------------------------------------------------*/
        /* The task ressource block pointer is not our business */
        /*------------------------------------------------------*/
        scosTaskHeader[24] = scosTaskHeader[24];
        scosTaskHeader[25] = scosTaskHeader[25];

        /*---------------------------------------*/
        /* nor the task header extension pointer */
        /*---------------------------------------*/
        scosTaskHeader[26] = scosTaskHeader[26];
        scosTaskHeader[27] = scosTaskHeader[27];

        /*-------------------------*/
        /* nor the utility pointer */
        /*-------------------------*/
        scosTaskHeader[28] = scosTaskHeader[28];
        scosTaskHeader[29] = scosTaskHeader[29];
        scosTaskHeader[30] = scosTaskHeader[30];
        scosTaskHeader[31] = scosTaskHeader[31];
                        
        /*------------------------------------*/
        /* Here we put the task name (8 char) */
        /*------------------------------------*/
        taskName = taskName.toUpperCase();
        //System.out.println("Task Name: "+taskName);
        if( taskName.length() <= 8 ) {
            taskName.getBytes(  0,
                                taskName.length(),
                                scosTaskHeader,
                                32);
        } else {
            taskName.getBytes(  taskName.length()-9,
                                taskName.length(),
                                scosTaskHeader,
                                32);
        }
                        
        /*---------------------------------------------------------*/
        /* And finally the command line (first the length, max=80).*/
        /* Get the command line and truncate if more than 80 chars */
        /*---------------------------------------------------------*/
        String strCommandLine = fileName + " " + commandLine;
        strCommandLine = strCommandLine.toUpperCase();
        //System.out.println("Command line: "+strCommandLine);
        if( strCommandLine.length() > 80 ) {
            System.out.println("ERROR: COMMAND LINE TOO LONG!");
            strCommandLine = strCommandLine.substring(0,79);
        }
        //Do not forget to add one to put the trailing zero...
                        
        //Replace spaces by zeros....
        byte[] byteCommandLine = strCommandLine.getBytes();

        scosTaskHeader[40] = (byte)(byteCommandLine.length+2);

        for( int i=0; i<byteCommandLine.length; i++ ) {
            if( byteCommandLine[i] == 0x20 ) {
                byteCommandLine[i] = 0;
            }
            scosTaskHeader[42+i] = byteCommandLine[i];
        }
                        
        /*---------------------------------------------------*/
        /* Before sending data we must load the binary into  */
        /* memory and take care of relocation                */
        /* Note that some part of the relocation has already */
        /* been taken care of in the SCOS header (first 122  */
        /* bytes of the binary image...)                     */
        /*---------------------------------------------------*/        
        try{ 
            fis = new FileInputStream (fileName);
            binaryImage = new byte[fis.available()];
            fis.read(binaryImage); 
        } catch( IOException ioe ) {
            log.warning("Unable to read binary image: "+ioe);
            binaryImage = null;
        } finally {
            try{fis.close();}catch(IOException e){}
        }
                        
        /*-----------------------------------------------------*/
        /* Since all relocation is relative to start of binary */
        /* create a new pointer...                             */
        /* (yes, I'm using way more memory than I should, but  */
        /* who cares...)                                       */
        /*-----------------------------------------------------*/
        int nextReadWithinBinary = 0;

        /*--------------------------*/
        /* Go through every byte... */
        /*--------------------------*/
        for( ; nextReadWithinBinary < binaryImage.length ;nextReadWithinBinary++ ){

            /*----------------------------------------*/
            /* Check every relocation item address... */
            /*----------------------------------------*/
            for( int i=0; i<relocItemAddress.length; i++ ) {

                /*--------------------------------------------------*/
                /* Here we have not to forget that our binary array */
                /* actually starts at offset 122 since we skipped   */
                /* the SCOS header. This explains the "-122"        */
                /*--------------------------------------------------*/
                if( nextReadWithinBinary == (relocItemAddress[i]-122) ) {
                    //System.out.print(   "At "
                    //+ Integer.toString(nextReadWithinBinary,16)
                    //+ " relocating " );

                    /*----------------------------------------*/
                    /* THERE MUST BE A BETTER WAY!!!!!!!!!!!!!*/
                    /* to play with signed bytes...           */
                    /*----------------------------------------*/
                    lsb=0;
                    msb=0;

                    /*---------------------------------------------*/
                    /* Relocate (add the "real" segment address to */
                    /* the address in the binary...)               */
                    /*---------------------------------------------*/
                    if( (binaryImage[nextReadWithinBinary] & 0x80) > 0 )
                        lsb = (((byte)binaryImage[nextReadWithinBinary]&0x7F) | 0x0080 );
                    else    lsb = binaryImage[nextReadWithinBinary];

                    if( (binaryImage[nextReadWithinBinary+1] & 0x80) > 0 )
                        msb = (((byte)binaryImage[nextReadWithinBinary+1]&0x7F) | 0x0080 );
                    else    msb = binaryImage[nextReadWithinBinary+1];

                    int segmentToRelocate = (lsb | (msb<<8));
                    //System.out.print(""+Integer.toString(segmentToRelocate,16));
                    int segmentRelocated = segmentToRelocate + segment;
                    //System.out.println(" to "+Integer.toString(segmentRelocated,16));
                    binaryImage[nextReadWithinBinary]   = (byte)(segmentRelocated & 0x00ff);
                    binaryImage[nextReadWithinBinary+1] = (byte)((segmentRelocated & 0xff00)>>8);
                }
            }

        }//end relocation
                        
        /*---------------------------------------------------------------*/
        /* Create a stream to read from the executable binary byte array */
        /*---------------------------------------------------------------*/
        if( binaryImage != null )
            binaryImageInputStream = new ByteArrayInputStream( binaryImage );
        else
            binaryImageInputStream = null;
        
    }
        
    
    /*==================================================================*/
    /* Class to store unacknowledged packet while uploading a task with */
    /* the new extended protocole                                       */
    /*==================================================================*/
    public class UploadPacket {
    /*==================================================================*/    
        public int segment;
        public int offset;
        public byte[] data;
        public UploadPacket(int segmentToUse,int offsetToUse,byte[] dataToUse){
            segment = segmentToUse;
            offset = offsetToUse;
            data = dataToUse;
        }
    }
    
}
