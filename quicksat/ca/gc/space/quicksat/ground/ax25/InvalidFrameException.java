/*
 * InvalidFrameException.java
 *
 * Created on October 19, 2001, 10:32 AM
 */

package ca.gc.space.quicksat.ground.ax25;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class InvalidFrameException extends java.lang.Exception {
String errorMsg = null;

    /*========================================================================*/
    /**Creates new <code>InvalidFrameException</code> without detail message. */
    /*========================================================================*/
    public InvalidFrameException() {
    /*========================================================================*/    
        errorMsg = "Invalid Frame";
    }

    /*========================================================================*/
    /**Constructs an <code>InvalidFrameException</code> with the specified 
     * detail message.
     * @param msg the detail message.                                         */
    /*========================================================================*/
    public InvalidFrameException(String msg) {
    /*========================================================================*/    
        super(msg);
        errorMsg = msg;
    }
    
    /*========================================================================*/
    /** Returns the code of the error that created this exception.
     * @return For now no code: returns 0                                     */
    /*========================================================================*/
    public int    getErrorCode()  { return 0; }
    /*========================================================================*/
    
    
    /*========================================================================*/
    /** Returns a message giving info on what created this exception.
     * @return A relevant message.                                            */
    /*========================================================================*/
    public String getMessage()    { return errorMsg; }
    /*========================================================================*/
    
    /*========================================================================*/
    /** Returns a message giving info on what created this exception.
     * @return A relevant message.                                            */
    /*========================================================================*/
    public String toString()    { return errorMsg; }
    /*========================================================================*/
    
}


