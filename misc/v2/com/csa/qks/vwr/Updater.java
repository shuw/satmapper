// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:17:39
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Updater.java

package com.csa.qks.vwr;

import com.csa.qks.cont.SatDataToSend;
import java.awt.Component;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

// Referenced classes of package com.csa.qks.vwr:
//            SatMapper, Sprite, SatData

public class Updater extends Thread
{

    public Updater(SatMapper applet)
    {
        this.applet = applet;
        setPriority(10);
       // start();
    }

    public void run()
    {
        do
        {
            if(applet.followSat && applet.zoomMapThread == null && applet.satSelected != null)
                applet.moveScreen(applet.getXCoord(applet.satSelected.getLong()) + Math.abs(applet.offScreenX), applet.getYCoord(applet.satSelected.getLat()) + Math.abs(applet.offScreenY));
            SatDataToSend newData;
            int alt;
            int speed;
            long lat;
            long longi;
            for(Enumeration enum = applet.myData.getSats().elements(); enum.hasMoreElements(); applet.updatePosition(newData.getName(), longi, lat, speed, alt))
            {
                newData = (SatDataToSend)enum.nextElement();
                alt = newData.getAlt();
                speed = newData.getSpeed();
                lat = newData.getLat();
                longi = newData.getLongi();
                if(longi < 180L)
                    longi = -longi;
                else
                if(longi > 180L)
                    longi = 360L - longi;
                System.out.println(newData.getName() + "  LAT: " + lat + "  LONG:  " + longi + "  Altitude: " + alt + " Speed: " + speed);
            }

            applet.repaint();
        } while(true);
    }

    public SatMapper applet;
}