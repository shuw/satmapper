package com.csa.qks.vwr;

import ca.gc.space.quicksat.ground.satellite.*;
import com.csa.qks.cont.SatDataToSend;
import java.awt.Component;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

// Referenced classes of package com.csa.qks.vwr:
//            SatMapper, Sprite, SatData

public class AppUpdater extends Thread
{

    public SatMapper mapper;
    public ca.gc.space.quicksat.ground.client.jload j;
    public AppUpdater(ca.gc.space.quicksat.ground.client.jload j, SatMapper mapper)
    {
        this.mapper = mapper;
        this.j=j;
        //setPriority(10);
       // start();
    }
public void run() {
    while (true) {
        if (mapper.followSat && mapper.satSelected != null)
            mapper.moveScreen(mapper.getXCoord(mapper.satSelected.getLong()) + Math.abs(mapper.offScreenX), mapper.getYCoord(mapper.satSelected.getLat()) + Math.abs(mapper.offScreenY));
        Satellite myCurrentSat = j.getAsat();
        int alt;
        int speed;
        long lat;
        long longi;

        alt = myCurrentSat.getAltitudeKm();
        speed = myCurrentSat.getVelocityKmH();
        lat = myCurrentSat.getLatitude();
        longi = myCurrentSat.getLongitude();
        if (longi < 180L)
            longi = -longi;
        else
            if (longi > 180L)
                longi = 360L - longi;
        //System.out.println(newData.getName() + "  LAT: " + lat + "  LONG:  " + longi + "  Altitude: " + alt + " Speed: " + speed);
        mapper.updatePosition(myCurrentSat.getName(), longi, lat, speed, alt);

        mapper.updateContacts();
        mapper.repaint();
    }
}
}
