/*
 * DirFrame.java
 *
 * Created on May 7, 2001, 2:20 PM
 */

package ca.gc.space.quicksat.ground.pacsat;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class DirFrame extends java.lang.Object {
/*-----------------------------------------Header*/
private int flags;      //unsigned char
private int file_id;    //unsigned long
private int offset;     //unsigned long
private int t_old;      //time_t
private int t_new;      //time_t

private boolean isFramePFHBroadcast;
private int     versionIdentifier;
private boolean isLastFrame;
private boolean isNewestFile;
/*
      flags          A bit field as follows:
      
           7  6  5  4  3  2  1  0
          /----------------------\
          |*  N  E  0  V  V  T  T|
          \----------------------/
      TT                  Two bit frame type identifier
                          00   PFH broadcast 
                          01   reserved
                          10   reserved
                          11   reserved
      
      VV                  Two bit version identifier.  This version is 00.
      
      0                   Always 0 indicates a server generated frame.
      
      E              1    Last byte of frame is the last byte of the directory PFH.
                     0    Not the last frame.
      
      N              1    This is the newest file on the server.
                     0    This is not the newest file on the server.
      
      *                   Reserved, always 0.
      
      
      file_id    A number which identifies the file.  All directory broadcast 
      frames which are part of the same file's PFH are tagged with this number.
      
      offset     This is  the offset from the start of the PFH for the first data 
      byte in this frame.  
      
      t_old     Number of seconds since 00:00:00 1/1/80. See below.
      
      t_new     Number of seconds since 00:00:00 1/1/80. See below.
      
           There  are no files other than the file  identified  by 
           file_id with t_old <= UPLOAD_TIME <= t_new.    
 */
    
/*-----------------------------------------------*/

private FileHeader fileHeader;

private int crc;
    /** Creates new DirFrame */
    public DirFrame() {
    }

}
