package ca.gc.space.quicksat.trackingapplet;


import ca.gc.space.quicksat.ground.tracking.*;
import ca.gc.space.quicksat.ground.tracking.Satellite;
import java.util.Enumeration;
import java.util.Vector;

// Referenced classes of package com.csa.qks.serv:
//            GroundStation, KepsFetcher, Satellite, Server

public class Sender
implements Runnable {
    
    KepsFetcher myFetcher;
    GroundStation agency;
    SerializedContainer container;
    Vector dataToSend;
    String data;
    Server myServer;
    boolean ok2;
    boolean ok1;
    Vector theSats;
    public Sender(KepsFetcher myFetcher, Server myServer) {
        data = null;
        ok2 = false;
        ok1 = false;
        
        this.myServer = myServer;
        this.myFetcher = myFetcher;
        this.theSats=myFetcher.theSats;
        
        agency = new GroundStation("montreal", "10.20.36.71", 45.5D, 73.582999999999998D, 0);
        Thread mySelf = new Thread(this, "Sender thread");
        mySelf.start();
    }
    public void run() {
        try {
            
            SatDataToSend myData;
            while (true) {
                if (theSats != null) {
                    container = new SerializedContainer();
                    dataToSend = new Vector();
                    Enumeration canadaSats = theSats.elements();
                    while (canadaSats.hasMoreElements()) {
                        
                        myData = new SatDataToSend();
                        Satellite aSat = (Satellite) canadaSats.nextElement();
                        aSat.calculatePosition(agency, System.currentTimeMillis());
                        myData.setName(aSat.getName());
                        myData.setLat(aSat.getLatitude());
                        myData.setLongi(aSat.getLongitude());
                        myData.setAlt(aSat.getAltitudeKm());
                        myData.setSpeed(aSat.getVelocityKmH());
                        
                        dataToSend.addElement(myData);
                    }
                    
                    try {
                        container.setMySatsDatas(dataToSend);
                        myServer.sendSatellitesDataToAllClients(container);
                    } catch (Exception _ex) {
                    }
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException _ex) {
                    }
                }
            }
        } catch (Exception s) {
            s.printStackTrace();
            System.exit(0);
        }
    }
}
