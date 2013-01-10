/*
 * RequestHeader.java
 *
 * Created on May 7, 2001, 1:43 PM
 */

package ca.gc.space.quicksat.ground.pacsat;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class RequestHeader extends java.lang.Object {

private int flags;
private int file_id;
private int block_size;

private boolean isRequestToStartSendingFile;
private boolean isRequestToStopSendingFile;
private boolean isContainingHoleList;
private int     versionIdentifier;

/*
 flags          - A bit field as follows:

      7  6  5  4  3  2  1  0
    /-----------------------\
    | *  *  *  1  V  V  C  C|
    \-----------------------/

CC                  Two bit field as follows:

               00   start sending file 
               01   stop sending file 
               10   Frame contains a hole list.  

VV                  Two bit version identifier.  This version is 0.

1                   Always 1.

*                   Reserved, must be 0.


block_size      Requests that the broadcast use this value as a maximum 
size. 

file_id         File id of the requested file.  
 **/

    /** Creates new RequestHeader */
    public RequestHeader() {
    }

}
