/*
 * SatMapper Test Version 0.9
 *
 * Created on February 12, 2002, 2:29 PM
 */

/**
 * @author  Tom Wu
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

public class SatMapper extends java.applet.Applet implements Runnable {
    final static double PI = Math.acos(-1);
    final static double degToRadConv = (PI / 180.0);
    
    //grid for map
    static final int mapWindowX = 700;
    static final int mapWindowY = (int)( mapWindowX / 2.0 );
    //total screen Size
    static final int ScreenX = mapWindowX + 30;
    static final int ScreenY = mapWindowY + 15;
    
    //midpoint used for calculating coordinates
    int mapMidX = mapWindowX / 2;
    int mapMidY = mapWindowY / 2;
    
    //controls applet speed
    int speedTimeX = 400;
    int framesPerSecond = 40;
    int frameCounter, fps;
    
    //fonts and colors
    Font nameFont = new Font("Arial" , Font.PLAIN, 11);
    Font gridFont = new Font("Arial" , Font.PLAIN, 14);
    AlphaComposite alphaOpaque = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
    Color backColor = Color.black;
    Color shuttleFlameColor = Color.orange;
    
    //location of mouse
    double mouseLong, mouseLat; int mouseX, mouseY;

    boolean followSat = false; //whether SatSprite is followed
    boolean started = false; //whether applet has started, used to load night background
    boolean removeClouds = false; //whether clouds are removed at all times
    boolean langEng; //if langugage is english
    boolean langSelected = false; //if langugage is english
    
    Vector satsVect = new Vector(); //vector of all Satellites
    Vector placesVect = new Vector(); //vector of all places
    SatSprite satSelected; //currently selected SatSprite
    SatSprite sat83;
    SatSprite satFix = new SatSprite();
    
    //threads
    Thread drawSprites;
    Thread clock;
    Thread updateContactsThread;
    Thread beamAnimate; 
        double beamFraction = 0; //used to animate beam
        boolean startBeam; //starts animation of beam transfer
        Color beamColor;
        Place beamPlace;
        SatSprite beamSat;
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
    Thread nightTransitionThread;
        double nightTransition = 0;
    Thread satInfoAnimateThread;
        double satInfoFractionX;
        double satInfoFractionY;
    Thread animationThread;
        double anFraction = 0;
        
    //used for astronaut and canadaarm animation on shuttle
    int flyX = 0;
    int flyY = 0;
    double asFraction = 0;
    double asFraction2 = 0;
    int armX = 0;
    int armY = 0;
    double armFraction = 0;
    int armLength = 80;
    int armPartLength = armLength / 2;
    int distanceArm = 0;
        
    //images
    Image offscreenImage;
    Image mapImg, mapNightImg, mapCloudsImg, 
          satImg,recImg,recSignalImg, shuttleImg, anImg,
          flameImg1, flameImg2, flameDrawImg;
    
    Graphics offscreen;
    Graphics2D g2d;
    
    Date DateNow;
    
    public void init () {
        setSize( ScreenX, ScreenY );
        //creates double buffer
        offscreenImage = createImage(getSize().width, getSize().height);
        offscreen = offscreenImage.getGraphics();
        g2d = (Graphics2D)offscreen;
        
        //images for maps
        mapImg = getImage(getDocumentBase(),"");
        mapNightImg = getImage(getDocumentBase(),"");
        mapCloudsImg = getImage(getDocumentBase(),"");
        
        mapImg = getImage(getDocumentBase(),"mapHiRes.jpg");
        mapNightImg = getImage(getDocumentBase(),"mapNight.jpg");
        mapCloudsImg = getImage(getDocumentBase(),"mapClouds.jpg");
        
        //images for sprites
        satImg = getImage(getDocumentBase(),"radarSat.gif");
        shuttleImg = getImage(getDocumentBase(),"Shuttle.gif");
        recImg = getImage(getDocumentBase(),"Receiver.gif");
        recSignalImg = getImage(getDocumentBase(),"ReceiverSignal.gif");
        anImg = getImage(getDocumentBase(),"Astronaut.gif"); //image of astronaut
        flameImg1 = getImage(getDocumentBase(),"Flame1.gif"); //image of astronaut
        flameImg2 = getImage(getDocumentBase(),"Flame2.gif"); //image of astronaut
        
        //satellites are created here
        satsVect.addElement( new SatSprite(0, 0, 2000, 3000, 15000, 320, "Tom") );
        satsVect.addElement( new SatSprite(0, 0, 3000, 4000, 12000, 175, "SatSprite2") );
        satsVect.addElement( new SatSprite(0, 20, 4000, 5000, 7000, 64, "SatSprite3") );
        satsVect.addElement( new SatSprite(0, 0, 7000, 6000, 5000, 143, "SatSprite4") );
        
        //Places are created here
        //              CANADIAN CAPITALS AND MONTREAL
        placesVect.addElement( new Place(45.47, -73.75, "Montreal", true) );
        placesVect.addElement( new Place(45.32, -75.67, "Ottawa") );
        placesVect.addElement( new Place(43.68, -79.63, "Toronto") );
        placesVect.addElement( new Place(51.10, -114.02, "Calgary") );
        placesVect.addElement( new Place(48.42, -123.32, "Victoria") );
        placesVect.addElement( new Place(49.9, -97.23, "Winnipeg") );
        placesVect.addElement( new Place(45.87, -66.53, "Fredericton") );
        placesVect.addElement( new Place(47.62, -52.75, "St. John's") );
        placesVect.addElement( new Place(62.47, -114.45, "YellowKnife") );              
        placesVect.addElement( new Place(44.65, -63.57, "Halifax") );        
        placesVect.addElement( new Place(46.28, -63.13, "Charlottetown") );
        placesVect.addElement( new Place(46.8, -71.38, "Quebec") );        
        placesVect.addElement( new Place(50.52, -104.67, "Regina") );
        placesVect.addElement( new Place(60.72, -135.07, "Whitehorse") );
        placesVect.addElement( new Place(74.72, -94.98, "Resolute") ); //not a capital but canada's northern most city
        
        //other places
        placesVect.addElement( new Place(20, -90, "Place1", true) );
        placesVect.addElement( new Place(40,100, "Place2", true) );
        placesVect.addElement( new Place(-28,123, "Place3", true) );
    }
    
    public void start() { //starts all essential threads
            if (drawSprites == null) { drawSprites = new Thread(this); drawSprites.start(); }
            if (clock == null) { clock = new Thread(this); clock.start(); }
            if (frameCountThread == null) { frameCountThread = new Thread(this); frameCountThread.start(); }
            if (updateContactsThread == null) { updateContactsThread = new Thread(this); updateContactsThread.start(); }
    }
    
    public void stop() { drawSprites = null; clock = null; frameCountThread = null; 
                         updateContactsThread = null; zoomMapThread = null; nightTransitionThread = null;
                         satInfoAnimateThread = null; animationThread = null;}
    
    public void run() {
    Thread thisThread = Thread.currentThread();
        while (drawSprites ==  thisThread) {
            if (followSat && zoomMapThread == null && satSelected != null)
                //moves screen to absolute coordinates of image
                moveScreen( getXCoord(satSelected.getLong()) + Math.abs(offScreenX), 
                            getYCoord(satSelected.getLat()) + Math.abs(offScreenY) );
            
            //updates SatSprite Position for minute
            for (int i = 0; i < satsVect.size(); i++) {
                SatSprite sat = (SatSprite)satsVect.elementAt(i);
                sat.moveScalar(speedTimeX/framesPerSecond);  }
            
            try { Thread.sleep(1000/framesPerSecond); } catch (InterruptedException e) {}
            
            repaint();
            //stop();
        }
   
        while (clock ==  thisThread) {
            DateNow = new Date();
            try { Thread.sleep(10000); } catch (InterruptedException e) {}
        }
        
        while (beamAnimate == thisThread) {
            int frames = 8;
            startBeam = false;
   
            if (beamFraction < 1) {
                beamColor = Color.white;
                for (beamFraction = 0; beamFraction <= 1; beamFraction = beamFraction + (1.0 / frames) ) {
                    try { Thread.sleep( 400 / frames ); } catch (InterruptedException e) {} //pauses
                }
                beamFraction = 1; startBeam = true;
                repaint();
            }
            else {
                if (beamColor == Color.white)
                    beamColor = new Color( 220, 220, 220);
                else beamColor = Color.white;
                try { Thread.sleep( 200 ); } catch (InterruptedException e) {}
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

        if (zoomMapThread == thisThread) { //animates zooming
            int i;
            if (zoom <= zoomTo) i = 1; //determines direction of zoom
            else i = -1;
            int frames = 20;
            
            prevMapX = mapX; prevMapY = mapY;
            prevZoom = zoom;
            
            while ( Math.abs(zoomTo - gridZoom) > 0.3) {
                gridZoom += (1.0 / 4)*i;
                try { Thread.sleep( 100 ); } catch (InterruptedException e) {}
                repaint();
            } gridZoom = zoomTo;
            
            if (  zoom <= zoomTo ) {
                //int i = 0;
                while ( i * (zoomTo - mapZoom) > 0) {
                    mapZoom += 1.0 / frames *i ;
                    if (i * (zoomTo - mapZoom) < 0)
                        mapZoom = zoomTo; 

                    try { Thread.sleep( 1000 / frames ); } catch (InterruptedException e) {}
                    mapOffScreenX = - (int)( (mapZoom/prevZoom) * (mapClickX - prevMapX / mapZoom / 2) ); //mapZooms to center of mouse click
                    // makes sure map does not go off edge
                        if (mapOffScreenX > 0) mapOffScreenX = 0; else if (mapOffScreenX < mapWindowX - mapWindowX * mapZoom) mapOffScreenX = (int)( mapWindowX - mapWindowX * mapZoom );
                    mapOffScreenY = - (int)( (mapZoom/prevZoom) * (mapClickY - prevMapY / mapZoom / 2) );
                        if (mapOffScreenY > 0) mapOffScreenY = 0; else if (mapOffScreenY < mapWindowY - mapWindowY * mapZoom) mapOffScreenY = (int)( mapWindowY - mapWindowY * mapZoom  );
                    repaint();
                }  

                while ( i * (zoomTo - zoom) > 0) {
                    zoom += 1.0 / frames * i;
                    try { Thread.sleep( 1000 / frames ); } catch (InterruptedException e) {}
                    calcZoom(zoom, mapClickX, mapClickY); //zooms Image consequtively for smooth sequence
                    repaint();
                }
            }
            //rezooms map for zooming out
            zoom = zoomTo; calcZoom(zoom, mapClickX, mapClickY);
            mapZoom = zoomTo;
            
            mapOffScreenX = offScreenX;
            mapOffScreenY = offScreenY;
            
            repaint(); 
            //stops the thread once zoom is complete
            zoomMapThread = null;
        }
        
        if (nightTransitionThread == thisThread) {
            int i; if (nightTransition >= 1) i = 1; //determines direction of zoom
                   else i = -1;
            
            if (nightTransition == 1) {
                while (nightTransition > 0) {
                    nightTransition -= 0.1;
                    try { Thread.sleep( 300 ); } catch (InterruptedException e) {}
                    repaint();
                } nightTransition = 0;
            }
            else if (nightTransition == 0) {
                while (nightTransition < 1) {
                    nightTransition += 0.1;
                    try { Thread.sleep( 300 ); } catch (InterruptedException e) {}
                    repaint();
                } nightTransition = 1;
            }
            nightTransitionThread = null;
        }
    
        if (satInfoAnimateThread == thisThread) {
            while (satInfoFractionX < 1) {
                satInfoFractionX += 0.05;
                try { Thread.sleep( 50 ); } catch (InterruptedException e) {}
            } satInfoFractionX = 1;
            while (satInfoFractionY < 1) {
                satInfoFractionY += 0.05;
                try { Thread.sleep( 50 ); } catch (InterruptedException e) {}
            } satInfoFractionY = 1;
            satInfoAnimateThread = null;
        }
    
        if (animationThread == thisThread) {
            anFraction = 0;
            while (anFraction < 1) {
                anFraction += 0.05;
                try { Thread.sleep( 50 ); } catch (InterruptedException e) {}
            } anFraction = 1;
            animationThread = null;
        }
    }
    
    public void update(Graphics g) { paint(g); }
    
    public void paint(Graphics g) {
        if (zoom == 1) followSat = false;
        
        //creates background
        offscreen.setColor(backColor);
        offscreen.fillRect(0, 0, getSize().width, getSize().height);
        //draws Map
        drawMap(offscreen);     
        
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
        drawSatInfo(offscreen, satSelected); //draws information about SatSprite
        
        //draws date
        offscreen.setColor(Color.orange); offscreen.setFont( gridFont );  
        offscreen.drawString(DateNow.toString() , 20, mapWindowY + 25); //date
        offscreen.setColor(Color.white);
        
        offscreen.drawString("FPS: " + fps , 20, mapWindowY + 40);
        
        offscreen.setColor(Color.white); 
        offscreen.setFont( new Font("Arial", Font.BOLD, 10) );        
        if (mouseX <= mapWindowX && mouseY <=mapWindowY)  //location longitude latitude of mouse
            offscreen.drawString("Lat:" + (int)mouseLat + " Long:" + (int)mouseLong , mouseX -15, mouseY + 40); 
        
        
        if (!langSelected) selectLang(offscreen);
        
        //g.drawImage(gridImage, 0, 0 ,this);
        g.drawImage(offscreenImage, 0, 0 ,this); frameCounter++;
    }
    
    private void selectLang(Graphics g) {
        if (anFraction == 1) langSelected = true;
        int screenMoveX = (int)(anFraction * ScreenX); //for animation
        
        calcZoom(anFraction, 0, 0);
        g.setColor(backColor);
        g.fillRect(screenMoveX, 0, getSize().width, getSize().height);
        
        g.setColor( new Color( 0, 0, 175));
        g.fillRoundRect(screenMoveX, 0, ScreenX, ScreenY, 50,50);        
        g.setColor( new Color(235, 235, 235));
        g.fillRoundRect(5 + screenMoveX, 5, ScreenX -10, ScreenY-10, 50,50); 
        
        g.setColor(Color.gray);
        g.fillRect(ScreenX / 2 - 1 + screenMoveX, 0, 2, ScreenY); 
        
        g.setColor(Color.darkGray);
        g.setFont( new Font("Arial", Font.BOLD, 25) );
        g.drawString("FRANCAIS" , 110 + screenMoveX, ScreenY / 2);
        g.drawString("ENGLISH" , ScreenX / 2 + 110+ screenMoveX, ScreenY / 2);
    }
    
    private void drawMap(Graphics g) {
        AlphaComposite myAlpha;
        
        if ( (mapZoom < 2 || mapZoom > 2) || removeClouds) {
            if (nightTransition < 1)
                g.drawImage(mapImg, mapOffScreenX, mapOffScreenY, (int)(mapWindowX * mapZoom), (int)(mapWindowY * mapZoom), this);

            //testing for nightime transparency
            if (nightTransition > 0 && nightTransition < 1) {
                myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)nightTransition);
                g2d.setComposite(myAlpha);
            }
            
            if (nightTransition > 0) //loads image into memory first time applet is started
                g.drawImage(mapNightImg,mapOffScreenX, mapOffScreenY, (int)(mapWindowX * mapZoom), (int)(mapWindowY * mapZoom),this);
        }
        
        if ( (mapZoom >= 2 && mapZoom < 4) && !removeClouds) {
            if (mapZoom > 2) { //creates map zooming effect
                myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)( 1 - ((mapZoom - 2) / 2 )) );
                g2d.setComposite(myAlpha);
            }
            g.drawImage(mapCloudsImg, mapOffScreenX, mapOffScreenY, (int)(mapWindowX * mapZoom), (int)(mapWindowY * mapZoom), this);
        }
        
        myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
        g2d.setComposite(myAlpha);
    }
  
    //draws all Sprites in Vector
    private void drawSprites(Graphics g, Vector satsVectDraw) {
        for (int i = 0; i < satsVectDraw.size(); i++) {
            if (satsVectDraw.elementAt(i) instanceof SatSprite)
                drawSatSprite( g, (SatSprite)satsVectDraw.elementAt(i) );
            else if (satsVectDraw.elementAt(i) instanceof Place)
                drawPlace( g, (Place)satsVectDraw.elementAt(i) );
        }
    }
    
    private void drawPlace(Graphics g, Place spriteDraw) {
        g.setColor(Color.white);
        
        //get SatSprite coordinates in pixel system
        int placeX = getXCoord(spriteDraw.getLong());
        int placeY = getYCoord(spriteDraw.getLat());
        
        Image receiverImage; //image to use for receiver
        
        if (zoom <= 2 && !spriteDraw.important) {
            g.setColor(Color.red);g.fillOval(placeX - 2, placeY - 2, 4, 4); }
        else {
            if ( spriteDraw.hasContact && satSelected != null) {
                if ( spriteDraw.meetsWith(satSelected) ) {
                    if (beamAnimate == null) {
                        beamAnimate = new Thread(this);
                        beamAnimate.start(); }
                    beamPlace = spriteDraw;
                    beamSat = satSelected;
                    
                    //animated Beam which triangulates to angle of SatSprite and location
                    double beamAngle = getRad( 450 - (int)( getAngle(satSelected,spriteDraw ) ) );
                    int satX = getXCoord(satSelected.getLong());    int x = (int)(Math.sin(beamAngle) * 5.0);
                    int satY = getYCoord(satSelected.getLat());     int y = (int)(Math.cos(beamAngle) * 5.0);
                    
                    int[] uplinkX = { placeX, (int)( placeX + beamFraction * (  satX - x - placeX )), 
                                      (int)( placeX + beamFraction * ( satX + x -placeX )) };
                    int[] uplinkY = { placeY, (int) (placeY + beamFraction * ( satY - y - placeY ) ), 
                                      (int)( placeY + beamFraction * ( satY + y - placeY) )};

                    g.setColor(beamColor);
                    g.fillPolygon( uplinkX, uplinkY, 3);
                }
                receiverImage = recSignalImg;
                
                if (spriteDraw.important || zoom >= 5) { //prints the contact information
                    g.setColor(Color.white); g.setFont( new Font("Arial" , Font.BOLD, 11) );
                    if (langEng) g.drawString( "Contact With:" , placeX - 15, placeY + 32);
                    else g.drawString( "Contact Avec:" , placeX - 15, placeY + 32);
                    for (int i = 0; i < spriteDraw.getContacts().length; i++) {
                        g.drawString( spriteDraw.getContacts()[i] , placeX - 15, placeY + 43 + i*10); }
                }
            }
            else {
                //resets beam animation if SatSprite leaves range of location or another SatSprite is selected
                if (beamAnimate != null && (beamPlace == spriteDraw || beamSat != satSelected) ) {
                     beamAnimate = null;
                     beamFraction = 0; }
                receiverImage = recImg;                
            }
            //draws receiver
            if (zoom > 2 && zoom < 4 && !spriteDraw.important) {
                double ratio = (zoom - 2) / 2;
                g.drawImage(receiverImage, placeX - 7, (int)(placeY -30 + (1.0-( ratio)) * (double)receiverImage.getHeight(this)),                            
                            (int)(ratio * (double)recImg.getWidth(this)),(int)( ratio * (double)receiverImage.getHeight(this)), this); }
            else g.drawImage(receiverImage, placeX - 7, placeY -30, this);
            
            g.setColor(Color.white); g.setFont(nameFont); //draws name of sats
            g.drawString( spriteDraw.getName(), placeX - 15, placeY + 18);
        } //end of if zoom > 2
    }
    
    private void drawSatSprite(Graphics g, SatSprite spriteDraw) {
        //get SatSprite coordinates in pixel system
        int satX = getXCoord(spriteDraw.getLong());
        int satY = getYCoord(spriteDraw.getLat());

        //draws range circle
        if ( spriteDraw.showRange )
            drawRange( g, spriteDraw );
        
        g.setColor(Color.white); g.setFont(nameFont);
        g.setColor(Color.white); g.fillOval(satX -2, satY - 2, 4, 4); //used before images are loaded        
        
        //draws SatSprite rotation animation
        if (spriteDraw.hasContact) {
            int angle = getAngle( getXCoord(spriteDraw.getLong()), getYCoord(spriteDraw.getLat()),
                                  getXCoord(spriteDraw.contactLong) , getYCoord(spriteDraw.contactLat) );

            java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform();
            g2d = (Graphics2D)g;

            at.translate(satX, satY);
            at.rotate(getRad(angle - 110));
            at.translate(-(1.0/2)*satImg.getWidth(this), -(1.0/2)*satImg.getHeight(this));

            g2d.drawImage( satImg ,at, this);
        }
        else if (spriteDraw.getName() == "sat83") { //secret
            java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform();
            g2d = (Graphics2D)g;
            
            int imgWidth = shuttleImg.getWidth(this);
            int imgHeight = shuttleImg.getHeight(this);
            
            at.translate(satX, satY);
            at.rotate(getRad(spriteDraw.angle));
            at.translate(-(1.0/2)*imgWidth, -(1.0/2)*imgHeight);

            g2d.drawImage( shuttleImg , at, this);
            at.translate(0, imgHeight);
            at.scale(1, 0.2f);
            
            if ( flameDrawImg == flameImg1 ) flameDrawImg = flameImg2;
            else flameDrawImg = flameImg1;
            
            if (spriteDraw.speed > 0) {
                //at.scale(1.0f, (float)(spriteDraw.speed / 3000) / 25 );
                g2d.drawImage( flameDrawImg , at, this);
            }
            
                      //ASTRONAUT CODE
            boolean fixSat = false;
            boolean useCanadaArm = false;
            
            int i = 0;
            while ( !fixSat && i < satsVect.size() ) { //resets all contacts to null
                satFix = (SatSprite)satsVect.elementAt(i);
                if (satFix != spriteDraw) {
                    
                    int flyXTemp = getXCoord(satFix.getLong()) - satX;
                    int flyYTemp = getYCoord(satFix.getLat()) - satY;
                    //checks whether Canada Arm can Reach Target
                    int distanceArmTemp = (int)(Math.sqrt(flyXTemp * flyXTemp + flyYTemp * flyYTemp));
                    if ( distanceArmTemp + 3 < armLength ) {
                        armX = flyXTemp;
                        armY = flyYTemp;
                        useCanadaArm = true;
                        distanceArm = distanceArmTemp;
                    }
                    if ( Math.abs(flyXTemp) < 50 * zoom && Math.abs(flyYTemp) < 50 * zoom ){
                        fixSat = true;
                        flyX = flyXTemp;
                        flyY = flyYTemp;
                    }
                } i++;
            }

            //animates astronauts flying out and in
            if (!fixSat) {
                if (asFraction - 0.05 <= 0) asFraction = 0;
                else asFraction -=0.03;
            }
            else {
                if (asFraction >= 1) asFraction = 1;
                else asFraction +=0.02;
            }
            
            //CANADA ARM CODE
            //animates arm extending
            if (!useCanadaArm) {
                if (armFraction - 0.05 <= 0) armFraction = 0;
                else armFraction -=0.025;
            }
            else {
                if (armFraction >= 1) asFraction = 1;
                else armFraction +=0.025;                
            }
            
            g.setColor(Color.lightGray);
            
            //CANADA ARM CODE
            if (armFraction > 0) {
                g2d = (Graphics2D)(g);

                //extends arm and animates it's diagonal motion
                if (armFraction < 1) {
                    armX *= armFraction;
                    armY *= armFraction;
                }
                else {
                    armX -= Math.sin(asFraction2) * 3;
                    armY += Math.sin(asFraction2) * 3;
                }
                
                //cosine law
                double angleBeta = (PI - Math.acos( 1.0 - distanceArm * distanceArm / (0.5 * armLength * armLength) )) / 2;
                double angle = getRad( getAngle(satX,satY, satX + armX, satY + armY) ) - PI/2 - angleBeta;
                int jointX = satX + armX + (int)(Math.cos(angle) * armPartLength);
                int jointY = satY + armY + (int)(Math.sin(angle) * armPartLength);
                
                g2d.setStroke( new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND) );
                g2d.setColor( new Color(240,240,240) );
                g2d.drawLine(satX, satY, jointX, jointY);
               
                g2d.drawLine(jointX, jointY, satX + armX, satY + armY ); //makes arm rotate
                g2d.setStroke( new BasicStroke(1) );
                
                g.setColor(Color.gray); 
                g2d.fillOval(jointX - 2, jointY - 2, 4, 4);
            }
            
            //astroNaut Animation
            if (asFraction > 0) {
                if (asFraction < 1) {
                    g.drawImage( anImg, (int)(satX + asFraction* (flyX + 22) + Math.sin(asFraction * PI) * flyY / 2 - anImg.getWidth(this) /2 ),
                                 (int)(satY + asFraction * flyY + Math.sin(asFraction * PI) * flyX / 2 - anImg.getHeight(this) / 2), this  );
                    g.drawImage( anImg, (int)(satX + asFraction* (flyX - 13) - Math.sin(asFraction * PI) * flyY / 2  - anImg.getWidth(this) / 2),
                                 (int)(satY + asFraction * flyY - Math.sin(asFraction * PI) * flyX / 2  - anImg.getHeight(this) /2 ), this  );
                }
                else {
                    asFraction2+=0.16;
                    g.drawImage( anImg, (int)(satX + flyX - anImg.getWidth(this) /2 ) + 22,
                                 (int)(satY + flyY - Math.sin(asFraction2) * 5 - anImg.getHeight(this) / 2), this  );
                    g.drawImage( anImg, (int)(satX + flyX + anImg.getWidth(this) / 2) - 13,
                                 (int)(satY + flyY + Math.sin(asFraction2) * 5 - anImg.getHeight(this) /2 ),
                                 -anImg.getWidth(this), anImg.getHeight(this), this  );
                    if (asFraction2 > 50) asFraction2 = 1;
                } 
            }
            
            if (!useCanadaArm && !fixSat) {
                g.setColor(Color.white);
                if (langEng) g.drawString( "pilot me using the arrows", satX - 15, satY + 41);
                else g.drawString( "pilote-moi en utilisant les directions", satX - 25, satY + 41);
            }
        }
        else
            g.drawImage(satImg, satX - (satImg.getWidth(this)/2), satY - (satImg.getHeight(this)/2), this);

        //int size = satImg.getWidth(this);
        if (spriteDraw.showName) {
            if (spriteDraw == satSelected) {
                if (followSat) g.setColor(Color.red);
                else g.setColor(Color.green); 
            }
            else g.setColor(Color.white);
            g.setFont(nameFont);
            g.drawString( spriteDraw.getName(), satX - 15, satY + 26);
        }
    }
    
    private void drawRange(Graphics g, SatSprite sat) {
        if (sat.getRange() > 0) {
            if (sat == satSelected)
                g.setColor(Color.green);
            else if (sat.hasContact)
               g.setColor(Color.white);
            else
               g.setColor( new Color(180,180,180) ); //color is SatSprite has no contact

            Range satRange = sat.getRangeObj();

            int countCoords = satRange.getLongCoords().length;

            //System.out.println("\nCOUNT CORDS: " + countCoords);

            int[] XCoords = new int[countCoords];
            int[] YCoords = new int[countCoords];
            for (int i = 0; i < countCoords; i++) {
               XCoords[i] = (int)(getXCoord(satRange.getLongCoords()[i]));
               YCoords[i] = (int)(getYCoord(satRange.getLatCoords()[i]));
               //System.out.println("XRange: " + SatRange.getLongCoords()[i] + "YRange: " + SatRange.getLatCoords()[i] );

               //only draws if line is continuous
               if ( i > 0 && Math.abs(XCoords[i] - XCoords[i-1]) < 200 )
                    g.drawLine(XCoords[i-1], YCoords[i-1], XCoords[i], YCoords[i] );
            }
            //finishes off circle
            if ( Math.abs(XCoords[0] - XCoords[countCoords - 1]) < 200 )
             g.drawLine(XCoords[0], YCoords[0], XCoords[countCoords - 1], YCoords[countCoords - 1] );
        }
    }
    
    private void drawSatInfo(Graphics g, SatSprite sat) {
        if (zoom % 1 > 0) { //tells zoom while zooming
                g.setColor(Color.white); g.setFont( new Font("Arial" , Font.BOLD, 20) );
                g.drawString("ZOOM: " + zoom + "X", mapWindowX / 2 - 150, mapWindowY /2); }
        
        int infoBoxX = 125;
        int infoBoxY = 125;
        int marginX = 25;
        int marginY = 25;
        
        if (satSelected != null) {
            if (satInfoFractionX < 1)  //starts animation
                if (satInfoAnimateThread == null) { satInfoAnimateThread = new Thread(this); satInfoAnimateThread.start(); }
            else satInfoAnimateThread = null;
                        
            
            int circleX = (int)(infoBoxX + marginX - (infoBoxX + infoBoxX * (satInfoFractionX)) / 2);
            int circleY = mapWindowY - (int)((infoBoxY + infoBoxY * (satInfoFractionY)) / 2) - marginY;
            
            //makes circle transparent
            g2d = (Graphics2D)g;
            AlphaComposite myAlpha;
            myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.2f);
            g2d.setComposite(myAlpha); g.setColor(Color.black);
            //draws infomation box circle with animation centered around center
            g.fillOval(circleX, circleY, (int)(infoBoxX * (satInfoFractionX)), (int)(infoBoxY * (satInfoFractionY)));
            
            g2d.setComposite(alphaOpaque);
            g.setColor(Color.white);
            g.drawOval(circleX, circleY, (int)(infoBoxX * (satInfoFractionX)), (int)(infoBoxY * (satInfoFractionY)));
            
            g.setColor(Color.blue);
            if (satInfoFractionY != 1) { //draws line that connects satellite to circle just for effects
                if (satInfoFractionX !=1) {
                    g.drawLine( getXCoord(satSelected.getLong()), getYCoord(satSelected.getLat()), circleX, circleY + (int)(infoBoxY * satInfoFractionY / 2));
                    g.drawLine( getXCoord(satSelected.getLong()), getYCoord(satSelected.getLat()), circleX + (int)(infoBoxX * satInfoFractionX),
                                circleY + (int)(infoBoxY * satInfoFractionY / 2)); 
                }
                else {
                    g.drawLine( getXCoord(satSelected.getLong()), getYCoord(satSelected.getLat()), circleX + (int)(infoBoxX * satInfoFractionX / 2), circleY );
                    g.drawLine( getXCoord(satSelected.getLong()), getYCoord(satSelected.getLat()), circleX + (int)(infoBoxX * satInfoFractionX / 2),
                                    circleY + (int)(infoBoxY * satInfoFractionY));
                }
            }
            
            if (satInfoFractionY == 1) { //starts drawing Satellite Info
                if (langEng) {
                    g.setFont( new Font("Arial" , Font.BOLD, 13) ); g.setColor(Color.white);
                    g.drawString( "Name: " + sat.getName() , marginX + 10, mapWindowY - infoBoxY / 2 - 20);
                    
                    g.setFont( new Font("Arial" , Font.PLAIN, 12) ); 
                    
                                        
                    if (followSat) { 
                        g.setColor(Color.red);
                        g.drawString( "Following Satellite", mapWindowX - 125, mapWindowY - 10);
                        g.setColor(Color.white); }
                    
                    g.drawString( "Longitude: " + (int)sat.getLong() , marginX + 15, mapWindowY - infoBoxY / 2 - 40);
                    g.drawString( "Latitude: " + (int)sat.getLat() , marginX + 25, mapWindowY - infoBoxY / 2 - 55);
                    g.drawString( "Altitude: " + sat.getAltitude() , marginX + 15, mapWindowY - infoBoxY / 2);
                    g.drawString( "LOS: " + sat.getRange() , marginX + 25, mapWindowY - infoBoxY / 2 + 15);
                }
                else { //for FRENCH TRANSLATION                   
                    g.setFont( new Font("Arial" , Font.BOLD, 13) ); g.setColor(Color.white);
                    g.drawString( "Nom: " + sat.getName() , marginX + 10, mapWindowY - infoBoxY / 2 - 20);
                    
                    g.setFont( new Font("Arial" , Font.PLAIN, 12) );
                                        
                    if (followSat) { 
                        g.setColor(Color.red);
                        g.drawString( "Suivre Le Satellite", mapWindowX - 125, mapWindowY - 10);
                        g.setColor(Color.white); }
                    
                    g.drawString( "Longitude: " + (int)sat.getLong() , marginX + 15, mapWindowY - infoBoxY / 2 - 40);
                    g.drawString( "Latitude: " + (int)sat.getLat() , marginX + 25, mapWindowY - infoBoxY / 2 - 55);
                    g.drawString( "Altitude: " + sat.getAltitude() , marginX + 15, mapWindowY - infoBoxY / 2);
                    g.drawString( "LOS: " + sat.getRange() , marginX + 25, mapWindowY - infoBoxY / 2 + 15);
                }            
            }
        }
        else if (satSelected == null) { satInfoFractionX = 0; satInfoFractionY = 0; }
        else {}
    }
    
    private void drawGrid(Graphics g) {
        int GridX = (int)( gridZoom * 6 );      int GridY = GridX / 2;
        double intervalDegLong = 180 / GridX;   double intervalDegLat = 90 / GridY;
        double degLat = 0;                      double degLong = 0;
        int xE, xW, yN, yS;
        
         g.setFont(gridFont);
        
        //draws Horizontal Grid
        for (int i = 0; i < GridX; i++) {
            xE = getXCoord(degLong); xW = getXCoord(-degLong);
           
            if (xE <= mapWindowX) {//stop draw is it goes outside map window
                g.setColor(Color.gray); g.drawLine( xE, 0, xE, mapWindowY);
                g.setColor(Color.white); g.drawString("" + degLong + "E" ,xE - 12, mapWindowY + 12); }
            
            if (i > 0 && xW <= mapWindowX) { //makes sure E and W does not overlap at 0
                g.setColor(Color.gray); g.drawLine( xW, 0, xW, mapWindowY);
                g.setColor(Color.white); g.drawString("" + degLong + "W" , xW - 12, mapWindowY + 12); }            
            degLong += intervalDegLong;
        }
        
        //draws Vertical Grid
        for (int i = 0; i < GridY; i ++) {
            yN = getYCoord(degLat); yS = getYCoord(-degLat);
           
            if (yN < mapWindowY) {
                g.setColor(Color.gray); g.drawLine( 0, yN, mapWindowX, yN);
                g.setColor(Color.white); g.drawString("" + (int)degLat + "N" , mapWindowX + 3, yN + 5); }
            if (i > 0 && yS <= mapWindowY) { //makes sure N and S does not overlap at 0
                g.setColor(Color.gray); g.drawLine( 0, yS, mapWindowX, yS);
                g.setColor(Color.white); g.drawString("" + (int)degLat + "S" , mapWindowX + 3, yS + 5); }
            degLat += intervalDegLat;
        }         
    }
    
    public boolean mouseMove(Event evt, int x, int y) {
        mouseX = x;                     mouseY = y;
        mouseLong = getLongCoord(x);    mouseLat = getLatCoord(y);
        return true;
    }
        
    public boolean mouseDown(Event evt, int x, int y) {
        boolean foundSat = false;
        boolean zoomPositive;
        
        if (!langSelected) { //selects language
            if (y < mapWindowY && x < mapWindowX) {
                if (x < (mapWindowX / 2))
                    langEng = false;
                else langEng = true;
                if (animationThread == null) { animationThread = new Thread(this); animationThread.start(); }
            }
        }
        else {
            if (evt.shiftDown() || evt.controlDown()) //handles unzooming
                zoomPositive = false;
            else zoomPositive = true;

            switch (evt.clickCount) { 
                case 1: { //selects SatSprite
                    int satCount = 0;
                    while ( !foundSat && satCount < satsVect.size() ) { 
                        SatSprite sat = (SatSprite)satsVect.elementAt(satCount);
                        if ( ( Math.abs( getXCoord(sat.getLong()) - x) < 30 )
                            && ( Math.abs( getYCoord(sat.getLat()) - y) < 20 ) ) {
                            if (satSelected == sat) {//deselects if satellite is clicked on again
                                 satSelected = null; followSat = false; }
                             else satSelected = sat;
                             satInfoFractionX = 0; satInfoFractionY = 0; //resets animation for info circle
                             foundSat = true;
                        } satCount++;                    
                    } 
                    break;
                }
                case 2: {
                    followSat = false;
                    //begins zooming algorithm
                    if ( ( (zoomPositive && zoomTo < 6) || (zoomTo > 1 && !zoomPositive ) ) && !followSat && zoomMapThread == null) {
                        if (zoomPositive) { //increases zoom factor
                            if (zoomPositive && zoomTo == 1) zoomTo += 1;
                            else  zoomTo += 2;
                        }
                        else if (zoomTo > 2) zoomTo -= 2; //zooms out double speed if zoom is high
                        else zoomTo -= 1;

                        //starts zoom map animation
                        if (zoomMapThread == null) { zoomMapThread = new Thread(this); zoomMapThread.start(); }
                        mapClickX = x + Math.abs(offScreenX); mapClickY = y + Math.abs(offScreenY); //coordinates for zoom to focus on
                    } break;
                }
            }
        }
        repaint();
        return true;
    }
    
    public boolean keyDown(Event evt, int key) {
        System.out.println("Key: " + key);
        
        switch (key) {
            case Event.ENTER:{
                if (followSat == false && zoomMapThread == null) //does not follow while in zoom animation
                    followSat = true;
                else followSat = false;
                
                
                break;
            }
            case 111: { //in case o is pressed
                SatMapperOptions optionsDialog = new SatMapperOptions(this);
                optionsDialog.show();
                break;
            }
            
            case Event.ESCAPE:{
                    switchNightMode();
                break;                    
            }
            case Event.HOME: {
                if (sat83 == null) {
                    sat83 = new SatSprite(getLatCoord(mapWindowY / 2), getLongCoord(mapWindowX / 2), 0, 0, 0, 0, "sat83");
                    satsVect.addElement(sat83); }
                satSelected = sat83;
                followSat = true;
                satInfoFractionX = 0; satInfoFractionY = 0; //resets animation for info circle
                break;
            }
            
            //for shuttle piloting
            case Event.UP: { satSelected.speed += 200; } break;
            case Event.DOWN: {
                if ( satSelected.speed - 400 < 0) //ensures speed is not negative
                    satSelected.speed = 0;
                else satSelected.speed -= 400;
                satSelected.speed -= 400;
            } break;
            case Event.RIGHT: { satSelected.angle += 2; } break;
            case Event.LEFT: { satSelected.angle -= 2; } break;
        } return true;
    }
    
                                      //location of mouse clicks
    private void calcZoom(double zoom, int mapClickX, int mapClickY) { //algorithm for zooming objects
        mapX = (int)(zoom * mapWindowX);
        mapY = (int)(zoom * mapWindowY);
       
        offScreenX = - (int)( (zoom/prevZoom) * (mapClickX - prevMapX / zoom / 2) ); //zooms to center of mouse click
            // makes sure map does not go off edge
            if (offScreenX > 0) offScreenX = 0; else if (offScreenX < mapWindowX - mapX) offScreenX = mapWindowX - mapX;
        offScreenY = - (int)( (zoom/prevZoom) * (mapClickY - prevMapY / zoom / 2) );
            if (offScreenY > 0) offScreenY = 0; else if (offScreenY < mapWindowY - mapY) offScreenY = mapWindowY - mapY;
        mapMidX =  mapX / 2; mapMidY =  mapY / 2;
    }
    
    private void moveScreen(int centerX, int centerY) { //algorithm for moving entire screen
        offScreenX = - (int)( centerX - (mapWindowX / 2) ); //zooms to center of mouse click
            // makes sure map does not go off edge
            if (offScreenX > 0) offScreenX = 0; else if (offScreenX < mapWindowX - mapX) offScreenX = mapWindowX - mapX;
        offScreenY = - (int)( centerY - (mapWindowY / 2) );
            if (offScreenY > 0) offScreenY = 0; else if (offScreenY < mapWindowY - mapY) offScreenY = mapWindowY - mapY;
        mapOffScreenX = offScreenX; mapOffScreenY = offScreenY;
    }
    
    private int getAngle(Sprite s1, Sprite s2) {
        return getAngle( getXCoord(s1.getLong()), getYCoord(s1.getLat()), getXCoord(s2.getLong()), getYCoord(s2.getLat()) ); }
    
    private int getAngle(double x1, double y1, double x2, double y2) { //gets angle between two sprites, used for animation
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
    
    //returns SatSprite coordinates using pixel form
    private int getXCoord(double coordLong) {
        return (int)(( ( (coordLong) / 180.0 ) * mapMidX ) + mapMidX + offScreenX ); }
    private int getYCoord(double coordLat) {
        return (int)(mapMidY - ( (coordLat / 90.0 ) * mapMidY - offScreenY)); }
    
    //gets the longitutde latitude based on coordinates on map
    private double getLongCoord(int x) {
        return (x - mapMidX - offScreenX) / (double)mapMidX * 180; }
    private double getLatCoord(int y) {
        return -(y - mapMidY - offScreenY) / (double)mapMidY * 90; }
        
    //converts degrees to radians
    private double getRad(double deg) { return ( degToRadConv * deg ); }
    
    private void updateContacts(Vector sats, Vector places) {     
        for (int i = 0; i < sats.size(); i++) { //resets all contacts to null
            SatSprite sat = (SatSprite)sats.elementAt(i);
            sat.hasContact = false; }            
        for (int i = places.size() - 1; i >= 0; i--) { //points to the city decalared first if more than 1 are in range
            Place place = (Place)places.elementAt(i);
            if (place.important)
                place.findContacts(sats); 
        }
    }
    
    public void destroy() { offscreen.dispose(); }
    
    //OBJECT INTERFACE OBJECT INTERFACE OBJECT INTERFACE OBJECT INTERFACE OBJECT INTERFACE OBJECT INTERFACE 
    public void addSatSprite(double latitude, double longitude, int losRange, int altitude, String name) {
        satsVect.addElement( new SatSprite(latitude, longitude, losRange, altitude, 0, 0, name) ); }
        
    public void addPlace(double latitude, double longitude, String name) {
        placesVect.addElement( new Place(latitude, longitude, name, true) ); }
        
    public boolean removeSatSprite(String name) {
       for (int i = 0; i < satsVect.size(); i++) {
            SatSprite sat = (SatSprite)satsVect.elementAt(i);
            if ( sat.getName() == name)
                return removeSatSprite(i);
        } return false;
    }
    
    public boolean removeSatSprite(int vectorLocation) {
        if (vectorLocation >=0 && vectorLocation < satsVect.size() ) {
            satsVect.removeElementAt(vectorLocation);
            repaint(); return true;
        } else return false;
    }
    
    public boolean removePlace(String name) {
        for (int i = 0; i < placesVect.size(); i++) {
            Place place = (Place)placesVect.elementAt(i);
            if ( place.getName() == name)
                return removePlace(i);
        } return false;
    }
    
    public boolean removePlace(int vectorLocation) {
        if (vectorLocation >=0 && vectorLocation < placesVect.size() ) {
            placesVect.removeElementAt(vectorLocation);
            repaint(); return true;
        } else return false;
    }
    
    public void removeAllSatSprites() { satsVect.removeAllElements(); }
    public void removeAllPlaces() { placesVect.removeAllElements(); }
    
    public boolean updatePosition(String satName, double longitude, double latitude) {
        for (int i = 0; i < satsVect.size(); i++) {
            SatSprite sat = (SatSprite)satsVect.elementAt(i);
            if ( sat.getName() == satName)
                return updatePosition(i, longitude, latitude);
        } return false;
    }
    
    //updates SatSprite postion based directly on location in Vector
    public boolean updatePosition(int vectorLocation, double longitude, double latitude) {
        if (vectorLocation >=0 && vectorLocation < satsVect.size() ) {
            SatSprite sat = (SatSprite)satsVect.elementAt(vectorLocation);
            sat.setLong(longitude); sat.setLat(latitude);
            if (followSat)
                //moves screen to absolute coordinates of image
                moveScreen( getXCoord(satSelected.getLong()) + Math.abs(offScreenX), 
                            getYCoord(satSelected.getLat()) + Math.abs(offScreenY) );
            repaint(); return true;
        } else return false;
    }
    
    
    public void switchNightMode() {
        if (nightTransitionThread == null) { //does not follow while in zoom animation
            nightTransitionThread = new Thread(this);
            nightTransitionThread.start();
        }
    }
}