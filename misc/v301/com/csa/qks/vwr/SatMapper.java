package com.csa.qks.vwr;

//import ca.gc.space.quicksat.ground.tracking.*;
import java.net.*;
import java.io.*;
//import ca.gc.space.quicksat.ground.satellite.*;
//import com.csa.qks.jpg.*;
import com.csa.qks.cont.SatDataToSend;
import com.csa.qks.serv.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.PrintStream;
import java.util.*;

import java.awt.event.*;
import javax.swing.*;

// Referenced classes of package com.csa.qks.vwr:
//            Place, SatSprite, SatData, Sprite,
//            Range, Zoomer, Updater, Receiver

public class SatMapper extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener {
    
    SatData myData;
    Zoomer myZoomer;
    Graphics2D g2d;
    //changed
    SatMapperOptions optionsDialog = new SatMapperOptions(this);
    int updateInterval = 5000;
    boolean showGrid = true;
    boolean showRange = true;
    boolean shuttleCreated = false;
    boolean isLocal = true;
    boolean showPath = false;
    
    //Files
    static String mapFile = "mapHiRes.jpg";
    static String mapCloudsFile = "mapClouds.jpg";
    static String satFile = "SatGeneric.gif";
    static String mapNightFile = "mapNight.jpg";
    static String recFile = "Receiver.gif";
    static String recSigtFile = "ReceiverSignal.gif";
    
    static String shuttleFile = "sat83.gif";
    static String alouFile = "alouetteSat.gif";
    static String issFile = "ISS.gif";
    static String radFile = "radarSat.gif";
    
    //
    final static double PI = Math.acos(-1);
    final static double degToRadConv = (PI / 180.0);
    
    //grid for map
    double gridDensity = 1;
    
    //total screen Size
    static final int ScreenX = 730;
    static final int ScreenY = 365;
    //map size
    int mapWindowX = ScreenX - 30;
    int mapWindowY = ScreenY - 15;
    
    //midpoint used for calculating coordinates
    int mapMidX = mapWindowX / 2;
    int mapMidY = mapWindowY / 2;
    
    //controls applet speed
    int speedTimeX = 400;
    int framesPerSecond = 40;
    int frameCounter, fps;
    
    //fonts and colors
    Font nameFont = new Font("Arial", Font.PLAIN, 11);
    Font gridFont = new Font("Arial", Font.PLAIN, 14);
    AlphaComposite alphaOpaque = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
    Color backColor = Color.black;
    Color shuttleFlameColor = Color.orange;
    
    //location of mouse
    double mouseLong, mouseLat;
    double mouseClickLong, mouseClickLat;
    
    int mouseX, mouseY;
    
    boolean followSat = false; //whether SatSprite is followed
    boolean started = false;
    //whether applet has started, used to load night background
    boolean removeClouds = true; //whether clouds are removed at all times
    boolean langEng; //if langugage is english
    boolean langSelected = false; //if langugage is english
    boolean zoomerFinished = true;
    public boolean isPainted = false;
    boolean pathSelected;
    
    public static Vector satsVect = new Vector(); //vector of all Satellites
    Vector placesVect = new Vector(); //vector of all places
    SatSprite satSelected; //currently selected SatSprite
    SatSprite satFix = new SatSprite();
    SatSprite shuttleSprite;
    
    long dateArrayPosition;
    long dateSelected;
    
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
    Thread shuttleThread;
    Thread shuttleVectorLocation;
    
    int shuttleX, shuttleY;
    
    Thread zoomMapThread; //zooms Map
    double gridZoom = 1;
    double mapZoom = 1;
    double zoom = 1;
    double prevZoom = zoom;
    int mapClickX, mapClickY; //position of mouse when zooming
    int mouseDownX, mouseDownY;
    int startingOffScreenX, startingOffScreenY;
    int zoomTo = 1;
    
    //used for map zooming
    int mapX = mapWindowX;
    int mapY = mapWindowY;
    int prevMapX = mapX;
    int prevMapY = mapY;
    int offScreenX = 0;
    int offScreenY = 0;
    double mapPosX = 0;
    double mapPosY = 0;
    
    int mapOffScreenX = 0;
    
    int mapOffScreenY = 0;
    Thread nightTransitionThread;
    double nightTransition = 0;
    Thread satInfoAnimateThread;
    double satInfoFractionX;
    double satInfoFractionY;
    Thread animationThread;
    double anFraction = 0;
    
    
    //number foirmat used for longitude latitude
    java.text.DecimalFormat coordNumberFormat = new java.text.DecimalFormat("###.00");
    
    
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
    public Image offscreenImage;
    Image mapImg, mapNightImg, mapCloudsImg, satImg, recImg, recSignalImg, shuttleImg, anImg, flameImg1, flameImg2, flameDrawImg;
    
    Image satGen;
    Image satAlou;
    Image satRad;
    Image satIss;
    Image satEnvi;
    public Graphics graphs;
    //    ca.gc.space.quicksat.ground.client.jload j;
    /**
     * Insert the method's description here.
     * Creation date: (3/13/2002 3:51:46 PM)
     */
    public SatMapper() {
        
        //   setMaximumSize(new Dimension(730,365));
        
        //  setPreferredSize(new Dimension(730,365));
        
        
        satsVect.removeAllElements();
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        
        satGen = Tracker.satGen;
        
        mapImg = Tracker.mapImg;
        mapNightImg = Tracker.mapNightImg;
        mapCloudsImg = Tracker.mapCloudsImg;
        recImg = Tracker.recImg;
        recSignalImg = Tracker.recSignalImg;
        
        satIss = Tracker.issImg;
        satAlou = Tracker.alouImg;
        satRad = Tracker.radImg;
        shuttleImg = Tracker.shuttleImg;
        anImg = Tracker.anImg;
        flameImg1 = Tracker.flameImg1;
        flameImg2 = Tracker.flameImg2;
        
        createPlaces();
        
        this.start();
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/13/2002 3:51:46 PM)
     */
    public SatMapper(String path) {
        
        // setMaximumSize(new Dimension(730,365));
        
        //  setPreferredSize(new Dimension(730,365));
        
        satsVect.removeAllElements();
        addMouseMotionListener(this);
        addMouseListener(this);
        addKeyListener(this);
        
        satGen = Toolkit.getDefaultToolkit().getImage(path + satFile);
        mapImg = Toolkit.getDefaultToolkit().getImage(path + mapFile);
        recImg = Toolkit.getDefaultToolkit().getImage(path + recFile);
        recSignalImg = Toolkit.getDefaultToolkit().getImage(path + recSigtFile);
        satAlou = Toolkit.getDefaultToolkit().getImage(path + alouFile);
        satIss = Toolkit.getDefaultToolkit().getImage(path + issFile);
        satRad = Toolkit.getDefaultToolkit().getImage(path + radFile);
        //satEnvi =  Toolkit.getDefaultToolkit().getImage(path+enviFile);
        
        shuttleImg = Toolkit.getDefaultToolkit().getImage(path + "sat83.gif");
        anImg = Toolkit.getDefaultToolkit().getImage(path + "Astronaut.gif"); //image of astronaut
        flameImg1 = Toolkit.getDefaultToolkit().getImage(path + "Flame1.gif");
        flameImg2 = Toolkit.getDefaultToolkit().getImage(path + "Flame2.gif");
        
        createPlaces();
        this.start();
        
    }
    //    public SatMapper(String path, ca.gc.space.quicksat.ground.client.jload j) {
    //        this.j = j;
    //      //  this.setSize(200, 200);
    //        addMouseMotionListener(this);
    //        addMouseListener(this);
    //        satGen = Toolkit.getDefaultToolkit().getImage(path + satFile);
    //        mapImg = Toolkit.getDefaultToolkit().getImage(path + mapFile);
    //        recImg = Toolkit.getDefaultToolkit().getImage(path + recFile);
    //        recSignalImg = Toolkit.getDefaultToolkit().getImage(path + recSigtFile);
    //        createPlaces();
    //        langSelected = true;
    //        langEng = true;
    //       // this.setWindowSize(300, 200);
    //        this.start();
    //
    //    }
    public void addPlace(double latitude, double longitude, String name) {
        placesVect.addElement(new Place(latitude, longitude, name));
    }
    public void addPlace(double latitude, double longitude, String name, boolean isImportant) {
        placesVect.addElement(new Place(latitude, longitude, name, isImportant));
    }
    public void addSatSprite(double latitude, double longitude, int losRange, int altitude, String name) {
        satsVect.addElement(new SatSprite(latitude, longitude, losRange, altitude, 0, 0, name));
    }
    public static void addSatSprite(double latitude, double longitude, String name) {
        satsVect.addElement(new SatSprite(latitude, longitude, name));
    }
    public void calcMapZoom(double mapZoom, int zoomX, int zoomY) {
        int mapOffScreenX = (int)((mapZoom / prevZoom) * ((double)zoomX - (double)prevMapX / mapZoom / 2D));
        mapPosX = mapOffScreenX / ( mapWindowX * mapZoom);
        if (mapPosX < 0) mapPosX = 0;
        else if ( mapPosX + (1 / mapZoom) > 1 ) mapPosX = 1 - (1 / mapZoom);
        
        int mapOffScreenY = (int)((mapZoom / prevZoom) * ((double)zoomY - (double)prevMapY / mapZoom / 2D));
        mapPosY = mapOffScreenY / ( mapWindowY * mapZoom);
        if (mapPosY < 0) mapPosY = 0;
        else if ( mapPosY + (1 / mapZoom) > 1 ) mapPosY = 1 - (1 / mapZoom);
    }
    public void calcZoom(double zoom, int mapClickX, int mapClickY) {
        mapX = (int) (zoom * mapWindowX);
        mapY = (int) (zoom * mapWindowY);
        
        offScreenX = (int) ((zoom / prevZoom) * ((double) mapClickX - (double) prevMapX / zoom / 2D));
        
        if (offScreenX < 0)
            offScreenX = 0;
        else
            if (offScreenX > mapX - mapWindowX)
                offScreenX = mapX - mapWindowX;
        
        offScreenY = (int) ((zoom / prevZoom) * ((double) mapClickY - (double) prevMapY / zoom / 2D));
        
        if (offScreenY < 0)
            offScreenY = 0;
        else
            if (offScreenY > mapY - mapWindowY)
                offScreenY = mapY - mapWindowY;
        mapMidX = mapX / 2;
        mapMidY = mapY / 2;
    }
    /**
     * Insert the method's description here.
     * Creation date: (3/15/2002 11:50:32 AM)
     * @param g java.awt.Graphics
     */
    public void createImg(Graphics g) {
        
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(mapWindowX, mapWindowY, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics b = bi.getGraphics();
        
        isPainted = false;
        if (zoom == 1.0D || zoom % 1 != 0.0D)
            followSat = false;
        
        //creates background
        b.setColor(backColor);
        b.fillRect(0, 0, getSize().width, getSize().height);
        
        //draws Map
        drawMap(b);
        
        b.fillRect(mapWindowX, 0, ScreenX, ScreenY);
        b.fillRect(0, mapWindowY, ScreenX, ScreenY);
        
        //draws objects
        if (showGrid)
            drawGrid(b);
        
        drawSprites(b, placesVect);
        drawSprites(b, satsVect);
        
        //prevents from drawing outside window
        b.setColor(backColor);
        b.fillRect(mapWindowX + 30, 0, ScreenX, ScreenY);
        b.fillRect(0, mapWindowY + 13, ScreenX, ScreenY);
        
        //information about screen
        drawSatInfo(b, satSelected); //draws information about SatSprite
        
        //Options Button
        drawOptionsButton(b);
        
        b.setColor(Color.white);
        b.setFont(new Font("Arial", Font.BOLD, 10));
        if (mouseX <= mapWindowX && mouseY <= mapWindowY) //location longitude latitude of mouse
            b.drawString("Lat:" + (int) mouseLat + " Long:" + (int) mouseLong, mouseX - 15, mouseY + 40);
        
        b.drawString(new Date(System.currentTimeMillis()).toString(), 10, 10);
        
        Image img = (Image) bi;
        offscreenImage = img;
        isPainted = true;
        
    }
    private void createPlaces() {
        
        //Places are created here
        //              CANADIAN CAPITALS AND MONTREAL
        placesVect.addElement(new Place(45.47, -73.75, "Montreal", true));
        placesVect.addElement(new Place(45.32, -75.67, "Ottawa"));
        placesVect.addElement(new Place(43.68, -79.63, "Toronto"));
        placesVect.addElement(new Place(51.10, -114.02, "Calgary"));
        placesVect.addElement(new Place(48.42, -123.32, "Victoria"));
        placesVect.addElement(new Place(49.9, -97.23, "Winnipeg"));
        placesVect.addElement(new Place(45.87, -66.53, "Fredericton"));
        placesVect.addElement(new Place(47.62, -52.75, "St. John's"));
        placesVect.addElement(new Place(62.47, -114.45, "YellowKnife"));
        placesVect.addElement(new Place(44.65, -63.57, "Halifax"));
        placesVect.addElement(new Place(46.28, -63.13, "Charlottetown"));
        placesVect.addElement(new Place(46.8, -71.38, "Quebec"));
        placesVect.addElement(new Place(50.52, -104.67, "Regina"));
        placesVect.addElement(new Place(60.72, -135.07, "Whitehorse"));
        placesVect.addElement(new Place(74.72, -94.98, "Resolute")); //not a capital but canada's northern most city
        
        //other places
        placesVect.addElement(new Place(20, -90, "Place1", true));
        placesVect.addElement(new Place(40, 100, "Place2", true));
        placesVect.addElement(new Place(-28, 123, "Place3", true));
    }
    public boolean createSats() {
        boolean satsCreated = false;
        do {
            try {
                SatDataToSend newData;
                int alt;
                double range;
                int speed;
                double lat;
                double longi;
                System.out.println("Creating");
                
                Enumeration enum = myData.getSats().elements();
                
                while (enum.hasMoreElements()) {
                    
                    newData = (SatDataToSend) enum.nextElement();
                    
                    alt = newData.getAlt();
                    
                    speed = newData.getSpeed();
                    lat = newData.getLat();
                    longi = newData.getLongi();
                    if (longi < 180L)
                        longi = -longi;
                    else
                        if (longi > 180L)
                            longi = 360L - longi;
                    //    System.out.println(newData.getName() + "  LAT: " + lat + "  LONG:  " + longi + "  Altitude: " + alt + " Speed: " + speed);
                    
                    //wE CALCULATE THE RANGE USING THE ALTITUDE
                    range = Math.acos(6357.0 / (6357.0 + (double) alt)) * 6357.0;
                    System.out.println("Altitude: " + alt);
                    System.out.println("RANGE: " + range);
                    
                    SatSprite newSat;
                    if ( newData.pathCreated ) {
                        newSat = new SatSprite(lat, longi, (int) range, alt, speed, 175, newData.getName(), newData.pathCoords); }
                    else { newSat = new SatSprite(lat, longi, (int) range, alt, speed, 175, newData.getName()); }
                    
                    if (newSat.getName().equals("RADARSAT"))
                        newSat.mySatImage = satRad;
                    else
                        if (newSat.getName().equals("ALOUETTE 1 (S-27)"))
                            newSat.mySatImage = satAlou;
                        else
                            if (newSat.getName().equals("ISS (ZARYA)"))
                                newSat.mySatImage = satIss;
                    //                    else
                    //                        if (newSat.getName().equals("ENVISAT"))
                    //                            newSat.mySatImage = satGen;
                    satsVect.addElement(newSat);
                    myData.available = true;
                    satsCreated = true;
                }
                
            } catch (Exception c) {
                satsCreated = false;
                System.out.println("Could not create Sats;");
                c.printStackTrace();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException _ex) {}
        } while (!satsCreated);
        
        if (this.isLocal){
            WarningFrame warnLocal = new WarningFrame();
            warnLocal.show();
            warnLocal.setVisible(true);
        }
        
        return true;
    }
    
    public void createSats(Satellite satChosen) {
        try {
            int alt;
            double range;
            int speed;
            double lat;
            double longi;
            
            System.out.println("Creating new sat and insert in sat vector");
            alt = satChosen.getAltitudeKm();
            
            speed = satChosen.getVelocityKmH();
            lat = (long)satChosen.getLatitude();
            longi = (long)satChosen.getLongitude();
            if (longi < 180L)
                longi = -longi;
            else
                if (longi > 180L)
                    longi = 360L - longi;
            //    System.out.println(newData.getName() + "  LAT: " + lat + "  LONG:  " + longi + "  Altitude: " + alt + " Speed: " + speed);
            
            //wE CALCULATE THE RANGE USING THE ALTITUDE
            range = Math.acos(6357.0 / (6357.0 + (double) alt)) * 6357.0;
            
            SatSprite newSat = new SatSprite(lat, longi, (int) range, alt, speed, 175, satChosen.getName());
            
            newSat.mySatImage = satGen;
            newSat.createRangeObj();
            satsVect.addElement(newSat);
            
        } catch (Exception c) {
            System.out.println("Could not create Sat;");
            c.printStackTrace();
        }
        
    }
    private void drawGrid(Graphics g) {
        
        int GridX = (int) (gridZoom * 6 * gridDensity);
        int GridY = GridX / 2;
        double intervalDegLong = 180 / GridX;
        double intervalDegLat = 90 / GridY;
        double degLat = 0;
        double degLong = 0;
        int xE = 0;
        int xW = 0;
        int yN = 0;
        int yS = 0;
        
        g.setFont(gridFont);
        //draws Horizontal Grid
        while (degLong < 180) {
            xE = getXCoord(degLong);
            xW = getXCoord(-degLong);
            //dosen't calculate more grid if previous line was offscreen
            if (xE <= mapWindowX && xE >= 0) { //stop draw is it goes outside map window
                g.setColor(Color.gray);
                g.drawLine(xE, 0, xE, mapWindowY);
                g.setColor(Color.white);
                g.drawString("" + degLong + "E", xE - 12, mapWindowY + 12);
            }
            if (degLong != 0 && xW <= mapWindowX && xW >= 0) { //makes sure E and W does not overlap at 0
                g.setColor(Color.gray);
                g.drawLine(xW, 0, xW, mapWindowY);
                g.setColor(Color.white);
                g.drawString("" + degLong + "W", xW - 12, mapWindowY + 12);
            }
            degLong += intervalDegLong;
        }
        
        //draws Vertical Grid
        while (degLat < 90) {
            yN = getYCoord(degLat);
            yS = getYCoord(-degLat);
            if (yN < mapWindowY && yN >= 0) {
                g.setColor(Color.gray);
                g.drawLine(0, yN, mapWindowX, yN);
                g.setColor(Color.white);
                g.drawString("" + (int) degLat + "N", mapWindowX + 3, yN + 5);
            }
            if (degLat != 0 && yS <= mapWindowY && yS >= 0) { //makes sure N and S does not overlap at 0
                g.setColor(Color.gray);
                g.drawLine(0, yS, mapWindowX, yS);
                g.setColor(Color.white);
                g.drawString("" + (int) degLat + "S", mapWindowX + 3, yS + 5);
            }
            degLat += intervalDegLat;
        }
        
        //draws the grid animation when satellite selected
        if (satSelected != null && satInfoAnimateThread != null && satInfoFractionX < 1 && satInfoFractionX > 0) {
            g.setColor(Color.gray);
            int moveX = (int) (((1.0 - satInfoFractionX)) * mapWindowX);
            int moveY = moveX / 8;
            
            int x = getXCoord(satSelected.getLong());
            g.drawLine(x + moveX, 0, x + moveX, mapWindowY);
            g.drawLine(x - moveX, 0, x - moveX, mapWindowY);
            
            int y = getYCoord(satSelected.getLat());
            g.drawLine(0, y + moveY, mapWindowX, y + moveY);
            g.drawLine(0, y - moveY, mapWindowX, y - moveY);
        }
    }
    private void drawMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AlphaComposite myAlpha;
        
        if (( mapZoom > 2 ) || removeClouds) {
            if (nightTransition > 0) drawMapImg(g, mapNightImg);
            
            if (nightTransition > 0 && nightTransition < 1) {
                myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - (float) nightTransition);
                g2d.setComposite(myAlpha); }
            
            if (nightTransition < 1) drawMapImg(g, mapImg);
        }
        
        if ( mapZoom < 4 && !removeClouds) {
            if (mapZoom > 2) { //creates map zooming effect
                myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - ((mapZoom - 2) / 2)));
                g2d.setComposite(myAlpha);
            }
            drawMapImg(g, mapCloudsImg);
        }
        myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
        g2d.setComposite(myAlpha);
    }
    private void drawMapImg(Graphics g, Image mapImg) {
        int mapX = (int)Math.abs( mapPosX * mapImg.getWidth(this) );
        int mapY = (int)Math.abs( mapPosY * mapImg.getHeight(this) );
        int mapX2 = (int)( mapX + mapImg.getWidth(this) / mapZoom );
        int mapY2 = (int)( mapY + mapImg.getHeight(this) / mapZoom );
        g.drawImage(mapImg, 0, 0, mapWindowX, mapWindowY, mapX, mapY, mapX2, mapY2, this);
    }
    private void drawOptionsButton(Graphics g) {
        Graphics2D g2d;
        //makes circle transparent
        g2d = (Graphics2D) g;
        AlphaComposite myAlpha;
        myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
        g2d.setComposite(myAlpha);
        
        g.setColor(Color.black);
        g.fillRoundRect(mapWindowX - 80, mapWindowY - 35, 70, 20, 15, 15);
        
        g2d.setComposite(alphaOpaque);
        
        g.setColor(Color.white);
        g.drawRoundRect(mapWindowX - 80, mapWindowY - 35, 70, 20, 15, 15);
        g.setFont(new Font("Arial" , Font.PLAIN, 16) );
        g.drawString("Options", mapWindowX - 71, mapWindowY - 19);
    }
    private void drawPlace(Graphics g, Place spriteDraw) {
        g.setColor(Color.white);
        int placeX = getXCoord(spriteDraw.getLong());
        int placeY = getYCoord(spriteDraw.getLat());
        if(zoom <= 3 && !spriteDraw.important) {
            g.setColor(Color.red);
            g.fillOval(placeX - 2, placeY - 2, 4, 4);
        } else {
            Image receiverImage;
            if(spriteDraw.hasContact && satSelected != null) {
                if(spriteDraw.meetsWith(satSelected)) {
                    if(beamAnimate == null) {
                        beamAnimate = new Thread(this);
                        beamAnimate.start();
                    }
                    beamPlace = spriteDraw;
                    beamSat = satSelected;
                    double beamAngle = getRad(450 - getAngle(satSelected, spriteDraw));
                    int satX = getXCoord(satSelected.getLong());
                    int x = (int)(Math.sin(beamAngle) * 5D);
                    int satY = getYCoord(satSelected.getLat());
                    int y = (int)(Math.cos(beamAngle) * 5D);
                    int uplinkX[] = {
                        placeX, (int)((double)placeX + beamFraction * (double)(satX - x - placeX)), (int)((double)placeX + beamFraction * (double)((satX + x) - placeX))
                    };
                    int uplinkY[] = {
                        placeY, (int)((double)placeY + beamFraction * (double)(satY - y - placeY)), (int)((double)placeY + beamFraction * (double)((satY + y) - placeY))
                    };
                    g.setColor(beamColor);
                    g.fillPolygon(uplinkX, uplinkY, 3);
                }
                receiverImage = recSignalImg;
                if(spriteDraw.important || zoom >= 5D) {
                    g.setColor(Color.white);
                    g.setFont(new Font("Arial", 1, 11));
                    
                    if (langEng)
                        g.drawString("Contact With:", placeX - 15, placeY + 32);
                    else
                        g.drawString("Contact With:", placeX - 15, placeY + 32);
                    
                    for(int i = 0; i < spriteDraw.getContacts().length; i++)
                        g.drawString(spriteDraw.getContacts()[i], placeX - 15, placeY + 43 + i * 10);
                    
                }
            } else {
                if(beamAnimate != null && (beamPlace == spriteDraw || beamSat != satSelected)) {
                    beamAnimate = null;
                    beamFraction = 0.0D;
                }
                receiverImage = recImg;
            }
            if(zoom > 2D && zoom < 4D && !spriteDraw.important) {
                double ratio = (zoom - 2D) / 2D;
                g.drawImage(receiverImage, placeX - 7, (int)((double)(placeY - 30) + (1.0D - ratio) * (double)receiverImage.getHeight(this)), (int)(ratio * (double)recImg.getWidth(this)), (int)(ratio * (double)receiverImage.getHeight(this)), this);
            } else {
                g.drawImage(receiverImage, placeX - 7, placeY - 30, this);
            }
            g.setColor(Color.white);
            g.setFont(nameFont);
            g.drawString(spriteDraw.getName(), placeX - 15, placeY + 18);
        }
    }
    private void drawRange(Graphics g, SatSprite sat) {
        if(sat.getRange() > 0) {
            if(sat == satSelected)
                g.setColor(Color.green);
            else
                if(sat.hasContact)
                    g.setColor(Color.white);
                else
                    g.setColor(new Color(180, 180, 180));
            Range satRange = sat.getRangeObj();
            int countCoords = satRange.getLongCoords().length;
            int[] XCoords = new int[countCoords];
            int[] YCoords = new int[countCoords];
            for(int i = 0; i < countCoords; i++) {
                XCoords[i] = getXCoord(satRange.getLongCoords()[i]);
                YCoords[i] = getYCoord(satRange.getLatCoords()[i]);
                if(i > 0 && Math.abs(XCoords[i] - XCoords[i - 1]) < 200)
                    g.drawLine(XCoords[i - 1], YCoords[i - 1], XCoords[i], YCoords[i]);
            }
            
            if(Math.abs(XCoords[0] - XCoords[countCoords - 1]) < 200)
                g.drawLine(XCoords[0], YCoords[0], XCoords[countCoords - 1], YCoords[countCoords - 1]);
        }
    }

    private void drawSatInfo(Graphics g, SatSprite sat) {
        
        Graphics2D g2d = (Graphics2D)g;
        if (zoom % 1 > 0) { //tells zoom while zooming
            g.setColor(Color.green);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("ZOOM: " + (int)(zoom+0.3) + "X", 15, mapWindowY -15);
        }
        
        int infoBoxX = 125;
        int infoBoxY = 125;
        int marginX = 25;
        int marginY = 25;
        
        if (satSelected != null) {
            if (satInfoFractionX < 1 && satInfoAnimateThread == null) {
                satInfoAnimateThread = new Thread(this);
                satInfoAnimateThread.start();
            }
            
            int circleX =
            (int) (infoBoxX + marginX - (infoBoxX + infoBoxX * (satInfoFractionX)) / 2);
            int circleY =
            mapWindowY - (int) ((infoBoxY + infoBoxY * (satInfoFractionY)) / 2) - marginY;
            
            //makes circle transparent
            g2d = (Graphics2D) g;
            AlphaComposite myAlpha;
            myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
            g2d.setComposite(myAlpha);
            g.setColor(Color.black);
            //draws infomation box circle with animation centered around center
            g.fillOval(circleX, circleY, (int) (infoBoxX * satInfoFractionX), (int) (infoBoxY * satInfoFractionY));
            
            g2d.setComposite(alphaOpaque);
            g.setColor(Color.white);
            g.drawOval(circleX, circleY, (int) (infoBoxX * (satInfoFractionX)), (int) (infoBoxY * (satInfoFractionY)));
            
            g.setColor(Color.blue);
            if (satInfoFractionY != 1) {
                //draws line that connects satellite to circle just for effects
                if (satInfoFractionX != 1) {
                    g.drawLine(getXCoord(satSelected.getLong()),
                    getYCoord(satSelected.getLat()),
                    circleX,
                    circleY + (int) (infoBoxY * satInfoFractionY / 2));
                    g.drawLine(
                    getXCoord(satSelected.getLong()),
                    getYCoord(satSelected.getLat()),
                    circleX + (int) (infoBoxX * satInfoFractionX),
                    circleY + (int) (infoBoxY * satInfoFractionY / 2));
                } else {
                    g.drawLine(
                    getXCoord(satSelected.getLong()),
                    getYCoord(satSelected.getLat()),
                    circleX + (int) (infoBoxX * satInfoFractionX / 2),
                    circleY);
                    g.drawLine(
                    getXCoord(satSelected.getLong()),
                    getYCoord(satSelected.getLat()),
                    circleX + (int) (infoBoxX * satInfoFractionX / 2),
                    circleY + (int) (infoBoxY * satInfoFractionY));
                }
            }
            
            if (satInfoFractionY == 1) { //starts drawing Satellite Info
                g.setFont(new Font("Arial", Font.PLAIN, 11));
                g.setColor(Color.white);
                
                
                g.drawString(
                "Longitude: " + getLongString(sat.getLong()),
                marginX + 15,
                mapWindowY - infoBoxY / 2 - 40);
                g.drawString(
                "Latitude: " + getLatString(sat.getLat()),
                marginX + 25,
                mapWindowY - infoBoxY / 2 - 55);
                
                if (langEng) {
                    g.setFont(new Font("Arial", Font.BOLD, 13));
                    g.setColor(Color.white);
                    g.drawString(
                    "Name: " + sat.getName(),
                    marginX + 10,
                    mapWindowY - infoBoxY / 2 - 20);
                    
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    
                    if (followSat) {
                        g.setColor(Color.red);
                        g.drawString("Following Satellite", mapWindowX - 125, 13);
                        g.setColor(Color.white);
                    }
                    
                    g.setFont(new Font("Arial", Font.PLAIN, 11));
                    
                    g.drawString(
                    "Velocity: " + sat.getSpeed() + "kmh",
                    marginX + 15,
                    mapWindowY - infoBoxY / 2);
                    g.drawString(
                    "Altitude: " + sat.getAltitude() + "km",
                    marginX + 25,
                    mapWindowY - infoBoxY / 2 + 15);
                }
                
                else { //for FRENCH TRANSLATION
                    g.setFont(new Font("Arial", Font.BOLD, 13));
                    g.setColor(Color.white);
                    g.drawString(
                    "Nom: " + sat.getName(),
                    marginX + 10,
                    mapWindowY - infoBoxY / 2 - 20);
                    
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    
                    if (followSat) {
                        g.setColor(Color.red);
                        g.drawString("Suivre Le Satellite", mapWindowX - 125, 13);
                        g.setColor(Color.white);
                    }
                    
                    g.drawString(
                    "Vitesse: " + sat.getSpeed() + "kmh",
                    marginX + 15,
                    mapWindowY - infoBoxY / 2);
                    g.drawString(
                    "Altitude: " + sat.getAltitude() + "kmh" ,
                    marginX + 25,
                    mapWindowY - infoBoxY / 2 + 15);
                }
            }
        } else if (satSelected == null) {
            satInfoFractionX = 0;
            satInfoFractionY = 0;
        }
    }
    private void drawSatSprite(Graphics g, SatSprite spriteDraw) {
        if(followSat && spriteDraw == satSelected)
            moveScreen(getXCoord(satSelected.getLong()) + Math.abs(offScreenX), getYCoord(satSelected.getLat()) + Math.abs(offScreenY));
        
        Graphics2D g2d = (Graphics2D)g;
        int satX = getXCoord(spriteDraw.getLong());
        int satY = getYCoord(spriteDraw.getLat());
        
        if( spriteDraw.showRange && showRange)
            drawRange(g, spriteDraw);
        if( showPath && spriteDraw == satSelected)
            drawPath(g, spriteDraw);
        
        g.setColor(Color.white);
        g.setFont(nameFont);
        
        if (spriteDraw == shuttleSprite)
            drawShuttle(g, spriteDraw);
        else if (spriteDraw.mySatImage != null)
            g.drawImage(spriteDraw.mySatImage, satX - spriteDraw.mySatImage.getWidth(this) / 2, satY - spriteDraw.mySatImage.getHeight(this) / 2, this);
        else if(spriteDraw.hasContact) {
            int angle = getAngle(getXCoord(spriteDraw.getLong()), getYCoord(spriteDraw.getLat()), getXCoord(spriteDraw.contactLong), getYCoord(spriteDraw.contactLat));
            AffineTransform at = new AffineTransform();
            g2d = (Graphics2D)g;
            at.translate(satX, satY);
            at.rotate(getRad(angle - 110));
            at.translate(-0.5D * (double)satGen.getWidth(this), -0.5D * (double)satGen.getHeight(this));
            g2d.drawImage(satGen, at, this);
        }
        else
            g.drawImage(satGen, satX - satGen.getWidth(this) / 2, satY - satGen.getHeight(this) / 2, this);
        
        if(((Sprite) (spriteDraw)).showName) {
            if(spriteDraw == satSelected) {
                if(followSat)
                    g.setColor(Color.red);
                else
                    g.setColor(Color.green);
            }
            else
                g.setColor(Color.white);
            
            g.setFont(nameFont);
            g.drawString(spriteDraw.getName(), satX - (spriteDraw.getName().length() * 3), satY + 26);
        }
        
        if (spriteDraw == satSelected) {
            g.setColor(Color.white);
            g.fillOval(satX - 2, satY - 2, 4, 4);
        }
    }
    
    private void drawPath(Graphics g, SatSprite spriteDraw) {
        if (showPath && spriteDraw.pathCreated) {
            int countCoords = spriteDraw.pathCoords[0].length;
            int[] XCoords = new int[countCoords];
            int[] YCoords = new int[countCoords];
            int dateArrayPosition = 0;
            int lineJumpChecker = (int)( 100 * zoom );
            int distanceSelect = (int)( 5 * zoom );
            
            for(int i = 0; i < countCoords; i++) {
                XCoords[i] = getXCoord(spriteDraw.pathCoords[0][i]);
                YCoords[i] = getYCoord(spriteDraw.pathCoords[1][i]);
                if ( Math.abs(mouseX - XCoords[i] ) < distanceSelect && Math.abs(mouseY - YCoords[i]) < distanceSelect ) {
                    pathSelected = true;
                    dateArrayPosition = i;
                    dateSelected = (long)spriteDraw.pathCoords[2][i];
                }
                else g.setColor(Color.red);
                
                
                if ( i > 0 && Math.abs(XCoords[i] - XCoords[i - 1]) < lineJumpChecker && Math.abs(YCoords[i] - YCoords[i - 1]) < lineJumpChecker)
                    g.drawLine(XCoords[i- 1], YCoords[i - 1], XCoords[i], YCoords[i]);
            }
            
            
            if (pathSelected) {
                g.setColor(Color.white); g.setFont(nameFont);
                Date dateAtMouse = new Date( (long)spriteDraw.pathCoords[2][dateArrayPosition]);
                int x = getXCoord(spriteDraw.pathCoords[0][dateArrayPosition] );
                int y = getYCoord(spriteDraw.pathCoords[1][dateArrayPosition] ) ;
                if ( x > 120 )
                    g.drawString( dateAtMouse.toLocaleString(), x - 120, y );
                else
                    g.drawString( dateAtMouse.toLocaleString(), 5, y );
                g.setColor(Color.red);
                g.drawOval(x - 3, y - 3, 6 , 6);
            }
            

        }
    }
    private void drawShuttle(Graphics g, SatSprite spriteDraw) {
        int satX = getXCoord(spriteDraw.getLong());
        int satY = getYCoord(spriteDraw.getLat());
        
        Graphics2D g2d;
        java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform();
        g2d = (Graphics2D) g;
        
        int imgWidth = shuttleImg.getWidth(this);
        int imgHeight = shuttleImg.getHeight(this);
        
        at.translate(satX, satY);
        at.rotate(getRad(spriteDraw.angle));
        at.translate(- (1.0 / 2) * imgWidth, - (1.0 / 2) * imgHeight);
        
        g2d.drawImage(shuttleImg, at, this);
        at.translate(0, imgHeight);
        at.scale(1, 0.2f);
        
        if (flameDrawImg == flameImg1)
            flameDrawImg = flameImg2;
        else
            flameDrawImg = flameImg1;
        
        if (spriteDraw.speed > 0) {
            //at.scale(1.0f, (float)(spriteDraw.speed / 3000) / 25 );
            g2d.drawImage(flameDrawImg, at, this);
        }
        
        //ASTRONAUT CODE
        boolean fixSat = false;
        boolean useCanadaArm = false;
        
        int i = 0;
        while (!fixSat && i < satsVect.size()) { //resets all contacts to null
            satFix = (SatSprite) satsVect.elementAt(i);
            if (satFix != spriteDraw) {
                
                int flyXTemp = getXCoord(satFix.getLong()) - satX;
                int flyYTemp = getYCoord(satFix.getLat()) - satY;
                //checks whether Canada Arm can Reach Target
                int distanceArmTemp = (int) (Math.sqrt(flyXTemp * flyXTemp + flyYTemp * flyYTemp));
                if (distanceArmTemp + 3 < armLength) {
                    armX = flyXTemp;
                    armY = flyYTemp;
                    useCanadaArm = true;
                    distanceArm = distanceArmTemp;
                }
                if (Math.abs(flyXTemp) < 50 * zoom && Math.abs(flyYTemp) < 50 * zoom) {
                    fixSat = true;
                    flyX = flyXTemp;
                    flyY = flyYTemp;
                }
            }
            i++;
        }
        
        //animates astronauts flying out and in
        if (!fixSat) {
            if (asFraction - 0.05 <= 0)
                asFraction = 0;
            else
                asFraction -= 0.03;
        } else {
            if (asFraction >= 1)
                asFraction = 1;
            else
                asFraction += 0.02;
        }
        
        //CANADA ARM CODE
        //animates arm extending
        if (!useCanadaArm) {
            if (armFraction - 0.05 <= 0)
                armFraction = 0;
            else
                armFraction -= 0.025;
        } else {
            if (armFraction >= 1)
                asFraction = 1;
            else
                armFraction += 0.025;
        }
        
        g.setColor(Color.lightGray);
        
        //CANADA ARM CODE
        if (armFraction > 0) {
            g2d = (Graphics2D) (g);
            
            //extends arm and animates it's diagonal motion
            if (armFraction < 1) {
                armX *= armFraction;
                armY *= armFraction;
            } else {
                armX -= Math.sin(asFraction2) * 3;
                armY += Math.sin(asFraction2) * 3;
            }
            
            //cosine law
            double angleBeta = (PI - Math.acos(1.0 - distanceArm * distanceArm / (0.5 * armLength * armLength))) / 2;
            double angle = getRad(getAngle(satX, satY, satX + armX, satY + armY)) - PI / 2 - angleBeta;
            int jointX = satX + armX + (int) (Math.cos(angle) * armPartLength);
            int jointY = satY + armY + (int) (Math.sin(angle) * armPartLength);
            
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(240, 240, 240));
            g2d.drawLine(satX, satY, jointX, jointY);
            
            g2d.drawLine(jointX, jointY, satX + armX, satY + armY); //makes arm rotate
            g2d.setStroke(new BasicStroke(1));
            
            g.setColor(Color.gray);
            g2d.fillOval(jointX - 2, jointY - 2, 4, 4);
        }
        
        //astroNaut Animation
        if (asFraction > 0) {
            if (asFraction < 1) {
                g.drawImage(
                anImg,
                (int) (satX + asFraction * (flyX + 22) + Math.sin(asFraction * PI) * flyY / 2 - anImg.getWidth(this) / 2),
                (int) (satY + asFraction * flyY + Math.sin(asFraction * PI) * flyX / 2 - anImg.getHeight(this) / 2),
                this);
                g.drawImage(
                anImg,
                (int) (satX + asFraction * (flyX - 13) - Math.sin(asFraction * PI) * flyY / 2 - anImg.getWidth(this) / 2),
                (int) (satY + asFraction * flyY - Math.sin(asFraction * PI) * flyX / 2 - anImg.getHeight(this) / 2),
                this);
            } else {
                asFraction2 += 0.16;
                g.drawImage(anImg, (int) (satX + flyX - anImg.getWidth(this) / 2) + 22, (int) (satY + flyY - Math.sin(asFraction2) * 5 - anImg.getHeight(this) / 2), this);
                g.drawImage(
                anImg,
                (int) (satX + flyX + anImg.getWidth(this) / 2) - 13,
                (int) (satY + flyY + Math.sin(asFraction2) * 5 - anImg.getHeight(this) / 2),
                -anImg.getWidth(this),
                anImg.getHeight(this),
                this);
                if (asFraction2 > 50)
                    asFraction2 = 1;
            }
        }
        
        if (!useCanadaArm && !fixSat) {
            g.setColor(Color.white);
            if (langEng)
                g.drawString("pilot me using the arrows", satX - 15, satY + 41);
            else
                g.drawString("pilote-moi en utilisant les directions", satX - 25, satY + 41);
        }
    }
    private void drawSprites(Graphics g, Vector satsVectDraw) {
        for(int i = 0; i < satsVectDraw.size(); i++)
            if(satsVectDraw.elementAt(i) instanceof SatSprite)
                drawSatSprite(g, (SatSprite)satsVectDraw.elementAt(i));
            else
                if(satsVectDraw.elementAt(i) instanceof Place)
                    drawPlace(g, (Place)satsVectDraw.elementAt(i));
        
    }
    
    private void drawDate(Graphics g) {
        if (zoom == 1) g.setColor(Color.black);
        else g.setColor(Color.white);
        g.setFont(nameFont);
        Date date = new Date(myData.timeJump + myData.timeNow);
        g.drawString( date.toLocaleString(), 5, 13);
        if ( Math.abs(myData.timeJump) > 0) {
            if (myData.timeJump > 0)
                g.drawString("TIME FORWARD" , 5 , 25 );
            else
                g.drawString("TIME PAST" , 5 , 25 );
        }
    }
    
    private void drawZoomCircle(Graphics g) {
        double ratio = 0;
        int circD = 30;
        
        if (gridZoom == zoomTo) {
            if (zoomTo == prevZoom) {ratio = 0;}
            else if (mapZoom < zoomTo) {
                ratio = 1.0 - (double)(zoomTo - mapZoom) / (double)(zoomTo - prevZoom);
            }
            else if (zoom < zoomTo) {
                ratio = (double)(zoomTo - zoom) / (double)(zoomTo - prevZoom);
                circD = (int)( ( (1.0 - ratio) * 2 + 1) * circD );
            }
            
            int mouseX = getXCoord(mouseClickLong);
            int mouseY = getYCoord(mouseClickLat);
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.setStroke( new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND) );
            
            g.setColor(Color.blue);
            g.drawArc( mouseX - circD / 2 , mouseY - circD / 2,
            circD , circD, 90, (int)(180 * ratio) );
            g.drawArc( mouseX - circD / 2 , mouseY - circD / 2,
            circD , circD, -90, (int)(180 * ratio) );
            
            circD = (int)( circD * 0.5 );
            g.setColor(Color.green);
            g.drawArc( mouseX - circD / 2 , mouseY - circD / 2,
            circD , circD, 90, -(int)(180 * ratio) );
            g.drawArc( mouseX - circD / 2 , mouseY - circD / 2,
            circD , circD, -90, -(int)(180 * ratio) );
            
            g2d.setStroke( new BasicStroke(1) );
        }
    }
    private int getAngle(double x1, double y1, double x2, double y2) {
        double deltaX = x1 - x2;
        double deltaY = -(y1 - y2);
        int angle = 0;
        int angleAbs = (int)Math.abs(Math.atan(deltaY / deltaX) / Sprite.degToRadConv);
        if(deltaX >= 0.0D && deltaY >= 0.0D)
            angle = 90 - angleAbs;
        else
            if(deltaX >= 0.0D && deltaY < 0.0D)
                angle = angleAbs + 90;
            else
                if(deltaX < 0.0D && deltaY < 0.0D)
                    angle = 270 - angleAbs;
                else
                    if(deltaX < 0.0D && deltaY >= 0.0D)
                        angle = angleAbs + 270;
        return angle;
    }
    private int getAngle(Sprite s1, Sprite s2) {
        return getAngle(getXCoord(s1.getLong()), getYCoord(s1.getLat()), getXCoord(s2.getLong()), getYCoord(s2.getLat()));
    }
    public double getLatCoord(int y) {
        return ((double)(-(y - mapMidY + offScreenY)) / (double)mapMidY) * 90D;
    }
    private String getLatString(double latitude) {
        if (latitude >= 0)
            return (coordNumberFormat.format(latitude) + "N");
        else return (coordNumberFormat.format(-latitude) + "S");
    }
    public double getLongCoord(int x) {
        return ((double)(x - mapMidX + offScreenX) / (double)mapMidX) * 180D;
    }
    private String getLongString(double longitude) {
        if (longitude >= 0)
            return (coordNumberFormat.format(longitude) + "E");
        else return (coordNumberFormat.format(-longitude) + "W");
    }
    private double getRad(double deg) {
        return degToRadConv * deg;
    }
    public int getXCoord(double coordLong) {
        return (int)((coordLong / 180D) * (double)mapMidX + (double)mapMidX - (double)offScreenX);
    }
    public int getYCoord(double coordLat) {
        return (int)((double)mapMidY - ((coordLat / 90D) * (double)mapMidY + (double)offScreenY));
    }
    /**
     * Invoked when a key has been pressed.
     */
    public void keyPressed(java.awt.event.KeyEvent e) {
    }
    public void keyReleased(java.awt.event.KeyEvent e) {
    }
    /**
     * Invoked when a key has been typed.
     * This event occurs when a key press is followed by a key release.
     */
    public void keyTyped(java.awt.event.KeyEvent e) {
    }
    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(java.awt.event.MouseEvent e) {
        
        boolean foundSat = false;
        boolean zoomPositive;
        
        if (!langSelected) { //selects language
            if (e.getY() < mapWindowY && e.getX() < mapWindowX) {
                if (e.getX() < (mapWindowX / 2))
                    langEng = false;
                else
                    langEng = true;
                if (animationThread == null) {
                    animationThread = new Thread(this);
                    animationThread.start();
                }
            }
        }
        else {
            if (e.isShiftDown()) //handles unzooming
                zoomPositive = false;
            else
                zoomPositive = true;
            
            switch (e.getClickCount()) {
                case 1 : {
                    if (e.getX() > mapWindowX - 80 && e.getY() > mapWindowY - 35 ) {
                        optionsDialog.show();
                        optionsDialog.pack();
                        optionsDialog.setVisible(true);
                    }
                    else if (!pathSelected) {
                        
                        for (int satCount = 0; !foundSat && satCount < satsVect.size(); satCount++) {
                            SatSprite sat = (SatSprite) satsVect.elementAt(satCount);
                            if (Math.abs(getXCoord(sat.getLong()) - e.getX()) < 30
                            && Math.abs(getYCoord(sat.getLat()) - e.getY()) < 20) {
                                
                                satSelected = sat;
                                followSat = true;
                                
                                satInfoFractionX = 0.0D;
                                satInfoFractionY = 0.0D;
                                foundSat = true;
                            }
                            //deselects satellite
                            if (!foundSat) {
                                satSelected = null;
                                followSat = false;
                            }
                        }
                    }
                    else if (pathSelected) {
                        myData.timeJump = dateSelected - myData.timeNow;
                    }
                    break;
                }
                case 2 : {
                    if (zoomerFinished) {
                        followSat = false;
                        //begins zooming algorithm
                        if ( (zoomPositive && zoomTo < 6) || (zoomTo > 1 && !zoomPositive) && !foundSat && zoomerFinished) {
                            if (zoomPositive) { //increases zoom factor
                                if (zoomTo == 1) zoomTo += 1;
                                else zoomTo += 2;
                            }
                            else if (zoomTo > 2) zoomTo -= 2; //zooms out double speed if zoom is high
                            else zoomTo -= 1;
                            
                            //starts zoom map animation
                            myZoomer = new Zoomer(this);
                            
                            mapClickX = e.getX() + Math.abs(offScreenX);
                            mapClickY = e.getY() + Math.abs(offScreenY);
                            mouseClickLat = mouseLat;
                            mouseClickLong = mouseLong;
                            //coordinates for zoom to focus on
                        }
                    }
                    break;
                }
                case 3: {
                    int x = Math.abs( e.getX() - getXCoord(-73.75) );
                    int y = Math.abs( e.getY() - getYCoord( 45.67) );
                    
                    if (shuttleCreated) {
                        shuttleCreated = false;
                        satsVect.remove(shuttleSprite);
                    }
                    
                    if ( !pathSelected && !shuttleCreated && !zoomPositive && x < 20 && y < 20) {
                        shuttleSprite = new SatSprite(getLatCoord(mapWindowY / 2), getLongCoord(mapWindowX / 2), 0, 0, 0, 0, "sat83");
                        satSelected = shuttleSprite;
                        if (shuttleThread == null) {
                            shuttleThread = new Thread(this);
                            shuttleThread.start();
                        }
                        satsVect.addElement(shuttleSprite);
                        followSat = true;
                        satInfoFractionX = 0; satInfoFractionY = 0; //resets animation for info circle
                        shuttleCreated = true;
                    }
                    else if (shuttleCreated && !zoomPositive && x < 20 && y < 20) {
                        shuttleCreated = false;
                        shuttleThread = null;
                    }
                    break;
                }
            }
        }
        repaint();
    }
    public void mouseDragged(java.awt.event.MouseEvent e) {
        followSat = false;
        
        if (zoomerFinished) {
            int x = e.getX() - mouseDownX;
            int y = e.getY() - mouseDownY;
            
            moveScreen( (mapWindowX /2) + startingOffScreenX - x, (mapWindowY / 2) - y + startingOffScreenY );
            repaint();
        }
    }
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }
    public void mouseExited(java.awt.event.MouseEvent e) {}
    public boolean mouseMove(Event evt, int x, int y) {
        mouseX = x;
        mouseY = y;
        mouseLong = getLongCoord(x);
        mouseLat = getLatCoord(y);
        return true;
        
        
    }
    public void mouseMoved(java.awt.event.MouseEvent e) {
        
        mouseX = e.getX();
        mouseY = e.getY();
        
        mouseLong = getLongCoord(e.getX());
        mouseLat = getLatCoord(e.getY());
        
        pathSelected = false; //deselects the previous path
        repaint();
    }
    public void mousePressed(java.awt.event.MouseEvent e) {
        startingOffScreenX = Math.abs(offScreenX);
        startingOffScreenY = Math.abs(offScreenY);
        mouseDownX = e.getX();
        mouseDownY = e.getY();
    }
    public void mouseReleased(java.awt.event.MouseEvent e) {}
    public void moveScreen(int centerX, int centerY) { //algorithm for moving entire screen
        offScreenX = (int)( centerX - (mapWindowX / 2) ); //zooms to center of mouse click
        // makes sure map does not go off edge
        if (offScreenX < 0) offScreenX = 0; else if (offScreenX > mapX - mapWindowX) offScreenX = mapX - mapWindowX;
        offScreenY = (int)( centerY - (mapWindowY / 2) );
        if (offScreenY < 0) offScreenY = 0; else if (offScreenY > mapY - mapWindowY) offScreenY = mapY - mapWindowY;
        
        mapPosX = (double)offScreenX / (double)mapX;
        mapPosY = (double)offScreenY / (double)mapY;
    }
    
    
    public void paintComponent(Graphics b) {
        isPainted = false;
        if (!zoomerFinished || zoom == 1) followSat = false;
        
        int x = ScreenX;
        int y = ScreenY;
        
        Color backColor = Color.black;
        
        //        if ( j != null) {
        //            //sets the windows size just right... dependent on constants
        //            x = j.getSize().width - 45;
        //            y = j.getSize().height - 415;
        //
        //            if ( x < 600 || y < 300)
        //                gridDensity = 0.5;
        //            else if ( x < 300 || y < 150)
        //                showGrid = false;
        //            else if (x > 1000 && y > 500)
        //                gridDensity = 2;
        //            else gridDensity = 1;
        //
        //            if ( y <=  x / 2 && zoomerFinished)
        //                setWindowSize(y * 2, y);
        //            else if ( y >  x / 2 && zoomerFinished)
        //                setWindowSize(x, x /2);
        //
        //            //            setWindowSize(700,350);
        //            backColor = j.getBackground();
        //            b.setColor(backColor);
        //
        //            x = j.getSize().width;
        //            y = j.getSize().height;
        //        }
        
        b.fillRect(0,0,x, y + 200);
        
        b.setColor(Color.white); b.setFont( new Font("Arial", Font.BOLD, 20) );
        b.drawString("LOADING MAP", mapWindowX / 2 - 50, 30);
        
        //draws Map
        drawMap(b);
        
        if (!zoomerFinished)
            drawZoomCircle(b);
        
        //language selection
        if (!langSelected)
            selectLang(b);
        else {
            if (showGrid) drawGrid(b);
            
            drawSprites(b, placesVect);
            drawSprites(b, satsVect);
            
            
            
            b.setColor(backColor);
            b.fillRect( 0, mapWindowY+ 15, x, y);
            b.fillRect( mapWindowX + 30, 0, x, y);
            
            //draws date
            drawDate(b);
            
            drawSatInfo(b, satSelected); //draws information about SatSprite
            //
            //Options Button
            drawOptionsButton(b);
            
            b.setColor(Color.white);
            b.setFont(new Font("Arial", Font.BOLD, 10));
            if (mouseX <= mapWindowX && mouseY <= mapWindowY) //location longitude latitude of mouse
                b.drawString( getLatString(mouseLat) + " " + getLongString(mouseLong), mouseX - 15, mouseY + 40);
        }
        
        isPainted = true;
    }
    public void removeAllPlaces() {
        placesVect.removeAllElements();
    }
    public void removeAllSatSprites() {
        satsVect.removeAllElements();
    }
    public boolean removePlace(int vectorLocation) {
        if (vectorLocation >= 0 && vectorLocation < placesVect.size()) {
            placesVect.removeElementAt(vectorLocation);
            repaint();
            return true;
        } else {
            return false;
        }
    }
    public boolean removePlace(String name) {
        for (int i = 0; i < placesVect.size(); i++) {
            Place place = (Place) placesVect.elementAt(i);
            if (place.getName() == name)
                return removePlace(i);
        }
        
        return false;
    }
    public boolean removeSatSprite(int vectorLocation) {
        if (vectorLocation >= 0 && vectorLocation < satsVect.size()) {
            satsVect.removeElementAt(vectorLocation);
            repaint();
            return true;
        } else {
            return false;
        }
    }
    public boolean removeSatSprite(String name) {
        for (int i = 0; i < satsVect.size(); i++) {
            SatSprite sat = (SatSprite) satsVect.elementAt(i);
            if (sat.getName() == name)
                return removeSatSprite(i);
        }
        
        return false;
    }
    public void run() {
        
        Thread thisThread;
        for (thisThread = Thread.currentThread(); beamAnimate == thisThread;) {
            int frames = 8;
            startBeam = false;
            if (beamFraction < 1.0D) {
                beamColor = Color.white;
                for (beamFraction = 0.0D;
                beamFraction <= 1.0D;
                beamFraction += 1.0D / (double) frames)
                    try {
                        Thread.sleep(400 / frames);
                    } catch (InterruptedException _ex) {
                    }
                    
                    beamFraction = 1.0D;
                    startBeam = true;
                    repaint();
            } else {
                if (beamColor == Color.white)
                    beamColor = new Color(220, 220, 220);
                else
                    beamColor = Color.white;
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException _ex) {
                }
                repaint();
            }
        }
        
        while (frameCountThread == thisThread) {
            frameCounter = 0;
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException _ex) {
            }
            fps = frameCounter;
        }
        
        if (nightTransitionThread == thisThread) {
            int i;
            if (nightTransition >= 1)
                i = 1; //determines direction of zoom
            else
                i = -1;
            
            if (nightTransition == 1) {
                while (nightTransition > 0) {
                    nightTransition -= 0.1;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                    }
                    repaint();
                }
                nightTransition = 0;
            }
            else if (nightTransition == 0) {
                while (nightTransition < 1) {
                    nightTransition += 0.1;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                    }
                    repaint();
                }
                nightTransition = 1;
            }
            nightTransitionThread = null;
        }
        
        if (satInfoAnimateThread == thisThread) {
            thisThread.setPriority(Thread.MAX_PRIORITY);
            while (satInfoFractionX < 1.0D) {
                satInfoFractionX += 0.1;
                repaint();
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException _ex) {
                }
            }
            satInfoFractionX = 1.0D;
            repaint();
            
            while (satInfoFractionY < 1.0D) {
                satInfoFractionY += 0.1;
                repaint();
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException _ex) {
                }
            }
            satInfoFractionY = 1.0D;
            repaint();
            
            satInfoAnimateThread = null;
        }
        
        if (animationThread == thisThread) {
            for (anFraction = 0.0D; anFraction < 1.0D;) {
                anFraction += 0.05D;
                repaint();
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException _ex) {
                }
            }
            anFraction = 1.0D;
            repaint();
            animationThread = null;
        }
        
        while (shuttleThread == thisThread) {
            shuttleSprite.moveScalar(speedTimeX/framesPerSecond);
            int shuttleX = getXCoord( shuttleSprite.getLong() );
            int shuttleY = getYCoord( shuttleSprite.getLat() );
            int currentAngle = getAngle(mouseX, mouseY, shuttleX, shuttleY);
            int angleDifference = currentAngle - shuttleSprite.angle;
            
            int distanceX = mouseX - (shuttleX );
            int distanceY = mouseY - (shuttleY );
            int distance = (int)Math.abs( Math.sqrt( distanceX * distanceX + distanceY *  distanceY) );
            
            if (angleDifference > 180 && angleDifference < 360)
                angleDifference = angleDifference - 360;
            
            if (distance > 20) {
                if (Math.abs(angleDifference) <= 2)
                    shuttleSprite.angle = currentAngle;
                else if (angleDifference >= 0)
                    shuttleSprite.angle +=2;
                else shuttleSprite.angle -= 2;
            }
            
            double angleRatio = (1.0 - (double)Math.abs(angleDifference) / 360.0);
            
            
            if ( distance <= 20)
                shuttleSprite.speed = 0;
            else if ( distance <= 70)
                shuttleSprite.speed = 6200;
            else if ( distance <= 250 ) // deceleration
                shuttleSprite.speed = 3000 + (int)(angleRatio * 12000 * distance / 250.0);
            else shuttleSprite.speed = (int)(angleRatio * 15000);
            
            repaint();
            try {
                Thread.sleep(50L);
            } catch (InterruptedException _ex) {}
        }
    }
    private void selectLang(Graphics g) {
        if (anFraction >= 0.9) {
            langSelected = true;
            setWindowSize(ScreenX - 30, ScreenY - 15);
        }
        
        if (anFraction > 0.1) {
            drawSprites(g, satsVect);
        }
        
        g2d = (Graphics2D) g;
        AlphaComposite myAlpha;
        
        if (anFraction > 0  && anFraction <= 1) {
            myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(1.0f - anFraction) );
            g2d.setComposite(myAlpha);
        }
        if (!langSelected){
            setWindowSize(ScreenX, ScreenY);
            g.setColor( new Color( 0, 0, 200));
            g.fillRect(0, ScreenY / 2 - 20, ScreenX, 40);
            //        g.setColor( new Color(235, 235, 235));
            g.setColor( Color.white );
            g.fillRect(0, ScreenY / 2 - 17, ScreenX, 34);
            
            g.setColor(Color.gray);
            g.fillRect(ScreenX / 2 - 1, ScreenY / 2 -17, 2, 34);
            
            g.setColor(Color.darkGray);
            g.setFont( new Font("Arial", Font.BOLD, 25) );
            g.drawString("FRANCAIS" , 110 , ScreenY / 2 + 9);
            g.drawString("ENGLISH" , ScreenX / 2 + 110, ScreenY / 2 + 9);
        }
        
        g2d.setComposite(alphaOpaque);
    }
    public void setWindowSize( int x, int y) {
        mapWindowX = x;
        mapWindowY = y;
        mapX = (int)(x * mapZoom);
        mapY = (int)(y * mapZoom);
        //reoptimizes screen
        moveScreen(mapWindowX / 2 + offScreenX, mapWindowY / 2 + offScreenY);
        mapMidX = (int)(mapX   / 2);
        mapMidY = (int)(mapY  / 2);
        repaint();
    }
    public void start() {
        
//        System.out.println("1");
        
        myData = new SatData();
        try {
            startReceiving();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Updater myUpdater = new Updater(this);
        
//        System.out.println("2");
        createSats(); //Creates the sats dynamically
//        System.out.println("3");
        
        
        //Run normally
        
        if (drawSprites == null) {
            drawSprites = new Thread(this);
            // drawSprites.setPriority(10);
            drawSprites.start();
        }
        
    }
    public void startReceiving() throws Exception {
        try {
            new Receiver(myData, this);
        } catch (Exception e) {  }
    }
    public void stop() {
        drawSprites = null;
        satInfoAnimateThread = null;
        frameCountThread = null;
        nightTransitionThread = null;
        shuttleThread = null;
        
    }
    public void switchNightMode() {
        if (nightTransitionThread == null) { //does not follow while in zoom animation
            nightTransitionThread = new Thread(this);
            nightTransitionThread.start();
        }
    }
    public void update(Graphics g) {
        paint(g);
    }
    public void updateContacts() {
        for (int i = 0; i < satsVect.size(); i++) {
            SatSprite sat = (SatSprite) satsVect.elementAt(i);
            sat.hasContact = false;
        }
        
        for (int i = placesVect.size() - 1; i >= 0; i--) {
            Place place = (Place) placesVect.elementAt(i);
            if (place.important)
                place.findContacts(satsVect);
        }
        
    }
    private void updateContacts(Vector sats, Vector places) {
        for (int i = 0; i < sats.size(); i++) {
            SatSprite sat = (SatSprite) sats.elementAt(i);
            sat.hasContact = false;
        }
        
        for (int i = places.size() - 1; i >= 0; i--) {
            Place place = (Place) places.elementAt(i);
            if (place.important)
                place.findContacts(sats);
        }
        
    }
    public boolean updatePosition(int vectorLocation, double longitude, double latitude, int speed, int altitude) {
        if (vectorLocation >= 0 && vectorLocation < satsVect.size()) {
            SatSprite sat = (SatSprite) satsVect.elementAt(vectorLocation);
            sat.setLong(longitude);
            sat.setLat(latitude);
            sat.setSpeed(speed);
            sat.setAltitude(altitude);
            
            int range = (int) (Math.acos(6357.0 / (6357.0 + (double) altitude)) * 6357.0);
            sat.setRange(range);
            sat.createRangeObj();
            
            repaint();
            return true;
        }
        else return false;
    }
    public boolean updatePosition(String satName, double longitude, double latitude, int speed, int altitude) {
        for (int i = 0; i < satsVect.size(); i++) {
            SatSprite sat = (SatSprite) satsVect.elementAt(i);
            if (sat.getName().equals(satName))
                return updatePosition(i, longitude, latitude, speed, altitude);
        }
        return false;
    }
    
    
    //updating class specifically used for satData
    public boolean updatePosition(SatDataToSend satData) {
        for(int i = 0; i < satsVect.size(); i++) {
            SatSprite sat = (SatSprite)satsVect.elementAt(i);
            if ( sat.getName().equals( satData.getName() ) )
                return updatePosition(i, satData);
        }
        return false;
    }
    
    public boolean updatePosition(int vectorLocation, SatDataToSend newData) {
        if(vectorLocation >= 0 && vectorLocation < satsVect.size()) {
            SatSprite sat = (SatSprite)satsVect.elementAt(vectorLocation);
            sat.setAltitude( newData.getAlt() );
            sat.setSpeed( newData.getSpeed() );
            double lat= newData.getLat();
            double longi = newData.getLongi();
            if(longi < 180L) longi = -longi;
            else if(longi > 180L) longi = 360L - longi;
            
            sat.setLong(longi);
            sat.setLat(lat);
            if (newData.pathCreated) {
                myData.pathsReceivedBySatMapper = true;
                sat.setPathCoords( newData.pathCoords );
            }
            sat.createRangeObj();
            
            return true;
        } else {
            return false;
        }
    }
    
    public void updateSat(Satellite myCurrentSat) {
        
        if (followSat && satSelected != null)
            moveScreen(getXCoord(satSelected.getLong()) + Math.abs(offScreenX), getYCoord(satSelected.getLat()) + Math.abs(offScreenY));
        
        int alt;
        int speed;
        double lat;
        double longi;
        
        alt = myCurrentSat.getAltitudeKm();
        speed = myCurrentSat.getVelocityKmH();
        lat = myCurrentSat.getLatitude();
        longi = myCurrentSat.getLongitude();
        if (longi < 180L)
            longi = -longi;
        else
            if (longi > 180L)
                longi = 360L - longi;
        
        updatePosition(myCurrentSat.getName(), longi, lat, speed, alt);
        updateContacts();
        repaint();
        
    }
}
