// Decompiled by DJ v2.9.9.61 Copyright 2000 Atanas Neshkov  Date: 3/10/2002 20:18:07
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   SerializedContainer.java

package com.csa.qks.cont;

import java.io.Serializable;
import java.util.Vector;

public class SerializedContainer
    implements Serializable
{

    public SerializedContainer()
    {
        mySatsDatas = new Vector();
    }

    public Vector getMySatsDatas()
    {
        return mySatsDatas;
    }

    public void setMySatsDatas(Vector newMySatsDatas)
    {
        mySatsDatas = newMySatsDatas;
    }

    Vector mySatsDatas;
}