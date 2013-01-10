package com.csa.qks.vwr;

import java.awt.image.*;
import java.applet.*;
import java.awt.*;
import java.net.*;
import java.io.*;
/**
 * Insert the type's description here.
 * Creation date: (3/13/2002 10:39:04 AM)
 * @author:
 */
public class Tracker extends javax.swing.JApplet {
    static String mapFile = "mapHiRes.jpg";
    static String satFile = "SatGeneric.gif";
    static String mapCloudsFile = "mapClouds.jpg";
    static String mapNightFile = "mapNight.jpg";
    static String recFile = "Receiver.gif";
    static String recSigtFile = "ReceiverSignal.gif";
    
    
    static URL codeBase;
    static Image satGen;
    static Image mapImg, mapNightImg, mapCloudsImg, satImg, recImg, recSignalImg, shuttleImg, anImg, flameImg1, flameImg2, flameDrawImg;
    static Image alouImg;
    static Image radImg;
    static Image issImg;
    static File localSatData;
    SatMapper myMapper;
    //grid for map
    //  static final int mapWindowX = 700;
    //  static final int mapWindowY = (int) (mapWindowX / 2.0);
    //total screen Size
    //  static final int ScreenX = mapWindowX + 30;
    //  static final int ScreenY = mapWindowY + 15;
    
    //midpoint used for calculating coordinates
    //   int mapMidX = mapWindowX / 2;
    //    int mapMidY = mapWindowY / 2;
    public Image offscreenImage;
    SatMapper satMapper;
    
    /**
     * Returns information about this applet.
     * @return a string of information about this applet
     */
    public String getAppletInfo() {
        return "Tracker\n" +
        "\n" +
        "Insert the type's description here.\n" +
        "Creation date: (3/13/2002 10:39:04 AM)\n" +
        "@author: \n" +
        "";
    }
    public void init() {
        
        
        setSize(730, 365);
        this.codeBase = getDocumentBase();
        
        mapNightImg = getImage(getDocumentBase(), "mapNight.jpg");
        mapCloudsImg = getImage(getDocumentBase(), "mapClouds.jpg");
        mapImg = getImage(getDocumentBase(), "mapHiRes.jpg");
        
        satGen = getImage(getDocumentBase(), "SatGeneric.gif");
        
        recImg = getImage(getDocumentBase(), "Receiver.gif");
        recSignalImg = getImage(getDocumentBase(), "ReceiverSignal.gif");
        alouImg = getImage(getDocumentBase(), "alouetteSat.gif");
        issImg = getImage(getDocumentBase(), "ISS.gif");
        radImg = getImage(getDocumentBase(), "radarSat.gif");
        
        shuttleImg = getImage(getDocumentBase(), "sat83.gif");
        anImg = getImage(getDocumentBase(),"Astronaut.gif");
        flameImg1 = getImage(getDocumentBase(),"Flame1.gif");
        flameImg2 = getImage(getDocumentBase(),"Flame2.gif");
       
        satMapper = new SatMapper();
        
        getContentPane().add(satMapper);
        

    }
    /**
     * Insert the method's description here.
     * Creation date: (3/15/2002 10:47:02 AM)
     */
    public void paint() {
        BufferedImage bi =  new java.awt.image.BufferedImage(200,100,java.awt.image.BufferedImage.TYPE_INT_RGB);
        myMapper.paintComponent(bi.getGraphics());
        
        this.paint(myMapper.offscreenImage.getGraphics());
        
        
    }
}
