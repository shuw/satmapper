package ca.gc.space.quicksat.ground.tracking;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;
import ca.gc.space.quicksat.ground.tracking.*;
// Referenced classes of package com.csa.qks.serv:
//            Satellite

public class KepsFetcher extends Thread {
    
    String baseDir;
    Vector satVector;
    String celestrakRessources;
    String celestrakAmateur;
    String celestrakScience;
    String celestrakVisual;
    String dir;
    String freq;
    Vector link;
    Vector list;
    Vector theSats;
    ObjectOutputStream objOut;
    FileOutputStream fout;
    SatMapper mapper;
    
    public KepsFetcher(String dir, double freq, SatMapper mapper) {
        
        this.satVector = new Vector();
        this.mapper = mapper;
        System.out.println("Starting Initialization process" + "[" + initialize() + "]");
        System.out.println("Done");
        
        this.start();
        
    }
    public boolean buildSatsFromKeps(Vector fileList) {
        
        Enumeration filelist = fileList.elements();
        while (filelist.hasMoreElements()) {
            String file = (String) filelist.nextElement();
            FileInputStream fis = null;
            DataInputStream dis = null;
            String lineRead = null;
            try {
                if (dir != null) {
                    fis = new FileInputStream(this.dir + file);
                } else {
                    fis = new FileInputStream(file);
                }
                dis = new DataInputStream(fis);
                System.out.println("Found file '" + dir + file + "': Loading keps");
                while (dis.available() > 0) {
                    lineRead = dis.readLine().trim();
                    if (lineRead.length() > 0) {
                        if (lineRead.length() > 20)
                            lineRead = lineRead.substring(0, 19);
                        Satellite sat = new Satellite(lineRead);
                        String firstLine = dis.readLine().trim();
                        if (firstLine.length() < 69) {
                            sat = null;
                        } else {
                            String secondLine = dis.readLine().trim();
                            if (secondLine.length() < 69) {
                                sat = null;
                            } else {
                                sat.setKeps(firstLine, secondLine);
                                
                                satVector.addElement(sat);
                            }
                        }
                    }
                }
            } catch (IOException _ex) {
                System.out.println("Sat data file '" + dir + file + "' not found");
                _ex.printStackTrace();
                return false;
            }
        }
        
        mapper.myData.allSats = satVector;
        
        return true;
        
    }
    public boolean downloadKeps(String from, String to) {
        try {
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
            return true;
        }
        catch(Exception d) {
            d.printStackTrace();
            return false;
        }
    }
    public boolean initialize() {
            
            dir = "c:\\netscape\\server4\\docs\\";
            freq = "1";
//            list = AppEnvironment.getInstance().getList();
//            link = AppEnvironment.getInstance().getLinks();
            link =  new Vector();
            link.addElement("http://www.celestrak.com/NORAD/elements/resource.txt");
             link.addElement("http://www.celestrak.com/NORAD/elements/science.txt");
//link3=http://www.celestrak.com/NORAD/elements/amateur.txt
//link4=http://www.celestrak.com/NORAD/elements/tle-new.txt
//            
            
            return true;

    }
    public void run() {
        try {
            while (true) {
                
                System.out.println("Starting Kepplerian maintenance process");
                
                int files = 1;
                Enumeration enum = link.elements();
                while (enum.hasMoreElements()) {
                    String url = (String) enum.nextElement();
                    System.out.println("Downloading from [ " + url + " ]   [" + downloadKeps(url, "keps" + files + ".txt") + " ]");
                    files++;
                }
                
                System.out.println("Done");
                System.out.println("");
                System.out.println("Starting satellites creation process");
                Vector fileList = new Vector();
                for (int f = 1; f < files; f++) {
                    String file = "keps" + f + ".txt";
                    fileList.addElement(file);
                }
                
                
                buildSatsFromKeps(fileList);
                
                
                System.out.println("[The Keplerian updater is now going to sleep for  " + freq + " days]");
                Thread.currentThread();
                Thread.sleep(Integer.parseInt(freq) * 24 * 60 * 60 * 1000);
            }
        } catch (Exception e) {
            System.out.println("Could not download keps");
            e.printStackTrace();
        }
    }
}
