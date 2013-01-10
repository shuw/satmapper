package ca.gc.space.quicksat.ground.tracking;


import ca.gc.space.quicksat.ground.tracking.SerializedCanSats;
import java.util.*;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.net.*;

// Referenced classes of package com.csa.qks.vwr:
//            SatData

public class Receiver extends Thread {
    
    public static final String HOST = "10.20.35.192";
    public static final int port = 9901;
    ObjectInputStream in;
    JApplet parent;
    URL baseURL;
    Socket socket;
    SatData incommingData;
    public boolean isConnected;
    SatMapper mapper;
    
    long currentTime = 0;
    long timeJump = 0;
    long timeSelected = 0;
    
    public Receiver(SatData incommingData) {
        this.incommingData = incommingData;
        setPriority(9);
        start();
    }
    public Receiver(SatData incommingData,SatMapper mapper,JApplet parent) {
        this.mapper= mapper;
        this.parent = parent;
        this.incommingData = incommingData;
        setPriority(9);
        start();
    }
    public boolean connect() {
        try {
            System.out.println("Connecting to server");
            
            if(!isConnected){
                socket = new Socket(HOST, port);
                in = new ObjectInputStream(socket.getInputStream());
                isConnected=true;
                return true;
            }
            else{
                return true;
                
            }
        }
        catch(Exception e) {
            //  e.printStackTrace();
            isConnected=false;
            mapper.isLocal = false;
            System.out.println("Could not connect");
        }
        return false;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/26/2002 3:11:02 PM)
     */
    
    public static void main(String args[]) {
        new Receiver(new SatData());
    }
    public void receive() throws Exception{
        try {
            if (isConnected) {
                System.out.println("Server Connected");
                while (!mapper.isLocal) {
                    Object obj = in.readObject();
                    if (obj instanceof SerializedContainer) {
                        SerializedContainer cont = (SerializedContainer) obj;
                        incommingData.setSats(cont.getMySatsDatas());
                    }
                    try {Thread.sleep(4000L);} catch(Exception e) {}
                }
            }
        } catch (Exception e) {
            System.out.println("Server Connected, but could not receive");
            isConnected = false;
            //Server has disconnected the client
            //warns that local version is starting
            WarningFrame warn = new WarningFrame();
            warn.show();
            warn.setVisible(true);
        }
    }
    public void run() {
        boolean error = false;
        
        while (!error) {
            try {
                if (connect()) {
                    mapper.isLocal = false;
                    isConnected = true;
                    receive();
                }
                else {
                    //warns that could not connect to server
                    //some kind of error with server
                    WarningFrame warn = new WarningFrame();
                    warn.show();
                    warn.setVisible(true);
                }
                
                localReceiver();
                
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            }
            
            try { Thread.sleep(2000);}
            catch (Exception e) {}
        }
    }
    
    public void localReceiver() throws Exception {
        mapper.isLocal = true;
        System.out.println("Starting Local Version");
        
        
        
        // Excepotion is throw if CSA PredServer is down
        //We will use local time to calculate sats positions
        
        Vector builtData;
        SatDataToSend data;
        
        GroundStation agency = new GroundStation("montreal", "10.20.36.71", 45.5D, 73.582999999999998D, 0);
        //        System.out.println(parent.getDocumentBase());
        URL url = new URL(mapper.documentBase, "data//sats.dat" );
        URLConnection acsC = url.openConnection();
        java.io.ObjectInputStream objin = new java.io.ObjectInputStream(url.openStream());
        
        SatTimer myTimer = new SatTimer(1);
        long prevTimeJump = -234234324; //prev time jump in satData, used to see whether timejump as changed
        long originalTimeJump = 0; //original time jump in satdata, used as base to increment timeJump
        
        Object obj = objin.readObject();
        if (obj instanceof ca.gc.space.quicksat.ground.tracking.SerializedCanSats) {
            SerializedCanSats sats = (ca.gc.space.quicksat.ground.tracking.SerializedCanSats) obj;
            
            long timeElapsedCheck = 0;
            while (mapper.isLocal) {
                builtData = new Vector();
                Enumeration canadaSats = sats.getSats().elements();
                currentTime = System.currentTimeMillis();
                //                System.out.println("Size: " + sats.getSats().size());
                boolean createPaths = false;
                timeSelected = currentTime + mapper.myData.timeJump;
                if (  Math.abs(timeElapsedCheck - timeSelected) > 1300*1000   ||
                (  !mapper.myData.pathsReceivedBySatMapper ) ) {
                    createPaths = true;
                    timeElapsedCheck = timeSelected;
                    System.out.println("PATH CREATED");
                }
                //                System.out.println("Data updated");
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
                
                //code used to forward time
                if (mapper.myData.timeSpeedX != 1 && myTimer == null)
                    myTimer = new SatTimer(0, mapper.myData.timeSpeedX, 50);
                
                if (myTimer != null) {
                    //timeJump has changed because user has selected another time
                    if (mapper.myData.timeSpeedX == 1)
                        myTimer = null;
                    else {
                        if (mapper.myData.timeJump != prevTimeJump ) {
                            myTimer.setSpeed(mapper.myData.timeSpeedX);
                            myTimer.setTime(0);
                            originalTimeJump = mapper.myData.timeJump;
                        }
                        else  myTimer.setSpeed(mapper.myData.timeSpeedX);
                        timeJump = originalTimeJump + myTimer.getTime();
                        mapper.myData.timeJump = timeJump;
                        prevTimeJump = mapper.myData.timeJump;
                    }
                }
                else timeJump = mapper.myData.timeJump;
                
                mapper.myData.timeNow = currentTime;
                
                
                mapper.myData.available = false; //tells other classes the data has been updated
                mapper.myData.setSats(builtData);
                
                //updateSats(true); //updates on demand
                try { Thread.sleep(mapper.myData.updateInterval);}
                catch (Exception e) {}
            }
            
        }
    }
    
    public void createPath(SatDataToSend myData, Satellite aSat) {
        GroundStation agency = new GroundStation("montreal", "10.20.36.71", 45.5D, 73.582999999999998D, 0);
        int maxArraySize = 300;
        double[] longCoords = new double[maxArraySize];
        double[] latCoords = new double[maxArraySize];
        long[] timeCoords = new long[maxArraySize];
        long[] colorCoords = new long[maxArraySize];
        int longCoordsSize = 0;
        
        //the coordinates of the path going west from satellite
        int maxArraySize2 = 75;
        double[] longCoords2 = new double[maxArraySize];
        double[] latCoords2 = new double[maxArraySize];
        long[] timeCoords2 = new long[maxArraySize];
        int longCoords2Size = 0;
        
        //        aSat.calculatePosition(agency, System.currentTimeMillis());
        
        long timeNow = timeSelected;
        
        
        //        double timeInterval = sat.getSpeed() * constant; //determines how often to get coordinates
        long timeInterval = ( 30000 / aSat.getVelocityKmH() ) * 60000;
        long time = timeNow; //test code
        
        double longCoord;
        double latCoord;
        boolean timeElapsed = false;
        
        int i = 0;
        do {
            aSat.calculatePosition(agency, time);
            if ( time > timeNow + 3600 * 1000 * 2 ) //calculates path for next 2 hours minimum
                timeElapsed  = true;
            
            longCoord = aSat.getLongitude();
            latCoord = aSat.getLatitude();
            
            if(longCoord < 180L) longCoord = -longCoord;
            else if(longCoord > 180L) longCoord = 360L - longCoord;
            
            
            longCoords[i] = longCoord;
            latCoords[i] = latCoord;
            timeCoords[i] = time;
            
            time += timeInterval; i++;
            
        } while (i < maxArraySize );
        
        longCoordsSize = i;
        
        //draws the path for the past until the screen ends
        time = timeNow;
        
        
        i = 0;
        do {
            time -= timeInterval;
            
            aSat.calculatePosition(agency, time);
            
            longCoord = aSat.getLongitude();
            latCoord = aSat.getLatitude();
            
            if(longCoord < 180L) longCoord = -longCoord;
            else if(longCoord > 180L) longCoord = 360L - longCoord;
            
            
            longCoords2[i] = longCoord;
            latCoords2[i] = latCoord;
            timeCoords2[i] = time;
            
            i++;
        } while ( i < maxArraySize2 );
        
        
        longCoords2Size = i;
        
        
        int size = longCoordsSize + longCoords2Size;
        
        double[][] pathCoords = new double[3][size];
        
        
        i = 0;
        while ( longCoords2Size > 0 ) {
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
}
