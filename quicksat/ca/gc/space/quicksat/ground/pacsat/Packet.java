/*
 * Packet.java
 *
 * Created on November 5, 2001, 11:26 AM
 */

package ca.gc.space.quicksat.ground.pacsat;

/**
 *
 * @author  jfcusson
 * @version 
 *
 * Format of the FTL packet:
 *
 * ----------------------------------------------------
 * | LLLL LLLL | MMMT TTTT | data ....... | ... | ... |
 * ----------------------------------------------------
 * Where LLLL LLLL = lsb of the data length 
 *       MMM       = msb of the data length
 *       T TTTT    = packet type
 */
public class Packet {

protected byte[] packetBytes = null;    
protected byte[] dataBytes = null;

/*-------------------------------------------------*/
/* These two should definitely be the same value...*/
/*-------------------------------------------------*/
protected int reportedDataLength  = 0; //Length of data field, as reported
protected int actualDataLength    = 0; //"Real" length of the data field

final int ID_UNKNOWN          = -1;
final int ID_DATA             = 0;
final int ID_DATA_END         = 1;
final int ID_LOGIN_RESP       = 2;
final int ID_UPLOAD_CMD       = 3;
final int ID_UL_GO_RESP       = 4;
final int ID_UL_ERR_RESP      = 5;
final int ID_UL_ACK_RESP      = 6;
final int ID_UL_NACK_RESP     = 7;
final int ID_DOWNLOAD_CMD     = 8;
final int ID_DL_ERR_RESP      = 9;
final int ID_DL_ABORTED_RESP  = 10;
final int ID_DL_COMPLETED_RESP= 11;
final int ID_DL_ACK_CMD       = 12;
final int ID_DL_NACK_CMD      = 13;
final int ID_DIR_SHORT_CMD    = 14;
final int ID_DIR_LONG_CMD     = 15;
final int ID_SELECT_CMD       = 16;
final int ID_SELECT_RESP      = 17;
final int ID_AUTH_CMD         = 18;
final int ID_AUTH_RESP        = 19;
final int ID_SEC_CMD          = 20;
final int ID_SEC_RESP         = 21;

protected int id = ID_UNKNOWN;

final int NO_ERROR                      = -2;
final int ERR_UNKNOWN                   = -1;
final int ERR_ILL_FORMED_CMD            = 1;
final int ERR_BAD_CONTINUE              = 2;
/*.....................................................*/
/*ERR_SERVER_FSYS: Failure of on-board file system.    */
/* There is no file handles to complete the operations */
/*.....................................................*/
final int ERR_SERVER_FSYS               = 3;
final int ERR_NO_SUCH_FILE_NUMBER       = 4;
final int ERR_SELECTION_EMPTY           = 5;
final int ERR_MANDATORY_FIELD_MISSING   = 6;
final int ERR_NO_PFH                    = 7;
final int ERR_POORLY_FORMED_SEL         = 8;
final int ERR_ALREADY_LOCKED            = 9;
final int ERR_NO_SUCH_DESTINATION       = 10;
final int ERR_PARTIAL_FILE              = 11;
final int ERR_FILE_COMPLETE             = 12;
final int ERR_NO_ROOM                   = 13;
final int ERR_BAD_HEADER                = 14;
final int ERR_HEADER_CHECKSUM           = 15;
final int ERR_BODY_CHECK                = 16;

protected int reportedError = NO_ERROR;

/*---------------------------------------------------------------------------*/
/* If we were able to build the frame correctly, set these flags accordingly */
/*---------------------------------------------------------------------------*/
protected boolean isComplete              = false;
protected boolean isError                 = false;

/*-----------------------------------------------------------------*/
/* This string contains a message describing any error encountered */
/* with this frame...                                              */
/*-----------------------------------------------------------------*/
protected String errorMessage         = "";

/*----------------------------------------------------------------------*/
/* Ok, maybe it's not the ideal structure, but I decided to include all */
/* parameter of all possible packet type here. The problem is that we   */
/* cannot "extend" a packet to one of its child once we know its type   */
/* (for example we cannot cast a Packet into a LoginRespPacket once the */
/* Packet has been instanciated as a Packet). So instead of creating a  */
/* new packet of the intended type and copying all values to it every   */
/* then a Packet supports all. Children classes are used solely for     */
/* building new packet easier, but we could do without them...          */
/*----------------------------------------------------------------------*/

/*----------------------------------------------------------------*/
/* Login Resp              Where:                                 */
/* ---------------------   F(0-1) = PACSAT Version                */
/* |   |   |   |   |   |   F(2)   = 1 if PFH required             */
/* |  Login UTC    | F |   F(3)   = 1 if Sel list active for user */
/* ---------------------                                          */
/*----------------------------------------------------------------*/
private int     loginTimeUTC                    = 0;    
private boolean selectionListActiveForThisUser  = false;
private boolean PFHRequired                     = false;
private int     pacsatVersion                   = 0;


    /*========================================================================*/
    /** Creates new Packet                                                    */
    /*========================================================================*/
    public Packet() {
    /*========================================================================*/    
    }

    /*========================================================================*/
    /** Creates new Packet                                                    */
    /*========================================================================*/
    public Packet( byte[] packetBytes ) {
    /*========================================================================*/    
        
        this.packetBytes = packetBytes;
        if( packetBytes == null ) {
            isError = true;
            errorMessage = "Packet buffer is NULL";
            return;
        }
        
        if( packetBytes.length < 2 ) {
            isError = true;
            errorMessage = "Packet buffer too short";            
            return;
        }
        
        /*-------------------------------------------------------------*/
        /* Find the size of the data field. Get it, and then calculate */
        /* it. If both are different we'll flag an error later...      */
        /*-------------------------------------------------------------*/
        reportedDataLength = (packetBytes[0]&0xFF) 
                             | ((int)(packetBytes[1]&0xE0)<<3);
        actualDataLength = packetBytes.length - 2;
        if( actualDataLength != reportedDataLength ) {
            /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
            /* FOR NOW JUST FLAG AN ERROR, MAYBE WE SHOULD ABORT */
            /* AND IGNORE THESE FRAMES!!!!!!!!!                  */
            /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
            isError = true;
            errorMessage = "Data field not same size as reported!";
        }
        
        /*---------------------*/
        /* Get the packet type */
        /*---------------------*/
        id = (int)(packetBytes[1]&0x1F);
        
        /*---------------------------*/
        /* Build the data byte array */
        /*---------------------------*/
        if( actualDataLength > 0 ) {
            dataBytes = new byte[actualDataLength];
            for( int i=0; i<actualDataLength; i++ )
                dataBytes[i] = packetBytes[i+2];            
        }
        
        switch( id ) {
            //..................................................................
            case ID_DATA:
            //..................................................................    
                break;
            //..................................................................    
            case ID_DATA_END:
            //..................................................................    
                break;
            //..................................................................    
            case ID_LOGIN_RESP:
            //..................................................................    
                if( dataBytes.length < 5 ) {
                    isError         = true;
                    errorMessage    = "LOGIN RESP data field too short";
                    loginTimeUTC    = 0;
                    selectionListActiveForThisUser = false;
                    PFHRequired     = false;
                    pacsatVersion   = 0;
                    break;
                }
                loginTimeUTC =  (int)dataBytes[0]&0xFF;
                loginTimeUTC |= (int)(dataBytes[1]&0xFF)<<8;
                loginTimeUTC |= (int)(dataBytes[2]&0xFF)<<16;
                loginTimeUTC |= (int)(dataBytes[3]&0xFF)<<24;
                selectionListActiveForThisUser = 
                                        ((dataBytes[4]&0x08)==0x08)?true:false;
                PFHRequired = ((dataBytes[4]&0x04)==0x04)?true:false;
                pacsatVersion = (int)(dataBytes[4]&0x03);                
                break;
            //..................................................................    
            case ID_UPLOAD_CMD:
            //..................................................................    
                break;
            //..................................................................    
            case ID_UL_GO_RESP:
            //..................................................................    
                break;
            //..................................................................    
            case ID_UL_ERR_RESP:
            //..................................................................    
                break;
            //..................................................................    
            case ID_UL_ACK_RESP:
            //..................................................................    
                break;
            //..................................................................    
            case ID_UL_NACK_RESP:
            //..................................................................    
                break;
            //..................................................................    
            case ID_DOWNLOAD_CMD:
            //..................................................................    
                break;
            //..................................................................    
            case ID_DL_ERR_RESP:
            //..................................................................    
                break;
            //..................................................................    
            case ID_DL_ABORTED_RESP:
            //..................................................................    
                break;
            //..................................................................    
            case ID_DL_COMPLETED_RESP:
            //..................................................................    
                break;
            //..................................................................    
            case ID_DL_ACK_CMD:
            //..................................................................    
                break;
            //..................................................................    
            case ID_DL_NACK_CMD:
            //..................................................................    
                break;
            //..................................................................    
            case ID_DIR_SHORT_CMD:
            //..................................................................    
                break;
            //..................................................................    
            case ID_DIR_LONG_CMD:
            //..................................................................    
                break;
            //..................................................................    
            case ID_SELECT_CMD:
            //..................................................................    
                break;
            //..................................................................    
            case ID_SELECT_RESP:
            //..................................................................    
                break;
            //..................................................................    
            default:
            //..................................................................    
                isError = true;
                errorMessage = "Unrecognized packet type: " + id;
                id = ID_UNKNOWN;
                break;
        }
        isComplete = true;
        return;
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public boolean isComplete() {
    /*========================================================================*/
        return( isComplete );
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public void setDataBytes( byte[] dataBytes ) {
    /*========================================================================*/
        this.dataBytes      = dataBytes;
        reportedDataLength  = dataBytes.length; 
        actualDataLength    = dataBytes.length; 
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public boolean isDATA() {
    /*========================================================================*/
        return( (id==ID_DATA)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDATA() {
    /*========================================================================*/
        id = ID_DATA;
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public boolean isDATA_END() {
    /*========================================================================*/
        return( (id==ID_DATA_END)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDATA_END() {
    /*========================================================================*/
        id = ID_DATA_END;
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public boolean isLOGIN_RESP() {
    /*========================================================================*/
        return( (id==ID_LOGIN_RESP)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setLOGIN_RESP() {
    /*========================================================================*/
        id = ID_LOGIN_RESP;
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public boolean isUPLOAD_CMD() {
    /*========================================================================*/
        return( (id==ID_UPLOAD_CMD)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setUPLOAD_CMD() {
    /*========================================================================*/
        id = ID_UPLOAD_CMD;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isUL_GO_RESP() {
    /*========================================================================*/
        return( (id==ID_UL_GO_RESP)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setUL_GO_RESP() {
    /*========================================================================*/
        id = ID_UL_GO_RESP;
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public boolean isUL_ERR_RESP() {
    /*========================================================================*/
        return( (id==ID_UL_ERR_RESP)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setUL_ERR_RESP() {
    /*========================================================================*/
        id = ID_UL_ERR_RESP;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isUL_ACK_RESP() {
    /*========================================================================*/
        return( (id==ID_UL_ACK_RESP)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setUL_ACK_RESP() {
    /*========================================================================*/
        id = ID_UL_ACK_RESP;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isUL_NACK_RESP() {
    /*========================================================================*/
        return( (id==ID_UL_NACK_RESP)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setUL_NACK_RESP() {
    /*========================================================================*/
        id = ID_UL_NACK_RESP;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isDOWNLOAD_CMD() {
    /*========================================================================*/
        return( (id==ID_DOWNLOAD_CMD)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDOWNLOAD_CMD() {
    /*========================================================================*/
        id = ID_DOWNLOAD_CMD;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isDL_ERR_RESP() {
    /*========================================================================*/
        return( (id==ID_DL_ERR_RESP)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDL_ERR_RESP() {
    /*========================================================================*/
        id = ID_DL_ERR_RESP;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isDL_ABORTED_RESP() {
    /*========================================================================*/
        return( (id==ID_DL_ABORTED_RESP)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDL_ABORTED_RESP() {
    /*========================================================================*/
        id = ID_DL_ABORTED_RESP;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isDL_COMPLETED_RESP() {
    /*========================================================================*/
        return( (id==ID_DL_COMPLETED_RESP)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDL_COMPLETED_RESP() {
    /*========================================================================*/
        id = ID_DL_COMPLETED_RESP;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isDL_ACK_CMD() {
    /*========================================================================*/
        return( (id==ID_DL_ACK_CMD)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDL_ACK_CMD() {
    /*========================================================================*/
        id = ID_DL_ACK_CMD;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isDL_NACK_CMD() {
    /*========================================================================*/
        return( (id==ID_DL_NACK_CMD)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDL_NACK_CMD() {
    /*========================================================================*/
        id = ID_DL_NACK_CMD;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isDIR_SHORT_CMD() {
    /*========================================================================*/
        return( (id==ID_DIR_SHORT_CMD)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDIR_SHORT_CMD() {
    /*========================================================================*/
        id = ID_DIR_SHORT_CMD;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isDIR_LONG_CMD() {
    /*========================================================================*/
        return( (id==ID_DIR_LONG_CMD)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setDIR_LONG_CMD() {
    /*========================================================================*/
        id = ID_DIR_LONG_CMD;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isSELECT_CMD() {
    /*========================================================================*/
        return( (id==ID_SELECT_CMD)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setSELECT_CMD() {
    /*========================================================================*/
        id = ID_SELECT_CMD;
    }

    /*========================================================================*/
    /*========================================================================*/
    public boolean isSELECT_RESP() {
    /*========================================================================*/
        return( (id==ID_SELECT_RESP)?true:false );
    }
    /*========================================================================*/
    /*========================================================================*/
    public void setSELECT_RESP() {
    /*========================================================================*/
        id = ID_SELECT_RESP;
    }

    /*========================================================================*/
    /*========================================================================*/
    public String toString() {
    /*========================================================================*/
        
        StringBuffer str = new StringBuffer(80);
        str.append("PACSAT ");
        if( isError )
            str.append( " ***"+errorMessage+"*** " );
        if( !isComplete )
            str.append( " ***INCOMPLETE*** " );

        switch( id ) {
            case ID_DATA:
                str.append( "{DATA}" );
                break;
            case ID_DATA_END:
                str.append( "{DATA END}" );
                break;
            case ID_LOGIN_RESP:
                str.append( "{LOGIN RESPONSE}"
                            + " > Login time: " + loginTimeUTC 
                            + (selectionListActiveForThisUser?" Sel. Active":"")
                            + (PFHRequired? " / PFH Required":"")
                            + " / PACSAT Ver." + pacsatVersion  );                
                break;
            case ID_UPLOAD_CMD:
                str.append( "{UPLOAD COMMAND}" );
                break;
            case ID_UL_GO_RESP:
                str.append( "{UPLOAD GO RESPONSE}" );
                break;
            case ID_UL_ERR_RESP:
                str.append( "{UPLOAD ERROR RESPONSE}" );
                break;
            case ID_UL_ACK_RESP:
                str.append( "{UPLOAD ACKNOWLEDGE RESPONSE}" );
                break;
            case ID_UL_NACK_RESP:
                str.append( "{UPLOAD NEGATIVE ACKNOWLEDGE RESPONSE}" );
                break;
            case ID_DOWNLOAD_CMD:
                str.append( "{DOWNLOAD COMMAND}" );
                break;                
            case ID_DL_ERR_RESP:
                str.append( "{DOWNLOAD ERROR RESPONSE}" );
                break;
            case ID_DL_ABORTED_RESP:
                str.append( "{DOWNLOAD ABORTED RESPONSE}" );
                break;
            case ID_DL_COMPLETED_RESP:
                str.append( "{DOWNLOAD COMPLETED RESPONSE}" );
                break;
            case ID_DL_ACK_CMD:
                str.append( "{DOWNLOAD ACKNOWLEDGE COMMAND}" );
                break;
            case ID_DL_NACK_CMD:
                str.append( "{DOWNLOAD NEGATIVE ACKNOWLEDGE COMMAND}" );
                break;
            case ID_DIR_SHORT_CMD:
                str.append( "{SHORT DIRECTORY COMMAND}" );
                break;
            case ID_DIR_LONG_CMD:
                str.append( "{LONG DIRECTORY COMMAND}" );
                break;
            case ID_SELECT_CMD:
                str.append( "{SELECT COMMAND}" );
                break;
            case ID_SELECT_RESP:
                str.append( "{SELECT RESPONSE}" );
                break;
            default:
                str.append( "{UNKNOWN PACKET ID!}" );
                break;
        }
        //str.append( " " + actualDataLength
        //            + "(a)/" + reportedDataLength + "(r) data bytes" );
        
        return( str.toString() );
    }

    /*========================================================================*/
    /*========================================================================*/
    public byte[] getBytes() {
    /*========================================================================*/
                        
        /*---------------------------------------------*/
        /* If the byte array does not exist, create it */
        /* (unless we do not know the packet id...)    */
        /*---------------------------------------------*/
        if( (packetBytes==null) && (id!=ID_UNKNOWN) ) {
            if( dataBytes == null ) {
                packetBytes = new byte[2];
                packetBytes[0] = 0;                 //data length lsb
                packetBytes[1] = (byte)(id & 0x1F); //packet id, data length msb
            }
            else {
                packetBytes = new byte[ 2 + dataBytes.length ];
                packetBytes[0] = (byte)(dataBytes.length & 0xFF);  //dlength lsb
                packetBytes[1] = (byte)((dataBytes.length & 0x700)>>>3);//msb
                packetBytes[1] |= (byte)(id & 0x1F);               //packet id
                for( int i=0; i<dataBytes.length; i++ ) {
                    packetBytes[i+2] = dataBytes[i];
                }
            }            
        }
        return( packetBytes );
    }
    
}
