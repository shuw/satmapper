// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:17:39
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Sprite.java

package com.csa.qks.vwr;


public class Sprite
{

    Sprite()
    {
        this(0.0D, 0.0D, "");
    }

    Sprite(double newLat, double newLong, String newName)
    {
        range = 0;
        name = newName;
        coordLong = newLong;
        coordLat = newLat;
        showName = true;
    }

    public double getLat()
    {
        return coordLat;
    }

    protected double getLatMove(double move, double original, int degTot)
    {
        double satMove = move + original;
        angle %= 360;
        if(satMove <= (double)degTot && satMove >= (double)(-degTot))
            return satMove;
        if(satMove > (double)degTot)
        {
            coordLong = -coordLong;
            if(angle > 270 || angle < 90)
                angle += 180;
            return (double)degTot - move;
        } else
        {
            coordLong = -coordLong;
            angle += 180;
            return (double)(-degTot) - move;
        }
    }

    public double getLong()
    {
        return coordLong;
    }

    protected double getLongMove(double move, double original, int degTot)
    {
        double satMove = move + original;
        if(satMove <= (double)degTot && satMove >= (double)(-degTot))
            return satMove;
        if(satMove > (double)degTot)
            return -((double)degTot - (satMove - (double)degTot));
        if(satMove < (double)(-degTot))
            return (double)degTot - (satMove + (double)degTot);
        else
            return 50D;
    }

    public String getName()
    {
        return name;
    }

    protected double getRad(double deg)
    {
        return degToRadConv * deg;
    }

    public int getRange()
    {
        return range;
    }

    protected boolean meets(double sLat, double sLong, double sRange)
    {
        double totalRange = sRange + (double)range;
        double AOB = Math.acos(Math.cos(getRad(coordLat)) * Math.cos(getRad(sLat)) * Math.cos(getRad(sLong - coordLong)) + Math.sin(getRad(coordLat)) * Math.sin(getRad(sLat)));
        return AOB * 6378.1000000000004D <= totalRange;
    }

    public boolean meetsPoint(double pointLat, double pointLong)
    {
        return meets(pointLat, pointLong, 0.0D);
    }

    public boolean meetsWith(Sprite secondObj)
    {
        return meets(secondObj.getLat(), secondObj.getLong(), secondObj.getRange());
    }

    public void move(double moveLat, double moveLong)
    {
        coordLat = getLatMove(moveLat, coordLat, 90);
        coordLong = getLongMove(moveLong, coordLong, 180);
    }

    public void setLat(double latitude)
    {
        coordLat = latitude;
    }

    public void setLong(double longitude)
    {
        coordLong = longitude;

    }

    public static final double PI;
    public static final double radEarth = 6378.1000000000004D;
    public static final double degToRadConv;
    double coordLong;
    double coordLat;
    public String name;
    public boolean showName;
    int angle;
    int range;

    static 
    {
        PI = Math.acos(-1D);
        degToRadConv = PI / 180D;
    }
}