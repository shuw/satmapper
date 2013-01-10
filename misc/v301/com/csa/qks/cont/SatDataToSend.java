package com.csa.qks.cont;

import java.io.Serializable;

public class SatDataToSend
    implements Serializable
{

    public String name;
    public double lat;
    public double longi;
    public int alt;
    public int speed;
    public int range;
    public double[][] pathCoords;
    public boolean pathCreated = false;
        
    public SatDataToSend()
    {
        pathCreated = false;
        lat = 0L;
        longi = 0L;
    }
    public int getAlt()
    {
        return alt;
    }
    public double getLat()
    {
        return lat;
    }
    public double getLongi()
    {
        return longi;
    }
    public double[][] getPathCoords() {
        return pathCoords;
    }
    
    public String getName()
    {
        return name;
    }
    public void setPathCoords(double[][] pathCoords) {
        pathCreated = true;
        this.pathCoords = pathCoords;
    }
    public void removePathCoords() {
        this.pathCoords = null;
    }
    
/**
 * Insert the method's description here.
 * Creation date: (3/14/2002 2:53:43 PM)
 * @return int
 */
public int getRange() {
	return range;
}
    public int getSpeed()
    {
        return speed;
    }
    public void setAlt(int newAlt)
    {
        alt = newAlt;
    }
    public void setLat(double newLat)
    {
        lat = newLat;
    }
    public void setLongi(double newLongi)
    {
        longi = newLongi;
    }
    public void setName(String newName)
    {
        name = newName;
    }
/**
 * Insert the method's description here.
 * Creation date: (3/14/2002 2:53:43 PM)
 * @param newRange int
 */
public void setRange(int newRange) {
	range = newRange;
}
    public void setSpeed(int newSpeed)
    {
        speed = newSpeed;
    }
}
