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
    public final static double PI = Math.acos(-1);
    public final static double degToRadConv = (PI / 180.0);
    
    //grid for map
    static final int mapWindowX = 700;
    static final int mapWindowY = (int)( mapWindowX / 2.0 );
    //screen Size
    static final int ScreenX = mapWindowX + 50;
    static final int ScreenY = mapWindowY + 300;    
    //midpoint used for calculating coordinates
    int mapMidX = mapWindowX / 2;
    int mapMidY = mapWindowY / 2;
    
    //controls applet speed
    int speedTimeX = 400;
    int framesPerSecond = 40;
    
    int i, frameCounter, fps;
    
    //fonts and colors
    Font nameFont = new Font("Arial" , Font.PLAIN, 11);
    Font gridFont = new Font("Arial" , Font.PLAIN, 14);
    Color backColor = Color.black;
    
    //location of mouse
    double mouseLong, mouseLat;
    
    boolean isZoomed = false; //whether Map is zoomed
    boolean followSat = false; //whether Satellite is followed
    
    Vector satsVect = new Vector();
    Vector placesVect = new Vector();

    Range satsRange;
    Satellite satSelected = new Satellite();
    
    //threads
    Thread drawSprites;
    Thread clock;
    Thread updateContactsThread;
    Thread beamAnimate; 
        double beamFraction = 0; //used to animate beam
        boolean startBeam; //starts animation of beam transfer
        Color beamColor;
        Place beamPlace;
        Satellite beamSat;
    Thread frameCountThread;
    Thread zoomMapThread; //zooms Map
        double gridZoom = 1;
        double mapZoom = 1;
        double zoom = 1;
        double prevZoom = zoom;
        int mapClickX, mapClickY; //position of mouse when zooming
        int zoomTo = 1;
        //used for map zooming
        int mapX = mapWindowX;
        int mapY = mapWindowY;
        int prevMapX = mapX;
        int prevMapY = mapY;
        int offScreenX = 0; int mapOffScreenX = 0;
        int offScreenY = 0; int mapOffScreenY = 0;
        
    //images
    Image offscreenImage;
    Image mapImg,satImg,recImg,recSignalImg;
    Image satRotateImgs[]; //image used for satellite rotation animation
    

    
    Graphics offscreen;
    Date DateNow;
    
    public void init () {
        setSize( ScreenX, ScreenY );
        //creates double buffer
        offscreenImage = createImage(getSize().width, getSize().height);        
        offscreen = offscreenImage.getGraphics();
        
        //mapImg = getImage(getDocumentBase(),"mapHiRes.jpg");
        mapImg = getImage(getDocumentBase(),"Map.jpg");
        satImg = getImage(getDocumentBase(),"SatGeneric.gif");
        recImg = getImage(getDocumentBase(),"Receiver.gif");
        recSignalImg = getImage(getDocumentBase(),"ReceiverSignal.gif");
        //Satellite animation frames
        String[] satRotateImgsString = { "sat0.gif", "sat9.gif", "sat18.gif", "sat27.gif", "sat36.gif", "sat45.gif",
                                         "sat54.gif", "sat63.gif", "sat72.gif", "sat81.gif", "sat90.gif" };
        satRotateImgs = new Image[satRotateImgsString.length];
            for (i = 0; i < satRotateImgsString.length; i++) {
                satRotateImgs[i] = getImage( getDocumentBase(), satRotateImgsString[i]); }
        
        System.out.println("\nCodeBase: " + getCodeBase() );
        
        //Satellites are created here
        satsVect.addElement( new Satellite(0, 0, 2000, 3000, 15000, 320, "Tom") );
        satsVect.addElement( new Satellite(0, 0, 3000, 4000, 12000, 175, "Satellite2") );
        satsVect.addElement( new Satellite(0, 20, 4000, 5000, 7000, 64, "Satellite3") );
        satsVect.addElement( new Satellite(0, 0, 7000, 6000, 5000, 143, "Satellite4") );
        
        placesVect.addElement( new Place(45.68,-74.03, "Montreal") );
        placesVect.addElement( new Place(20, -90, "Place1") );
        placesVect.addElement( new Place(40,100, "Place2") );
        placesVect.addElement( new Place(-28,123, "Place3") );
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
            if (frameCountThread == null) {
                frameCountThread = new Thread(this);
                frameCountThread.start();
            }
            if (updateContactsThread == null) {
                updateContactsThread = new Thread(this);
                updateContactsThread.start();
            }
    }
    
    public void stop() { drawSprites = null; clock = null; }
    
    public void run() {
    Thread thisThread = Thread.currentThread();
        while (drawSprites ==  thisThread) {
            if (followSat)
                //moves screen to absolute coordinates of image
                moveScreen( getXCoord(satSelected.getLong()) + Math.abs(offScreenX), 
                            getYCoord(satSelected.getLat()) + Math.abs(offScreenY) );
            
            int i;
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
        
        while (beamAnimate == thisThread) {
            int frames = 8;
            startBeam = false;
   
            if (beamFraction < 1) {
                beamColor = Color.white;
                for (beamFraction = 0; beamFraction <= 1; beamFraction = beamFraction + (1.0 / frames) ) {
                    try {
                        Thread.sleep( 400 / frames );
                    } catch (InterruptedException e) {}
                }
                beamFraction = 1; startBeam = true;
                repaint();
            }
            else {
                if (beamColor == Color.white)
                    beamColor = new Color( 220, 220, 220);
                else beamColor = Color.white;
                
                try {
                        Thread.sleep( 200 );
                } catch (InterruptedException e) {}
                repaint();
            }
         }
         
         while (frameCountThread == thisThread) {
            frameCounter = 0;
            try { Thread.sleep( 1000 ); } catch (InterruptedException e) {}
            fps = frameCounter;
        }
    
        while (updateContactsThread == thisThread) {
            updateContacts(satsVect, placesVect);
            try { Thread.sleep( 100 ); } catch (InterruptedException e) {}
            repaint();
        }

        if (zoomMapThread == thisThread) { //animates zooming7
            int i;
            if (zoom <= zoomTo) i = 1; //determines direction of zoom
            else i = -1;
            int frames = 20;
            
            prevMapX = mapX; prevMapY = mapY;
            prevZoom = zoom;
            
            while ( Math.abs(zoomTo - gridZoom) > 0.3) {
                gridZoom += (1.0 / 4)*i;
                try {
                    Thread.sleep( 100 );
                } catch (InterruptedException e) {}
            } gridZoom = zoomTo;
            
            if (  zoom <= zoomTo ) {
                //int i = 0;
                while ( i * (zoomTo - mapZoom) > 0) {
                    mapZoom += 1.0 / frames *i ;
                    if (i * (zoomTo - mapZoom) < 0)
                        mapZoom = zoomTo; 

                    try {
                        Thread.sleep( 1000 / frames );
                    } catch (InterruptedException e) {}
                    mapOffScreenX = - (int)( (mapZoom/prevZoom) * (mapClickX - prevMapX / mapZoom / 2) ); //mapZooms to center of mouse click
                    // makes sure map does not go off edge
                        if (mapOffScreenX > 0) mapOffScreenX = 0; else if (mapOffScreenX < mapWindowX - mapWindowX * mapZoom) mapOffScreenX = (int)( mapWindowX - mapWindowX * mapZoom );
                    mapOffScreenY = - (int)( (mapZoom/prevZoom) * (mapClickY - prevMapY / mapZoom / 2) );
                        if (mapOffScreenY > 0) mapOffScreenY = 0; else if (mapOffScreenY < mapWindowY - mapWindowY * mapZoom) mapOffScreenY = (int)( mapWindowY - mapWindowY * mapZoom  );
                }  //rezooms map for zooming out

                while ( i * (zoomTo - zoom) > 0) {
                    zoom += 1.0 / frames * i;
                    try {
                        Thread.sleep( 1000 / frames );
                    } catch (InterruptedException e) {}
                    calcZoom(zoom, mapClickX, mapClickY); //zooms Image consequtively for smooth sequence
                }
            }
            zoom = zoomTo; calcZoom(zoom, mapClickX, mapClickY);
            mapZoom = zoomTo;
            
            mapOffScreenX = offScreenX;
            mapOffScreenY = offScreenY;
            
            
            isZoomed = true; repaint(); 
            //stops the thread once zoom is complete
            zoomMapThread = null;
        }
    }
    
    public void update(Graphics g) { paint(g); }
    
    public void paint(Graphics g) {
        //creates background
        offscreen.setColor(backColor);
        offscreen.fillRect(0, 0, getSize().width, getSize().height);
        //draws Map
        offscreen.drawImage(mapImg, mapOffScreenX, mapOffScreenY, (int)(mapWindowX * mapZoom), (int)(mapWindowY * mapZoom), this);
        offscreen.fillRect(mapWindowX, 0, ScreenX, ScreenY);
        offscreen.fillRect(0, mapWindowY, ScreenX, ScreenY);
        //draws objects
        drawGrid(offscreen);
        drawSprites(offscreen, placesVect);
        drawSprites(offscreen, satsVect);
        
        //prevents from drawing outside window
        offscreen.setColor(backColor);
        offscreen.fillRect(mapWindowX + 30, 0, ScreenX, ScreenY);
        offscreen.fillRect(0, mapWindowY + 13, ScreenX, ScreenY);
        
        //information about screen
        drawSatInfo(offscreen, satSelected); //draws information about satellite
        offscreen.setColor(Color.orange);
        offscreen.drawString(DateNow.toString() , 20, mapWindowY + 25);
        offscreen.setColor(Color.white);
        offscreen.drawString("ZOOM: " + zoom, 20, mapWindowY + 40);
        offscreen.drawString("Latitude: " + (int)mouseLat + " Longitude: " + (int)mouseLong , 400, mapWindowY + 25);
        offscreen.drawString("Angle: " + getAngle(satSelected, (Sprite)placesVect.elementAt(0) ) , 400, mapWindowY + 40);
        offscreen.drawString("FPS: " + fps , 400, mapWindowY + 55);
        offscreen.setColor(Color.red);
        offscreen.drawString("double click to zoom", 400, mapWindowY + 70);
        offscreen.drawString("Shift double click to zoom out", 400, mapWindowY + 85);
        offscreen.drawString("Click to select sat, press enter to follow", 400, mapWindowY + 100);
        
        //g.drawImage(gridImage, 0, 0 ,this);
        g.drawImage(offscreenImage, 0, 0 ,this);
        frameCounter++;
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
        int placeX = getXCoord(spriteDraw.getLong());
        int placeY = getYCoord(spriteDraw.getLat());
        
        if ( spriteDraw.hasContact ) {
            if ( spriteDraw.meetsWith(satSelected) ) {
                if (beamAnimate == null) {
                    beamAnimate = new Thread(this);
                    beamAnimate.start();
                    beamPlace = spriteDraw;
                    beamSat = satSelected;
                }

                //animated Beam which triangulates to angle of satellite and location
                double beamAngle = getRad( 450 - (int)( getAngle(beamSat,beamPlace ) ) );
                int satX = getXCoord(satSelected.getLong());    int x = (int)(Math.sin(beamAngle) * 5.0);
                int satY = getYCoord(satSelected.getLat());     int y = (int)(Math.cos(beamAngle) * 5.0);
                
                int[] uplinkX = { placeX, (int)( placeX + beamFraction * (  satX - x - placeX )), 
                                  (int)( placeX + beamFraction * ( satX + x -placeX )) };
                int[] uplinkY = { placeY, (int) (placeY + beamFraction * ( satY - y - placeY ) ), 
                                  (int)( placeY + beamFraction * ( satY + y - placeY) )};
                
                g.setColor(beamColor);
                g.fillPolygon( uplinkX, uplinkY, 3);
            }
            
            g.setColor(Color.white); g.fillOval(placeX - 5, placeY -5, 2, 2);
            g.drawImage(recSignalImg, placeX - 7, placeY -30, this);
            
            g.setColor(Color.white); g.setFont( new Font("Arial" , Font.BOLD, 11) );
            g.drawString( "Contact With:" , placeX - 15, placeY + 32);
            
            int i; for (i = 0; i < spriteDraw.getContacts().length; i++) {
                g.drawString( spriteDraw.getContacts()[i] , placeX - 15, placeY + 43 + i*10);
            }           
        }
        else {
            //resets beam animation if satellite leaves range of location or another satellite is selected
            if (beamAnimate != null && (beamPlace == spriteDraw || beamSat != satSelected) ) {
                 beamAnimate = null;
                 beamFraction = 0;
            }
            g.setColor(Color.white); g.fillOval(placeX - 3, placeY -3, 2, 2);
            g.drawImage(recImg, placeX - 5, placeY -28, this);
        }
        
        if (spriteDraw.showName) {
            g.setColor(Color.white); g.setFont(nameFont);
            g.drawString( spriteDraw.getName(), placeX - 15, placeY + 18);
        }
    }
    
     public void drawSatellite(Graphics g, Satellite spriteDraw) {
        //get satellite coordinates in pixel system
        int satX = getXCoord(spriteDraw.getLong());
        int satY = getYCoord(spriteDraw.getLat());
        
        //draws range circle
        if ( spriteDraw.showRange )
            drawRange( g, spriteDraw );

        g.setColor(Color.white); g.fillOval(satX -2, satY - 2, 4, 4); //used before images are loaded        
        
        //draws Satellite rotation animation
        if (spriteDraw.hasContact) {
            int i;
            int angle = getAngle( getXCoord(spriteDraw.getLong()), getYCoord(spriteDraw.getLat()),
                                  getXCoord(spriteDraw.contactLong) , getYCoord(spriteDraw.contactLat) );

            int imageToUse = (int)((angle % 90.0) / 9.0 );
            
            int imgX, imgY;
            if (angle <= 90) {
                imgX = satRotateImgs[imageToUse].getWidth(this);
                imgY = satRotateImgs[imageToUse].getHeight(this); }
            else if (angle <= 180) {
                imageToUse = satRotateImgs.length - 1 - imageToUse;
                imgX = satRotateImgs[imageToUse].getWidth(this);
                imgY = -satRotateImgs[imageToUse].getHeight(this); }
            else if (angle <= 270) {
                imgX = -satRotateImgs[imageToUse].getWidth(this);
                imgY = -satRotateImgs[imageToUse].getHeight(this); }
            else {
                imageToUse = satRotateImgs.length - 1 - imageToUse;
                imgX = -satRotateImgs[imageToUse].getWidth(this);
                imgY = satRotateImgs[imageToUse].getHeight(this); }
                
            g.drawImage(satRotateImgs[imageToUse], satX - imgX/2, satY - imgY/2, imgX, imgY, this);
        }
        else
            g.drawImage(satImg, satX - (satImg.getWidth(this)/2), satY - (satImg.getHeight(this)/2), this);
        
        //int size = satImg.getWidth(this);
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
       else if (sat.hasContact)
           g.setColor(Color.white);
       else
           g.setColor( new Color(180,180,180) ); //color is satellite has no contact
       
       Range satRange = sat.getRangeObj();
       
       int countCoords = satRange.getLongCoords().length;
       
       //System.out.println("\nCOUNT CORDS: " + countCoords);

       int[] XCoords = new int[countCoords];
       int[] YCoords = new int[countCoords];
       
       for (i=0; i < countCoords; i++) {
           XCoords[i] = (int)(getXCoord(satRange.getLongCoords()[i]));
           YCoords[i] = (int)(getYCoord(satRange.getLatCoords()[i]));
           //System.out.println("XRange: " + SatRange.getLongCoords()[i] + "YRange: " + SatRange.getLatCoords()[i] );
           
           //only draws if line is continuous
       if ( i > 0 && Math.abs(XCoords[i] - XCoords[i-1]) < 200 )
                g.drawLine(XCoords[i-1], YCoords[i-1], XCoords[i], YCoords[i] );
       }
       //finishes off circle
       if ( i > 0 && Math.abs(XCoords[0] - XCoords[i-1]) < 200 )
         g.drawLine(XCoords[0], YCoords[0], XCoords[i-1], YCoords[i-1] );
       //g.drawPolygon(XCoords, YCoords, countCoords);
    }
    
    void drawSatInfo(Graphics g, Satellite sat) {
        g.setColor(Color.white);
        g.setFont( new Font("Arial" , Font.BOLD, 14) );
        g.drawString( "Name: " + sat.getName() , 250, mapWindowY + 25);
        g.setFont( new Font("Arial" , Font.PLAIN, 12) );

        int lineSpace = 13;
        g.drawString( "Longitude: " + (int)sat.getLong() , 250, mapWindowY + 41 + 0*lineSpace);
        g.drawString( "Latitude: " + (int)sat.getLat() , 250, mapWindowY + 41 + 1*lineSpace);
        g.drawString( "Altitude: " + sat.getAltitude() , 250, mapWindowY + 41 + 2*lineSpace);
        g.drawString( "Range: " + sat.getRange() , 250, mapWindowY + 41 + 3*lineSpace);
        if (followSat) {
            g.setColor(Color.red); g.drawString( "FOLLOW MODE ON" , 250, mapWindowY + 41 + 4*lineSpace); }
        else g.drawString( "FOLLOW MODE OFF" , 250, mapWindowY + 41 + 4*lineSpace);
    }
    
    void drawGrid(Graphics g) {
        int GridX = (int)( gridZoom * 6 );      int GridY = GridX / 2;
        double intervalDegLong = 180 / GridX;   double intervalDegLat = 90 / GridY;
        double degLat = 0;                      double degLong = 0;
        int xE, xW, yN, yS;
        
        //color of grid
        g.setColor(Color.gray);  g.setFont(gridFont);
        
        //draws Horizontal Grid
        int i;
        for (i = 0; i < GridX; i++) {
            xE = getXCoord(degLong); xW = getXCoord(-degLong);
            
            if (xE <= mapWindowX) {//stop draw is it goes outside map window
                g.drawLine( xE, 0, xE, mapWindowY);
                g.drawString("" + degLong + "E" ,xE - 12, mapWindowY + 12);
            }
            
            if (i > 0) { //makes sure E and W does not overlap at 0
                g.drawLine( xW, 0, xW, mapWindowY);
                g.drawString("" + degLong + "W" , xW - 12, mapWindowY + 12);
            }
            degLong += intervalDegLong;
        }
        
        //draws Vertical Grid
        for (i = 0; i < GridY; i ++) {
            yN = getYCoord(degLat); yS = getYCoord(-degLat);
            
            g.drawLine( 0, yN, mapWindowX, yN);
            g.drawString("" + degLat + "N" , mapWindowX + 10, yN + 5);
            
            if (i > 0 && yS <= mapWindowY) { //makes sure N and S does not overlap at 0
                g.drawLine( 0, yS, mapWindowX, yS);
                g.drawString("" + degLat + "S" , mapWindowX + 10, yS + 5);
            }
            degLat += intervalDegLat;
         }
    }
    
    public boolean mouseMove(Event evt, int x, int y) {
        mouseLong = getLongCoord(x); 
        mouseLat = getLatCoord(y);
        return true;
    }
        
    public boolean mouseDown(Event evt, int x, int y) {
        boolean foundSat = false;
        boolean zoomPositive;
        
        if (evt.shiftDown() || evt.controlDown()) //handles unzooming
            zoomPositive = false;
        else zoomPositive = true;
        
        switch (evt.clickCount) {
            case 1: {
                int i;
                for (i = 0; i < satsVect.size(); i++) {
                    Satellite sat = (Satellite)satsVect.elementAt(i);
                    if ( ( Math.abs( getXCoord(sat.getLong()) - x) < 15 )
                        && ( Math.abs( getYCoord(sat.getLat()) - y) < 20 ) ) {
                         satSelected = sat;
                         foundSat = true;
                    }
                    if (foundSat) break; //stop searching if found
                }
                break;
            }
            case 2: {
                //begins zooming algorithm
                if ( ( (zoomPositive && zoomTo <= 3) || (zoomTo > 1 && !zoomPositive ) ) && !followSat && zoomMapThread == null) {
                    if (zoomPositive) //increases zoom factor
                        zoomTo += 1;
                    else zoomTo -= 1;
                    //starts zoom map animation
                    if (zoomMapThread == null) { zoomMapThread = new Thread(this); zoomMapThread.start(); }
                    mapClickX = x + Math.abs(offScreenX); mapClickY = y + Math.abs(offScreenY); //coordinates for zoom to focus on
                }
                else if (followSat)
                    System.out.println("You need to exit follow mode by pressing enter to zoom");
            }
        }
        repaint();
        return true;
    }
    
    public boolean keyDown(Event evt, int key) {
        switch (key) {
            case Event.ENTER:{
                if (followSat == false && zoomMapThread == null) //does not follow while in zoom animation
                    followSat = true;
                else followSat = false;
            }
        } return true;
    }

                                      //location of mouse clicks
    public void calcZoom(double zoom, int mapClickX, int mapClickY) { //algorithm for zooming map
        mapX = (int)(zoom * mapWindowX);
        mapY = (int)(zoom * mapWindowY);
       
        offScreenX = - (int)( (zoom/prevZoom) * (mapClickX - prevMapX / zoom / 2) ); //zooms to center of mouse click
            // makes sure map does not go off edge
            if (offScreenX > 0) offScreenX = 0; else if (offScreenX < mapWindowX - mapX) offScreenX = mapWindowX - mapX;
        offScreenY = - (int)( (zoom/prevZoom) * (mapClickY - prevMapY / zoom / 2) );
            if (offScreenY > 0) offScreenY = 0; else if (offScreenY < mapWindowY - mapY) offScreenY = mapWindowY - mapY;
        mapMidX =  mapX / 2; mapMidY =  mapY / 2;
    }
    
    public void moveScreen(int centerX, int centerY) { //algorithm for zooming map
        offScreenX = - (int)( centerX - (mapWindowX / 2) ); //zooms to center of mouse click
            // makes sure map does not go off edge
            if (offScreenX > 0) offScreenX = 0; else if (offScreenX < mapWindowX - mapX) offScreenX = mapWindowX - mapX;
        offScreenY = - (int)( centerY - (mapWindowY / 2) );
            if (offScreenY > 0) offScreenY = 0; else if (offScreenY < mapWindowY - mapY) offScreenY = mapWindowY - mapY;
        mapOffScreenX = offScreenX; mapOffScreenY = offScreenY;
    }
    
    public int getAngle(Sprite s1, Sprite s2) {
        return getAngle( getXCoord(s1.getLong()), getYCoord(s1.getLat()), getXCoord(s2.getLong()), getYCoord(s2.getLat()) ); }
    
    public int getAngle(double x1, double y1, double x2, double y2) { //gets angle between two sprites, used for animation
        double deltaX =  x1 - x2;
        double deltaY = -( y1 - y2 );
        int angle = 0;

        int angleAbs = (int)( Math.abs(Math.atan( deltaY / deltaX) / Sprite.degToRadConv) );

        if ( deltaX >= 0 && deltaY >= 0)
            angle = 90 - angleAbs;
        else if ( deltaX >= 0 && deltaY < 0)
            angle = angleAbs + 90;
        else if ( deltaX < 0 && deltaY < 0)
            angle = 270 - angleAbs;
        else if ( deltaX < 0 && deltaY >= 0)
            angle = angleAbs + 270;
        
        return angle;
    }
    
    //returns satellite coordinates using pixel form
    private int getXCoord(double coordLong) {
        return (int)(( ( (coordLong) / 180.0 ) * mapMidX ) + mapMidX + offScreenX ); }
    private int getYCoord(double coordLat) {
        return (int)(mapMidY - ( (coordLat / 90.0 ) * mapMidY - offScreenY)); }
    
    private double getLongCoord(int x) {
        return (x - mapMidX - offScreenX) / (double)mapMidX * 180; }
    private double getLatCoord(int y) {
        return -(y - mapMidY - offScreenY) / (double)mapMidY * 90; }
        
    //converts degrees to radians
    private double getRad(double deg) { return ( degToRadConv * deg ); }
    
    
    public void updateContacts(Vector sats, Vector places) {
        int i;        
        for (i = 0; i < sats.size(); i++) { //resets all contacts to null
            Satellite sat = (Satellite)sats.elementAt(i);
            sat.hasContact = false; }            
        for (i = places.size() - 1; i >= 0; i--) { //points to the city decalared first if more than 1 are in range
            Place place = (Place)places.elementAt(i);
            place.findContacts(sats); }
    }
    
    public void destroy() { offscreen.dispose(); }
}