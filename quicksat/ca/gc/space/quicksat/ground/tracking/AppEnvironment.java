package ca.gc.space.quicksat.ground.tracking;

import java.util.*;
import java.io.*;
import java.util.Properties;

// Referenced classes of package dev:
//            Filter

class AppEnvironment {
    
    private static AppEnvironment singleton = null;
    private String freq;
    private String path;
    private String imagepath;
    private String satdatapath;
    private Vector list = new Vector();
    private Vector links = new Vector();
    
    private AppEnvironment() {
        freq = null;
        path = null;
        imagepath=null;
        
        
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/18/2002 11:08:16 AM)
     * @return java.lang.String
     */
    public java.lang.String getFreq() {
        return freq;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/18/2002 11:08:16 AM)
     * @return java.lang.String
     */
    public java.lang.String getImagepath() {
        return imagepath;
    }
    public static AppEnvironment getInstance() {
        if(singleton == null)
            singleton = new AppEnvironment();
        return singleton;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/20/2002 11:58:17 AM)
     * @return java.util.Vector
     */
    public java.util.Vector getLinks() {
        return links;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/20/2002 11:58:17 AM)
     * @return java.util.Vector
     */
    public java.util.Vector getList() {
        return list;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/18/2002 11:08:16 AM)
     * @return java.lang.String
     */
    public java.lang.String getPath() {
        return path;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/25/2002 10:35:41 AM)
     * @return java.lang.String
     */
    public java.lang.String getSatdatapath() {
        return satdatapath;
    }
    public boolean init() {
        try {
            
            Properties p = new Properties();
            p.load(new FileInputStream("c:\\netscape\\server4\\docs\\predserver.ini"));
            
            System.out.println("APP ENVIRONMENT STARTED");
            path = p.getProperty("DataPath");
            freq = p.getProperty("UpdateFreq");
            imagepath = p.getProperty("ImageDataPath");
            satdatapath = p.getProperty("SatDataPath");
            
            
            
            
            Properties l = new Properties();
            l.load(new FileInputStream("c:\\netscape\\server4\\docs\\satellites.lst"));
            int cnt=1;
            for (int c =0 ; c<l.size();c++) {
                list.addElement(l.getProperty("Satellite"+cnt));
                cnt++;
            }
            
            Properties lk = new Properties();
            lk.load(new FileInputStream("c:\\netscape\\server4\\docs\\links.lst"));
            cnt=1;
            for (int cc =0 ; cc<lk.size();cc++) {
                links.addElement(lk.getProperty("link"+cnt));
                cnt++;
            }
            
            
            
            return true;
        }
        catch(Exception e) {
            System.out.println("----THE SERVER COULD NOT INITIALIZE-----");
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/18/2002 11:08:16 AM)
     * @param newFreq java.lang.String
     */
    public void setFreq(java.lang.String newFreq) {
        freq = newFreq;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/18/2002 11:08:16 AM)
     * @param newImagepath java.lang.String
     */
    public void setImagepath(java.lang.String newImagepath) {
        imagepath = newImagepath;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/20/2002 11:58:17 AM)
     * @param newLinks java.util.Vector
     */
    public void setLinks(java.util.Vector newLinks) {
        links = newLinks;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/20/2002 11:58:17 AM)
     * @param newList java.util.Vector
     */
    public void setList(java.util.Vector newList) {
        list = newList;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/18/2002 11:08:16 AM)
     * @param newPath java.lang.String
     */
    public void setPath(java.lang.String newPath) {
        path = newPath;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/25/2002 10:35:41 AM)
     * @param newSatdatapath java.lang.String
     */
    public void setSatdatapath(java.lang.String newSatdatapath) {
        satdatapath = newSatdatapath;
    }
}
