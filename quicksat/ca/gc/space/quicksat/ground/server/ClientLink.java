/*============================================================================*/
/** ClientLink.java
 *  Object instanciated for each client connected to the server.
 *  @author Jean-Francois Cusson, Canadian Space Agency
 *  THIS IS NOT AN OFFICIAL RELEASE. THIS CODE IS STILL BEING DEVELOPED AND
 *  TESTED!
 * Created on July 23, 2001, 9:57 AM
/*============================================================================*/

package ca.gc.space.quicksat.ground.server;
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
Server parentServer = null;
boolean terminated = false;
Thread myThread = null;
DataInputStream inputLink = null;
DataOutputStream outputLink = null;

    /*======================================================================*/
    /** Creates new ClientLink. No thread is started automatically here.
     * @param s Socket used to communicate with the client.
     * @param ps Reference to the main server                               */
    /*======================================================================*/
    public ClientLink(Socket s, Server ps) {
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
    
        System.out.println("Entering client manager");
        if( socket == null ) return;
        myThread = Thread.currentThread();
        
        /*-------------------------------------------------*/
        /* Creates necessary data input and output streams */
        /*-------------------------------------------------*/
        try {
            InputStream is = socket.getInputStream();
            inputLink = new DataInputStream( is );
            OutputStream os = socket.getOutputStream();
            outputLink = new DataOutputStream( os );
        } catch( IOException ioe ) {
            System.out.println("ERROR setting streams in client manager: "+ioe);
        }    
        int i = 0;
        
        /*---------------------------------------------------*/
        /* Then loop here for the duration of the connection */
        /*---------------------------------------------------*/
        System.out.println("Entering client manager's main loop");
        RUNNING_LOOP:
        while( !terminated ) { 
            try{
                i = 0;
                i = inputLink.readInt();
                /*--------------------------------------------*/
                /* Here is the format of what is expected:    */
                /* [command][size_of_data/value][data]        */
                /* Where [command] is an integer              */
                /*       [size_of_data] is an integer telling */
                /*                      telling us how many   */
                /*                      chars are in [data]   */
                /*  ..or [value] an integer, parameter of     */
                /*               [command]                    */
                /*       [data] a serie of chars...           */
                /*--------------------------------------------*/
                switch( i ) {
                    /*........................................................*/
                    case Server.KILL_SERVER:    //Kill server
                    /*........................................................*/    
                        System.out.println("Got request to kill server");
                        param = inputLink.readInt(); //Don't really need...
                        parentServer.kill();
                        break;
                    /*........................................................*/                        
                    case Server.KILL_MYSELF:    //Kill myself
                    /*........................................................*/    
                        System.out.println("Got request to terminate myself");
                        param = inputLink.readInt();
                        break RUNNING_LOOP;
                    /*........................................................*/                        
                    case Server.SET_SPACECRAFT_LINK_SPEED:
                    /*........................................................*/    
                        System.out.println("Request to change port speed");
                        param = inputLink.readInt();
                        parentServer.setSpacecraftSerialPortBaudRate( param );
                        break;
                    /*........................................................*/    
                    case Server.UPDATE:
                    /*........................................................*/    
                        parentServer.sendSpacecraftPortInfoToAllClients();
                        break;
                    /*........................................................*/                        
                    case Server.DATA_BOOTLOADER:
                    /*........................................................*/    
                        /*======================================*/    
                        /* Get data for spacecraft's bootloader */
                        /*======================================*/
                        System.out.println("Got data for bootloader");                        
                        dataSize = inputLink.readInt();
                        byteArrayRead = new byte[dataSize];
                        inputLink.read( byteArrayRead );
                        //System.out.println("Data="+byteArrayRead);
                        parentServer.sendDataToSpacecraft(
                                        Server.DATA_BOOTLOADER, byteArrayRead );
                        break;
                    /*........................................................*/    
                    case Server.DATA_HOUSEKEEPING:
                    /*........................................................*/    
                        /*=============================================*/    
                        /* Get data for spacecraft's housekeeping task */
                        /*=============================================*/
                        System.out.println("Got data for spacecraft's pht");
                        dataSize = inputLink.readInt();
                        byteArrayRead = new byte[dataSize];
                        inputLink.read( byteArrayRead );
                        //System.out.println("Data="+new String(byteArrayRead));
                        parentServer.sendDataToSpacecraft( 
                                      Server.DATA_HOUSEKEEPING, byteArrayRead );
                        break;
                    /*........................................................*/    
                    case Server.DATA_SPACECRAFT:
                    /*........................................................*/    
                        /*==============================*/    
                        /* Get data for spacecraft link */
                        /*==============================*/
                        System.out.println("Got data for spacecraft link");
                        dataSize = inputLink.readInt();
                        byteArrayRead = new byte[dataSize];
                        inputLink.read( byteArrayRead );
                        //System.out.println("Data="+new String(byteArrayRead));
                        parentServer.sendDataToSpacecraft( 
                                      Server.DATA_SPACECRAFT, byteArrayRead );
                        break;
                    /*........................................................*/    
                    case Server.DATA_ROTATOR:
                    /*........................................................*/    
                        System.out.println("Got data for antenna rotator");
                        dataSize = inputLink.readInt();
                        byteArrayRead = new byte[dataSize];
                        inputLink.read( byteArrayRead );
                        System.out.println("Data="+new String(byteArrayRead));
                        parentServer.sendDataToRotator(byteArrayRead);
                        break;
                    /*........................................................*/    
                    case Server.DATA_RADIO:    
                    /*........................................................*/    
                        //System.out.println("Got data for radio");
                        dataSize = inputLink.readInt();
                        byteArrayRead = new byte[dataSize];
                        inputLink.read( byteArrayRead );
                        //System.out.println("Data="+new String(byteArrayRead));
                        parentServer.sendDataToRadio(byteArrayRead);
                        break;
                    /*........................................................*/    
                    case Server.DATA_TNC:    
                    /*........................................................*/    
                        //System.out.println("Got data for TNC");
                        dataSize = inputLink.readInt();
                        byteArrayRead = new byte[dataSize];
                        inputLink.read( byteArrayRead );
                        //System.out.println("Data="+new String(byteArrayRead));
                        parentServer.sendDataToTnc(byteArrayRead);
                        break;
                    /*........................................................*/    
                    case Server.KILL_A_CLIENT:                        
                    /*........................................................*/    
                        /*==========================*/    
                        /* KILL A CLIENT CONNECTION */
                        /* (won't stay here...)     */
                        /*==========================*/                                  
                        System.out.println("Request to kill client connection");
                        int clientToKill = inputLink.readInt();                        
                        System.out.println("Killing client #"+clientToKill);
                        parentServer.killClientConnection( clientToKill );
                    /*........................................................*/    
                    default:
                    /*........................................................*/    
                        System.out.println("Got illegal request");
                        break;
                }
            }
            catch( IOException ioe ) { 
                /*-----------------------------------------*/
                /* If the client disconnects ungracefully, */
                /* we'll end up here...                    */
                /*-----------------------------------------*/
                System.out.println("Client link event: "+ioe);
                terminated = true;
            }
            /*--------------*/
            /* Be gentle... */
            /*--------------*/
            try{Thread.currentThread().sleep(25);}
            catch( InterruptedException ie ) {}
        }
        
        System.out.println("Terminating connection with client");        
        try{
            outputLink.close();
            inputLink.close();
            socket.close();
        }catch(IOException ioe){}
        myThread = null;
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
    /** Call this method to terminate this client's connection.               */
    /*========================================================================*/
    public void kill() {
    /*========================================================================*/        
        if( myThread == null ) this.terminate();        
        try{
            outputLink.writeInt(-1);    //kill client
            try{myThread.sleep(2000);}catch(InterruptedException ie){}            
            outputLink.close();
            inputLink.close();
            socket.close();
        }catch(IOException ioe){}
        this.terminated = true;
    }
    
    /*========================================================================*/
    /** Sends data to client, identified as "coming from the spacecraft".
     * @param data Byte array to send to the client, marked as coming from
     *             the spacecraft.
    /*========================================================================*/
    public void sendSpacecraftData( byte[] data ) {
    /*========================================================================*/    
        try{            
            outputLink.writeInt( Server.DATA_SPACECRAFT );  //Command
            outputLink.writeInt( data.length );             //Data length
            outputLink.write( data );                       //Data
        } catch( IOException ioe ) {
            System.out.println("Error sending data to client: "+ioe);
        }
    }
    /*========================================================================*/
    /** Sends port status to client, identified as "coming from the spacecraft".
     * @param data status of the serial port lines used for the s/c.
    /*========================================================================*/
    public void sendSpacecraftPortInfo( byte portLineStatus, int speed ) {
    /*========================================================================*/    
        try{            
            outputLink.writeInt( Server.PORTINFO_SPACECRAFT );  //Command
            outputLink.writeInt( 5 );                           //Data length
            outputLink.write( portLineStatus );                 //Data
            outputLink.writeInt( speed );
        } catch( IOException ioe ) {
            System.out.println("Error sending s/c port info to client: "+ioe);
        }
    }
    /*========================================================================*/
    /** Sends data to client, identified as "coming from the radio".
     * @param data Byte array to send to the client, marked as coming from
     *             the radio.
    /*========================================================================*/
    public void sendRadioData( byte[] data ) {
    /*========================================================================*/    
        try{
            outputLink.writeInt( Server.DATA_RADIO );   //Command
            outputLink.writeInt( data.length );         //Data length
            outputLink.write( data );                   //Data
        } catch( IOException ioe ) {
            System.out.println("Error sending radio data to client: "+ioe);
        }
    }
    /*========================================================================*/
    /** Sends data to client, identified as "coming from the antenna rotator".
     * @param data Byte array to send to the client, marked as coming from
     *             the antenna rotator.
    /*========================================================================*/
    public void sendRotatorData( byte[] data ) {
    /*========================================================================*/    
        try{
            outputLink.writeInt( Server.DATA_ROTATOR ); //Command
            outputLink.writeInt( data.length );         //Data length
            outputLink.write( data );                   //Data
        } catch( IOException ioe ) {
            System.out.println("Error sending rotator data to client: "+ioe);
        }
    }
    
}
