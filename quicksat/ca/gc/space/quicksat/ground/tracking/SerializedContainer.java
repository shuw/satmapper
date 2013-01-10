package ca.gc.space.quicksat.ground.tracking;

import java.io.Serializable;
import java.util.Vector;

public class SerializedContainer
    implements Serializable
{

    Vector mySatsDatas;
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
}
