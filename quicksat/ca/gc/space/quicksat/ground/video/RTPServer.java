package ca.gc.space.quicksat.ground.video;
/*============================================================================*/
/** RTPServer.java
 *  RTPServer send RTP streams to multiple clients (multi unicasting)
 *  It is used to monitor the position of an antenna for the quicksat ground station
 *  IMPORTANT NOTE:
 *          IF BY ANY CHANCE THE SERVER WOULD COME TO CRASH, THE DRIVER FOR THE USB DEVICE USED FOR VIDEO CAPTURE WILL LOCK
 *            IT IS IMPERATIVE TO REBOOT TO REMOVE THIS LOCK
 *          NORMAL EXIT MADE BY ADMIN KILLING THE SERVER WILL NOT PRODUCE THE SAME LOCK
 *  @author Louis-Philippe Ouellet inspired by code from Jean-Francois Cusson, Canadian Space Agency
 * Created on July 2, 2001, 9:22 AM
 */



 import java.net.*;
 import java.io.*;
 import java.util.*;

 import java.util.Date.*;


 import java.awt.*;
 import java.io.*;
 import java.net.InetAddress;
 import javax.media.*;
 import javax.media.protocol.*;
 import javax.media.protocol.DataSource;
 import javax.media.format.*;

 import com.sun.media.vfw.*;

 import javax.media.Format;


/*============================================================================*/
 /** Main class for server, destined to be standalone application.
  */
/*============================================================================*/
 public class RTPServer extends Object implements Runnable{
/*============================================================================*/

     public static final int KILL_SERVER                 = -2;
     public static final int KILL_MYSELF                 = -1;
     public static final int KILL_A_CLIENT               = 2;




/*--------------------------------------------------------------------------*/
/* vector containing the list of all currently connected clients and admins */
/*--------------------------------------------------------------------------*/
     Vector                  adminsConnected = null;
     Vector                  clientsConnected = null;

/*-----------------------------------------*/
/* Threads started for every connection... */
/*-----------------------------------------*/
     Thread                  threadWaitingForAdmin = null;
     Thread                  threadWaitingForClients = null;
     Thread                  threadMonitor = null;


/*---------------------------------------*/
/* Socket parameters and usefull vars... */
/*---------------------------------------*/
     Socket                  adminSocket;
     Socket                  clientSocket;
     final int               adminPort = 9906;   //Should be a parameter!!!
     final int               clientPort = 9907;
     ServerSocket            adminWait = null;
     ServerSocket            clientWait = null;
     VideoTransmit at = null;


    /*========================================================================*/
     /** main method starting server. Simply instantiate a new server...
      * @param arg command line arguments                                      */
    /*========================================================================*/
     public static void main(java.lang.String[] arg) {
    /*========================================================================*/
         System.out.println("==========================================================");
         System.out.println("               *****RTP Server*****");
         System.out.println("Antenna rotation monitoring for QuickSat GroundStation");
         System.out.println("             Canadian space agency");
         System.out.println("             This is a test version");
         System.out.println("==========================================================");
         RTPServer server = new RTPServer();
     }

    /*========================================================================*/
     /** Creates new Server. Reads setup files and starts all necessary threads*/
    /*========================================================================*/
     public RTPServer() {
    /*========================================================================*/



        /*-----------------------------------------------*/
        /* Initialize the vectors containing the list of */
        /* clients and administrators connected          */
        /*-----------------------------------------------*/
         adminsConnected = new Vector();
         clientsConnected= new Vector();
         at = new VideoTransmit(); //Creates the object that is sending the RTP stream




         try{
            /*---------------------------------------------*/
            /* Creates socket to wait for admin connection */
            /*---------------------------------------------*/
             adminWait = new ServerSocket( adminPort );
             System.out.println("Waiting on port " + adminPort + " for admin");

            /*----------------------------------------------*/
            /* Creates socket to wait for client connection */
            /*----------------------------------------------*/
             clientWait = new ServerSocket( clientPort );
             System.out.println("Waiting on port " +clientPort+ " for clients");

            /*---------------------------------------------------------------*/
            /* Creates a thread that will actually wait for an administrator */
            /* and communicate with it...                                    */
            /*---------------------------------------------------------------*/
             threadWaitingForAdmin = new Thread( this );
             threadWaitingForAdmin.start();

            /*-------------------------------------------------------*/
            /* Creates a thread that will actually wait for a client */
            /* and communicate with it...                            */
            /*-------------------------------------------------------*/
             threadWaitingForClients = new Thread( this );
             threadWaitingForClients.start();

            /*--------------------------------------------*/
            /* Thread to monitor all of the other threads */
            /*--------------------------------------------*/
             threadMonitor = new Thread( this );
             threadMonitor.start();

         } catch( Exception e ) {
             at.stop();
             System.out.println("Server error: " + e);}

     }

    /*========================================================================*/
     /** Executes our local threads: threadWaitingForAdmin,
      *  threadWaitingForClients and threadMonitor. */
    /*========================================================================*/
     public void run() {
    /*========================================================================*/

        /*--------------------------------------------------------------------*/
        /*        This is a thread to communicate with an administrator       */
        /*--------------------------------------------------------------------*/
         if( Thread.currentThread() == threadWaitingForAdmin ) {
             System.out.println("This is the thread to wait for admin...");
             while( true ) {
                 try {

                    /*-----------------------------*/
                    /* Block here until connection */
                    /*-----------------------------*/
                     adminSocket = adminWait.accept();
                     System.out.println("Admin connected:"
                     + adminSocket.getInetAddress());

                    /*----------------------------------------------*/
                    /* Create new object to take care of this admin */
                    /*----------------------------------------------*/
                     AdminLink adminServer = new AdminLink( adminSocket, this );
                     Thread thread = new Thread( adminServer, "admin" );
                     thread.start();

                    /*--------------------------------------------*/
                    /* Adds this admin object in the admin Vector */
                    /* to keep track of it...                     */
                    /*--------------------------------------------*/
                     adminsConnected.add( (AdminLink) adminServer );

                 } catch(Exception e) {System.out.println("Server error: " + e);}
             }
         }

        /*--------------------------------------------------------------------*/
        /*            This is a thread to communicate with a client           */
        /*--------------------------------------------------------------------*/
         else if( Thread.currentThread() == threadWaitingForClients ) {
             System.out.println("This is the thread to wait for clients...");
             while( true ) {
                 try {
                    /*------------------------------------*/
                    /* Block here until a client connects */
                    /*------------------------------------*/
                     clientSocket = clientWait.accept();
                     System.out.println("Client connected: "
                     + clientSocket.getInetAddress() );

                    /*-----------------------------------------------------*/
                    /* Client connected: Create new object to take care of */
                    /* this client...                                      */
                    /*-----------------------------------------------------*/
                     ClientLink clientServer = new ClientLink(clientSocket,this);
                     Thread thread = new Thread( clientServer, "client" );
                     thread.start();

                    /*--------------------------------------------------*/
                    /* Adds this client object to the client Vector, to */
                    /* keep track of it...                              */
                    /*--------------------------------------------------*/
                     clientsConnected.add( (ClientLink) clientServer );
                     this.transmitToCLients(clientsConnected);
                 } catch(Exception e) {
                     at.stop();
                     System.out.println("Server error: " + e);}
             }
         }

        /*--------------------------------------------------------------------*/
        /*           This is a thread to monitor all other threads...         */
        /*--------------------------------------------------------------------*/
         else if( Thread.currentThread() == threadMonitor ) {

             while( true ) {

                /*----------------------------------------------------------*/
                /* Do verification periodically, so sleep here 4 seconds... */
                /*----------------------------------------------------------*/
                 try{ Thread.sleep( 4000 ); } catch( InterruptedException ie ){}

                /*----------------------------------------*/
                /* Verify all administrator's connections */
                /*----------------------------------------*/
                 //System.out.println(
                 //     "Number of connected admins:" + adminsConnected.size() );
                 for( int i=0; i<adminsConnected.size(); i++ ) {
                     AdminLink adm = (AdminLink)adminsConnected.get(i);
                     //Thread thread = (Thread)adminsConnected.get(i);
                     //System.out.print("Thread #"+i);
                     //if( adm.isConnectionAlive() ) System.out.println(" : alive");
                     //else System.out.println(" : Dead");

                 }
                /*-----------------------------------------------------*/
                /* Remove dead adminstrators connections from the list */
                /* (one at a time for simplicity...)                   */
                /*-----------------------------------------------------*/
                 REMOVING_DEAD_ADMINS:
                     for( int i=0; i<adminsConnected.size(); i++ ) {
                         AdminLink adm = (AdminLink)adminsConnected.get(i);
                         if( !adm.isConnectionAlive() ) {
                             adminsConnected.removeElementAt(i);
                             break REMOVING_DEAD_ADMINS;
                         }
                     }

                /*---------------------------------*/
                /* Verify all client's connections */
                /*---------------------------------*/
                     //System.out.println(
                     // "\nNumber of connected clients:" + clientsConnected.size() );
                     //for( int i=0; i<clientsConnected.size(); i++ ) {
                     //ClientLink clt = (ClientLink)clientsConnected.get(i);
                     //System.out.print("Client #"+i);
                     //   if(clt.isConnectionAlive()) System.out.println(" : alive");
                     // else System.out.println(" : Dead");
                     // }

                /*-----------------------------------------------*/
                /* Remove dead clients connections from the list */
                /* (one at a time for simplicity...)             */
                /*-----------------------------------------------*/
                     REMOVING_DEAD_CLIENTS:
                         for( int i=0; i<clientsConnected.size(); i++ ) {
                             ClientLink clt = (ClientLink)clientsConnected.get(i);
                             if( !clt.isConnectionAlive() ) {
                                 clientsConnected.removeElementAt(i);

                                 //refresh the streams clients
                                 this.transmitToCLients(clientsConnected);

                                 break REMOVING_DEAD_CLIENTS;
                             }
                         }

             }
        /*============================*/
        /* ERROR, should never happen */
        /*============================*/
         } else {
             System.out.println("ERROR: Bad thread!");
         }
     }

    /*=======================================================================*/
     /** Used to send the stream to the clients
      * /*=======================================================================*/

     public void transmitToCLients(Vector clientsConnected) {
         String result=null;
         at.stop();
           System.out.println("Result from stop: "+result);
         System.out.println("Transmission STOPED");
         result = at.start(clientsConnected);
        System.out.println("Result from start: "+result);
         for( int i=0; i<clientsConnected.size(); i++ ) {
                System.out.println("Transmission STARTED for:");
             ClientLink clt = (ClientLink)clientsConnected.get(i);
             System.out.println("Client "+i+": "+clt.socket.getInetAddress());
         }
     }

    /*=======================================================================*/
     /** Used to force an admin connection to abort, and free corresponing
      *  resources.
      *  @param adminToKill Index of the admin to kill, from admin Vector.    */
    /*=======================================================================*/
     public void killAdminConnection(int adminToKill) {
    /*=======================================================================*/
         if( adminToKill >= adminsConnected.size() ) {
             System.out.println("Error killing admin connection");
             return;
         }

         AdminLink admToKill = (AdminLink)adminsConnected.get(adminToKill);

         admToKill.kill();
     }

    /*=======================================================================*/
     /** Forces a client connection to abort, freeing corresponding resources
      *  @param clientToKill Index of client to terminate, from client Vector */
    /*=======================================================================*/
     public void killClientConnection(int clientToKill) {
    /*=======================================================================*/
         if( clientToKill >= clientsConnected.size() ) {
             System.out.println("Error killing client connection");
             return;
         }
         ClientLink cltToKill = (ClientLink)clientsConnected.get(clientToKill);
         cltToKill.kill();

     }

    /*=======================================================================*/
     /** Returns the number of admins currently connected.
      *  @return Number of admins currently connected.                        */
    /*=======================================================================*/
     public int nbAdmins() {
    /*=======================================================================*/
         return( adminsConnected.size() );
     }

    /*=======================================================================*/
     /** Kills ourself, after terminating all connections and freeing
      *  resources. FOR NOW USED ONLY ON ADMIN FOR TESTING...                 */
    /*=======================================================================*/
     public void kill() {
    /*=======================================================================*/
         System.out.println("KILLING ALL CLIENTS");
         try{
             for( int i=0; i<adminsConnected.size(); i++ ) {
                 AdminLink adm = (AdminLink)adminsConnected.get(i);
                 adm.kill();
             }
         }
         catch(Exception e){}
         System.out.println("KILLING MYSELF");
         try{
             at.stop();
             adminSocket.close();
         }catch(IOException ioe){


         }

         System.exit( 0 );
     }











 }
