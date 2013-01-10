import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.Vector;
import java.lang.Math;
import java.net.URL;
import java.applet.*;


public class SatSprite extends Sprite{
    
    //satellite properities
    public int speed, altitude;
    Range satRange; //draws range circle-like shape  
   
    public boolean showRange, hasContact;
    public double contactLong, contactLat;
    public int imgAngle; //used for tracking rotation animation
    
    SatSprite() {
        this(0, 0, 0, 0, 0, 0, "null"); }
    SatSprite(double newLat, double newLong, String newName) {
        this(newLat, newLong, 0, 0, 0, 0, newName); }
    SatSprite(double newLat, double newLong, int newRange) {
        this(newLat, newLong, newRange, 0, 0, 0, ""); }  
    SatSprite(double newLat, double newLong, int newRange, int newAltitude, int newSpeedKmH, int newAngleNorth, String newName) {
        name = newName;
        coordLong = newLong;
        coordLat = newLat;        
        range = newRange;
        altitude = newAltitude;
        angle = newAngleNorth;
        speed = newSpeedKmH;        
        showRange = true;
        showName = true;
    }
    
    
    public void setVelocity(int newSpeedKmH, int newAngleNorth) {
        angle = newAngleNorth;
        speed = newSpeedKmH;  }
        
       
    public void setAltitude(int newAltitude) { altitude = newAltitude; }        
    public void setRange( int newRange) { range = newRange; }
    
    public Range getRangeObj()  {
        showRange = true;
        satRange = new Range(this, range);
        return satRange;
    }
    public int getAltitude() { return altitude; }
        
    public void hideRange() { showRange = false; }
    
    //calculates move for satellite
    public void updatePosition() { updatePosition(60); }
    public void updatePosition(int seconds) {
        //converts speed KM/h to traveled km in seconds
        double distanceTraveled = (speed / 3600) * seconds;
        double alpha = distanceTraveled / radEarth;
        
        double moveLat = Math.asin( (Math.cos(getRad(angle)) *Math.cos( getRad(coordLat) ) 
                               *Math.sin(alpha) ) 
                               + Math.sin ( getRad(coordLat) ) * Math.cos(alpha) )
                               / degToRadConv;


        double dlon = Math.atan2(Math.sin(angle)*Math.sin(alpha) * Math.cos(getRad(coordLat)) , 
                              ( Math.cos(alpha) -
                                Math.sin(getRad(coordLat))*Math.sin(getRad(moveLat))) );
        double moveLong = (  (getRad(coordLong) - dlon + PI) % (2*PI) - PI )
                              / degToRadConv;
        //updates position
        coordLat = moveLat; coordLong = moveLong;
    }
    
    //just for test
    public void moveScalar(int seconds) {
        //converts speed KM/h to traveled km in seconds
        double distanceTraveled = (speed / 3600) * seconds;
        
        coordLong = getLongMove( ( Math.sin(getRad(angle)) * ( distanceTraveled) / (radEarth *PI) * 180 ), coordLong, 180 );
        coordLat = getLatMove( ( Math.cos(getRad(angle)) * ( distanceTraveled) / (radEarth *PI) * 90 ), coordLat, 90 );
    }
}

