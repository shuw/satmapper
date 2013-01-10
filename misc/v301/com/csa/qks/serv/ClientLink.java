// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:18:43
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   ClientLink.java

package com.csa.qks.serv;

import com.csa.qks.cont.SerializedContainer;
import java.io.*;
import java.net.Socket;

// Referenced classes of package com.csa.qks.serv:
//            Server

public class ClientLink
    implements Runnable
{

    public ClientLink(Socket s, Server ps)
    {
        socket = null;
        parentServer = null;
        terminated = false;
        myThread = null;
        outputLink = null;
        out = null;
        outStrm = null;
        socket = s;
        parentServer = ps;
    }

    public boolean isConnectionAlive()
    {
        if(myThread == null)
            return false;
        else
            return myThread.isAlive();
    }

    public void run()
    {
        if(socket == null)
            return;
        myThread = Thread.currentThread();
        try
        {
            java.io.OutputStream os = socket.getOutputStream();
            outStrm = new ObjectOutputStream(os);
            outputLink = new DataOutputStream(os);
            out = new PrintWriter(os, true);
            do
                try
                {
                    Thread.sleep(5000L);
                }
                catch(InterruptedException _ex) { }
            while(true);
        }
        catch(IOException ioe)
        {
            System.out.println("ERROR setting streams in client manager: " + ioe);
        }
    }

    public void sendSatellitesData(SerializedContainer cont)
        throws Exception
    {
        try
        {
            outStrm.writeObject(cont);
        }
        catch(Exception ioe)
        {
            throw ioe;
        }
    }

    public void terminate()
    {
        terminated = true;
        try
        {
            socket.close();
            myThread.stop();
        }
        catch(IOException _ex)
        {
            Thread.currentThread().stop();
            System.out.println("Fatal : Could not close client socket properly");
        }
    }

    Socket socket;
    Server parentServer;
    boolean terminated;
    Thread myThread;
    DataOutputStream outputLink;
    PrintWriter out;
    ObjectOutputStream outStrm;
}