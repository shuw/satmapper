/*============================================================================*/
/** AdminLink.java
 *  Object instanciated for each administrator connected to the server.
 *  @author Jean-Francois Cusson, Canadian Space Agency
 *  THIS IS NOT AN OFFICIAL RELEASE. THIS CODE IS STILL BEING DEVELOPED AND
 *  TESTED!
 * Created on July 2, 2001, 9:51 AM
/*============================================================================*/

package ca.gc.space.quicksat.ground.server;
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
Server parentServer = null;
boolean terminated = false;
Thread myThread = null;
DataInputStream inputLink = null;
DataOutputStream outputLink = null;

    
    /*======================================================================*/
    /** Creates new AdminLink. No thread is started automatically here.
     * @param s Socket used to communicate with the administrator.
     * @param ps Reference to the main server                               */
    /*======================================================================*/
    public AdminLink( Socket s, Server ps ) {
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
            InputStream is = socket.getInputStream();
            inputLink = new DataInputStream( is );
            OutputStream os = socket.getOutputStream();
            outputLink = new DataOutputStream( os );
        } catch( IOException ioe ) {}    
        int i = 0;
        RUNNING_LOOP:
        while( !terminated ) {            
            try{
                i = 0;
                i = inputLink.readInt();
                System.out.println("Read " + i);
                switch( i ) {
                    case -2:    //Kill server
                        System.out.println("Got request to kill server");
                        parentServer.kill();
                        break;
                    case -1:    //Kill myself
                        System.out.println("Got request to terminate myself");
                        break RUNNING_LOOP;
                    case 0:
                        break;
                    /*=====================*/    
                    /* UPDATE ADMIN STATUS */
                    /*=====================*/
                    case 1:
                        System.out.println("Got a request for update");
                        outputLink.writeInt( 1 ); //Admins status
                        outputLink.writeInt( parentServer.nbAdmins() );
                        for( int j=0; j<parentServer.nbAdmins(); j++ ) {
                            int id = j;
                            String username = null;
                            int status = 0;
                            //parentServer.getAdminStatus( id, username, status );
                            outputLink.writeInt( id );
                            if( username == null ) username = "N/A";
                            outputLink.writeInt( username.getBytes().length );
                            outputLink.write( username.getBytes() );
                            outputLink.writeInt( status );                          
                        }    
                        break;
                        
                    /*==========================*/    
                    /* KILL AN ADMIN CONNECTION */
                    /*==========================*/                        
                    case 2:                        
                        System.out.println("Got request to kill an admin connection");
                        int adminToKill = inputLink.readInt();                        
                        System.out.println("Killing admin #"+adminToKill);
                        parentServer.killAdminConnection( adminToKill );
                    default:
                        System.out.println("Got illegal request");
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
