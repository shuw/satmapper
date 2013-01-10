/*==============================================================================
 * PanelRotator.java
 *
 * Created on January 25, 2002, 3:22 PM
  =============================================================================*/

package ca.gc.space.quicksat.ground.rotator;

import javax.swing.*;
import java.awt.event.*;
import ca.gc.space.quicksat.ground.client.*;
import ca.gc.space.quicksat.ground.video.*;
import ca.gc.space.quicksat.ground.util.*;

/**=============================================================================
 * Panel used to talk to the antenna rotator, via the server. Will also include
 * a live view of the ground station and the antenna.
 * @author  jfcusson
 =============================================================================*/
public class PanelRotator extends javax.swing.JPanel implements Runnable {
/*============================================================================*/
ServerLink  serverLink  = null;
StrmRcver   rcv         = null;
Thread      rtpRcv      = null;
Thread      thread      = null;
boolean     receiving   = false;
boolean     hasReceived = false;
int         current     = 0;
String      currentServer=null;
Log         log         = null;

    /*========================================================================*/
    /** Creates new form PanelRotator                                         */
    /*========================================================================*/
    public PanelRotator( Log log ) {
    /*========================================================================*/    
        initComponents();
        this.log = log;
        if( log == null ) log = new Log();
        this.btnDisconnectVideo.setVisible(false);
    }
    
    /*========================================================================*/
    /** Allow us to define the serverLink object used to communicate with the 
     *  ground station server.
     *  @param serverLink ServerLink object used to communicate with the ground
     *                    station.                                            */
    /*========================================================================*/
    public void setServerLink( ServerLink serverLink ) {
    /*========================================================================*/    
        this.serverLink = serverLink;
    }
    
    /*------------------------------------------------------------------------*/
    /** Executes the thread managing the video stream.                        */
    /*------------------------------------------------------------------------*/
    public void run() {
    /*------------------------------------------------------------------------*/       
        try{
            if(hasReceived){
                System.out.println("sleeping 10");
                rtpRcv.currentThread().sleep(10000);
            }

            while(!detectServerChange() && receiving){
                this.hasReceived=true;
                System.out.println("while");
                this.setCurrentServer(this.serverLink.getCurrentServerAddress());
                if(rcv == null){
                    if(this.currentServer==null || this.currentServer.equals("127.0.0.1")){
                        JLabel noC = new JLabel("Not connected to server");
                        this.jPanel3.add(noC);
                        this.jPanel3.repaint();
                    }
                    else{
                       
                        
                        rcv = new StrmRcver(this.currentServer,this.jPanel3,log);
                        thread = new Thread(rcv,"rcv");
                        thread.start();
                         this.receiving=true;
                    }
                    
                }

          //      System.out.println("waiting 10");
                rtpRcv.currentThread(). sleep(10000); // check every 10 seconds for server change
                
            }
            if(rcv!=null){  //preventing a null pointer exception when trying to close connection before it is connected
            rcv.close();
            }
            thread.stop();
            this.receiving=false;
            rcv=null;

                    this.rtpRcv.stop();
        }
        catch(Exception th){th.printStackTrace();}
        
    }
    
    public void setCurrentServer(String currentServer){
        this.currentServer = currentServer;
    }
    
    public boolean detectServerChange(){
        if(this.serverLink.getCurrentServerAddress().equals(this.currentServer) || this.serverLink.getCurrentServerAddress().equals(null)){
            
            return false;
        }
        else{
            System.out.println("serverLink: "+this.serverLink.getCurrentServerAddress());
            System.out.println("current: "+this.currentServer);
            this.setCurrentServer(this.serverLink.getCurrentServerAddress());
            return true;
        }
        
    }
    
    /*========================================================================*/
    /** Appends the specified message into the main textbox display of this
     *  panel, generally to indicate a message received from the antenna
     *  rotator, and appends a line feed.
     *  @param message The message to display.                                */
    /*========================================================================*/
    public void println( String message ) {
    /*========================================================================*/    
        txtReceived.append(message+"\n");
        txtReceived.setCaretPosition(txtReceived.getText().length());
        txtReceived.revalidate();        
    }
    
    /*========================================================================*/
    /** Appends the specified message into the main textbox display of this
     *  panel, generally to indicate a message received from antenna rotator.
     *  @param message The message to display.                                */
    /*========================================================================*/
    public void print( String message ) {
    /*========================================================================*/    
        txtReceived.append(message);
        txtReceived.setCaretPosition(txtReceived.getText().length());
        txtReceived.revalidate();        
    }
    
    /*========================================================================*/
    /** This method called from within the constructor to initialize the form.
     *  WARNING: Do NOT modify this code. The content of this method is
     *  always regenerated by the Form Editor.                                 */
    /*========================================================================*/
    private void initComponents() {//GEN-BEGIN:initComponents
              jPanel1 = new javax.swing.JPanel();
              jPanel2 = new javax.swing.JPanel();
              jScrollPane1 = new javax.swing.JScrollPane();
              txtReceived = new javax.swing.JTextArea();
              txtToSend = new javax.swing.JTextField();
              jPanel4 = new javax.swing.JPanel();
              btnConnectVideo = new javax.swing.JButton();
              btnDisconnectVideo = new javax.swing.JButton();
              jPanel3 = new javax.swing.JPanel();
              
              jPanel1.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
              jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));
              
              jPanel2.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
              jScrollPane1.setPreferredSize(new java.awt.Dimension(500, 150));
              jScrollPane1.setViewportView(txtReceived);
              
              jPanel2.add(jScrollPane1);
            
            txtToSend.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent evt) {
                    txtToSendKeyTyped(evt);
                }
            });
            
            jPanel2.add(txtToSend);
            
            btnConnectVideo.setText("Connect Video");
              btnConnectVideo.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnConnectVideoActionPerformed(evt);
                  }
              });
              
              jPanel4.add(btnConnectVideo);
              
              btnDisconnectVideo.setText("Disconnect Video");
              btnDisconnectVideo.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnDisconnectVideoActionPerformed(evt);
                  }
              });
              
              jPanel4.add(btnDisconnectVideo);
              
              jPanel2.add(jPanel4);
            
            jPanel1.add(jPanel2);
          
          jPanel3.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
          jPanel3.setPreferredSize(new java.awt.Dimension(330, 250));
          jPanel1.add(jPanel3);
          
          add(jPanel1);
        
    }//GEN-END:initComponents

    private void btnDisconnectVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisconnectVideoActionPerformed
   if(receiving){
         
       this.btnDisconnectVideo.setVisible(false);
      this.btnConnectVideo.setVisible(true);
               this.receiving=false;
       if(rcv!=null){
   try{
        rcv.close();
        rcv=null;
        thread.stop();        
        

        this.rtpRcv.stop();

//long cTime = System.currentTimeMillis();
//while((cTime+10000)>System.currentTimeMillis()){
//do nothing
//}
      JLabel disconnected = new JLabel("Disconnected from server");
                        this.jPanel3.add(disconnected);
                        this.jPanel3.repaint();

           
      }
      catch(Exception c){c.printStackTrace();
        thread.stop();        
        

        this.rtpRcv.stop();
      
      }
       }
   }
    }//GEN-LAST:event_btnDisconnectVideoActionPerformed

    private void btnConnectVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectVideoActionPerformed
    if (!receiving){
                JLabel connecting = new JLabel("Please wait... Connecting to server");
                        this.jPanel3.add(connecting);
                        this.jPanel3.repaint();
        this.btnConnectVideo.setVisible(false);
        this.btnDisconnectVideo.setVisible(true);
                       this.receiving=true;
                      
        this.setCurrentServer(this.serverLink.getCurrentServerAddress());
          rtpRcv = new Thread(this,"RCV");
        rtpRcv.start();
        
    }
    }//GEN-LAST:event_btnConnectVideoActionPerformed

    /*========================================================================*/
    /** Called whenever we type a key in the "to send" text field, and make 
     *  sure we send the message only when a carriage return or a line feed is
     *  typed.
     *  @param evt The event encapsulating the key typed.
    /*========================================================================*/
    private void txtToSendKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtToSendKeyTyped
    /*========================================================================*/
        
        /*-----------------------------------------------------------*/
        /* Do an action only when carriage return or line feed typed */
        /*-----------------------------------------------------------*/
        if( (evt.getKeyChar() == '\n') || (evt.getKeyChar() == '\r') ){
            
            /*---------------------------------------------------------------*/
            /* Check the serverlink, if it is null we do not send anything   */
            /* (because we do not have a link to the ground station server...*/
            /*---------------------------------------------------------------*/
            if( serverLink != null ) {
                serverLink.sendPacketToAntennaRotator( 
                             new String(txtToSend.getText()+"\r").getBytes());
            }
            
            /*---------------------------------------*/
            /* Always clear the text field afterward */
            /*---------------------------------------*/
            txtToSend.setText("");
        }
    }//GEN-LAST:event_txtToSendKeyTyped



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txtReceived;
    private javax.swing.JTextField txtToSend;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton btnConnectVideo;
    private javax.swing.JButton btnDisconnectVideo;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables

}
