/*
 * FrameHeader.java
 *
 * Created on May 7, 2001, 1:30 PM
 */

package ca.gc.space.quicksat.ground.pacsat;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class FrameHeader extends java.lang.Object {

    private int flags;                      //unsigned char
    private boolean isLengthFieldPresent;   
    private boolean isLastFrame;            
    private boolean isOffsetInBytes;
    private int versionIdentifier;
    
//   flags          A bit field as follows:
// 
//      7  6  5  4  3  2  1   0
//     /-----------------------\
//     |*  *  E  0  V  V  Of  L |
//     \-----------------------/
//
// L              1    length field is present
//                0    length field not present
//
// Of             1    Offset is a byte offset from the beginning of the file
//                0    Offset is a block number (not currently used
//    
// VV                  Two bit version identifier.  This version is 0.
// 
// 0                   Always 0.
//
// E              1    Last byte of frame is the last byte of the file.
//                0    Not last.
//
// *                   Reserved, must be 0.
//
    
    private int file_id;                    //unsigned long
    private int file_type;                  //unsigned char
    private int offset;                     //unsigned int
    private int offset_msb;                 //unsigned char
    private int length;                     //unsigned int
/*
file_id         A number which identifies an active broadcast file.  All 
frames which are part of the same file are tagged with this number.

file_type       The file_type byte from the file header.  Provided so that 
partial files received without the header can be decoded.  File types are 
defined in a separate document.

length          If the L bit is set, this field is present it in the header.  

It is the number of bits that are to be used in the data field.  This field 
has two intended uses: when variable length blocks are used with the broadcast 
carried inside a higher level protocol (so that the frame length is lost), and 
when a non-integer number of octets of data are used.

offset          If the O bit is not set, this is the block number of the 
block.  If the O bit is set, this is  the offset from the start of the file 
for the first byte in this frame.  This field is the lower  16 bits of a 24 
bit integer.

offset_msb      The high order 8 bits of the offset.

If the block mode is used, the length of the data must be fixed.  It may be 
any length, but the following must be true:

  file_offset_of_first_byte_in_frame = offset * this_frame_size

All frames in a particular transmission of a file should be the same length, 
to avoid having the receiver resize his bit map too often.

If byte numbers rather than block numbers are used, the data may be any 
length.  If the length field is present, the data must contain only that 
number of bits, rounded up to the next octet boundary.
 
 */ 
    
    
    /** Creates new FrameHeader */
    public FrameHeader() {
    }

}
