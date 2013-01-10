/*
 * PHT.java
 *
 * Created on May 25, 2001, 2:44 PM
 */

package ca.gc.space.quicksat.ground.control;

import java.util.*;
import java.io.*;
import javax.swing.*;
import ca.gc.space.quicksat.ground.client.*;
import ca.gc.space.quicksat.ground.radio.*;

/**
 *
 * @author  jfcusson
 * @version 
 */
/*============================================================================*/
/* CLASS: PHT                                                                 */
/*============================================================================*/
public class PHT extends Object {

/*-----------------------------------------------*/    
/* Verbose: to get an idea of what's going on... */
/*-----------------------------------------------*/
static boolean verbose = false;    

/*------------------------------------------------------------------*/
/* CommandPrime: to know if we should send prime or backup commands */
/*------------------------------------------------------------------*/
static boolean commandPrime = true;
    
/*----------------------------------------*/    
/* Spacecraft ID (defaults to QuickSat's) */
/*----------------------------------------*/
static int spacecraftID = 0x01;    

/*---------------------------------------*/
/* Command strings (default to QuickSat) */
/*---------------------------------------*/
//static String commandStringMagicCode       = "mnoziercsacsa COMMND";   //Security?!?!
static String commandStringMagicCode       = "";
static String commandStringHeader          = "CCC";
static final int paramString1Char          = 1;
static final int paramString2Chars         = 2;
static final int paramString3Chars         = 3;
static final int paramString4Chars         = 4;
/*..........................................*/
static String cmdGeneric                   = "";
/*..........................................*/
static String cmdCrashToBootloader         = "CCC";
/*..........................................*/
static String cmdRequestAcknowledgement    = "AAA";
/*..........................................*/
static String cmdPayload                   = "333";
static     String payload_OFF_Primary      = "0";
static     String payload_ON_Primary       = "1";
static     String payload_OFF_Backup       = "2";
static     String payload_ON_Backup        = "3";
static     String payload_1_Activate       = "A";
static     String payload_2_Activate       = "B";
static     String payload_3_Activate       = "C";
static     String payload_4_Activate       = "D";
static     String payload_5_Activate       = "E";
static     String payload_6_Activate       = "F";
static     String payload_7_Activate       = "G";
static     String payload_8_Activate       = "H";
static     String payload_1_Deactivate     = "a";
static     String payload_2_Deactivate     = "b";
static     String payload_3_Deactivate     = "c";
static     String payload_4_Deactivate     = "d";
static     String payload_5_Deactivate     = "e";
static     String payload_6_Deactivate     = "f";
static     String payload_7_Deactivate     = "g";
static     String payload_8_Deactivate     = "h";
/*..........................................*/
static String cmdTXDelay                   = "BBB";
/*..........................................*/
static String cmdDigipeat                  = "DDD";
static     String digipeat_Activate        = "1";
static     String digipeat_Deactivate      = "0";
/*..........................................*/    
static String cmdModemA                    = "EEE";    
static String cmdModemA_SourceAnalog       = "EEESAA";
static String cmdModemA_SourceDigital      = "EEESDD";
static String cmdModemA_Offset             = "EEEO";
static String cmdModemA_Deviation          = "EEED";
static String cmdModemA_CTCSS              = "EEEC";
static String cmdModemA_Rate1              = "EEER1";  
static String cmdModemA_Rate2              = "EEER2";
static String cmdModemA_Rate3              = "EEER4";
static String cmdModemA_Rate4              = "EEER9";
static String cmdModemA_ScrambleON         = "EEEB1";
static String cmdModemA_ScrambleOFF        = "EEEB0";
/*..........................................*/
static String cmdModemB                    = "FFF";    
static String cmdModemB_SourceAnalog       = "FFFSAA";
static String cmdModemB_SourceDigital      = "FFFSDD";
static String cmdModemB_Offset             = "FFFO";
static String cmdModemB_Deviation          = "FFFD";
static String cmdModemB_CTCSS              = "FFFC";
static String cmdModemB_Rate1              = "FFFR11";  
static String cmdModemB_Rate2              = "FFFR22";
static String cmdModemB_Rate3              = "FFFR44";
static String cmdModemB_Rate4              = "FFFR99";
static String cmdModemB_ScrambleON         = "FFFB11";
static String cmdModemB_ScrambleOFF        = "FFFB00";
/*..........................................*/
static String cmdModems_Init               = "OOO";
/*..........................................*/
static String cmdTX1_ON                    = "YYY10";//SQ CUSTOM!!!!
static String cmdTX1_OFF                   = "YYY0"; //SQ CUSTOM!!!
static String cmdTX1_Pwr                   = "XXXA";
static String cmdTX1_SourceA               = "GGG1A";
static String cmdTX1_SourceB               = "GGG1B";
static String cmdTX2_ON                    = "YYYB";
static String cmdTX2_OFF                   = "YYYb";
static String cmdTX2_Pwr                   = "XXXB";
static String cmdTX2_SourceA               = "GGG2A";
static String cmdTX2_SourceB               = "GGG2B";

/*..........................................*/
static String cmdIO                        = "III";
static String cmdIO_ReadByte               = "IIIBR";
static String cmdIO_ReadWord               = "IIIWR";
static String cmdIO_WriteByte              = "IIIBP";
static String cmdIO_WriteWord              = "IIIWP";
/*..........................................*/
static String cmdRate                      = "RRR";
static String cmdRate_Telemetry            = "RRR1";
static String cmdRate_Status               = "RRR2";
static String cmdRate_Broadcast            = "RRR3";
/*..........................................*/
static String cmdTelemStandbye             = "LLL0";
static String cmdTelemOneShot              = "LLL1";
static String cmdTelemContinuous           = "LLL2";
static String cmdTelemDisable              = "LLL3";
static String cmdTelemOnReply              = "LLLR";
static String cmdTelemNotOnReply           = "LLLN";
static String cmdCrashHWAC                 = "LLLK";
/*..........................................*/
static String cmdEDAC                      = "MMM";
static String cmdEDAC_Read                 = "MMMR";
static String cmdEDAC_Write                = "MMMP";
/*..........................................*/
static String cmdPHTWakeupDelay            = "PPP";
/*..........................................*/
static String cmdSetBroadcastText          = "UUU";
/*..........................................*/
static String cmdChargeControl             = "777";
static String cmdChargeControl_DAC1        = "7771";
static String cmdChargeControl_DAC1_ON     = "7771YYY";
static String cmdChargeControl_DAC1_OFF    = "7771NNN";
static String cmdChargeControl_DAC2        = "7772";
static String cmdChargeControl_DAC2_ON     = "7772YYY";
static String cmdChargeControl_DAC2_OFF    = "7772NNN";
/*..........................................*/
static String cmdSolarPanel1Normal_Prime   = "88810";
static String cmdSolarPanel1Shutdown_Prime = "88811";
static String cmdSolarPanel1Normal_Backup  = "88812";
static String cmdSolarPanel1Shutdown_Backup= "88813";
static String cmdSolarPanel2Normal_Prime   = "88820";
static String cmdSolarPanel2Shutdown_Prime = "88821";
static String cmdSolarPanel2Normal_Backup  = "88822";
static String cmdSolarPanel2Shutdown_Backup= "88823";
/*..........................................*/
static String cmdWheel                     = "WWW";
static String cmdWheel_SetSpeed            = "WWWA";
static String cmdWheel_SetTorque           = "WWWB";
static String cmdWheel_Enable              = "WWWC";
static String cmdWheel_SetSpeedMode        = "WWWD";
static String cmdWheel_GetSpeed            = "WWWE";
static String cmdWheel_GetTorque           = "WWWF";
static String cmdWheel_GetTorqueError      = "WWWG";
static String cmdWheel_GetTemperature      = "WWWH";
static String cmdWheel_Get14V              = "WWWI";
static String cmdWheel_Get10V              = "WWWJ";
static String cmdWheel_Get5V               = "WWWK";
static String cmdWheel_Get8V               = "WWWL";
static String cmdWheel_GetAllTelemetry     = "WWWM";
static String cmdWheel_SetTorqueMode       = "WWWN";
static String cmdWheel_Disable             = "WWWO";
static String cmdWheel_Get14VI             = "WWWP";
/*..........................................*/
static String cmdTorqueRods                = "666";
static String cmdTorqueRodXA_ON            = "666XAYYY";
static String cmdTorqueRodXB_ON            = "666XBYYY";
static String cmdTorqueRodXA_OFF           = "666XANNN";
static String cmdTorqueRodXB_OFF           = "666XBNNN";
static String cmdTorqueRodYA_ON            = "666YAYYY";
static String cmdTorqueRodYB_ON            = "666YBYYY";
static String cmdTorqueRodYA_OFF           = "666YANNN";
static String cmdTorqueRodYB_OFF           = "666YBNNN";
static String cmdTorqueRodZA_ON            = "666ZAYYY";
static String cmdTorqueRodZB_ON            = "666ZBYYY";
static String cmdTorqueRodZA_OFF           = "666ZANNN";
static String cmdTorqueRodZB_OFF           = "666ZBNNN";
static String cmdTorqueRodXA_DutyCycle     = "666XA";
static String cmdTorqueRodXB_DutyCycle     = "666XB";
static String cmdTorqueRodYA_DutyCycle     = "666YA";
static String cmdTorqueRodYB_DutyCycle     = "666YB";
static String cmdTorqueRodZA_DutyCycle     = "666ZA";
static String cmdTorqueRodZB_DutyCycle     = "666ZB";
/*..........................................*/
static String cmdPyros                     = "555";
static     String pyros_OFF_Primary        = "0";
static     String pyros_ON_Primary         = "1";
static     String pyros_OFF_Backup         = "2";
static     String pyros_ON_Backup          = "3";
static     String pyros_Master_Enable      = "X";
static     String pyros_Master_Disable     = "x";
static     String pyros_1_Activate         = "A";
static     String pyros_2_Activate         = "B";
static     String pyros_3_Activate         = "C";
static     String pyros_4_Activate         = "D";
static     String pyros_5_Activate         = "E";
static     String pyros_6_Activate         = "F";
static     String pyros_1_Deactivate       = "a";
static     String pyros_2_Deactivate       = "b";
static     String pyros_3_Deactivate       = "c";
static     String pyros_4_Deactivate       = "d";
static     String pyros_5_Deactivate       = "e";
static     String pyros_6_Deactivate       = "f";
/*..........................................*/
static String cmdHeaters                   = "444";
static     String heaters_Master_Enable    = "X";
static     String heaters_Master_Disable   = "x";    
static     String heaters_Surv_OFF_Primary = "s";
static     String heaters_Surv_ON_Primary  = "S";
static     String heaters_Surv_OFF_Backup  = "t";
static     String heaters_Surv_ON_Backup   = "T";
static     String heaters_Op_OFF_Primary   = "o";
static     String heaters_Op_ON_Primary    = "O";
static     String heaters_Op_OFF_Backup    = "p";
static     String heaters_Op_ON_Backup     = "P";
static     String heater_1_Activate        = "A";
static     String heater_2_Activate        = "B";
static     String heater_3_Activate        = "C";
static     String heater_4_Activate        = "D";
static     String heater_5_Activate        = "E";
static     String heater_6_Activate        = "F";
static     String heater_7_Activate        = "G";
static     String heater_8_Activate        = "H";
static     String heater_1_Deactivate      = "a";
static     String heater_2_Deactivate      = "b";
static     String heater_3_Deactivate      = "c";
static     String heater_4_Deactivate      = "d";
static     String heater_5_Deactivate      = "e";
static     String heater_6_Deactivate      = "f";
static     String heater_7_Deactivate      = "g";
static     String heater_8_Deactivate      = "h";
/*..........................................*/
static String cmdACS_Disable_Primary       = "2220";
static String cmdACS_Enable_Primary        = "2221";
static String cmdACS_Disable_Backup        = "2222";
static String cmdACS_Enable_Backup         = "2223";
/*..........................................*/
    
    

Vector CTCSSVector                  = null;

    
    /*=======================================================================*/
    /** Creates new PHT */
    /*=======================================================================*/
    public PHT( String baseDir, DialogAbout progressDialog ) {
    /*=======================================================================*/    
        CTCSSVector = new Vector();
        try {
            FileInputStream fis = new FileInputStream(baseDir+"qsgs_ctcss.txt");
            DataInputStream dis = new DataInputStream( fis );
            if( progressDialog == null )
                System.out.println("Found file '"+baseDir+"qsgs_ctcss.txt': Loading flight CTCSS data");            
            else
                progressDialog.appendInfo("Found file '"+baseDir+"qsgs_ctcss.txt': Loading flight CTCSS data\n");
            while( (dis.available()>0) ) {                
                String codeRead = dis.readLine().trim();
                String frequencyRead = dis.readLine().trim();
                CTCSSVector.add( new CTCSS( frequencyRead, codeRead ) );
            }
        } catch( IOException ioe ) { }
        
        for( int i=0; i<CTCSSVector.size(); i++ ) {
            CTCSS ctcss = (CTCSS)CTCSSVector.elementAt(i);
            //System.out.println( "Freq:"+ctcss.frequency + "  Code:"+ctcss.code );
        }
                                
    }

    /*=======================================================================*/
    /*=======================================================================*/
    public void setControlKey( String controlKeyToUse ) {
    /*=======================================================================*/
        commandStringMagicCode = controlKeyToUse;
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
        return (CTCSS) CTCSSVector.elementAt( i );
    }

    
    
    /*=======================================================================*/
    /*=======================================================================*/
    public static byte[] dataPacket( String cmd ) {
    /*=======================================================================*/    
        return createDataPacket( cmd, "" );
    }
    
    
    
    /*=======================================================================*/
    /*=======================================================================*/
    public static byte[] dataPacket( String cmd, String data ) {
    /*=======================================================================*/ 
        data = data.trim();
        if( cmd.startsWith(cmdIO) ) {
            /*------------------------------------------------------*/
            /* with IO ports command, the first parameter (IO port) */
            /* must be 3 chars long...                              */
            /*------------------------------------------------------*/
            if( data.length() <= 0 )        data = "000";
            else if( data.length() == 1 )   data = "00" + data;
            else if( data.length() == 2 )   data = "0" + data;
            else if( data.length() != 3 )   data = "";  //Error
        }
        else if( cmd.startsWith( cmdSetBroadcastText ) ) {
            if( data.length() > 199 )       data = data.substring(0,199);
        }
        return createDataPacket( cmd, data );
    }
    

    /*=======================================================================*/
    /*=======================================================================*/
    public static byte[] dataPacket( String cmd, String data1, String data2 ) {
    /*=======================================================================*/    
        
        data1 = data1.trim();
        data2 = data2.trim();
        
        if( cmd.startsWith(cmdIO) ) {
            /*------------------------------------------------------*/
            /* with IO ports command, the first parameter (IO port) */
            /* must be 3 chars long, the 2nd (value) 4 chars...     */
            /*------------------------------------------------------*/
            if( data1.length() <= 0 )        data1 = "000";
            else if( data1.length() == 1 )   data1 = "00" + data1;
            else if( data1.length() == 2 )   data1 = "0" + data1;            
            else if( data1.length() != 3 )   data1 = "";
            
            if( data2.length() <= 0 )        data2 = "0000";
            else if( data2.length() == 1 )   data2 = "000" + data2;
            else if( data2.length() == 2 )   data2 = "00" + data2;            
            else if( data2.length() == 3 )   data2 = "0" + data2;
            else if( data2.length() != 3 )   data2 = "";  //Error
        }
        else if( cmd.startsWith(cmdEDAC) ) {
            /*-------------------------------------------------------*/
            /* With Mem Read command, the two params are 4 hex chars */
            /*-------------------------------------------------------*/
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
            
        }
        return createDataPacket( cmd, data1+data2 );
    }

    /*=======================================================================*/
    /*=======================================================================*/
    public static byte[] dataPacket( String cmd, String data1, String data2, String data3 ) {
    /*=======================================================================*/    
        
        data1 = data1.trim();
        data2 = data2.trim();
        data3 = data3.trim();
        
        if( cmd.startsWith(cmdEDAC) ) {
            /*------------------------------------------------------------*/
            /* with mem write command, the first two parameters           */
            /* are hex 4 chars, the last one a decimal value, max 3 chars */
            /*------------------------------------------------------------*/
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
            
        }
        return createDataPacket( cmd, data1+data2+data3 );
    }
    
    
    /*=======================================================================*/
    /* Note: dataStringLength = lenght of the data field (after the command) */
    /* once it is transferred in a string...                                 */
    /*=======================================================================*/
    public static synchronized byte[] dataPacket( String cmd, int data )
    /*=======================================================================*/
    {
    String dataString = null;
    
        /*------------------------------------------------------------------*/
        /* Transfer the data into a suitable parameter string, according to */
        /* which command is requested...                                    */
        /*------------------------------------------------------------------*/
        
        
        //...............................................................TXDelay
        if( (cmd.equals(cmdTXDelay)) || 
            (cmd.equals(cmdTX1_Pwr)) || 
            (cmd.equals(cmdTX2_Pwr)) ||
            (cmd.equals(cmdTorqueRods)) ||
            (cmd.startsWith(cmdChargeControl))   ) {
            /*-------------------------------------------------------*/
            /* TX Delay or Power or rods requires a 3 chars paramter */
            /*-------------------------------------------------------*/
            dataString = Integer.toString( data, 16 ).trim();
            if( (dataString.length() <= 0) || (data < 0) )  dataString = "000";
            else if( dataString.length() == 1 )             dataString = "00" + dataString;
            else if( dataString.length() == 2 )             dataString = "0" + dataString;
            else if( dataString.length() != 3 )             dataString = "FFF";
            
        //................................................................Modems    
        } else if( (cmd.startsWith(cmdModemA)) || (cmd.startsWith(cmdModemB)) ) {
            /*---------------------------------------------------------------*/
            /* All num param to modem command involves appending 2 chars hex */
            /*---------------------------------------------------------------*/
            dataString = Integer.toString( data, 16 ).trim();
            if( (dataString.length() <= 0) || (data < 0) )  dataString = "00";
            else if( dataString.length() == 1 )             dataString = "0" + dataString;
            else if( dataString.length() != 2 )             dataString = "FF";
        
        //................................................................Modems    
        } else if( cmd.startsWith(cmdRate) ) {
            /*--------------------------------*/
            /* All rate param are 1 chars hex */
            /*--------------------------------*/
            dataString = Integer.toString( data, 16 ).trim();
            if( (dataString.length() <= 0) || (data < 0) )  dataString = "0";            
            else if( dataString.length() != 1 )             dataString = "F";
        //................................................................Wheel
        } else if( cmd.startsWith(cmdWheel) ) {
            /*---------------------*/
            /* This is NOT in HEXA */
            /*---------------------*/
            dataString = Integer.toString( data ).trim();
        } else {
            dataString = Integer.toString( data, 16 ).trim();
        }
        
        return createDataPacket( cmd, dataString );
        
    }

    
    
    /*=======================================================================*/
    /* Method: createDataPacket                                              */
    /*=======================================================================*/
    public static synchronized byte[] createDataPacket( String cmd, String data )
    /*=======================================================================*/
    {
      /*-------------------*/
      /* Prepare the frame */
      /*-------------------*/
      String toSend =    commandStringHeader + 
                         commandStringMagicCode + 
                         cmd +
                         data;
      
      if( verbose ) System.out.println( "PHT Packet: "+toSend );
      
      return toSend.getBytes();
      
    }
    
    
    /*=======================================================================*/
    /* Method: isCommandPrime                                                */
    /*=======================================================================*/
    public void setCommandPrime( boolean value ) {
    /*=======================================================================*/    
        commandPrime = value;
    }    
    
    /*=======================================================================*/
    /* Method: isCommandPrime                                                */
    /*=======================================================================*/
    public static boolean isCommandPrime() {
    /*=======================================================================*/    
        return( commandPrime );
    }    

}
