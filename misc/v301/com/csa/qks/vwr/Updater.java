// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:17:39
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3)
// Source File Name:   Updater.java

package com.csa.qks.vwr;
import com.csa.qks.serv.*;
import java.net.*;

import java.applet.Applet;
import java.io.ObjectInputStream;

import com.csa.qks.cont.SatDataToSend;
import java.awt.Component;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;


import java.util.*;
import com.csa.qks.cont.SerializedContainer;
import java.applet.Applet;
import java.io.ObjectInputStream;
import java.io.*;
import java.net.*;

// Referenced classes of package com.csa.qks.vwr:
//            SatMapper, Sprite, SatData

public class Updater extends Thread {
    long nowTime = 0;
    long timeJump = 0;
    long timeSelected = 0;
    Object obj;
    
    public Updater(SatMapper applet) {
        this.applet = applet;
        
        
        //predownloads the satellite info for local version
        try {
            //            URL url = new URL("http://10.20.35.192/data/sats.dat");
            //            URLConnection acsC = url.openConnection();
            
            File file = new File("c:\\SatMapper\\v3\\sats.dat");
            FileInputStream fileIn = new FileInputStream(file);
            
            //            java.io.ObjectInputStream objin = new java.io.ObjectInputStream(url.openStream());
            java.io.ObjectInputStream objin = new java.io.ObjectInputStream(fileIn );
            obj = objin.readObject();
        }
        catch(Exception e) {e.printStackTrace();}
        
        
        //setPriority(10);
        start();
        
        
    }
    
    public void run() {
        do {
            if (applet.isLocal) {
                try {
                    System.out.println(" Error connecting to CSA server, Switching to Local Version");
                    
                    // Excepotion is throw if CSA PredServer is down
                    //We will use local time to calculate sats positions
                    
                    Vector builtData;
                    SatDataToSend data;
                    
                    com.csa.qks.serv.GroundStation agency = new com.csa.qks.serv.GroundStation("montreal", "10.20.36.71", 45.5D, 73.582999999999998D, 0);
                    
                    
                    //                    URL url = new URL("http://10.20.35.192/data/sats.dat");
                    //                    URLConnection acsC = url.openConnection();
                    //                    java.io.ObjectInputStream objin = new java.io.ObjectInputStream(url.openStream());
                    //                    Object obj = objin.readObject();
                    
                    
                    if (obj instanceof com.csa.qks.cont.SerializedCanSats) {
                        com.csa.qks.cont.SerializedCanSats sats = (com.csa.qks.cont.SerializedCanSats) obj;
                        
                        long timeElapsedCheck = 0;
                        while (applet.isLocal) {
                            builtData = new Vector();
                            Enumeration canadaSats = sats.getSats().elements();
                            
                            boolean createPaths = false;
                            timeSelected = System.currentTimeMillis() + applet.myData.timeJump;
                            if (  Math.abs(timeElapsedCheck - timeSelected) > 1300*1000   ||
                            (  !applet.myData.pathsReceivedBySatMapper ) ) {
                                createPaths = true;
                                nowTime = System.currentTimeMillis();
                                timeElapsedCheck = timeSelected;
                                System.out.println("PATH CREATED");
                            }
                            
                            while (canadaSats.hasMoreElements()) {
                                data = new SatDataToSend();
                                Satellite aSat = (Satellite) canadaSats.nextElement();
                                aSat.calculatePosition(agency, timeSelected );
                                data.setName(aSat.getName());
                                data.setLat(aSat.getLatitude());
                                data.setLongi(aSat.getLongitude());
                                data.setAlt(aSat.getAltitudeKm());
                                data.setSpeed(aSat.getVelocityKmH());
                                
                                if (createPaths)  createPath(data, aSat);
                                //only creates new path every half hour
                                
                                
                                builtData.addElement(data); //dosen't send new path
                            }
                            
                            applet.myData.timeNow = nowTime;
                            timeJump = applet.myData.timeJump;
                            

                            
                            applet.myData.available = false; //tells other classes the data has been updated
                            applet.myData.setSats(builtData);
                            updateSats();
                            
                            //updateSats(true); //updates on demand
                            try { Thread.sleep(applet.updateInterval);}
                            catch (Exception e) {}
                        }
                    }
                } catch (Exception e) {}
            }
            else {
                System.out.println("Connected to Server"); updateSats(); //updates whenever receiver receives
            }
            try {Thread.sleep(1000); } catch(Exception e) {}
        } while(true);
    }
    public void updateSats() {
        if(applet.followSat && applet.satSelected != null)
            applet.moveScreen(applet.getXCoord(applet.satSelected.getLong()) + Math.abs(applet.offScreenX), applet.getYCoord(applet.satSelected.getLat()) + Math.abs(applet.offScreenY));
        SatDataToSend newData;
        
        Enumeration enum;
        for (  enum = applet.myData.sats.elements(); enum.hasMoreElements();  applet.updatePosition( newData ) ) {
            newData = (SatDataToSend)enum.nextElement();
        }
        applet.updateContacts();
        applet.repaint();
        
    }
    
    public void createPath(SatDataToSend myData, Satellite aSat) {
        com.csa.qks.serv.GroundStation agency = new com.csa.qks.serv.GroundStation("montreal", "10.20.36.71", 45.5D, 73.582999999999998D, 0);
        int maxArraySize = 500;
        double[] longCoords = new double[maxArraySize];
        double[] latCoords = new double[maxArraySize];
        long[] timeCoords = new long[maxArraySize];
        long[] colorCoords = new long[maxArraySize];
        int longCoordsSize = 0;
        
        //the coordinates of the path going west from satellite
        double[] longCoords2 = new double[maxArraySize];
        double[] latCoords2 = new double[maxArraySize];
        long[] timeCoords2 = new long[maxArraySize];
        int longCoords2Size = 0;
        
        //        aSat.calculatePosition(agency, System.currentTimeMillis());
        
        long timeNow = timeSelected;
        
        
        //        double timeInterval = sat.getSpeed() * constant; //determines how often to get coordinates
        long timeInterval = ( 30000 / aSat.getVelocityKmH() ) * 30000;
        long time = timeNow; //test code
        
        double longCoord;
        double latCoord;
        boolean timeElapsed = false;
        boolean endOfMapReached = false;
        int i = 0;
        do {
            aSat.calculatePosition(agency, time);
            if ( time > timeNow + 3600 * 1000 * 2 ) //calculates path for next 2 hours minimum
                timeElapsed  = true;
            
            longCoord = aSat.getLongitude();
            latCoord = aSat.getLatitude();
            
            if(longCoord < 180L) longCoord = -longCoord;
            else if(longCoord > 180L) longCoord = 360L - longCoord;
            
            if (i > 0 && timeElapsed) {
                if ( Math.abs(longCoord - longCoords[i-1] ) > 180 || Math.abs(latCoord - latCoords[i-1] ) > 90 )
                    endOfMapReached = true; //stops the loop
            }
            
            longCoords[i] = longCoord;
            latCoords[i] = latCoord;
            timeCoords[i] = time;
            
            time += timeInterval; i++;
            
        } while (i < maxArraySize - 50 && !endOfMapReached);
        
        //        //draws the track where it travels near montreal
        //        aSat.calculatePosition(agency, System.currentTimeMillis());
        //        long timeAOS = (long)(aSat.getAOS());
        //
        //        System.out.println( "AOS: " + timeAOS + "LOS: " );//+ aSat.getNextLOSMinutes() );
        //        do {
        ////            System.out.println("YAH");
        //            aSat.calculatePosition(agency, timeAOS + timeNow);
        //
        //            longCoord = aSat.getLongitude();
        //            latCoord = aSat.getLatitude();
        //
        //            if(longCoord < 180L) longCoord = -longCoord;
        //            else if(longCoord > 180L) longCoord = 360L - longCoord;
        //
        //            longCoords[i] = longCoord;
        //            latCoords[i] = latCoord;
        //            timeCoords[i] = timeAOS+ timeNow;
        //
        //            timeAOS += timeInterval;
        //            i++;
        //            aSat.calculatePosition(agency, System.currentTimeMillis());
        //
        //
        //        }   while ( i < maxArraySize && i < 20 );
        longCoordsSize = i;
        
        
        
        
        
        
        
        //draws the path for the past until the screen ends
        time = timeNow;
        
        
        i = 0; endOfMapReached = false;
        do {
            time -= timeInterval;
            
            aSat.calculatePosition(agency, time);
            
            longCoord = aSat.getLongitude();
            latCoord = aSat.getLatitude();
            
            if(longCoord < 180L) longCoord = -longCoord;
            else if(longCoord > 180L) longCoord = 360L - longCoord;
            if (i > 0) {
                if ( Math.abs(longCoord - longCoords2[i-1] ) > 180 || Math.abs(latCoord - latCoords2[i-1] ) > 90 )
                    endOfMapReached = true; //stops the loop
            }
            
            
            longCoords2[i] = longCoord;
            latCoords2[i] = latCoord;
            timeCoords2[i] = time;
            
            
            
            
            i++;
            
            
        } while ( !endOfMapReached && i < maxArraySize && time > timeNow - 3600 * 1000 * 3);
        
        
        longCoords2Size = i;
        
        
        int size = longCoordsSize + longCoords2Size;
        
        double[][] pathCoords = new double[3][size];
        
        
        i = 0;
        while ( longCoords2Size > 0 ) {
            //            System.out.println("i: " + longCoords2Size);
            
            pathCoords[0][i] = longCoords2[ longCoords2Size -1 ];
            pathCoords[1][i] = latCoords2[ longCoords2Size -1 ];
            pathCoords[2][i] = timeCoords2[ longCoords2Size -1 ];
            longCoords2Size--;
            i++;
            
        }
        
        int k = 0;
        while ( k < longCoordsSize  ) {
            pathCoords[0][i] = longCoords[ k ];
            pathCoords[1][i] = latCoords[ k ];
            pathCoords[2][i] = timeCoords[ k ];
            //            System.out.println("TIme2: " + pathTime[i]);
            i++; k++;
        }
        myData.setPathCoords( pathCoords );
    }
    
    
    public SatMapper applet;
}