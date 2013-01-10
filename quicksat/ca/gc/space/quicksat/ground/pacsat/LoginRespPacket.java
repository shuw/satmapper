/*
 * LoginRespPacket.java
 *
 * Created on November 5, 2001, 2:45 PM
 */

package ca.gc.space.quicksat.ground.pacsat;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class LoginRespPacket extends Packet {

private int     loginTimeUTC                    = 0;    
private boolean selectionListActiveForThisUser  = false;
private boolean PFHRequired                     = false;
private int     pacsatVersion                   = 0;
    
    /*========================================================================*/
    /** Creates new LoginRespPacket                                           */
    /*========================================================================*/
    public LoginRespPacket()  {
    /*========================================================================*/    
        System.out.println("Parsing LOGIN RESP packet!");
        if( dataBytes == null ) {
            isError = true;
            errorMessage = "Data buffer is NULL: Unable to interpret LOGIN RESP";
            return;
        }
        if( id == ID_UNKNOWN )
            id = ID_LOGIN_RESP;
        if( id != ID_LOGIN_RESP ) {
            isError = true;
            errorMessage = "Internal error #1 interpreting LOGIN RESP";
            return;
        }
        if( dataBytes.length < 5 ) {
            isError = true;
            errorMessage = "Data buffer too short for LOGIN RESP interpretation";
            return;
        }
        loginTimeUTC =  (int)dataBytes[0]&0xFF;
        loginTimeUTC |= (int)(dataBytes[1]&0xFF)<<8;
        loginTimeUTC |= (int)(dataBytes[2]&0xFF)<<16;
        loginTimeUTC |= (int)(dataBytes[3]&0xFF)<<24;
        selectionListActiveForThisUser = ((dataBytes[4]&0x08)==0x08)?true:false;
        PFHRequired = ((dataBytes[4]&0x04)==0x04)?true:false;
        pacsatVersion = (int)(dataBytes[4]&0x03);
    }

    /*========================================================================*/
    /** Creates new LoginRespPacket                                           */
    /*========================================================================*/
    public LoginRespPacket( Packet packet )  {
    /*========================================================================*/    
        System.out.println("Parsing LOGIN RESP packet from Packet!");
        if( packet == null ) {
            System.out.println("ERROR: Packet buffer is NULL!");
            return;
        }
        if( packet.dataBytes == null ) {
            isError = true;
            errorMessage = "Data buffer is NULL: Unable to interpret LOGIN RESP";
            return;
        }
        if( packet.id == ID_UNKNOWN )
            id = ID_LOGIN_RESP;
        if( packet.id != ID_LOGIN_RESP ) {
            isError = true;
            errorMessage = "Internal error #1 interpreting LOGIN RESP";
            return;
        }
        dataBytes = packet.dataBytes;
        packetBytes = packet.packetBytes;
        actualDataLength = packet.actualDataLength;
        reportedDataLength = packet.reportedDataLength;
        if( dataBytes.length < 5 ) {
            isError = true;
            errorMessage = "Data buffer too short for LOGIN RESP interpretation";
            return;
        }
        loginTimeUTC =  (int)dataBytes[0]&0xFF;
        loginTimeUTC |= (int)(dataBytes[1]&0xFF)<<8;
        loginTimeUTC |= (int)(dataBytes[2]&0xFF)<<16;
        loginTimeUTC |= (int)(dataBytes[3]&0xFF)<<24;
        selectionListActiveForThisUser = ((dataBytes[4]&0x08)==0x08)?true:false;
        PFHRequired = ((dataBytes[4]&0x04)==0x04)?true:false;
        pacsatVersion = (int)(dataBytes[4]&0x03);
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public String toString() {
    /*========================================================================*/
        return( super.toString()
                + " > Login time: " + loginTimeUTC 
                + (selectionListActiveForThisUser? " Sel. Active":"")
                + (PFHRequired? " PFH Required":"")
                + " PACSAT Ver." + pacsatVersion );
    }

}
