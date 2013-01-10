// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:18:45
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Manager.java

package com.csa.qks.serv;


// Referenced classes of package com.csa.qks.serv:
//            Server, KepsFetcher, Sender

public class Manager
{

    public Manager()
    {
    }

    public static void main(String args[])
    {
        Server myServer = new Server();
        KepsFetcher kf = new KepsFetcher();
       
        Thread mySelf = new Thread(kf, "KepsFetcher");
        mySelf.start();
        new Sender(kf, myServer);
    }
}