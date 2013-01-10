/*
 * Link.java
 *
 * Created on April 12, 2001, 2:21 PM
 */

package ca.gc.space.quicksat.ground.radio;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class Link extends java.lang.Object {
String description = "";
String frequency = "";
int    intFrequency = 0;
    
    /*------------------------------------------------------------------------*/
    /** Creates new Link. Note that the frequence string must not contain
     *  any dots, and must be in Hertz
     *  @PARAM freq Frequency in Hertz, no dots (ex: 145836000 for 145.836 MHz)
     *  @PARAM desc A string describing this link, to be used in menus...     */
    /*------------------------------------------------------------------------*/
    public Link(String freq,String desc) {
    /*------------------------------------------------------------------------*/    
        description = desc;
        frequency = freq;
        try{
            intFrequency = Integer.parseInt( freq );
        } catch( NumberFormatException nfe ) {
            intFrequency = 0;
        }
    }

    public int getFrequencyIntValue() {
        return( intFrequency );
    }
    
    public String toString() {
        return( description );
    }
    
}
