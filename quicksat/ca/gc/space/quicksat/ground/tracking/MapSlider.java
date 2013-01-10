/*
 * MapSlider.java
 *
 * Created on April 11, 2002, 5:23 PM
 */

package ca.gc.space.quicksat.ground.tracking;

import java.awt.Component;
/*
 This thread will move the map to the specified coordinates with animation
 */
public class MapSlider extends Thread {
    SatMapper parent;
    SatSprite moveToSat;
    double startLong, startLat;
    public double longitude, latitude;
    boolean followSatAfterSlide =false;
    
    /** Creates new MapSlider */
    public MapSlider(SatSprite moveToSat, SatMapper parent) {
        this( moveToSat.getLong(), moveToSat.getLat(), parent);
        this.moveToSat = moveToSat;        
        if (parent.zoom > 1) followSatAfterSlide = true;
    }
    
    public MapSlider(double longitude, double latitude, SatMapper parent) {
        this.parent = parent;
        //starting coordinates for slide, center of screen
        startLong = parent.getLongCoord(parent.mapWindowX / 2);
        startLat = parent.getLatCoord(parent.mapWindowY / 2);
        
        //final coordinates for slide
        this.longitude = longitude;
        this.latitude = latitude;
        
        if (parent.myZoomer == null) start();
        else Thread.currentThread().stop();
    }
    synchronized public void run() {
        //animates the movement that centers currently selected satellite
        
        parent.followSat = false;
        
        
        double moveLong = (longitude - startLong);
        double moveLat = (latitude - startLat);
        
        if ( Math.abs(moveLong) > 180 ) {
            if (moveLong < 0) moveLong = (moveLong + 360);
            else moveLong = moveLong - 360;
        }
        
        for (double i = 0; i <= 1; i+=0.05) {
            if (followSatAfterSlide) { //move to point is continuosly moving if the satellite is moving
                moveLong = (moveToSat.getLong() - startLong);
                moveLat = (moveToSat.getLat() - startLat);
                if ( Math.abs(moveLong) > 180 ) {
                    if (moveLong < 0) moveLong = (moveLong + 360);
                    else moveLong = moveLong - 360;
                }
            }
            
            double currentLong = startLong + moveLong *  Math.sqrt(i);
            if ( currentLong < -180) currentLong += 360;
            
            double currentLat = startLat + moveLat *  Math.sqrt(i);
            
            
            parent.moveScreen(currentLong , currentLat);
            
            try { Thread.sleep(50L); } catch (InterruptedException _ex) { }
            parent.repaint();
        }
        
        parent.repaint();
        if (followSatAfterSlide == true) parent.followSat = true;
        parent.mySlider = null;
        System.gc();
        Thread.currentThread().stop();
    }
    
    
}
