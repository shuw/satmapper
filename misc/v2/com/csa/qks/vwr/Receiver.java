package com.csa.qks.vwr;

import com.csa.qks.cont.SerializedContainer;
import java.applet.Applet;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.URL;

// Referenced classes of package com.csa.qks.vwr:
//            SatData

public class Receiver extends Thread
{

    public static final String HOST = "10.20.35.192";
    public static final int port = 9902;
    ObjectInputStream in;
    Applet applet;
    URL baseURL;
    Socket socket;
    SatData incommingData;
	public boolean isConnected;
    public Receiver(SatData incommingData)
    {
        this.incommingData = incommingData;
        setPriority(9);
        start();
    }
    public boolean connect()
    {
        try
        {
	        if(!isConnected){
            socket = new Socket(HOST, 9902);
            in = new ObjectInputStream(socket.getInputStream());
            isConnected=true;
            return true;
	        }
	        else{
		        //CLient is already conected no need to reconnect(happens if "REFRESH" in browser is pressed)
		        
		        }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            isConnected=false;
        }
        return false;
    }
    public static void main(String args[])
    {
        new Receiver(new SatData());
    }
public void receive() {
    try {
        if (isConnected) {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof SerializedContainer) {
                    SerializedContainer cont = (SerializedContainer) obj;
                    incommingData.setSats(cont.getMySatsDatas());
                }
                Thread.sleep(4000L);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        isConnected = false;
    }
}
    public void run()
    {
        try
        {
            if(!connect())
                throw new Exception("Could not connect to server port");
            receive();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
