/*
 * Telemetry.java
 *
 * Created on June 18, 2001, 10:53 AM
    *
    *IMPORTANT: THIS CLASS IS CLOSELY TIED WITH THE GUI, CONTRARY TO
    *MOST OTHER CLASSES.
 */

package ca.gc.space.quicksat.ground.telemetry;
import java.util.*;
import java.io.*;
import java.awt.*;
import ca.gc.space.quicksat.ground.client.*;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class Telemetry extends Object {

/*-----------------------------*/
/* Time stamp for last refresh */
/*-----------------------------*/    
long timeLastRefresh[] = null;

/*------------------*/
/* Raw data storage */
/*------------------*/
int  rawData[] = null;

/*------------------------------------*/
/* Description of each telemetry item */
/*------------------------------------*/
String strItemDesc[] = null;
    
/*------------------------------*/    
/* Telemetry items for QuickSat */
/*------------------------------*/
float TX1_RF_OutputLevel_1      = 0;
final int   TX1_RF_OutputLevel_1_ndx  = 0;

float TX1_RF_OutputLevel_2          = 0;
final int   TX1_RF_OutputLevel_2_ndx      = 1;

float TX2_RF_OutputLevel_1          = 0;
final int   TX2_RF_OutputLevel_1_ndx      = 2;

float TX2_RF_OutputLevel_2          = 0;
final int   TX2_RF_OutputLevel_2_ndx      = 3;

float TX1_5V_TestPoint              = 0;
final int   TX1_5V_TestPoint_ndx          = 4;

float TX2_5V_TestPoint              = 0;
final int   TX2_5V_TestPoint_ndx          = 5;

float RX1_GaAsFET_Voltage           = 0;
final int   RX1_GaAsFET_Voltage_ndx       = 6;

float RX2_GaAsFET_Voltage           = 0;
final int   RX2_GaAsFET_Voltage_ndx       = 7;

float CDH_EPC_3V3_Supply_Voltage    = 0;
final int   CDH_EPC_3V3_Supply_Voltage_ndx= 8;

float CDH_EPC_5V_Supply_Voltage     = 0;
final int   CDH_EPC_5V_Supply_Voltage_ndx = 9;

float CDH_EPC1_7V5_Voltage          = 0;
final int   CDH_EPC1_7V5_Voltage_ndx      = 10;

float CDH_EPC2_7V5_Voltage          = 0;
final int   CDH_EPC2_7V5_Voltage_ndx      = 11;

float CDH_EPC1_7V5_Current          = 0;
final int   CDH_EPC1_7V5_Current_ndx      = 12;

float CDH_EPC2_7V5_Current          = 0;
final int   CDH_EPC2_7V5_Current_ndx      = 13;

float heaters_Pyros_EPC_28V_Voltage = 0;
final int   heaters_Pyros_EPC_28V_Voltage_ndx = 14;

float heaters_Pyros_EPC_5V_Voltage  = 0;
final int   heaters_Pyros_EPC_5V_Voltage_ndx = 15;

// Note: item 16 is not allocated yet 

float battery_1_Voltage             = 0;
final int   battery_1_Voltage_ndx         = 17;

float battery_2_Voltage             = 0;
final int   battery_2_Voltage_ndx         = 18;

float battery_1_Current             = 0;
final int   battery_1_Current_ndx         = 19;

float battery_2_Current             = 0;
final int   battery_2_Current_ndx         = 20;

float solar_Array_13_Voltage        = 0;
final int   solar_Array_13_Voltage_ndx    = 21;

float solar_Array_24_Voltage        = 0;
final int   solar_Array_24_Voltage_ndx    = 22;

float solar_Array_13_Current        = 0;
final int   solar_Array_13_Current_ndx    = 23;

float solar_Array_24_Current        = 0;
final int   solar_Array_24_Current_ndx    = 24;

float solar_Array_1_DPPT            = 0;
final int   solar_Array_1_DPPT_ndx        = 25;

float solar_Array_2_DPPT            = 0;
final int   solar_Array_2_DPPT_ndx        = 26;

float bus_Voltage                   = 0;
final int   bus_Voltage_ndx               = 27;

float CDH_Supply_Current            = 0;
final int   CDH_Supply_Current_ndx        = 28;

float ACS_Supply_Current            = 0;
final int   ACS_Supply_Current_ndx        = 29;

float payload_Supply_Current        = 0;
final int   payload_Supply_Current_ndx    = 30;

float payload_Monitor_1             = 0;
final int   payload_Monitor_1_ndx         = 31; 

float payload_Monitor_2             = 0;
final int   payload_Monitor_2_ndx         = 32; 

float payload_Monitor_3             = 0;
final int   payload_Monitor_3_ndx         = 33; 

float payload_Monitor_4             = 0;
final int   payload_Monitor_4_ndx         = 34; 

float payload_Monitor_5             = 0;
final int   payload_Monitor_5_ndx         = 35; 

float payload_Monitor_6             = 0;
final int   payload_Monitor_6_ndx         = 36; 

float payload_Monitor_7             = 0;
final int   payload_Monitor_7_ndx         = 37; 

float payload_Monitor_8             = 0;
final int   payload_Monitor_8_ndx         = 38; 

float payload_Monitor_9             = 0;
final int   payload_Monitor_9_ndx         = 39; 

float payload_Monitor_10            = 0;
final int   payload_Monitor_10_ndx        = 40; 

float payload_Monitor_11            = 0;
final int   payload_Monitor_11_ndx        = 41; 

float payload_Monitor_12            = 0;
final int   payload_Monitor_12_ndx        = 42; 

float panel_1_deploy_pot            = 0;
final int   panel_1_deploy_pot_ndx        = 43;

float panel_2_deploy_pot            = 0;
final int   panel_2_deploy_pot_ndx        = 44;

float ACS_EPC_5V_Supply_Voltage     = 0;
final int   ACS_EPC_5V_Supply_Voltage_ndx = 45;

float ACS_EPC_15V_Supply_Voltage    = 0;
final int   ACS_EPC_15V_Supply_Voltage_ndx= 46;

float ACS_EPC_m15V_Supply_Voltage   = 0;
final int   ACS_EPC_m15V_Supply_Voltage_ndx= 47;

float battery_1_Temp                = 0;
final int   battery_1_Temp_ndx            = 48;

float battery_2_Temp                = 0;
final int   battery_2_Temp_ndx            = 49;

float solar_Array_1_Temp            = 0;
final int   solar_Array_1_Temp_ndx        = 50;

float solar_Array_2_Temp            = 0;
final int   solar_Array_2_Temp_ndx        = 51;

float solar_Array_3_Temp            = 0;
final int   solar_Array_3_Temp_ndx        = 52;

float solar_Array_4_Temp            = 0;
final int   solar_Array_4_Temp_ndx        = 53;

float main_Converter_Temp_1         = 0;
final int   main_Converter_Temp_1_ndx     = 54;

float main_Converter_Temp_2         = 0;
final int   main_Converter_Temp_2_ndx     = 55;

float TX1_Temp                      = 0;
final int   TX1_Temp_ndx                  = 56;

float TX2_Temp                      = 0;
final int   TX2_Temp_ndx                  = 57;

float RX1_Temp                      = 0;
final int   RX1_Temp_ndx                  = 58;

float RX2_Temp                      = 0;
final int   RX2_Temp_ndx                  = 59;

float CPU_Temp                      = 0;
final int   CPU_Temp_ndx                  = 60;

float CDH_EPC_Temp                  = 0;
final int   CDH_EPC_Temp_ndx              = 61;

float ACS_Temp_1                    = 0;
final int   ACS_Temp_1_ndx                = 62;

float ACS_Temp_2                    = 0;
final int   ACS_Temp_2_ndx                = 63;

float magnetic_field_X              = 0;
final int   magnetic_field_X_ndx          = 64;

float magnetic_field_Y              = 0;
final int   magnetic_field_Y_ndx          = 65;

float magnetic_field_Z              = 0;
final int   magnetic_field_Z_ndx          = 66;

float earth_Sensor_1_A              = 0;
final int   earth_Sensor_1_A_ndx          = 67;

float earth_Sensor_1_B              = 0;
final int   earth_Sensor_1_B_ndx          = 68;

float earth_Sensor_1_S1             = 0;
final int   earth_Sensor_1_S1_ndx         = 69;

float earth_Sensor_1_S2             = 0;
final int   earth_Sensor_1_S2_ndx         = 70;

float earth_Sensor_1_Temp           = 0;
final int   earth_Sensor_1_Temp_ndx       = 71;

float earth_Sensor_2_A              = 0;
final int   earth_Sensor_2_A_ndx          = 72;

float earth_Sensor_2_B              = 0;
final int   earth_Sensor_2_B_ndx          = 73;

float earth_Sensor_2_S1             = 0;
final int   earth_Sensor_2_S1_ndx         = 74;

float earth_Sensor_2_S2             = 0;
final int   earth_Sensor_2_S2_ndx         = 75;

float earth_Sensor_2_Temp           = 0;
final int   earth_Sensor_2_Temp_ndx       = 76;

// 77 - 80 not allocated

float coarse_Sun_Sensor_1_Diode     = 0;
final int   coarse_Sun_Sensor_1_Diode_ndx = 81;

float coarse_Sun_Sensor_2_Diode     = 0;
final int   coarse_Sun_Sensor_2_Diode_ndx = 82;

float coarse_Sun_Sensor_3_Diode     = 0;
final int   coarse_Sun_Sensor_3_Diode_ndx = 83;

float coarse_Sun_Sensor_4_Diode     = 0;
final int   coarse_Sun_Sensor_4_Diode_ndx = 84;

float coarse_Sun_Sensor_5_Diode     = 0;
final int   coarse_Sun_Sensor_5_Diode_ndx = 85;

float coarse_Sun_Sensor_6_Diode     = 0;
final int   coarse_Sun_Sensor_6_Diode_ndx = 86;

float medium_Sun_Sensor_1_QA        = 0;
final int   medium_Sun_Sensor_1_QA_ndx    = 87;

float medium_Sun_Sensor_1_QB        = 0;
final int   medium_Sun_Sensor_1_QB_ndx    = 88;

float medium_Sun_Sensor_1_QC        = 0;
final int   medium_Sun_Sensor_1_QC_ndx    = 89;

float medium_Sun_Sensor_1_QD        = 0;
final int   medium_Sun_Sensor_1_QD_ndx    = 90;

float medium_Sun_Sensor_2_QA        = 0;
final int   medium_Sun_Sensor_2_QA_ndx    = 91;

float medium_Sun_Sensor_2_QB        = 0;
final int   medium_Sun_Sensor_2_QB_ndx    = 92;

float medium_Sun_Sensor_2_QC        = 0;
final int   medium_Sun_Sensor_2_QC_ndx    = 93;

float medium_Sun_Sensor_2_QD        = 0;
final int   medium_Sun_Sensor_2_QD_ndx    = 94;

float torque_Rod_1_Driver_Current       = 0;
final int   torque_Rod_1_Driver_Current_ndx   = 95;

float torque_Rod_2_Driver_Current       = 0;
final int   torque_Rod_2_Driver_Current_ndx   = 96;

float torque_Rod_3_Driver_Current       = 0;
final int   torque_Rod_3_Driver_Current_ndx   = 97;

float ACS_5V_Control_Supply_Voltage     = 0;
final int   ACS_5V_Control_Supply_Voltage_ndx = 98;

float ACS_14V_Control_Supply_Voltage    = 0;
final int   ACS_14V_Control_Supply_Voltage_ndx= 99;

float ACS_14V_Control_Supply_Current    = 0;
final int   ACS_14V_Control_Supply_Current_ndx= 100;


// 101 - 111 not allocated

float magnetometer_Thermistor           = 0;
final int   magnetometer_Thermistor_ndx       = 112;

float medium_Sun_Sensor_1_Thermistor    = 0;
final int   medium_Sun_Sensor_1_Thermistor_ndx= 113;

float medium_Sun_Sensor_2_Thermistor    = 0;
final int   medium_Sun_Sensor_2_Thermistor_ndx= 114;

float coarse_Sun_Sensor_Thermistor_1    = 0;
final int   coarse_Sun_Sensor_Thermistor_1_ndx= 115;

float coarse_Sun_Sensor_Thermistor_2    = 0;
final int   coarse_Sun_Sensor_Thermistor_2_ndx= 116;

float coarse_Sun_Sensor_Thermistor_3    = 0;
final int   coarse_Sun_Sensor_Thermistor_3_ndx= 117;

// 118 - 119 not allocated

float payload_Bus_Temp_1                = 0;
final int   payload_Bus_Temp_1_ndx            = 120;

float payload_Bus_Temp_2                = 0;
final int   payload_Bus_Temp_2_ndx            = 121;

float payload_Bus_Temp_3                = 0;
final int   payload_Bus_Temp_3_ndx            = 122;

float payload_Bus_Temp_4                = 0;
final int   payload_Bus_Temp_4_ndx            = 123;

float payload_Bus_Temp_5                = 0;
final int   payload_Bus_Temp_5_ndx            = 124;

float payload_Bus_Temp_6                = 0;
final int   payload_Bus_Temp_6_ndx            = 125;

float payload_Bus_Temp_7                = 0;
final int   payload_Bus_Temp_7_ndx            = 126;

float payload_Bus_Temp_8                = 0;
final int   payload_Bus_Temp_8_ndx            = 127;

boolean TX1_PLL_Error                   = false;
final int     TX1_PLL_Error_ndx               = 128;

boolean TX2_PLL_Error                   = false;
final int     TX2_PLL_Error_ndx               = 129;

boolean RX1_Regulator_Alarm             = false;
final int     RX1_Regulator_Alarm_ndx         = 130;

boolean RX2_Regulator_Alarm             = false;
final int     RX2_Regulator_Alarm_ndx         = 131;

boolean RX1_Carrier                     = false;
final int     RX1_Carrier_ndx                 = 132;

boolean RX2_Carrier                     = false;
final int     RX2_Carrier_ndx                 = 133;

boolean modem_A_CTCSS                   = false;
final int     modem_A_CTCSS_ndx               = 134;

boolean modem_B_CTCSS                   = false;
final int     modem_B_CTCSS_ndx               = 135;

boolean main_Current_Limit_1_Fault      = false;
final int     main_Current_Limit_1_Fault_ndx  = 136;

boolean main_Current_Limit_2_Fault      = false;
final int     main_Current_Limit_2_Fault_ndx  = 137;

final int wheel_Status_ndx      = 138;  int wheel_Status = 0;
final int TLM_WHEEL_STATUS_MASK_ENABLE          = 0x8000;
final int TLM_WHEEL_STATUS_MASK_NO_ANSWER       = 0x4000;
final int TLM_WHEEL_STATUS_MASK_TELEMETRY_ERROR = 0x2000;

final int wheel_Temp_ndx        = 139;  float wheel_Temp  = 0; //C
final int wheel_14VI_ndx        = 140;  float wheel_14VI  = 0; //mA
final int wheel_14V_ndx         = 141;  float wheel_14V   = 0; //V
final int wheel_8V_ndx          = 142;  float wheel_8V    = 0; //V
final int wheel_10V_ndx         = 143;  float wheel_10V   = 0; //V
final int wheel_Torque_Error_ndx= 144;  float wheel_Torque_Error = 0; //Oz-in
final int wheel_5V_ndx          = 145;  float wheel_5V    = 0; //V
final int wheel_Torque_ndx      = 146;  float wheel_Torque= 0; //Oz-in
final int wheel_Speed_ndx       = 147;  float wheel_Speed = 0; //Rpm

final int spacecraft_Mode_ndx   = 148;  int spacecraft_Mode = 0;
final int TLM_SC_MODE_MASK_DIGIPEAT     = 0x0001;

final int time_Since_Boot_ndx   = 149;  int time_Since_Boot = 0;

final int HW_Access_Status_ndx = 150;  int HW_Access_Status = 0;
final int TLM_HWAC_STATUS_MASK_ACS_ON   = 0x0001;

int   PHT_Status                        = 0;
final int   PHT_Status_ndx              = 151;

int         TX1_PTT                     = 0;
final int   TX1_PTT_ndx                 = 152;
int         TX1_Power                   = 0;
final int   TX1_Power_ndx               = 153;
int         TX2_PTT                     = 0;
final int   TX2_PTT_ndx                 = 154;
int         TX2_Power                   = 0;
final int   TX2_Power_ndx               = 155;
int         TX_Delay                    = 0;
final int   TX_Delay_ndx                = 156;
int         TX1_Source                  = -1;
final int   TX1_Source_ndx              = 157;
int         TX2_Source                  = -1;
final int   TX2_Source_ndx              = 158;

int         pyros_State                 = -1;
final int   pyros_State_ndx             = 159;
final int   TLM_PYROS_STATE_MASK_ENABLE	= 0x8000;
final int   TLM_PYROS_STATE_MASK_POWER	= 0x4000;
final int   TLM_PYROS_STATE_MASK_1	= 0x0001;
final int   TLM_PYROS_STATE_MASK_2	= 0x0002;
final int   TLM_PYROS_STATE_MASK_3	= 0x0004;
final int   TLM_PYROS_STATE_MASK_4	= 0x0008;
final int   TLM_PYROS_STATE_MASK_5	= 0x0010;
final int   TLM_PYROS_STATE_MASK_6	= 0x0020;

int         heaters_State               = -1;
final int   heaters_State_ndx           = 160;
final int   TLM_HEATERS_STATE_MASK_ENABLE		= 0x8000;
final int   TLM_HEATERS_STATE_MASK_POWER_SURVIVAL	= 0x4000;
final int   TLM_HEATERS_STATE_MASK_POWER_OPERATION	= 0x2000;
final int   TLM_HEATERS_STATE_MASK_1			= 0x0001;
final int   TLM_HEATERS_STATE_MASK_2			= 0x0002;
final int   TLM_HEATERS_STATE_MASK_3			= 0x0004;
final int   TLM_HEATERS_STATE_MASK_4			= 0x0008;
final int   TLM_HEATERS_STATE_MASK_5			= 0x0010;
final int   TLM_HEATERS_STATE_MASK_6			= 0x0020;
final int   TLM_HEATERS_STATE_MASK_7			= 0x0040;
final int   TLM_HEATERS_STATE_MASK_8			= 0x0080;

final int modemA_Offset_ndx     = 161;  int modemA_Offset       = 0;
final int modemA_Deviation_ndx  = 162;  int modemA_Deviation    = 0;
final int modemA_Source_ndx     = 163;  int modemA_Source       = 0;
final int modemA_CTCSS_ndx      = 164;  int modemA_CTCSS        = 0;
final int modemB_Offset_ndx     = 165;  int modemB_Offset       = 0;
final int modemB_Deviation_ndx  = 166;  int modemB_Deviation    = 0;
final int modemB_Source_ndx     = 167;  int modemB_Source       = 0;
final int modemB_CTCSS_ndx      = 168;  int modemB_CTCSS        = 0;
final int modemB_Baud_ndx       = 169;  int modemB_Baud         = 0;
final int modemB_Scrambler_ndx  = 170;  int modemB_Scrambler    = 0;

final int payloads_State_ndx    = 171;  int payloads_State      = 0;
final int TLM_PAYLOADS_STATE_MASK_POWER     = 0x4000;
final int TLM_PAYLOADS_STATE_MASK_1	    = 0x0001;
final int TLM_PAYLOADS_STATE_MASK_2	    = 0x0002;
final int TLM_PAYLOADS_STATE_MASK_3	    = 0x0004;
final int TLM_PAYLOADS_STATE_MASK_4	    = 0x0008;
final int TLM_PAYLOADS_STATE_MASK_5	    = 0x0010;
final int TLM_PAYLOADS_STATE_MASK_6	    = 0x0020;
final int TLM_PAYLOADS_STATE_MASK_7	    = 0x0040;
final int TLM_PAYLOADS_STATE_MASK_8	    = 0x0080;

final int rodXA_State_ndx       = 172;  int rodXA_State         = 0;
final int rodXB_State_ndx       = 173;  int rodXB_State         = 0;
final int rodYA_State_ndx       = 174;  int rodYA_State         = 0;
final int rodYB_State_ndx       = 175;  int rodYB_State         = 0;
final int rodZA_State_ndx       = 176;  int rodZA_State         = 0;
final int rodZB_State_ndx       = 177;  int rodZB_State         = 0;
final int TLM_RODS_STATE_MASK_ENABLE        = 0x8000;
final int TLM_RODS_STATE_MASK_DUTY_CYCLE    = 0x0FFF;

final int charge1_Status_ndx    = 178;  int charge1_Status      = 0;
final int charge2_Status_ndx    = 179;  int charge2_Status      = 0;
final int TLM_CHARGE_STATUS_MASK_ENABLE     = 0x8000;
final int TLM_CHARGE_STATUS_MASK_DUTY_CYCLE = 0x0FFF;

final int solarArray_Status_ndx = 180;  int solarArray_Status   = 0;
final int TLM_SOLAR_ARRAYS_STATUS_MASK_FEED_1	= 0x0001;
final int TLM_SOLAR_ARRAYS_STATUS_MASK_FEED_2	= 0x0002;


/*------------------------------*/    
int   nbTelemItems                      = 181;  //MUST BE Always 1 more than max index

jload gui = null;

String baseDir = "C:/jload/";




    /** Creates new Telemetry */
    public Telemetry( String appBaseDir, jload gui ) {
        timeLastRefresh = new long[nbTelemItems];        
        rawData = new int[nbTelemItems];
        strItemDesc = new String[nbTelemItems];
        this.gui = gui;
        baseDir = appBaseDir;
    }
    
    /*-----------------------------------------------------*/
    /* Decode the telemetry packet, put items into the     */
    /* proper class variables, and return a summary string */
    /*-----------------------------------------------------*/
    public String decode( byte tlmPacket[] ) {
        
        if( tlmPacket.length < 4 ) return( "Telemetry packet too short");

        long onboardTime = 0;
        onboardTime |= ((int)tlmPacket[3] & 0xFF);
        onboardTime = onboardTime << 8;
        onboardTime |= ((int)tlmPacket[2] & 0xFF);
        onboardTime = onboardTime << 8;
        onboardTime |= ((int)tlmPacket[1] & 0xFF);
        onboardTime = onboardTime << 8;
        onboardTime |= ((int)tlmPacket[0] & 0xFF);
        
        //System.out.println( ""+onboardTime );
        gui.txtTelemUTC.setText(""+onboardTime);
        
        int nbItemsDecoded  = 0;
        int itemIndex       = 0;
        int itemData        = 0;
        
        SCAN_PACKET:
        for( int i=4; i<tlmPacket.length; i+=3 ) {
            
            if( tlmPacket.length < (i+3) ) {
                /*--------------------------------------*/
                /* Here we could trigger an exception...*/
                /*--------------------------------------*/
                break SCAN_PACKET;
            }
            
            //itemIndex = 0;
            itemIndex = (int)tlmPacket[i] & 0xFF;
            
            //itemData = 0;
            //itemData = (int)tlmPacket[i+2] & 0xFF;            
            itemData = tlmPacket[i+2];
            itemData  = itemData << 8;
            itemData |= ((int)tlmPacket[i+1] & 0xFF);            
            
            switch( itemIndex ) {               
                case TX1_RF_OutputLevel_1_ndx:
                    gui.panelTelemetry.prgrssTX1RFOut1.setValue(itemData);
                    gui.panelTelemetry.txtTX1RFOut1.setText(""+itemData);
                    break;
                case TX1_RF_OutputLevel_2_ndx:
                    gui.panelTelemetry.prgrssTX1RFOut2.setValue(itemData);
                    gui.panelTelemetry.txtTX1RFOut2.setText(""+itemData);
                    break;
                case TX2_RF_OutputLevel_1_ndx:
                    gui.panelTelemetry.prgrssTX2RFOut1.setValue(itemData);
                    gui.panelTelemetry.txtTX2RFOut1.setText(""+itemData);
                    break;
                case TX2_RF_OutputLevel_2_ndx:
                    gui.panelTelemetry.prgrssTX2RFOut2.setValue(itemData);
                    gui.panelTelemetry.txtTX2RFOut2.setText(""+itemData);
                    break;
                case TX1_5V_TestPoint_ndx:
                    gui.panelTelemetry.prgrssTX1_5V.setValue(itemData);
                    gui.panelTelemetry.txtTX1_5V.setText(""+itemData);                    
                    break;
                case TX2_5V_TestPoint_ndx:
                    gui.panelTelemetry.prgrssTX2_5V.setValue(itemData);
                    gui.panelTelemetry.txtTX2_5V.setText(""+itemData);                    
                    break;
                case RX1_GaAsFET_Voltage_ndx:
                    break;
                case RX2_GaAsFET_Voltage_ndx:
                    break;
                case CDH_EPC_3V3_Supply_Voltage_ndx:
                    break;
                case CDH_EPC_5V_Supply_Voltage_ndx:
                    break;
                case CDH_EPC1_7V5_Voltage_ndx:
                    break;
                case CDH_EPC2_7V5_Voltage_ndx:
                    break;
                case CDH_EPC1_7V5_Current_ndx:
                    break;
                case CDH_EPC2_7V5_Current_ndx:
                    break;
                case heaters_Pyros_EPC_28V_Voltage_ndx:
                    break;
                case heaters_Pyros_EPC_5V_Voltage_ndx:
                    break;
                case battery_1_Voltage_ndx:
                    break;
                case battery_2_Voltage_ndx:
                    break;
                case battery_1_Current_ndx:
                    break;
                case battery_2_Current_ndx:
                    break;
                case solar_Array_13_Voltage_ndx:
                    break;
                case solar_Array_24_Voltage_ndx:
                    break;
                case solar_Array_13_Current_ndx:
                    break;
                case solar_Array_24_Current_ndx:
                    break;
                case solar_Array_1_DPPT_ndx:
                    break;
                case solar_Array_2_DPPT_ndx:
                    break;
                case bus_Voltage_ndx:
                    break;
                case CDH_Supply_Current_ndx:
                    break;
                case ACS_Supply_Current_ndx:
                    break;
                case payload_Supply_Current_ndx:
                    break;
                case payload_Monitor_1_ndx:
                    gui.panelTelemetry.prgrssPayload1.setValue(itemData);
                    break;
                case payload_Monitor_2_ndx:
                    gui.panelTelemetry.prgrssPayload2.setValue(itemData);
                    break;
                case payload_Monitor_3_ndx:
                    gui.panelTelemetry.prgrssPayload3.setValue(itemData);
                    break;
                case payload_Monitor_4_ndx:
                    gui.panelTelemetry.prgrssPayload4.setValue(itemData);
                    break;
                case payload_Monitor_5_ndx:
                    gui.panelTelemetry.prgrssPayload5.setValue(itemData);
                    break;
                case payload_Monitor_6_ndx:
                    gui.panelTelemetry.prgrssPayload6.setValue(itemData);
                    break;
                case payload_Monitor_7_ndx:
                    gui.panelTelemetry.prgrssPayload7.setValue(itemData);
                    break;
                case payload_Monitor_8_ndx:
                    gui.panelTelemetry.prgrssPayload8.setValue(itemData);
                    break;
                case payload_Monitor_9_ndx:
                    gui.panelTelemetry.prgrssPayload9.setValue(itemData);
                    break;
                case payload_Monitor_10_ndx:
                    gui.panelTelemetry.prgrssPayload10.setValue(itemData);
                    break;
                case payload_Monitor_11_ndx:
                    gui.panelTelemetry.prgrssPayload11.setValue(itemData);
                    break;
                case payload_Monitor_12_ndx:
                    gui.panelTelemetry.prgrssPayload12.setValue(itemData);
                    break;
                case panel_1_deploy_pot_ndx:
                    break;
                case panel_2_deploy_pot_ndx:
                    break;
                case ACS_EPC_5V_Supply_Voltage_ndx:
                    break;
                case ACS_EPC_15V_Supply_Voltage_ndx:
                    break;
                case ACS_EPC_m15V_Supply_Voltage_ndx:
                    break;
                case battery_1_Temp_ndx:
                    break;
                case battery_2_Temp_ndx:
                    break;
                case solar_Array_1_Temp_ndx:
                    break;
                case solar_Array_2_Temp_ndx:
                    break;
                case solar_Array_3_Temp_ndx:
                    break;
                case solar_Array_4_Temp_ndx:
                    break;
                case main_Converter_Temp_1_ndx:
                    break;
                case main_Converter_Temp_2_ndx:
                    break;
                case TX1_Temp_ndx:
                    gui.panelTelemetry.prgrssTX1Temp.setValue(itemData);
                    gui.panelTelemetry.txtTX1Temp.setText(""+itemData);                    
                    break;
                case TX2_Temp_ndx:
                    gui.panelTelemetry.prgrssTX2Temp.setValue(itemData);
                    gui.panelTelemetry.txtTX2Temp.setText(""+itemData);                    
                    break;
                case RX1_Temp_ndx:
                    break;
                case RX2_Temp_ndx:
                    break;
                case CPU_Temp_ndx:
                    break;
                case CDH_EPC_Temp_ndx:
                    break;
                case ACS_Temp_1_ndx:
                    break;
                case ACS_Temp_2_ndx:
                    break;
                case magnetic_field_X_ndx:
                    break;
                case magnetic_field_Y_ndx:
                    break;
                case magnetic_field_Z_ndx:
                    break;
                case earth_Sensor_1_A_ndx:
                    break;
                case earth_Sensor_1_B_ndx:
                    break;
                case earth_Sensor_1_S1_ndx:
                    break;
                case earth_Sensor_1_S2_ndx:
                    break;
                case earth_Sensor_1_Temp_ndx:
                    break;
                case earth_Sensor_2_A_ndx:
                    break;
                case earth_Sensor_2_B_ndx:
                    break;
                case earth_Sensor_2_S1_ndx:
                    break;
                case earth_Sensor_2_S2_ndx:
                    break;
                case earth_Sensor_2_Temp_ndx:
                    break;
                case coarse_Sun_Sensor_1_Diode_ndx:
                    break;
                case coarse_Sun_Sensor_2_Diode_ndx:
                    break;
                case coarse_Sun_Sensor_3_Diode_ndx:
                    break;
                case coarse_Sun_Sensor_4_Diode_ndx:
                    break;
                case coarse_Sun_Sensor_5_Diode_ndx:
                    break;
                case coarse_Sun_Sensor_6_Diode_ndx:
                    break;
                case medium_Sun_Sensor_1_QA_ndx:
                    break;
                case medium_Sun_Sensor_1_QB_ndx:
                    break;
                case medium_Sun_Sensor_1_QC_ndx:
                    break;
                case medium_Sun_Sensor_1_QD_ndx:
                    break;
                case medium_Sun_Sensor_2_QA_ndx:
                    break;
                case medium_Sun_Sensor_2_QB_ndx:
                    break;
                case medium_Sun_Sensor_2_QC_ndx:
                    break;
                case medium_Sun_Sensor_2_QD_ndx:
                    break;
                case torque_Rod_1_Driver_Current_ndx:
                    break;
                case torque_Rod_2_Driver_Current_ndx:
                    break;
                case torque_Rod_3_Driver_Current_ndx:
                    break;
                case ACS_5V_Control_Supply_Voltage_ndx:
                    break;
                case ACS_14V_Control_Supply_Voltage_ndx:
                    break;
                case ACS_14V_Control_Supply_Current_ndx:
                    break;
                case magnetometer_Thermistor_ndx:
                    break;
                case medium_Sun_Sensor_1_Thermistor_ndx:
                    break;
                case medium_Sun_Sensor_2_Thermistor_ndx:
                    break;
                case coarse_Sun_Sensor_Thermistor_1_ndx:
                    break;
                case coarse_Sun_Sensor_Thermistor_2_ndx:
                    break;
                case coarse_Sun_Sensor_Thermistor_3_ndx:
                    break;
                case payload_Bus_Temp_1_ndx:
                    break;
                case payload_Bus_Temp_2_ndx:
                    break;
                case payload_Bus_Temp_3_ndx:
                    break;
                case payload_Bus_Temp_4_ndx:
                    break;
                case payload_Bus_Temp_5_ndx:
                    break;
                case payload_Bus_Temp_6_ndx:
                    break;
                case payload_Bus_Temp_7_ndx:
                    break;
                case payload_Bus_Temp_8_ndx:
                    break;
                case TX1_PLL_Error_ndx:
                    if( itemData == 0 ) {
                        gui.panelTelemetry.lblTX1PLLError.setBackground(new Color(102,51,0));
                        gui.panelTelemetry.lblTX1PLLError.setForeground(java.awt.Color.gray);
                    } else {
                        gui.panelTelemetry.lblTX1PLLError.setBackground(java.awt.Color.red);
                        gui.panelTelemetry.lblTX1PLLError.setForeground(java.awt.Color.yellow);
                    }
                    break;
                case TX2_PLL_Error_ndx:
                    if( itemData == 0 ) {
                        gui.panelTelemetry.lblTX2PLLError.setBackground(new Color(102,51,0));
                        gui.panelTelemetry.lblTX2PLLError.setForeground(java.awt.Color.gray);
                    } else {
                        gui.panelTelemetry.lblTX2PLLError.setBackground(java.awt.Color.red);
                        gui.panelTelemetry.lblTX2PLLError.setForeground(java.awt.Color.yellow);
                    }
                    break;
                case RX1_Regulator_Alarm_ndx:
                    break;
                case RX2_Regulator_Alarm_ndx:
                    break;
                case RX1_Carrier_ndx:
                    break;
                case RX2_Carrier_ndx:
                    break;
                case modem_A_CTCSS_ndx:
                    break;
                case modem_B_CTCSS_ndx:
                    break;
                case main_Current_Limit_1_Fault_ndx:
                    break;
                case main_Current_Limit_2_Fault_ndx:
                    break;
                case wheel_Temp_ndx:
                    break;
                case wheel_14VI_ndx:
                    break;
                case wheel_14V_ndx:
                    break;
                case wheel_8V_ndx:
                    break;
                case wheel_10V_ndx:
                    break;
                case wheel_Torque_Error_ndx:
                    break;
                case wheel_5V_ndx:
                    break;
                case wheel_Torque_ndx:
                    break;
                case wheel_Speed_ndx:
                    break;
                case spacecraft_Mode_ndx:
                    if( (itemData & TLM_SC_MODE_MASK_DIGIPEAT) != 0 ) {
                        gui.panelModems_cmd.showButtonActiveON(gui.panelModems_cmd.btnDigipeatON);
                        gui.panelModems_cmd.showButtonInactive(gui.panelModems_cmd.btnDigipeatOFF);
                    } else {
                        gui.panelModems_cmd.showButtonInactive(gui.panelModems_cmd.btnDigipeatON);
                        gui.panelModems_cmd.showButtonActiveOFF(gui.panelModems_cmd.btnDigipeatOFF);
                    }
                    break;
                case time_Since_Boot_ndx:
                    break;
                //case spacecraft_Time_ndx:
                //    break;
                case HW_Access_Status_ndx:
                    if( (itemData & TLM_HWAC_STATUS_MASK_ACS_ON) != 0 ) {
                        /*--------------------------------------*/
                        /* This means that the ACS module is ON */
                        /*--------------------------------------*/
                        gui.panelACS_cmd.showButtonActiveON(gui.panelACS_cmd.btnACSON);
                        gui.panelACS_cmd.showButtonInactive(gui.panelACS_cmd.btnACSOFF);
                    } else {
                        /*---------------------------------------*/
                        /* This means that the ACS module is OFF */
                        /*---------------------------------------*/
                        gui.panelACS_cmd.showButtonInactive(gui.panelACS_cmd.btnACSON);
                        gui.panelACS_cmd.showButtonActiveOFF(gui.panelACS_cmd.btnACSOFF);
                    }
                    break;
                case PHT_Status_ndx:
                    break;
                case TX1_PTT_ndx:
                    if( itemData == 0 ) {
                        gui.panelTX_cmd.showButtonInactive(gui.panelTX_cmd.btnTX1ON);
                        gui.panelTX_cmd.showButtonActiveOFF(gui.panelTX_cmd.btnTX1OFF);
                        gui.panelTelemetry.lblTX1PTT.setBackground(new Color(0,51,51));
                        gui.panelTelemetry.lblTX1PTT.setForeground(java.awt.Color.gray);                        
                    } else {
                        gui.panelTX_cmd.showButtonActiveON(gui.panelTX_cmd.btnTX1ON);
                        gui.panelTX_cmd.showButtonInactive(gui.panelTX_cmd.btnTX1OFF);
                        gui.panelTelemetry.lblTX1PTT.setBackground(java.awt.Color.green);
                        gui.panelTelemetry.lblTX1PTT.setForeground(java.awt.Color.blue);                        
                    }
                    break;
                case TX1_Power_ndx:
                    break;
                case TX2_PTT_ndx:
                    if( itemData == 0 ) {
                        gui.panelTX_cmd.showButtonInactive(gui.panelTX_cmd.btnTX2ON);
                        gui.panelTX_cmd.showButtonActiveOFF(gui.panelTX_cmd.btnTX2OFF);
                        gui.panelTelemetry.lblTX2PTT.setBackground(new Color(0,51,51));
                        gui.panelTelemetry.lblTX2PTT.setForeground(java.awt.Color.gray);                        
                    } else {
                        gui.panelTX_cmd.showButtonActiveON(gui.panelTX_cmd.btnTX2ON);
                        gui.panelTX_cmd.showButtonInactive(gui.panelTX_cmd.btnTX2OFF);
                        gui.panelTelemetry.lblTX2PTT.setBackground(java.awt.Color.green);
                        gui.panelTelemetry.lblTX2PTT.setForeground(java.awt.Color.blue);                        
                    }
                    break;
                case TX2_Power_ndx:
                    break;
                case TX_Delay_ndx:
                    break;
                //=====================    
                case TX1_Source_ndx:
                //=====================    
                    if( itemData == TX1_Source ) break;
                    TX1_Source = itemData;
                    if( itemData == 1 ) {
                        gui.panelTX_cmd.showButtonActiveON(gui.panelTX_cmd.btnTX1SourceA);
                        gui.panelTX_cmd.showButtonInactive(gui.panelTX_cmd.btnTX1SourceB);
                    } else if( itemData == 2 ){
                        gui.panelTX_cmd.showButtonInactive(gui.panelTX_cmd.btnTX1SourceA);
                        gui.panelTX_cmd.showButtonActiveON(gui.panelTX_cmd.btnTX1SourceB);
                    } else {
                        gui.panelTX_cmd.showButtonNeutral(gui.panelTX_cmd.btnTX1SourceA);
                        gui.panelTX_cmd.showButtonNeutral(gui.panelTX_cmd.btnTX1SourceB);
                    }
                    break;
                //=====================                    
                case TX2_Source_ndx:
                //=====================    
                    if( itemData == TX2_Source ) break;
                    TX2_Source = itemData;
                    if( itemData == 1 ) {
                        gui.panelTX_cmd.showButtonActiveON(gui.panelTX_cmd.btnTX2SourceA);
                        gui.panelTX_cmd.showButtonInactive(gui.panelTX_cmd.btnTX2SourceB);
                    } else if( itemData == 2 ){
                        gui.panelTX_cmd.showButtonInactive(gui.panelTX_cmd.btnTX2SourceA);
                        gui.panelTX_cmd.showButtonActiveON(gui.panelTX_cmd.btnTX2SourceB);
                    } else {
                        gui.panelTX_cmd.showButtonNeutral(gui.panelTX_cmd.btnTX2SourceA);
                        gui.panelTX_cmd.showButtonNeutral(gui.panelTX_cmd.btnTX2SourceB);
                    }
                    break;
                //=====================        
                case pyros_State_ndx:
                //=====================        
                    if( itemData == pyros_State ) break;
                    pyros_State = itemData;                    
                    if( (itemData & TLM_PYROS_STATE_MASK_ENABLE) == 0 ) {
                        //-------------------------------------
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyrosEnable);
                        gui.panelPyros_cmd.showButtonActiveOFF(gui.panelPyros_cmd.btnPyrosDisable);
                    } else {
                        gui.panelPyros_cmd.showButtonActiveON(gui.panelPyros_cmd.btnPyrosEnable);
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyrosDisable);
                    }                        
                    if( (itemData & TLM_PYROS_STATE_MASK_POWER) == 0 ) {
                        //-------------------------------------
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyrosON);
                        gui.panelPyros_cmd.showButtonActiveOFF(gui.panelPyros_cmd.btnPyrosOFF);
                    } else {
                        gui.panelPyros_cmd.showButtonActiveON(gui.panelPyros_cmd.btnPyrosON);
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyrosOFF);
                    }                        
                    if( (itemData & TLM_PYROS_STATE_MASK_1) == 0 ) {
                        //--------------------------------
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro1ON);
                        gui.panelPyros_cmd.showButtonActiveOFF(gui.panelPyros_cmd.btnPyro1OFF);
                    } else {
                        gui.panelPyros_cmd.showButtonActiveON(gui.panelPyros_cmd.btnPyro1ON);
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro1OFF);
                    }                        
                    if( (itemData & TLM_PYROS_STATE_MASK_2) == 0 ) {
                        //---------------------------------
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro2ON);
                        gui.panelPyros_cmd.showButtonActiveOFF(gui.panelPyros_cmd.btnPyro2OFF);
                    } else {
                        gui.panelPyros_cmd.showButtonActiveON(gui.panelPyros_cmd.btnPyro2ON);
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro2OFF);
                    }                        
                    if( (itemData & TLM_PYROS_STATE_MASK_3) == 0 ) {
                        //---------------------------------
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro3ON);
                        gui.panelPyros_cmd.showButtonActiveOFF(gui.panelPyros_cmd.btnPyro3OFF);
                    } else {
                        gui.panelPyros_cmd.showButtonActiveON(gui.panelPyros_cmd.btnPyro3ON);
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro3OFF);
                    }                        
                    if( (itemData & TLM_PYROS_STATE_MASK_4) == 0 ) {
                        //---------------------------------
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro4ON);
                        gui.panelPyros_cmd.showButtonActiveOFF(gui.panelPyros_cmd.btnPyro4OFF);
                    } else {
                        gui.panelPyros_cmd.showButtonActiveON(gui.panelPyros_cmd.btnPyro4ON);
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro4OFF);
                    }                        
                    if( (itemData & TLM_PYROS_STATE_MASK_5) == 0 ) {
                        //---------------------------------
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro5ON);
                        gui.panelPyros_cmd.showButtonActiveOFF(gui.panelPyros_cmd.btnPyro5OFF);
                    } else {
                        gui.panelPyros_cmd.showButtonActiveON(gui.panelPyros_cmd.btnPyro5ON);
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro5OFF);
                    }                        
                    if( (itemData & TLM_PYROS_STATE_MASK_6) == 0 ) {
                        //---------------------------------
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro6ON);
                        gui.panelPyros_cmd.showButtonActiveOFF(gui.panelPyros_cmd.btnPyro6OFF);
                    } else {
                        gui.panelPyros_cmd.showButtonActiveON(gui.panelPyros_cmd.btnPyro6ON);
                        gui.panelPyros_cmd.showButtonInactive(gui.panelPyros_cmd.btnPyro6OFF);
                    }                        
                    
                    break;
                //=====================    
                case heaters_State_ndx:
                //=====================    
                    if( itemData == heaters_State ) break;
                    heaters_State = itemData;
                    if( (itemData & TLM_HEATERS_STATE_MASK_ENABLE) == 0 ) {
                        //-------------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtrEnable);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtrDisable);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtrEnable);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtrDisable);
                    }                        
                    if( (itemData & TLM_HEATERS_STATE_MASK_POWER_SURVIVAL) == 0 ) {
                        //-------------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtrSurvON);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtrSurvOFF);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtrSurvON);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtrSurvOFF);
                    }                        
                    if( (itemData & TLM_HEATERS_STATE_MASK_POWER_OPERATION) == 0 ) {
                        //-------------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtrOpON);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtrOpOFF);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtrOpON);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtrOpOFF);
                    }                        
                    if( (itemData & TLM_HEATERS_STATE_MASK_1) == 0 ) {
                        //--------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr1ON);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtr1OFF);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtr1ON);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr1OFF);
                    }                        
                    if( (itemData & TLM_HEATERS_STATE_MASK_2) == 0 ) {
                        //---------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr2ON);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtr2OFF);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtr2ON);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr2OFF);
                    }                        
                    if( (itemData & TLM_HEATERS_STATE_MASK_3) == 0 ) {
                        //---------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr3ON);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtr3OFF);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtr3ON);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr3OFF);
                    }                        
                    if( (itemData & TLM_HEATERS_STATE_MASK_4) == 0 ) {
                        //---------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr4ON);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtr4OFF);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtr4ON);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr4OFF);
                    }                        
                    if( (itemData & TLM_HEATERS_STATE_MASK_5) == 0 ) {
                        //---------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr5ON);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtr5OFF);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtr5ON);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr5OFF);
                    }                        
                    if( (itemData & TLM_HEATERS_STATE_MASK_6) == 0 ) {
                        //---------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr6ON);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtr6OFF);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtr6ON);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr6OFF);
                    }                        
                    if( (itemData & TLM_HEATERS_STATE_MASK_7) == 0 ) {
                        //---------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr7ON);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtr7OFF);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtr7ON);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr7OFF);
                    }                        
                    if( (itemData & TLM_HEATERS_STATE_MASK_8) == 0 ) {
                        //---------------------------------
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr8ON);
                        gui.panelHeaters_cmd.showButtonActiveOFF(gui.panelHeaters_cmd.btnHtr8OFF);
                    } else {
                        gui.panelHeaters_cmd.showButtonActiveON(gui.panelHeaters_cmd.btnHtr8ON);
                        gui.panelHeaters_cmd.showButtonInactive(gui.panelHeaters_cmd.btnHtr8OFF);
                    }                        
                    
                    break;
                    
                case modemA_Offset_ndx:
                    break;
                case modemA_Deviation_ndx:
                    break;
                case modemA_Source_ndx:
                    if( itemData == 0 ) {
                        gui.panelModems_cmd.showButtonInactive(gui.panelModems_cmd.btnModemASourceAnalog);
                        gui.panelModems_cmd.showButtonActiveON(gui.panelModems_cmd.btnModemASourceDigital);
                    } else if( itemData == 1 ) {
                        gui.panelModems_cmd.showButtonActiveON(gui.panelModems_cmd.btnModemASourceAnalog);
                        gui.panelModems_cmd.showButtonInactive(gui.panelModems_cmd.btnModemASourceDigital);
                    }
                    break;
                case modemA_CTCSS_ndx:
                    break;
                case modemB_Offset_ndx:
                    break;
                case modemB_Deviation_ndx:
                    break;
                case modemB_Source_ndx:
                    if( itemData == 0 ) {
                        gui.panelModems_cmd.showButtonInactive(gui.panelModems_cmd.btnModemBSourceAnalog);
                        gui.panelModems_cmd.showButtonActiveON(gui.panelModems_cmd.btnModemBSourceDigital);
                    } else if( itemData == 1 ) {
                        gui.panelModems_cmd.showButtonActiveON(gui.panelModems_cmd.btnModemBSourceAnalog);
                        gui.panelModems_cmd.showButtonInactive(gui.panelModems_cmd.btnModemBSourceDigital);
                    }
                    break;
                case modemB_CTCSS_ndx:
                    break;
                case modemB_Baud_ndx:
                    break;
                case modemB_Scrambler_ndx:
                    if( itemData == 0 ) {
                        gui.panelModems_cmd.showButtonInactive(gui.panelModems_cmd.btnModemBScrambleActivate);
                        gui.panelModems_cmd.showButtonActiveOFF(gui.panelModems_cmd.btnModemBScrambleDeactivate);
                    } else if( itemData == 1 ) {
                        gui.panelModems_cmd.showButtonActiveON(gui.panelModems_cmd.btnModemBScrambleActivate);
                        gui.panelModems_cmd.showButtonInactive(gui.panelModems_cmd.btnModemBScrambleDeactivate);
                    }
                    break;

                case payloads_State_ndx:
                    if( (itemData & TLM_PAYLOADS_STATE_MASK_POWER) != 0 ) {
                        gui.panelPayload_cmd.showButtonActiveON(gui.panelPayload_cmd.btnPayloadON);
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayloadOFF);
                    } else {
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayloadON);
                        gui.panelPayload_cmd.showButtonActiveOFF(gui.panelPayload_cmd.btnPayloadOFF);
                    }
                    if( (itemData & TLM_PAYLOADS_STATE_MASK_1) != 0 ) {
                        gui.panelPayload_cmd.showButtonActiveON(gui.panelPayload_cmd.btnPayload1Activate);
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload1Deactivate);
                    } else {
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload1Activate);
                        gui.panelPayload_cmd.showButtonActiveOFF(gui.panelPayload_cmd.btnPayload1Deactivate);
                    }
                    if( (itemData & TLM_PAYLOADS_STATE_MASK_2) != 0 ) {
                        gui.panelPayload_cmd.showButtonActiveON(gui.panelPayload_cmd.btnPayload2Activate);
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload2Deactivate);
                    } else {
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload2Activate);
                        gui.panelPayload_cmd.showButtonActiveOFF(gui.panelPayload_cmd.btnPayload2Deactivate);
                    }
                    if( (itemData & TLM_PAYLOADS_STATE_MASK_3) != 0 ) {
                        gui.panelPayload_cmd.showButtonActiveON(gui.panelPayload_cmd.btnPayload3Activate);
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload3Deactivate);
                    } else {
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload3Activate);
                        gui.panelPayload_cmd.showButtonActiveOFF(gui.panelPayload_cmd.btnPayload3Deactivate);
                    }
                    if( (itemData & TLM_PAYLOADS_STATE_MASK_4) != 0 ) {
                        gui.panelPayload_cmd.showButtonActiveON(gui.panelPayload_cmd.btnPayload4Activate);
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload4Deactivate);
                    } else {
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload4Activate);
                        gui.panelPayload_cmd.showButtonActiveOFF(gui.panelPayload_cmd.btnPayload4Deactivate);
                    }
                    if( (itemData & TLM_PAYLOADS_STATE_MASK_5) != 0 ) {
                        gui.panelPayload_cmd.showButtonActiveON(gui.panelPayload_cmd.btnPayload5Activate);
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload5Deactivate);
                    } else {
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload5Activate);
                        gui.panelPayload_cmd.showButtonActiveOFF(gui.panelPayload_cmd.btnPayload5Deactivate);
                    }
                    if( (itemData & TLM_PAYLOADS_STATE_MASK_6) != 0 ) {
                        gui.panelPayload_cmd.showButtonActiveON(gui.panelPayload_cmd.btnPayload6Activate);
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload6Deactivate);
                    } else {
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload6Activate);
                        gui.panelPayload_cmd.showButtonActiveOFF(gui.panelPayload_cmd.btnPayload6Deactivate);
                    }
                    if( (itemData & TLM_PAYLOADS_STATE_MASK_7) != 0 ) {
                        gui.panelPayload_cmd.showButtonActiveON(gui.panelPayload_cmd.btnPayload7Activate);
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload7Deactivate);
                    } else {
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload7Activate);
                        gui.panelPayload_cmd.showButtonActiveOFF(gui.panelPayload_cmd.btnPayload7Deactivate);
                    }
                    if( (itemData & TLM_PAYLOADS_STATE_MASK_8) != 0 ) {
                        gui.panelPayload_cmd.showButtonActiveON(gui.panelPayload_cmd.btnPayload8Activate);
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload8Deactivate);
                    } else {
                        gui.panelPayload_cmd.showButtonInactive(gui.panelPayload_cmd.btnPayload8Activate);
                        gui.panelPayload_cmd.showButtonActiveOFF(gui.panelPayload_cmd.btnPayload8Deactivate);
                    }
                    
                    break;
                    //TLM_PAYLOADS_STATE_MASK_POWER     = 0x4000;
                    //TLM_PAYLOADS_STATE_MASK_1	    = 0x0001;
                    //TLM_PAYLOADS_STATE_MASK_2	    = 0x0002;
                    //TLM_PAYLOADS_STATE_MASK_3	    = 0x0004;
                    //TLM_PAYLOADS_STATE_MASK_4	    = 0x0008;
                    //TLM_PAYLOADS_STATE_MASK_5	    = 0x0010;
                    //TLM_PAYLOADS_STATE_MASK_6	    = 0x0020;
                    //TLM_PAYLOADS_STATE_MASK_7	    = 0x0040;
                    //TLM_PAYLOADS_STATE_MASK_8	    = 0x0080;

                case rodXA_State_ndx:
                    if( (itemData & TLM_RODS_STATE_MASK_ENABLE) != 0 ) {
                        gui.panelACS_cmd.showButtonActiveON(gui.panelACS_cmd.btnTorqueXAON);
                        gui.panelACS_cmd.showButtonInactive(gui.panelACS_cmd.btnTorqueXAOFF);
                    } else {
                        gui.panelACS_cmd.showButtonInactive(gui.panelACS_cmd.btnTorqueXAON);
                        gui.panelACS_cmd.showButtonActiveOFF(gui.panelACS_cmd.btnTorqueXAOFF);
                    }
                    break;
                case rodXB_State_ndx:
                    //if( (itemData & TLM_RODS_STATE_MASK_ENABLE) != 0 ) {
                    //    gui.btnTorqueXBON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/active_on.gif")));
                    //    gui.btnTorqueXBOFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/inactive_off.gif")));
                    //} else {
                    //    gui.btnTorqueXBON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/active_off.gif")));
                    //    gui.btnTorqueXBOFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/inactive_on.gif")));
                    //}
                    break;
                case rodYA_State_ndx:
                    if( (itemData & TLM_RODS_STATE_MASK_ENABLE) != 0 ) {
                        gui.panelACS_cmd.showButtonActiveON(gui.panelACS_cmd.btnTorqueYAON);
                        gui.panelACS_cmd.showButtonInactive(gui.panelACS_cmd.btnTorqueYAOFF);
                    } else {
                        gui.panelACS_cmd.showButtonInactive(gui.panelACS_cmd.btnTorqueYAON);
                        gui.panelACS_cmd.showButtonActiveOFF(gui.panelACS_cmd.btnTorqueYAOFF);
                    }
                    break;
                case rodYB_State_ndx:
                    //if( (itemData & TLM_RODS_STATE_MASK_ENABLE) != 0 ) {
                    //    gui.btnTorqueYBON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/active_on.gif")));
                    //    gui.btnTorqueYBOFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/inactive_off.gif")));
                    //} else {
                    //    gui.btnTorqueYBON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/active_off.gif")));
                    //    gui.btnTorqueYBOFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/inactive_on.gif")));
                    //}
                    break;
                case rodZA_State_ndx:
                    if( (itemData & TLM_RODS_STATE_MASK_ENABLE) != 0 ) {
                        gui.panelACS_cmd.showButtonActiveON(gui.panelACS_cmd.btnTorqueZAON);
                        gui.panelACS_cmd.showButtonInactive(gui.panelACS_cmd.btnTorqueZAOFF);
                    } else {
                        gui.panelACS_cmd.showButtonInactive(gui.panelACS_cmd.btnTorqueZAON);
                        gui.panelACS_cmd.showButtonActiveOFF(gui.panelACS_cmd.btnTorqueZAOFF);
                    }
                    break;
                case rodZB_State_ndx:
                    //if( (itemData & TLM_RODS_STATE_MASK_ENABLE) != 0 ) {
                    //    gui.btnTorqueZBON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/active_on.gif")));
                    //    gui.btnTorqueZBOFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/inactive_off.gif")));
                    //} else {
                    //    gui.btnTorqueZBON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/active_off.gif")));
                    //    gui.btnTorqueZBOFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/qsgs/inactive_on.gif")));
                    //}
                    break;
                    //TLM_RODS_STATE_MASK_ENABLE        = 0x8000;
                    //TLM_RODS_STATE_MASK_DUTY_CYCLE    = 0x0FFF;

                //======================    
                case charge1_Status_ndx:
                //======================    
                    if( (itemData & TLM_CHARGE_STATUS_MASK_ENABLE) != 0 ) {
                        gui.panelPower_cmd.showButtonActiveON(gui.panelPower_cmd.btnDAC1ON);
                        gui.panelPower_cmd.showButtonInactive(gui.panelPower_cmd.btnDAC1OFF); 
                    } else {
                        gui.panelPower_cmd.showButtonInactive(gui.panelPower_cmd.btnDAC1ON);
                        gui.panelPower_cmd.showButtonActiveOFF(gui.panelPower_cmd.btnDAC1OFF);
                    }
                    break;
                //======================    
                case charge2_Status_ndx:
                //======================
                    if( (itemData & TLM_CHARGE_STATUS_MASK_ENABLE) != 0 ) {
                        gui.panelPower_cmd.showButtonActiveON(gui.panelPower_cmd.btnDAC2ON);
                        gui.panelPower_cmd.showButtonInactive(gui.panelPower_cmd.btnDAC2OFF);
                    } else {
                        gui.panelPower_cmd.showButtonInactive(gui.panelPower_cmd.btnDAC2ON);
                        gui.panelPower_cmd.showButtonActiveOFF(gui.panelPower_cmd.btnDAC2OFF);
                    }
                    break;
                    //TLM_CHARGE_STATUS_MASK_ENABLE     = 0x8000;
                    //TLM_CHARGE_STATUS_MASK_DUTY_CYCLE = 0x0FFF;

                //=========================    
                case solarArray_Status_ndx:
                //=========================    
                    if( (itemData & TLM_SOLAR_ARRAYS_STATUS_MASK_FEED_1) != 0 ) {
                        gui.panelPower_cmd.showButtonActiveON(gui.panelPower_cmd.btnPanel1Normal);
                        gui.panelPower_cmd.showButtonInactive(gui.panelPower_cmd.btnPanel1Shutdown);
                    } else {
                        gui.panelPower_cmd.showButtonInactive(gui.panelPower_cmd.btnPanel1Normal);
                        gui.panelPower_cmd.showButtonActiveOFF(gui.panelPower_cmd.btnPanel1Shutdown);
                    }
                    if( (itemData & TLM_SOLAR_ARRAYS_STATUS_MASK_FEED_2) != 0 ) {
                        gui.panelPower_cmd.showButtonActiveON(gui.panelPower_cmd.btnPanel2Normal);
                        gui.panelPower_cmd.showButtonInactive(gui.panelPower_cmd.btnPanel2Shutdown);
                    } else {
                        gui.panelPower_cmd.showButtonInactive(gui.panelPower_cmd.btnPanel2Normal);
                        gui.panelPower_cmd.showButtonActiveOFF(gui.panelPower_cmd.btnPanel2Shutdown);
                    }
                    
                    break;
                    
                //======    
                default:
                //======    
                    break;
            }
            nbItemsDecoded++;
            //System.out.println("index: "+itemIndex);
            if( (itemIndex < nbTelemItems) && (itemIndex >= 0) ) {                
                rawData[itemIndex] = itemData;                
                gui.panelTelemetry.tblRawTelem.setValueAt( new Integer(itemIndex), itemIndex, 0 );
                gui.panelTelemetry.tblRawTelem.setValueAt( strItemDesc[itemIndex], itemIndex, 1 );
                //gui.panelTelemetry.tblRawTelem.setValueAt( new Integer((int)tlmPacket[i+1] & 0xFF), itemIndex, 0 );
                //gui.panelTelemetry.tblRawTelem.setValueAt( new Integer((int)tlmPacket[i+2] & 0xFF), itemIndex, 1 );
                gui.panelTelemetry.tblRawTelem.setValueAt( new Integer(itemData), itemIndex, 2 ); 
                timeLastRefresh[itemIndex] = onboardTime;
                gui.panelTelemetry.tblRawTelem.setValueAt( new Long(timeLastRefresh[itemIndex]), itemIndex, 3 ); 
            } else {
                System.out.println("Bad telemetry item #: " + itemIndex );
            }
        }
        
        return( ""+ nbItemsDecoded + " telemetry items received and decoded" );
    }

    public int getNbItems(){
        return( nbTelemItems );
    }
    
    public void loadItemDescr( String fileName, DialogAbout dialogProgress ) {
        
        /*-------------------------------------------*/
        /* Open the telemetry itesm description file */
        /*-------------------------------------------*/
        FileInputStream fis = null;
        DataInputStream dis = null;
        String lineRead = null;
        
        try {
            String message = "Loading telemetry specs from file "+baseDir+fileName+"\n";
            if( dialogProgress != null )
                dialogProgress.appendInfo(message);
            fis = new FileInputStream(baseDir+fileName);
            dis = new DataInputStream( fis );
            int i = 0;
            while( (dis.available()>0) ) {
                lineRead = dis.readLine();
                strItemDesc[i++] = lineRead;
            }
            fis.close();
        } catch( IOException ioe ) {
            String message = "Error loading telemetry item descriptions from file "+baseDir+fileName+": "+ioe;
            if( dialogProgress == null )
                System.out.print(message);
            else
                dialogProgress.appendInfo(message);
        }                
        
    }
    
}
