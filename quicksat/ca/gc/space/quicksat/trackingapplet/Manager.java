package ca.gc.space.quicksat.trackingapplet;

import java.io.*;
import java.util.*;
import ca.gc.space.quicksat.ground.tracking.*;

// Referenced classes of package com.csa.qks.serv:
//            Server, KepsFetcher, Sender

public class Manager
{
    public Manager()
    {
    }
public static void main(String args[]) {

    Manager me = new Manager();
    if (AppEnvironment.getInstance().init()) {

        KepsFetcher kf = new KepsFetcher(AppEnvironment.getInstance().getPath(), AppEnvironment.getInstance().getFreq());
        try {
           Thread.sleep(7000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //   Thread mySelf = new Thread(kf, "KepsFetcher");
        //   mySelf.start();

            Server myServer = new Server();
        new Sender(kf, myServer);
        JpgWriter myWriter = new JpgWriter(new SatMapper(AppEnvironment.getInstance().getImagepath()));

    }
}
}
