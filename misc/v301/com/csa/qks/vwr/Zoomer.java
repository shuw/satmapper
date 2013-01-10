// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:17:40
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3)
// Source File Name:   Zoomer.java

package com.csa.qks.vwr;

import java.awt.Component;

// Referenced classes of package com.csa.qks.vwr:
//            SatMapper

public class Zoomer extends Thread {
    
    public  Zoomer(SatMapper applet) {
        applet.zoomerFinished = false;
        this.applet = applet;
        setPriority(MAX_PRIORITY);
        setName("Zoomer");
        start();
    }
    
    synchronized public void run() {
        applet.zoomerFinished = false;
        applet.satSelected = null;
        applet.followSat = false;
        
        int i;
        if (applet.zoom <= (double) applet.zoomTo)
            i = 1;
        else
            i = -1;
        int frames = 24;
        applet.prevMapX = applet.mapX;
        applet.prevMapY = applet.mapY;
        applet.prevZoom = applet.zoom;
        
        int centerX = applet.mapWindowX / 2 + Math.abs(applet.offScreenX);
        int centerY = applet.mapWindowY / 2 + Math.abs(applet.offScreenY);
        
        for (; Math.abs((double) applet.zoomTo - applet.gridZoom) > 0.29999999999999999D; applet.repaint()) {
            applet.gridZoom += 0.25D * (double) i;
            try {
                Thread.sleep(100L);
            } catch (InterruptedException _ex) {
            }
        }
        applet.gridZoom = applet.zoomTo;
        
        if (applet.zoom <= (double) applet.zoomTo) {
            while (i * (applet.zoomTo - applet.mapZoom) > 0) {
                
                applet.mapZoom += (1.0D / (double) frames) * (double) i;
                if ((double) i * ((double) applet.zoomTo - applet.mapZoom) < 0.0D)
                    applet.mapZoom = applet.zoomTo;
                
                int zoomX = (int) ((double) centerX + (double) (applet.mapClickX - centerX) * (1.0D - ((double) applet.zoomTo - applet.mapZoom) / ((double) applet.zoomTo - applet.prevZoom)));
                int zoomY = (int) ((double) centerY + (double) (applet.mapClickY - centerY) * (1.0D - ((double) applet.zoomTo - applet.mapZoom) / ((double) applet.zoomTo - applet.prevZoom)));
                
                applet.calcMapZoom(applet.mapZoom, zoomX, zoomY);
                
                try { Thread.sleep(1000 / frames); } catch (InterruptedException e) { }
                applet.repaint();                
            }
            
            while ((double) i * ((double) applet.zoomTo - applet.zoom) > 0.0D) {
                applet.zoom += (1.0D / (double) frames) * (double) i;
                
                int zoomX = (int) ((double) centerX + (double) (applet.mapClickX - centerX) * (1.0D - ((double) applet.zoomTo - applet.zoom) / ((double) applet.zoomTo - applet.prevZoom)));
                int zoomY = (int) ((double) centerY + (double) (applet.mapClickY - centerY) * (1.0D - ((double) applet.zoomTo - applet.zoom) / ((double) applet.zoomTo - applet.prevZoom)));
                
                applet.calcZoom(applet.zoom, zoomX, zoomY);                
                
                try { Thread.sleep(1000 / frames); } catch (InterruptedException e) { }
                applet.repaint();
            }
        } 
        applet.zoom = applet.zoomTo;        
        applet.mapZoom = applet.zoomTo;
        
        //for zoom out calculation
        applet.calcZoom(applet.zoom, applet.mapClickX, applet.mapClickY);
        applet.calcMapZoom(applet.zoom, applet.mapClickX, applet.mapClickY);               
        
        applet.repaint();
        applet.zoomerFinished = true;
        Thread.currentThread().stop();
        //Thread.currentThread().destroy();
        //this.destroy();
        System.gc();
        
    }
    
    public SatMapper applet;
}