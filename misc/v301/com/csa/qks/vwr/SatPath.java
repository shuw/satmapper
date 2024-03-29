/*
 * SatPath.java
 *
 * Created on March 27, 2002, 2:52 PM
 */

package com.csa.qks.vwr;

/**
 *
 * @author  TWu
 * @version 
 */
public class SatPath {
    double[][] pathCoords = new double[0][0];

    /** Creates new SatPath */
    public SatPath() {
    }
    
    
    public void createPath( Satellite aSat) {
        Satellite aSat = new Satellite();
        com.csa.qks.vwr.GroundStation agency = new com.csa.qks.vwr.GroundStation("montreal", "10.20.36.71", 45.5D, 73.582999999999998D, 0);
        
                
        
        int maxArraySize = 500;
        double[] longCoords = new double[maxArraySize];
        double[] latCoords = new double[maxArraySize];
        double[] timeCoords = new double[maxArraySize];
        int longCoordsSize = 0;
        
        //the coordinates of the path going west from satellite
        double[] longCoords2 = new double[maxArraySize];
        double[] latCoords2 = new double[maxArraySize];
        double[] timeCoords2 = new double[maxArraySize];
        int longCoords2Size = 0;
        
        //        aSat.calculatePosition(agency, System.currentTimeMillis());
        
        long timeNow = System.currentTimeMillis();
        
        
        //        double timeInterval = sat.getSpeed() * constant; //determines how often to get coordinates
        long timeInterval = 25000;
        long time = timeNow; //test code
        
        double longCoord;
        double latCoord;
        
        int i = 0;
        do {
            aSat.calculatePosition(agency, time);
            
            longCoord = aSat.getLongitude();
            latCoord = aSat.getLatitude();
            
            if(longCoord < 180L) longCoord = -longCoord;
            else if(longCoord > 180L) longCoord = 360L - longCoord;
            
            if (i == 0) {
                longCoords[i] = longCoord;
                latCoords[i] = latCoord;
                timeCoords[i] = time;
            }
            else {
                timeCoords[i] = time;
                if ( Math.abs(longCoords[i-1] - longCoord)  <  180 && i > 0)
                    longCoords[i] = longCoord;
                else break;
                if ( Math.abs(latCoords[i-1] - latCoord) <  90 && i > 0)
                    latCoords[i] = latCoord;
                else break;
            }            
            
            time += timeInterval; i++;
            
        } while (i < maxArraySize);
        longCoordsSize = i;
        
        time = timeNow;
        
        i = 0;
        do {
            time -= timeInterval;
            
            aSat.calculatePosition(agency, time);
            
            longCoord = aSat.getLongitude();
            latCoord = aSat.getLatitude();
            
            if(longCoord < 180L) longCoord = -longCoord;
            else if(longCoord > 180L) longCoord = 360L - longCoord;
            
            if (i == 0) {
                longCoords2[i] = longCoord;
                latCoords2[i] = latCoord;
                timeCoords2[i] = time;
            }
            else {
                if ( Math.abs(longCoords2[i-1] - longCoord)  <  180 && i > 0)
                    longCoords2[i] = longCoord;
                else break;
                if ( Math.abs(latCoords2[i-1] - latCoord) <  90 && i > 0)
                    latCoords2[i] = latCoord;
                else break;
                timeCoords2[i] = time;
            }

            i++;
        } while (i < maxArraySize);        
        longCoords2Size = i;
        
        
        int size = longCoordsSize + longCoords2Size;
        
        pathCoords = new double[3][size];

        
        
        i = 0;
        while ( longCoords2Size > 0 ) {
//            System.out.println("i: " + longCoords2Size);
            
            pathCoords[0][i] = longCoords2[ longCoords2Size -1 ];
            pathCoords[1][i] = latCoords2[ longCoords2Size -1 ];
            pathCoords[2][i] = timeCoords2[ longCoords2Size -1 ];
            longCoords2Size--;
            i++;
            System.out.println("TIme1: " + pathCoords[2][i]);
        }
        
        int k = 0;
        while ( k < longCoordsSize  ) {
            pathCoords[0][i] = longCoords[ k ];
            pathCoords[1][i] = latCoords[ k ];
            pathCoords[2][i] = timeCoords2[ k ];
            System.out.println("TIme2: " + pathCoords[2][i]);
            i++; k++;
        }
    }
}
