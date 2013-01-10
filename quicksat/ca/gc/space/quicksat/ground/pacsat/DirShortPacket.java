/*
 * DirShortPacket.java
 *
 * Created on November 9, 2001, 11:27 AM
 */

package ca.gc.space.quicksat.ground.pacsat;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class DirShortPacket extends Packet {

    /** Creates new DirShortPacket */
    public DirShortPacket( int fileNumber ) {
        setDIR_SHORT_CMD();
        /*-------------------------------------------*/
        /* Little endian 4 bytes unsigned integer... */
        /*-------------------------------------------*/
        byte[] data = new byte[4];
        data[0] = (byte)(fileNumber & 0x000000FF);
        data[1] = (byte)(((fileNumber & 0x0000FF00) >> 8) & 0xFF);
        data[2] = (byte)(((fileNumber & 0x00FF0000) >> 16) & 0xFF);
        data[3] = (byte)(((fileNumber & 0xFF000000) >> 24) & 0xFF);
        setDataBytes( data );
    }

}
