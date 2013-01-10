/*
 * DirRequest.java
 *
 * Created on May 7, 2001, 2:24 PM
 */

package ca.gc.space.quicksat.ground.pacsat;
import java.util.*;
/**
 *
 * @author  jfcusson
 * @version 
 */
public class DirRequest extends java.lang.Object {
//PID = 0xbd
private int flags;      //unsigned char
private int block_size; //int
private boolean isDirectoryFillRequest;
private int     versionIdentifier;
private Vector  dirHoleList;
/*
      flags          - A bit field as follows:
      
           7  6  5  4  3  2  1
          /-------------------\
          |*  *  1  V  V  C  C|
          \-------------------/
      
      CC                  Two bit field as follows:
      
                          00   directory fill request.
                          01   reserved
                          10   reserved
                          11   reserved
      
      VV                  Two bit version identifier.  This version is 00.
      
      1                   Always 1 indicates a client-generated frame.
      
      *                   Reserved, must be 0.
*/    


    /** Creates new DirRequest */
    public DirRequest() {
    }
    public String toString() { return(""); }

}
