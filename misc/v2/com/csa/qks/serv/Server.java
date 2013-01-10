// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:18:46
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Server.java

package com.csa.qks.serv;

import com.csa.qks.cont.SerializedContainer;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

// Referenced classes of package com.csa.qks.serv:
//            ClientLink

public class Server
    implements Runnable
{

    public Server()
    {
        clientsConnected = null;
        threadWaitingForClients = null;
        threadMonitor = null;
        clientWait = null;
        clientsConnected = new Vector();
        try
        {
            clientWait = new ServerSocket(9902);
            System.out.println("Waiting on port 9902 for clients");
            threadWaitingForClients = new Thread(this);
            threadWaitingForClients.start();
        }
        catch(Exception e)
        {
            System.out.println("Server error: " + e);
        }
    }

    public Vector getClientsConnected()
    {
        return clientsConnected;
    }

    public static void main(String arg[])
    {
        new Server();
    }

    public void run()
    {
        if(Thread.currentThread() == threadWaitingForClients)
        {
            System.out.println("Waiting for clients");
            do
                try
                {
                    clientSocket = clientWait.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    ClientLink clientServer = new ClientLink(clientSocket, this);
                    Thread thread = new Thread(clientServer, "client" + clientSocket.getInetAddress());
                    thread.start();
                    clientsConnected.add(clientServer);
                    try
                    {
                        Thread.sleep(1000L);
                    }
                    catch(InterruptedException _ex) { }
                }
                catch(Exception e)
                {
                    System.out.println("Server error: " + e);
                }
            while(true);
        } else
        {
            return;
        }
    }

    public synchronized void sendSatellitesDataToAllClients(SerializedContainer cont)
        throws Exception
    {
        for(int i = 0; i < clientsConnected.size(); i++)
        {
            ClientLink clt = (ClientLink)clientsConnected.get(i);
            if(clt != null && clt.isConnectionAlive())
                try
                {
                    clt.sendSatellitesData(cont);
                }
                catch(Exception _ex)
                {
                    System.out.println("Removing client at position " + i + " :" + clt.socket.getInetAddress());
                    clt.terminate();
                    clt = null;
                    clientsConnected.removeElementAt(i);
                }
        }

    }

    Vector clientsConnected;
    Thread threadWaitingForClients;
    Thread threadMonitor;
    Socket clientSocket;
    final int clientPort = 9902;
    ServerSocket clientWait;
}