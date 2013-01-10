/*
 * FileHeader.java
 *
 * Created on May 7, 2001, 2:42 PM
 */

package ca.gc.space.quicksat.ground.pacsat;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class FileHeader extends Object {
/*Header Format
struct FTL0_PKT {
     unsigned char length_lsb;
     unsigned char h1;
}

  - 8 bit unsigned integer supplying the least significant 8  bits 
of data_length.

 - an 8-bit field.
     bits 7-5 contribute 3 most significant bits to data_length.

     bits 4-0 encode 32 packet types as follows:

     0    DATA
     1    DATA_END
     2    LOGIN_RESP
     3    UPLOAD_CMD
     4    UL_GO_RESP
     5    UL_ERROR_RESP
     6    UL_ACK_RESP
     7    UL_NAK_RESP
     8    DOWNLOAD_CMD
     9    DL_ERROR_RESP
     10   DL_ABORTED_RESP
     11   DL_COMPLETED_RESP
     12   DL_ACK_CMD
     13   DL_NAK_CMD
     14   DIR_SHORT_CMD
     15   DIR_LONG_CMD
     16   SELECT_CMD
     17   SELECT_RESP
          All other values reserved


2.2 Information Length

The  value is formed by pre-pending bits 7-5 of the  
byte to the  byte.   indicates how many more 
bytes will be received before the beginning of the next packet.  If  is 0, there are no information bytes.
 */
    
public final int DATA = 0;
public final int DATA_END =  1;
public final int LOGIN_RESP =     2;
public final int UPLOAD_CMD = 3;
public final int UL_GO_RESP = 4;
public final int UL_ERROR_RESP = 5;
public final int UL_ACK_RESP = 6;
public final int UL_NAK_RESP = 7;
public final int DOWNLOAD_CMD = 8;
public final int DL_ERROR_RESP = 9;
public final int DL_ABORTED_RESP = 10;
public final int DL_COMPLETED_RESP = 11;
public final int DL_ACK_CMD = 12;
public final int DL_NAK_CMD = 13;
public final int DIR_SHORT_CMD = 14;
public final int DIR_LONG_CMD = 15;
public final int SELECT_CMD = 16;
public final int SELECT_RESP = 17;
    
public int type;    //Take one of the above values...
public int length;

    /** Creates new FileHeader */
    public FileHeader() {
    }

}
