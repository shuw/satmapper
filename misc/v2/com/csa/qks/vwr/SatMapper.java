package com.csa.qks.vwr;

import com.csa.qks.cont.SatDataToSend;
import java.applet.Applet;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.PrintStream;
import java.util.*;

// Referenced classes of package com.csa.qks.vwr:
//            Place, SatSprite, SatData, Sprite, 
//            Range, Zoomer, Updater, Receiver

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
        //used for  map zooming
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
    
    SatData myData;
    Zoomer myZoomer;
    Image satGen;
    Image satAlou;
    Image satRad;
    Image satIss;
    Image satEnvi;
    Image satRotateImgs[];
    
    public int main() { return 1; }
     public void init() {
        satsVect.removeAllElements();
        setSize(750, 650);
        offscreenImage = createImage(getSize().width, getSize().height);
        offscreen = offscreenImage.getGraphics();
        g2d = (Graphics2D) offscreen;
        mapImg = getImage(getDocumentBase(), "mapHiRes.jpg");
        mapNightImg = getImage(getDocumentBase(), "mapNight.jpg");
        mapCloudsImg = getImage(getDocumentBase(), "mapClouds.jpg");
        mapImg = getImage(getDocumentBase(), "Map.jpg");
        satGen = getImage(getDocumentBase(), "SatGeneric.gif");
        satRad = getImage(getDocumentBase(), "envisat.gif");
        satIss = getImage(getDocumentBase(), "iss.gif");
        satAlou = getImage(getDocumentBase(), "alouette.gif");
        satEnvi = getImage(getDocumentBase(), "envisat.gif");
        shuttleImg = getImage(getDocumentBase(), "Shuttle.gif");
        recImg = getImage(getDocumentBase(), "Receiver.gif");
        recSignalImg = getImage(getDocumentBase(), "ReceiverSignal.gif");
        String satRotateImgsString[] =
            {
                "sat0.gif",
                "sat9.gif",
                "sat18.gif",
                "sat27.gif",
                "sat36.gif",
                "sat45.gif",
                "sat54.gif",
                "sat63.gif",
                "sat72.gif",
                "sat81.gif",
                "sat90.gif" };
        satRotateImgs = new Image[satRotateImgsString.length];
        for (int i = 0; i < satRotateImgsString.length; i++)
            satRotateImgs[i] = getImage(getDocumentBase(), satRotateImgsString[i]);

        System.out.println("\nCodeBase: " + getCodeBase());
        placesVect.addElement(
            new Place(45.469999999999999D, -73.75D, "Montreal", true));
        placesVect.addElement(new Place(45.32D, -75.670000000000002D, "Ottawa"));
        placesVect.addElement(new Place(43.68D, -79.629999999999995D, "Toronto"));
        placesVect.addElement(new Place(51.100000000000001D, -114.02D, "Calgary"));
        placesVect.addElement(
            new Place(48.420000000000002D, -123.31999999999999D, "Victoria"));
        placesVect.addElement(
            new Place(49.899999999999999D, -97.230000000000004D, "Winnipeg"));
        placesVect.addElement(
            new Place(45.869999999999997D, -66.530000000000001D, "Fredericton"));
        placesVect.addElement(new Place(47.619999999999997D, -52.75D, "St. John's"));
        placesVect.addElement(new Place(62.469999999999999D, -114.45D, "YellowKnife"));
        placesVect.addElement(new Place(44.649999999999999D, -63.57D, "Halifax"));
        placesVect.addElement(
            new Place(46.280000000000001D, -63.130000000000003D, "Charlottetown"));
        placesVect.addElement(
            new Place(46.799999999999997D, -71.379999999999995D, "Quebec"));
        placesVect.addElement(new Place(50.520000000000003D, -104.67D, "Regina"));
        placesVect.addElement(
            new Place(60.719999999999999D, -135.06999999999999D, "Whitehorse"));
        placesVect.addElement(
            new Place(74.719999999999999D, -94.980000000000004D, "Resolute"));
        placesVect.addElement(new Place(20D, -90D, "Place1", true));
        placesVect.addElement(new Place(40D, 100D, "Place2", true));
        placesVect.addElement(new Place(-28D, 123D, "Place3", true));
        myData = new SatData();
        startReceiving(myData);

    }
    
   
    public void addPlace(double latitude, double longitude, String name)
    {
        placesVect.addElement(new Place(latitude, longitude, name));
    }
    public void addSatSprite(double latitude, double longitude, int losRange, int altitude, String name)
    {
        satsVect.addElement(new SatSprite(latitude, longitude, losRange, altitude, 0, 0, name));
    }
    public void addSatSprite(double latitude, double longitude, String name)
    {
        satsVect.addElement(new SatSprite(latitude, longitude, name));
    }
    public void calcZoom(double zoom, int mapClickX, int mapClickY)
    {
        mapX = (int)(zoom * 700D);
        mapY = (int)(zoom * 350D);
        offScreenX = -(int)((zoom / prevZoom) * ((double)mapClickX - (double)prevMapX / zoom / 2D));
        if(offScreenX > 0)
            offScreenX = 0;
        else
        if(offScreenX < 700 - mapX)
            offScreenX = 700 - mapX;
        offScreenY = -(int)((zoom / prevZoom) * ((double)mapClickY - (double)prevMapY / zoom / 2D));
        if(offScreenY > 0)
            offScreenY = 0;
        else
        if(offScreenY < 350 - mapY)
            offScreenY = 350 - mapY;
        mapMidX = mapX / 2;
        mapMidY = mapY / 2;
    }
    public void createSats()
    {
        SatDataToSend newData;
        int alt;
        int speed;
        long lat;
        long longi;
        for(Enumeration enum = myData.getSats().elements(); enum.hasMoreElements(); satsVect.addElement(new SatSprite(lat, longi, 3000, alt, speed, 175, newData.getName())))
        {
            newData = (SatDataToSend)enum.nextElement();
            alt = newData.getAlt();
            speed = newData.getSpeed();
            lat = newData.getLat();
            longi = newData.getLongi();
            if(longi < 180L)
                longi = -longi;
            else
            if(longi > 180L)
                longi = 360L - longi;
            System.out.println(newData.getName() + "  LAT: " + lat + "  LONG:  " + longi + "  Altitude: " + alt + " Speed: " + speed);
        }

    }
    public void destroy()
    {
        offscreen.dispose();
    }
    private void drawGrid(Graphics g)
    {
        int GridX = (int)(gridZoom * 6D);
        int GridY = GridX / 2;
        double intervalDegLong = 180 / GridX;
        double intervalDegLat = 90 / GridY;
        double degLat = 0.0D;
        double degLong = 0.0D;
        g.setColor(Color.gray);
        g.setFont(gridFont);
        for(int i = 0; i < GridX; i++)
        {
            int xE = getXCoord(degLong);
            int xW = getXCoord(-degLong);
            if(xE <= 700)
            {
                g.drawLine(xE, 0, xE, 350);
                g.drawString("" + degLong + "E", xE - 12, 362);
            }
            if(i > 0 && xW <= 700)
            {
                g.drawLine(xW, 0, xW, 350);
                g.drawString("" + degLong + "W", xW - 12, 362);
            }
            degLong += intervalDegLong;
        }

        for(int i = 0; i < GridY; i++)
        {
            int yN = getYCoord(degLat);
            int yS = getYCoord(-degLat);
            if(yN < 350)
            {
                g.drawLine(0, yN, 700, yN);
                g.drawString("" + degLat + "N", 710, yN + 5);
            }
            if(i > 0 && yS <= 350)
            {
                g.drawLine(0, yS, 700, yS);
                g.drawString("" + degLat + "S", 710, yS + 5);
            }
            degLat += intervalDegLat;
        }

    }
    private void drawMap(Graphics g)
    {
        AlphaComposite myAlpha;
        if(mapZoom < 2D || mapZoom > 2D || removeClouds)
        {
            if(nightTransition < 1.0D)
                g.drawImage(mapImg, mapOffScreenX, mapOffScreenY, (int)(700D * mapZoom), (int)(350D * mapZoom), this);
            if(nightTransition > 0.0D && nightTransition < 1.0D)
            {
                myAlpha = AlphaComposite.getInstance(3, (float)nightTransition);
                g2d.setComposite(myAlpha);
            }
            if(nightTransition > 0.0D)
                g.drawImage(mapNightImg, mapOffScreenX, mapOffScreenY, (int)(700D * mapZoom), (int)(350D * mapZoom), this);
        }
        if(mapZoom >= 2D && mapZoom < 4D && !removeClouds)
        {
            if(mapZoom > 2D)
            {
                myAlpha = AlphaComposite.getInstance(3, (float)(1.0D - (mapZoom - 2D) / 2D));
                g2d.setComposite(myAlpha);
            }
            g.drawImage(mapCloudsImg, mapOffScreenX, mapOffScreenY, (int)(700D * mapZoom), (int)(350D * mapZoom), this);
        }
        myAlpha = AlphaComposite.getInstance(3, 1.0F);
        g2d.setComposite(myAlpha);
    }
    private void drawPlace(Graphics g, Place spriteDraw)
    {
        g.setColor(Color.white);
        int placeX = getXCoord(spriteDraw.getLong());
        int placeY = getYCoord(spriteDraw.getLat());
        if(zoom <= 2D && !spriteDraw.important)
        {
            g.setColor(Color.red);
            g.fillOval(placeX - 2, placeY - 2, 4, 4);
        } else
        {
            Image receiverImage;
            if(spriteDraw.hasContact && satSelected != null)
            {
                if(spriteDraw.meetsWith(satSelected))
                {
                    if(beamAnimate == null)
                    {
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
                if(spriteDraw.important || zoom >= 5D)
                {
                    g.setColor(Color.white);
                    g.setFont(new Font("Arial", 1, 11));
                    g.drawString("Contact With:", placeX - 15, placeY + 32);
                    for(int i = 0; i < spriteDraw.getContacts().length; i++)
                        g.drawString(spriteDraw.getContacts()[i], placeX - 15, placeY + 43 + i * 10);

                }
            } else
            {
                if(beamAnimate != null && (beamPlace == spriteDraw || beamSat != satSelected))
                {
                    beamAnimate = null;
                    beamFraction = 0.0D;
                }
                receiverImage = recImg;
            }
            if(zoom > 2D && zoom < 4D && !spriteDraw.important)
            {
                double ratio = (zoom - 2D) / 2D;
                g.drawImage(receiverImage, placeX - 7, (int)((double)(placeY - 30) + (1.0D - ratio) * (double)receiverImage.getHeight(this)), (int)(ratio * (double)recImg.getWidth(this)), (int)(ratio * (double)receiverImage.getHeight(this)), this);
            } else
            {
                g.drawImage(receiverImage, placeX - 7, placeY - 30, this);
            }
            g.setColor(Color.white);
            g.setFont(nameFont);
            g.drawString(spriteDraw.getName(), placeX - 15, placeY + 18);
        }
    }
    private void drawRange(Graphics g, SatSprite sat)
    {
        if(sat.getRange() > 0)
        {
            if(sat == satSelected)
                g.setColor(Color.green);
            else
            if(sat.hasContact)
                g.setColor(Color.white);
            else
                g.setColor(new Color(180, 180, 180));
            Range satRange = sat.getRangeObj();
            int countCoords = satRange.getLongCoords().length;
            int XCoords[] = new int[countCoords];
            int YCoords[] = new int[countCoords];
            for(int i = 0; i < countCoords; i++)
            {
                XCoords[i] = getXCoord(satRange.getLongCoords()[i]);
                YCoords[i] = getYCoord(satRange.getLatCoords()[i]);
                if(i > 0 && Math.abs(XCoords[i] - XCoords[i - 1]) < 200)
                    g.drawLine(XCoords[i - 1], YCoords[i - 1], XCoords[i], YCoords[i]);
            }

            if(Math.abs(XCoords[0] - XCoords[countCoords - 1]) < 200)
                g.drawLine(XCoords[0], YCoords[0], XCoords[countCoords - 1], YCoords[countCoords - 1]);
        }
    }
    private void drawSatInfo(Graphics g, SatSprite sat)
    {
        if(zoom % 1.0D > 0.0D)
        {
            g.setColor(Color.white);
            g.setFont(new Font("Arial", 1, 20));
            g.drawString("ZOOM: " + zoom + "X", 200, 175);
        }
        int infoBoxX = 125;
        int infoBoxY = 125;
        int marginX = 25;
        int marginY = 25;
        if(satSelected != null)
        {
            if(satInfoFractionX < 1.0D)
                if(satInfoAnimateThread == null)
                {
                    satInfoAnimateThread = new Thread(this);
                    satInfoAnimateThread.start();
                } else
                {
                    satInfoAnimateThread = null;
                }
            int circleX = (int)((double)(infoBoxX + marginX) - ((double)infoBoxX + (double)infoBoxX * satInfoFractionX) / 2D);
            int circleY = 350 - (int)(((double)infoBoxY + (double)infoBoxY * satInfoFractionY) / 2D) - marginY;
            g2d = (Graphics2D)g;
            AlphaComposite myAlpha = AlphaComposite.getInstance(3, 0.2F);
            g2d.setComposite(myAlpha);
            g.setColor(Color.black);
            g.fillOval(circleX, circleY, (int)((double)infoBoxX * satInfoFractionX), (int)((double)infoBoxY * satInfoFractionY));
            g2d.setComposite(alphaOpaque);
            g.setColor(Color.white);
            g.drawOval(circleX, circleY, (int)((double)infoBoxX * satInfoFractionX), (int)((double)infoBoxY * satInfoFractionY));
            g.setColor(Color.blue);
            if(satInfoFractionY != 1.0D)
                if(satInfoFractionX != 1.0D)
                {
                    g.drawLine(getXCoord(satSelected.getLong()), getYCoord(satSelected.getLat()), circleX, circleY + (int)(((double)infoBoxY * satInfoFractionY) / 2D));
                    g.drawLine(getXCoord(satSelected.getLong()), getYCoord(satSelected.getLat()), circleX + (int)((double)infoBoxX * satInfoFractionX), circleY + (int)(((double)infoBoxY * satInfoFractionY) / 2D));
                } else
                {
                    g.drawLine(getXCoord(satSelected.getLong()), getYCoord(satSelected.getLat()), circleX + (int)(((double)infoBoxX * satInfoFractionX) / 2D), circleY);
                    g.drawLine(getXCoord(satSelected.getLong()), getYCoord(satSelected.getLat()), circleX + (int)(((double)infoBoxX * satInfoFractionX) / 2D), circleY + (int)((double)infoBoxY * satInfoFractionY));
                }
            if(satInfoFractionY == 1.0D)
                if(langEng)
                {
                    g.setFont(new Font("Arial", 1, 13));
                    g.setColor(Color.white);
                    String name = sat.getName();
                    if(name.length() > 8)
                        g.setFont(new Font("Arial", 0, 8));
                    g.drawString("Name: " + name, marginX + 10, 350 - infoBoxY / 2 - 20);
                    g.setFont(new Font("Arial", 0, 12));
                    if(followSat)
                    {
                        g.setColor(Color.red);
                        g.drawString("Following Satellite", 575, 340);
                        g.setColor(Color.white);
                    }
                    g.drawString("Longitude: " + (int)sat.getLong(), marginX + 15, 350 - infoBoxY / 2 - 40);
                    g.drawString("Latitude: " + (int)sat.getLat(), marginX + 25, 350 - infoBoxY / 2 - 55);
                    g.drawString("Altitude: " + sat.getAltitude(), marginX + 15, 350 - infoBoxY / 2);
                    g.drawString("Velocity: " + sat.getSpeed(), marginX + 25, (350 - infoBoxY / 2) + 15);
                } else
                {
                    g.setFont(new Font("Arial", 1, 13));
                    g.setColor(Color.white);
                    String name = sat.getName();
                    if(name.length() > 8)
                        g.setFont(new Font("Arial", 0, 8));
                    g.drawString("Nom: " + name, marginX + 10, 350 - infoBoxY / 2 - 20);
                    g.setFont(new Font("Arial", 0, 12));
                    if(followSat)
                    {
                        g.setColor(Color.red);
                        g.drawString("Suivre Le Satellite", 575, 340);
                        g.setColor(Color.white);
                    }
                    g.drawString("Longitude: " + (int)sat.getLong(), marginX + 15, 350 - infoBoxY / 2 - 40);
                    g.drawString("Latitude: " + (int)sat.getLat(), marginX + 25, 350 - infoBoxY / 2 - 55);
                    g.drawString("Altitude: " + sat.getAltitude(), marginX + 15, 350 - infoBoxY / 2);
                    g.drawString("V\351locit\351: " + sat.getRange(), marginX + 25, (350 - infoBoxY / 2) + 15);
                }
        } else
        if(satSelected == null)
        {
            satInfoFractionX = 0.0D;
            satInfoFractionY = 0.0D;
        }
    }
    private void drawSatSprite(Graphics g, SatSprite spriteDraw)
    {
        int satX = getXCoord(spriteDraw.getLong());
        int satY = getYCoord(spriteDraw.getLat());
        if(spriteDraw.showRange)
            drawRange(g, spriteDraw);
        g.setColor(Color.white);
        g.setFont(nameFont);
        g.setColor(Color.white);
        g.fillOval(satX - 2, satY - 2, 4, 4);
        if(spriteDraw.hasContact)
        {
            int angle = getAngle(getXCoord(spriteDraw.getLong()), getYCoord(spriteDraw.getLat()), getXCoord(spriteDraw.contactLong), getYCoord(spriteDraw.contactLat));
            AffineTransform at = new AffineTransform();
            g2d = (Graphics2D)g;
            at.translate(satX, satY);
            at.rotate(getRad(angle - 110));
            at.translate(-0.5D * (double)satGen.getWidth(this), -0.5D * (double)satGen.getHeight(this));
            g2d.drawImage(satGen, at, this);
        } else
        if(spriteDraw.getName() == "sat83")
        {
            AffineTransform at = new AffineTransform();
            g2d = (Graphics2D)g;
            at.translate(satX, satY);
            at.rotate(getRad(((Sprite) (spriteDraw)).angle));
            at.translate(-0.5D * (double)shuttleImg.getWidth(this), -0.5D * (double)shuttleImg.getHeight(this));
            g2d.drawImage(shuttleImg, at, this);
            g.drawString("pilot me using the arrows", satX - 15, satY + 41);
        } else
        {
            g.drawImage(satGen, satX - satGen.getWidth(this) / 2, satY - satGen.getHeight(this) / 2, this);
        }
        if(((Sprite) (spriteDraw)).showName)
        {
            if(spriteDraw == satSelected)
            {
                if(followSat)
                    g.setColor(Color.red);
                else
                    g.setColor(Color.green);
            } else
            {
                g.setColor(Color.white);
            }
            g.setFont(nameFont);
            g.drawString(spriteDraw.getName(), satX - 15, satY + 26);
        }
    }
    private void drawSprites(Graphics g, Vector satsVectDraw)
    {
        for(int i = 0; i < satsVectDraw.size(); i++)
            if(satsVectDraw.elementAt(i) instanceof SatSprite)
                drawSatSprite(g, (SatSprite)satsVectDraw.elementAt(i));
            else
            if(satsVectDraw.elementAt(i) instanceof Place)
                drawPlace(g, (Place)satsVectDraw.elementAt(i));

    }
    private int getAngle(double x1, double y1, double x2, double y2)
    {
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
    private int getAngle(Sprite s1, Sprite s2)
    {
        return getAngle(getXCoord(s1.getLong()), getYCoord(s1.getLat()), getXCoord(s2.getLong()), getYCoord(s2.getLat()));
    }
    public double getLatCoord(int y)
    {
        return ((double)(-(y - mapMidY - offScreenY)) / (double)mapMidY) * 90D;
    }
    public double getLongCoord(int x)
    {
        return ((double)(x - mapMidX - offScreenX) / (double)mapMidX) * 180D;
    }
    private double getRad(double deg)
    {
        return degToRadConv * deg;
    }
    public int getXCoord(double coordLong)
    {
        return (int)((coordLong / 180D) * (double)mapMidX + (double)mapMidX + (double)offScreenX);
    }
    public int getYCoord(double coordLat)
    {
        return (int)((double)mapMidY - ((coordLat / 90D) * (double)mapMidY - (double)offScreenY));
    }
   
    public boolean keyDown(Event evt, int key) {
        switch (key) {
            case Event.ENTER:{
                if (followSat == false && zoomMapThread == null) //does not follow while in zoom animation
                    followSat = true;
                else followSat = false;
                break;
            }
            case Event.ESCAPE:{
                if (nightTransitionThread == null) { //does not follow while in zoom animation
                    nightTransitionThread = new Thread(this);
                    nightTransitionThread.start();
                }
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
   
    public synchronized boolean mouseDown(Event evt, int x, int y)
    {
        boolean foundSat = false;
        if(!langSelected)
        {
            if(y < 350 && x < 700)
            {
                if(x < 350)
                    langEng = false;
                else
                    langEng = true;
                if(animationThread == null)
                {
                    animationThread = new Thread(this);
                    animationThread.start();
                }
            }
        } else
        {
            boolean zoomPositive;
            if(evt.shiftDown() || evt.controlDown())
                zoomPositive = false;
            else
                zoomPositive = true;
            switch(evt.clickCount)
            {
            default:
                break;

            case 2: // '\002'
                followSat = false;
                if((zoomPositive && zoomTo < 6 || zoomTo > 1 && !zoomPositive) && !followSat && zoomMapThread == null)
                {
                    if(zoomPositive)
                    {
                        if(zoomPositive && zoomTo == 1)
                            zoomTo++;
                        else
                            zoomTo += 2;
                    } else
                    if(zoomTo > 2)
                        zoomTo -= 2;
                    else
                        zoomTo--;
                    mapClickX = x + Math.abs(offScreenX);
                    mapClickY = y + Math.abs(offScreenY);
                    myZoomer = new Zoomer(this);
                }
                break;

            case 1: // '\001'
                for(int satCount = 0; !foundSat && satCount < satsVect.size(); satCount++)
                {
                    SatSprite sat = (SatSprite)satsVect.elementAt(satCount);
                    if(Math.abs(getXCoord(sat.getLong()) - x) < 30 && Math.abs(getYCoord(sat.getLat()) - y) < 20)
                    {
                        if(satSelected == sat)
                        {
                            satSelected = null;
                            followSat = false;
                        } else
                        {
                            satSelected = sat;
                        }
                        satInfoFractionX = 0.0D;
                        satInfoFractionY = 0.0D;
                        foundSat = true;
                    }
                }

                break;
            }
        }
        repaint();
        return true;
    }
    public boolean mouseMove(Event evt, int x, int y)
    {
        mouseX = x;
        mouseY = y;
        mouseLong = getLongCoord(x);
        mouseLat = getLatCoord(y);
        return true;
    }
    public void moveScreen(int centerX, int centerY)
    {
        offScreenX = -(centerX - 350);
        if(offScreenX > 0)
            offScreenX = 0;
        else
        if(offScreenX < 700 - mapX)
            offScreenX = 700 - mapX;
        offScreenY = -(centerY - 175);
        if(offScreenY > 0)
            offScreenY = 0;
        else
        if(offScreenY < 350 - mapY)
            offScreenY = 350 - mapY;
        mapOffScreenX = offScreenX;
        mapOffScreenY = offScreenY;
    }
    public void paint(Graphics g)
    {
        if(zoom == 1.0D)
            followSat = false;
        offscreen.setColor(backColor);
        offscreen.fillRect(0, 0, getSize().width, getSize().height);
        drawMap(offscreen);
        offscreen.fillRect(700, 0, 750, 650);
        offscreen.fillRect(0, 350, 750, 650);
        drawGrid(offscreen);
        drawSprites(offscreen, placesVect);
        drawSprites(offscreen, satsVect);
        offscreen.setColor(backColor);
        offscreen.fillRect(730, 0, 750, 650);
        offscreen.fillRect(0, 363, 750, 650);
        drawSatInfo(offscreen, satSelected);
        offscreen.setColor(Color.orange);
        offscreen.setFont(gridFont);
        offscreen.drawString(DateNow.toString(), 20, 375);
        offscreen.setColor(Color.white);
        offscreen.drawString("FPS: " + fps, 20, 390);
        offscreen.setColor(Color.white);
        offscreen.setFont(new Font("Arial", 1, 10));
        if(mouseX <= 700 && mouseY <= 350)
            offscreen.drawString("Lat:" + (int)mouseLat + " Long:" + (int)mouseLong, mouseX - 15, mouseY + 40);
        if(!langSelected)
            selectLang(offscreen);
        g.drawImage(offscreenImage, 0, 0, this);
        frameCounter++;
    }
    public void removeAllPlaces()
    {
        placesVect.removeAllElements();
    }
    public void removeAllSatSprites()
    {
        satsVect.removeAllElements();
    }
    public boolean removePlace(int vectorLocation)
    {
        if(vectorLocation >= 0 && vectorLocation < placesVect.size())
        {
            placesVect.removeElementAt(vectorLocation);
            repaint();
            return true;
        } else
        {
            return false;
        }
    }
    public boolean removePlace(String name)
    {
        for(int i = 0; i < placesVect.size(); i++)
        {
            Place place = (Place)placesVect.elementAt(i);
            if(place.getName() == name)
                return removePlace(i);
        }

        return false;
    }
    public boolean removeSatSprite(int vectorLocation)
    {
        if(vectorLocation >= 0 && vectorLocation < satsVect.size())
        {
            satsVect.removeElementAt(vectorLocation);
            repaint();
            return true;
        } else
        {
            return false;
        }
    }
    public boolean removeSatSprite(String name)
    {
        for(int i = 0; i < satsVect.size(); i++)
        {
            SatSprite sat = (SatSprite)satsVect.elementAt(i);
            if(sat.getName() == name)
                return removeSatSprite(i);
        }

        return false;
    }
    public void run()
    {
        Updater myUpdater = new Updater(this);
    Thread newThread = new Thread(myUpdater);
    newThread.start();
        //new Thread(myUpdater);
        Thread thisThread;
        for(thisThread = Thread.currentThread(); beamAnimate == thisThread;)
        {
            int frames = 8;
            startBeam = false;
            if(beamFraction < 1.0D)
            {
                beamColor = Color.white;
                for(beamFraction = 0.0D; beamFraction <= 1.0D; beamFraction += 1.0D / (double)frames)
                    try
                    {
                        Thread.sleep(400 / frames);
                    }
                    catch(InterruptedException _ex) { }

                beamFraction = 1.0D;
                startBeam = true;
                repaint();
            } else
            {
                if(beamColor == Color.white)
                    beamColor = new Color(220, 220, 220);
                else
                    beamColor = Color.white;
                try
                {
                    Thread.sleep(200L);
                }
                catch(InterruptedException _ex) { }
                repaint();
            }
        }

        while(frameCountThread == thisThread) 
        {
            frameCounter = 0;
            try
            {
                Thread.sleep(1000L);
            }
            catch(InterruptedException _ex) { }
            fps = frameCounter;
        }
        while(updateContactsThread == thisThread) 
        {
            updateContacts(satsVect, placesVect);
            try
            {
                Thread.sleep(100L);
            }
            catch(InterruptedException _ex) { }
            repaint();
        }
        while(clock == thisThread) 
        {
            DateNow = new Date();
            try
            {
                Thread.sleep(1000L);
            }
            catch(InterruptedException _ex) { }
        }
        if(nightTransitionThread == thisThread)
        {
            if(nightTransition < 1.0D);
            if(nightTransition == 1.0D)
            {
                while(nightTransition > 0.0D) 
                {
                    nightTransition -= 0.10000000000000001D;
                    try
                    {
                        Thread.sleep(300L);
                    }
                    catch(InterruptedException _ex) { }
                    repaint();
                }
                nightTransition = 0.0D;
            } else
            if(nightTransition == 0.0D)
            {
                while(nightTransition < 1.0D) 
                {
                    nightTransition += 0.10000000000000001D;
                    try
                    {
                        Thread.sleep(300L);
                    }
                    catch(InterruptedException _ex) { }
                    repaint();
                }
                nightTransition = 1.0D;
            }
            nightTransitionThread = null;
        }
        if(satInfoAnimateThread == thisThread)
        {
            while(satInfoFractionX < 1.0D) 
            {
                satInfoFractionX += 0.050000000000000003D;
                try
                {
                    Thread.sleep(50L);
                }
                catch(InterruptedException _ex) { }
            }
            satInfoFractionX = 1.0D;
            while(satInfoFractionY < 1.0D) 
            {
                satInfoFractionY += 0.050000000000000003D;
                try
                {
                    Thread.sleep(50L);
                }
                catch(InterruptedException _ex) { }
            }
            satInfoFractionY = 1.0D;
            satInfoAnimateThread = null;
        }
        if(animationThread == thisThread)
        {
            for(anFraction = 0.0D; anFraction < 1.0D;)
            {
                anFraction += 0.050000000000000003D;
                try
                {
                    Thread.sleep(50L);
                }
                catch(InterruptedException _ex) { }
            }

            anFraction = 1.0D;
            animationThread = null;
        }
        
    }
    private void selectLang(Graphics g)
    {
        if(anFraction == 1.0D)
            langSelected = true;
        int screenMoveX = (int)(anFraction * 750D);
        calcZoom(anFraction, 0, 0);
        g.setColor(backColor);
        g.fillRect(screenMoveX, 0, getSize().width, getSize().height);
        g.setColor(new Color(0, 0, 175));
        g.fillRoundRect(screenMoveX, 0, 750, 650, 50, 50);
        g.setColor(new Color(235, 235, 235));
        g.fillRoundRect(5 + screenMoveX, 5, 740, 640, 50, 50);
        g.setColor(Color.gray);
        g.fillRect(374 + screenMoveX, 0, 2, 650);
        g.setColor(Color.darkGray);
        g.setFont(new Font("Arial", 1, 25));
        g.drawString("FRANCAIS", 110 + screenMoveX, 325);
        g.drawString("ENGLISH", 485 + screenMoveX, 325);
    }
    public void start()
    {
        createSats();
        if(drawSprites == null)
        {
            drawSprites = new Thread(this);
           // drawSprites.setPriority(10);
            drawSprites.start();
        }
        if(clock == null)
        {
            clock = new Thread(this);
            //clock.setDaemon(true);
            clock.start();
        }
        if(frameCountThread == null)
        {
            frameCountThread = new Thread(this);
           // frameCountThread.setDaemon(true);
            frameCountThread.start();
        }
        if(updateContactsThread == null)
        {
            updateContactsThread = new Thread(this);
           // updateContactsThread.setDaemon(true);
            updateContactsThread.start();
        }
    }
    public void startReceiving(SatData myData)
    {
        try
        {
            new Receiver(myData);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void stop()
    {
        drawSprites = null;
        clock = null;
        frameCountThread = null;
        updateContactsThread = null;
        zoomMapThread = null;
        nightTransitionThread = null;
    }
    public void update(Graphics g)
    {
        paint(g);
    }
    private void updateContacts(Vector sats, Vector places)
    {
        for(int i = 0; i < sats.size(); i++)
        {
            SatSprite sat = (SatSprite)sats.elementAt(i);
            sat.hasContact = false;
        }

        for(int i = places.size() - 1; i >= 0; i--)
        {
            Place place = (Place)places.elementAt(i);
            if(place.important)
                place.findContacts(sats);
        }

    }
    public boolean updatePosition(int vectorLocation, double longitude, double latitude, int speed, int altitude)
    {
        if(vectorLocation >= 0 && vectorLocation < satsVect.size())
        {
            SatSprite sat = (SatSprite)satsVect.elementAt(vectorLocation);
            sat.setLong(longitude);
            sat.setLat(latitude);
            sat.setSpeed(speed);
            sat.setAltitude(altitude);
            if(followSat)
                moveScreen(getXCoord(satSelected.getLong()) + Math.abs(offScreenX), getYCoord(satSelected.getLat()) + Math.abs(offScreenY));
            repaint();
            return true;
        } else
        {
            return false;
        }
    }
    public boolean updatePosition(String satName, double longitude, double latitude, int speed, int altitude)
    {
        for(int i = 0; i < satsVect.size(); i++)
        {
            SatSprite sat = (SatSprite)satsVect.elementAt(i);
            if(sat.getName().equals(satName))
                return updatePosition(i, longitude, latitude, speed, altitude);
        }

        return false;
    }
}