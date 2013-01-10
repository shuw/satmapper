package ca.gc.space.quicksat.trackingapplet;

import java.awt.image.*;
import java.applet.*;
import java.awt.*;
import java.net.*;
import ca.gc.space.quicksat.ground.tracking.SatMapper;
/**
 * Insert the type's description here.
 * Creation date: (3/13/2002 10:39:04 AM)
 * @author:
 */
public class TrackerLowRes extends javax.swing.JApplet {
    
    
    SatMapper myMapper;
    
    public Image offscreenImage;
    SatMapper satMapper;
    
    public void init() {
        setSize(730, 365);
        
        try {          
            //starts the low res version of SatMapper
            satMapper = new SatMapper(getDocumentBase(), false);
        }
        catch(Exception e){}
        getContentPane().add(satMapper);
    }
    
    public String getAppletInfo() {
        return "Tracker\n" +
        "\n" +
        "Insert the type's description here.\n" +
        "Creation date: (3/13/2002 10:39:04 AM)\n" +
        "@author: \n" +
        "";
    }
}
