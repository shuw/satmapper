// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:18:44
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   KepsFetcher.java

package com.csa.qks.serv;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;

// Referenced classes of package com.csa.qks.serv:
//            Satellite

public class KepsFetcher
    implements Runnable
{

public KepsFetcher() {
	 initialize();
    // baseDir = "c:\\TEMP\\";
    satVector = null;
    celestrakRessources = "http://www.celestrak.com/NORAD/elements/resource.txt";
    celestrakScience = "http://www.celestrak.com/NORAD/elements/science.txt";
    satVector = new Vector();
    downloadKeps(celestrakRessources, "resource.txt");
    downloadKeps(celestrakScience, "science.txt");
    buildSatsFromKeps("resource.txt");
    buildSatsFromKeps("science.txt");
}

    public void buildSatsFromKeps(String file)
    {
        FileInputStream fis = null;
        DataInputStream dis = null;
        String lineRead = null;
        try
        {
            fis = new FileInputStream(this.dir + file);
            dis = new DataInputStream(fis);
            System.out.println("Found file '" + dir + file + "': Loading keps");
            while(dis.available() > 0) 
            {
                lineRead = dis.readLine().trim();
                if(lineRead.length() > 0)
                {
                    if(lineRead.length() > 20)
                        lineRead = lineRead.substring(0, 19);
                    Satellite sat = new Satellite(lineRead);
                    String firstLine = dis.readLine().trim();
                    if(firstLine.length() < 69)
                    {
                        sat = null;
                    } else
                    {
                        String secondLine = dis.readLine().trim();
                        if(secondLine.length() < 69)
                        {
                            sat = null;
                        } else
                        {
                            sat.setKeps(firstLine, secondLine);
                            satVector.addElement(sat);
                        }
                    }
                }
            }
        }
        catch(IOException _ex)
        {
            System.out.println("Sat data file '" + dir + file + "' not found");
             _ex.printStackTrace();
        }
    }

    public void downloadKeps(String from, String to)
    {
        try
        {
            String data = null;
            File keps = new File(this.dir, to);
            if(!keps.exists())
                keps.createNewFile();
            FileOutputStream fout = new FileOutputStream(keps);
            PrintStream out = new PrintStream(fout);
            URL url = new URL(from);
            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            while((data = reader.readLine()) != null) 
                out.println(data);
        }
        catch(Exception d)
        {
            d.printStackTrace();
            
        }
    }

    public void initialize()
    {
        try
        {
            Properties p = new Properties();
            p.load(new FileInputStream("predserver.ini"));
            dir = p.getProperty("DataPath");
            freq = p.getProperty("UpdateFreq");
        }
        catch(Exception e)
        {
            System.out.println("Could not initialize: " + e);
        }
    }

    public static void main(String args[])
    {
        KepsFetcher kf = new KepsFetcher();
        kf.initialize();
        Thread mySelf = new Thread(kf, "KepsFetcher");
        mySelf.start();
    }

    public void run()
    {
        try
        {
            System.out.println("Sleeping for " + freq + " days....");
            Thread.currentThread();
            Thread.sleep(Integer.parseInt(freq) * 24 * 60 * 60 * 1000);
            System.out.println("Downloading KEPS");
            downloadKeps(celestrakRessources, "ressources.txt");
            downloadKeps(celestrakScience, "science.txt");
            buildSatsFromKeps("resource.txt");
        	buildSatsFromKeps("science.txt");
        }
        catch(Exception e)
        {
            System.out.println("Could not download keps");
            e.printStackTrace();
        }
    }

    String baseDir;
    Vector satVector;
    String celestrakRessources;
    String celestrakScience;
    String dir;
    String freq;
}