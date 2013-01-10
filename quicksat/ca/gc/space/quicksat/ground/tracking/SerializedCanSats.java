package ca.gc.space.quicksat.ground.tracking;

/**
 * Insert the type's description here.
 * Creation date: (3/25/2002 10:11:14 AM)
 * @author: 
 */
 import java.util.*;
public class SerializedCanSats implements java.io.Serializable{
	    Vector sats; 
/**
 * SerializedCanSats constructor comment.
 */
public SerializedCanSats() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (3/25/2002 10:13:05 AM)
 * @return java.util.Vector
 */
public java.util.Vector getSats() {
	return sats;
}
/**
 * Insert the method's description here.
 * Creation date: (3/25/2002 10:13:05 AM)
 * @param newSats java.util.Vector
 */
public void setSats(java.util.Vector newSats) {
	sats = newSats;
}
}
