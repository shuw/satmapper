/*
 * CTCSS.java
 *
 * Created on May 30, 2001, 10:52 AM
 */

package ca.gc.space.quicksat.ground.radio;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class CTCSS extends Object {
public String frequency = "00.0";
public String code      = "00";
    
    /** Creates new CTCSS */
    public CTCSS( String freqToUse, String codeToUse ) {
        frequency   = freqToUse;
        code        = codeToUse;
    }
  
    public String toString() {
        return frequency;
    }

}
