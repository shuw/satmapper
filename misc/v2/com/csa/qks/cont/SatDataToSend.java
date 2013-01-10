// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:18:07
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   SatDataToSend.java

package com.csa.qks.cont;

import java.io.Serializable;

public class SatDataToSend
    implements Serializable
{

    public SatDataToSend()
    {
        lat = 0L;
        longi = 0L;
    }

    public int getAlt()
    {
        return alt;
    }

    public long getLat()
    {
        return lat;
    }

    public long getLongi()
    {
        return longi;
    }

    public String getName()
    {
        return name;
    }

    public int getSpeed()
    {
        return speed;
    }

    public void setAlt(int newAlt)
    {
        alt = newAlt;
    }

    public void setLat(long newLat)
    {
        lat = newLat;
    }

    public void setLongi(long newLongi)
    {
        longi = newLongi;
    }

    public void setName(String newName)
    {
        name = newName;
    }

    public void setSpeed(int newSpeed)
    {
        speed = newSpeed;
    }

    public String name;
    public long lat;
    public long longi;
    public int alt;
    public int speed;
}