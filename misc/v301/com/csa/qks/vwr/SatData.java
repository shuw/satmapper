// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:17:37
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3)
// Source File Name:   SatData.java

package com.csa.qks.vwr;

import java.io.Serializable;
import java.util.Vector;

public class SatData
implements Serializable {
    
public long timeNow;
public long timeJump;
public long timeUserSelected;
public boolean pathsReceivedBySatMapper = false;

    public SatData() {
    }
    
    public synchronized Vector getSats() {
        while(!available)
            try {
                wait();
            }
            catch(InterruptedException _ex) { }
            available = false;
            notifyAll();
            return sats;
    }
    
    public synchronized void setSats(Vector newSats) {
        while(available)
            try {
                wait();
            }
            catch(InterruptedException _ex) { }
            sats = newSats;
            available = true;
            notifyAll();
    }
    
    public Vector sats;
    boolean available;
}