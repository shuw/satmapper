// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:18:45
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Satellite.java

package com.csa.qks.serv;

import java.io.*;
import java.util.Date;
import java.util.Vector;
import javax.swing.text.JTextComponent;

// Referenced classes of package com.csa.qks.serv:
//            GroundStation

public class Satellite
{

    public Satellite()
    {
        name = "";
        fullName = "";
        features = "";
        notes = "";
        verbose = false;
        callsign = "";
        controlCallsign = "";
        controlSSID = 0;
        controlKey = "";
        pacsatBroadcastCallsign = "";
        pacsatCallsign = "";
        pacsatSSID = 0;
        uplinks = null;
        currentUplink = 0;
        downlinks = null;
        currentDownlink = 0;
        bootloader_scid = 0;
        bootloader_aval = 0;
        bootloader_bval = 0;
        bootloader_d3val = 0;
        bootloader_k1h = 0;
        bootloader_k1l = 0;
        bootloader_loadcmd = 0;
        bootloader_dumpcmd = 0;
        bootloader_execcmd = 0;
        bootloader_memecmd = 0;
        bootloader_memwcmd = 0;
        bootloader_ioecmd = 0;
        bootloader_iopcmd = 0;
        bootloader_tlmcmd = 0;
        bootloader_movcmd = 0;
        bootloader_nocmd1 = 0;
        bootloader_nocmd2 = 0;
        kepsLoaded = false;
        catalogNumber = "";
        epochTime = "";
        epochYear = 0;
        epochDayOfYear = 0.0D;
        elementSet = "";
        inclination = 0.0D;
        RAAN = 0.0D;
        eccentricity = 0.0D;
        argOfPerigee = 0.0D;
        meanAnomaly = 0.0D;
        meanMotion = 0.0D;
        decayRate = 0.0D;
        epochRevolution = 0;
        checksum = 0;
        ans = 0;
        oldAz = 0;
        oldEl = 0;
        rv = 0;
        iel = 0;
        iaz = 0;
        iak = 0;
        ivk = 0;
        ifk = 0;
        ma256 = 0;
        irk = 0;
        isplat = 0;
        isplong = 0;
        isApproaching = true;
        isSignalAcquired = false;
        oldClock = 0.0D;
        oldRange = 0.0D;
        dp = 0.0D;
        dt = 0.0D;
        doppler146 = 0.0D;
        doppler435 = 0.0D;
        fraction = 0.0D;
        oldTime = 0.0D;
        nextaos = 0.0D;
        lostime = 0.0D;
        aoslos = 0.0D;
        ssplat = 0.0D;
        ssplong = 0.0D;
        epoch = 0.0D;
        age = 0.0D;
        year = 0.0D;
        daynum = 0.0D;
        t1 = 0.0D;
        df = 0.0D;
        se = 0.0D;
        n0 = 0.0D;
        sma = 0.0D;
        e2 = 0.0D;
        e1 = 0.0D;
        s1 = 0.0D;
        c1 = 0.0D;
        l8 = 0.0D;
        s9 = 0.0D;
        c9 = 0.0D;
        s8 = 0.0D;
        c8 = 0.0D;
        r9 = 0.0D;
        z9 = 0.0D;
        x9 = 0.0D;
        y9 = 0.0D;
        apogee = 0.0D;
        azimuth = 0.0D;
        perigee = 0.0D;
        elevation = 0.0D;
        vk = 0.0D;
        vm = 0.0D;
        rm = 0.0D;
        rk = 0.0D;
        ak = 0.0D;
        am = 0.0D;
        fk = 0.0D;
        fm = 0.0D;
        o = 0.0D;
        w = 0.0D;
        q = 0.0D;
        k2 = 0.0D;
        s0 = 0.0D;
        c0 = 0.0D;
        s2 = 0.0D;
        c2 = 0.0D;
        q0 = 0.0D;
        m = 0.0D;
        e = 0.0D;
        s3 = 0.0D;
        c3 = 0.0D;
        r3 = 0.0D;
        m1 = 0.0D;
        m5 = 0.0D;
        x0 = 0.0D;
        yzero = 0.0D;
        r = 0.0D;
        x1 = 0.0D;
        yone = 0.0D;
        z1 = 0.0D;
        g7 = 0.0D;
        s7 = 0.0D;
        c7 = 0.0D;
        x = 0.0D;
        y = 0.0D;
        z = 0.0D;
        x5 = 0.0D;
        y5 = 0.0D;
        z5 = 0.0D;
        x8 = 0.0D;
        y8 = 0.0D;
        z8 = 0.0D;
        range = 0.0D;
        aoshappens = false;
        geostationary = false;
        decayed = false;
        oncethru = false;
        currentSatAzimuth = 0;
        currentSatElevation = 0;
        currentSatLatitude = 0;
        currentSatLongitude = 0;
        currentSatRangeKm = 0;
        currentSatAltitudeKm = 0;
        currentSatVelocityKmH = 0;
        currentSatFootprintKm = 0;
        nextAOSMinutes = 0.0D;
        nextLOSMinutes = 0.0D;
        initSatellite();
    }

    public Satellite(String satName)
    {
        name = "";
        fullName = "";
        features = "";
        notes = "";
        verbose = false;
        callsign = "";
        controlCallsign = "";
        controlSSID = 0;
        controlKey = "";
        pacsatBroadcastCallsign = "";
        pacsatCallsign = "";
        pacsatSSID = 0;
        uplinks = null;
        currentUplink = 0;
        downlinks = null;
        currentDownlink = 0;
        bootloader_scid = 0;
        bootloader_aval = 0;
        bootloader_bval = 0;
        bootloader_d3val = 0;
        bootloader_k1h = 0;
        bootloader_k1l = 0;
        bootloader_loadcmd = 0;
        bootloader_dumpcmd = 0;
        bootloader_execcmd = 0;
        bootloader_memecmd = 0;
        bootloader_memwcmd = 0;
        bootloader_ioecmd = 0;
        bootloader_iopcmd = 0;
        bootloader_tlmcmd = 0;
        bootloader_movcmd = 0;
        bootloader_nocmd1 = 0;
        bootloader_nocmd2 = 0;
        kepsLoaded = false;
        catalogNumber = "";
        epochTime = "";
        epochYear = 0;
        epochDayOfYear = 0.0D;
        elementSet = "";
        inclination = 0.0D;
        RAAN = 0.0D;
        eccentricity = 0.0D;
        argOfPerigee = 0.0D;
        meanAnomaly = 0.0D;
        meanMotion = 0.0D;
        decayRate = 0.0D;
        epochRevolution = 0;
        checksum = 0;
        ans = 0;
        oldAz = 0;
        oldEl = 0;
        rv = 0;
        iel = 0;
        iaz = 0;
        iak = 0;
        ivk = 0;
        ifk = 0;
        ma256 = 0;
        irk = 0;
        isplat = 0;
        isplong = 0;
        isApproaching = true;
        isSignalAcquired = false;
        oldClock = 0.0D;
        oldRange = 0.0D;
        dp = 0.0D;
        dt = 0.0D;
        doppler146 = 0.0D;
        doppler435 = 0.0D;
        fraction = 0.0D;
        oldTime = 0.0D;
        nextaos = 0.0D;
        lostime = 0.0D;
        aoslos = 0.0D;
        ssplat = 0.0D;
        ssplong = 0.0D;
        epoch = 0.0D;
        age = 0.0D;
        year = 0.0D;
        daynum = 0.0D;
        t1 = 0.0D;
        df = 0.0D;
        se = 0.0D;
        n0 = 0.0D;
        sma = 0.0D;
        e2 = 0.0D;
        e1 = 0.0D;
        s1 = 0.0D;
        c1 = 0.0D;
        l8 = 0.0D;
        s9 = 0.0D;
        c9 = 0.0D;
        s8 = 0.0D;
        c8 = 0.0D;
        r9 = 0.0D;
        z9 = 0.0D;
        x9 = 0.0D;
        y9 = 0.0D;
        apogee = 0.0D;
        azimuth = 0.0D;
        perigee = 0.0D;
        elevation = 0.0D;
        vk = 0.0D;
        vm = 0.0D;
        rm = 0.0D;
        rk = 0.0D;
        ak = 0.0D;
        am = 0.0D;
        fk = 0.0D;
        fm = 0.0D;
        o = 0.0D;
        w = 0.0D;
        q = 0.0D;
        k2 = 0.0D;
        s0 = 0.0D;
        c0 = 0.0D;
        s2 = 0.0D;
        c2 = 0.0D;
        q0 = 0.0D;
        m = 0.0D;
        e = 0.0D;
        s3 = 0.0D;
        c3 = 0.0D;
        r3 = 0.0D;
        m1 = 0.0D;
        m5 = 0.0D;
        x0 = 0.0D;
        yzero = 0.0D;
        r = 0.0D;
        x1 = 0.0D;
        yone = 0.0D;
        z1 = 0.0D;
        g7 = 0.0D;
        s7 = 0.0D;
        c7 = 0.0D;
        x = 0.0D;
        y = 0.0D;
        z = 0.0D;
        x5 = 0.0D;
        y5 = 0.0D;
        z5 = 0.0D;
        x8 = 0.0D;
        y8 = 0.0D;
        z8 = 0.0D;
        range = 0.0D;
        aoshappens = false;
        geostationary = false;
        decayed = false;
        oncethru = false;
        currentSatAzimuth = 0;
        currentSatElevation = 0;
        currentSatLatitude = 0;
        currentSatLongitude = 0;
        currentSatRangeKm = 0;
        currentSatAltitudeKm = 0;
        currentSatVelocityKmH = 0;
        currentSatFootprintKm = 0;
        nextAOSMinutes = 0.0D;
        nextLOSMinutes = 0.0D;
        initSatellite();
        name = satName;
    }

    private void Calc()
    {
        age = daynum - epoch;
        o = 0.0174532925199D * (RAAN - age * k2 * c1);
        s0 = Math.sin(o);
        c0 = Math.cos(o);
        w = 0.0174532925199D * (argOfPerigee + age * k2 * (2.5D * c1 * c1 - 0.5D));
        s2 = Math.sin(w);
        c2 = Math.cos(w);
        c[1][1] = c2 * c0 - s2 * s0 * c1;
        c[1][2] = -s2 * c0 - c2 * s0 * c1;
        c[2][1] = c2 * s0 + s2 * c0 * c1;
        c[2][2] = -s2 * s0 + c2 * c0 * c1;
        c[3][1] = s2 * s1;
        c[3][2] = c2 * s1;
        q0 = meanAnomaly / 360D + (double)epochRevolution;
        q = n0 * age + q0;
        rv = (int)Math.floor(q);
        q -= Math.floor(q);
        m = q * 6.2831853071795862D;
        e = m + eccentricity * (Math.sin(m) + 0.5D * eccentricity * Math.sin(m * 2D));
        do
        {
            s3 = Math.sin(e);
            c3 = Math.cos(e);
            r3 = 1.0D - eccentricity * c3;
            m1 = e - eccentricity * s3;
            m5 = m1 - m;
            e -= m5 / r3;
        } while(Math.abs(m5) >= 9.9999999999999995E-007D);
        x0 = sma * (c3 - eccentricity);
        yzero = sma * e1 * s3;
        r = sma * r3;
        x1 = x0 * c[1][1] + yzero * c[1][2];
        yone = x0 * c[2][1] + yzero * c[2][2];
        z1 = x0 * c[3][1] + yzero * c[3][2];
        g7 = (daynum - df) * 1.0027379093D + se;
        g7 = 6.2831853071795862D * (g7 - Math.floor(g7));
        s7 = -Math.sin(g7);
        c7 = Math.cos(g7);
        x = x1 * c7 - yone * s7;
        y = x1 * s7 + yone * c7;
        z = z1;
        x5 = x - x9;
        y5 = y - y9;
        z5 = z - z9;
        range = x5 * x5 + y5 * y5 + z5 * z5;
        z8 = x5 * c8 * c9 + y5 * s8 * c9 + z5 * s9;
        x8 = (-x5 * c8 * s9 - y5 * s8 * s9) + z5 * c9;
        y8 = y5 * c8 - x5 * s8;
        ak = r - 6378.1350000000002D;
        elevation = Math.atan(z8 / Math.sqrt(range - z8 * z8)) / 0.0174532925199D;
        azimuth = Math.atan(y8 / x8) / 0.0174532925199D;
        if(x8 < 0.0D)
            azimuth += 180D;
        if(azimuth < 0.0D)
            azimuth += 360D;
        ma256 = (int)(256D * q);
        am = ak / 1.6093440000000001D;
        rk = Math.sqrt(range);
        rm = rk / 1.6093440000000001D;
        vk = 3.6000000000000001D * Math.sqrt(398652000000000D * (2D / (r * 1000D) - 1.0D / (sma * 1000D)));
        vm = vk / 1.6093440000000001D;
        fk = 12756.33D * Math.acos(6378.1350000000002D / r);
        fm = fk / 1.6093440000000001D;
        ssplat = Math.atan(z / Math.sqrt(r * r - z * z)) / 0.0174532925199D;
        ssplong = -Math.atan(y / x) / 0.0174532925199D;
        if(x < 0.0D)
            ssplong += 180D;
        if(ssplong < 0.0D)
            ssplong += 360D;
        irk = (int)Math.rint(rk);
        iak = (int)Math.rint(ak);
        ivk = (int)Math.rint(vk);
        ifk = (int)Math.rint(fk);
        isplat = (int)Math.rint(ssplat);
        isplong = (int)Math.rint(ssplong);
        iaz = (int)Math.rint(azimuth);
        iel = (int)Math.rint(elevation);
    }

    public void calculatePosition(GroundStation groundRef, long refTime)
    {
        daynum = (double)refTime / 86400000D - 3651D;
        epoch = (double)dayNumber(1, 0, epochYear) + epochDayOfYear;
        age = daynum - epoch;
        year = epochYear;
        if(year <= 50D)
            year += 100D;
        t1 = year - 1.0D;
        df = ((366D + Math.floor(365.25D * (t1 - 80D))) - Math.floor(t1 / 100D)) + Math.floor(t1 / 400D + 0.75D);
        t1 = (df + 29218.5D) / 36525D;
        t1 = 6.6460656D + t1 * (2400.051262D + t1 * 2.5809999999999999E-005D);
        se = t1 / 24D - year;
        n0 = meanMotion + age * decayRate;
        sma = 331.25D * Math.pow(1440D / n0, 0.66666666666666663D);
        e2 = 1.0D - eccentricity * eccentricity;
        e1 = Math.sqrt(e2);
        k2 = (9.9499999999999993D * Math.exp(Math.log(6378.1350000000002D / sma) * 3.5D)) / (e2 * e2);
        s1 = Math.sin(inclination * 0.0174532925199D);
        c1 = Math.cos(inclination * 0.0174532925199D);
        l8 = groundRef.getLatitude() * 0.0174532925199D;
        s9 = Math.sin(l8);
        c9 = Math.cos(l8);
        s8 = Math.sin(-groundRef.getLongitude() * 0.0174532925199D);
        c8 = Math.cos(groundRef.getLongitude() * 0.0174532925199D);
        r9 = 6378.1350000000002D * (1.0D + 0.00167644593462D * (Math.cos(2D * l8) - 1.0D)) + (double)groundRef.getAltitude() / 1000D;
        l8 = Math.atan((0.99330545814540672D * s9) / c9);
        z9 = r9 * Math.sin(l8);
        x9 = r9 * Math.cos(l8) * c8;
        y9 = r9 * Math.cos(l8) * s8;
        apogee = sma * (1.0D + eccentricity) - 6378.1350000000002D;
        perigee = sma * (1.0D - eccentricity) - 6378.1350000000002D;
        aoshappens = isAOSPossible(groundRef);
        geostationary = isGeostationary();
        decayed = isDecayed();
        daynum = (double)refTime / 86400000D - 3651D;
        Calc();
        dt = daynum * 86400D - oldClock;
        dp = rk * 1000D - oldRange;
        if(dt > 0.0D)
        {
            fraction = -(dp / dt / 299792458D);
            doppler146 = fraction * 146000000D;
            doppler435 = fraction * 435000000D;
            oldClock = 86400D * daynum;
            oldRange = rk * 1000D;
        }
        if(elevation >= 0.0D)
        {
            isSignalAcquired = true;
            if(oncethru && dt > 0.0D)
                if(doppler435 > 0.0D)
                    isApproaching = true;
                else
                    isApproaching = false;
        } else
        {
            isSignalAcquired = false;
            lostime = 0.0D;
        }
        currentSatAzimuth = iaz;
        currentSatElevation = iel;
        currentSatLatitude = isplat;
        currentSatLongitude = isplong;
        currentSatRangeKm = irk;
        currentSatAltitudeKm = iak;
        currentSatVelocityKmH = ivk;
        currentSatFootprintKm = ifk;
        double currentDaynum = daynum;
        if(elevation >= 0.0D && !geostationary && !decayed && daynum > lostime)
        {
            lostime = FindLOS2(groundRef);
            aoslos = lostime;
        } else
        if(elevation < 0.0D && !geostationary && !decayed && aoshappens && daynum > nextaos)
        {
            daynum += 0.0030000000000000001D;
            nextaos = FindAOS(groundRef);
            aoslos = nextaos;
        }
        nextAOSMinutes = nextaos - currentDaynum;
        nextAOSMinutes = nextAOSMinutes * 1440D;
        nextLOSMinutes = lostime - currentDaynum;
        nextLOSMinutes = nextLOSMinutes * 1440D;
        oncethru = true;
    }

    public void copyFrom(Satellite sat)
    {
        if(sat == null)
        {
            return;
        } else
        {
            name = sat.name;
            fullName = sat.fullName;
            callsign = sat.callsign;
            controlCallsign = sat.controlCallsign;
            controlKey = sat.controlKey;
            bootloader_scid = sat.bootloader_scid;
            bootloader_aval = sat.bootloader_aval;
            bootloader_bval = sat.bootloader_bval;
            bootloader_d3val = sat.bootloader_d3val;
            bootloader_k1h = sat.bootloader_k1h;
            bootloader_k1l = sat.bootloader_k1l;
            bootloader_loadcmd = sat.bootloader_loadcmd;
            bootloader_dumpcmd = sat.bootloader_dumpcmd;
            bootloader_execcmd = sat.bootloader_execcmd;
            bootloader_memecmd = sat.bootloader_memecmd;
            bootloader_memwcmd = sat.bootloader_memwcmd;
            bootloader_ioecmd = sat.bootloader_ioecmd;
            bootloader_iopcmd = sat.bootloader_iopcmd;
            bootloader_tlmcmd = sat.bootloader_tlmcmd;
            bootloader_movcmd = sat.bootloader_movcmd;
            bootloader_nocmd1 = sat.bootloader_nocmd1;
            bootloader_nocmd2 = sat.bootloader_nocmd2;
            pacsatBroadcastCallsign = sat.pacsatBroadcastCallsign;
            pacsatCallsign = sat.pacsatCallsign;
            pacsatSSID = sat.pacsatSSID;
            features = sat.features;
            notes = sat.notes;
            catalogNumber = sat.catalogNumber;
            epochTime = sat.epochTime;
            epochYear = sat.epochYear;
            epochDayOfYear = sat.epochDayOfYear;
            elementSet = sat.elementSet;
            inclination = sat.inclination;
            RAAN = sat.RAAN;
            eccentricity = sat.eccentricity;
            argOfPerigee = sat.argOfPerigee;
            meanAnomaly = sat.meanAnomaly;
            meanMotion = sat.meanMotion;
            decayRate = sat.decayRate;
            epochRevolution = sat.epochRevolution;
            checksum = sat.checksum;
            uplinks = sat.uplinks;
            currentUplink = sat.currentUplink;
            downlinks = sat.downlinks;
            currentDownlink = sat.currentDownlink;
            verbose = sat.verbose;
            kepsLoaded = sat.kepsLoaded;
            return;
        }
    }

    double currentDayNumber()
    {
        Date date = new Date();
        return (double)date.getTime() / 86400000D - 3651D;
    }

    private long dayNumber(int month, int day, int year)
    {
        long dn = 0L;
        if(month < 3)
        {
            year--;
            month += 12;
        }
        if(year < 50)
            year += 100;
        double yy = year;
        double mm = month;
        dn = (long)(((Math.floor(365.25D * (yy - 80D)) - Math.floor(19D + yy / 100D)) + Math.floor(4.75D + yy / 400D)) - 16D);
        dn += (long)(day + 30 * month) + (long)Math.floor(0.59999999999999998D * mm - 0.29999999999999999D);
        return dn;
    }

    double FindAOS(GroundStation groundRef)
    {
        double aostime = 0.0D;
        if(isAOSPossible(groundRef) && !isGeostationary() && !isDecayed())
        {
            Calc();
            while(elevation < -1D) 
            {
                daynum -= 0.00035D * (elevation * (ak / 8400D + 0.46000000000000002D) - 2D);
                Calc();
            }
            while(aostime == 0.0D) 
                if(Math.abs(elevation) < 0.029999999999999999D)
                {
                    aostime = daynum;
                } else
                {
                    daynum -= (elevation * Math.sqrt(ak)) / 530000D;
                    Calc();
                }
        }
        return aostime;
    }

    private double FindLOS(GroundStation groundRef)
    {
        lostime = 0.0D;
        if(!isGeostationary() && isAOSPossible(groundRef) && !isDecayed())
        {
            Calc();
            do
            {
                daynum += (elevation * Math.sqrt(ak)) / 502500D;
                Calc();
                if(Math.abs(elevation) < 0.029999999999999999D)
                    lostime = daynum;
            } while(lostime == 0.0D);
        }
        return lostime;
    }

    private double FindLOS2(GroundStation groundRef)
    {
        do
        {
            daynum += (Math.cos((elevation - 1.0D) * 0.0174532925199D) * Math.sqrt(ak)) / 25000D;
            Calc();
        } while(elevation >= 0.0D);
        return FindLOS(groundRef);
    }

    public int getAltitudeKm()
    {
        return currentSatAltitudeKm;
    }

    public long getAOS()
    {
        return (long)(nextaos * 24D * 60D * 60D * 1000D);
    }

    public int getApogeeKm()
    {
        return (int)Math.rint(apogee);
    }

    public int getAzimuth()
    {
        return currentSatAzimuth;
    }

    public int getBootloaderAVAL()
    {
        return bootloader_aval;
    }

    public int getBootloaderBVAL()
    {
        return bootloader_bval;
    }

    public int getBootloaderD3VAL()
    {
        return bootloader_d3val;
    }

    public int getBootloaderDumpCmd()
    {
        return bootloader_dumpcmd;
    }

    public int getBootloaderExecCmd()
    {
        return bootloader_execcmd;
    }

    public int getBootloaderIOeCmd()
    {
        return bootloader_ioecmd;
    }

    public int getBootloaderIOpCmd()
    {
        return bootloader_iopcmd;
    }

    public int getBootloaderK1H()
    {
        return bootloader_k1h;
    }

    public int getBootloaderK1L()
    {
        return bootloader_k1l;
    }

    public int getBootloaderLoadCmd()
    {
        return bootloader_loadcmd;
    }

    public int getBootloaderMemeCmd()
    {
        return bootloader_memecmd;
    }

    public int getBootloaderMemwCmd()
    {
        return bootloader_memwcmd;
    }

    public int getBootloaderMovCmd()
    {
        return bootloader_movcmd;
    }

    public int getBootloaderNoCmd1()
    {
        return bootloader_nocmd1;
    }

    public int getBootloaderNoCmd2()
    {
        return bootloader_nocmd2;
    }

    public int getBootloaderSCID()
    {
        return bootloader_scid;
    }

    public int getBootloaderTlmCmd()
    {
        return bootloader_tlmcmd;
    }

    public String getControlCallsign()
    {
        return controlCallsign.trim();
    }

    public String getControlKey()
    {
        return controlKey.trim();
    }

    public int getControlSSID()
    {
        return controlSSID;
    }

    public int getDopplerCompensationAt146()
    {
        return (int)doppler146;
    }

    public int getDopplerCompensationAt435()
    {
        return (int)doppler435;
    }

    public int getDownlinksCount()
    {
        return downlinks.size();
    }

    public int getElevation()
    {
        return currentSatElevation;
    }

    public int getFootprintKm()
    {
        return currentSatFootprintKm;
    }

    public String getFullName()
    {
        if(fullName != null)
            return fullName;
        else
            return "N/A";
    }

    public int getKepsAgeDays(long refTime)
    {
        double daynumX = (double)refTime / 86400000D - 3651D;
        double epochX = (double)dayNumber(1, 0, epochYear) + epochDayOfYear;
        double ageX = daynumX - epochX;
        return (int)ageX;
    }

    public int getLatitude()
    {
        return currentSatLatitude;
    }

    public int getLongitude()
    {
        return currentSatLongitude;
    }

    public long getLOS()
    {
        return (long)(lostime * 24D * 60D * 60D * 1000D);
    }

    public String getName()
    {
        return name;
    }

    public double getNextAOSMinutes()
    {
        if(nextAOSMinutes < 0.0D)
            return 0.0D;
        else
            return nextAOSMinutes;
    }

    public double getNextLOSMinutes()
    {
        if(nextLOSMinutes < 0.0D)
            return 0.0D;
        else
            return nextLOSMinutes;
    }

    public void getNotes(String baseDir, JTextComponent pane)
    {
        notes = "";
        if(name == null)
            return;
        if(pane == null)
            return;
        pane.setText("N/A");
        if(baseDir == null)
            baseDir = "";
        try
        {
            FileReader fr = new FileReader(baseDir.trim() + name.trim() + ".txt");
            pane.read(fr, null);
            fr.close();
        }
        catch(IOException _ex) { }
        catch(Exception _ex) { }
        notes = pane.getText();
    }

    public String getPacsatAddress()
    {
        return pacsatCallsign + "-" + pacsatSSID;
    }

    public String getPacsatCallsign()
    {
        return pacsatCallsign.trim();
    }

    public int getPacsatSSID()
    {
        return pacsatSSID;
    }

    public int getPerigeeKm()
    {
        return (int)Math.rint(perigee);
    }

    public int getRangeKm()
    {
        return currentSatRangeKm;
    }

    public int getUplinksCount()
    {
        return uplinks.size();
    }

    public int getVelocityKmH()
    {
        return currentSatVelocityKmH;
    }

    private void initSatellite()
    {
        uplinks = new Vector();
        downlinks = new Vector();
        c = new double[4][3];
        kepsLoaded = false;
    }

    private boolean isAOSPossible(GroundStation groundRef)
    {
        double lin = 0.0D;
        double sma = 0.0D;
        double apogee = 0.0D;
        lin = inclination;
        if(lin >= 90D)
            lin = 180D - lin;
        sma = 331.25D * Math.exp(Math.log(1440D / meanMotion) * 0.66666666666666663D);
        apogee = sma * (1.0D + eccentricity) - 6378.1350000000002D;
        return Math.acos(6378.1350000000002D / (apogee + 6378.1350000000002D)) + lin * 0.0174532925199D > Math.abs(groundRef.getLatitude() * 0.0174532925199D);
    }

    boolean isDecayed()
    {
        double satepoch = (double)dayNumber(1, 0, epochYear) + epochDayOfYear;
        return satepoch + (16.666665999999999D - meanMotion) / (10D * Math.abs(decayRate)) < daynum;
    }

    boolean isGeostationary()
    {
        return Math.abs(meanMotion - 1.0026999999999999D) < 0.00020000000000000001D;
    }

    public boolean isVisible()
    {
        return isSignalAcquired;
    }

    public boolean kepsLoaded()
    {
        return kepsLoaded;
    }

    public void saveNotes(String baseDir, String notesText)
    {
        if(name == null)
            return;
        if(notesText == null)
            return;
        if(baseDir == null)
            baseDir = "";
        try
        {
            FileWriter fw = new FileWriter(baseDir.trim() + name.trim() + ".txt");
            fw.write(notesText);
            fw.close();
        }
        catch(IOException _ex) { }
        catch(Exception _ex) { }
    }

    public void setBootloaderAVAL(int aval)
    {
        bootloader_aval = aval;
    }

    public void setBootloaderBVAL(int bval)
    {
        bootloader_bval = bval;
    }

    public void setBootloaderD3VAL(int d3val)
    {
        bootloader_d3val = d3val;
    }

    public void setBootloaderDumpCmd(int param)
    {
        bootloader_dumpcmd = param;
    }

    public void setBootloaderEncryptionKeys(int aval, int bval, int d3val, int k1h, int k1l)
    {
        bootloader_aval = aval;
        bootloader_bval = bval;
        bootloader_d3val = d3val;
        bootloader_k1h = k1h;
        bootloader_k1l = k1l;
    }

    public void setBootloaderExecCmd(int param)
    {
        bootloader_execcmd = param;
    }

    public void setBootloaderIOeCmd(int param)
    {
        bootloader_ioecmd = param;
    }

    public void setBootloaderIOpCmd(int param)
    {
        bootloader_iopcmd = param;
    }

    public void setBootloaderK1H(int k1h)
    {
        bootloader_k1h = k1h;
    }

    public void setBootloaderK1L(int k1l)
    {
        bootloader_k1l = k1l;
    }

    public void setBootloaderLoadCmd(int param)
    {
        bootloader_loadcmd = param;
    }

    public void setBootloaderMemeCmd(int param)
    {
        bootloader_memecmd = param;
    }

    public void setBootloaderMemwCmd(int param)
    {
        bootloader_memwcmd = param;
    }

    public void setBootloaderMovCmd(int param)
    {
        bootloader_movcmd = param;
    }

    public void setBootloaderNoCmd1(int param)
    {
        bootloader_nocmd1 = param;
    }

    public void setBootloaderNoCmd2(int param)
    {
        bootloader_nocmd2 = param;
    }

    public void setBootloaderSCID(int scid)
    {
        bootloader_scid = scid;
    }

    public void setBootloaderTlmCmd(int param)
    {
        bootloader_tlmcmd = param;
    }

    public void setControlKey(String controlKey)
    {
        this.controlKey = controlKey;
    }

    public void setKeps(String firstLine, String secondLine)
    {
        if(firstLine == null)
            return;
        if(secondLine == null)
            return;
        firstLine = firstLine.trim();
        secondLine = secondLine.trim();
        if(firstLine.length() < 69)
            return;
        if(secondLine.length() < 69)
            return;
        catalogNumber = "";
        epochTime = "";
        epochYear = 0;
        epochDayOfYear = 0.0D;
        decayRate = 0.0D;
        elementSet = "";
        inclination = 0.0D;
        RAAN = 0.0D;
        eccentricity = 0.0D;
        argOfPerigee = 0.0D;
        meanAnomaly = 0.0D;
        meanMotion = 0.0D;
        epochRevolution = 0;
        try
        {
            catalogNumber = firstLine.substring(2, 7).trim();
            epochTime = firstLine.substring(18, 32).trim();
            if(epochTime.length() > 13)
            {
                epochYear = Integer.parseInt(epochTime.substring(0, 2).trim());
                epochDayOfYear = Double.parseDouble(epochTime.substring(2).trim());
            }
            decayRate = Double.parseDouble(firstLine.substring(33, 43).trim());
            elementSet = firstLine.substring(65, 68).trim();
        }
        catch(NumberFormatException _ex)
        {
            return;
        }
        try
        {
            inclination = Double.parseDouble(secondLine.substring(8, 16).trim());
            RAAN = Double.parseDouble(secondLine.substring(17, 25).trim());
            eccentricity = Double.parseDouble("0." + secondLine.substring(26, 33).trim());
            argOfPerigee = Double.parseDouble(secondLine.substring(34, 42).trim());
            meanAnomaly = Double.parseDouble(secondLine.substring(43, 51).trim());
            meanMotion = Double.parseDouble(secondLine.substring(52, 63).trim());
            epochRevolution = Integer.parseInt(secondLine.substring(63, 68).trim());
        }
        catch(NumberFormatException _ex)
        {
            return;
        }
        kepsLoaded = true;
    }

    public void setPacsatCallsign(String pacsatCallsign)
    {
        this.pacsatCallsign = pacsatCallsign;
    }

    public void setPacsatSSID(int pacsatSSID)
    {
        this.pacsatSSID = pacsatSSID;
    }

    public void setVerbose(boolean verboseSetting)
    {
        verbose = verboseSetting;
    }

    public String toString()
    {
        return name;
    }

    private void verbosePrint(String message)
    {
        if(verbose)
            System.out.println(message);
    }

    private String name;
    private String fullName;
    private String features;
    private String notes;
    private boolean verbose;
    private String callsign;
    private String controlCallsign;
    private int controlSSID;
    private String controlKey;
    private String pacsatBroadcastCallsign;
    private String pacsatCallsign;
    private int pacsatSSID;
    private Vector uplinks;
    private int currentUplink;
    private Vector downlinks;
    private int currentDownlink;
    private final int COMMAND_PID = 0;
    private final int LOADER_PID = 121;
    private final int EXTENDED_LOADER_PID = 122;
    private int bootloader_scid;
    private int bootloader_aval;
    private int bootloader_bval;
    private int bootloader_d3val;
    private int bootloader_k1h;
    private int bootloader_k1l;
    private int bootloader_loadcmd;
    private int bootloader_dumpcmd;
    private int bootloader_execcmd;
    private int bootloader_memecmd;
    private int bootloader_memwcmd;
    private int bootloader_ioecmd;
    private int bootloader_iopcmd;
    private int bootloader_tlmcmd;
    private int bootloader_movcmd;
    private int bootloader_nocmd1;
    private int bootloader_nocmd2;
    private boolean kepsLoaded;
    private String catalogNumber;
    private String epochTime;
    private int epochYear;
    private double epochDayOfYear;
    private String elementSet;
    private double inclination;
    private double RAAN;
    private double eccentricity;
    private double argOfPerigee;
    private double meanAnomaly;
    private double meanMotion;
    private double decayRate;
    private int epochRevolution;
    private int checksum;
    private int ans;
    private int oldAz;
    private int oldEl;
    private int rv;
    private int iel;
    private int iaz;
    private int iak;
    private int ivk;
    private int ifk;
    private int ma256;
    private int irk;
    private int isplat;
    private int isplong;
    private boolean isApproaching;
    private boolean isSignalAcquired;
    private double oldClock;
    private double oldRange;
    private double dp;
    private double dt;
    private double doppler146;
    private double doppler435;
    private double fraction;
    private double oldTime;
    private double nextaos;
    private double lostime;
    private double aoslos;
    private double ssplat;
    private double ssplong;
    private double epoch;
    private double age;
    private double year;
    private double daynum;
    private double t1;
    private double df;
    private double se;
    private double n0;
    private double sma;
    private double e2;
    private double e1;
    private double s1;
    private double c1;
    private double l8;
    private double s9;
    private double c9;
    private double s8;
    private double c8;
    private double r9;
    private double z9;
    private double x9;
    private double y9;
    private double apogee;
    private double azimuth;
    private double perigee;
    private double elevation;
    private double vk;
    private double vm;
    private double rm;
    private double rk;
    private double ak;
    private double am;
    private double fk;
    private double fm;
    private double o;
    private double w;
    private double q;
    private double k2;
    private double s0;
    private double c0;
    private double s2;
    private double c2;
    private double q0;
    private double m;
    private double e;
    private double s3;
    private double c3;
    private double r3;
    private double m1;
    private double m5;
    private double x0;
    private double yzero;
    private double r;
    private double x1;
    private double yone;
    private double z1;
    private double g7;
    private double s7;
    private double c7;
    private double x;
    private double y;
    private double z;
    private double x5;
    private double y5;
    private double z5;
    private double x8;
    private double y8;
    private double z8;
    private double range;
    private double c[][];
    private boolean aoshappens;
    private boolean geostationary;
    private boolean decayed;
    private boolean oncethru;
    private int currentSatAzimuth;
    private int currentSatElevation;
    private int currentSatLatitude;
    private int currentSatLongitude;
    private int currentSatRangeKm;
    private int currentSatAltitudeKm;
    private int currentSatVelocityKmH;
    private int currentSatFootprintKm;
    private double nextAOSMinutes;
    private double nextLOSMinutes;
    final double TP = 6.2831853071795862D;
    final double PT = 1.570796326794897D;
    final double S1 = 0.39781867500000001D;
    final double C1 = 0.91746406000000003D;
    final double deg2rad = 0.0174532925199D;
    final double R0 = 6378.1350000000002D;
    final double FF = 0.0033528918692400001D;
    final double KM = 1.6093440000000001D;
}