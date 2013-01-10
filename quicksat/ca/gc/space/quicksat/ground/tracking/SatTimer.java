/*
 * Timer.java
 *
 * Created on April 12, 2002, 10:00 PM
 */

package ca.gc.space.quicksat.ground.tracking;
import java.awt.Component;
/**
 *
 * @author  User
 * @version
 */
public class SatTimer extends Thread{
    int speedTimeX;
    long timeMillis;
    long timeIncrementMillis;
    int waitMillis = 50;
    
    /** Creates new Timer */
    public SatTimer(int speedTimeX) {
        this(System.currentTimeMillis(), speedTimeX, 50);
    }
    
    public SatTimer(long time, int speedTimeX, int updateTimeIntervalMillis) {
        timeMillis = System.currentTimeMillis();
        setSpeed(speedTimeX);
        waitMillis = updateTimeIntervalMillis;
        this.start();
    }
    
    public void setUpdateTimeIntervalMillis(int updateTimeIntervalMillis) {
        waitMillis = updateTimeIntervalMillis;
        setSpeed(this.speedTimeX);
    }
    
    public void setSpeed(int speedTimeX) {
        this.speedTimeX = speedTimeX;
        timeIncrementMillis = speedTimeX * waitMillis;
    }
    
    public void setTime(long timeMillis) {
        this.timeMillis = timeMillis;
    }
    
    public long getTime() { return timeMillis; }
    
    public int getSpeedTimeX() { return speedTimeX; }
    
    synchronized public void run() {
        while (true) {
            this.setPriority(MAX_PRIORITY);
            timeMillis += timeIncrementMillis;
            try { Thread.sleep(waitMillis); } catch (InterruptedException _ex) { }
        }
    }
    
}
