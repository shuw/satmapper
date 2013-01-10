/*
 * panelTesting.java
 *
 * Created on July 25, 2001, 5:19 PM
 */

package ca.gc.space.quicksat.ground.control;

import javax.swing.*;
import ca.gc.space.quicksat.ground.client.*;
import ca.gc.space.quicksat.ground.satellite.*;
import ca.gc.space.quicksat.ground.control.*;
import ca.gc.space.quicksat.ground.util.*;

/**
 *
 * @author  jfcusson
 */
public class PanelTesting extends javax.swing.JPanel {
ServerLink  srvLnk               = null;
Satellite   sat                  = null;
Log         log                  = null;

    
    /** Creates new form panelTesting */
    public PanelTesting( Log log ) {
        initComponents();
        this.log = log;                
        if( this.log == null ) this.log = new Log();
    }
    /*========================================================================*/    
    /** Set the object that is in charge of managing the link with the ground
     *  station server.
     *  @param srvLnk ServerLink used to communicate with the server.     */
    /*========================================================================*/
    public void setServerLink( ServerLink srvLnk ) {
    /*========================================================================*/    
        this.srvLnk = srvLnk;
    }
    
    /*========================================================================*/
    /** Set the object representing the satellite that we want to communicate
     *  with, and understanding the format of the commands.
     *  @param sat Satellite object representing our satellite.               */
    /*========================================================================*/
    public void setSatellite( Satellite sat ) {
    /*========================================================================*/    
        this.sat = sat;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
              panelTesting = new javax.swing.JPanel();
              panelTstCalc = new javax.swing.JPanel();
              jPanel92 = new javax.swing.JPanel();
              btnTSTCALCExit = new javax.swing.JButton();
              panelTstCDH = new javax.swing.JPanel();
              jPanel247 = new javax.swing.JPanel();
              btnTstCDHTerminate = new javax.swing.JButton();
              
              setLayout(new java.awt.BorderLayout());
              
              panelTesting.setBackground(new java.awt.Color(204, 204, 255));
              panelTstCalc.setBorder(new javax.swing.border.TitledBorder("TSTCALC"));
              panelTstCalc.setOpaque(false);
              jPanel92.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
              btnTSTCALCExit.setText("Terminate");
              btnTSTCALCExit.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnTSTCALCExitActionPerformed(evt);
                  }
              });
              
              jPanel92.add(btnTSTCALCExit);
              
              panelTstCalc.add(jPanel92);
            
            panelTesting.add(panelTstCalc);
          
          panelTstCDH.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "TSTCDH"));
              panelTstCDH.setOpaque(false);
              jPanel247.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
              btnTstCDHTerminate.setText("Terminate");
              btnTstCDHTerminate.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnTstCDHTerminateActionPerformed(evt);
                  }
              });
              
              jPanel247.add(btnTstCDHTerminate);
              
              panelTstCDH.add(jPanel247);
            
            panelTesting.add(panelTstCDH);
          
          add(panelTesting, java.awt.BorderLayout.CENTER);
        
    }//GEN-END:initComponents

    private void btnTstCDHTerminateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTstCDHTerminateActionPerformed
      /*-----------------------*/
      /* Create the data frame */
      /*-----------------------*/
      //byte[] ldrFrame = new byte[13];
      //ldrFrame[0] = 0;              
      //ldrFrame[1] = (byte)12;       //Size of packet
                                    //without leading 0       
      //ldrFrame[2] = 'X';            //code for ABORT
      //ldrFrame[3] = 0;              //File block counter
      //for( int i=0; i<8; i++ ) {
      //  ldrFrame[i+4] = (byte) 0;
      //}
      //ldrFrame[12] = phtLink.calculateChecksum( ldrFrame, 12 );
      
      /*-------------*/
      /* And send it */
      /*-------------*/
      //phtLink.sendPacket( "COMND", 0x01, "TSTCDH", 0x08, 0x79, ldrFrame );
      JOptionPane.showMessageDialog(this,"This function not implemented");
      
    }//GEN-LAST:event_btnTstCDHTerminateActionPerformed

    private void btnTSTCALCExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTSTCALCExitActionPerformed

      /*-----------------------*/
      /* Create the data frame */
      /*-----------------------*/
      //byte[] ldrFrame = new byte[13];
      //ldrFrame[0] = 0;              //I do not know why this byte is here
      //ldrFrame[1] = (byte)12;       //Size of packet
                                    //without leading 0       
      //ldrFrame[2] = 'X';            //code for ABORT
      //ldrFrame[3] = 0;              //File block counter
      //for( int i=0; i<8; i++ ) {
      //  ldrFrame[i+4] = (byte) 0;
      //}
      //ldrFrame[12] = phtLink.calculateChecksum( ldrFrame, 12 );
      
      /*-------------*/
      /* And send it */
      /*-------------*/
      //phtLink.sendPacket( "COMND", 0x01, "TSTCALC", 0x08, 0x79, ldrFrame );
      JOptionPane.showMessageDialog(this,"This function not implemented");
                                            
    }//GEN-LAST:event_btnTSTCALCExitActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel panelTesting;
    private javax.swing.JPanel panelTstCalc;
    private javax.swing.JPanel jPanel92;
    private javax.swing.JButton btnTSTCALCExit;
    private javax.swing.JPanel panelTstCDH;
    private javax.swing.JPanel jPanel247;
    private javax.swing.JButton btnTstCDHTerminate;
    // End of variables declaration//GEN-END:variables

}
