// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:17:38
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   SatSprite.java

package com.csa.qks.vwr;


// Referenced classes of package com.csa.qks.vwr:
//            Sprite, Range

public class SatSprite extends Sprite
{

    SatSprite()
    {
        this(0.0D, 0.0D, 0, 0, 0, 0, "null");
    }

    SatSprite(double newLat, double newLong, int newRange)
    {
        this(newLat, newLong, newRange, 0, 0, 0, "");
    }

    SatSprite(double newLat, double newLong, int newRange, int newAltitude, int newSpeedKmH, 
            int newAngleNorth, String newName)
    {
        super.name = newName;
        super.coordLong = newLong;
        super.coordLat = newLat;
        super.range = newRange;
        altitude = newAltitude;
        super.angle = newAngleNorth;
        speed = newSpeedKmH;
        showRange = true;
        super.showName = true;
    }

    SatSprite(double newLat, double newLong, String newName)
    {
        this(newLat, newLong, 0, 0, 0, 0, newName);
    }

    public int getAltitude()
    {
        return altitude;
    }

    public Range getRangeObj()
    {
        showRange = true;
        satRange = new Range(this, super.range);
        return satRange;
    }

    int getSpeed()
    {
        return speed;
    }

    public void hideRange()
    {
        showRange = false;
    }

    public void moveScalar(int seconds)
    {
        double distanceTraveled = (speed / 3600) * seconds;
        super.coordLong = getLongMove(((Math.sin(getRad(super.angle)) * distanceTraveled) / (6378.1000000000004D * Sprite.PI)) * 180D, super.coordLong, 180);
        super.coordLat = getLatMove(((Math.cos(getRad(super.angle)) * distanceTraveled) / (6378.1000000000004D * Sprite.PI)) * 90D, super.coordLat, 90);
    }

    public void setAltitude(int newAltitude)
    {
        altitude = newAltitude;
    }

    public void setRange(int newRange)
    {
        super.range = newRange;
    }

    public void setSpeed(int newSpeed)
    {
        speed = newSpeed;
    }

    public void setVelocity(int newSpeedKmH, int newAngleNorth)
    {
        super.angle = newAngleNorth;
        speed = newSpeedKmH;
    }

    public void updatePosition()
    {
        updatePosition(60);
    }

    public void updatePosition(int seconds)
    {
        double distanceTraveled = (speed / 3600) * seconds;
        double alpha = distanceTraveled / 6378.1000000000004D;
        double moveLat = Math.asin(Math.cos(getRad(super.angle)) * Math.cos(getRad(super.coordLat)) * Math.sin(alpha) + Math.sin(getRad(super.coordLat)) * Math.cos(alpha)) / Sprite.degToRadConv;
        double dlon = Math.atan2(Math.sin(super.angle) * Math.sin(alpha) * Math.cos(getRad(super.coordLat)), Math.cos(alpha) - Math.sin(getRad(super.coordLat)) * Math.sin(getRad(moveLat)));
        double moveLong = (((getRad(super.coordLong) - dlon) + Sprite.PI) % (2D * Sprite.PI) - Sprite.PI) / Sprite.degToRadConv;
        super.coordLat = moveLat;
        super.coordLong = moveLong;
    }

    public int speed;
    public int altitude;
    Range satRange;
    public boolean showRange;
    public boolean hasContact;
    public double contactLong;
    public double contactLat;
    public int imgAngle;
}