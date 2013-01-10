package ca.gc.space.quicksat.ground.tracking;

import java.util.Vector;

// Referenced classes of package com.csa.qks.vwr:
//            Sprite, SatSprite

public class Place extends Sprite
{

    public boolean hasContact;
    public boolean important;
    public Vector contactsVect;
    String contacts[];
    Place(double newLat, double newLong, String newName)
    {
        this(newLat, newLong, newName, false);
    }
    Place(double newLat, double newLong, String newName, boolean isImportant)
    {
        hasContact = false;
        important = false;
        important = isImportant;
        super.name = newName;
        super.coordLong = newLong;
        super.coordLat = newLat;
        super.showName = true;
    }
    public void addContactWith(Sprite newContact)
    {
        contactsVect.addElement(newContact.getName());
    }
    public void findContacts(Vector satsVect)
    {
        contactsVect = new Vector();
        for(int i = 0; i < satsVect.size(); i++)
            if(meetsWith((SatSprite)satsVect.elementAt(i)))
            {
                SatSprite meetSat = (SatSprite)satsVect.elementAt(i);
                addContactWith(meetSat);
                meetSat.contactLong = super.coordLong;
                meetSat.contactLat = super.coordLat;
                meetSat.hasContact = true;
            }

        if(contactsVect.size() == 0)
            hasContact = false;
        else
            hasContact = true;
        contacts = new String[contactsVect.size()];
        for(int i = 0; i < contacts.length; i++)
            contacts[i] = (String)contactsVect.elementAt(i);

    }
    public String[] getContacts()
    {
        return contacts;
    }
}