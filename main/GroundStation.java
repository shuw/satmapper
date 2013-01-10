/*
 * GroundStation.java
 *
 * Created on April 20, 2001, 10:27 AM
 */

package qsgs;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class GroundStation extends Object {
double  latitude = 0;
double  longitude = 0;   //Degr
int     altitude = 0; //m
String  callsign = "";

private String name = "Local";
private String networkAddress = "localhost";        


    /** Creates new GroundStation */
    public GroundStation() {
    }
    
    public GroundStation(  String name, 
                            String address, 
                            double latitude, double longitude, int altitude ) {
                                
            this.name           = name;
            this.networkAddress = address;
            this.latitude       = latitude;
            this.longitude      = longitude;
            this.altitude       = altitude;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    public String getName() {
        return( name );
    }
    
    public void setLatitude( double latToUse ) {
        latitude = latToUse;
    }
    public double getLatitude() {
        return( latitude );
    }
    public void setLongitude( double longToUse ) {
        longitude = longToUse;
    }
    public double getLongitude() {
        return( longitude );
    }
    public void setAltitude( int altToUse ) {
        altitude = altToUse;
    }
    public int getAltitude() {
        return( altitude );
    }
    public void setCallsign( String callsignToUse ) {
        callsign = callsignToUse;
    }
    public String getCallsign() {
        return( callsign );
    }
    public void setNetworkAddress( String networkAddress ) {
        this.networkAddress = networkAddress;
    }
    public String getNetworkAddress(){
        return( networkAddress );
    }
    public String toString() {
        return( name );
    }

}
