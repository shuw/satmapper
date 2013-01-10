/*
 * Start.java
 *
 * Created on February 12, 2002, 10:31 AM
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
import java.util.Vector;
import java.lang.Math;
import java.net.URL;
import java.applet.*;


public class Place extends Sprite{
    public boolean hasContact = false;
    public boolean important = false;
    public Vector contactsVect;
    String[] contacts;
    
    Place(double newLat, double newLong, String newName) {
        this(newLat, newLong, newName, false);    }    
    Place(double newLat, double newLong, String newName, boolean isImportant) {
        important = isImportant;
        name = newName;
        coordLong = newLong;
        coordLat = newLat;
        showName = true;
    }
    
    public String[] getContacts() { 
        return contacts; }
    
    
    public void findContacts(Vector satsVect) {
        int i;
        contactsVect = new Vector();
        
        for (i = 0; i < satsVect.size(); i++) {
                if ( meetsWith( (SatSprite)satsVect.elementAt(i) ) ) {
                    SatSprite meetSat = (SatSprite)satsVect.elementAt(i);
                    addContactWith( meetSat );
                    meetSat.contactLong = coordLong; meetSat.contactLat = coordLat; //sets satellite contact to location
                    meetSat.hasContact = true;
                }
        }
        
        if ( contactsVect.size() == 0 )
            hasContact = false;
        else hasContact = true;
        
        contacts = new String[contactsVect.size()];

        for (i = 0; i < contacts.length; i++) {
            contacts[i] = (String)contactsVect.elementAt(i);
        }
    }
    
    public void addContactWith(Sprite newContact) {        
        contactsVect.addElement( newContact.getName() );
    }
}

