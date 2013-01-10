package ca.gc.space.quicksat.ground.control;

/*===Bootloader.java==========================================================*/
/**
 * Class used to talk to spacecraft's bootloader. Implements the
 * data encapsulation.
 *
 * @author Jean-Francois Cusson for the Canadian Space Agency
 * @version 1.2
 * @since Created on May 23, 2001, 3:45 PM
  *
==============================================================================*/
public class Bootloader extends Object {
/*============================================================================*/

/*---------------*/    
/* Spacecraft ID */
/*---------------*/
int spacecraftID = 0x00;    

/*------------------*/
/* Encryption Keys  */
/*------------------*/
int AVAL = 0;
int BVAL = 0;
int D3VAL= 0;
int K1H  = 0;
int K1L  = 0;

/*----------------------------------------*/
/* Command Codes (defaults to QuickSat's) */
/*----------------------------------------*/
//int cmdMove     = 0x00BFB;      //Move Kernal/PHT
//int cmdExecute  = 0x0476E;      //Execute loaded program
//int cmdBeacon   = 0x00D40;      //Request beacon
//int cmdPeekMem  = 0x05281;      //Examine memory
//int cmdPeekIO   = 0x0E255;      //Examine IO port
//int cmdPokeMem  = 0x0E5A3;      //Write memory
//int cmdPokeIO   = 0x0448C;      //Write IO port
//int cmdDumpMem  = 0x06880;      //Dump memory content
//int cmdLoad     = 0x03E0B;      //Load a file

public int cmdMove     = 0;      //Move Kernal/PHT
public int cmdExecute  = 0;      //Execute loaded program
public int cmdBeacon   = 0;      //Request beacon
public int cmdPeekMem  = 0;      //Examine memory
public int cmdPeekIO   = 0;      //Examine IO port
public int cmdPokeMem  = 0;      //Write memory
public int cmdPokeIO   = 0;      //Write IO port
public int cmdDumpMem  = 0;      //Dump memory content
public int cmdLoad     = 0;      //Load a file
public int cmd1        = 0;
public int cmd2        = 0;



    /*========================================================================*/
    /** Creates new Bootloader                                                
     * Class used to talk to spacecraft's bootloader. Implements the
     * communication protocole and data encapsulation.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @since Beginning
     =========================================================================*/
    public Bootloader() {
    /*========================================================================*/    
    }

    /*=DATAPACKET=============================================================*/
    /** Creates a data packet understandable by the bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code:<ul>
     *  <li>cmdMove (Move Kernal/PHT)
     *  <li>cmdExecute (Execute loaded program)
     *  <li>cmdBeacon (Request beacon)
     *  <li>cmdPeekMem (Examine memory)
     *  <li>cmdPeekIO (Examine IO port)
     *  <li>cmdPokeMem (Write memory)
     *  <li>cmdPokeIO (Write IO port)
     *  <li>cmdDumpMem (Dump memory content)
     *  <li>cmdLoad (Load a file)
     *  </ul>
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/
    public byte[] dataPacket( int cmd ) {
    /*========================================================================*/    
        return createDataPacket( cmd, 0, 0, 0, 0 );
    }
    
    /*=DATAPACKET=============================================================*/
    /** Creates a data packet understandable by the bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code:<ul>
     *  <li>cmdMove (Move Kernal/PHT)
     *  <li>cmdExecute (Execute loaded program)
     *  <li>cmdBeacon (Request beacon)
     *  <li>cmdPeekMem (Examine memory)
     *  <li>cmdPeekIO (Examine IO port)
     *  <li>cmdPokeMem (Write memory)
     *  <li>cmdPokeIO (Write IO port)
     *  <li>cmdDumpMem (Dump memory content)
     *  <li>cmdLoad (Load a file)
     *  </ul>
     * @param param1 First parameter to the command (specific to it)
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/
    public byte[] dataPacket( int cmd, int param1 ) {
    /*========================================================================*/    
        return createDataPacket( cmd, param1, 0, 0, 0 );
    }
    
    /*=DATAPACKET=============================================================*/
    /** Creates a data packet understandable by the bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code:<ul>
     *  <li>cmdMove (Move Kernal/PHT)
     *  <li>cmdExecute (Execute loaded program)
     *  <li>cmdBeacon (Request beacon)
     *  <li>cmdPeekMem (Examine memory)
     *  <li>cmdPeekIO (Examine IO port)
     *  <li>cmdPokeMem (Write memory)
     *  <li>cmdPokeIO (Write IO port)
     *  <li>cmdDumpMem (Dump memory content)
     *  <li>cmdLoad (Load a file)
     *  </ul>
     * @param param1 First parameter to the command (specific to it)
     * @param param2 Second parameter to the command (specific to it)
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/
    public byte[] dataPacket( int cmd, int param1, int param2 ) {
    /*========================================================================*/    
        return createDataPacket( cmd, param1, param2, 0, 0 );
    }
    
    /*=DATAPACKET=============================================================*/
    /** Creates a data packet understandable by the bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code:<ul>
     *  <li>cmdMove (Move Kernal/PHT)
     *  <li>cmdExecute (Execute loaded program)
     *  <li>cmdBeacon (Request beacon)
     *  <li>cmdPeekMem (Examine memory)
     *  <li>cmdPeekIO (Examine IO port)
     *  <li>cmdPokeMem (Write memory)
     *  <li>cmdPokeIO (Write IO port)
     *  <li>cmdDumpMem (Dump memory content)
     *  <li>cmdLoad (Load a file)
     *  </ul>
     * @param param1 First parameter to the command (specific to it)
     * @param param2 Second parameter to the command (specific to it)
     * @param param3 Third parameter to the command (specific to it)
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/
    public byte[] dataPacket( int cmd, int param1, int param2, int param3 ) {
    /*========================================================================*/    
        return createDataPacket( cmd, param1, param2, param3, 0 );
    }
    
    /*=DATAPACKET=============================================================*/
    /** Creates a data packet understandable by the bootloader.
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param cmd command code:<ul>
     *  <li>cmdMove (Move Kernal/PHT)
     *  <li>cmdExecute (Execute loaded program)
     *  <li>cmdBeacon (Request beacon)
     *  <li>cmdPeekMem (Examine memory)
     *  <li>cmdPeekIO (Examine IO port)
     *  <li>cmdPokeMem (Write memory)
     *  <li>cmdPokeIO (Write IO port)
     *  <li>cmdDumpMem (Dump memory content)
     *  <li>cmdLoad (Load a file)
     *  </ul>
     * @param param1 First parameter to the command (specific to it)
     * @param param2 Second parameter to the command (specific to it)
     * @param param3 Third parameter to the command (specific to it)
     * @param param4 Fourth parameter to the command (specific to it)
     * @return complete data packet in a byte array
     * @since Beginning
    ==========================================================================*/
    public byte[] dataPacket( int cmd, int param1, int param2, int param3, int param4 ) {
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
    public synchronized byte[] createDataPacket(int cmd,int param1,int param2,int param3,int param4) {
    /*========================================================================*/    
    int[] qblWords = new int[7];
    int random;      
    
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
        qblFrame[0] = (byte)spacecraftID;
        
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
    public String interpretReply( byte[] message, String type, int EDACCounter, int data ) {
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
    ==========================================================================*/       
    public void setEncryptionKeys( int aval, int bval, int d3val, int k1h, int k1l ) {
    /*========================================================================*/
        AVAL    = aval;
        BVAL    = bval;
        D3VAL   = d3val;
        K1H     = k1h;
        K1L     = k1l;
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
    public void setCommandCodes(    int load, int dump, int exec, int meme,
                                    int memw, int ioe, int iop, int tlm,
                                    int mov, int cmd1, int cmd2 ) {
    /*========================================================================*/
        cmdMove     = mov;       //Move Kernal/PHT
        cmdExecute  = exec;      //Execute loaded program
        cmdBeacon   = tlm;       //Request beacon
        cmdPeekMem  = meme;      //Examine memory
        cmdPeekIO   = ioe;       //Examine IO port
        cmdPokeMem  = memw;      //Write memory
        cmdPokeIO   = iop;       //Write IO port
        cmdDumpMem  = dump;      //Dump memory content
        cmdLoad     = load;      //Load a file
        this.cmd1   = cmd1;
        this.cmd2   = cmd2;
    }
    
    /*=SETSCID================================================================*/
    /** Sets the bootloader spacecraft ID
     *
     * @author Jean-Francois Cusson, for the Canadian Space Agency
     * @version 1.2
     * @param scid Spacecraft ID to use
    ==========================================================================*/       
    public void setSCID( int scid ) {
    /*========================================================================*/
        spacecraftID = scid;
    }
    
    
}
