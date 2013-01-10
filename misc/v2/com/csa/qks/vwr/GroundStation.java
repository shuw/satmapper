package com.csa.qks.vwr;

// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/8/2002 3:49:39 PM
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   GroundStation.java




public class GroundStation
{

    public GroundStation()
    {
        latitude = 0.0D;
        longitude = 0.0D;
        altitude = 0;
        callsign = "";
        name = "Local";
        networkAddress = "localhost";
    }

    public GroundStation(String name, String address, double latitude, double longitude, int altitude)
    {
        this.latitude = 0.0D;
        this.longitude = 0.0D;
        this.altitude = 0;
        callsign = "";
        this.name = "Local";
        networkAddress = "localhost";
        this.name = name;
        networkAddress = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public int getAltitude()
    {
        return altitude;
    }

    public String getCallsign()
    {
        return callsign;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public String getName()
    {
        return name;
    }

    public String getNetworkAddress()
    {
        return networkAddress;
    }

    public void setAltitude(int altToUse)
    {
        altitude = altToUse;
    }

    public void setCallsign(String callsignToUse)
    {
        callsign = callsignToUse;
    }

    public void setLatitude(double latToUse)
    {
        latitude = latToUse;
    }

    public void setLongitude(double longToUse)
    {
        longitude = longToUse;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setNetworkAddress(String networkAddress)
    {
        this.networkAddress = networkAddress;
    }

    public String toString()
    {
        return name;
    }

    double latitude;
    double longitude;
    int altitude;
    String callsign;
    private String name;
    private String networkAddress;
}