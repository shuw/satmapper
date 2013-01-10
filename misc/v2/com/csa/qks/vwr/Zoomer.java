// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:17:40
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Zoomer.java

package com.csa.qks.vwr;

import java.awt.Component;

// Referenced classes of package com.csa.qks.vwr:
//            SatMapper

public class Zoomer extends Thread
{

    public Zoomer(SatMapper applet)
    {
        this.applet = applet;
        setPriority(MAX_PRIORITY);
        setName("Zoomer");
        start();
    }

    public void run()
    {
        int i;
        if(applet.zoom <= (double)applet.zoomTo)
            i = 1;
        else
            i = -1;
        int frames = 50;
        applet.prevMapX = applet.mapX;
        applet.prevMapY = applet.mapY;
        applet.prevZoom = applet.zoom;
        for(; Math.abs((double)applet.zoomTo - applet.gridZoom) > 0.29999999999999999D; applet.repaint())
        {
            applet.gridZoom += 0.25D * (double)i;
            try
            {
                Thread.sleep(100L);
            }
            catch(InterruptedException _ex) { }
        }

        applet.gridZoom = applet.zoomTo;
        if(applet.zoom <= (double)applet.zoomTo)
        {
            while((double)i * ((double)applet.zoomTo - applet.mapZoom) > 0.0D) 
            {
                applet.mapZoom += (1.0D / (double)frames) * (double)i;
                if((double)i * ((double)applet.zoomTo - applet.mapZoom) < 0.0D)
                    applet.mapZoom = applet.zoomTo;
                try
                {
                    Thread.sleep(1000 / frames);
                }
                catch(InterruptedException _ex) { }
                applet.mapOffScreenX = -(int)((applet.mapZoom / applet.prevZoom) * ((double)applet.mapClickX - (double)applet.prevMapX / applet.mapZoom / 2D));
                if(applet.mapOffScreenX > 0)
                    applet.mapOffScreenX = 0;
                else
                if((double)applet.mapOffScreenX < 700D - 700D * applet.mapZoom)
                    applet.mapOffScreenX = (int)(700D - 700D * applet.mapZoom);
                applet.mapOffScreenY = -(int)((applet.mapZoom / applet.prevZoom) * ((double)applet.mapClickY - (double)applet.prevMapY / applet.mapZoom / 2D));
                if(applet.mapOffScreenY > 0)
                    applet.mapOffScreenY = 0;
                else
                if((double)applet.mapOffScreenY < 350D - 350D * applet.mapZoom)
                    applet.mapOffScreenY = (int)(350D - 350D * applet.mapZoom);
                applet.repaint();
            }
            while((double)i * ((double)applet.zoomTo - applet.zoom) > 0.0D) 
            {
                applet.zoom += (1.0D / (double)frames) * (double)i;
                try
                {
                    Thread.sleep(1000 / frames);
                }
                catch(InterruptedException _ex) { }
                applet.calcZoom(applet.zoom, applet.mapClickX, applet.mapClickY);
                applet.repaint();
            }
        }
        applet.zoom = applet.zoomTo;
        applet.calcZoom(applet.zoom, applet.mapClickX, applet.mapClickY);
        applet.mapZoom = applet.zoomTo;
        applet.mapOffScreenX = applet.offScreenX;
        applet.mapOffScreenY = applet.offScreenY;
        applet.repaint();
        Thread.currentThread().stop();
    }

    public SatMapper applet;
}