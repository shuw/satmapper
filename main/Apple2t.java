/*
 * Applet.java
 *
 * Created on February 12, 2002, 2:29 PM
 */

/**
 *
 * @author  TWu
 * @version 
 */
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.lang.Math;
import java.net.URL;
import java.applet.*;

public class Applet extends java.applet.Applet implements Runnable {
    int numSats = 1;
    int i = 0;
    //grid for map
    static final int MapX = 700;
    static final int MapY = 350;
    //midpoint used for calculating coordinates
    static final double MapMidX = MapX / 2;
    static final double MapMidY = MapY / 2;
    int GridX = 12;
    int GridY = 6;
    
    //fonts
    Font nameFont = new Font("Arial" , Font.PLAIN, 11);
    Font gridFont = new Font("Arial" , Font.PLAIN, 14);    
    
    //location of mouse
    double mouseLong, mouseLat;
    
    boolean testVariable = true;
    
    Vector satsVect = new Vector();
    Vector placesVect = new Vector();

    Range satsRange;
    Satellite satSelected = new Satellite();
    
    //threads
    Thread drawSprites;
    Thread clock;
    Thread beamAnimate; 
        double beamFraction = 0; //used to animate beam
        boolean startBeam; //starts animation of beam transfer
        Color beamColor;
        Place beamPlace;
        Satellite beamSat;
    
    
    //images
    Image offscreenImage;
    Image mapImg,satImg,recImg,recSignalImg;
    
    Graphics offscreen;
    Date DateNow;
    
    public void init () {
        setSize( 800, 650 );
        //creates double buffer
        offscreenImage = createImage(getSize().width, getSize().height);        
        offscreen = offscreenImage.getGraphics();
        
        mapImg = getImage(getDocumentBase(),"Map.jpg");
        satImg = getImage(getDocumentBase(),"SatGeneric.gif");
        recImg = getImage(getDocumentBase(),"Receiver.gif");
        recSignalImg = getImage(getDocumentBase(),"ReceiverSignal.gif");        
        
        System.out.println("\nCodeBase: " + getCodeBase() );
        
        //Satellites are created here
        satsVect.addElement( new Satellite(0, 0, 7000, 3000, 5000, 2, "Tom") );
        satsVect.addElement( new Satellite(0, 0, 3000, 4000, 12000, 43, "Satellite2") );
        satsVect.addElement( new Satellite(0, 20, 4000, 5000, 7000, 70, "Satellite3") );
        satsVect.addElement( new Satellite(0, 0, 4000, 6000, 15000, 143, "Satellite4") );
        
        placesVect.addElement( new Place(45.68,-74.03, "Montreal") );
        placesVect.addElement( new Place(40,100, "Beijing") );
    }
    
    public void start() {
            if (drawSprites == null) {
                drawSprites = new Thread(this);
                drawSprites.start();
           }
            if (clock == null) {
                clock = new Thread(this);
                clock.start();
            }
    }
    
    public void stop() { drawSprites = null; clock = null; }
    
    public void run() {
    Thread thisThread = Thread.currentThread();
        while (drawSprites ==  thisThread) {
            int speedTimeX = 400;
            int framesPerSecond = 24;
            
            
            //System.out.println("SIZE: " + satsVect.size() ); 
            //updates Satellite Position for minute
            for (i = 0; i < satsVect.size(); i++) {
                Satellite sat = (Satellite)satsVect.elementAt(i);
                sat.moveScalar(speedTimeX/framesPerSecond);  }
            
            try {
                Thread.sleep(1000/framesPerSecond);
            } catch (InterruptedException e) {}

            repaint();
            //stop();
        }
   
        while (clock ==  thisThread) {
            DateNow = new Date();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
        
        if (beamAnimate == thisThread) {
            int frames = 8;
            beamFraction = 0;
            startBeam = false;
            
            if (beamFraction < 1) {
                for (beamFraction = 0; beamFraction <= 1; beamFraction = beamFraction + (1.0 / frames) ) {
                    try {
                            Thread.sleep( 400 / frames );
                        } catch (InterruptedException e) {}
                    }
            }
            else {
                beamColor = Color.gray;
                beamFraction = 1;
                startBeam = true;
            }    
         }
    }
    
    public void update(Graphics g) { paint(g); }
    
    public void paint(Graphics g) {
        //creates background
        offscreen.setColor(Color.black);
        offscreen.fillRect(0, 0, getSize().width, getSize().height);
        
        offscreen.drawImage(mapImg, 0, 0, MapX, MapY, this);
        
        offscreen.setColor(Color.orange);
        offscreen.drawString(DateNow.toString() , 20, MapY + 25);
        
        offscreen.setColor(Color.white);
        offscreen.drawString("Latitude: " + (int)mouseLat + " Longitude: " + (int)mouseLong , 400, MapY + 25);
        offscreen.drawString("BeamAnimation: " + beamFraction, 400, MapY + 40);
        
        drawGrid(offscreen);
        drawSprites(offscreen, placesVect);
        drawSprites(offscreen, satsVect);       
        drawSatInfo(offscreen, satSelected);
        
        //g.drawImage(gridImage, 0, 0 ,this);
        g.drawImage(offscreenImage, 0, 0 ,this);
        
    }
    
  
    //draws all Sprites in Vector
    public void drawSprites(Graphics g, Vector satsVectDraw) {
        int i; 
        for (i = 0; i < satsVectDraw.size(); i++) {
            if (satsVectDraw.elementAt(i) instanceof Satellite)
                drawSatellite( g, (Satellite)satsVectDraw.elementAt(i) ); 
            else if (satsVectDraw.elementAt(i) instanceof Place)
                drawPlace( g, (Place)satsVectDraw.elementAt(i) );
        }  
    }
        
    public void drawPlace(Graphics g, Place spriteDraw) {
        g.setColor(Color.white);
        
        //get satellite coordinates in pixel system
        int satX = getXCoord(spriteDraw.getLong());
        int satY = getYCoord(spriteDraw.getLat());
        
        spriteDraw.findContacts(satsVect);
        if ( spriteDraw.hasContact ) {
            if ( spriteDraw.meetsWith(satSelected) ) {
                if (beamAnimate == null) {
                    beamAnimate = new Thread(this);
                    beamAnimate.start();
                    beamPlace = spriteDraw;
                    beamSat = satSelected;
                    System.out.println("BEAM STATRTED");
                }

                
                if (startBeam) { g.setColor(beamColor); }            
                else { g.setColor(Color.white); }
                    
                g.setColor(beamColor);
                //animated Beam
                int[] uplinkX = { satX, (int)( satX + beamFraction * (  getXCoord(satSelected.getLong()) - 7  -satX)), 
                                  (int)( satX + beamFraction * ( getXCoord(satSelected.getLong()) + 5 -satX )) };
                int[] uplinkY = { satY, (int) (satY + beamFraction * ( getYCoord(satSelected.getLat()) - satY ) ), 
                                  (int)( satY + beamFraction * ( getYCoord(satSelected.getLat()) - 1 -satY) )};
                g.fillPolygon( uplinkX, uplinkY, 3);
            }
            
            //g.fillOval(satX - 5, satY -5, 10, 10);
            g.drawImage(recSignalImg, satX - 7, satY -30, this);
            
            g.setColor(Color.white); g.setFont( new Font("Arial" , Font.BOLD, 11) );
            g.drawString( "Contact With:" , satX - 15, satY + 32);
            
            int i; for (i = 0; i < spriteDraw.getContacts().length; i++) {
                g.drawString( spriteDraw.getContacts()[i] , satX - 15, satY + 43 + i*10);
            }           
        }
        else {
            //resets animation if satellite leaves range of location or another satellite is selected
            if (beamPlace == spriteDraw || beamSat != satSelected ) {
                 beamAnimate = null;
                 beamFraction = 0;
            }
            
            g.drawImage(recImg, satX - 5, satY -28, this);
            g.setColor(Color.white);
            //g.fillOval(satX - 3, satY -3, 6, 6);
        }
        
        if (spriteDraw.showName) {
            g.setColor(Color.white); g.setFont(nameFont);
            g.drawString( spriteDraw.getName(), satX - 15, satY + 18);
        }
    }
    
     public void drawSatellite(Graphics g, Satellite spriteDraw) {
        //get satellite coordinates in pixel system
        int satX = getXCoord(spriteDraw.getLong());
        int satY = getYCoord(spriteDraw.getLat());
        
 
        //draws range circle
        if ( spriteDraw.showRange )
            drawRange( g, spriteDraw );  
        
        //draws Sat
        g.setColor(Color.yellow);
        g.drawImage(satImg, satX - 20, satY - 10, this);
        
        if (spriteDraw.showName) {
            if (spriteDraw == satSelected)
                g.setColor(Color.green);
            else
                g.setColor(Color.white);
            g.setFont(nameFont);
            g.drawString( spriteDraw.getName(), satX - 15, satY + 26);
        }
    }
    
    public void drawRange(Graphics g, Satellite sat) {
       if (sat == satSelected)
            g.setColor(Color.green);
       else
           g.setColor(Color.white);
       
       Range satRange = sat.getRangeObj();
       
       int countCoords = satRange.getLongCoords().length;
       
       //System.out.println("\nCOUNT CORDS: " + countCoords);

       int[] XCoords = new int[countCoords];
       int[] YCoords = new int[countCoords];
       
       for (i=0; i < countCoords; i++) {
           XCoords[i] = (int)(( ( (satRange.getLongCoords()[i]) / 180.0 ) * MapMidX ) + MapMidX);
           YCoords[i] = (int)(( MapMidY - (  (satRange.getLatCoords()[i]) / 90.0 ) * MapMidY ) );
           //System.out.println("XRange: " + SatRange.getLongCoords()[i] + "YRange: " + SatRange.getLatCoords()[i] );
           
           //only draws if line is continuous
           if ( i > 0 && Math.abs(XCoords[i] - XCoords[i-1]) < 50 )
                g.drawLine(XCoords[i-1], YCoords[i-1], XCoords[i], YCoords[i] );
       }
       //finishes off circle
       if ( i > 0 && Math.abs(XCoords[0] - XCoords[i-1]) < 50 )
         g.drawLine(XCoords[0], YCoords[0], XCoords[i-1], YCoords[i-1] );
       //g.drawPolygon(XCoords, YCoords, countCoords);
    }
    
    void drawSatInfo(Graphics g, Satellite sat) {
        g.setColor(Color.white);
        g.setFont( new Font("Arial" , Font.BOLD, 14) );
        g.drawString( "Name: " + sat.getName() , 250, MapY + 25);
        g.setFont( new Font("Arial" , Font.PLAIN, 12) );

        int lineSpace = 13;
        g.drawString( "Longitude: " + (int)sat.getLong() , 250, MapY + 41 + 0*lineSpace);
        g.drawString( "Latitude: " + (int)sat.getLat() , 250, MapY + 41 + 1*lineSpace);
        g.drawString( "Altitude: " + sat.getAltitude() , 250, MapY + 41 + 2*lineSpace);
        g.drawString( "Range: " + sat.getRange() , 250, MapY + 41 + 3*lineSpace);           
    }
    
    void drawGrid(Graphics g) {
        double intervalX = MapX / GridX;        double intervalY = MapY / GridY;
        double intervalDegLong = 360 / GridX;   double intervalDegLat = 180 / GridY;
        double degLong = 180;                   double degLat = 90;
        
        //color of grid
        g.setColor(Color.gray);  g.setFont(gridFont);
        int i;
        double xA = 0; double yA = 0;
        int x,y;
        
        //direction N/W/S/E
        char dir;
        
        //draws Horizontal Grid
        for (i = 1; i < GridX; i ++) {
            xA += intervalX;
            x = (int)xA;
            
            //draws degrees
            if (i < (GridX / 2) ) {
                dir = 'E';
                degLong -= intervalDegLong; }
            else if (i == (GridX /2) ) { //draws center degrees
                dir = ' ';
                degLong = 0; }
            else {
                dir = 'W';
                degLong += intervalDegLong; }
                
            g.drawLine( x, 0, x, MapY);
            g.drawString("" + degLong + dir ,x - 12, MapY + 12);
        }
        
        //draws Vertical Grid
        for (i = 1; i < GridY; i ++) {
            yA +=intervalY;
            y = (int)yA;
            
            
            g.drawLine( 0, y, MapX, y);
            
            //draws degrees
            if (i < (GridY / 2) ) {
                dir = 'N';
                degLat -= intervalDegLat; }
            else if (i == (GridY /2) ) { //draws center degrees
                dir = ' ';
                degLat = 0; }
            else {
                dir = 'S';
                degLat += intervalDegLat; }

            g.drawString("" + degLat + dir ,MapX + 10, y + 5);
         }
    }
    
    public boolean mouseDown(Event evt, int x, int y) {
        mouseLong = getLongCoord(x); 
        mouseLat = getLatCoord(y);
        
        int i;
        for (i = 0; i < satsVect.size(); i++) {
            Satellite sat = (Satellite)satsVect.elementAt(i);
            if ( ( Math.abs( sat.getLong() - mouseLong) < 15 )
                && ( Math.abs( sat.getLat() - mouseLat) < 20 ) )
                 satSelected = sat;
        }
        
        repaint();
        return true;
    }
    
    //returns satellite coordinates using pixel form
    private int getXCoord(double coordLong) {
        return (int)(( ( (coordLong) / 180.0 ) * MapMidX ) + MapMidX); }
    private int getYCoord(double coordLat) {
        return (int)(MapMidY - ( (coordLat / 90.0 ) * MapMidY )); }
    
    private double getLongCoord(int x) {
        return (x - MapMidX) / MapMidX * 180; }
    private double getLatCoord(int y) {
        return -(y - MapMidY) / MapMidY * 90; }
        
    public void destroy() { offscreen.dispose(); }
}