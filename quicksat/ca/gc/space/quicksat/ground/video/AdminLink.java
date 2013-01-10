package ca.gc.space.quicksat.ground.video;
/*============================================================================*/
/** AdminLink.java
 *  Object instanciated for each administrator connected to the server.
 *  @author Jean-Francois Cusson, Canadian Space Agency
 *  THIS IS NOT AN OFFICIAL RELEASE. THIS CODE IS STILL BEING DEVELOPED AND
 *  TESTED!
 * Created on July 2, 2001, 9:51 AM
/*============================================================================*/
import java.net.*;
import java.io.*;

/*============================================================================*/
/** This object is instantiated for each new administrator connecting to the
 *  server. It starts a thread that manages this client, and exists only when
 *  the client deconnects.
 *  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *  THIS CLASS HAS NOT REALLY BEEN USED YET, SO ITS DEVELOPMENT IS IMMATURE!!
 *  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*============================================================================*/
public class AdminLink extends Object implements Runnable {
/*============================================================================*/
Socket socket = null;
RTPServer parentServer = null;
boolean terminated = false;
Thread myThread = null;
DataInputStream inputLink = null;
DataOutputStream outputLink = null;
 BufferedReader is =null;
     PrintWriter os = null;
    /*======================================================================*/
    /** Creates new AdminLink. No thread is started automatically here.
     * @param s Socket used to communicate with the administrator.
     * @param ps Reference to the main server                               */
    /*======================================================================*/
    public AdminLink( Socket s, RTPServer ps ) {
    /*======================================================================*/
        socket = s;
        parentServer = ps;
    }

    /*========================================================================*/
    /** Used to terminate the current connection with this administrator, but
     *  use Kill() to do it gracefully...                                     */
    /*========================================================================*/
    public void terminate() {
    /*========================================================================*/
        terminated = true;
        try{socket.close();}catch(IOException ioe){};
    }

    /*======================================================================*/
    /** Implements the thread that will take care of one admin connection.  */
    /*======================================================================*/
    public void run() {
    /*======================================================================*/

        if( socket == null ) return;
        myThread = Thread.currentThread();
        try {

            is = new BufferedReader( new InputStreamReader(socket.getInputStream()));
           // inputLink = new DataInputStream( is );
           os = new PrintWriter(socket.getOutputStream());
           // outputLink = new DataOutputStream( os );

        } catch( IOException ioe ) {}
        int i =0;
        RUNNING_LOOP:
        while( !terminated ) {
            try{
                i = 0;
                i = is.read();
                System.out.println("Read " + i);
                switch( i ) {
                    case 49:    //Kill server
                        System.out.println("Got request to kill server");
                        parentServer.kill();
                        break;
                }
            }
            catch( IOException ioe ) {
                System.out.println("ERROR on the admin link: "+ioe);
                terminated = true;
            }
            try{Thread.currentThread().sleep(50);}catch( InterruptedException ie ) {}
        }
        System.out.println("Exiting");
        try{
            outputLink.close();
            inputLink.close();
            socket.close();
        }catch(IOException ioe){}
    }

    /*========================================================================*/
    /** Tells us if the current connection with this admin is alive and well.
     * @return true if the connection with this admin is alive and well, false
     *              otherwise.                                                */
    /*========================================================================*/
    public boolean isConnectionAlive() {
    /*========================================================================*/
        if( myThread == null ) return( false );
        return( myThread.isAlive() );
    }

    /*========================================================================*/
    /* Used to terminate the connection with this administrator.              */
    /*========================================================================*/
    public void kill() {
    /*========================================================================*/
        if( myThread == null ) this.terminate();
        try{
            outputLink.writeInt(-1);    //kill admin client
            try{myThread.sleep(2000);}catch(InterruptedException ie){}
            outputLink.close();
            inputLink.close();
            socket.close();
        }catch(IOException ioe){}
        this.terminated = true;
    }

}
