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
public class Tracker extends javax.swing.JApplet {
    
    
    SatMapper myMapper;
    
    public Image offscreenImage;
    SatMapper satMapper;
    
    public void init() {
        setSize(730, 365);
        
        try {            
            satMapper = new SatMapper(getDocumentBase());
        }
        catch(Exception e){}
        getContentPane().add(satMapper);
        
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/15/2002 10:47:02 AM)
     */
//    public void paint() {
//        BufferedImage bi =  new java.awt.image.BufferedImage(200,100,java.awt.image.BufferedImage.TYPE_INT_RGB);
//        myMapper.paintComponent(bi.getGraphics());
//        
//        this.paint(myMapper.offscreenImage.getGraphics());
//        
//        
//    }
    
    public String getAppletInfo() {
        return "Tracker\n" +
        "\n" +
        "Insert the type's description here.\n" +
        "Creation date: (3/13/2002 10:39:04 AM)\n" +
        "@author: \n" +
        "";
    }
}
