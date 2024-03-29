package ca.gc.space.quicksat.ground.tracking;

import java.io.Serializable;
import java.util.Vector;

public class SatData
implements Serializable {
    public long timeNow;
    public long timeJump;
    public long timeUserSelected;
    public int timeSpeedX = 1;
    public boolean pathsReceivedBySatMapper = false;
    public int updateInterval = 2000;
    
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