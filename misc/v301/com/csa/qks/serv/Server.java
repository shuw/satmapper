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

    Vector clientsConnected;
    Thread threadWaitingForClients;
    Thread threadMonitor;
    Socket clientSocket;
    //default port is 9902
    final int clientPort = 9900;
    ServerSocket clientWait;
    public Server()
    {
        clientsConnected = null;
        threadWaitingForClients = null;
        threadMonitor = null;
        clientWait = null;
        clientsConnected = new Vector();
        try
        {
            clientWait = new ServerSocket(clientPort);
            System.out.println("Waiting on port"+clientPort+" for clients");
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
            System.out.println("[The server is online and waiting for clients]");
            do
                try
                {
                    clientSocket = clientWait.accept();
                    System.out.println("A new client has connected: " + clientSocket.getInetAddress());
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
}
