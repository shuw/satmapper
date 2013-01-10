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


public class Sprite {
    public final static double PI = Math.acos(-1);
    public final static double radEarth = 6378.1;
    public final static double degToRadConv = (PI / 180.0);
    //Sprite properities
    double coordLong, coordLat;
    public String name;
    public boolean showName;
    int angle;
    
    int range = 0; //only changed in instance of Satellite
    
    
    Sprite() { this(0,0,""); }
    Sprite(double newLat, double newLong, String newName) {
        name = newName;
        coordLong = newLong;
        coordLat = newLat;
        showName = true;
    }
        
    public double getLong() { return coordLong; }
    public double getLat() { return coordLat; }
    public String getName() { return name; }
    public int getRange() { return range; }
    
    public void setLong(double longitude) { coordLong = longitude; }
    public void setLat(double latitude) { coordLat = latitude; }
    
    public void move(double moveLat, double moveLong) {
        coordLat = getLatMove(moveLat, coordLat, 90);
        coordLong = getLongMove(moveLong, coordLong, 180);
    }
    
    protected double getLongMove(double move,  double original, int degTot) {
        double satMove = move + original;

        if ( (satMove <= degTot) && (satMove >= -degTot) )
            return satMove;
        else if (satMove > degTot )
            return -(degTot - (satMove - degTot) );
        else if (satMove < -degTot)
            return (degTot - (satMove + degTot) );
        else return 50;
    }
    
    protected double getLatMove(double move,  double original, int degTot) {
        double satMove = move + original;
        angle =  angle % 360;
        if ( (satMove <= degTot) && (satMove >= -degTot) )
            return satMove;
        else if (satMove > degTot){
            coordLong = -coordLong;
            if ( angle > 270 || angle < 90)
                angle+=180;
            return degTot - move;
        }
        else {
            coordLong = -coordLong;
                angle+=180;
            return -degTot - move;
        }
    } 
    
    public boolean meetsWith(Sprite secondObj) {
        return meets( secondObj.getLat(), secondObj.getLong(), secondObj.getRange() ); }
    public boolean meetsPoint(double pointLat, double pointLong) { return meets( pointLat, pointLong, 0 ); }
    
    protected boolean meets(double sLat, double sLong, double sRange) {
        double totalRange = sRange + range;
                
        //original formula: cos(AOB) = cos(latA)cos(latB)cos(lonB-lonA)+sin(latA)sin(latB)
        //finds great circle distance
        double AOB = Math.acos( Math.cos( getRad(coordLat) ) 
                            * Math.cos( getRad(sLat) )
                            * Math.cos( getRad( sLong - coordLong ) )
                            + Math.sin( getRad(coordLat) ) 
                            * Math.sin( getRad( sLat ) ));
        
        //System.out.println("distance Away: " + AOB *radEarth + "totalrange: " + totalRange);
        if ( (AOB * radEarth) <= totalRange) return true;
        else return false;
    }
    
    //converts degrees to radians
    protected double getRad(double deg) { return ( degToRadConv * deg ); }
}

