/*
 * Range.java
 *
 * Created on February 13, 2002, 2:21 PM
 */

/**
 *
 * @author  TWu
 * @version 
 */

//import java.io.*;
//import java.net.*;
import java.awt.*;
//import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.lang.Math;
import java.net.URL;
import java.applet.*;

public class Range {
    final static double PI = Math.acos(-1);
    final static double radEarth = 6378.1;
    final static double degToRadConv = (PI / 180.0);
    
    double alpha,dlon;    
    double satLat, satLong, range, rad, drawPrecision;
    
    int numCoords, count;
    double[] rangeLong;
    double[] rangeLat;
    
    boolean satEdge;   
    
    public Range() {    }    
    public Range(SatSprite sat, double newRange) { 
        update(sat, newRange); //updates
    }
    
    public void update(SatSprite sat, double newRange) {
        satLat = sat.getLat();
        satLong = sat.getLong();
        range = newRange;
        
        //Constants used for calculating LOS
        alpha = range / radEarth; //angular distance travelled
        drawPrecision = 0.5 / Math.sqrt( (alpha * 100 ) );
        numCoords = (int)(2 * PI / drawPrecision);
        
        rangeLong = new double[numCoords];
        rangeLat = new double[numCoords];
        calcLatLongCoords();
    }
    
    public double[] getLongCoords() { return rangeLong; }
    public double[] getLatCoords() { return rangeLat; }    
    
    public void calcLatLongCoords() {
        rad = 0;
        //equation constants for latitude calculations
        double beta = Math.cos(getRad(satLat)) * Math.sin(alpha);
        double theta = Math.sin ( getRad(satLat) ) * Math.cos(alpha);
        //equation constants for longitude calcutions
        double beta2 = Math.sin(alpha) * Math.cos(getRad(satLat));
        double gamma2 = Math.cos(alpha);
        double theta2 = Math.sin(getRad(satLat));
        double epsilon2 = getRad(satLong) + PI;
        double PI2 = 2*PI;
        double rangeLongTemp;
        
        //calculates circle for 360 degrees
        for (count = 0; count < numCoords; count++) {

            //calculates latitude
            rangeLat[count] = Math.asin( (Math.cos(rad) * beta ) + theta ) / degToRadConv;
            
            //calculates longitude
            dlon = Math.atan2(Math.sin(rad) * beta2 , ( gamma2 - theta2 * Math.sin(getRad(rangeLat[count]))) );
            rangeLong[count] = ( (epsilon2 - dlon) % (PI2) - PI ) / degToRadConv;
            
            //draws left circle on other side of map if range passes left edge
            if (rangeLong[count] < -180)
                rangeLong[count] += 360;
            
            //System.out.println("\nLat: " + rangeLat[count] + " Long: " + rangeLong[count] + " Angle (degrees): " + rad / degToRadConv);
            rad+=drawPrecision;
         }
    }
    
    //converts degrees to radians
    private double getRad(double deg) { return ( degToRadConv * deg ); }
}
