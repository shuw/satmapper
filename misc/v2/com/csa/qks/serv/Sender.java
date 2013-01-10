// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:18:46
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Sender.java

package com.csa.qks.serv;

import com.csa.qks.cont.SatDataToSend;
import com.csa.qks.cont.SerializedContainer;
import java.util.Enumeration;
import java.util.Vector;

// Referenced classes of package com.csa.qks.serv:
//            GroundStation, KepsFetcher, Satellite, Server

public class Sender
    implements Runnable
{

    public Sender(KepsFetcher myFetcher, Server myServer)
    {
        data = null;
        ok2 = false;
        ok1 = false;
        this.myServer = myServer;
        this.myFetcher = myFetcher;
        agency = new GroundStation("montreal", "10.20.36.71", 45.5D, 73.582999999999998D, 0);
        Thread mySelf = new Thread(this, "Sender thread");
        mySelf.start();
    }

    public void run()
    {
        try
        {
            theSats = new Vector();
            for(Enumeration mySats = myFetcher.satVector.elements(); mySats.hasMoreElements();)
            {
                Satellite mySat = (Satellite)mySats.nextElement();
                if(mySat.getName().equals("RADARSAT"))
                    theSats.addElement(mySat);
                else
                if(mySat.getName().equals("ALOUETTE 1 (S-27)"))
                    theSats.addElement(mySat);
            }

            do
            {
                SatDataToSend myData;
                for(Enumeration canadaSats = theSats.elements(); canadaSats.hasMoreElements(); dataToSend.addElement(myData))
                {
                    container = new SerializedContainer();
                    dataToSend = new Vector();
                    myData = new SatDataToSend();
                    Satellite aSat = (Satellite)canadaSats.nextElement();
                    aSat.calculatePosition(agency, System.currentTimeMillis());
                    myData.setName(aSat.getName());
                    myData.setLat(aSat.getLatitude());
                    myData.setLongi(aSat.getLongitude());
                }

                try
                {
                    container.setMySatsDatas(dataToSend);
                    myServer.sendSatellitesDataToAllClients(container);
                }
                catch(Exception _ex) { }
                try
                {
                    Thread.sleep(5000L);
                }
                catch(InterruptedException _ex) { }
            } while(true);
        }
        catch(Exception s)
        {
            s.printStackTrace();
        }
    }

    KepsFetcher myFetcher;
    GroundStation agency;
    SerializedContainer container;
    Vector dataToSend;
    String data;
    Server myServer;
    boolean ok2;
    boolean ok1;
    Vector theSats;
}