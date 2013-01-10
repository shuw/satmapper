/*==============================================================================
 * satellite.java
 * Created on April 10, 2001, 4:22 PM
 =============================================================================*/

package ca.gc.space.quicksat.ground.satellite;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import java.lang.Math.*;
import ca.gc.space.quicksat.ground.radio.*;
import ca.gc.space.quicksat.ground.control.*;
import ca.gc.space.quicksat.ground.tracking.*;

/*============================================================================*/
/** This class represents a satellite, and contains all data related to
 *  it, from its name to its Keplerian elements.
 *  Methods allow to calculate this satellite's position around the earth and
 *  relative to a ground station. To use this class: First define your sat,
 *  with its name exactly the same as the name in the Orbital Element (2-line
 *  elements) data file, and a descriptive full name (anything you like...). 
 *  Then for orbit prediction you must set the orbital elements using setKeps()
 *  then periodically (and before getting any data) call calculatePosition().
 *  You can always verify the age of the oribital elements by calling 
 *  getKepsAgeDays(). After that, if you call calculatePosition() periodically
 *  with the right time, then you'll get the following info:
 *  verify if acquisition of signal is possible with isAOSPossible(), and
 *  if this sat is geostationary with isGeostationary(), the frequency shift 
 *  caused by the Doppler effect with getDopplerCompensationAt435/146(), 
 *  The remaining time before next acquisition and loss of signal with getAOS()
 *  and getLOS() or getNextAOSMinutes() and getNextLOSMinutes(), 
 *  the distance between the sat and our reference ground station using 
 *  getRangeKm(), its relative position using getElevation() and getAzimuth(),
 *  its footprint with getFootprintKm(), current velocity with getVelocityKmH(),
 *  altitude with getAltitudeKm, absolute position using getLongitude() and
 *  getLatitude(), and finally if the satellite is currently seen by our
 *  ground station using isVisible().
 *
 *
 * @author  jean-francois Cusson, for the Canadian Space Agency, QuickSat
 *          Space Technologies.
 * @version                                                                   */
/*============================================================================*/
public class Satellite extends java.lang.Object implements Serializable {
/*============================================================================*/

/*--------------*/
/* Gereral info */
/*--------------*/
private String   name                     = "";
private String   fullName                 = "";
private String   features                 = "";
private String   notes                    = "";

private boolean  verbose = false;

/*-------------------------------------------*/
/* Identification, for communication purpose */
/*-------------------------------------------*/
private String   callsign                 = "";
private String   controlCallsign          = "";
private int      controlSSID              = 0;
private String   controlKey               = ""; //aka magic cookie
private String   pacsatBroadcastCallsign  = "";
private String   pacsatCallsign           = "";
private int      pacsatSSID               = 0;

/*--------------------------------*/
/* Communication links definition */
/*--------------------------------*/
private Vector      uplinks                  = null;
private int         currentUplink            = 0;
private Link        activeUplink             = null;
private Vector      downlinks                = null;
private int         currentDownlink          = 0;
private Link        activeDownlink           = null;

/*----------------------------------*/
/* Object defining control protocol */
/*----------------------------------*/
private SatControl   satCtrl           = null;

/*--------------------------*/
/* Protocole identification */
/*--------------------------*/
private final int COMMAND_PID            = 0x00;//default Proto ID
private final int LOADER_PID             = 0x79;
private final int EXTENDED_LOADER_PID    = 0x7A;

/*----------------------------------------------------------------*/
/* Communication keys for the bootloader module of the spacecraft */
/*----------------------------------------------------------------*/
private int     bootloader_scid          = 0; //Spacecraft ID
private int     bootloader_aval          = 0;
private int     bootloader_bval          = 0;
private int     bootloader_d3val         = 0;
private int     bootloader_k1h           = 0;
private int     bootloader_k1l           = 0;
private int     bootloader_loadcmd       = 0; //Load data
private int     bootloader_dumpcmd       = 0; //Dump memory
private int     bootloader_execcmd       = 0; //Execute program
private int     bootloader_memecmd       = 0; //Examine Memory
private int     bootloader_memwcmd       = 0; //Write Memory
private int     bootloader_ioecmd        = 0; //Examine port
private int     bootloader_iopcmd        = 0; //Write port
private int     bootloader_tlmcmd        = 0; //send beacon
private int     bootloader_movcmd        = 0; //move kernal/pht
private int     bootloader_nocmd1        = 0; //user command 1
private int     bootloader_nocmd2        = 0; //user command 2

/*------------------*/
/* Orbital Elements */
/*------------------*/
private boolean  kepsLoaded               = false;
private String   catalogNumber            = "";
private String   epochTime                = "";
private int      epochYear                = 0;  //format = XX + 1900 or 2000...
private double   epochDayOfYear           = 0.0;
private String   elementSet               = "";
private double   inclination              = 0.0;
private double   RAAN                     = 0.0;
private double   eccentricity             = 0.0;
private double   argOfPerigee             = 0.0;
private double   meanAnomaly              = 0.0;
private double   meanMotion               = 0.0;
private double   decayRate                = 0.0;  //Drag
private int      epochRevolution          = 0;
private int      checksum                 = 0;

/*-------------------------------*/
/* Tracking Variables and Status */
/*-------------------------------*/
private int     ans   = 0;
private int     oldAz = 0;
private int     oldEl = 0;
private int     rv    = 0;              //orbit number
private int     iel   = 0;
private int     iaz   = 0;
private int     iak   = 0;
private int     ivk   = 0;
private int     ifk   = 0;
private int     ma256 = 0;
private int     irk   = 0;
private int     isplat = 0;
private int     isplong= 0;

private boolean isApproaching = true;
private boolean isSignalAcquired = false;
private double  oldClock     = 0.0;
private double  oldRange     = 0.0;
private double  dp           = 0.0;
private double  dt           = 0.0;
private double  doppler146   = 0.0;
private double  doppler435   = 0.0;
private double  fraction     = 0.0;
private double  oldTime      = 0.0;
private double  nextaos      = 0.0;
private double  lostime      = 0.0;
private double  aoslos       = 0.0; //next event
private double  ssplat       = 0.0; //Spacecraft current latitude
private double  ssplong      = 0.0; //Spacecraft current longitude

private double  epoch        = 0.0;
private double  age          = 0.0;
private double  year         = 0.0;
    
private double  daynum       = 0.0;

private double  t1           = 0.0;  //???
private double  df           = 0.0;  //???
private double  se           = 0.0;  //???
private double  n0           = 0.0;  //???
private double  sma          = 0.0;  //???
private double  e2           = 0.0;  //???
private double  e1           = 0.0;  //???
private double  s1           = 0.0;
private double  c1           = 0.0;
private double  l8           = 0.0;  //Ground reference latitude, in radian
private double  s9           = 0.0;
private double  c9           = 0.0;
private double  s8           = 0.0;  
private double  c8           = 0.0;
private double  r9           = 0.0;
private double  z9           = 0.0;
private double  x9           = 0.0;
private double  y9           = 0.0;
private double  apogee       = 0.0;
private double  azimuth      = 0.0;
private double  perigee      = 0.0;
private double  elevation    = 0.0;     // Elevation of the satellite
private double  vk           = 0.0;     //velocity in km
private double  vm           = 0.0;     //velocity in miles
private double  rm           = 0.0;     //range in miles
private double  rk           = 0.0;     //range in km
private double  ak           = 0.0;     //altitude in km
private double  am           = 0.0;     //altitude in miles
private double  fk           = 0.0;     //footprint in km
private double  fm           = 0.0;     //footprint in miles
private double  o            = 0.0;
private double  w            = 0.0;
private double  q            = 0.0;     //orbital phase
private double  k2           = 0.0;
private double  s0           = 0.0;
private double  c0           = 0.0;
private double  s2           = 0.0;
private double  c2           = 0.0;
private double  q0           = 0.0;
private double  m            = 0.0;
private double  e            = 0.0;
private double  s3           = 0.0;
private double  c3           = 0.0;
private double  r3           = 0.0;
private double  m1           = 0.0;
private double  m5           = 0.0;
private double  x0           = 0.0;
private double  yzero        = 0.0;
private double  r            = 0.0;
private double  x1           = 0.0;
private double  yone         = 0.0;
private double  z1           = 0.0;
private double  g7           = 0.0;
private double  s7           = 0.0;
private double  c7           = 0.0;
private double  x            = 0.0;
private double  y            = 0.0;
private double  z            = 0.0;
private double  x5           = 0.0;
private double  y5           = 0.0;
private double  z5           = 0.0;
private double  x8           = 0.0;
private double  y8           = 0.0;
private double  z8           = 0.0;
private double  range        = 0.0;
private double  c[][];

private boolean aoshappens  = false;
private boolean geostationary = false;
private boolean decayed     = false;
private boolean oncethru    = false;

/*-----------------------------------------------*/
/* Current status of the satellite, according to */
/* latest calculations                           */
/*-----------------------------------------------*/
private int currentSatAzimuth   = 0;
private int currentSatElevation = 0;
private int currentSatLatitude  = 0;
private int currentSatLongitude = 0;
private int currentSatRangeKm   = 0;
private int currentSatAltitudeKm= 0;
private int currentSatVelocityKmH=0;
private int currentSatFootprintKm=0;

private double nextAOSMinutes = 0.0;
private double nextLOSMinutes = 0.0;


/*-----------------------------------------------------*/
/* Usefull constants for orbit prediction calculations */
/*-----------------------------------------------------*/
final double TP     = 6.283185307179586;
final double PT     = 1.570796326794897;
final double S1     = 0.397818675;
final double C1     = 0.91746406;
final double deg2rad= 1.74532925199e-02;
final double R0     = 6378.135;
final double FF     = 3.35289186924e-03;
final double KM     = 1.609344;    



   
    /*-----------------------------------------------------------------------*/
    /** Creates a new satellite, with nothing set by default.                */
    /*-----------------------------------------------------------------------*/
    public Satellite() {
    /*-----------------------------------------------------------------------*/    
        initSatellite();
    }

    /*------------------------------------------------------------------------*/
    /** Creates a new satellite, setting its name at the same time.
     *  @param satName A string identifying this satellite. MUST be the EXACT
     *                 (including case) same name as used in the keplerian
     *                 element two-line database.                             */
    /*------------------------------------------------------------------------*/
    public Satellite(java.lang.String satName) {
    /*-----------------------------------------------------------------------*/    
        initSatellite();
        this.name = satName;
    }

    private void initSatellite() {
        uplinks = new Vector();
        downlinks = new Vector();
        c = new double[4][3];
        kepsLoaded = false;        
    }

    public void setFeatures( String str ) {
        this.features = str;
    }
    public void appendFeatures( String str ) {
        this.features += " " + str;
    }
    public String getFeatures() {
        return( features );
    }
    
    public String getCatalogNumber() {
        return( catalogNumber );
    }
    public String getKepsEpochTime() {
        return( epochTime );
    }
    public String getKepsElementSet() {
        return( elementSet );
    }
    public double getInclination() {
        return( inclination );
    }
    public double getRAAN() {
        return( RAAN );
    }
    public double getEccentricity() {
        return( eccentricity );
    }
    public double getArgOfPerigee() {
        return( argOfPerigee );
    }
    public double getMeanAnomaly() {
        return( meanAnomaly );
    }
    public double getMeanMotion() {
        return( meanMotion );
    }
    public double getDecayRate() {
        return( decayRate );
    }
    public int getEpochRevolution() {
        return( epochRevolution );
    }
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the SatControl object, which knows everything
     *  about how to control the satellite (commands...).
     *  @param satCtrl SatControl object implementing control scheme for this
     *                 satellite.                                             */
    /*------------------------------------------------------------------------*/
    public void setControl( SatControl satCtrl ) {
    /*------------------------------------------------------------------------*/
        this.satCtrl = satCtrl;
    }
    /*------------------------------------------------------------------------*/
    /** Allow us to get a link to the SatControl object associated with this
     *  satellite, and that implements the control scheme of this satellite.
     *  @return SatControl object associated with this satellite, null if none
     *                     is defined.                                        */
    /*------------------------------------------------------------------------*/
    public SatControl getControl() {
    /*------------------------------------------------------------------------*/
        return( satCtrl );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us if the satellite is visible, FROM THE LAST CALCULATION done
     *  with calculatePosition, and the groundstation given at this time.     */
    /*------------------------------------------------------------------------*/
    public boolean isVisible() {
    /*------------------------------------------------------------------------*/
        return( isSignalAcquired );
    }
    
    public boolean isApproaching() {
        return( isApproaching );
    }
    
    
    /*------------------------------------------------------------------------*/
    /** Make this satellite object a perfect duplicate of the given sat.
     *  Will not however take a snapshot of the tracking calculations...
     * @parm sat Satellite to duplicate.                                      */
    /*------------------------------------------------------------------------*/
    public void copyFrom( Satellite sat ) {
        if( sat == null ) return;
        this.name                     = sat.name;
        this.fullName                 = sat.fullName;
        this.callsign                 = sat.callsign;
        this.controlCallsign          = sat.controlCallsign;
        this.controlKey               = sat.controlKey;

        this.bootloader_scid          = sat.bootloader_scid;
        this.bootloader_aval          = sat.bootloader_aval;
        this.bootloader_bval          = sat.bootloader_bval;
        this.bootloader_d3val         = sat.bootloader_d3val;
        this.bootloader_k1h           = sat.bootloader_k1h;
        this.bootloader_k1l           = sat.bootloader_k1l;
        this.bootloader_loadcmd       = sat.bootloader_loadcmd; //Load data
        this.bootloader_dumpcmd       = sat.bootloader_dumpcmd; //Dump memory
        this.bootloader_execcmd       = sat.bootloader_execcmd; //Execute program
        this.bootloader_memecmd       = sat.bootloader_memecmd; //Examine Memory
        this.bootloader_memwcmd       = sat.bootloader_memwcmd; //Write Memory
        this.bootloader_ioecmd        = sat.bootloader_ioecmd; //Examine port
        this.bootloader_iopcmd        = sat.bootloader_iopcmd; //Write port
        this.bootloader_tlmcmd        = sat.bootloader_tlmcmd; //send beacon
        this.bootloader_movcmd        = sat.bootloader_movcmd; //move kernal/pht
        this.bootloader_nocmd1        = sat.bootloader_nocmd1; //user command 1
        this.bootloader_nocmd2        = sat.bootloader_nocmd2; //user command 2

        this.pacsatBroadcastCallsign  = sat.pacsatBroadcastCallsign;
        this.pacsatCallsign           = sat.pacsatCallsign;
        this.pacsatSSID               = sat.pacsatSSID;
        this.features                 = sat.features;
        this.notes                    = sat.notes;
        this.catalogNumber            = sat.catalogNumber;
        this.epochTime                = sat.epochTime;
        this.epochYear                = sat.epochYear;
        this.epochDayOfYear           = sat.epochDayOfYear;
        this.elementSet               = sat.elementSet;
        this.inclination              = sat.inclination;
        this.RAAN                     = sat.RAAN;
        this.eccentricity             = sat.eccentricity;
        this.argOfPerigee             = sat.argOfPerigee;
        this.meanAnomaly              = sat.meanAnomaly;
        this.meanMotion               = sat.meanMotion;
        this.decayRate                = sat.decayRate;
        this.epochRevolution          = sat.epochRevolution;
        this.checksum                 = sat.checksum;
        this.uplinks                  = sat.uplinks;
        this.currentUplink            = sat.currentUplink;
        this.downlinks                = sat.downlinks;
        this.currentDownlink          = sat.currentDownlink;

        this.verbose                  = sat.verbose;

        this.kepsLoaded               = sat.kepsLoaded;

    }

    /*------------------------------------------------------------------------*/
    /** Tells us the current latitude position of the satellate, acording to
     *  the last calculations performed.
     *  @return The latitude, 0 at equator, positive North, negative South, 
     *          up to 90 deg.                                                 */
    /*------------------------------------------------------------------------*/    
    public int getLatitude() {
    /*------------------------------------------------------------------------*/
        return( currentSatLatitude );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the current longitude position of the satellate, acording to
     *  the last calculations performed.
     *  @return The longitude, 0 at Greenwich, then positive towards west up
     *          to 359 back to greenwich.                                     */
    /*------------------------------------------------------------------------*/    
    public int getLongitude() {
    /*------------------------------------------------------------------------*/
        return( currentSatLongitude );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the current altitude of the satellite, in kilometers, at the 
     *  time specified during the last calculation pass.
     *  @return The altitude, in kilometers, of the satellite.                */
    /*------------------------------------------------------------------------*/    
    public int getAltitudeKm() {
    /*------------------------------------------------------------------------*/
        return( currentSatAltitudeKm );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the current velocity of the satellite, in km/h, at the 
     *  time specified during the last calculation pass.
     *  @return The velocity, in kilometers per hour, of the satellite.       */
    /*------------------------------------------------------------------------*/    
    public int getVelocityKmH() {
    /*------------------------------------------------------------------------*/
        return( currentSatVelocityKmH );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the footprint of the satellite, in kilometers, at the 
     *  time specified during the last calculation pass.
     *  @return The footprint (diameter), in kilometers, of the satellite.    */
    /*------------------------------------------------------------------------*/    
    public int getFootprintKm() {
    /*------------------------------------------------------------------------*/
        return( currentSatFootprintKm );
    }
    
    /*------------------------------------------------------------------------*/    
    /** Tells us the apogee of the satellite's orbit, in kilometers.
     *  @return The apogee, in kilometers, of the satellite's orbit.          */
    /*------------------------------------------------------------------------*/        
    public int getApogeeKm() {
    /*------------------------------------------------------------------------*/            
        return( (int)java.lang.Math.rint( apogee ) );
    }
    
    /*------------------------------------------------------------------------*/    
    /** Tells us the perigee of the satellite's orbit, in kilometers.
     *  @return The perigee, in kilometers, of the satellite's orbit.         */
    /*------------------------------------------------------------------------*/        
    public int getPerigeeKm() {
    /*------------------------------------------------------------------------*/            
        return( (int)java.lang.Math.rint( perigee ) );
    }

    /*------------------------------------------------------------------------*/
    /** Tell us the azimuth position of this satellite, in degrees, according
     *  to the last calculation that was performed with the ground station set
     *  at the time of the calculations.
     *  @return Azimuth position of satellite, in degree, from our station.   
     *          From 0 (North) to 359 degrees                                 */
    /*------------------------------------------------------------------------*/    
    public int getAzimuth() {
    /*------------------------------------------------------------------------*/
        return( currentSatAzimuth );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tell us the elevation position of this satellite, in degrees, according
     *  to the last calculation that was performed with the ground station set
     *  at the time of the calculations.
     *  @return Elevation position of satellite, in degree, from our station.
     *          From -90 to +90 degrees (negative when under horizon...)      */
    /*------------------------------------------------------------------------*/    
    public int getElevation() {
    /*------------------------------------------------------------------------*/
        return( currentSatElevation );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the range, in kilometers, of the satellite from the specified
     *  ground station at the time of the last calculations.
     *  @return The distance between our ground station and the satellite,
     *          in kilometers.                                                */
    /*------------------------------------------------------------------------*/    
    public int getRangeKm() {
    /*------------------------------------------------------------------------*/
        return( currentSatRangeKm );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the amount of time we have to wait before the next pass of
     *  this satellite over the ground station specified at the time of the 
     *  calculation.
     *  @return Number of minutes to wait before the next pass. Note that it
     *          is given in decimal format, so 8.5 is not 8 min 50 seconds, but
     *          8 min 30 seconds.                                             */
    /*------------------------------------------------------------------------*/    
    public double getNextAOSMinutes() {
    /*------------------------------------------------------------------------*/        
        if( nextAOSMinutes < 0.0 )
            return( (double) 0.0 );
        else
            return( (double) nextAOSMinutes );
    }

    /*------------------------------------------------------------------------*/
    /** Tells us the amount of time we have to wait before the current pass of
     *  this satellite over the ground station specified at the time of the 
     *  calculation completes.
     *  @return Number of minutes we still have for the current pass. Note that
     *          it is given in decimal format, so 8.5 is not 8 min 50 seconds,
     *          but 8 min 30 seconds.                                         */
    /*------------------------------------------------------------------------*/    
    public double getNextLOSMinutes() {
    /*------------------------------------------------------------------------*/        
        if( nextLOSMinutes < 0.0 )
            return( (double) 0.0 );
        else
            return( (double) nextLOSMinutes );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the time of the next pass, in UTC milliseconds 
     *  @return The time, in milliseconds UTC, of the start of the next pass  */
    /*------------------------------------------------------------------------*/
    public long getAOS() {
    /*------------------------------------------------------------------------*/    
        return( (long)(nextaos*24.0*60.0*60.0*1000.0) );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the time of the next end of pass, in UTC milliseconds 
     *  @return The time, in milliseconds UTC, of the end of the next pass    */
    /*------------------------------------------------------------------------*/
    public long getLOS() {
    /*------------------------------------------------------------------------*/    
        return( (long)(lostime*24.0*60.0*60.0*1000.0) );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the frequency (in hertz) to add to the nominal frequency of
     *  satellite downlink, to compensate for the Doppler effect, when this
     *  nominal frequency is at around 146MHz. Rely on a previous orbit 
     *  prediction pass.
     *  @return The frequency to add to the nominal radio frequency used to 
     *          communicate with the spacecraft, to compensate for the Doppler
     *          effect.                                                       */
    /*------------------------------------------------------------------------*/    
    public int getDopplerCompensationAt146() {
    /*------------------------------------------------------------------------*/        
        return( (int)doppler146 );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the frequency (in hertz) to add to the nominal frequency of
     *  satellite downlink, to compensate for the Doppler effect, when this
     *  nominal frequency is at around 435MHz. Rely on a previous orbit 
     *  prediction pass.
     *  @return The frequency to add to the nominal radio frequency used to 
     *          communicate with the spacecraft, to compensate for the Doppler
     *          effect.                                                       */
    /*------------------------------------------------------------------------*/    
    public int getDopplerCompensationAt435() {
    /*------------------------------------------------------------------------*/        
        return( (int)doppler435 );
    }
    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the callsign to use when sending commands to the 
     *  satellite.
     *  @param controlCallsign Callsign to use for control. 6 chars max.      */
    /*------------------------------------------------------------------------*/
    public void setControlCallsign( String controlCallsign ) {
    /*------------------------------------------------------------------------*/    
        this.controlCallsign = controlCallsign;
        if( satCtrl != null )
            satCtrl.setControlCallsign( controlCallsign );
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the secondary station ID to use when sending commands
     *  to the satellite. 
     *  @param controlSSID SSID to use for control, between 0-8.              */
    /*------------------------------------------------------------------------*/
    public void setControlSSID( int controlSSID ) {
    /*------------------------------------------------------------------------*/    
        this.controlSSID = controlSSID;
        if( satCtrl != null )
            satCtrl.setControlSSID( controlSSID );
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the current callsign used for satellite control.
     *  @return The callsign used when sending commands to the satellite.     */
    /*------------------------------------------------------------------------*/
    public String getControlCallsign() {
    /*------------------------------------------------------------------------*/    
        return( controlCallsign.trim() );
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the current SSID used for satellite control.
     *  @return The SSID used when sending commands to the satellite.         */
    /*------------------------------------------------------------------------*/ 
    public int getControlSSID() {
    /*------------------------------------------------------------------------*/    
        return( controlSSID );
    }    

    /*------------------------------------------------------------------------*/
    /** Allow us to define the callsign to use when conversing PACSAT with 
     *  the satellite.
     *  @param pacsatCallsign The callsign to use for PACSAT communication.   */
    /*------------------------------------------------------------------------*/
    public void setPacsatCallsign( String pacsatCallsign ) {
    /*------------------------------------------------------------------------*/    
        this.pacsatCallsign = pacsatCallsign;
    }    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the callsign to use when conversing PACSAT with 
     *  the satellite.
     *  @param pacsatCallsign The callsign to use for PACSAT communication.   */
    /*------------------------------------------------------------------------*/
    public void setPacsatBroadcastCallsign( String callsign ) {
    /*------------------------------------------------------------------------*/    
        this.pacsatBroadcastCallsign = callsign;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the current callsign used for PACSAT communications.
     *  @return The callsign used when talking PACSAT with this satellite.    */
    /*------------------------------------------------------------------------*/
    public String getPacsatCallsign() {
    /*------------------------------------------------------------------------*/    
        return( pacsatCallsign.trim() );
    }    
    /*------------------------------------------------------------------------*/
    /** Tells us the current callsign used for PACSAT communications.
     *  @return The callsign used when talking PACSAT with this satellite.    */
    /*------------------------------------------------------------------------*/
    public String getPacsatBroadcastCallsign() {
    /*------------------------------------------------------------------------*/    
        return( pacsatBroadcastCallsign.trim() );
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the SSID to use when conversing PACSAT with 
     *  the satellite.
     *  @param pacsatSSID The SSID to use for PACSAT communication.           */
    /*------------------------------------------------------------------------*/
    public void setPacsatSSID( int pacsatSSID ) {
    /*------------------------------------------------------------------------*/    
        this.pacsatSSID = pacsatSSID;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the current SSID used for PACSAT communications.
     *  @return The SSID used when talking PACSAT with this satellite.        */
    /*------------------------------------------------------------------------*/
    public int getPacsatSSID() {
    /*------------------------------------------------------------------------*/    
        return( pacsatSSID );
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the current address (Callsign-SSID) used for PACSAT comms.
     *  @return The address used when talking PACSAT with this satellite, in 
     *          the form CALSIGN-SSID.                                        */
    /*------------------------------------------------------------------------*/
    public String getPacsatAddress() {
    /*------------------------------------------------------------------------*/    
        return( pacsatCallsign + "-" + pacsatSSID );
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the control key for commanding this satellite.
     *  @param controlKey Control key (aka "magic cookie") to set.            */
    /*------------------------------------------------------------------------*/
    public void setControlKey( String controlKey ) {
    /*------------------------------------------------------------------------*/    
        this.controlKey = controlKey;
        if( satCtrl != null )
            satCtrl.setControlKey( controlKey );        
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the currently set control key for this satellite.
     *  @return The current control key (Magic Cookie...) for this sat.       */
    /*------------------------------------------------------------------------*/
    public String getControlKey( ) {
    /*------------------------------------------------------------------------*/    
        return(controlKey.trim());
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader encryption keys.
     *  @param aval AVAL key
     *  @param bval BVAL key
     *  @param d3val D3VAL key
     *  @param k1h K1H key
     *  @param k1l K1L key                                                    */
    /*------------------------------------------------------------------------*/
    public void setBootloaderEncryptionKeys(    int aval,
                                                int bval,
                                                int d3val,
                                                int k1h,
                                                int k1l ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) {
            satCtrl.setBLEncryptionKeys(aval,bval,d3val,k1h,k1l);
        } else {
            System.out.println("UNABLE TO SET BOOTLOADER ENCRYPTION KEYS");
        }
//        this.bootloader_aval          = aval;
//        this.bootloader_bval          = bval;
//        this.bootloader_d3val         = d3val;
//        this.bootloader_k1h           = k1h;
//        this.bootloader_k1l           = k1l;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the spacecraft ID, as used for bootloader comms.
     *  @param scid Spacecraft ID, used by bootloader.                        */
    /*------------------------------------------------------------------------*/
    public void setBootloaderSCID( int scid ) {
    /*------------------------------------------------------------------------*/        
        if( satCtrl != null )
            satCtrl.setBLSpacecraftID(scid);
        //bootloader_scid         = scid;
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the current spacecraft ID used for bootloader.
     *  @return Spacecraft ID, as used for bootloader communications.         */
    /*------------------------------------------------------------------------*/
    public int getBootloaderSCID() {
    /*------------------------------------------------------------------------*/        
        if( satCtrl != null )
            return( satCtrl.getBLSpacecraftID() );
        else
            return( -1 );
    }
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the AVAL bootloader key.
     *  @param aval AVAL bootloader key                                       */
    /*------------------------------------------------------------------------*/
    public void setBootloaderAVAL( int aval ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) satCtrl.setBLEncryptionKeys(aval,-1,-1,-1,-1);
        this.bootloader_aval          = aval;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the AVAL bootloader key.
     *  @return Value of the AVAL bootloader key                              */
    /*------------------------------------------------------------------------*/
    public int getBootloaderAVAL( ) {
    /*------------------------------------------------------------------------*/            
        return(bootloader_aval);        
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the BVAL bootloader key.
     *  @param bval BVAL bootloader key                                       */
    /*------------------------------------------------------------------------*/
    public void setBootloaderBVAL( int bval ) {
    /*------------------------------------------------------------------------*/ 
        if( satCtrl != null ) satCtrl.setBLEncryptionKeys(-1,bval,-1,-1,-1);
        bootloader_bval          = bval;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the BVAL bootloader key.
     *  @return Value of the BVAL bootloader key                              */
    /*------------------------------------------------------------------------*/
    public int getBootloaderBVAL( ) {
    /*------------------------------------------------------------------------*/    
        return( bootloader_bval );
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the D3VAL bootloader key.
     *  @param d3val D3VAL bootloader key                                     */
    /*------------------------------------------------------------------------*/
    public void setBootloaderD3VAL( int d3val ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) satCtrl.setBLEncryptionKeys(-1,-1,d3val,-1,-1);
        bootloader_d3val         = d3val;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the D3VAL bootloader key.
     *  @return Value of the D3VAL bootloader key                             */
    /*------------------------------------------------------------------------*/
    public int getBootloaderD3VAL( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_d3val);
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the K1H bootloader key.
     *  @param k1h K1H bootloader key                                         */
    /*------------------------------------------------------------------------*/
    public void setBootloaderK1H( int k1h ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) satCtrl.setBLEncryptionKeys(-1,-1,-1,k1h,-1);
        bootloader_k1h           = k1h;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the K1H bootloader key.
     *  @return Value of the K1H bootloader key                               */
    /*------------------------------------------------------------------------*/
    public int getBootloaderK1H( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_k1h);
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the K1L bootloader key.
     *  @param k1l K1L bootloader key                                         */
    /*------------------------------------------------------------------------*/
    public void setBootloaderK1L( int k1l ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) satCtrl.setBLEncryptionKeys(-1,-1,-1,-1,k1l);
        bootloader_k1l           = k1l;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the K1L bootloader key.
     *  @return Value of the K1L bootloader key                               */
    /*------------------------------------------------------------------------*/
    public int getBootloaderK1L( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_k1l);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command used to load.
     *  @param param bootloader command used to load                          */
    /*------------------------------------------------------------------------*/
    public void setBootloaderLoadCmd( int param ) {
    /*------------------------------------------------------------------------*/            
        if( satCtrl != null ) 
            satCtrl.setBLCommandCodes(param,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1);
        bootloader_loadcmd = param;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command to load
     *  @return Value of the bootloader command to load                       */
    /*------------------------------------------------------------------------*/
    public int getBootloaderLoadCmd( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_loadcmd);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command used to dump.
     *  @param param bootloader command used to dump                          */
    /*------------------------------------------------------------------------*/
    public void setBootloaderDumpCmd( int param ) {
    /*------------------------------------------------------------------------*/            
        if( satCtrl != null ) 
            satCtrl.setBLCommandCodes(-1,param,-1,-1,-1,-1,-1,-1,-1,-1,-1);
        bootloader_dumpcmd = param;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command to dump
     *  @return Value of the bootloader command to dump                       */
    /*------------------------------------------------------------------------*/
    public int getBootloaderDumpCmd( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_dumpcmd);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command used to execute a task.
     *  @param param bootloader command used to execute a task.               */
    /*------------------------------------------------------------------------*/
    public void setBootloaderExecCmd( int param ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) 
            satCtrl.setBLCommandCodes(-1,-1,param,-1,-1,-1,-1,-1,-1,-1,-1);
        bootloader_execcmd = param;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command to execute tasks
     *  @return Value of the bootloader command to execute tasks.             */
    /*------------------------------------------------------------------------*/
    public int getBootloaderExecCmd( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_execcmd);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command used to examine memory content.
     *  @param param bootloader command used to examine memory                */
    /*------------------------------------------------------------------------*/
    public void setBootloaderMemeCmd( int param ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) 
            satCtrl.setBLCommandCodes(-1,-1,-1,param,-1,-1,-1,-1,-1,-1,-1);
        bootloader_memecmd = param;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command to examine memory
     *  @return Value of the bootloader command to examine memory             */
    /*------------------------------------------------------------------------*/
    public int getBootloaderMemeCmd( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_memecmd);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command used to write memory content
     *  @param param bootloader command used to write memory content          */
    /*------------------------------------------------------------------------*/
    public void setBootloaderMemwCmd( int param ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) 
            satCtrl.setBLCommandCodes(-1,-1,-1,-1,param,-1,-1,-1,-1,-1,-1);
        bootloader_memwcmd = param;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command to write to memory
     *  @return Value of the bootloader command to write to memory            */
    /*------------------------------------------------------------------------*/
    public int getBootloaderMemwCmd( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_memwcmd);    
    }   
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command used to examine I/O port.
     *  @param param bootloader command used to examine I/O port              */
    /*------------------------------------------------------------------------*/
    public void setBootloaderIOeCmd( int param ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) 
            satCtrl.setBLCommandCodes(-1,-1,-1,-1,-1,param,-1,-1,-1,-1,-1);
        bootloader_ioecmd = param;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command to examine I/O port
     *  @return Value of the bootloader command to examine an I/O port        */
    /*------------------------------------------------------------------------*/
    public int getBootloaderIOeCmd( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_ioecmd);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command used to write to an I/O port.
     *  @param param bootloader command used to write to an I/O port          */
    /*------------------------------------------------------------------------*/
    public void setBootloaderIOpCmd( int param ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) 
            satCtrl.setBLCommandCodes(-1,-1,-1,-1,-1,-1,param,-1,-1,-1,-1);
        bootloader_iopcmd = param;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command to write to an I/O port
     *  @return Value of the bootloader command to write to an I/O port       */
    /*------------------------------------------------------------------------*/
    public int getBootloaderIOpCmd( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_iopcmd);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command used to request telemetry.
     *  @param param bootloader command used to request telemetry             */
    /*------------------------------------------------------------------------*/
    public void setBootloaderTlmCmd( int param ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) {
            System.out.println("Setting beacon bootloader command for sat "+fullName+" to "+param);
            satCtrl.setBLCommandCodes(-1,-1,-1,-1,-1,-1,-1,param,-1,-1,-1);
        }
        bootloader_tlmcmd = param;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command to request telemetry
     *  @return Value of the bootloader command to request telemetry          */
    /*------------------------------------------------------------------------*/
    public int getBootloaderTlmCmd( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_tlmcmd);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command used to move tasks into ram.
     *  @param param bootloader command used to move SCOS & PHTL into RAM     */
    /*------------------------------------------------------------------------*/
    public void setBootloaderMovCmd( int param ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) 
            satCtrl.setBLCommandCodes(-1,-1,-1,-1,-1,-1,-1,-1,param,-1,-1);
        bootloader_movcmd = param;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command to move tasks in RAM
     *  @return Value of the bootloader command to move SCOS and PHTL to RAM  */
    /*------------------------------------------------------------------------*/
    public int getBootloaderMovCmd( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_movcmd);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command 1.
     *  @param param bootloader command 1                                     */
    /*------------------------------------------------------------------------*/
    public void setBootloaderNoCmd1( int param ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) 
            satCtrl.setBLCommandCodes(-1,-1,-1,-1,-1,-1,-1,-1,-1,param,-1);
        bootloader_nocmd1 = param;
    }    
    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command 1
     *  @return Value of the bootloader command 1                             */
    /*------------------------------------------------------------------------*/
    public int getBootloaderNoCmd1( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_nocmd1);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Allow us to define the bootloader command 2
     *  @param param bootloader command 2                                     */
    /*------------------------------------------------------------------------*/
    public void setBootloaderNoCmd2( int param ) {
    /*------------------------------------------------------------------------*/    
        if( satCtrl != null ) 
            satCtrl.setBLCommandCodes(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,param);
        bootloader_nocmd2 = param;
    }    
    /*------------------------------------------------------------------------*/
    /** Tells us the value of the bootloader command 2
     *  @return Value of the bootloader command 2                             */
    /*------------------------------------------------------------------------*/
    public int getBootloaderNoCmd2( ) {
    /*------------------------------------------------------------------------*/    
        return(bootloader_nocmd2);    
    }    
    
    /*------------------------------------------------------------------------*/
    /** Set all keplerian elements of this satellite from NASA's two-line
     *  elements format strings.
     *  @param firstLine First line of the two line element keps format.
     *  @param secondLine Second line of the two line element keps format.    */
    /*------------------------------------------------------------------------*/
    public void setKeps( String firstLine, String secondLine ) {
    /*------------------------------------------------------------------------*/    

     /*------------------------------------------------*/
     /* First verify the validity of the two line text */
     /*------------------------------------------------*/
     if( firstLine == null ) return;
     if( secondLine == null) return;
     firstLine = firstLine.trim();
     secondLine = secondLine.trim();
     if( firstLine.length() < 69 ) return;
     if( secondLine.length()< 69 ) return;

     /*-----------------------------------------------------------------*/
     /* Then initialize everything to zero, to prevent residual effects */
     /*-----------------------------------------------------------------*/
     catalogNumber   = "";
     epochTime       = "";
     epochYear       = 0;
     epochDayOfYear  = 0;
     decayRate       = (double)0;
     elementSet      = "";
     inclination     = (double)0;
     RAAN            = (double)0;
     eccentricity    = (double)0;
     argOfPerigee    = (double)0;
     meanAnomaly     = (double)0;
     meanMotion      = (double)0;
     epochRevolution = 0;
        
     /*-----------------------------------------------------------------------*/
     /* Decipher first line:                                                  */
     /* 0         1         2         3         4         5         6         */
     /* 012345678901234567890123456789012345678901234567890123456789012345678 */
     /* 1 AAAAAU 00  0  0 BBBBB.BBBBBBBB  .CCCCCCCC  00000-0  00000-0 0  DDDZ */
     /* KEY: A-CATALOGNUM B-EPOCHTIME C-DECAY D-ELSETNUM Z-CHECKSUM           */
     /*-----------------------------------------------------------------------*/            
     try{
        catalogNumber = firstLine.substring( 2, 7).trim();
        epochTime     = firstLine.substring(18,32).trim();
        if( epochTime.length() > 13 ) {
            epochYear = Integer.parseInt(epochTime.substring(0,2).trim());
            epochDayOfYear = Double.parseDouble(epochTime.substring(2).trim());
        }            
        decayRate  = Double.parseDouble(firstLine.substring(33,43).trim());
        elementSet = firstLine.substring(65,68).trim();
     } catch( NumberFormatException nfe ) {
        return;
     }
        
     /*-----------------------------------------------------------------------*/
     /* Decipher second line:                                                 */
     /* 0         1         2         3         4         5         6         */
     /* 012345678901234567890123456789012345678901234567890123456789012345678 */
     /* 2 AAAAA EEE.EEEE FFF.FFFF GGGGGGG HHH.HHHH III.IIII JJ.JJJJJJJJKKKKKZ */
     /* E-INCLINATION F-RAAN G-ECCENTRICITY H-ARGPERIGEE I-MNANOM J-MNMOTION  */
     /* K-ORBITNUM                                                            */
     /*-----------------------------------------------------------------------*/
     try{
       inclination =Double.parseDouble(secondLine.substring(8,16).trim());
       RAAN        =Double.parseDouble(secondLine.substring(17,25).trim());
       eccentricity=Double.parseDouble("0."+secondLine.substring(26,33).trim());
       argOfPerigee=Double.parseDouble(secondLine.substring(34,42).trim());
       meanAnomaly =Double.parseDouble(secondLine.substring(43,51).trim());
       meanMotion  =Double.parseDouble(secondLine.substring(52,63).trim());
       epochRevolution = Integer.parseInt(secondLine.substring(63,68).trim());
     } catch( NumberFormatException nfe ) {
        return;            
     }
     kepsLoaded = true;

    }
    
    /*-----------------------------------------------------------------------*/
    /** Returns true if the keps were loaded correctly
     *  @return true if the keps were loaded correctly, false otherwise.     */
    /*-----------------------------------------------------------------------*/
    public boolean kepsLoaded() {
    /*-----------------------------------------------------------------------*/    
        return( kepsLoaded );
    }
    
    /*------------------------------------------------------------------------*/
    /** Tells us how old are the current orbital elements for this satellite.
     *  @param refTime Current time since UTC, in milliseconds.
     *  @return How old are the keps, in days.                                */
    /*------------------------------------------------------------------------*/
    public int getKepsAgeDays( long refTime ) {
    /*------------------------------------------------------------------------*/
        double daynumX = (double)( (refTime/86400000.0)-3651.0 );
        double epochX = (double)(dayNumber(1,0,epochYear)+epochDayOfYear);
        double ageX   = (double)(daynumX - epochX);
        return( (int)ageX );
    }
    
    /*-----------------------------------------------------------------------*/
    /** Returns the name of the satellite
     *  @return The name of the satellite, as it appears in the orbital 
     *          two-line elements file.                                      */
    /*-----------------------------------------------------------------------*/
    public String toString() {
    /*-----------------------------------------------------------------------*/    
        return( name );
    }

    /*-----------------------------------------------------------------------*/
    /** Returns the name of the satellite
     *  @return The name of the satellite, as it appears in the orbital 
     *          two-line elements file.                                      */
    /*-----------------------------------------------------------------------*/
    public String getName() {
    /*-----------------------------------------------------------------------*/    
        return( name );
    }
    public void setName( String str ) {
        this.name = str;
    }
    
    /*-----------------------------------------------------------------------*/
    /** Tells us the full descriptive name of the satellite.
     *  @return The full descriptive name of the satellite.                  */
    /*-----------------------------------------------------------------------*/    
    public String getFullName() {
    /*-----------------------------------------------------------------------*/        
        if( fullName != null )
            return( fullName );
        else
            return( "N/A" );
    }
    public void setFullName( String str ) {
        this.fullName = str;
    }
    
    /*-----------------------------------------------------------------------*/
    /** Adds an uplink definition to this satellite.
     *  @param freq Frequency in HERTZ (no points or formatting...)
     *  @param desc Description of the link (title...)                       */
    /*-----------------------------------------------------------------------*/
    public void addUplink(java.lang.String freq,java.lang.String desc) {        
    /*-----------------------------------------------------------------------*/    
        uplinks.addElement( new Link( freq, desc ) );
    }
    
    /*-----------------------------------------------------------------------*/
    /** Returns the first uplink defined in the list for this satellite.
     *  @return The first radio uplink for this satellite.                   */
    /*-----------------------------------------------------------------------*/
    public Link getFirstUplink() {        
    /*-----------------------------------------------------------------------*/    
        currentUplink = 0;
        if( uplinks.size() > 0 )
            return( (Link)uplinks.firstElement() );
        else
            return( null );
    }
    
    /*-----------------------------------------------------------------------*/
    /** Returns the next uplink in the list, for this satellite.
     *  @return The next uplink in the list, for this satellite.             */
    /*-----------------------------------------------------------------------*/
    public Link getNextUplink() {        
    /*-----------------------------------------------------------------------*/    
        currentUplink++;
        if( currentUplink >= uplinks.size() ) 
            return( null );
        else
            return( (Link)uplinks.elementAt(currentUplink) );
    }
    
    /*-----------------------------------------------------------------------*/
    /** Tells us how many radio comm uplinks are defined for this satellite.
     * @return The count of radio communication uplinks for this satellite.  */
    /*-----------------------------------------------------------------------*/
    public int getUplinksCount() {        
    /*-----------------------------------------------------------------------*/    
        return( uplinks.size() );
    }
    
    /*------------------------------------------------------------------------*/
    /** Allow us to select an active uplink for this satellite. This link
     *  will be used to set the radio, for example...
     *  @PARAM activeLink Defines the active uplink. Note that it can be a
     *                    link not defined in the available link list. It is 
     *                    not tied to this list.                              */
    /*------------------------------------------------------------------------*/
    public void setActiveUplink( Link activeLink ) {
    /*------------------------------------------------------------------------*/    
        this.activeUplink = activeLink;
    }
    
    /*------------------------------------------------------------------------*/
    /** Return an integer value representing the active uplink frequency in
     *  Hertz.
     *  @Return The active uplink frequency, in Hertz.                        */
    /*------------------------------------------------------------------------*/
    public int getActiveUplinkFrequency() {
    /*------------------------------------------------------------------------*/    
        if( activeUplink == null )
            return( 0 );
        else
            return( activeUplink.getFrequencyIntValue() );
    }
    
    /*-----------------------------------------------------------------------*/
    /** Adds a downlink definition to this satellite.
     *  @param freq Frequency in HERTZ (no points or formatting...)
     *  @param desc Description of the link (title...)                       */
    /*-----------------------------------------------------------------------*/
    public void addDownlink(java.lang.String freq,java.lang.String desc) { 
    /*-----------------------------------------------------------------------*/    
        downlinks.addElement( (Link)new Link( freq, desc ) );
    }
    
    /*-----------------------------------------------------------------------*/
    /** Returns the first downlink defined in the list for this satellite.
     *  @return The first radio downlink for this satellite.                 */
    /*-----------------------------------------------------------------------*/
    public Link getFirstDownlink() {        
    /*-----------------------------------------------------------------------*/    
        currentDownlink = 0;
        if( uplinks.size() > 0 )
            return( (Link)downlinks.firstElement() );
        else
            return( null );
    }
    
    /*-----------------------------------------------------------------------*/
    /** Returns the next downlink in the list, for this satellite.
     *  @return The next downlink in the list, for this satellite.           */
    /*-----------------------------------------------------------------------*/
    public Link getNextDownlink() {        
    /*-----------------------------------------------------------------------*/    
        currentDownlink++;
        if( currentDownlink >= downlinks.size() ) 
            return( null );
        else
            return( (Link)downlinks.elementAt(currentDownlink) );
    }
    
    /*-----------------------------------------------------------------------*/
    /** Tells us how many radio comm downlinks are defined for this satellite.
     * @return The count of radio communication downlinks for this satellite.*/
    /*-----------------------------------------------------------------------*/
    public int getDownlinksCount() {        
    /*-----------------------------------------------------------------------*/    
        return( downlinks.size() );
    }
    
    /*------------------------------------------------------------------------*/
    /** Allow us to select an active downlink for this satellite. This link
     *  will be used to set the radio, for example...
     *  @PARAM activeLink Defines the active downlink. Note that it can be a
     *                    link not defined in the available link list. It is 
     *                    not tied to this list.                              */
    /*------------------------------------------------------------------------*/
    public void setActiveDownlink( Link activeLink ) {
    /*------------------------------------------------------------------------*/    
        this.activeDownlink = activeLink;
    }
    
    /*------------------------------------------------------------------------*/
    /** Return an integer value representing the active downlink frequency in
     *  Hertz.
     *  @Return The active downlink frequency, in Hertz.                      */
    /*------------------------------------------------------------------------*/
    public int getActiveDownlinkFrequency() {
    /*------------------------------------------------------------------------*/    
        if( activeDownlink == null )
            return( 0 );
        else
            return( activeDownlink.getFrequencyIntValue() );
    }
    
    /*------------------------------------------------------------------------*/
    /** Returns text notes associated with this satellite.
     *  For now the notes are located in a file in the base directory
     *  @param baseDir Base directory where to look for note file
     *  @param pane Text field where to put the notes text.                   */
    /*------------------------------------------------------------------------*/
    public void getNotes( String baseDir, JTextComponent pane ) {
    /*------------------------------------------------------------------------*/
        notes = "";
        if( name == null ) return;
        if( pane == null ) return;
        pane.setText("N/A");
        if( baseDir == null ) baseDir = "";
        try {
            FileReader fr = new FileReader( baseDir.trim()+name.trim()+".txt" );
            pane.read(fr,null);
            fr.close();
        } catch( IOException ioe ) {}
        catch( Exception e ) {}
        
        notes = pane.getText();
        
        return;
    }
    
    /*-----------------------------------------------------------------------*/
    /** Save text notes associated with this satellite
     *  @param baseDir Base directory where the note files reside.
     *  @param notesText Text of the note.                                   */
    /*-----------------------------------------------------------------------*/
    public void saveNotes( String baseDir, String notesText ) {
    /*-----------------------------------------------------------------------*/
        if( name == null ) return;
        if( notesText == null ) return;
        if( baseDir == null ) baseDir = "";
        try {
            FileWriter fw = new FileWriter( baseDir.trim()+name.trim()+".txt" );
            fw.write(notesText);
            fw.close();
        } catch( IOException ioe ) {}
        catch( Exception e ) {}
        return;        
    }
    
    /*------------------------------------------------------------------------*/
    /** Allow us to set a mode where more messages are printed reguarding the
     *  status of this satellite.
     *  @param verboseSetting true to print more details.                     */
    /*------------------------------------------------------------------------*/
    public void setVerbose( boolean verboseSetting ) {
    /*------------------------------------------------------------------------*/    
        verbose = verboseSetting;
    }

    
    /*------------------------------------------------------------------------*/
    /** Calculates the position of the satellite from the current computer
     *  time, the keplerian elements of this satellite, and the ground station
     *  coordinates given.
     * @param groundRef Reference ground station location
     * @param refTime Reference time (in milliseconds since UTC epoch, 1970)  */
    /*------------------------------------------------------------------------*/
    public void calculatePosition( GroundStation groundRef, long refTime ) {
    /*-----------------------------------------------------------------------*/    
    
        /*-----------------*/
        /* Pre-calculation */
        /*-----------------*/                
        daynum = (double)( (refTime/86400000.0)-3651.0 );
    
        /*------------------------*/
        /* Date reference (epoch) */
        /*------------------------*/
        epoch = (double)(dayNumber( 1, 0, epochYear ) + epochDayOfYear);  
        
        /*--------------------*/
        /* Age of keps (days) */
        /*--------------------*/
        age   = (double)(daynum - epoch);
        
        /*------------------------------------*/
        /* Year, compensated for post 2000... */
        /*------------------------------------*/
        year  = (double)epochYear;
        if( year <= 50.0 )
            year += 100.0;    //Y2K... Pretty poor stuff
    
        t1 =    (double)(year - 1.0);    //???
        
	df  =   (double)366.0 +
                (double)java.lang.Math.floor(365.25*(t1-80.0)) -
                (double)java.lang.Math.floor((double)(t1/100.0)) +
                (double)java.lang.Math.floor((double)(t1/400.0+0.75));                
        
	t1  =   (double)((df+29218.5)/36525.0);        
	t1  =   (double)(6.6460656+t1*(2400.051262+t1*2.581e-5));
        
	se  =   (double)(t1/24.0)-(double)year;        
        
        /*----------------------------*/
        /* Mean motion with decayrate */
        /*----------------------------*/
	n0  =   (double)meanMotion + (double)( age * decayRate );
        
        /*-----------------*/
        /* Semi Major Axis */
        /*-----------------*/
	sma =   (double)331.25 * (double)java.lang.Math.pow( (1440.0/n0), (2.0/3.0) );
        
	e2  =   (double)1.0 - ( eccentricity * eccentricity );
        
	e1  =   (double)java.lang.Math.sqrt( e2 );
        
	k2  =   (double)9.95 * 
                ( (double)java.lang.Math.exp( java.lang.Math.log( R0/sma ) * 3.5 ) ) / (e2 * e2);        
        
	s1  =   (double)java.lang.Math.sin( inclination * deg2rad );
        
	c1  =   (double)java.lang.Math.cos( inclination * deg2rad );                
        
	l8  =   (double)(groundRef.getLatitude() * deg2rad); 
        
	s9  =   (double)java.lang.Math.sin( l8 );         
        
	c9  =   (double)java.lang.Math.cos( l8 );        
        
	s8  =   (double)java.lang.Math.sin( -groundRef.getLongitude() * deg2rad ); 
        
	c8  =   (double)java.lang.Math.cos( groundRef.getLongitude() * deg2rad );        
        
	r9  =   (double)R0 * 
                (double)(1.0 + (FF/2.0) * ( java.lang.Math.cos(2.0*l8) - 1.0 )) + 
                ((double)groundRef.getAltitude()/1000.0);        
        
	l8  =   (double)java.lang.Math.atan( (1.0-FF) * (1.0-FF) * s9/c9 );        
        
	z9  =   (double)r9 * (double)java.lang.Math.sin(l8);
        
	x9  =   (double)r9 * (double)java.lang.Math.cos(l8) * (double)c8;        
        
	y9  =   (double)r9 * (double)java.lang.Math.cos(l8) * (double)s8;
        
	apogee  = (double)sma * (double)(1.0 + eccentricity) - (double)R0;
	perigee = (double)sma * (double)(1.0 - eccentricity) - (double)R0;

//        verbosePrint( "Semi-Major Axis: "+Double.toString(sma) );
//        verbosePrint( "eccentricity: "+Double.toString(eccentricity) );
//        verbosePrint( "R0: "+Double.toString(R0) );
//        verbosePrint( "Apogee: "+Double.toString(apogee) );
//        verbosePrint( "perigee: "+Double.toString(perigee) );

        /*----------------------------------------*/
        /* Then calculate the tracking parameters */
        /*----------------------------------------*/
	aoshappens      = isAOSPossible( groundRef );
//        if( aoshappens ) verbosePrint( "AOS is possible" );
//        else verbosePrint( "AOS is NOT possible" );
        
	geostationary   = isGeostationary();
//        if( geostationary ) verbosePrint( "This is a geostat sat" );
//        else verbosePrint( "Sat orbit is not geostat" );
        
	decayed         = isDecayed();
//        if( decayed ) verbosePrint( "This sat's orbit has decayed" );
//        else verbosePrint( "Sat is in orbit" );
        
        daynum = (double)( (refTime/86400000.0)-3651.0 );
//        verbosePrint( "Current day number: "+daynum );
        
        Calc();

        /*....................................................*/
//        verbosePrint("Elevation: "+iel+" Azimuth: "+iaz);
        /*....................................................*/

        dt = daynum * 86400.0 - oldClock;
        dp = (rk * 1000.0) - oldRange;

        if( dt > 0.0 ) {
            /*-----------------------------*/
            /* Calculate the Doppler shift */
            /*-----------------------------*/
            fraction    = -((dp/dt)/299792458.0);
            doppler146  = fraction * 146.0e6;
            doppler435  = fraction * 435.0e6;
            oldClock    = 86400.0 * daynum;
            oldRange    = rk * 1000.0;
        }

        /*---------------------------------------------*/
        /* During a pass compensate for Doppler effect */
        /*---------------------------------------------*/
        if( elevation >= 0.0 ) {
            
            isSignalAcquired = true;
            
            /*-----------------*/
            /* Path Loss in DB */
            /*-----------------*/
            //pl146 = 75.6870571232 + (20.0*log10(rk));
            //pl435 = pl146 + 9.482728;

            if( oncethru && (dt>0.0) ) {
                if( doppler435 > 0.0 )
                    isApproaching = true;
                else
                    isApproaching = false;
            }
            
        }//Elevation was > 0
        else {
            isSignalAcquired = false;
            lostime = 0.0;
        }
        
        /*----------------------------------------------*/
        /* Record the sat parameters for others         */
        /* to retrieve, before we do other calculations */
        /* to find los, aos, etc....                    */
        /*----------------------------------------------*/
        currentSatAzimuth       = iaz;
        currentSatElevation     = iel;
        currentSatLatitude      = isplat;
        currentSatLongitude     = isplong;
        currentSatRangeKm       = irk;
        currentSatAltitudeKm    = iak;
        currentSatVelocityKmH   = ivk;
        currentSatFootprintKm   = ifk;

        /*----------------------------------------------------*/
        /* Find acquisition of signal and loss of signal time */
        /*----------------------------------------------------*/
        double currentDaynum = daynum;
        if( (elevation>=0.0) && !geostationary && !decayed && daynum>lostime ) {
            lostime = FindLOS2(groundRef);
            aoslos = lostime;
        }
        else if( (elevation<0.0) && !geostationary && !decayed && aoshappens && (daynum>nextaos) ) {
            daynum+=0.003;  // Move ahead slightly...
            nextaos=FindAOS( groundRef );
            aoslos=nextaos;
        }
        nextAOSMinutes = (double)nextaos - (double)currentDaynum;
        nextAOSMinutes = (double)nextAOSMinutes * (double)1440.0;
        
        nextLOSMinutes = (double)lostime - (double)currentDaynum;
        nextLOSMinutes = (double)nextLOSMinutes * (double)1440.0;
        
        oncethru = true;
        
    }
    
    /*------------------------------------------------------------------------*/
    /** Returns the number of days between the given date and our reference
     * date, 31 dec 1979...
     * @param month 1-12
     * @param day   0-30 (first day = 0, DO NOT FORGET!)
     * @param year  XX + 1900 or 2000
     * @return Number of days between the given date and our reference.       */
    /*------------------------------------------------------------------------*/    
    private long dayNumber( int month, int day, int year ) {
    /*------------------------------------------------------------------------*/        
        long dn = 0;
        double mm, yy;
        
        if( month < 3 ) {
            year--;
            month += 12;
        }
        
        if( year < 50 ) year += 100;
        
        yy = (double)year;
        mm = (double)month;
        dn = (long)(   java.lang.Math.floor(365.25*(yy-80.0))
                     - java.lang.Math.floor(19.0+yy/100.0)
                     + java.lang.Math.floor(4.75+yy/400.0) 
                     - 16.0 );
        dn += day + 30 * month + (long)java.lang.Math.floor(0.6*mm-0.3);
        
        return( dn );   
    }
    
    /*------------------------------------------------------------*/
    /** reads the system clock and return the number of days since 
     *  31 Dec 1979 00:00:00 (daynum 0).    
     * @DEPRECATED                                                */
    /*------------------------------------------------------------*/
    double currentDayNumber() {
    /*------------------------------------------------------------*/    
        Date date = new Date(); //Gets today's date (in OUR timezone)
        return( (date.getTime()/86400000.0)-3651.0 );
    }
    
    /*------------------------------------------------------------------------*/
    /** Returns FALSE is there is no possibility of AOS at this station...
     *  @param groundRef A GroundStation object defining our position on 
     *                   the ground (lat & long, height above sea...)
     *  @return true is acquisition of signal is possible from our position
     *          on the ground, false otherwise.                               */
    /*------------------------------------------------------------------------*/
    private boolean isAOSPossible( GroundStation groundRef ) {
    /*--------------------------------------------------------------------*/    
        double lin      = 0.0;
        double sma      = 0.0;
        double apogee   = 0.0;
    
        lin = inclination;
        if( lin >= 90.0 ) lin = 180.0-lin;
	sma = 331.25 
          * java.lang.Math.exp(java.lang.Math.log(1440.0/meanMotion)*(2.0/3.0));
	apogee = sma * (1.0+eccentricity) - R0;
	if(   (java.lang.Math.acos(R0/(apogee+R0))+(lin*deg2rad)) 
            >  java.lang.Math.abs(groundRef.getLatitude()*deg2rad) )
        {
		return true;
        } else {
		return false;        
        }
    }
    
    /*------------------------------------------------------------------------*/
    /** Returns TRUE if this satellite appears to be in geostat orbit
     *  @return true if this satellite is geostationary, false otherwise.     */
    /*------------------------------------------------------------------------*/
    boolean isGeostationary() {
    /*------------------------------------------------------------------------*/
        if( java.lang.Math.abs(meanMotion-1.0027) < 0.0002 )
		return true;
	else
		return false;
    }
    
    /*------------------------------------------------------------------------*/
    /** Returns TRUE if it appears that the satellite has decayed at this time.
     *  @return true if satellite has decayed, false otherwise                */
    /*------------------------------------------------------------------------*/
    boolean isDecayed() {
    /*------------------------------------------------------------------------*/    
        double satepoch;

	satepoch = dayNumber( 1, 0, epochYear ) + epochDayOfYear;

	if( satepoch + ((16.666666-meanMotion)/(10.0*java.lang.Math.abs(decayRate))) < daynum)
		return true;
	else
		return false;        
    }

    /*------------------------------------------------------------------------*/
    /** This function finds and returns the time of AOS (aostime).
     *  @param groundRef A GroundStation object defining our position on 
     *                   the ground (lat & long, height above sea...)
     *  @return Time of the acquisition of signal, in fraction of days since
     *          our absolute reference epoch.                                 */
    /*------------------------------------------------------------------------*/
    double FindAOS( GroundStation groundRef ) {
    /*------------------------------------------------------------------------*/	
        double aostime = 0.0;
        if( isAOSPossible( groundRef ) && !isGeostationary() && !isDecayed() ) {
            
            Calc();

            /*----------------------------*/
            /* Get the satellite in range */
            /*----------------------------*/
            while( elevation < -1.0 ) {
                
                daynum-=0.00035*(elevation*(((ak/8400.0)+0.46))-2.0);

                /*--------------------------------------------------------*/
                /* Technically, this should be:                           */
                /*                                                        */
                /*  daynum-=0.0007*(elevation*(((ak/8400.0)+0.46))-2.0);  */
                /*                                                        */
                /*  but it sometimes skipped passes for satellites in     */
                /*  highly elliptical orbits.                             */
                /*--------------------------------------------------------*/
                Calc();
            }

            /*----------*/
            /* Find AOS */
            /*----------*/
            /*----------------------------------------------------------*/
            /** Users using Keplerian data to track the Sun MAY find    */
            /*  this section goes into an infinite loop when tracking   */
            /*  the Sun if their QTH is below 30 deg N!                 */
            /*----------------------------------------------------------*/
            while( aostime == 0.0 ) {

                if( java.lang.Math.abs( elevation ) < 0.03 )
                    aostime = daynum;
                else {
                    daynum -= elevation * java.lang.Math.sqrt(ak)/530000.0;
                    Calc();
                }
            }
            
	}

	return( aostime );
    }//end find AOS

    /*------------------------------------------------------------------------*/
    /** This function finds and returns the time of LOS (Loss of Signal).
     *  @param groundRef A GroundStation object defining our position on 
     *                   the ground (lat & long, height above sea...)
     *  @return Time of the loss of signal, in fraction of days since
     *          our absolute reference epoch.                                 */
    /*------------------------------------------------------------------------*/
    private double FindLOS(GroundStation groundRef) {
    /*------------------------------------------------------------------------*/        
        
	lostime = 0.0;

	if( !isGeostationary() && isAOSPossible(groundRef) && !isDecayed() ) {
            Calc();
            while( true ) {
                daynum += elevation * java.lang.Math.sqrt(ak)/502500.0;
                Calc();
                if( java.lang.Math.abs(elevation) < 0.03 )
                    lostime=daynum;
                if( lostime!=0.0 ) break;
            }
	}
        return lostime;
    }

    /*------------------------------------------------------------------------*/
    /** This function steps through the pass to find LOS.
     *  FindLOS() is called to "fine tune" and return the result.
     *  @param groundRef A GroundStation object defining our position on 
     *                   the ground (lat & long, height above sea...)
     *  @return Time of the loss of signal, in fraction of days since
     *          our absolute reference epoch.                                 */
    /*------------------------------------------------------------------------*/
    private double FindLOS2(GroundStation groundRef) {
    /*------------------------------------------------------------------------*/

        while( true ) {
            daynum +=   java.lang.Math.cos((elevation-1.0) * deg2rad) 
                      * java.lang.Math.sqrt(ak)/25000.0;
            Calc();
            if( elevation < 0.0 ) break;
	}
	return( FindLOS(groundRef) );
    }

    
    /*------------------------------------------------------------------------*/
    /** This is the stuff we need to do repetitively.                         */
    /*------------------------------------------------------------------------*/
    private void Calc() {
    /*------------------------------------------------------------------------*/    

	age     = daynum-epoch;
	o       = deg2rad * (RAAN - (age) * k2 * c1);
	s0      = java.lang.Math.sin( o ); 
	c0      = java.lang.Math.cos( o );
	w       = deg2rad * (argOfPerigee + (age) * k2 * (2.5*c1*c1-0.5) );
	s2      = java.lang.Math.sin( w ); 
	c2      = java.lang.Math.cos( w );
	c[1][1] = c2 * c0 - s2 * s0 * c1;
	c[1][2] = -s2 * c0 - c2 * s0 * c1;
	c[2][1] = c2 * s0 + s2 * c0 * c1;
	c[2][2] = -s2 * s0 + c2 * c0 * c1;
	c[3][1] = s2 * s1;
	c[3][2] = c2 * s1;
	q0      = (meanAnomaly/360.0) + epochRevolution;
	q       = n0 * age + q0; 
	rv      = (int)java.lang.Math.floor( q );
	q       = q - java.lang.Math.floor( q );
	m       = q * TP;
	e       = m + eccentricity * (java.lang.Math.sin(m)+0.5 * eccentricity * java.lang.Math.sin(m*2.0) );

        /*-------------------*/
        /* Kepler's Equation */
        /*-------------------*/
        int count = 0;
        while( true ) {
            s3 = java.lang.Math.sin( e ); 
            c3 = java.lang.Math.cos( e ); 
            r3 = 1.0 - eccentricity * c3;
            m1 = e - eccentricity * s3; 
            m5 = m1 - m;
            e  = e - m5/r3;
            if( java.lang.Math.abs(m5)<1.0e-6 ) break;
            //if( count++ > 20000000 ) {
            //    System.out.println("UNRELIABLE DATA");
            //    count = 0;
            //    break;
            //}
        }

        x0      = sma * (c3-eccentricity); 
	yzero   = sma * e1 * s3;
	r       = sma * r3; 
	x1      = x0 * c[1][1] + yzero * c[1][2];
	yone    = x0 * c[2][1] + yzero * c[2][2];
	z1      = x0 * c[3][1] + yzero * c[3][2];
	g7      = (daynum-df) * 1.0027379093 + se;
	g7      = TP * (g7-java.lang.Math.floor(g7));
	s7      = -java.lang.Math.sin(g7); 
	c7      = java.lang.Math.cos(g7);
        x       = x1 * c7 - yone * s7; 
        y       = x1 * s7 + yone * c7;
        z       = z1; 
        x5      = x - x9; 
        y5      = y - y9; 
        z5      = z - z9;
        range   = x5 * x5 + y5 * y5 + z5 * z5;
        z8      = x5 * c8 * c9 + y5 * s8 * c9 + z5 * s9;
        x8      = -x5 * c8 * s9 - y5 * s8 * s9 + z5 * c9;
        y8      = y5 * c8 - x5 * s8; 
        ak      = r - R0;
        
        elevation   = java.lang.Math.atan( z8 / java.lang.Math.sqrt( range - z8 * z8 ) ) / deg2rad;
        azimuth     = java.lang.Math.atan( y8 / x8 ) / deg2rad;

        if( x8 < 0.0 )
            azimuth += 180.0;

        if( azimuth < 0.0 )
            azimuth += 360.0;

        ma256   = (int)(256.0 * q);
        am      = ak/KM;
        rk      = java.lang.Math.sqrt(range); 
	rm      = rk/KM;
	vk      = 3.6 * java.lang.Math.sqrt(3.98652e+14*((2.0/(r*1000.0))-1.0/(sma*1000.0)));
	vm      = vk/KM;
	fk      = 12756.33 * java.lang.Math.acos(R0/r);
	fm      = fk/KM;
	ssplat  = java.lang.Math.atan(z/java.lang.Math.sqrt(r*r-z*z))/deg2rad;
	ssplong = -java.lang.Math.atan(y/x)/deg2rad;

        if( x < 0.0 )
            ssplong += 180.0;

        if( ssplong < 0.0 )
            ssplong+=360.0;

        irk     = (int)java.lang.Math.rint(rk);
        iak     = (int)java.lang.Math.rint(ak);
        ivk     = (int)java.lang.Math.rint(vk);
        ifk     = (int)java.lang.Math.rint(fk);
        isplat  = (int)java.lang.Math.rint(ssplat);
        isplong = (int)java.lang.Math.rint(ssplong);
        iaz     = (int)java.lang.Math.rint(azimuth);
        iel     = (int)java.lang.Math.rint(elevation);        
        
    }

    
    /*------------------------------------------------------------------------*/
    /** Prints message only when "verbose" is true.
     *  @param message String to print if verbose is active.                  */
    /*------------------------------------------------------------------------*/
    private void verbosePrint( String message ) {
    /*------------------------------------------------------------------------*/    
        if( verbose ) System.out.println( message );
    }
    
}
