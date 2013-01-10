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
/** ClientLink.java
 *  Object instanciated for each client connected to the server.
 *  @author Jean-Francois Cusson, Canadian Space Agency modified by Louis-Philippe Ouellet for use with RTPServer
 * Created on July 23, 2001, 9:57 AM
 * /*============================================================================*/
import java.net.*;
import java.io.*;

/*============================================================================*/
/** This object is instantiated for each new client connecting to the server.
 *  It starts a thread that manages this client, and exists only when the
 *  client deconnects.                                                        */
/*============================================================================*/
public class ClientLink extends java.lang.Object implements Runnable {
/*============================================================================*/
    Socket socket = null;
    RTPServer parentServer = null;
    boolean terminated = false;
    Thread myThread = null;
    DataInputStream inputLink = null;
    DataOutputStream outputLink = null;

    /*======================================================================*/
    /** Creates new ClientLink. No thread is started automatically here.
     * @param s Socket used to communicate with the client.
     * @param ps Reference to the main server                               */
    /*======================================================================*/
    public ClientLink(Socket s, RTPServer ps) {
    /*======================================================================*/
        socket = s;
        parentServer = ps;

    }

    /*======================================================================*/
    /** Called to terminate this client connection and this thread.         */
    /*======================================================================*/
    public void terminate() {
    /*======================================================================*/
        terminated = true;
        try{socket.close();}catch(IOException ioe){};
    }

    /*======================================================================*/
    /** Implements the thread that will take care of one client connection. */
    /*======================================================================*/
    public void run() {
    /*======================================================================*/
        int dataSize = 0;
        int param = 0;
        byte[] byteArrayRead = null;

        System.out.println("Client connection");
        if( socket == null ) return;
        myThread = Thread.currentThread();


            while( !terminated ) {
              try{
                   try{myThread.sleep(4000);}catch(InterruptedException ie){}
              socket.getOutputStream().write(1);
                 //System.out.println("got outStrm OK");
              }
              catch(Exception t){
                  System.out.println("Client disconnected: killing the thread...");
                  terminated=true;
                    myThread=null;
              }  // do nothing
            }
    }
    /*========================================================================*/
    /** Tells us if this client's connection is still alive and well.
     * @return true if this client's conneciton is alive and well, false
     *         otherwise.                                                     */
    /*========================================================================*/
    public boolean isConnectionAlive() {
    /*========================================================================*/
        if( myThread == null ) return( false );
        return( myThread.isAlive() );
    }

     /*========================================================================*/
    /** Returns client socket
     * @return true if this client's conneciton is alive and well, false
     *         otherwise.                                                     */
    /*========================================================================*/
    public Socket getClientSocket() {
    /*========================================================================*/

        return this.socket;
    }

    /*========================================================================*/
    /** Call this method to terminate this client's connection.               */
    /*========================================================================*/
    public void kill() {
    /*========================================================================*/
        if( myThread == null ) this.terminate();
        try{
            try{myThread.sleep(2000);}catch(InterruptedException ie){}
            socket.close();
        }catch(IOException ioe){}
        this.terminated = true;
    }




}

/*============================================================================*/
