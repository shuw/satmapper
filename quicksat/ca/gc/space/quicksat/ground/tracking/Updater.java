package ca.gc.space.quicksat.ground.tracking;


import java.awt.Component;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

// Referenced classes of package com.csa.qks.vwr:
//            SatMapper, Sprite, SatData

public class Updater extends Thread {
    
    public SatMapper applet;
    public Updater(SatMapper applet) {
        this.applet = applet;
        //setPriority(10);
        // start();
    }
    public void run() {
        do {
            if(applet.followSat && applet.satSelected != null)
                applet.moveScreen(applet.getXCoord(applet.satSelected.getLong()) + Math.abs(applet.offScreenX), applet.getYCoord(applet.satSelected.getLat()) + Math.abs(applet.offScreenY));
            SatDataToSend newData;
            int alt;
            int speed;
            double lat;
            double longi;
            Enumeration e;
            for (  e = applet.myData.getSats().elements(); e.hasMoreElements();  applet.updatePosition( newData ) ) {
                newData = (SatDataToSend)e.nextElement();
            }
            applet.updateContacts();
            applet.repaint();
        } while(true);
    }
}
