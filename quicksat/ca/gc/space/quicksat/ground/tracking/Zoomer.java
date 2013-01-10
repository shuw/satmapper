package ca.gc.space.quicksat.ground.tracking;

import java.awt.Component;
/*
 This Thread will zoom the map to the specified coordinates and magnify the map to the specified zoom.
 *There are 3 layors of zoom, grid, map and objects
 */
public class Zoomer extends Thread {
    public double longitude, latitude;
    int mapClickX = 0;
    int mapClickY = 0;
    double zoomTo = 1;
    int prevMapX, prevMapY;
    double prevZoom;
    //represents how complete the zoom process is, when complete this will become 2
    double zoomProcess = 0;
    
    
    public SatMapper parent;
    
    Zoomer(double longitude, double latitude, double newZoomTo, SatMapper parent) {
        this.parent = parent;
        setPriority(MAX_PRIORITY);
        setName("Zoomer");
        
        //sets the coordinate location zoomer zooms to
        this.latitude = latitude;
        this.longitude = longitude;
        
        //gets the longitude and latitude in pixel position on whole map
        mapClickX = parent.getXCoord(longitude) + parent.offScreenX;
        mapClickY = parent.getYCoord(latitude) + parent.offScreenY;
        
        zoomTo = newZoomTo;
        if (zoomTo > 6) zoomTo = 6;
        
        
        if ( parent.mySlider == null) start();
        else Thread.currentThread().stop();
        
    }
    
    
    
    
    
    synchronized public void run() {
        parent.satSelected = null;
        parent.followSat = false;
        
        int i;
        if (parent.zoom <= (double) zoomTo)
            i = 1;
        else
            i = -1;
        int frames = 24;
        prevMapX = parent.mapX;
        prevMapY = parent.mapY;
        prevZoom = parent.zoom;
        
        int centerX = parent.mapWindowX / 2 + Math.abs(parent.offScreenX);
        int centerY = parent.mapWindowY / 2 + Math.abs(parent.offScreenY);
        
        for (; Math.abs((double) zoomTo - parent.gridZoom) > 0.29999999999999999D; parent.repaint()) {
            parent.gridZoom += 0.25D * (double) i;
            try {
                Thread.sleep(100L);
            } catch (InterruptedException _ex) {}
        }
        parent.gridZoom = zoomTo;
        
        //pixel coordinate to zoom to
        int zoomX = 0;
        int zoomY = 0;
        
        double mapZoom = parent.mapZoom;
        if (parent.zoom <= (double) zoomTo) {
            while (i * (zoomTo - parent.mapZoom) > 0) {
                
                mapZoom += (1.0D / (double) frames) * (double) i;
                if ((double) i * ((double) zoomTo - mapZoom) < 0.0D)
                    mapZoom = zoomTo;
                //represents how complete the zoom process is, out of 2
                zoomProcess = (mapZoom - prevZoom) / ( zoomTo - prevZoom);
                
                zoomX = (int) ((double) centerX + (double) (mapClickX - centerX) * (zoomProcess) );
                zoomY = (int) ((double) centerY + (double) (mapClickY - centerY) * (zoomProcess) );
                
                calcMapZoom(mapZoom, zoomX, zoomY);
                
                
                try { Thread.sleep(1000 / frames); } catch (InterruptedException e) { }
                parent.repaint();
            }
            
            double zoom = parent.zoom;
            while ((double) i * ((double) zoomTo - zoom) > 0.0D) {
                zoom += (1.0D / (double) frames) * (double) i;
                
                //represents how complete the zoom process is, when complete this will become 2
                zoomProcess = 1 + (zoom - prevZoom) / ( zoomTo - prevZoom);
                
                zoomX = (int) ((double) centerX + (double) (mapClickX - centerX) * (zoomProcess - 1) );
                zoomY = (int) ((double) centerY + (double) (mapClickY - centerY) * (zoomProcess - 1) );
                
                calcZoom(zoom, zoomX, zoomY);
                
                try { Thread.sleep(1000 / frames); } catch (InterruptedException e) { }
                parent.repaint();
            }
        }
        
        zoomProcess = 2;
        
        
        //for zoom out calculation
        calcZoom(zoomTo, mapClickX, mapClickY);
        calcMapZoom(zoomTo, mapClickX, mapClickY);
        
        //alligns screen to center of map when zoom is 1
        if (zoomTo == 1) {
            parent.offScreenX = 0;
            parent.offScreenY = 0;
        }
        parent.mapImgOffScreenX = parent.offScreenX;
        parent.mapImgOffScreenY = parent.offScreenY;
        
        
        
        
        parent.repaint();
        
        //destroys this object after it's done
        parent.myZoomer = null;
        System.gc();
        Thread.currentThread().stop();
        
    }
    
    
    public void calcMapZoom(double mapZoom, int zoomX, int zoomY) {
        int mapImgOffScreenX = (int)((mapZoom / prevZoom) * ((double)zoomX - (double)prevMapX / mapZoom / 2D));
        int mapImgOffScreenY = (int)((mapZoom / prevZoom) * ((double)zoomY - (double)prevMapY / mapZoom / 2D));
        
        
        parent.mapImgSizeX = (int)(parent.mapWindowX * mapZoom);
        parent.mapImgSizeY = (int)(parent.mapWindowY * mapZoom);
        
        if (mapImgOffScreenY < 0)
            mapImgOffScreenY = 0;
        else if (mapImgOffScreenY > (parent.mapImgSizeY) - parent.mapWindowY)
            mapImgOffScreenY = parent.mapImgSizeY - parent.mapWindowY;
        
        
        parent.mapImgOffScreenX = mapImgOffScreenX;
        parent.mapImgOffScreenY = mapImgOffScreenY;
        parent.mapZoom = mapZoom;
        parent.checkMapFlip();
    }
    public void calcZoom(double zoom, int mapClickX, int mapClickY) {
        parent.mapX = (int) (zoom * parent.mapWindowX);
        parent.mapY = (int) (zoom * parent.mapWindowY);
        
        parent.offScreenX = (int) ((zoom / prevZoom) * ((double) mapClickX - (double) prevMapX / zoom / 2D));
        
        parent.offScreenY = (int) ((zoom / prevZoom) * ((double) mapClickY - (double) prevMapY / zoom / 2D));
        if (parent.offScreenY < 0) parent.offScreenY = 0;
        else if (parent.offScreenY > parent.mapY - parent.mapWindowY)
            parent.offScreenY = parent.mapY - parent.mapWindowY;
        
        //mapMid* simplifies calculations because it is used often to find the pixel positions of coordinates
        parent.mapMidX = parent.mapX / 2;
        parent.mapMidY = parent.mapY / 2;
        
        parent.zoom = zoom;
        parent.checkMapFlip();
    }
}