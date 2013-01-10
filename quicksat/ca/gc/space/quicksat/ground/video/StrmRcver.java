/*============================================================================*/
/* Class implementing a video streaming client (with viewer).                 */
/* March 2002 By Louis-Philippe Ouellet, for the Canadian Space Agency.       */
/* Modification from Sun's JMF.                                               */
/*============================================================================*/
package ca.gc.space.quicksat.ground.video;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import java.util.Vector;

import javax.media.*;
import javax.media.rtp.*;
import javax.media.rtp.event.*;
import javax.media.rtp.rtcp.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.Format;
import javax.media.format.FormatChangeEvent;
import javax.media.control.BufferControl;
import javax.swing.*;

import ca.gc.space.quicksat.ground.util.*;

/*============================================================================*/
/** This class is used to receive RTP streams comming  from the RTP server
 *  Each time a new participant joins , the panel is refreshed                */
/*============================================================================*/
public class StrmRcver implements   ReceiveStreamListener, 
                                    SessionListener, 
                                    ControllerListener,
                                    Runnable {
/*============================================================================*/
boolean     cancel          = false; //Put tu true to exit.
RTPManager  mgr             = null;
Vector      playerWindows   = null;
JPanel      panel           = null;
boolean     dataReceived    = false;
Object      dataSync        = new Object();
String      serverAddress   = null;
int         port            = 9904;
int         clientPort      = 9907;
int         ttl             = 1;
PlayerPanel pp              = null;
Socket      socket          = null;
Log         log             = null;
private int BUFFER_LENGTH   = 350;
private long RTP_SESSION_OPEN_TIMEOUT = 60000;
    
    /*------------------------------------------------------------------------*/
    /** Constructor for the video stream client. Opens a socket connection with
     *  the server.
     *  @param server Address of the RTP server.
     *  @param panel Panel where the viewer will reside.
     *  @param log Log object, used to log progress and errors.               */
    /*------------------------------------------------------------------------*/
    public StrmRcver(String server, JPanel panel, Log log) {
    /*------------------------------------------------------------------------*/
        
        this.panel          = panel;
        this.serverAddress  = server;
        this.log            = log;
        
        if( log == null ) 
            log = new Log();
        
        log.test(   "Attempting connection to video streaming server at '"
                    + serverAddress
                    + "'" );
        try{
            socket = new Socket(this.serverAddress,clientPort);
        }
        catch(Exception e) {
            log.error( "Could not create client socket to '"
                        + serverAddress
                        + "' : "
                        + e );
        }        
    }
    /*------------------------------------------------------------------------*/
    /** Initializes the RTP session with the server, allowing video streaming.
     *  @return True if everything went ok, false otherwise.                  */
    /*------------------------------------------------------------------------*/
    public boolean initialize() {
    /*------------------------------------------------------------------------*/
        
        if( mgr == null ) {
            try {
                cancel=false;
                /*----------------------*/
                /* Prepare the adresses */
                /*----------------------*/
                InetAddress ipAddr;
                SessionAddress localAddr = new SessionAddress();
                SessionAddress destAddr;
                
                /*------------------------*/
                /* Open the RTP sessions. */
                /*------------------------*/
                log.info(   "Opening RTP session: addr= " 
                            + this.serverAddress 
                            + " port= " 
                            + this.port 
                            + " ttl= " + 
                            this.ttl);
                
                mgr = RTPManager.newInstance();
                if( mgr == null ) {
                    log.error("Unable to instanciate RTP manager");
                    return( false );                    
                }
                mgr.addSessionListener(this);
                mgr.addReceiveStreamListener(this);
                
                ipAddr = InetAddress.getByName(serverAddress);
                localAddr= new SessionAddress(InetAddress.getLocalHost(),port);
                destAddr = new SessionAddress( ipAddr, port);
                mgr.initialize( localAddr);
                
                /*-----------------------------------------------*/
                /* You can try out some other buffer size to see */
                /* if you can get better smoothness.             */
                /*-----------------------------------------------*/
                BufferControl bc = (BufferControl)mgr.getControl(
                                           "javax.media.control.BufferControl");
                if( bc != null )
                    bc.setBufferLength(BUFFER_LENGTH);
                
                mgr.addTarget(destAddr);
                
            } catch (Exception e){
                log.error("Cannot create the RTP Session: "+e.getMessage());
                try{
                    socket.close();
                }
                catch(Exception s){}
                return false;
            
        }
         this.waitForData();
         
        }
       return true;
    }
        /*-------------------------------------------*/
        /* Wait for data to arrive before moving on. */
        /*-------------------------------------------*/
        
        public boolean waitForData(){
        while( !cancel ) {
            
            long then = System.currentTimeMillis();                        
            try{
                synchronized( dataSync ) {
                  while( !cancel ) {
                      if( dataReceived ) break;
                      long elapsed = System.currentTimeMillis() - then;
                      if((elapsed<0)||(elapsed>RTP_SESSION_OPEN_TIMEOUT)) break;                        
                      if( !dataReceived )
                          log.test(">>>Waiting for RTP data to arrive...");
                      dataSync.wait(500);
                  }
                }
            } catch (Exception e) {
                log.error("Unable to receive RTP data - " + e );
            }
            
            if( !dataReceived ) {
                log.warning("Timeout listening for initial RTP data");
                //panel.add(new JLabel("No RTP data was received. Try reconnecting"));                
                //  close();
                return false;
            }            
            try{Thread.currentThread().sleep(100);}
            catch(InterruptedException e){}
        }        
        return true;
    }
    
    /*------------------------------------------------------------------------*/
    /** Close the players and the session managers.                           */
    /*------------------------------------------------------------------------*/
    public void close() throws Exception {
    /*------------------------------------------------------------------------*/

        /*------------------------*/
        /* close the RTP session. */
        /*------------------------*/
        if( socket != null ) {
            try{ socket.close();
            }catch(Exception ss){
                throw new Exception("Couldn't close client socket properly");
            }
        }
        if( mgr != null ) {
            mgr.removeTargets( "Closing session" );
            mgr.dispose();
            mgr = null;
            if(panel.countComponents() != 0){
            //jPanel3.remove(pp);
              panel.removeAll();
              panel.repaint();
            }            
        }        
    }
    
    
       /*------------------------------------------------------------------------*/
    /** Close session only                     */
    /*------------------------------------------------------------------------*/
    public void closeSession() throws Exception {
    /*------------------------------------------------------------------------*/

        if( mgr != null ) {
            mgr.removeTargets( "Closing session" );
            mgr.dispose();
            mgr = null;
            if(panel.countComponents() != 0){
            //jPanel3.remove(pp);
              panel.removeAll();
            }            
        }        
    }
    
    
    
    
    /*------------------------------------------------------------------------*/
    /** SessionListener.
     *  @param evt Event received.                                            */
    /*------------------------------------------------------------------------*/
    public synchronized void update(SessionEvent evt) {
    /*------------------------------------------------------------------------*/
        if (evt instanceof NewParticipantEvent) {
            Participant p = ((NewParticipantEvent)evt).getParticipant();
            log.test("A new participant had just joined: " + p.getCNAME());
        }
    }
    
    
    /*------------------------------------------------------------------------*/
    /** ReceiveStreamListener
     *  @param evt Event received.                                            */
    /*------------------------------------------------------------------------*/
    public synchronized void update( ReceiveStreamEvent evt) {
    /*------------------------------------------------------------------------*/
        RTPManager mgr = (RTPManager)evt.getSource();
        Participant participant = evt.getParticipant(); // could be null.
        ReceiveStream stream = evt.getReceiveStream();  // could be null.
        
        if (evt instanceof RemotePayloadChangeEvent) {
            log.test("Received an RTP PayloadChangeEvent");
            log.test("Sorry, cannot handle payload change");
            cancel = true;
            return;
        }        
        else if( evt instanceof NewReceiveStreamEvent ) {
            
            try {
                stream = ((NewReceiveStreamEvent)evt).getReceiveStream();
                DataSource ds = stream.getDataSource();
                
                /*-----------------------*/
                /* Find out the formats. */
                /*-----------------------*/
                RTPControl ctl = 
                        (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
                if( ctl != null ){
                    log.test("Received new RTP stream: " + ctl.getFormat());
                } else
                    log.test("Received new RTP stream");
                
                if (participant == null)
                    log.test("Sender of stream yet to be identified");
                else {
                    log.test("Stream comes from: " + participant.getCNAME());
                }
                
                /*------------------------------------------------------------*/
                /* create a player by passing datasource to the Media Manager */
                /*------------------------------------------------------------*/
                Player p = javax.media.Manager.createPlayer(ds);
                if (p == null)
                    return;
                p.addControllerListener(this);
                p.realize();

                /*---------------------------------------------------*/
                /* Notify intialize() that a new stream had arrived. */
                /*---------------------------------------------------*/
                synchronized (dataSync) {
                    dataReceived = true;
                    dataSync.notifyAll();
                }
                
            } catch (Exception e) {
                log.error("NewReceiveStreamEvent exception " + e.getMessage());
                return;
            }
            
        }
        else if( evt instanceof StreamMappedEvent ) {
            
            if (stream != null && stream.getDataSource() != null) {
                DataSource ds = stream.getDataSource();
                
                /*-----------------------*/
                /* Find out the formats. */
                /*-----------------------*/
                RTPControl ctl = 
                        (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
                log.test("The previously unidentified stream ");
                if( ctl != null )
                    log.test(""+ctl.getFormat());
                log.test("had now been identified as sent by: " 
                            + participant.getCNAME());
            }
        }
        else if( evt instanceof ByeEvent ) {
            try{
            log.test("Got \"bye\" from: " + participant.getCNAME());
           // if(pp.countComponents()!=0){
             //   panel.remove(pp);
              //  panel.removeAll();
               // panel.repaint();}
            //this.closeSession();
            //this.initialize();
                   dataReceived = false;
                    dataSync.notifyAll();
            this.waitForData();
            }
            catch(Exception rc){log.test("Could not reconnect"+rc.toString());
            
            }
        }
        
    }
    
    /*------------------------------------------------------------------------*/
    /** ControllerListener for the Players.
     *  @param ce Event received.                                             */
    /*------------------------------------------------------------------------*/
    public synchronized void controllerUpdate(ControllerEvent ce) {
    /*------------------------------------------------------------------------*/
        
        Player p = (Player)ce.getSourceController();
        
        if (p == null)
            return;
        
        /*----------------------------------------------------------*/
        /* Get this when the internal players are realized.         */
        /* This is the section that  adds the player  to the panel  */
        /*----------------------------------------------------------*/
        if( ce instanceof RealizeCompleteEvent ) {
            pp = new PlayerPanel(p);
            panel.removeAll();
            panel.add(pp);
            p.start();
        }
        
        if( ce instanceof ControllerErrorEvent ) {
            p.removeControllerListener(this);
            log.error("Internal error: " + ce);
        }
        
    }
    
    public void run() {
        this.initialize();
    }
    
    
    
    /*========================================================================*/
    /** GUI classes for the Player.
    /*========================================================================*/
    class PlayerPanel extends Panel {
    /*========================================================================*/
    Component vc, cc;
        
        /*--------------------------------------------------------------------*/
        /*--------------------------------------------------------------------*/
        PlayerPanel(Player p) {
        /*--------------------------------------------------------------------*/
            setLayout(new BorderLayout());
            if((vc = p.getVisualComponent()) != null)
                add("Center", vc);
          //  if((cc = p.getControlPanelComponent()) != null)
            //    add("South", cc);
            setSize(320,240); // This is "required??"  for repaint
        }
        
    }
    
    
    
}// end of AVReceive2





